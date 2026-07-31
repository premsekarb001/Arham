# 🎓 Arham (अर्हम्) - The Rightful Merit 

[![Flutter](https://img.shields.io/badge/Flutter-02569B?style=for-the-badge&logo=flutter&logoColor=white)](https://flutter.dev/)
[![Supabase](https://img.shields.io/badge/Supabase-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white)](https://supabase.io/)
[![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![PostgreSQL](https://img.shields.io/badge/postgresql-4169e1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)

**Arham** is a real-time, cross-state "Clearing" marketplace built specifically for the Indian higher education system. Inspired by the UK's UCAS system, Arham connects unplaced students directly with last-minute vacant seats at private and deemed universities across India, bypassing agent fees and state-quota politics to ensure transparent, rightful admission.

---

## 🛑 The Problem

Every year in India, top students secure and hoard seats in multiple colleges. When they drop their backup seats at the last minute, state-level counseling rounds have already closed. 
* **For Universities:** Thousands of seats (especially in Tier-1/Tier-2 private universities) go vacant for four years, resulting in massive revenue loss.
* **For Students:** Deserving out-of-state students who missed initial cutoffs have no centralized way to discover or apply for these newly vacated seats.

## 💡 The Solution

Arham acts as a real-time clearinghouse. Once official counseling ends, universities list their vacant seats on Arham. Students can browse pan-India vacancies, verify their academic scores via **DigiLocker**, and claim seats instantly based on a unified merit-normalization algorithm.

---

## ✨ Key Features

* **Real-Time Vacancy Feed:** A dynamic, searchable feed of vacant college seats filtered by state, course, and availability.
* **Automated Score Normalization:** An algorithmic engine that normalizes disparate Indian state board scores (CBSE, UP Board, Bihar Board) and CUET percentiles to establish fair, cross-state eligibility.
* **DigiLocker Integration:** 100% verified student profiles. Class 10/12 marksheets and identity are pulled directly from government APIs, eliminating fake profiles.
* **Atomic Seat Locking (High Concurrency):** Utilizes Redis to prevent race conditions during "clearing rushes," ensuring that if 500 students try to claim the last 3 seats at the same millisecond, the system resolves it fairly without double-booking.
* **One-Tap UPI Payments:** Native Razorpay integration for instant application fee payments to lock the seat.

---

## 🏗️ System Architecture & Tech Stack

### Frontend (Mobile App)
* **Framework:** Flutter (Dart)
* **Design Pattern:** Clean Architecture (Domain, Data, Presentation layers)
* **State Management:** Riverpod
* **Routing:** GoRouter
* **UI:** Material 3 Design

### Backend & Database
* **BaaS:** Supabase
* **Database:** PostgreSQL with strict Row Level Security (RLS) policies.
* **Auth:** Supabase Auth (Phone OTP).
* **Realtime:** Supabase Realtime subscriptions for instant seat vacancy updates.

### Concurrency & Microservices
* **Seat Reservation Engine:** Upstash Redis + TypeScript Edge Functions. Implements Lua scripting for atomic seat decrements and 15-minute TTL locks while the user completes payment.
* **Notifications:** Firebase Cloud Messaging (FCM) for urgent push alerts when new cross-state vacancies open.

---

## 📂 Project Structure

```text
lib/
 ┣ core/               # App-wide constants, themes, and network clients
 ┣ data/               # Repositories, API calls, and Supabase integrations
 ┣ domain/             # Business logic, models, and normalization algorithms
 ┣ presentation/       # UI screens, widgets, and Riverpod controllers
 ┃ ┣ feed/             # Live Vacancy Feed screen & components
 ┃ ┣ profile/          # DigiLocker verification flow
 ┃ ┗ application/      # Seat claiming and Razorpay payment flow
 ┗ main.dart           # App entry point

```

---

## 🚀 Getting Started

### Prerequisites

* Flutter SDK (v3.19+)
* Supabase Account & CLI
* Redis Instance (Upstash recommended)
* DigiLocker API Credentials

### Installation

1. **Clone the repository:**
```bash
git clone [https://github.com/yourusername/arham-clearing-app.git](https://github.com/yourusername/arham-clearing-app.git)
cd arham-clearing-app

```


2. **Install dependencies:**
```bash
flutter pub get

```


3. **Environment Setup:**
Create a `.env` file in the root directory and add your keys:
```env
SUPABASE_URL=your_supabase_url
SUPABASE_ANON_KEY=your_supabase_anon_key
RAZORPAY_KEY=your_razorpay_key
DIGILOCKER_CLIENT_ID=your_client_id

```


4. **Run the app:**
```bash
flutter run

```



---

## 📸 Screenshots
<img width="385" height="577" alt="Screenshot 2026-07-31 075948" src="https://github.com/user-attachments/assets/b65df91d-4966-48cf-a508-f2c1e2a3392f" />
<img width="471" height="581" alt="image" src="https://github.com/user-attachments/assets/53162411-e0d5-4911-b215-f7c555cacb1a" />
<img width="438" height="572" alt="image" src="https://github.com/user-attachments/assets/4fc30768-e296-4115-9435-74933f3fa887" />

<img width="460" height="577" alt="image" src="https://github.com/user-attachments/assets/d0fa5019-790d-4880-a26d-afd7eb86e907" />


---

## 🤝 Contributing

This project is built to solve a massive structural issue in Indian education. If you are an EdTech developer, founder, or university admin, I welcome pull requests, architectural feedback, and feature suggestions.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📩 Contact

**[Premsekar Baskaran]**

*Lead Developer & Architect*

[LinkedIn Profile] https://www.linkedin.com/in/premsekar-baskaran/
Project Link: https://github.com/premsekarb001/Arham.git
