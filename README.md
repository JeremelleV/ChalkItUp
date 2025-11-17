# ChalkItUp — Android Tutoring App
![Android](https://img.shields.io/badge/Platform-Android-brightgreen?logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin)
![Firebase](https://img.shields.io/badge/Backend-Firebase-F5820D?logo=firebase)
![Status](https://img.shields.io/badge/Backend-Disabled-red)
![License](https://img.shields.io/badge/License-MIT-blue)

ChalkItUp is an Android tutoring application originally developed as a university group project.  
This repository contains my public version. All Firebase backend credentials have been removed for security.  
Backend-dependent features (login, database reads, booking, messaging) will not function in this version.

A full showcase video is included to demonstrate how the complete app works.

---

## App Showcase Video

### Click the thumbnail to watch:

<a href="https://github.com/JeremelleV/ChalkItUp/releases/tag/showcase-v1">
  <img src="https://github.com/JeremelleV/ChalkItUp/blob/main/app/src/main/assets/walkingChalkGif.gif" width="200">
</a>

The showcase includes:

- Home screen and Appointments
- Profiles, Messaging, Notifications
- Booking for Students
- Availability for Tutors
- Report/Admin system
- App navigation and UI/UX
- Overall design and functionality

Since the backend is disabled in this public repo, the video demonstrates the full functionality of the original private build.

---

## Features

### Home Screen and Appointments
- Central hub for navigation  
- Students can view upcoming tutoring appointments  
- Tutors can see scheduled sessions  

### User Profiles 
- Tutor information  
- Subjects, availability, ratings  
- Clean, readable layout  

### Booking 
- Automatic Matching with Tutors
- Highlighted in the showcase video

### Availability for Tutors 
- Tutors can set their availability
- Students can book based on real tutor availability  

### Messaging 
- Real-time Firestore chat  
- Not functional in the public repo

### Notifications 
- Real-time notifications for requests, updates, and approvals  

### UI/UX
- Material Design  
- Modern Android components  
- Responsive layouts  

---

## Backend Status

This public version does not include:
- `google-services.json`
- Firebase Authentication setup
- Firestore configuration
- Cloud Storage keys

As a result:
- Login will not work  
- Database features will not load  
- Messaging and booking cannot function  

To run the full version, you must configure your own Firebase project.

---

## Tech Stack

**Mobile**
- Kotlin  
- Android Studio (Gradle)  
- MVVM architecture  
- LiveData + ViewModel  
- RecyclerView  
- Material Components  

**Backend (removed in this version)**
- Firebase Authentication  
- Cloud Firestore  
- Firebase Cloud Storage  

---

## Credits

Originally created by a university development team.  
This is my personal, cleaned public version, shared with full approval from collaborators.  
All sensitive backend components have been removed.
