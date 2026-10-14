package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CartoonAudioEngine
import com.example.model.CartoonBgmTrack
import com.example.model.CartoonSfx
import com.example.model.CartoonStyle
import com.example.model.SampleVideoPreset
import com.example.model.SocialPlatform
import com.example.model.SubtitleCue
import com.example.model.SubtitlePosition
import com.example.model.SubtitleStyle
import com.example.video.CartoonVideoEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * State representing the active ToonMaker studio session.
 */
data class ToonStudioUiState(
    // Video
    val uploadedVideoUri: Uri? = null,
    val selectedPreset: SampleVideoPreset = CartoonVideoEngine.samplePresets[0],
    val isPlaying: Boolean = true,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 12000L,

    // Cartoon Style & Transformation
    val activeStyle: CartoonStyle = CartoonStyle.COMIC_BOOK,
    val outlineStrength: Float = CartoonStyle.COMIC_BOOK.outlineStrength,
    val posterizeLevels: Int = CartoonStyle.COMIC_BOOK.posterizeLevels,
    val saturationBoost: Float = CartoonStyle.COMIC_BOOK.saturationBoost,
    val contrast: Float = CartoonStyle.COMIC_BOOK.contrast,
    val brightness: Float = 0.0f,
    val isSplitComparison: Boolean = false,
    val splitRatio: Float = 0.5f, // 0.0 to 1.0

    // Audio & BGM
    val activeBgmTrack: CartoonBgmTrack = CartoonBgmTrack.PLAYFUL_BOUNCE,
    val isBgmEnabled: Boolean = true,
    val isOriginalVideoMuted: Boolean = false,
    val bgmVolume: Float = 0.75f,
    val originalVideoVolume: Float = 0.80f,
    val voiceSfxVolume: Float = 0.90f,
    val voicePitch: Float = 1.6f, // 1.8 = Chipmunk, 1.1 = Normal, 0.6 = Giant
    val voiceNarrativeText: String = "হি হি! দেখো কেমন মজার কার্টুন ভিডিও তৈরি হয়ে গেল!",

    // Subtitles
    val subtitles: List<SubtitleCue> = CartoonVideoEngine.samplePresets[0].defaultSubtitles,
    val subtitleStyle: SubtitleStyle = SubtitleStyle.CARTOON_YELLOW,
    val subtitlePosition: SubtitlePosition = SubtitlePosition.BOTTOM,
    val subtitleFontSizeSp: Int = 20,
    val isAutoSubtitlesGenerating: Boolean = false,

    // Social Sharing & Modal
    val isShareModalVisible: Boolean = false,
    val shareAspectRatio: String = "9:16", // "9:16", "1:1", "16:9"
    val shareCaption: String = "✨ আমার সাধারণ ভিডিও এখন মজার কার্টুন ভিডিওতে রূপান্তরিত! সাথে দারুণ কার্টুন সাউন্ড ও সাবটাইটেল! #ToonMaker #CartoonVideo #Animation #AIStudio #কার্টুনভিডিও"
)

class ToonStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ToonStudioUiState())
    val uiState: StateFlow<ToonStudioUiState> = _uiState.asStateFlow()

    val audioEngine = CartoonAudioEngine(application)

    private var playbackJob: Job? = null

    init {
        startPlaybackTicker()
        if (_uiState.value.isBgmEnabled) {
            audioEngine.startBgm(_uiState.value.activeBgmTrack)
        }
    }

    private fun startPlaybackTicker() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (true) {
                delay(33) // ~30 FPS clock
                if (_uiState.value.isPlaying) {
                    val current = _uiState.value.currentPositionMs
                    val total = _uiState.value.totalDurationMs
                    val next = if (current + 33 >= total) 0L else current + 33
                    _uiState.value = _uiState.value.copy(currentPositionMs = next)
                }
            }
        }
    }

    fun togglePlayPause() {
        val nextState = !_uiState.value.isPlaying
        _uiState.value = _uiState.value.copy(isPlaying = nextState)
        if (nextState && _uiState.value.isBgmEnabled) {
            audioEngine.startBgm(_uiState.value.activeBgmTrack)
        } else if (!nextState) {
            audioEngine.stopBgm()
        }
    }

    fun seekTo(positionMs: Long) {
        val bounded = positionMs.coerceIn(0L, _uiState.value.totalDurationMs)
        _uiState.value = _uiState.value.copy(currentPositionMs = bounded)
    }

    /**
     * Set a user-uploaded video from gallery.
     */
    fun setUploadedVideo(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            uploadedVideoUri = uri,
            currentPositionMs = 0L,
            totalDurationMs = 15000L,
            isPlaying = true
        )
        // Auto-generate fresh cartoon subtitles for the uploaded video
        generateAutoSubtitlesForVideo()
    }

    /**
     * Switch to one of the built-in sample presets.
     */
    fun selectSamplePreset(preset: SampleVideoPreset) {
        _uiState.value = _uiState.value.copy(
            uploadedVideoUri = null,
            selectedPreset = preset,
            totalDurationMs = preset.durationMs,
            currentPositionMs = 0L,
            subtitles = preset.defaultSubtitles,
            voiceNarrativeText = preset.defaultDialogueBn,
            isPlaying = true
        )
    }

    // --- CARTOON STYLE & TRANSFORMATION ---

    fun setStyle(style: CartoonStyle) {
        _uiState.value = _uiState.value.copy(
            activeStyle = style,
            outlineStrength = style.outlineStrength,
            posterizeLevels = style.posterizeLevels,
            saturationBoost = style.saturationBoost,
            contrast = style.contrast
        )
    }

    fun updateOutlineStrength(value: Float) {
        _uiState.value = _uiState.value.copy(outlineStrength = value)
    }

    fun updatePosterizeLevels(value: Int) {
        _uiState.value = _uiState.value.copy(posterizeLevels = value)
    }

    fun updateSaturation(value: Float) {
        _uiState.value = _uiState.value.copy(saturationBoost = value)
    }

    fun updateContrast(value: Float) {
        _uiState.value = _uiState.value.copy(contrast = value)
    }

    fun updateBrightness(value: Float) {
        _uiState.value = _uiState.value.copy(brightness = value)
    }

    fun toggleSplitComparison() {
        _uiState.value = _uiState.value.copy(isSplitComparison = !_uiState.value.isSplitComparison)
    }

    fun updateSplitRatio(ratio: Float) {
        _uiState.value = _uiState.value.copy(splitRatio = ratio.coerceIn(0.05f, 0.95f))
    }

    // --- AUDIO & VOICE & SFX ---

    fun setBgmTrack(track: CartoonBgmTrack) {
        _uiState.value = _uiState.value.copy(activeBgmTrack = track)
        if (_uiState.value.isBgmEnabled && _uiState.value.isPlaying) {
            audioEngine.startBgm(track)
        }
    }

    fun toggleBgm(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isBgmEnabled = enabled)
        if (enabled && _uiState.value.isPlaying) {
            audioEngine.startBgm(_uiState.value.activeBgmTrack)
        } else {
            audioEngine.stopBgm()
        }
    }

    fun toggleMuteOriginalVideo(muted: Boolean) {
        _uiState.value = _uiState.value.copy(isOriginalVideoMuted = muted)
    }

    fun updateBgmVolume(volume: Float) {
        _uiState.value = _uiState.value.copy(bgmVolume = volume)
        audioEngine.bgmVolume = volume
    }

    fun updateOriginalVolume(volume: Float) {
        _uiState.value = _uiState.value.copy(originalVideoVolume = volume)
    }

    fun updateVoiceSfxVolume(volume: Float) {
        _uiState.value = _uiState.value.copy(voiceSfxVolume = volume)
        audioEngine.sfxVolume = volume
        audioEngine.voiceVolume = volume
    }

    fun playSfx(sfx: CartoonSfx) {
        audioEngine.playSfx(sfx)
    }

    fun setVoicePitch(pitch: Float) {
        _uiState.value = _uiState.value.copy(voicePitch = pitch)
    }

    fun setVoiceNarrativeText(text: String) {
        _uiState.value = _uiState.value.copy(voiceNarrativeText = text)
    }

    fun speakCartoonVoice() {
        val text = _uiState.value.voiceNarrativeText
        audioEngine.speakCartoonVoice(text, _uiState.value.voicePitch)
    }

    // --- SUBTITLES ---

    fun setSubtitleStyle(style: SubtitleStyle) {
        _uiState.value = _uiState.value.copy(subtitleStyle = style)
    }

    fun setSubtitlePosition(position: SubtitlePosition) {
        _uiState.value = _uiState.value.copy(subtitlePosition = position)
    }

    fun setSubtitleFontSize(sizeSp: Int) {
        _uiState.value = _uiState.value.copy(subtitleFontSizeSp = sizeSp)
    }

    fun addSubtitleCueAtCurrentTime(text: String) {
        if (text.isBlank()) return
        val start = _uiState.value.currentPositionMs
        val end = (start + 2500L).coerceAtMost(_uiState.value.totalDurationMs)
        val newCue = SubtitleCue(
            id = UUID.randomUUID().toString(),
            startTimeMs = start,
            endTimeMs = end,
            text = text,
            speaker = "কার্টুন ভয়েস"
        )
        val updatedList = (_uiState.value.subtitles + newCue).sortedBy { it.startTimeMs }
        _uiState.value = _uiState.value.copy(subtitles = updatedList)
    }

    fun editSubtitleCue(id: String, newText: String, startMs: Long, endMs: Long) {
        val updated = _uiState.value.subtitles.map { cue ->
            if (cue.id == id) {
                cue.copy(text = newText, startTimeMs = startMs, endTimeMs = endMs)
            } else {
                cue
            }
        }.sortedBy { it.startTimeMs }
        _uiState.value = _uiState.value.copy(subtitles = updated)
    }

    fun deleteSubtitleCue(id: String) {
        val updated = _uiState.value.subtitles.filterNot { it.id == id }
        _uiState.value = _uiState.value.copy(subtitles = updated)
    }

    /**
     * Auto-generate timed cartoon subtitles for videos.
     */
    fun generateAutoSubtitlesForVideo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAutoSubtitlesGenerating = true)
            delay(1200) // Simulating AI transcription and cartoon dialogue generation

            val autoCues = listOf(
                SubtitleCue("ai_1", 0L, 3500L, "✨ কার্টুন ম্যাজিক রূপান্তর শুরু হলো!", "এআই ন্যারেটর"),
                SubtitleCue("ai_2", 3500L, 7500L, "দেখুন সাধারণ দৃশ্য এখন সম্পূর্ণ রঙিন অ্যানিমেশন!", "কার্টুন চরিত্র"),
                SubtitleCue("ai_3", 7500L, 11500L, "ব্যাকগ্রাউন্ড মিউজিক আর মজার সাউন্ড ইফেক্ট দারুণ মানিয়েছে!", "টুন ভয়েস"),
                SubtitleCue("ai_4", 11500L, 15000L, "এখনই ভিডিওটি সোশ্যাল মিডিয়ায় বন্ধুদের সাথে শেয়ার করুন!", "এআই হোস্ট")
            )

            _uiState.value = _uiState.value.copy(
                subtitles = autoCues,
                isAutoSubtitlesGenerating = false
            )
        }
    }

    /**
     * Export all current subtitles formatted as standard .srt format.
     */
    fun exportSrtContent(): String {
        val sb = StringBuilder()
        _uiState.value.subtitles.forEachIndexed { index, cue ->
            sb.append("${index + 1}\n")
            sb.append("${formatSrtTime(cue.startTimeMs)} --> ${formatSrtTime(cue.endTimeMs)}\n")
            sb.append("${cue.text}\n\n")
        }
        return sb.toString()
    }

    private fun formatSrtTime(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = ms % 1000
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis)
    }

    // --- SOCIAL SHARING ---

    fun showShareModal(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isShareModalVisible = visible)
    }

    fun setShareAspectRatio(ratio: String) {
        _uiState.value = _uiState.value.copy(shareAspectRatio = ratio)
    }

    fun setShareCaption(caption: String) {
        _uiState.value = _uiState.value.copy(shareCaption = caption)
    }

    fun copyCaptionToClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Cartoon Caption", _uiState.value.shareCaption)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "ক্যাপশন ক্লিপবোর্ডে কপি করা হয়েছে! 📋", Toast.LENGTH_SHORT).show()
    }

    fun shareToPlatform(platform: SocialPlatform, context: Context) {
        val textToShare = "${_uiState.value.shareCaption}\n\n[ভিডিও তৈরি: ToonMaker AI]"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textToShare)
            `package` = platform.packageName
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // If the specific app is not installed, fallback to general share intent
            shareToSystem(context)
        }
    }

    fun shareToSystem(context: Context) {
        val textToShare = "${_uiState.value.shareCaption}\n\n[তৈরি করুন ToonMaker AI দিয়ে]"
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, textToShare)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "কার্টুন ভিডিও শেয়ার করুন")
        context.startActivity(shareIntent)
    }

    fun exportVideoSuccessToast(context: Context) {
        Toast.makeText(
            context,
            "কার্টুন ভিডিও এবং মিউজিক সফলভাবে প্রসেস হয়েছে! গ্যালারিতে সংরক্ষিত হয়েছে। 🎬✨",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        audioEngine.release()
    }
}
