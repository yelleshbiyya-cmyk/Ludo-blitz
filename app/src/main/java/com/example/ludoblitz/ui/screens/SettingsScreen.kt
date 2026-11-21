package com.example.ludoblitz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ludoblitz.model.Language
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.service.LocalizationManager
import com.example.ludoblitz.ui.components.Game3DButton
import com.example.ludoblitz.ui.components.Game3DCard
import com.example.ludoblitz.ui.components.Section3DHeader
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoPurple
import com.example.ui.theme.BentoRose
import com.example.ui.theme.BentoSky
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    profile: PlayerProfile,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onSelectLanguage: (Language) -> Unit,
    onMusicVolumeChange: (Float) -> Unit = {},
    onSfxVolumeChange: (Float) -> Unit = {},
    onToggleMasterMute: (Boolean) -> Unit = { muted ->
        onToggleSound(!muted)
        if (muted) onToggleMusic(false)
        onToggleVibration(!muted)
    },
    modifier: Modifier = Modifier
) {
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    val lang = profile.languageCode
    val isMasterMuted = !profile.soundEnabled && !profile.musicEnabled

    if (showPrivacyDialog) {
        Legal3DDialog(
            title = "🔒 ${LocalizationManager.getString("privacy_policy", lang)}",
            leadText = "Ludo Blitz respects your privacy. All game progress, trophies, virtual Duck Coins, Gems, and settings are stored strictly locally on your device.",
            bulletText = "• Zero personal data is collected, transmitted, or shared with third parties.\n• No third-party advertising trackers or external analytics SDKs are bundled.\n• Safe for all ages and works completely offline.",
            buttonLabel = "OK, GOT IT",
            onDismiss = { showPrivacyDialog = false }
        )
    }

    if (showTermsDialog) {
        Legal3DDialog(
            title = "📜 ${LocalizationManager.getString("terms_service", lang)}",
            leadText = "Fair Play & Terms of Service:",
            bulletText = "• Ludo Blitz is a casual entertainment skill-and-strategy game. Virtual coins and gems have no real-world monetary value.\n• No real-money gambling, cash-out, or betting is offered or supported.\n• Cheating or modifying save files may reset leaderboard standings.",
            buttonLabel = "I AGREE",
            onDismiss = { showTermsDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Section3DHeader(
                title = "⚙️ GAME SETTINGS",
                badge = "AUDIO & PREFS",
                accentColor = RoyalGoldLight
            )
        }

        // AUDIO, MUSIC, VOLUME SLIDERS, VIBRATION & MASTER MUTE CARD
        item {
            Game3DCard(
                modifier = Modifier.fillMaxWidth(),
                gradientColors = listOf(Color(0xFF3730A3), Color(0xFF1E3A8A)),
                borderColors = listOf(RoyalGoldLight, BentoSky),
                cornerRadius = 20.dp,
                shadowColor = BentoPurple
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    SettingToggleRow(
                        label = "🔇 Master Mute All",
                        subtitle = if (isMasterMuted) "All sound effects & music silenced" else "Tap to mute all audio immediately",
                        checked = isMasterMuted,
                        accentColor = BentoRose,
                        testTagName = "settings_master_mute_switch",
                        onCheckedChange = onToggleMasterMute
                    )
                    SettingToggleRow(
                        label = "🔊 ${LocalizationManager.getString("sound_fx", lang)}",
                        subtitle = "Dice rolls, pawns, captures, power-ups & rewards",
                        checked = profile.soundEnabled,
                        accentColor = BentoEmerald,
                        testTagName = "settings_sound_switch",
                        onCheckedChange = onToggleSound
                    )
                    VolumeSliderRow(
                        label = "🎚️ Sound Effects Volume",
                        volume = profile.sfxVolume,
                        enabled = profile.soundEnabled,
                        accentColor = BentoEmerald,
                        testTagName = "settings_sfx_volume_slider",
                        onVolumeChange = onSfxVolumeChange
                    )
                    SettingToggleRow(
                        label = "🎵 ${LocalizationManager.getString("music", lang)}",
                        subtitle = if (profile.musicEnabled) "Upbeat royal arcade soundtrack ON" else "OFF by default — toggle ON to play soundtrack",
                        checked = profile.musicEnabled,
                        accentColor = BentoSky,
                        testTagName = "settings_music_switch",
                        onCheckedChange = onToggleMusic
                    )
                    VolumeSliderRow(
                        label = "🎼 Music Volume",
                        volume = profile.musicVolume,
                        enabled = profile.musicEnabled,
                        accentColor = BentoSky,
                        testTagName = "settings_music_volume_slider",
                        onVolumeChange = onMusicVolumeChange
                    )
                    SettingToggleRow(
                        label = "📳 ${LocalizationManager.getString("vibration", lang)}",
                        subtitle = "Tactile feedback on rolls, captures & victories",
                        checked = profile.vibrationEnabled,
                        accentColor = RoyalGoldLight,
                        testTagName = "settings_vibration_switch",
                        onCheckedChange = onToggleVibration
                    )
                }
            }
        }

        // LOCALIZATION
        item {
            Section3DHeader(
                title = "🌍 ${LocalizationManager.getString("language_select", lang).uppercase()}",
                badge = profile.languageCode.uppercase(),
                accentColor = BentoSky
            )
        }

        item {
            Game3DCard(
                modifier = Modifier.fillMaxWidth(),
                gradientColors = listOf(Color(0xFF3730A3), Color(0xFF1E1B4B)),
                borderColors = listOf(BentoSky, RoyalGoldLight.copy(alpha = 0.7f)),
                cornerRadius = 20.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Language.entries.forEach { l ->
                        val isSelected = profile.languageCode == l.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) Brush.horizontalGradient(listOf(RoyalGoldLight, BentoAmber))
                                    else Brush.horizontalGradient(listOf(Color(0xFF1E1B4B).copy(alpha = 0.55f), Color(0xFF1E1B4B).copy(alpha = 0.55f)))
                                )
                                .border(
                                    width = if (isSelected) 1.8.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onSelectLanguage(l) }
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                l.displayName,
                                color = if (isSelected) Color(0xFF1E1B4B) else Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            if (isSelected) {
                                Text("✓ ACTIVE", color = Color(0xFF1E1B4B), fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // LEGAL & PRIVACY
        item {
            Section3DHeader(
                title = "🛡️ LEGAL & FAIR PLAY",
                accentColor = Color(0xFF86EFAC)
            )
        }

        item {
            Game3DCard(
                modifier = Modifier.fillMaxWidth(),
                gradientColors = listOf(Color(0xFF312E81), Color(0xFF1E1B4B)),
                borderColors = listOf(Color(0xFF60A5FA), Color(0xFF86EFAC)),
                cornerRadius = 20.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF4338CA).copy(alpha = 0.7f))
                            .border(1.2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .clickable { showPrivacyDialog = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            LocalizationManager.getString("privacy_policy", lang),
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text("VIEW ▶", color = RoyalGoldLight, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF4338CA).copy(alpha = 0.7f))
                            .border(1.2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .clickable { showTermsDialog = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            LocalizationManager.getString("terms_service", lang),
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text("VIEW ▶", color = RoyalGoldLight, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }

        // ABOUT
        item {
            Section3DHeader(
                title = "ℹ️ ABOUT LUDO BLITZ",
                badge = "v1.0",
                accentColor = RoyalGoldLight
            )
        }

        item {
            Game3DCard(
                modifier = Modifier.fillMaxWidth(),
                gradientColors = listOf(Color(0xFF312E81), Color(0xFF1E3A8A)),
                borderColors = listOf(RoyalGoldLight.copy(alpha = 0.75f), BentoSky),
                cornerRadius = 20.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Ludo Blitz — Royal 3D Arcade Edition v1.0",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = RoyalGoldLight
                    )
                    Text(
                        "• Authentic 15×15 Classic Ludo Rules + 60-Second Blitz Mode",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Text(
                        "• Family-friendly offline play with virtual earned rewards only",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF86EFAC)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun VolumeSliderRow(
    label: String,
    volume: Float,
    enabled: Boolean,
    accentColor: Color,
    testTagName: String,
    onVolumeChange: (Float) -> Unit
) {
    val pct = (volume.coerceIn(0f, 1f) * 100f).roundToInt()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E1B4B).copy(alpha = 0.48f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.White else Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$pct%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = if (enabled) accentColor else Color(0xFF64748B)
            )
        }
        Slider(
            value = volume.coerceIn(0f, 1f),
            onValueChange = onVolumeChange,
            enabled = enabled,
            valueRange = 0f..1f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTagName),
            colors = SliderDefaults.colors(
                thumbColor = RoyalGoldLight,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color(0xFF334155),
                disabledThumbColor = Color(0xFF64748B),
                disabledActiveTrackColor = Color(0xFF475569)
            )
        )
    }
}

@Composable
private fun Legal3DDialog(
    title: String,
    leadText: String,
    bulletText: String,
    buttonLabel: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Game3DCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            gradientColors = listOf(Color(0xFF4338CA), Color(0xFF312E81), Color(0xFF1E1B4B)),
            borderColors = listOf(RoyalGoldLight, BentoSky),
            cornerRadius = 24.dp,
            borderWidth = 2.2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp, color = RoyalGoldLight)
                Text(leadText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, lineHeight = 19.sp)
                Text(bulletText, fontSize = 12.sp, color = Color(0xFFE0E7FF), lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Game3DButton(
                    onClick = onDismiss,
                    topColor = RoyalGoldLight,
                    bottomColor = BentoAmber,
                    ledgeColor = RoyalGoldDark,
                    height = 46.dp,
                    cornerRadius = 15.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(buttonLabel, color = Color(0xFF1E1B4B), fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    accentColor: Color,
    testTagName: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E1B4B).copy(alpha = 0.55f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = label,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 13.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFFE0E7FF),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTagName),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = Color(0xFFCBD5E1),
                uncheckedTrackColor = Color(0xFF334155)
            )
        )
    }
}
