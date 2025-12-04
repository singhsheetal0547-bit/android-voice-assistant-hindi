# Android Voice Assistant (Hindi)

A complete Android voice assistant application with Hindi language support, OpenAI integration, and YouTube transcript analysis capabilities.

## Project Structure

```
voice-assistant-project/
├─ backend/              # Node.js Express server
│  ├─ package.json
│  ├─ server.js
│  ├─ .env.example
│  └─ README.md
└─ android-app/          # Android Kotlin application
   ├─ app/
   │  ├─ src/main/AndroidManifest.xml
   │  ├─ src/main/res/layout/activity_main.xml
   │  └─ src/main/java/com/example/voiceassistant/MainActivity.kt
   ├─ build.gradle
   └─ README.md
```

## Features

- 🎤 **Hindi Voice Recognition** - Speak in Hindi and get responses
- 🤖 **OpenAI Integration** - Powered by GPT-4o-mini for intelligent responses
- 📺 **YouTube Transcript Analysis** - Extract and analyze YouTube video transcripts
- 🔊 **Text-to-Speech** - Hindi voice output for AI responses
- 💬 **Chat Interface** - Simple scrollable chat UI

## Quick Start

### Backend Setup

1. Navigate to backend directory:
   ```bash
   cd backend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Create `.env` file from example:
   ```bash
   cp .env.example .env
   ```

4. Add your OpenAI API key to `.env`:
   ```
   OPENAI_API_KEY=sk-your-key-here
   PORT=3000
   ```

5. Start the server:
   ```bash
   npm start
   ```

### Android App Setup

1. Open `android-app` folder in Android Studio

2. Update `BASE_URL` in `MainActivity.kt`:
   - For local testing with emulator: `http://10.0.2.2:3000/`
   - For production: `https://your-backend-domain.com/`

3. Build and run the app

4. Grant microphone permission when prompted

## Usage

1. **Voice Chat**: Tap the "🎤 बोलो (Hindi)" button and speak in Hindi
2. **YouTube Analysis**: 
   - Paste a YouTube link in the input field
   - Tap "Get Transcript" to fetch the transcript
   - Tap "Ask About Video" and ask questions about the video content
3. **Stop Voice**: Tap "Stop Voice" to stop TTS playback

## API Endpoints

- `POST /ask` - Send a prompt to OpenAI
- `POST /ask_with_context` - Ask questions with context (for YouTube transcripts)
- `GET /transcript?videoId=VIDEO_ID` - Fetch YouTube transcript

## Deployment

### Backend Deployment Options

- **Render**: Deploy as a web service
- **Heroku**: Use Heroku CLI to deploy
- **Vercel**: Deploy as serverless functions
- **VPS**: Any Linux server with Node.js

Ensure HTTPS is enabled for production.

## Requirements

### Backend
- Node.js 14+
- OpenAI API key

### Android
- Android Studio
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34

## Dependencies

### Backend
- express
- axios
- cors
- dotenv
- youtube-transcript

### Android
- Kotlin
- Retrofit 2
- OkHttp
- Material Design Components

## License

MIT

## Author

Created by Sheetal Singh