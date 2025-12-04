package com.example.voiceassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var tts: TextToSpeech

    private lateinit var chatContainer: LinearLayout
    private lateinit var btnMic: Button
    private lateinit var btnGetTranscript: Button
    private lateinit var btnAskTranscript: Button
    private lateinit var youtubeInput: EditText
    private lateinit var btnStopTTS: Button

    private val BASE_URL = "https://YOUR_BACKEND_DOMAIN_OR_IP/"

    private lateinit var api: BackendApi
    private var videoTranscriptCache: String? = null

    companion object {
        private const val REQUEST_RECORD_AUDIO = 101
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chatContainer = findViewById(R.id.chatContainer)
        btnMic = findViewById(R.id.btnMic)
        btnGetTranscript = findViewById(R.id.btnGetTranscript)
        btnAskTranscript = findViewById(R.id.btnAskTranscript)
        youtubeInput = findViewById(R.id.youtubeLinkInput)
        btnStopTTS = findViewById(R.id.btnStopTTS)

        tts = TextToSpeech(this, this)

        checkAudioPermission()

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(BackendApi::class.java)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        btnMic.setOnClickListener { startListening() }

        btnStopTTS.setOnClickListener { if (tts.isSpeaking) tts.stop() }

        btnGetTranscript.setOnClickListener {
            val link = youtubeInput.text.toString().trim()
            if (link.isNotEmpty()) {
                val videoId = extractYoutubeId(link)
                if (videoId != null) fetchTranscript(videoId) else appendChat("System", "Invalid YouTube link")
            } else appendChat("System", "YouTube link डालें")
        }

        btnAskTranscript.setOnClickListener {
            val transcript = videoTranscriptCache
            if (transcript == null) appendChat("System", "पहले Get Transcript दबाएँ")
            else promptUserForQuestionAndAsk(transcript)
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "अब बोलिए...")
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) { appendChat("System", "Speech error: $error") }
            override fun onResults(results: Bundle) {
                val data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!data.isNullOrEmpty()) {
                    val text = data[0]
                    appendChat("User", text)
                    sendQueryToBackend(text)
                }
            }
        })
        speechRecognizer.startListening(intent)
    }

    private fun sendQueryToBackend(text: String) {
        appendChat("System", "AI से पूछ रहे हैं...")
        val req = AskRequest(prompt = text)
        api.ask(req).enqueue(object: Callback<AskResponse>{
            override fun onResponse(call: Call<AskResponse>, response: Response<AskResponse>) {
                if (response.isSuccessful && response.body()!=null) {
                    val ans = response.body()!!.answer
                    appendChat("AI", ans)
                    speakOut(ans)
                } else appendChat("System", "AI response failed: ${response.code()}")
            }
            override fun onFailure(call: Call<AskResponse>, t: Throwable) { appendChat("System", "Network error: ${t.localizedMessage}") }
        })
    }

    private fun fetchTranscript(videoId: String) {
        appendChat("System", "Fetching transcript...")
        api.getTranscript(videoId).enqueue(object: Callback<TranscriptResponse>{
            override fun onResponse(call: Call<TranscriptResponse>, response: Response<TranscriptResponse>) {
                if (response.isSuccessful && response.body()!=null) {
                    val transcript = response.body()!!.transcript
                    videoTranscriptCache = transcript
                    appendChat("System", "Transcript loaded (length: ${transcript.length} chars)")
                    appendChat("Transcript", transcript.take(800) + if (transcript.length>800) "..." else "")
                } else appendChat("System", "Transcript unavailable")
            }
            override fun onFailure(call: Call<TranscriptResponse>, t: Throwable) { appendChat("System", "Transcript fetch error: ${t.localizedMessage}") }
        })
    }

    private fun promptUserForQuestionAndAsk(transcript: String) {
        appendChat("System", "अब वीडियो के बारे में अपना सवाल बोलिए...")
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) { appendChat("System", "Speech error: $error") }
            override fun onResults(results: Bundle) {
                val data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!data.isNullOrEmpty()) {
                    val q = data[0]
                    appendChat("User", q)
                    askAboutTranscript(q, transcript)
                }
            }
        })
        speechRecognizer.startListening(intent)
    }

    private fun askAboutTranscript(question: String, transcript: String) {
        appendChat("System", "AI से पूछ रहे हैं (video)...")
        val req = AskWithContextRequest(prompt = question, context = transcript)
        api.askWithContext(req).enqueue(object : Callback<AskResponse> {
            override fun onResponse(call: Call<AskResponse>, response: Response<AskResponse>) {
                if (response.isSuccessful && response.body()!=null) {
                    val ans = response.body()!!.answer
                    appendChat("AI", ans)
                    speakOut(ans)
                } else appendChat("System", "AI failed: ${response.code()}")
            }
            override fun onFailure(call: Call<AskResponse>, t: Throwable) { appendChat("System", "Network error: ${t.localizedMessage}") }
        })
    }

    private fun appendChat(who: String, text: String) {
        runOnUiThread {
            val tv = android.widget.TextView(this)
            tv.text = "$who: $text"
            tv.setPadding(6,6,6,6)
            chatContainer.addView(tv)
            val scroll = findViewById<android.widget.ScrollView>(R.id.scrollView)
            scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun extractYoutubeId(link: String): String? {
        return try {
            when {
                link.contains("youtu.be/") -> link.substringAfter("youtu.be/").substringBefore('?')
                link.contains("watch?v=") -> link.substringAfter("watch?v=").substringBefore('&')
                else -> null
            }
        } catch (e: Exception) { null }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("hi","IN")
        } else Log.e(TAG, "TTS init failed")
    }

    private fun speakOut(text: String) { tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AI_RESP") }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
        speechRecognizer.destroy()
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
        }
    }

    interface BackendApi {
        @POST("ask")
        fun ask(@Body req: AskRequest): Call<AskResponse>

        @POST("ask_with_context")
        fun askWithContext(@Body req: AskWithContextRequest): Call<AskResponse>

        @GET("transcript")
        fun getTranscript(@Query("videoId") videoId: String): Call<TranscriptResponse>
    }

    data class AskRequest(val prompt: String)
    data class AskWithContextRequest(val prompt: String, val context: String)
    data class AskResponse(val answer: String)
    data class TranscriptResponse(val transcript: String)
}