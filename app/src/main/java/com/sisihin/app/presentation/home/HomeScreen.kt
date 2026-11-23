package com.sisihin.app.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowDown
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowUp
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sisihin.app.core.theme.LocalSisihinColors
import com.sisihin.app.core.ui.GoalPlaceholderVisual
import com.sisihin.app.core.util.CurrencyFormatter
import com.sisihin.app.core.util.SavingsCalculator
import com.sisihin.app.data.local.GoalWithCollected
import com.sisihin.app.domain.model.CurrencyCode
import com.sisihin.app.domain.model.PlanFrequency
import com.sisihin.app.domain.model.SortDirection
import com.sisihin.app.domain.model.SortOption
import java.io.File

enum class HomeMode { ONGOING, ACHIEVED }

private val CardShape = RoundedCornerShape(16.dp)
private val ThumbShape = RoundedCornerShape(12.dp)
private val FabShape = RoundedCornerShape(999.dp)
private val ChipShape = RoundedCornerShape(999.dp)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    mode: HomeMode,
    onOpenGoal: (Long) -> Unit,
    onAddGoal: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalSisihinColors.current

    Scaffold(
        containerColor = colors.background,
        // Outer Scaffold di SisihinNavHost sudah menangani bottomBar + system bars.
        // Tanpa ini, inset navigation bar diterapkan dua kali dan menyisakan celah hitam.
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(colors.topBarBackground),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (mode == HomeMode.ONGOING) {
                        buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = colors.textPrimary,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.6).sp
                                )
                            ) {
                                append("Sisihin")
                            }
                            withStyle(
                                SpanStyle(
                                    color = colors.accentBlue,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) {
                                append(".")
                            }
                        }
                    } else {
                        buildAnnotatedString { append("Tercapai") }
                    },
                    modifier = Modifier.padding(start = 16.dp),
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    ) { padding ->
        val goals = if (mode == HomeMode.ONGOING) uiState.ongoingGoals else uiState.achievedGoals
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colors.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (mode == HomeMode.ONGOING) {
                    item(contentType = "summary") {
                        SummaryHeader(totalCollected = uiState.totalCollected, activeCount = uiState.activeCount)
                    }
                    item(contentType = "sort") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SortOptionDropdown(selected = uiState.sortOption, onSelect = { viewModel.setSortOption(it) })
                            SortDirectionDropdown(selected = uiState.sortDirection, onSelect = { viewModel.setSortDirection(it) })
                        }
                    }
                }

                if (goals.isEmpty()) {
                    item(contentType = "empty") {
                        EmptyState(
                            text = if (mode == HomeMode.ONGOING) {
                                "Belum ada celengan berlangsung"
                            } else {
                                "Belum ada celengan yang tercapai"
                            },
                            icon = if (mode == HomeMode.ONGOING) Icons.Outlined.Savings else Icons.Outlined.EmojiEvents
                        )
                    }
                } else {
                    items(goals, key = { it.goal.id }, contentType = { "goal" }) { gwc ->
                        if (mode == HomeMode.ONGOING) {
                            OngoingGoalCard(
                                gwc = gwc,
                                onClick = { onOpenGoal(gwc.goal.id) }
                            )
                        } else {
                            AchievedGoalCard(gwc = gwc, onClick = { onOpenGoal(gwc.goal.id) })
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = mode == HomeMode.ONGOING,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut(targetScale = 0.9f)
            ) {
                Surface(
                    onClick = onAddGoal,
                    modifier = Modifier.height(48.dp),
                    shape = FabShape,
                    color = colors.accentSoftContainer,
                    border = BorderStroke(0.8.dp, colors.hairlineBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = colors.accentSoftContent
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Tambah Celengan",
                            color = colors.accentSoftContent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryHeader(totalCollected: Long, activeCount: Int) {
    val colors = LocalSisihinColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = colors.cardSurface,
        border = BorderStroke(0.8.dp, colors.hairlineBorder)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TOTAL UANG TERKUMPUL",
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = CurrencyFormatter.format(totalCollected, CurrencyCode.IDR),
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
            androidx.compose.material3.VerticalDivider(
                modifier = Modifier.height(44.dp),
                thickness = 0.8.dp,
                color = colors.divider
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(0.7f)) {
                Text(
                    text = "TARGET BERJALAN",
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "$activeCount",
                    color = colors.accentBlue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }
    }
}

@Composable
private fun EmptyState(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val colors = LocalSisihinColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(56.dp), tint = colors.textMuted)
        Spacer(Modifier.height(12.dp))
        Text(text, color = colors.textSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun SortChipShell(onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    val colors = LocalSisihinColors.current
    Surface(
        modifier = Modifier.height(38.dp).clickable(onClick = onClick),
        shape = ChipShape,
        color = colors.cardSurface,
        border = BorderStroke(0.8.dp, colors.hairlineBorder)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, content = content)
    }
}

@Composable
private fun DropdownArrow() {
    val colors = LocalSisihinColors.current
    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, modifier = Modifier.size(20.dp), tint = colors.textSecondary)
}

@Composable
private fun SortOptionDropdown(selected: SortOption, onSelect: (SortOption) -> Unit) {
    val colors = LocalSisihinColors.current
    var open by remember { mutableStateOf(false) }
    Box {
        SortChipShell(onClick = { open = true }) {
            Icon(Icons.Rounded.Sort, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.textSecondary)
            Spacer(Modifier.width(6.dp))
            Text(selected.label, color = colors.textPrimary, fontSize = 13.sp)
            Spacer(Modifier.width(6.dp))
            DropdownArrow()
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            SortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label, color = colors.textPrimary, fontSize = 13.sp) },
                    onClick = { open = false; onSelect(option) }
                )
            }
        }
    }
}

@Composable
private fun SortDirectionDropdown(selected: SortDirection, onSelect: (SortDirection) -> Unit) {
    val colors = LocalSisihinColors.current
    var open by remember { mutableStateOf(false) }
    Box {
        SortChipShell(onClick = { open = true }) {
            Icon(
                imageVector = if (selected == SortDirection.ASC) Icons.Rounded.KeyboardDoubleArrowUp else Icons.Rounded.KeyboardDoubleArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colors.textSecondary
            )
            Spacer(Modifier.width(6.dp))
            Text(selected.label, color = colors.textPrimary, fontSize = 13.sp)
            Spacer(Modifier.width(6.dp))
            DropdownArrow()
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            SortDirection.entries.forEach { direction ->
                DropdownMenuItem(
                    text = { Text(direction.label, color = colors.textPrimary, fontSize = 13.sp) },
                    onClick = { open = false; onSelect(direction) }
                )
            }
        }
    }
}

@Composable
private fun GoalThumb(imagePath: String?, title: String) {
    val context = LocalContext.current
    val colors = LocalSisihinColors.current
    val photoFile = imagePath?.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.exists() }
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(ThumbShape)
            .then(
                if (photoFile != null) {
                    Modifier
                        .background(colors.photoPlaceholder)
                        .border(0.8.dp, colors.hairlineBorder, ThumbShape)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (photoFile != null) {
            val request = remember(photoFile, context) {
                ImageRequest.Builder(context).data(photoFile).size(300, 300).crossfade(150).build()
            }
            AsyncImage(
                model = request,
                contentDescription = "Foto $title",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            GoalPlaceholderVisual(
                title = title,
                isBanner = false,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun OngoingGoalCard(gwc: GoalWithCollected, onClick: () -> Unit) {
    val goal = gwc.goal
    val colors = LocalSisihinColors.current
    val percent = SavingsCalculator.percent(goal.targetAmount, gwc.collected).toInt()
    val frequency = when (goal.planFrequency) {
        PlanFrequency.DAILY -> "Perhari"
        PlanFrequency.WEEKLY -> "Perminggu"
        PlanFrequency.MONTHLY -> "Perbulan"
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = colors.cardSurface,
        border = BorderStroke(0.8.dp, colors.hairlineBorder)
    ) {
        Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp)) {
            GoalThumb(goal.imagePath, goal.title)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = goal.title,
                        modifier = Modifier.weight(1f),
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = if (goal.reminderEnabled) Icons.Outlined.NotificationsActive else Icons.Outlined.NotificationsOff,
                        contentDescription = if (goal.reminderEnabled) "Pengingat aktif" else "Pengingat nonaktif",
                        modifier = Modifier.size(18.dp),
                        tint = if (goal.reminderEnabled) colors.accentBlue else colors.textMuted
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = CurrencyFormatter.format(goal.targetAmount, goal.currencyCode),
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
                Text(
                    text = "${CurrencyFormatter.format(goal.planAmount, goal.currencyCode)} $frequency",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier.weight(1f).height(4.dp).clip(ChipShape),
                        color = colors.accentBlue,
                        trackColor = colors.ringTrack
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$percent%",
                        color = colors.accentBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievedGoalCard(gwc: GoalWithCollected, onClick: () -> Unit) {
    val goal = gwc.goal
    val colors = LocalSisihinColors.current
    val days = SavingsCalculator.daysTaken(goal.createdAtMillis, goal.completedAtMillis ?: System.currentTimeMillis())
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = colors.cardSurface,
        border = BorderStroke(0.8.dp, colors.hairlineBorder)
    ) {
        Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp)) {
            GoalThumb(goal.imagePath, goal.title)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = goal.title,
                        modifier = Modifier.weight(1f),
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("100%", color = colors.positiveGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = CurrencyFormatter.format(goal.targetAmount, goal.currencyCode),
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(thickness = 0.8.dp, color = colors.divider)
                Spacer(Modifier.height(8.dp))
                Text("Tercapai dalam $days hari", color = colors.textSecondary, fontSize = 12.sp)
            }
        }
    }
}
