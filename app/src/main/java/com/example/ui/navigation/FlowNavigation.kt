package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.motion.FlowHaptics
import com.example.ui.motion.FlowMotion
import com.example.ui.motion.flowPress
import com.example.ui.screens.builder.RoutineBuilderScreen
import com.example.ui.screens.developer.DeveloperScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.logs.AutomationLogsScreen
import com.example.ui.screens.modes.ModesScreen
import com.example.ui.screens.notifications.NotificationDebuggerScreen
import com.example.ui.screens.permissions.PermissionCenterScreen
import com.example.ui.screens.routines.RoutineDetailScreen
import com.example.ui.screens.routines.RoutinesScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.templates.TemplatesGalleryScreen
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.LocalFlowGlassColors
import com.example.ui.viewmodel.FlowViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Routines : Screen("routines", "Routines", Icons.Default.AutoAwesome)
    object Modes : Screen("modes", "Modes", Icons.Default.Tune)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    object RoutineDetail : Screen("routine_detail/{routineId}", "Routine Details") {
        fun createRoute(id: String) = "routine_detail/$id"
    }

    object RoutineBuilder : Screen("routine_builder?routineId={routineId}", "Routine Editor") {
        fun createRoute(id: String? = null) = if (id != null) "routine_builder?routineId=$id" else "routine_builder"
    }

    object PermissionCenter : Screen("permission_center", "Permissions")
    object AutomationLogs : Screen("automation_logs", "Logs")
    object Developer : Screen("developer", "Developer")
    object Templates : Screen("templates", "Templates")
    object NotificationDebugger : Screen("notification_debugger", "Notification Debugger")
}

@Composable
fun FlowAppContent(
    viewModel: FlowViewModel,
    navController: NavHostController = rememberNavController()
) {
    val glassColors = LocalFlowGlassColors.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for user feedback messages
    LaunchedEffect(Unit) {
        viewModel.userFeedback.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = remember {
        listOf(
            Screen.Home,
            Screen.Routines,
            Screen.Modes,
            Screen.Settings
        )
    }

    val isTopLevelDestination = remember(currentRoute) {
        bottomNavItems.any { it.route == currentRoute }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isTopLevelDestination) {
                val hapticFeedback = LocalHapticFeedback.current
                val view = LocalView.current

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(glassColors.glassSurface)
                            .border(1.dp, glassColors.glassBorder, RoundedCornerShape(24.dp))
                            .padding(vertical = 6.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomNavItems.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            val icon = screen.icon ?: Icons.Default.Home
                            val interactionSource = remember { MutableInteractionSource() }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) FlowCyan.copy(alpha = 0.16f) else Color.Transparent)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = ripple(),
                                        role = Role.Tab
                                    ) {
                                        if (currentRoute != screen.route) {
                                            FlowHaptics.tick(hapticFeedback, view)
                                            navController.navigate(screen.route) {
                                                popUpTo(Screen.Home.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = screen.title,
                                    tint = if (isSelected) FlowCyan else glassColors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = expandHorizontally() + fadeIn(),
                                    exit = shrinkHorizontally() + fadeOut()
                                ) {
                                    Text(
                                        text = screen.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FlowCyan,
                                        modifier = Modifier.padding(start = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { (it * 0.12f).toInt() },
                    animationSpec = FlowMotion.PageEnterOffset
                ) + fadeIn(animationSpec = FlowMotion.PageEnter)
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { (-it * 0.08f).toInt() },
                    animationSpec = FlowMotion.PageExitOffset
                ) + fadeOut(animationSpec = FlowMotion.PageExit)
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { (-it * 0.08f).toInt() },
                    animationSpec = FlowMotion.PageEnterOffset
                ) + fadeIn(animationSpec = FlowMotion.PageEnter)
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { (it * 0.12f).toInt() },
                    animationSpec = FlowMotion.PageExitOffset
                ) + fadeOut(animationSpec = FlowMotion.PageExit)
            }
        ) {
            composable(Screen.Home.route) {
                val rawRoutines by viewModel.rawRoutines.collectAsState()
                val modes by viewModel.modes.collectAsState()
                val logs by viewModel.executionLogs.collectAsState()

                HomeScreen(
                    routines = rawRoutines,
                    modes = modes,
                    logs = logs,
                    onNavigateToCreateRoutine = {
                        navController.navigate(Screen.RoutineBuilder.createRoute(null))
                    },
                    onNavigateToRoutineDetail = { id ->
                        navController.navigate(Screen.RoutineDetail.createRoute(id))
                    },
                    onNavigateToPermissions = {
                        navController.navigate(Screen.PermissionCenter.route)
                    },
                    onToggleRoutine = { viewModel.toggleRoutineEnabled(it) },
                    onRunRoutine = { viewModel.runRoutineNow(it) },
                    onToggleMode = { viewModel.toggleModeActive(it) },
                    onNavigateToTemplates = {
                        navController.navigate(Screen.Templates.route)
                    },
                    onNavigateToNotificationDebugger = {
                        navController.navigate(Screen.NotificationDebugger.route)
                    }
                )
            }

            composable(Screen.Routines.route) {
                val filteredRoutines by viewModel.filteredRoutines.collectAsState()
                val searchQuery by viewModel.routineSearchQuery.collectAsState()
                val filterFavorites by viewModel.routineFilterFavorites.collectAsState()
                val sortOrder by viewModel.routineSortOrder.collectAsState()

                RoutinesScreen(
                    routines = filteredRoutines,
                    searchQuery = searchQuery,
                    filterFavorites = filterFavorites,
                    sortOrder = sortOrder,
                    onSearchQueryChange = { viewModel.routineSearchQuery.value = it },
                    onFilterFavoritesChange = { viewModel.routineFilterFavorites.value = it },
                    onSortOrderChange = { viewModel.routineSortOrder.value = it },
                    onNavigateToCreate = {
                        navController.navigate(Screen.RoutineBuilder.createRoute(null))
                    },
                    onNavigateToDetail = { id ->
                        navController.navigate(Screen.RoutineDetail.createRoute(id))
                    },
                    onToggleRoutine = { viewModel.toggleRoutineEnabled(it) },
                    onToggleFavorite = { viewModel.toggleRoutineFavorite(it) },
                    onDuplicateRoutine = { viewModel.duplicateRoutine(it) },
                    onDeleteRoutine = { viewModel.deleteRoutine(it) },
                    onRunRoutine = { viewModel.runRoutineNow(it) },
                    onNavigateToTemplates = { navController.navigate(Screen.Templates.route) }
                )
            }

            composable(Screen.Modes.route) {
                val modes by viewModel.modes.collectAsState()

                ModesScreen(
                    modes = modes,
                    onToggleMode = { viewModel.toggleModeActive(it) }
                )
            }

            composable(Screen.Settings.route) {
                val themeMode by viewModel.themeMode.collectAsState()
                val oledBlack by viewModel.oledBlack.collectAsState()
                val isDeveloperMode by viewModel.isDeveloperMode.collectAsState()
                val isFlipToShhhEnabled by viewModel.isFlipToShhhEnabled.collectAsState()
                val isShakeTriggerEnabled by viewModel.isShakeTriggerEnabled.collectAsState()
                val isCalendarTriggerEnabled by viewModel.isCalendarTriggerEnabled.collectAsState()
                val phoneOrientation by viewModel.phoneOrientation.collectAsState()
                val modes by viewModel.modes.collectAsState()

                SettingsScreen(
                    themeMode = themeMode,
                    oledBlack = oledBlack,
                    isDeveloperMode = isDeveloperMode,
                    isFlipToShhhEnabled = isFlipToShhhEnabled,
                    isShakeTriggerEnabled = isShakeTriggerEnabled,
                    isCalendarTriggerEnabled = isCalendarTriggerEnabled,
                    phoneOrientation = phoneOrientation,
                    modes = modes,
                    onThemeModeChange = { viewModel.setThemeMode(it) },
                    onOledBlackChange = { viewModel.setOledBlack(it) },
                    onDeveloperModeChange = { viewModel.setDeveloperMode(it) },
                    onFlipToShhhChange = { viewModel.setFlipToShhhEnabled(it) },
                    onShakeTriggerChange = { viewModel.setShakeTriggerEnabled(it) },
                    onCalendarTriggerChange = { viewModel.setCalendarTriggerEnabled(it) },
                    onSimulateNfcTap = { viewModel.simulateNfcTap(it) },
                    onExportBackup = { viewModel.exportBackup() },
                    onImportBackup = { viewModel.importBackup(it) },
                    onNavigateToPermissions = { navController.navigate(Screen.PermissionCenter.route) },
                    onNavigateToLogs = { navController.navigate(Screen.AutomationLogs.route) },
                    onNavigateToDeveloper = { navController.navigate(Screen.Developer.route) }
                )
            }

            composable(
                route = Screen.RoutineDetail.route,
                arguments = listOf(navArgument("routineId") { type = NavType.StringType })
            ) { backStackEntry ->
                val rawRoutines by viewModel.rawRoutines.collectAsState()
                val routineId = backStackEntry.arguments?.getString("routineId")
                val routine = remember(rawRoutines, routineId) {
                    rawRoutines.firstOrNull { it.id == routineId }
                }

                RoutineDetailScreen(
                    routine = routine,
                    onNavigateBack = { navController.popBackStack() },
                    onEditRoutine = {
                        navController.navigate(Screen.RoutineBuilder.createRoute(routineId))
                    },
                    onDeleteRoutine = {
                        if (routineId != null) {
                            viewModel.deleteRoutine(routineId)
                            navController.popBackStack()
                        }
                    },
                    onToggleEnabled = {
                        if (routine != null) viewModel.toggleRoutineEnabled(routine)
                    },
                    onRunNow = {
                        if (routine != null) viewModel.runRoutineNow(routine)
                    }
                )
            }

            composable(
                route = Screen.RoutineBuilder.route,
                arguments = listOf(navArgument("routineId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val rawRoutines by viewModel.rawRoutines.collectAsState()
                val routineId = backStackEntry.arguments?.getString("routineId")
                val existingRoutine = remember(rawRoutines, routineId) {
                    if (routineId != null) rawRoutines.firstOrNull { it.id == routineId } else null
                }

                RoutineBuilderScreen(
                    initialRoutine = existingRoutine,
                    onSave = { savedRoutine ->
                        viewModel.saveRoutine(savedRoutine)
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PermissionCenter.route) {
                val permissions by viewModel.permissions.collectAsState()

                PermissionCenterScreen(
                    permissions = permissions,
                    onRefresh = { viewModel.refreshPermissions() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AutomationLogs.route) {
                val logs by viewModel.executionLogs.collectAsState()

                AutomationLogsScreen(
                    logs = logs,
                    onClearLogs = { viewModel.clearLogs() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Developer.route) {
                DeveloperScreen(
                    onTriggerCondition = { viewModel.testSimulatedTrigger(it) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Templates.route) {
                TemplatesGalleryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onImportTemplate = { routine ->
                        viewModel.saveRoutine(routine)
                        navController.popBackStack()
                    },
                    onCustomizeTemplate = { routine ->
                        viewModel.saveRoutine(routine)
                        navController.navigate(Screen.RoutineBuilder.createRoute(routine.id))
                    }
                )
            }

            composable(Screen.NotificationDebugger.route) {
                NotificationDebuggerScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPermissions = { navController.navigate(Screen.PermissionCenter.route) },
                    onCreateRoutineFromNotification = { routine ->
                        viewModel.saveRoutine(routine)
                        navController.navigate(Screen.RoutineBuilder.createRoute(routine.id))
                    }
                )
            }
        }
    }
}
