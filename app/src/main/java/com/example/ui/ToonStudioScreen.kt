package com.example.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AudioSection
import com.example.ui.components.CartoonPlayerView
import com.example.ui.components.SocialShareModal
import com.example.ui.components.StyleSelectorSection
import com.example.ui.components.SubtitleSection
import com.example.video.CartoonVideoEngine
import com.example.viewmodel.ToonStudioViewModel

/**
 * Main Studio Screen for ToonMaker AI.
 */
@Composable
fun ToonStudioScreen(
    viewModel: ToonStudioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Video Gallery Picker Launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setUploadedVideo(uri)
            Toast.makeText(context, "ভিডিও লোড হয়েছে! কার্টুন রূপান্তর শুরু হচ্ছে... 🎬", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = Color(0xFF090D16),
        topBar = {
            // --- TOP BAR WITH APP IDENTITY & UPLOAD ACTION ---
            Surface(
                color = Color(0xFF0B132B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎨", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ToonMaker AI",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "ভিডিও থেকে কার্টুন ও অডিও স্টুডিও",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Video Upload Action Button
                        Button(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9F43),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = "Upload Video",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ভিডিও আপলোড",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Share Button
                        IconButton(
                            onClick = { viewModel.showShareModal(true) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // --- QUICK SAMPLE PRESET CHIPS ---
            Text(
                text = "ভিডিও ক্লিপ নির্বাচন করুন:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                items(CartoonVideoEngine.samplePresets) { preset ->
                    val isSelected = (uiState.uploadedVideoUri == null && uiState.selectedPreset.id == preset.id)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFFFF9F43) else Color(0xFF1E293B),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, if (isSelected) Color(0xFFFF9F43) else Color(0xFF334155), RoundedCornerShape(20.dp))
                            .clickable { viewModel.selectSamplePreset(preset) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = preset.iconEmoji, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = preset.titleBn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- CARTOON VIDEO PLAYER VIEW ---
            CartoonPlayerView(
                uiState = uiState,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onToggleSplit = { viewModel.toggleSplitComparison() },
                onSplitRatioChange = { viewModel.updateSplitRatio(it) },
                onAspectRatioChange = { viewModel.setShareAspectRatio(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // --- STUDIO SEGMENTED TABS ---
            val tabs = listOf(
                Triple("কার্টুন স্টাইল", Icons.Default.Palette, 0),
                Triple("অডিও ও ভয়েস", Icons.Default.MusicNote, 1),
                Triple("সাবটাইটেল", Icons.Default.ChatBubbleOutline, 2)
            )

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF0F172A),
                contentColor = Color(0xFFFF9F43),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFFFF9F43),
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
            ) {
                tabs.forEach { (title, icon, index) ->
                    val isSelected = selectedTabIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isSelected) Color(0xFFFF9F43) else Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFFFF9F43) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- TAB CONTENT ---
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "StudioTabAnimation"
            ) { tabIdx ->
                when (tabIdx) {
                    0 -> StyleSelectorSection(
                        uiState = uiState,
                        onSelectStyle = { viewModel.setStyle(it) },
                        onOutlineChange = { viewModel.updateOutlineStrength(it) },
                        onPosterizeChange = { viewModel.updatePosterizeLevels(it) },
                        onSaturationChange = { viewModel.updateSaturation(it) },
                        onContrastChange = { viewModel.updateContrast(it) }
                    )
                    1 -> AudioSection(
                        uiState = uiState,
                        onSelectBgm = { viewModel.setBgmTrack(it) },
                        onToggleBgm = { viewModel.toggleBgm(it) },
                        onToggleMuteOriginal = { viewModel.toggleMuteOriginalVideo(it) },
                        onBgmVolumeChange = { viewModel.updateBgmVolume(it) },
                        onOriginalVolumeChange = { viewModel.updateOriginalVolume(it) },
                        onVoiceSfxVolumeChange = { viewModel.updateVoiceSfxVolume(it) },
                        onPlaySfx = { viewModel.playSfx(it) },
                        onVoiceTextChange = { viewModel.setVoiceNarrativeText(it) },
                        onVoicePitchChange = { viewModel.setVoicePitch(it) },
                        onSpeakVoice = { viewModel.speakCartoonVoice() }
                    )
                    2 -> SubtitleSection(
                        uiState = uiState,
                        onGenerateAutoSubtitles = { viewModel.generateAutoSubtitlesForVideo() },
                        onAddSubtitleCue = { viewModel.addSubtitleCueAtCurrentTime(it) },
                        onEditSubtitleCue = { id, text, s, e -> viewModel.editSubtitleCue(id, text, s, e) },
                        onDeleteSubtitleCue = { viewModel.deleteSubtitleCue(it) },
                        onSeekTo = { viewModel.seekTo(it) },
                        onSetSubtitleStyle = { viewModel.setSubtitleStyle(it) },
                        onSetSubtitlePosition = { viewModel.setSubtitlePosition(it) },
                        onSetSubtitleFontSize = { viewModel.setSubtitleFontSize(it) },
                        onExportSrt = { viewModel.exportSrtContent() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- BOTTOM QUICK SHARE BAR ---
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                    .clickable { viewModel.showShareModal(true) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🚀", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ভিডিও তৈরি শেষ? সোশ্যাল মিডিয়ায় শেয়ার করুন",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "WhatsApp, Facebook, Instagram Reels, YouTube Shorts",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color(0xFFFF9F43),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // --- SOCIAL SHARE MODAL DIALOG ---
        if (uiState.isShareModalVisible) {
            SocialShareModal(
                uiState = uiState,
                onDismiss = { viewModel.showShareModal(false) },
                onSelectAspectRatio = { viewModel.setShareAspectRatio(it) },
                onCaptionChange = { viewModel.setShareCaption(it) },
                onCopyCaption = { viewModel.copyCaptionToClipboard(context) },
                onShareToPlatform = { viewModel.shareToPlatform(it, context) },
                onShareToSystem = { viewModel.shareToSystem(context) },
                onExportVideo = {
                    viewModel.exportVideoSuccessToast(context)
                    viewModel.showShareModal(false)
                }
            )
        }
    }
}
