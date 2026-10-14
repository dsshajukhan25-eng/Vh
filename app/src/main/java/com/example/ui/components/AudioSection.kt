package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CartoonBgmTrack
import com.example.model.CartoonSfx
import com.example.viewmodel.ToonStudioUiState

/**
 * AudioSection provides automatic cartoon music selection, volume mixing,
 * sound effect triggers, and cartoon TTS voiceovers.
 */
@Composable
fun AudioSection(
    uiState: ToonStudioUiState,
    onSelectBgm: (CartoonBgmTrack) -> Unit,
    onToggleBgm: (Boolean) -> Unit,
    onToggleMuteOriginal: (Boolean) -> Unit,
    onBgmVolumeChange: (Float) -> Unit,
    onOriginalVolumeChange: (Float) -> Unit,
    onVoiceSfxVolumeChange: (Float) -> Unit,
    onPlaySfx: (CartoonSfx) -> Unit,
    onVoiceTextChange: (String) -> Unit,
    onVoicePitchChange: (Float) -> Unit,
    onSpeakVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // --- 1. CARTOON BGM TRACKS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎵 স্বয়ংক্রিয় কার্টুন মিউজিক (BGM)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF1F5F9)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (uiState.isBgmEnabled) "চালু" else "বন্ধ",
                    fontSize = 11.sp,
                    color = if (uiState.isBgmEnabled) Color(0xFF4ADE80) else Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = uiState.isBgmEnabled,
                    onCheckedChange = onToggleBgm,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFF9F43),
                        checkedTrackColor = Color(0xFF7C2D12),
                        uncheckedThumbColor = Color(0xFF64748B),
                        uncheckedTrackColor = Color(0xFF334155)
                    )
                )
            }
        }

        // BGM Horizontal Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            items(CartoonBgmTrack.values()) { track ->
                val isSelected = uiState.activeBgmTrack == track
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFFFF9F43) else Color(0xFF1E293B),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectBgm(track) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = track.moodEmoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = track.titleBn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color.White
                            )
                            Text(
                                text = "${track.tempoBpm} BPM",
                                fontSize = 10.sp,
                                color = if (isSelected) Color(0xFF451A03) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. AUDIO MIXER CONSOLE ---
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🎛️ অডিও মিক্সার (Volume Mixer)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // BGM Volume Slider
                VolumeSlider(
                    label = "কার্টুন মিউজিক ভলিউম",
                    value = uiState.bgmVolume,
                    onValueChange = onBgmVolumeChange,
                    accentColor = Color(0xFFFF9F43)
                )

                // Voice & SFX Volume Slider
                VolumeSlider(
                    label = "কার্টুন ভয়েস ও সাউন্ড এফেক্ট",
                    value = uiState.voiceSfxVolume,
                    onValueChange = onVoiceSfxVolumeChange,
                    accentColor = Color(0xFF38BDF8)
                )

                // Original Video Volume Slider
                VolumeSlider(
                    label = "মূল ভিডিও অরিজিনাল অডিও",
                    value = if (uiState.isOriginalVideoMuted) 0f else uiState.originalVideoVolume,
                    onValueChange = onOriginalVolumeChange,
                    accentColor = Color(0xFFA78BFA)
                )

                // Mute Original Video Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "মূল ভিডিও অডিও মিউট করে শুধু কার্টুন টিউন বাজান",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Switch(
                        checked = uiState.isOriginalVideoMuted,
                        onCheckedChange = onToggleMuteOriginal,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFEF4444),
                            checkedTrackColor = Color(0xFF7F1D1D),
                            uncheckedThumbColor = Color(0xFF64748B),
                            uncheckedTrackColor = Color(0xFF334155)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 3. INSTANT CARTOON SFX SOUNDBOARD ---
        Text(
            text = "⚡ তাৎক্ষণিক কার্টুন সাউন্ড এফেক্ট (SFX)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF1F5F9)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CartoonSfx.values().forEach { sfx ->
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                        .clickable { onPlaySfx(sfx) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = sfx.emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = sfx.titleBn,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 4. AI CARTOON VOICEOVER STUDIO ---
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🎙️ এআই কার্টুন ভয়েসওভার (Voiceover)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Pitch selector chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val pitches = listOf(
                        Triple("কাঠবিড়ালি (চিকন)", 1.8f, "🐿️"),
                        Triple("মজার কার্টুন", 1.2f, "🐱"),
                        Triple("দানব কার্টুন (ভারী)", 0.6f, "🐻")
                    )

                    pitches.forEach { (name, pitchVal, emoji) ->
                        val isPitchSelected = (uiState.voicePitch == pitchVal)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPitchSelected) Color(0xFF38BDF8) else Color(0xFF0F172A),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onVoicePitchChange(pitchVal) }
                        ) {
                            Text(
                                text = "$emoji $name",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPitchSelected) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Voiceover text field
                OutlinedTextField(
                    value = uiState.voiceNarrativeText,
                    onValueChange = onVoiceTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("কার্টুন চরিত্রের সংলাপ লিখুন...", color = Color(0xFF64748B), fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedBorderColor = Color(0xFFFF9F43),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSpeakVoice,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9F43),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Speak",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "কার্টুন ভয়েস শুনুন ও যুক্ত করুন",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun VolumeSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    accentColor: Color
) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Text(
                text = "${(value * 100).toInt()}%",
                fontSize = 11.sp,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.height(24.dp),
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color(0xFF475569)
            )
        )
    }
}
