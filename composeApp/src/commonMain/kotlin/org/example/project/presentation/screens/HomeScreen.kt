package org.example.project.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.stringResource
import voicetotext.composeapp.generated.resources.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.data.io.formatTimestamp
import org.example.project.domain.model.Note
import org.example.project.presentation.viewmodels.HomeState
import org.example.project.presentation.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToRecording: () -> Unit,
    onNavigateToNoteDetails: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val auroraBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFEEF2FF), // Pale Indigo
                Color(0xFFF3E8FF), // Soft Purple
                Color(0xFFFFF1F2), // Pale Rose
                Color(0xFFF0FDF4)  // Minty White
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).background(brush = auroraBrush),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.home_title)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(Res.string.home_settings_desc)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = Color(0xFF1E293B),
                    actionIconContentColor = Color(0xFF1E293B)
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onNavigateToRecording,
                shape = CircleShape,
                containerColor = Color(0xFF6366F1),
                contentColor = Color.White,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(Res.string.home_record_desc),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshNotes() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is HomeState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is HomeState.Success -> {
                    if (currentState.notes.isEmpty()) {
                        EmptyNotesContent(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                        ) {
                            items(currentState.notes, key = { it.id }) { note ->
                                AnimatedNoteCard(
                                    note = note,
                                    onClick = { onNavigateToNoteDetails(note.id) },
                                    onDeleteConfirm = { viewModel.deleteNoteById(note.id) }
                                )
                            }
                        }
                    }
                }

                is HomeState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFF43F5E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNotesContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MicNone,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFF475569).copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.home_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.home_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF475569)
        )
    }
}

@Composable
private fun AnimatedNoteCard(
    note: Note,
    onClick: () -> Unit,
    onDeleteConfirm: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(Res.string.home_delete_dialog_title)) },
            text = { Text(stringResource(Res.string.home_delete_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        // Play shrink + fade animation, then delete
                        scope.launch {
                            isVisible = false
                            delay(400) // wait for animation to finish
                            onDeleteConfirm()
                        }
                    }
                ) {
                    Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            }
        )
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 400),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = tween(durationMillis = 300))
    ) {
        NoteCard(
            note = note,
            onClick = onClick,
            onDeleteClick = { showDeleteDialog = true }
        )
    }
}

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.75f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title.ifBlank { stringResource(Res.string.home_untitled_note) },
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF1E293B),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onDeleteClick, modifier = Modifier.padding(start = 8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.action_delete),
                        tint = Color(0xFFF43F5E).copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = formatTimestamp(note.createdAt),
                color = Color(0xFF475569),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            val cleanContent = note.content
                .replace(Regex("#{1,6}\\s?"), "")
                .replace(Regex("\\*\\*"), "")
                .trim()

            Text(
                text = cleanContent,
                color = Color(0xFF475569),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
