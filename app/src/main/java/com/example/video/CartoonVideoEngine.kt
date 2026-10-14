package com.example.video

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.example.model.CartoonStyle
import com.example.model.SampleVideoPreset
import com.example.model.SubtitleCue
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * CartoonVideoEngine provides sample cartoon video animations,
 * cartoon bitmap processing, and color matrix transformations.
 */
object CartoonVideoEngine {

    /**
     * Built-in sample video presets with predefined animation scenes and subtitles.
     */
    val samplePresets = listOf(
        SampleVideoPreset(
            id = "kitty",
            titleBn = "বিড়াল ও প্রজাপতি",
            titleEn = "Playful Kitty & Butterfly",
            durationMs = 12000L,
            defaultSubtitles = listOf(
                SubtitleCue("s1", 0L, 3000L, "আরে দেখো! সুন্দর একটি প্রজাপতি উড়ছে!", "টুন বিড়াল"),
                SubtitleCue("s2", 3000L, 6500L, "আমি লাফ দিয়ে ওটাকে ধরবোই ধরবো!", "টুন বিড়াল"),
                SubtitleCue("s3", 6500L, 9500L, "ওরে বাবা! কত দ্রুত ডানা ঝাপটাচ্ছে!", "টুন বিড়াল"),
                SubtitleCue("s4", 9500L, 12000L, "হি হি! এবার বন্ধু হয়ে একসাথে খেলি!", "টুন বিড়াল")
            ),
            defaultDialogueBn = "আরে দেখো! সুন্দর একটি প্রজাপতি উড়ছে! আমি লাফ দিয়ে ওটাকে ধরবোই ধরবো! হি হি!",
            sceneType = "cat_garden",
            iconEmoji = "🐱"
        ),
        SampleVideoPreset(
            id = "city",
            titleBn = "হাসিখুশি শহর ও গাড়ি",
            titleEn = "Happy Cartoon City",
            durationMs = 10000L,
            defaultSubtitles = listOf(
                SubtitleCue("c1", 0L, 2500L, "আজকের সকালটা কত চমৎকার রোদেলা!", "কার্টুন শহর"),
                SubtitleCue("c2", 2500L, 5500L, "পিপ পিপ! লাল গাড়িটি বন্ধুদের নিয়ে যাচ্ছে!", "লাল গাড়ি"),
                SubtitleCue("c3", 5500L, 8000L, "সবুজ গাছপালা আর নীল আকাশের নিচে গান বাজছে!", "কার্টুন শহর"),
                SubtitleCue("c4", 8000L, 10000L, "চলুন একসাথে কার্টুন জগতে ঘুরে আসি!", "টুন হিরো")
            ),
            defaultDialogueBn = "পিপ পিপ! আজকের সকালটা কত চমৎকার! বন্ধুদের নিয়ে লাল গাড়িটি ঘুরতে যাচ্ছে!",
            sceneType = "city_cars",
            iconEmoji = "🚗"
        ),
        SampleVideoPreset(
            id = "pals",
            titleBn = "দুই কার্টুন বন্ধু",
            titleEn = "Two Toon Pals",
            durationMs = 14000L,
            defaultSubtitles = listOf(
                SubtitleCue("p1", 0L, 3500L, "বন্ধু বব! আজ কী মজার পরিকল্পনা করেছো?", "বন্ধু এলিস"),
                SubtitleCue("p2", 3500L, 7000L, "আমরা আজ নতুন ম্যাজিক কার্টুন তৈরি করবো!", "বন্ধু বব"),
                SubtitleCue("p3", 7000L, 10500L, "ওয়াও! সাথে দারুণ মিউজিক আর সাউন্ড থাকবে?", "বন্ধু এলিস"),
                SubtitleCue("p4", 10500L, 14000L, "একদম! সোশ্যাল মিডিয়ায় শেয়ার করলে সবাই অবাক হবে!", "বন্ধু বব")
            ),
            defaultDialogueBn = "আমরা আজ নতুন ম্যাজিক কার্টুন তৈরি করবো! সোশ্যাল মিডিয়ায় শেয়ার করলে সবাই অবাক হবে!",
            sceneType = "two_friends",
            iconEmoji = "👥"
        ),
        SampleVideoPreset(
            id = "dance",
            titleBn = "নাচের জাদুকরী তারকা",
            titleEn = "Dancing Cartoon Star",
            durationMs = 9000L,
            defaultSubtitles = listOf(
                SubtitleCue("d1", 0L, 2500L, "ওয়ান, টু, থ্রি! গানের তালে নাচ শুরু!", "স্টার ড্যান্সার"),
                SubtitleCue("d2", 2500L, 5500L, "ঘুরো ডান দিকে, এবার বামে জাম্প!", "স্টার ড্যান্সার"),
                SubtitleCue("d3", 5500L, 7500L, "দেখো চারপাশে রঙিন আলোর ফুলকি ছড়াচ্ছে!", "স্টার ড্যান্সার"),
                SubtitleCue("d4", 7500L, 9000L, "দারুণ নাচ হলো! হাততালি দাও সবাই!", "স্টার ড্যান্সার")
            ),
            defaultDialogueBn = "ওয়ান, টু, থ্রি! গানের তালে নাচ শুরু! দারুণ নাচ হলো, হাততালি দাও সবাই!",
            sceneType = "dancing_star",
            iconEmoji = "⭐"
        )
    )

    /**
     * Build an Android ColorMatrix that models the selected cartoon style
     * with saturation boost, posterization contrast, and outline emphasis.
     */
    fun createCartoonColorMatrix(
        style: CartoonStyle,
        outlineStrength: Float = style.outlineStrength,
        saturationBoost: Float = style.saturationBoost,
        contrast: Float = style.contrast,
        brightnessOffset: Float = 0f
    ): ColorMatrix {
        val matrix = ColorMatrix()

        when (style) {
            CartoonStyle.COMIC_BOOK -> {
                // High contrast, saturated primary colors, crisp comic ink
                matrix.setSaturation(saturationBoost * 1.2f)
                val c = contrast * 1.3f
                val t = (-0.5f * c + 0.5f) * 255f + (brightnessOffset * 255f)
                val contrastMatrix = ColorMatrix(
                    floatArrayOf(
                        c, 0f, 0f, 0f, t,
                        0f, c, 0f, 0f, t,
                        0f, 0f, c, 0f, t,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(contrastMatrix)
            }
            CartoonStyle.ANIME_CEL -> {
                // Vibrant anime palette, clean highlights, slightly warmer skin tones
                matrix.setSaturation(saturationBoost * 1.35f)
                val c = contrast * 1.15f
                val t = (-0.5f * c + 0.5f) * 255f + 12f
                val animeMatrix = ColorMatrix(
                    floatArrayOf(
                        c * 1.05f, 0f, 0f, 0f, t + 8f,
                        0f, c * 1.02f, 0f, 0f, t + 4f,
                        0f, 0f, c * 1.12f, 0f, t + 10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(animeMatrix)
            }
            CartoonStyle.WATERCOLOR_TOON -> {
                // Soft pastel tones, reduced harsh contrast, dreamy warmth
                matrix.setSaturation(saturationBoost * 0.9f)
                val c = contrast * 0.95f
                val t = (-0.5f * c + 0.5f) * 255f + 25f
                val watercolorMatrix = ColorMatrix(
                    floatArrayOf(
                        c * 1.08f, 0f, 0f, 0f, t + 15f,
                        0f, c * 1.02f, 0f, 0f, t + 12f,
                        0f, 0f, c * 0.92f, 0f, t + 8f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(watercolorMatrix)
            }
            CartoonStyle.POP_ART -> {
                // Ultra vivid pop comic neon
                matrix.setSaturation(saturationBoost * 1.6f)
                val c = contrast * 1.4f
                val t = (-0.5f * c + 0.5f) * 255f
                val popMatrix = ColorMatrix(
                    floatArrayOf(
                        c * 1.2f, 0f, 0f, 0f, t + 20f,
                        0f, c * 1.1f, 0f, 0f, t,
                        0f, 0f, c * 1.3f, 0f, t + 30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(popMatrix)
            }
            CartoonStyle.PENCIL_SKETCH -> {
                // Grayscale graphite pencil sketch with high edge contrast
                matrix.setSaturation(0.05f)
                val c = contrast * 1.5f
                val t = (-0.5f * c + 0.5f) * 255f + 35f
                val sketchMatrix = ColorMatrix(
                    floatArrayOf(
                        c, 0f, 0f, 0f, t,
                        0f, c, 0f, 0f, t,
                        0f, 0f, c, 0f, t,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(sketchMatrix)
            }
        }

        return matrix
    }

    /**
     * Renders a procedurally animated cartoon frame for the sample preset.
     * Uses canvas primitives with cartoon ink strokes and cheerful characters.
     */
    fun drawSampleFrame(
        canvas: Canvas,
        width: Int,
        height: Int,
        presetId: String,
        timeMs: Long,
        cartoonStyle: CartoonStyle,
        isCartoon: Boolean
    ) {
        val t = (timeMs % 10000).toFloat() / 1000f
        val w = width.toFloat()
        val h = height.toFloat()

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            strokeWidth = if (isCartoon) 6f * cartoonStyle.outlineStrength.coerceAtLeast(0.4f) else 2f
            color = if (isCartoon) Color.BLACK else Color.DKGRAY
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
        }

        when (presetId) {
            "kitty" -> {
                // Sky & Green Grass
                fillPaint.color = if (isCartoon) Color.parseColor("#70C1FF") else Color.parseColor("#87CEEB")
                canvas.drawRect(0f, 0f, w, h * 0.65f, fillPaint)

                fillPaint.color = if (isCartoon) Color.parseColor("#44D368") else Color.parseColor("#4CAF50")
                canvas.drawRect(0f, h * 0.65f, w, h, fillPaint)

                // Sun
                fillPaint.color = Color.parseColor("#FFD93D")
                canvas.drawCircle(w * 0.85f, h * 0.18f, w * 0.10f, fillPaint)
                if (isCartoon) canvas.drawCircle(w * 0.85f, h * 0.18f, w * 0.10f, strokePaint)

                // Cartoon Cat in center bouncing
                val bounceY = sin(t * 3.5).toFloat() * (h * 0.04f)
                val catX = w * 0.45f
                val catY = h * 0.68f + bounceY

                // Body
                fillPaint.color = if (isCartoon) Color.parseColor("#FF9F43") else Color.parseColor("#E67E22")
                val bodyR = w * 0.16f
                canvas.drawCircle(catX, catY, bodyR, fillPaint)
                if (isCartoon) canvas.drawCircle(catX, catY, bodyR, strokePaint)

                // Head
                val headR = w * 0.12f
                val headY = catY - bodyR * 0.9f
                canvas.drawCircle(catX, headY, headR, fillPaint)
                if (isCartoon) canvas.drawCircle(catX, headY, headR, strokePaint)

                // Ears
                val earL = android.graphics.Path().apply {
                    moveTo(catX - headR * 0.8f, headY - headR * 0.4f)
                    lineTo(catX - headR * 0.9f, headY - headR * 1.5f)
                    lineTo(catX - headR * 0.2f, headY - headR * 0.8f)
                    close()
                }
                canvas.drawPath(earL, fillPaint)
                if (isCartoon) canvas.drawPath(earL, strokePaint)

                val earR = android.graphics.Path().apply {
                    moveTo(catX + headR * 0.2f, headY - headR * 0.8f)
                    lineTo(catX + headR * 0.9f, headY - headR * 1.5f)
                    lineTo(catX + headR * 0.8f, headY - headR * 0.4f)
                    close()
                }
                canvas.drawPath(earR, fillPaint)
                if (isCartoon) canvas.drawPath(earR, strokePaint)

                // Big Cartoon Eyes
                fillPaint.color = Color.WHITE
                canvas.drawCircle(catX - headR * 0.35f, headY - headR * 0.1f, headR * 0.25f, fillPaint)
                canvas.drawCircle(catX + headR * 0.35f, headY - headR * 0.1f, headR * 0.25f, fillPaint)
                if (isCartoon) {
                    canvas.drawCircle(catX - headR * 0.35f, headY - headR * 0.1f, headR * 0.25f, strokePaint)
                    canvas.drawCircle(catX + headR * 0.35f, headY - headR * 0.1f, headR * 0.25f, strokePaint)
                }

                // Pupils
                fillPaint.color = Color.BLACK
                val eyeLookX = sin(t * 2.0).toFloat() * 4f
                canvas.drawCircle(catX - headR * 0.35f + eyeLookX, headY - headR * 0.1f, headR * 0.12f, fillPaint)
                canvas.drawCircle(catX + headR * 0.35f + eyeLookX, headY - headR * 0.1f, headR * 0.12f, fillPaint)

                // Pink Nose & Smile
                fillPaint.color = Color.parseColor("#FF6B6B")
                canvas.drawCircle(catX, headY + headR * 0.2f, headR * 0.08f, fillPaint)

                // Butterfly fluttering
                val bflyX = w * 0.70f + cos(t * 3.0).toFloat() * (w * 0.12f)
                val bflyY = h * 0.35f + sin(t * 4.5).toFloat() * (h * 0.08f)
                fillPaint.color = Color.parseColor("#FF5252")
                canvas.drawCircle(bflyX - 12f, bflyY, 14f, fillPaint)
                fillPaint.color = Color.parseColor("#FFD166")
                canvas.drawCircle(bflyX + 12f, bflyY, 14f, fillPaint)
                fillPaint.color = Color.BLACK
                canvas.drawOval(bflyX - 4f, bflyY - 14f, bflyX + 4f, bflyY + 14f, fillPaint)
                if (isCartoon) {
                    canvas.drawCircle(bflyX - 12f, bflyY, 14f, strokePaint)
                    canvas.drawCircle(bflyX + 12f, bflyY, 14f, strokePaint)
                }
            }
            "city" -> {
                // Sunset / City Skyline
                fillPaint.color = if (isCartoon) Color.parseColor("#FFA07A") else Color.parseColor("#E59866")
                canvas.drawRect(0f, 0f, w, h * 0.7f, fillPaint)

                // City Buildings
                val buildingColors = intArrayOf(
                    Color.parseColor("#2C3E50"),
                    Color.parseColor("#34495E"),
                    Color.parseColor("#1B2631"),
                    Color.parseColor("#4A235A")
                )
                for (i in 0..4) {
                    fillPaint.color = buildingColors[i % buildingColors.size]
                    val bx = i * (w * 0.22f)
                    val bh = h * (0.35f + (i % 3) * 0.08f)
                    canvas.drawRect(bx, h * 0.7f - bh, bx + w * 0.20f, h * 0.7f, fillPaint)
                    if (isCartoon) canvas.drawRect(bx, h * 0.7f - bh, bx + w * 0.20f, h * 0.7f, strokePaint)

                    // Windows
                    fillPaint.color = Color.parseColor("#F9E79F")
                    for (wy in 1..4) {
                        for (wx in 1..2) {
                            val winX = bx + wx * (w * 0.06f) - 6f
                            val winY = h * 0.7f - bh + wy * (h * 0.06f)
                            canvas.drawRect(winX, winY, winX + 12f, winY + 16f, fillPaint)
                        }
                    }
                }

                // Road
                fillPaint.color = Color.parseColor("#34495E")
                canvas.drawRect(0f, h * 0.7f, w, h, fillPaint)
                if (isCartoon) canvas.drawLine(0f, h * 0.7f, w, h * 0.7f, strokePaint)

                // Cartoon Car moving
                val carX = ((t * 120f) % (w + 200f)) - 100f
                val carY = h * 0.80f
                fillPaint.color = Color.parseColor("#E74C3C")
                canvas.drawRoundRect(carX, carY - 30f, carX + 140f, carY + 20f, 15f, 15f, fillPaint)
                canvas.drawRoundRect(carX + 25f, carY - 60f, carX + 105f, carY - 20f, 12f, 12f, fillPaint)
                if (isCartoon) {
                    canvas.drawRoundRect(carX, carY - 30f, carX + 140f, carY + 20f, 15f, 15f, strokePaint)
                    canvas.drawRoundRect(carX + 25f, carY - 60f, carX + 105f, carY - 20f, 12f, 12f, strokePaint)
                }

                // Wheels
                fillPaint.color = Color.BLACK
                canvas.drawCircle(carX + 35f, carY + 20f, 18f, fillPaint)
                canvas.drawCircle(carX + 105f, carY + 20f, 18f, fillPaint)
                if (isCartoon) {
                    canvas.drawCircle(carX + 35f, carY + 20f, 18f, strokePaint)
                    canvas.drawCircle(carX + 105f, carY + 20f, 18f, strokePaint)
                }
            }
            "pals" -> {
                // Room / Studio background
                fillPaint.color = Color.parseColor("#85C1E9")
                canvas.drawRect(0f, 0f, w, h * 0.75f, fillPaint)
                fillPaint.color = Color.parseColor("#D5D8DC")
                canvas.drawRect(0f, h * 0.75f, w, h, fillPaint)

                // Friend 1 (Left)
                val p1X = w * 0.30f
                val p1Y = h * 0.65f + sin(t * 3.0).toFloat() * 10f
                fillPaint.color = Color.parseColor("#5DADE2")
                canvas.drawCircle(p1X, p1Y, w * 0.14f, fillPaint)
                fillPaint.color = Color.parseColor("#F5B7B1")
                canvas.drawCircle(p1X, p1Y - w * 0.18f, w * 0.12f, fillPaint)
                if (isCartoon) {
                    canvas.drawCircle(p1X, p1Y, w * 0.14f, strokePaint)
                    canvas.drawCircle(p1X, p1Y - w * 0.18f, w * 0.12f, strokePaint)
                }

                // Friend 2 (Right)
                val p2X = w * 0.70f
                val p2Y = h * 0.65f + cos(t * 3.0).toFloat() * 10f
                fillPaint.color = Color.parseColor("#F5B041")
                canvas.drawCircle(p2X, p2Y, w * 0.14f, fillPaint)
                fillPaint.color = Color.parseColor("#FADBD8")
                canvas.drawCircle(p2X, p2Y - w * 0.18f, w * 0.12f, fillPaint)
                if (isCartoon) {
                    canvas.drawCircle(p2X, p2Y, w * 0.14f, strokePaint)
                    canvas.drawCircle(p2X, p2Y - w * 0.18f, w * 0.12f, strokePaint)
                }
            }
            "dance" -> {
                // Disco stage with pulsating spotlights
                fillPaint.color = Color.parseColor("#17202A")
                canvas.drawRect(0f, 0f, w, h, fillPaint)

                // Spotlights
                val spotColor = if (t.toInt() % 2 == 0) Color.parseColor("#9B59B6") else Color.parseColor("#1ABC9C")
                fillPaint.color = spotColor
                canvas.drawCircle(w * 0.5f, h * 0.5f, w * 0.35f, fillPaint)

                // Dancing Star
                val starX = w * 0.5f + sin(t * 4.0).toFloat() * 30f
                val starY = h * 0.52f + sin(t * 8.0).toFloat() * 20f
                fillPaint.color = Color.parseColor("#F4D03F")
                canvas.drawCircle(starX, starY, w * 0.16f, fillPaint)
                if (isCartoon) canvas.drawCircle(starX, starY, w * 0.16f, strokePaint)

                // Star face & smile
                fillPaint.color = Color.BLACK
                canvas.drawCircle(starX - 25f, starY - 15f, 10f, fillPaint)
                canvas.drawCircle(starX + 25f, starY - 15f, 10f, fillPaint)
                canvas.drawArc(starX - 30f, starY - 10f, starX + 30f, starY + 30f, 0f, 180f, true, fillPaint)
            }
        }
    }
}
