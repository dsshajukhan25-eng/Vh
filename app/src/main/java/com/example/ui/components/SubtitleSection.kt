package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubtitleCue
import com.example.model.SubtitlePosition
import com.example.model.SubtitleStyle
import com.example.viewmodel.ToonStudioUiState

/**
 * SubtitleSection provides auto-subtitle generation, interactive timeline editing,
 * styling options, and .SRT subtitle exporting.
 */
@Composable
fun SubtitleSection(
    uiState: ToonStudioUiState,
    onGenerateAutoSubtitles: () -> Unit,
    onAddSubtitleCue: (String) -> Unit,
    onEditSubtitleCue: (String, String, Long, Long) -> Unit,
    onDeleteSubtitleCue: (String) -> Unit,
    onSeekTo: (Long) -> Unit,
    onSetSubtitleStyle: (SubtitleStyle) -> Unit,
    onSetSubtitlePosition: (SubtitlePosition) -> Unit,
    onSetSubtitleFontSize: (Int) -> Unit,
    onExportSrt: () -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var newSubtitleText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // --- 1. ACTION HEADER: AUTO-SUBTITLES & SRT EXPORT ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💬 ভিডিও সাবটাইটেল ম্যানেজার",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF1F5F9)
            )

            // Export SRT Button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        val srt = onExportSrt()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Subtitles SRT", srt))
                        Toast.makeText(context, ".SRT সাবটাইটেল ক্লিপবোর্ডে কপি হয়েছে! 📋", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy SRT",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SRT কপি",
                        fontSize = 11.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // AI Auto Subtitle Generator Action Card
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "✨ এআই অটো সাবটাইটেল জেনারেটর",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "ভিডিওর দৃশ্যের সাথে স্বয়ংক্রিয় বাংলা সাবটাইটেল তৈরি করুন",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Button(
                    onClick = onGenerateAutoSubtitles,
                    enabled = !uiState.isAutoSubtitlesGenerating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38BDF8),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState.isAutoSubtitlesGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Auto Generate",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "তৈরি করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. SUBTITLE STYLING & POSITION ---
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "🎨 সাবটাইটেল স্টাইল ও পজিশন",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Style Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SubtitleStyle.values().forEach { style ->
                        val isSelected = uiState.subtitleStyle == style
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFFF9F43) else Color(0xFF0F172A),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, if (isSelected) Color(0xFFFF9F43) else Color(0xFF334155), RoundedCornerShape(8.dp))
                                .clickable { onSetSubtitleStyle(style) }
                        ) {
                            Text(
                                text = style.titleBn,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Position Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "পজিশন:", fontSize = 11.sp, color = Color(0xFF94A3B8))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SubtitlePosition.values().forEach { pos ->
                            val isPosSelected = uiState.subtitlePosition == pos
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isPosSelected) Color(0xFF38BDF8) else Color(0xFF0F172A),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onSetSubtitlePosition(pos) }
                            ) {
                                Text(
                                    text = pos.titleBn,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isPosSelected) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 3. QUICK ADD SUBTITLE CUE ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newSubtitleText,
                onValueChange = { newSubtitleText = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    val sec = uiState.currentPositionMs / 1000
                    Text(
                        "${String.format("%02d:%02d", sec / 60, sec % 60)} সময়ে সাবটাইটেল লিখুন...",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFE2E8F0),
                    focusedBorderColor = Color(0xFFFF9F43),
                    unfocusedBorderColor = Color(0xFF334155)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(6.dp))

            Button(
                onClick = {
                    if (newSubtitleText.isNotBlank()) {
                        onAddSubtitleCue(newSubtitleText)
                        newSubtitleText = ""
                        Toast.makeText(context, "সাবটাইটেল যুক্ত হয়েছে! ✍️", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9F43),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 4. SUBTITLE CUES LIST ---
        Text(
            text = "📋 সাবটাইটেল তালিকা (${uiState.subtitles.size} টি কিউ)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFCBD5E1)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            uiState.subtitles.forEach { cue ->
                val isCurrent = uiState.currentPositionMs in cue.startTimeMs..cue.endTimeMs
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCurrent) Color(0xFF1E293B) else Color(0xFF0F172A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isCurrent) Color(0xFFFF9F43) else Color(0xFF1E293B),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play / Seek to cue button
                        IconButton(
                            onClick = { onSeekTo(cue.startTimeMs) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play at cue",
                                tint = if (isCurrent) Color(0xFFFF9F43) else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cue.displayTiming,
                                fontSize = 10.sp,
                                color = Color(0xFF38BDF8),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            Text(
                                text = cue.text,
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        // Delete cue button
                        IconButton(
                            onClick = { onDeleteSubtitleCue(cue.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
