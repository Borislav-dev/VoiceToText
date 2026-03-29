package org.example.project.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.presentation.viewmodels.OnboardingViewModel
import org.jetbrains.compose.resources.stringResource
import voicetotext.composeapp.generated.resources.*

private data class OnboardingPage(
    val titleRes: org.jetbrains.compose.resources.StringResource,
    val descriptionRes: org.jetbrains.compose.resources.StringResource,
    val icon: ImageVector
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onOnboardingFinished: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            titleRes = Res.string.onboarding_slide1_title,
            descriptionRes = Res.string.onboarding_slide1_desc,
            icon = Icons.Default.FormatQuote
        ),
        OnboardingPage(
            titleRes = Res.string.onboarding_slide2_title,
            descriptionRes = Res.string.onboarding_slide2_desc,
            icon = Icons.Default.AutoAwesome
        ),
        OnboardingPage(
            titleRes = Res.string.onboarding_slide3_title,
            descriptionRes = Res.string.onboarding_slide3_desc,
            icon = Icons.Default.CloudDone
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(onClick = {
                        viewModel.completeOnboarding()
                        onOnboardingFinished()
                    }) {
                        Text(
                            text = stringResource(Res.string.onboarding_skip),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp)) // Maintain height to prevent jumping
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Swipeable Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { position ->
                val page = pages[position]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Hero Icon
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = stringResource(page.titleRes),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(page.descriptionRes),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Section (Indicators + CTA)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicators (Dots)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .height(8.dp)
                                .width(width)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Action Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            viewModel.completeOnboarding()
                            onOnboardingFinished()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    val buttonText = if (pagerState.currentPage < pages.size - 1) {
                        stringResource(Res.string.onboarding_next)
                    } else {
                        stringResource(Res.string.onboarding_get_started)
                    }
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
