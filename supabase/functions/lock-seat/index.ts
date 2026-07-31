import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { Redis } from "https://esm.sh/@upstash/redis";

// Initialize Upstash Redis client
const redis = new Redis({
  url: Deno.env.get('UPSTASH_REDIS_REST_URL')!,
  token: Deno.env.get('UPSTASH_REDIS_REST_TOKEN')!,
});

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
};

serve(async (req) => {
  // Handle CORS preflight requests
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders });
  }

  try {
    // Initialize Supabase Client with Service Role for bypass RLS in backend logic
    const supabaseClient = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    );

    const { seatId, studentId } = await req.json();

    if (!seatId || !studentId) {
      return new Response(
        JSON.stringify({ error: 'Missing seatId or studentId in request body.' }), 
        { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      );
    }

    // 1. Atomic Lua script to prevent race conditions on seat availability
    // This script checks if available seats > 0. If so, it decrements the available count 
    // and creates a lock key with a 10-minute expiration (600 seconds).
    // Using Lua ensures these operations are evaluated atomically in Redis.
    const script = `
      local available = tonumber(redis.call('GET', KEYS[1]) or '0')
      if available > 0 then
        redis.call('DECR', KEYS[1])
        redis.call('SET', KEYS[2], ARGV[1], 'EX', 600)
        return 1
      else
        return 0
      end
    `;

    const availableKey = `seat_inventory:${seatId}:available`;
    const lockKey = `seat_lock:${seatId}:${studentId}`;
    
    // Evaluate the atomic script
    const lockResult = await redis.eval(
      script,
      [availableKey, lockKey],
      [Date.now().toString()]
    );

    if (lockResult === 0) {
      return new Response(
        JSON.stringify({ success: false, error: 'No seats available or all seats are currently locked by other users.' }), 
        { status: 409, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      );
    }

    // 2. Lock Acquired! Now reflect this state in PostgreSQL via Supabase.
    // Create an application with status 'payment_pending' and a 10 min expiration timestamp.
    const lockExpiresAt = new Date(Date.now() + 10 * 60 * 1000).toISOString();
    
    const { data: application, error: dbError } = await supabaseClient
      .from('applications')
      .insert({
        student_id: studentId,
        seat_id: seatId,
        status: 'payment_pending',
        lock_expires_at: lockExpiresAt
      })
      .select()
      .single();

    if (dbError) {
      // Rollback Redis state if the DB insert fails to maintain consistency
      await redis.incr(availableKey);
      await redis.del(lockKey);
      throw new Error(`Database error while creating application: ${dbError.message}`);
    }

    // 3. Atomically update the seat_inventory in Postgres to sync with Redis
    // Assuming an RPC function 'increment_locked_seats' exists to prevent Postgres race conditions:
    // UPDATE seat_inventory SET available_seats = available_seats - 1, locked_seats = locked_seats + 1 WHERE id = target_seat_id;
    const { error: rpcError } = await supabaseClient.rpc('increment_locked_seats', { 
        target_seat_id: seatId 
    });

    if (rpcError) {
        console.error('Failed to sync locked_seats to Postgres:', rpcError);
        // We don't fail the request here because the Redis lock and Application record are secure.
    }

    return new Response(
      JSON.stringify({ 
          success: true, 
          message: 'Seat locked successfully for 10 minutes. Proceed to Razorpay payment.',
          application 
      }), 
      {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      }
    );

  } catch (error: any) {
    return new Response(
      JSON.stringify({ error: error.message }), 
      { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
    );
  }
});
