# Voice Assistant Backend

Node.js Express server that acts as a proxy for OpenAI API calls and YouTube transcript fetching.

## Setup

1. Copy `.env.example` to `.env` and add your OpenAI API key:
   ```bash
   cp .env.example .env
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the server:
   ```bash
   npm start
   ```

## Endpoints

### POST /ask
Send a prompt to OpenAI and get a response.

**Request:**
```json
{
  "prompt": "Your question here"
}
```

**Response:**
```json
{
  "answer": "AI response"
}
```

### POST /ask_with_context
Ask questions with additional context (used for YouTube transcript analysis).

**Request:**
```json
{
  "prompt": "Your question",
  "context": "Additional context or transcript"
}
```

**Response:**
```json
{
  "answer": "AI response based on context"
}
```

### GET /transcript
Fetch YouTube video transcript.

**Query Parameters:**
- `videoId`: YouTube video ID

**Response:**
```json
{
  "transcript": "Full video transcript text"
}
```

## Deployment

For production deployment:

1. **Render**: Deploy as a web service
2. **Heroku**: Use Heroku CLI
3. **Vercel**: Deploy as serverless functions
4. **VPS**: Any Linux server with Node.js

**Important**: Ensure HTTPS is enabled for production use.

## Environment Variables

- `OPENAI_API_KEY`: Your OpenAI API key (required)
- `PORT`: Server port (default: 3000)

## Notes

If `youtube-transcript` package fails, you can replace it with alternatives like `youtube-captions-scraper` or implement a custom transcript fetcher.