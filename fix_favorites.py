import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add sharedPrefs and favoriteVacancyIds
content = content.replace(
    'val context = LocalContext.current\n    val firebaseAuthManager = remember { FirebaseAuthManager(context) }',
    '''val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("arham_prefs", android.content.Context.MODE_PRIVATE) }
    var favoriteVacancyIds by remember {
        mutableStateOf(sharedPrefs.getStringSet("favorite_vacancies", emptySet())?.toSet() ?: emptySet())
    }
    val firebaseAuthManager = remember { FirebaseAuthManager(context) }'''
)

# Update VacancyCardItem call in list
call_pattern = '''                                        isSelectedForCompare = selectedForComparison.contains(item),
                                        onToggleCompare = { isSelected ->'''
call_replacement = '''                                        isSelectedForCompare = selectedForComparison.contains(item),
                                        isFavorite = favoriteVacancyIds.contains(item.id),
                                        onToggleFavorite = { isFav ->
                                            val newFavorites = if (isFav) favoriteVacancyIds + item.id else favoriteVacancyIds - item.id
                                            favoriteVacancyIds = newFavorites
                                            sharedPrefs.edit().putStringSet("favorite_vacancies", newFavorites).apply()
                                            
                                            val topic = "vacancy_${item.id}"
                                            if (isFav) {
                                                com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic(topic)
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) Toast.makeText(context, "Subscribed to ${item.courseName} alerts!", Toast.LENGTH_SHORT).show()
                                                    }
                                            } else {
                                                com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) Toast.makeText(context, "Unsubscribed from alerts", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                        },
                                        onToggleCompare = { isSelected ->'''
content = content.replace(call_pattern, call_replacement)

# Update VacancyCardItem definition
def_pattern = '''    isSelectedForCompare: Boolean = false,
    onToggleCompare: (Boolean) -> Unit = {}'''
def_replacement = '''    isSelectedForCompare: Boolean = false,
    isFavorite: Boolean = false,
    onToggleFavorite: (Boolean) -> Unit = {},
    onToggleCompare: (Boolean) -> Unit = {}'''
content = content.replace(def_pattern, def_replacement)

# Update UI logic in VacancyCardItem
ui_pattern = '''                        var isSubscribed by remember { mutableStateOf(false) }
                        val context = LocalContext.current
                        
                        IconButton(
                            onClick = { 
                                isSubscribed = !isSubscribed
                                val topic = "vacancy_${vacancy.id}"
                                if (isSubscribed) {
                                    com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic(topic)
                                        .addOnCompleteListener { task ->
                                            var msg = "Subscribed to ${vacancy.courseName} alerts!"
                                            if (!task.isSuccessful) msg = "Subscription failed"
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                                        .addOnCompleteListener { task ->
                                            var msg = "Unsubscribed from alerts"
                                            if (!task.isSuccessful) msg = "Unsubscription failed"
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isSubscribed) Icons.Filled.NotificationsActive else Icons.Outlined.Notifications,
                                contentDescription = "Subscribe to alerts",
                                tint = if (isSubscribed) ArhamPrimary else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }'''
ui_replacement = '''                        IconButton(
                            onClick = { onToggleFavorite(!isFavorite) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.NotificationsActive else Icons.Outlined.Notifications,
                                contentDescription = "Subscribe to alerts",
                                tint = if (isFavorite) ArhamPrimary else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }'''
content = content.replace(ui_pattern, ui_replacement)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
