# Android Voice Assistant App

Android application with Hindi voice recognition, OpenAI integration, and YouTube transcript analysis.

## Setup

1. Open this folder in Android Studio

2. Update `BASE_URL` in `MainActivity.kt`:
   - **Local testing with emulator**: `http://10.0.2.2:3000/`
   - **Production**: `https://your-backend-domain.com/`

3. Build and run the app

4. Grant microphone permission when prompted

## Features

- **Hindi Voice Input**: Tap "🎤 बोलो (Hindi)" and speak
- **AI Responses**: Get intelligent responses from OpenAI
- **YouTube Analysis**: Paste YouTube links, fetch transcripts, and ask questions
- **Text-to-Speech**: Hear responses in Hindi
- **Stop Voice**: Stop TTS playback anytime

## Requirements

- Android Studio
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34
- Microphone permission

## Dependencies

- Kotlin Standard Library
- AndroidX Core KTX
- Material Design Components
- Retrofit 2 (HTTP client)
- OkHttp Logging Interceptor
- Gson Converter

## Project Structure

```
app/
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/example/voiceassistant/
│   │   └── MainActivity.kt
│   └── res/
│       └── layout/
│           └── activity_main.xml
└── build.gradle
```

## Testing Locally

If running the backend on your local machine:
- Use `http://10.0.2.2:3000/` as BASE_URL for Android emulator
- Use `http://YOUR_LOCAL_IP:3000/` for physical device on same network

## Permissions

The app requires:
- `INTERNET` - For API calls
- `RECORD_AUDIO` - For voice recognition