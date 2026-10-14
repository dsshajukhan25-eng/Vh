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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CartoonStyle
import com.example.viewmodel.ToonStudioUiState

/**
 * StyleSelectorSection provides cartoon style presets and precision sliders.
 */
@Composable
fun StyleSelectorSection(
    uiState: ToonStudioUiState,
    onSelectStyle: (CartoonStyle) -> Unit,
    onOutlineChange: (Float) -> Unit,
    onPosterizeChange: (Int) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "কার্টুন আর্ট স্টাইল নির্বাচন করুন",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF1F5F9),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        // Horizontal Style Cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            items(CartoonStyle.values()) { style ->
                val isSelected = uiState.activeStyle == style
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A),
                    modifier = Modifier
                        .width(135.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFFFF9F43) else Color(0xFF334155),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectStyle(style) }
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = style.iconEmoji,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = style.titleBn,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFFFF9F43) else Color.White
                        )
                        Text(
                            text = style.titleEn,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Precision Tuning Panel
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "⚙️ কার্টুন ইফেক্ট ফাইন-টিউন",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Outline Boldness Slider
                SliderControl(
                    labelBn = "আউটলাইন গাঢ়ত্ব (Outline)",
                    value = uiState.outlineStrength,
                    valueRange = 0.1f..1.0f,
                    displayValue = "${(uiState.outlineStrength * 100).toInt()}%",
                    onValueChange = onOutlineChange
                )

                // Posterize Quantization Steps
                SliderControl(
                    labelBn = "রঙের কার্টুন স্তর (Color Steps)",
                    value = uiState.posterizeLevels.toFloat(),
                    valueRange = 3f..10f,
                    displayValue = "${uiState.posterizeLevels} লেভেল",
                    onValueChange = { onPosterizeChange(it.toInt()) }
                )

                // Saturation Slider
                SliderControl(
                    labelBn = "রঙের উজ্জ্বলতা (Saturation)",
                    value = uiState.saturationBoost,
                    valueRange = 0.5f..2.5f,
                    displayValue = "${(uiState.saturationBoost * 100).toInt()}%",
                    onValueChange = onSaturationChange
                )

                // Contrast Slider
                SliderControl(
                    labelBn = "কনট্রাস্ট ও শ্যাডো (Contrast)",
                    value = uiState.contrast,
                    valueRange = 0.8f..2.0f,
                    displayValue = String.format("%.1fx", uiState.contrast),
                    onValueChange = onContrastChange
                )
            }
        }
    }
}

@Composable
private fun SliderControl(
    labelBn: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = labelBn, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Text(
                text = displayValue,
                fontSize = 11.sp,
                color = Color(0xFFFF9F43),
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.height(24.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF9F43),
                activeTrackColor = Color(0xFFFF9F43),
                inactiveTrackColor = Color(0xFF475569)
            )
        )
    }
}
