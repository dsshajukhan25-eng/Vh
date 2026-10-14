package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubtitleCue
import com.example.model.SubtitlePosition
import com.example.model.SubtitleStyle
import com.example.video.CartoonVideoEngine
import com.example.viewmodel.ToonStudioUiState

/**
 * CartoonPlayerView renders the live cartoon video preview, split-screen slider,
 * timeline scrubber, controls, and synchronized subtitle overlay.
 */
@Composable
fun CartoonPlayerView(
    uiState: ToonStudioUiState,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleSplit: () -> Unit,
    onSplitRatioChange: (Float) -> Unit,
    onAspectRatioChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showControls by remember { mutableStateOf(true) }

    // Find currently active subtitle based on playback timestamp
    val currentSubtitle = remember(uiState.currentPositionMs, uiState.subtitles) {
        uiState.subtitles.firstOrNull { cue ->
            uiState.currentPositionMs in cue.startTimeMs..cue.endTimeMs
        }
    }

    val aspect = when (uiState.shareAspectRatio) {
        "9:16" -> 9f / 16f
        "1:1" -> 1f
        else -> 16f / 9f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { showControls = !showControls })
            },
        contentAlignment = Alignment.Center
    ) {
        // --- VIDEO / CARTOON CANVAS RENDERING ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width.toInt().coerceAtLeast(1)
                val canvasHeight = size.height.toInt().coerceAtLeast(1)

                // Render offscreen bitmap with software cartoon engine
                val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
                val androidCanvas = Canvas(bitmap)

                if (uiState.isSplitComparison) {
                    val splitX = (canvasWidth * uiState.splitRatio).toInt()

                    // Left side: Original
                    androidCanvas.save()
                    androidCanvas.clipRect(0, 0, splitX, canvasHeight)
                    CartoonVideoEngine.drawSampleFrame(
                        androidCanvas,
                        canvasWidth,
                        canvasHeight,
                        uiState.selectedPreset.id,
                        uiState.currentPositionMs,
                        uiState.activeStyle,
                        isCartoon = false
                    )
                    androidCanvas.restore()

                    // Right side: Cartoonized
                    androidCanvas.save()
                    androidCanvas.clipRect(splitX, 0, canvasWidth, canvasHeight)
                    CartoonVideoEngine.drawSampleFrame(
                        androidCanvas,
                        canvasWidth,
                        canvasHeight,
                        uiState.selectedPreset.id,
                        uiState.currentPositionMs,
                        uiState.activeStyle,
                        isCartoon = true
                    )
                    androidCanvas.restore()
                } else {
                    // Full Cartoon Frame
                    CartoonVideoEngine.drawSampleFrame(
                        androidCanvas,
                        canvasWidth,
                        canvasHeight,
                        uiState.selectedPreset.id,
                        uiState.currentPositionMs,
                        uiState.activeStyle,
                        isCartoon = true
                    )
                }

                drawImage(bitmap.asImageBitmap())

                // Draw split divider line if enabled
                if (uiState.isSplitComparison) {
                    val splitX = size.width * uiState.splitRatio
                    drawLine(
                        color = Color.White,
                        start = Offset(splitX, 0f),
                        end = Offset(splitX, size.height),
                        strokeWidth = 4f
                    )
                }
            }

            // Split Comparison Drag Handle
            if (uiState.isSplitComparison) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newRatio = (change.position.x / size.width.toFloat())
                                onSplitRatioChange(newRatio)
                            }
                        }
                ) {
                    val handleX = 260.dp * 0.5f
                    // Split Comparison Badge
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .background(Color(0xCC000000), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("👈 আসল", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("|", color = Color(0xFF64748B), fontSize = 11.sp)
                        Text("কার্টুন 👉", color = Color(0xFFFFD166), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- SYNCHRONIZED SUBTITLE OVERLAY ---
            if (currentSubtitle != null) {
                val align = when (uiState.subtitlePosition) {
                    SubtitlePosition.TOP -> Alignment.TopCenter
                    SubtitlePosition.CENTER -> Alignment.Center
                    SubtitlePosition.BOTTOM -> Alignment.BottomCenter
                }

                val bottomPad = if (uiState.subtitlePosition == SubtitlePosition.BOTTOM) 54.dp else 12.dp
                val topPad = if (uiState.subtitlePosition == SubtitlePosition.TOP) 16.dp else 0.dp

                Box(
                    modifier = Modifier
                        .align(align)
                        .padding(bottom = bottomPad, top = topPad, start = 16.dp, end = 16.dp)
                ) {
                    SubtitleBox(
                        cue = currentSubtitle,
                        style = uiState.subtitleStyle,
                        fontSizeSp = uiState.subtitleFontSizeSp
                    )
                }
            }

            // Top overlay tags: Active Style & Aspect Ratio
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = Color(0xCC1E293B),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${uiState.activeStyle.iconEmoji} ${uiState.activeStyle.titleBn}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color(0xCC1E293B),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (uiState.uploadedVideoUri != null) "🎬 আপলোড করা ভিডিও" else "🌟 ${uiState.selectedPreset.titleBn}",
                        color = Color(0xFF93C5FD),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // --- PLAYBACK CONTROLS & TIMELINE SCRUBBER ---
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xDD0B132B))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Scrubber Timeline Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentSec = uiState.currentPositionMs / 1000
                    val totalSec = uiState.totalDurationMs / 1000
                    Text(
                        text = String.format("%02d:%02d", currentSec / 60, currentSec % 60),
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )

                    Slider(
                        value = uiState.currentPositionMs.toFloat(),
                        onValueChange = { onSeek(it.toLong()) },
                        valueRange = 0f..uiState.totalDurationMs.toFloat().coerceAtLeast(1f),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                            .height(28.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF9F43),
                            activeTrackColor = Color(0xFFFF9F43),
                            inactiveTrackColor = Color(0xFF334155)
                        )
                    )

                    Text(
                        text = String.format("%02d:%02d", totalSec / 60, totalSec % 60),
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                // Control Buttons Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { onSeek(0L) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Replay",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Split Comparison Toggle & Aspect Ratio
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            onClick = onToggleSplit,
                            color = if (uiState.isSplitComparison) Color(0xFFFF9F43) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Compare,
                                    contentDescription = "Split View",
                                    tint = if (uiState.isSplitComparison) Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "তুলনা",
                                    fontSize = 11.sp,
                                    color = if (uiState.isSplitComparison) Color.Black else Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Aspect ratio badge
                        Surface(
                            onClick = {
                                val nextRatio = when (uiState.shareAspectRatio) {
                                    "9:16" -> "1:1"
                                    "1:1" -> "16:9"
                                    else -> "9:16"
                                }
                                onAspectRatioChange(nextRatio)
                            },
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = uiState.shareAspectRatio,
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Styled subtitle text box rendered over the cartoon player.
 */
@Composable
fun SubtitleBox(
    cue: SubtitleCue,
    style: SubtitleStyle,
    fontSizeSp: Int,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(style.backgroundColor)
    val textColor = Color(style.textColor)

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(bgColor, RoundedCornerShape(8.dp))
            .then(
                if (style.hasBorder) {
                    Modifier.border(1.5.dp, textColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                } else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = cue.text,
                color = textColor,
                fontSize = fontSizeSp.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = (fontSizeSp + 6).sp
            )
        }
    }
}
