require('dotenv').config();
const express = require('express');
const cors = require('cors');
const axios = require('axios');

// Try to import the youtube-transcript package; if not available you will need to swap packages
let getTranscript = null;
try {
  getTranscript = require('youtube-transcript').default;
} catch (e) {
  try { getTranscript = require('youtube-transcript'); } catch (err) { getTranscript = null; }
}

const app = express();
app.use(cors());
app.use(express.json());

const OPENAI_API_KEY = process.env.OPENAI_API_KEY;
const PORT = process.env.PORT || 3000;

if (!OPENAI_API_KEY) {
  console.error('ERROR: OPENAI_API_KEY not set in .env');
  process.exit(1);
}

async function callOpenAI(prompt) {
  const url = 'https://api.openai.com/v1/chat/completions';
  const data = {
    model: 'gpt-4o-mini',
    messages: [{ role: 'user', content: prompt }],
    max_tokens: 700,
    temperature: 0.2
  };
  const headers = { Authorization: `Bearer ${OPENAI_API_KEY}` };
  const resp = await axios.post(url, data, { headers });
  const content = resp.data?.choices?.[0]?.message?.content;
  return content || 'No answer from model.';
}

app.post('/ask', async (req, res) => {
  try {
    const { prompt } = req.body;
    if (!prompt) return res.status(400).json({ error: 'prompt required' });
    const answer = await callOpenAI(prompt);
    res.json({ answer });
  } catch (e) {
    console.error(e?.response?.data || e.message || e);
    res.status(500).json({ error: e.message || 'server error' });
  }
});

app.post('/ask_with_context', async (req, res) => {
  try {
    const { prompt, context } = req.body;
    if (!prompt || !context) return res.status(400).json({ error: 'prompt and context required' });
    const fullPrompt = `आपके पास निम्नलिखित संदर्भ (context) है:\n${context}\n\nप्रश्न: ${prompt}\nकृपया संक्षेप में (Hindi) उत्तर दें।`;
    const answer = await callOpenAI(fullPrompt);
    res.json({ answer });
  } catch (e) {
    console.error(e?.response?.data || e.message || e);
    res.status(500).json({ error: e.message || 'server error' });
  }
});

app.get('/transcript', async (req, res) => {
  try {
    const videoId = req.query.videoId;
    if (!videoId) return res.status(400).json({ error: 'videoId required' });

    if (!getTranscript) return res.status(500).json({ error: 'transcript library not installed' });

    // try common languages first
    let parts = null;
    try {
      parts = await getTranscript(videoId);
    } catch (e) {
      try { parts = await getTranscript(videoId, 'en'); } catch (e2) { parts = null; }
    }

    if (!parts || parts.length === 0) return res.status(404).json({ transcript: '' });

    const transcript = parts.map(p => p.text).join(' ');
    res.json({ transcript });
  } catch (e) {
    console.error(e?.response?.data || e.message || e);
    res.status(500).json({ error: e.message || 'server error' });
  }
});

app.listen(PORT, () => console.log(`Server running on port ${PORT}`));