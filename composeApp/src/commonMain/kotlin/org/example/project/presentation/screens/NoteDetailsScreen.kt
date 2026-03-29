package org.example.project.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import org.example.project.auth.getPlatformContext
import org.example.project.data.io.formatTimestamp
import org.example.project.presentation.viewmodels.NoteDetailsState
import org.example.project.presentation.viewmodels.NoteDetailsViewModel
import org.jetbrains.compose.resources.stringResource
import voicetotext.composeapp.generated.resources.*

// Since these need to be evaluated in a Composable to get string resources,
// we'll change AiAction to hold the StringResource instance instead of a raw String.
private data class AiAction(
    val labelRes: org.jetbrains.compose.resources.StringResource,
    val icon: ImageVector,
    val type: String
)

private val aiActions = listOf(
    AiAction(Res.string.note_action_summarize, Icons.Default.Summarize, "Summary"),
    AiAction(Res.string.note_action_items, Icons.Default.Checklist, "Action Items"),
    AiAction(Res.string.note_action_to_email, Icons.Default.Email, "To Email"),
    AiAction(Res.string.note_action_translate, Icons.Default.Translate, "Translate")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailsScreen(
    viewModel: NoteDetailsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val platformContext = getPlatformContext()
    val clipboardManager = LocalClipboardManager.current
    var isTranslateMenuExpanded by remember { mutableStateOf(false) }
    var currentTargetLanguage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isPlaying by viewModel.isPlaying.collectAsState()
    val audioPosition by viewModel.audioPosition.collectAsState()
    val audioDuration by viewModel.audioDuration.collectAsState()

    val isEditing by viewModel.isEditing.collectAsState()
    val editedTitle by viewModel.editedTitle.collectAsState()
    val editedContent by viewModel.editedContent.collectAsState()

    val supportedLanguages = listOf(
        "English", "Bulgarian", "German", "Spanish", "French", "Italian", "Russian"
    )

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(Res.string.note_delete_dialog_title)) },
            text = { Text(stringResource(Res.string.note_delete_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteNote { onNavigateBack() }
                }) {
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

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    val fallbackTitle = stringResource(Res.string.note_details_title)
                    val title = when (val s = state) {
                        is NoteDetailsState.Success -> s.note.title.ifBlank { stringResource(Res.string.home_untitled_note) }
                        else -> fallbackTitle
                    }
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.action_back))
                    }
                },
                actions = {
                    if (state is NoteDetailsState.Success) {
                        val note = (state as NoteDetailsState.Success).note
                        IconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Save" else "Edit"
                            )
                        }
                        IconButton(onClick = { viewModel.shareNote(note) }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(Res.string.action_share))
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.action_delete))
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val currentState = state) {
                is NoteDetailsState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NoteDetailsState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(currentState.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.retry() }) {
                            Text(stringResource(Res.string.action_retry))
                        }
                    }
                }
                is NoteDetailsState.Success -> {
                    val note = currentState.note
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (isEditing) {
                            OutlinedTextField(
                                value = editedTitle,
                                onValueChange = { viewModel.updateEditedTitle(it) },
                                label = { Text(stringResource(Res.string.home_untitled_note)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editedContent,
                                onValueChange = { viewModel.updateEditedContent(it) },
                                label = { Text(stringResource(Res.string.note_details_content_label)) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 5
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.saveChanges() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(Res.string.action_save))
                            }
                        } else {
                            Text(
                                text = formatTimestamp(note.createdAt),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Audio Player (no card border)
                            if (note.audioUrl != null) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (isPlaying) viewModel.pauseAudio()
                                                else viewModel.playAudio(note.audioUrl)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = if (isPlaying) "Pause" else "Play",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        
                                        Text(
                                            text = "${formatMillis(audioPosition)} / ${formatMillis(audioDuration)}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    
                                    Slider(
                                        value = audioPosition.toFloat(),
                                        onValueChange = { viewModel.seekAudio(it) },
                                        valueRange = 0f..audioDuration.toFloat().coerceAtLeast(1f),
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Text(
                                text = stringResource(Res.string.note_ai_actions_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                aiActions.forEach { action ->
                                    if (action.type == "Translate") {
                                        Box {
                                            AssistChip(
                                                onClick = { isTranslateMenuExpanded = true },
                                                label = { Text(stringResource(action.labelRes)) },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = action.icon,
                                                        contentDescription = stringResource(action.labelRes),
                                                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                                                    )
                                                }
                                            )
                                            DropdownMenu(
                                                expanded = isTranslateMenuExpanded,
                                                onDismissRequest = { isTranslateMenuExpanded = false }
                                            ) {
                                                supportedLanguages.forEach { language ->
                                                    DropdownMenuItem(
                                                        text = { Text(language) },
                                                        onClick = {
                                                            isTranslateMenuExpanded = false
                                                            currentTargetLanguage = language
                                                            viewModel.performAiAction("Translate", note.content, language)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        AssistChip(
                                            onClick = { viewModel.performAiAction(action.type, note.content) },
                                            label = { Text(stringResource(action.labelRes)) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = action.icon,
                                                    contentDescription = stringResource(action.labelRes),
                                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            AnimatedVisibility(
                                visible = isAiLoading || aiResponse != null,
                                enter = fadeIn() + expandVertically()
                            ) {
                                Column {
                                    ElevatedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.elevatedCardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "AI Response",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                                if (isAiLoading) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            aiResponse?.let {
                                                Markdown(content = it)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                TextButton(
                                                    onClick = { clipboardManager.setText(AnnotatedString(it)) },
                                                    modifier = Modifier.align(Alignment.End)
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Copy")
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Markdown(
                                content = note.content,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

private fun formatMillis(millis: Int): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
