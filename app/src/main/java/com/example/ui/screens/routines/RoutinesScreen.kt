package com.example.ui.screens.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.Routine
import com.example.ui.components.FlowEmptyState
import com.example.ui.components.FlowGlassCard
import com.example.ui.components.FlowRowItem
import com.example.ui.components.FlowSwitch
import com.example.ui.components.IconHelper
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.LocalFlowGlassColors
import com.example.ui.viewmodel.RoutineSortOrder

@Composable
fun RoutinesScreen(
    routines: List<Routine>,
    searchQuery: String,
    filterFavorites: Boolean,
    sortOrder: RoutineSortOrder,
    onSearchQueryChange: (String) -> Unit,
    onFilterFavoritesChange: (Boolean) -> Unit,
    onSortOrderChange: (RoutineSortOrder) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onToggleRoutine: (Routine) -> Unit,
    onToggleFavorite: (Routine) -> Unit,
    onDuplicateRoutine: (Routine) -> Unit,
    onDeleteRoutine: (String) -> Unit,
    onRunRoutine: (Routine) -> Unit,
    onNavigateToTemplates: () -> Unit = {}
) {
    val glassColors = LocalFlowGlassColors.current
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = FlowCyan,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 76.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Routine")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item(key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Routines",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = glassColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${routines.size} automations",
                            style = MaterialTheme.typography.bodyMedium,
                            color = glassColors.textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(glassColors.glassSurface)
                            .border(1.dp, glassColors.glassBorder, RoundedCornerShape(10.dp))
                            .clickable { onNavigateToTemplates() }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = FlowCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Templates",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = glassColors.textPrimary
                            )
                        }
                    }
                }
            }

            // Compact Search & Filter Bar
            item(key = "search_filter") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search routines...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = glassColors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlowCyan,
                            unfocusedBorderColor = glassColors.glassBorder,
                            focusedContainerColor = glassColors.glassSurface,
                            unfocusedContainerColor = glassColors.glassSurface
                        )
                    )

                    // Favorite Filter Chip
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (filterFavorites) FlowCyan.copy(alpha = 0.2f) else glassColors.glassSurface)
                            .border(1.dp, if (filterFavorites) FlowCyan else glassColors.glassBorder, RoundedCornerShape(14.dp))
                            .clickable { onFilterFavoritesChange(!filterFavorites) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (filterFavorites) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Filter Favorites",
                            tint = if (filterFavorites) FlowCyan else glassColors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Sort Order Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(glassColors.glassSurface)
                            .border(1.dp, glassColors.glassBorder, RoundedCornerShape(14.dp))
                            .clickable { showSortMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Sort",
                            tint = glassColors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Default (Favorites first)") },
                                onClick = {
                                    onSortOrderChange(RoutineSortOrder.FAVORITES_FIRST)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Alphabetical") },
                                onClick = {
                                    onSortOrderChange(RoutineSortOrder.ALPHABETICAL)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Recently Triggered") },
                                onClick = {
                                    onSortOrderChange(RoutineSortOrder.LAST_TRIGGERED)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("By Priority") },
                                onClick = {
                                    onSortOrderChange(RoutineSortOrder.PRIORITY)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Routine List
            if (routines.isEmpty()) {
                item(key = "empty_state") {
                    FlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                        FlowEmptyState(
                            title = if (searchQuery.isNotBlank() || filterFavorites) "No routines matched" else "No routines yet",
                            description = if (searchQuery.isNotBlank() || filterFavorites) "Try adjusting your search terms or filter" else "Create your first automation routine",
                            buttonText = "Create Routine",
                            onButtonClick = onNavigateToCreate,
                            icon = Icons.Default.AutoAwesome
                        )
                    }
                }
            } else {
                // List of compact routine rows
                items(routines, key = { it.id }, contentType = { "routine_row" }) { routine ->
                    RoutineCompactItem(
                        routine = routine,
                        onClick = { onNavigateToDetail(routine.id) },
                        onToggleEnabled = { onToggleRoutine(routine) },
                        onToggleFavorite = { onToggleFavorite(routine) },
                        onDuplicate = { onDuplicateRoutine(routine) },
                        onDelete = { onDeleteRoutine(routine.id) },
                        onRun = { onRunRoutine(routine) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineCompactItem(
    routine: Routine,
    onClick: () -> Unit,
    onToggleEnabled: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onRun: () -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    var showMenu by remember { mutableStateOf(false) }

    val accentColor = remember(routine.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(routine.colorHex))
        } catch (_: Exception) {
            FlowCyan
        }
    }

    val triggerSummary = remember(routine.conditions) {
        routine.conditions.firstOrNull()?.summary
            ?: if (routine.conditions.isEmpty()) "Manual" else "${routine.conditions.size} triggers"
    }

    val routineIcon = remember(routine.iconName) {
        IconHelper.getIconByName(routine.iconName)
    }

    FlowGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = routineIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Name & Trigger summary
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = glassColors.textPrimary,
                        maxLines = 1
                    )
                    if (routine.isFavorite) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = triggerSummary,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = glassColors.textSecondary,
                    maxLines = 1
                )
            }

            // Actions: Quick more menu and switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = glassColors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Run Now") },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = FlowCyan) },
                            onClick = {
                                onRun()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (routine.isFavorite) "Remove from Favorites" else "Add to Favorites") },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                            onClick = {
                                onToggleFavorite()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                onDuplicate()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFEF4444)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                            onClick = {
                                onDelete()
                                showMenu = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                FlowSwitch(
                    checked = routine.isEnabled,
                    onCheckedChange = { onToggleEnabled() }
                )
            }
        }
    }
}
