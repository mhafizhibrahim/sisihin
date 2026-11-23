package com.sisihin.app.presentation.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sisihin.app.SisihinApplication
import com.sisihin.app.core.theme.LocalSisihinColors
import com.sisihin.app.core.theme.animateThemeColor
import com.sisihin.app.presentation.detail.GoalDetailScreen
import com.sisihin.app.presentation.detail.GoalDetailViewModel
import com.sisihin.app.presentation.form.GoalFormScreen
import com.sisihin.app.presentation.form.GoalFormViewModel
import com.sisihin.app.presentation.home.HomeMode
import com.sisihin.app.presentation.home.HomeScreen
import com.sisihin.app.presentation.home.HomeViewModel
import com.sisihin.app.presentation.settings.SettingsScreen
import com.sisihin.app.presentation.settings.SettingsViewModel
import kotlinx.coroutines.launch

private data class BottomDestination(val label: String, val icon: ImageVector)

private val BottomDestinations = listOf(
    BottomDestination("Tabungan", Icons.Outlined.Savings),
    BottomDestination("Tercapai", Icons.Outlined.EmojiEvents),
    BottomDestination("Pengaturan", Icons.Outlined.Settings)
)

private val NavPillShape = RoundedCornerShape(50)

@Composable
fun SisihinNavHost() {
    val context = LocalContext.current
    val container = (context.applicationContext as SisihinApplication).container
    val navController = rememberNavController()
    val colors = LocalSisihinColors.current
    val scope = rememberCoroutineScope()
    val pagerSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = 0.001f
    )

    // Diingat di luar NavHost agar posisi tab tidak reset saat membuka Detail/Form.
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { BottomDestinations.size }
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = backStackEntry?.destination?.route == "tabs"

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            if (showBottomBar) {
                SlimBottomBar(
                    currentPage = pagerState.currentPage,
                    onSelect = { index ->
                        scope.launch {
                            pagerState.animateScrollToPage(index, animationSpec = pagerSpring)
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "tabs",
            modifier = Modifier.padding(padding)
        ) {
            composable("tabs") {
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    flingBehavior = PagerDefaults.flingBehavior(
                        state = pagerState,
                        snapAnimationSpec = pagerSpring
                    )
                ) { page ->
                    when (page) {
                        0 -> {
                            val vm: HomeViewModel = viewModel(factory = simpleFactory { HomeViewModel(container) })
                            HomeScreen(
                                viewModel = vm,
                                mode = HomeMode.ONGOING,
                                onOpenGoal = { navController.navigate("detail/$it") },
                                onAddGoal = { navController.navigate("form") }
                            )
                        }
                        1 -> {
                            val vm: HomeViewModel = viewModel(factory = simpleFactory { HomeViewModel(container) })
                            HomeScreen(
                                viewModel = vm,
                                mode = HomeMode.ACHIEVED,
                                onOpenGoal = { navController.navigate("detail/$it") },
                                onAddGoal = { }
                            )
                        }
                        2 -> {
                            val vm: SettingsViewModel = viewModel(factory = simpleFactory { SettingsViewModel(container) })
                            SettingsScreen(viewModel = vm)
                        }
                    }
                }
            }
            composable(
                "detail/{goalId}",
                arguments = listOf(navArgument("goalId") { type = NavType.LongType })
            ) { backStack ->
                val goalId = backStack.arguments?.getLong("goalId") ?: 0L
                val vm: GoalDetailViewModel = viewModel(factory = simpleFactory { GoalDetailViewModel(container, goalId) })
                GoalDetailScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate("form?goalId=$goalId") }
                )
            }
            composable(
                "form?goalId={goalId}",
                arguments = listOf(navArgument("goalId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStack ->
                val goalId = backStack.arguments?.getLong("goalId") ?: -1L
                val vm: GoalFormViewModel = viewModel(factory = simpleFactory { GoalFormViewModel(container) })
                if (goalId > 0) vm.load(goalId)
                GoalFormScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
        }
    }
}

/** Bottom bar ramping ala Instagram: divider 0.6.dp + area item 66.dp murni + inset sistem terpisah. */
@Composable
private fun SlimBottomBar(currentPage: Int, onSelect: (Int) -> Unit) {
    val colors = LocalSisihinColors.current
    Surface(
        color = colors.background,
        contentColor = colors.textSecondary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(thickness = 0.6.dp, color = colors.hairlineBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomDestinations.forEachIndexed { index, destination ->
                    val active = currentPage == index
                    val navSpring = spring<Color>(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                    val pillColor by animateThemeColor(
                        targetValue = if (active) colors.accentSoftContainer else colors.background,
                        animationSpec = navSpring,
                        label = "navPill$index"
                    )
                    val iconColor by animateThemeColor(
                        targetValue = if (active) colors.accentBlue else colors.textMuted,
                        animationSpec = navSpring,
                        label = "navIcon$index"
                    )
                    val labelColor by animateThemeColor(
                        targetValue = if (active) colors.accentBlue else colors.textMuted,
                        animationSpec = navSpring,
                        label = "navLabel$index"
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelect(index) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(52.dp)
                                .height(28.dp)
                                .clip(NavPillShape)
                                .background(pillColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                modifier = Modifier.size(22.dp),
                                tint = iconColor
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = destination.label,
                            color = labelColor,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            maxLines = 1,
                            fontWeight = if (active) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun simpleFactory(create: () -> Any): androidx.lifecycle.ViewModelProvider.Factory {
    return object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return create() as T
        }
    }
}
