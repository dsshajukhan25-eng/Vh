package com.example.model

import android.net.Uri

/**
 * Cartoon transformation styles available in ToonMaker AI.
 */
enum class CartoonStyle(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val outlineStrength: Float,
    val posterizeLevels: Int,
    val saturationBoost: Float,
    val contrast: Float,
    val iconEmoji: String
) {
    COMIC_BOOK(
        id = "comic",
        titleBn = "কমিক বুক",
        titleEn = "Comic Book",
        descriptionBn = "গাঢ় কালো আউটলাইন ও সেল-শেডিং কার্টুন স্টাইল",
        outlineStrength = 0.85f,
        posterizeLevels = 5,
        saturationBoost = 1.6f,
        contrast = 1.35f,
        iconEmoji = "💥"
    ),
    ANIME_CEL(
        id = "anime",
        titleBn = "অ্যানিমে ড্রিম",
        titleEn = "Anime Cel",
        descriptionBn = "উজ্জ্বল জাপানি অ্যানিমে রঙ ও মসৃণ কনট্যুর",
        outlineStrength = 0.65f,
        posterizeLevels = 7,
        saturationBoost = 1.8f,
        contrast = 1.25f,
        iconEmoji = "✨"
    ),
    WATERCOLOR_TOON(
        id = "watercolor",
        titleBn = "জলরং কার্টুন",
        titleEn = "Watercolor",
        descriptionBn = "রূপকথার বইয়ের মতো সফট প্যাস্টেল কার্টুন",
        outlineStrength = 0.40f,
        posterizeLevels = 9,
        saturationBoost = 1.3f,
        contrast = 1.10f,
        iconEmoji = "🎨"
    ),
    POP_ART(
        id = "popart",
        titleBn = "পপ-আর্ট টুন",
        titleEn = "Pop Art",
        descriptionBn = "রেট্রো ভাইব্রেন্ট পপ কার্টুন ভিজ্যুয়াল",
        outlineStrength = 0.90f,
        posterizeLevels = 4,
        saturationBoost = 2.1f,
        contrast = 1.50f,
        iconEmoji = "⚡"
    ),
    PENCIL_SKETCH(
        id = "sketch",
        titleBn = "পেন্সিল স্কেচ",
        titleEn = "Pencil Sketch",
        descriptionBn = "হাতে আঁকা ক্লাসিক ড্রয়িং ও পেন্সিল স্ট্রোক",
        outlineStrength = 0.95f,
        posterizeLevels = 3,
        saturationBoost = 0.4f,
        contrast = 1.40f,
        iconEmoji = "✏️"
    )
}

/**
 * Subtitle styling options.
 */
enum class SubtitleStyle(
    val titleBn: String,
    val textColor: Long,
    val backgroundColor: Long,
    val hasBorder: Boolean
) {
    CARTOON_YELLOW("কার্টুন ইয়েলো", 0xFFFFEB3B, 0xCC1A1A1A, true),
    CLASSIC_WHITE("ক্ল্যাসিক হোয়াইট", 0xFFFFFFFF, 0x99000000, false),
    NEON_CYAN("নিয়ন সায়ান", 0xFF00E5FF, 0xCC0D1B2A, true),
    COMIC_BUBBLE("কমিক বাবল", 0xFF1A1A1A, 0xFFFFFFFF, true)
}

enum class SubtitlePosition(val titleBn: String) {
    BOTTOM("নিচে"),
    CENTER("মাঝে"),
    TOP("উপরে")
}

/**
 * Individual subtitle cue with timing and text.
 */
data class SubtitleCue(
    val id: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String,
    val speaker: String = "কার্টুন চরিত্র"
) {
    fun formatTimestamp(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val millis = (ms % 1000) / 100
        return String.format("%02d:%02d.%d", minutes, seconds, millis)
    }

    val displayTiming: String
        get() = "${formatTimestamp(startTimeMs)} - ${formatTimestamp(endTimeMs)}"
}

/**
 * Cartoon BGM tracks synthesized with procedural audio.
 */
enum class CartoonBgmTrack(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val tempoBpm: Int,
    val moodEmoji: String
) {
    PLAYFUL_BOUNCE("bounce", "হাসিখুশি কার্টুন টিউন", "Playful Bounce", 128, "🎵"),
    SLAPSTICK_COMEDY("slapstick", "কমিক কমেডি রিদম", "Slapstick Comedy", 140, "🎷"),
    CIRCUS_FUN("circus", "সার্কাস কার্টুন মেলা", "Circus Fun", 132, "🎪"),
    UPBEAT_GROOVE("groove", "চিল টুন ভাইবস", "Upbeat Toon", 120, "🎸"),
    MAGIC_WONDER("magic", "ম্যাজিকাল কার্টুন পরী", "Magic Wonder", 110, "🪄")
}

/**
 * Sound effect triggers.
 */
enum class CartoonSfx(val id: String, val titleBn: String, val emoji: String) {
    BOING("boing", "বোয়িং জাম্প", "🌀"),
    POP("pop", "কার্টুন পপ", "💥"),
    WHISTLE("whistle", "মজার হুইসেল", "💨"),
    DING("ding", "ম্যাজিক ডিং", "✨"),
    FANFARE("fanfare", "ফান ফেয়ার", "🎺")
}

/**
 * Preset sample videos for immediate testing.
 */
data class SampleVideoPreset(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val durationMs: Long,
    val defaultSubtitles: List<SubtitleCue>,
    val defaultDialogueBn: String,
    val sceneType: String,
    val iconEmoji: String
)

/**
 * Target social platforms for sharing.
 */
enum class SocialPlatform(
    val id: String,
    val nameBn: String,
    val packageName: String,
    val colorHex: Long,
    val iconEmoji: String
) {
    WHATSAPP("whatsapp", "হোয়াটসঅ্যাপ", "com.whatsapp", 0xFF25D366, "💬"),
    FACEBOOK("facebook", "ফেসবুক", "com.facebook.katana", 0xFF1877F2, "📘"),
    INSTAGRAM("instagram", "ইনস্টাগ্রাম রিলস", "com.instagram.android", 0xFFE1306C, "📸"),
    YOUTUBE("youtube", "ইউটিউব শর্টস", "com.google.android.youtube", 0xFFFF0000, "▶️"),
    TIKTOK("tiktok", "টিকটক", "com.zhiliaoapp.musically", 0xFF000000, "🎵"),
    TWITTER("twitter", "এক্স (টুইটার)", "com.twitter.android", 0xFF1DA1F2, "✖️")
}
