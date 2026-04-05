package com.wickedcoder.app.ui.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wickedcoder.app.ui.theme.Background
import com.wickedcoder.app.ui.theme.Indigo500
import com.wickedcoder.app.ui.theme.Indigo700
import com.wickedcoder.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

// ── Page data ─────────────────────────────────────────────────────────────────

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val body: String
)

private val pages = listOf(
    OnboardingPage(
        emoji = "💰",
        title = "Track Every Rupee,\nAutomatically",
        body  = "SpendSense reads your bank transaction SMS to categorize your spending — no manual entry needed."
    ),
    OnboardingPage(
        emoji = "🔒",
        title = "Your Data Stays\non Device",
        body  = "We never upload your SMS or financial data. Everything is processed locally and stored securely in your phone's database."
    ),
    OnboardingPage(
        emoji = "📱",
        title = "One Permission\nNeeded",
        body  = "SpendSense needs permission to read transaction messages from your bank. We never access your personal chats."
    )
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(vm: OnboardingViewModel = hiltViewModel()) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope      = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { vm.markComplete() }   // complete regardless of grant/deny — user can grant later

    Scaffold(containerColor = Background) { innerPadding ->
        Column(
            modifier              = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.SpaceBetween
        ) {
            // ── Pager ─────────────────────────────────────────────────────────
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                PageContent(pages[pageIndex])
            }

            // ── Dots + Buttons ─────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.padding(bottom = 40.dp)
            ) {
                DotsIndicator(
                    total   = pages.size,
                    current = pagerState.currentPage,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                val isLastPage = pagerState.currentPage == pages.size - 1

                Button(
                    onClick = {
                        if (isLastPage) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.READ_SMS
                                )
                            )
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
                ) {
                    Text(
                        text       = if (isLastPage) "Grant SMS Permission" else "Next →",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 16.sp
                    )
                }

                if (!isLastPage) {
                    TextButton(
                        onClick  = { vm.markComplete() },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            "Skip for now",
                            color    = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Single page content ───────────────────────────────────────────────────────

@Composable
private fun PageContent(page: OnboardingPage) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text     = page.emoji,
            fontSize = 80.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Text(
            text       = page.title,
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color      = Indigo700,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text      = page.body,
            style     = MaterialTheme.typography.bodyLarge,
            color     = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

// ── Dots indicator ────────────────────────────────────────────────────────────

@Composable
private fun DotsIndicator(
    total:    Int,
    current:  Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val isSelected = index == current
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Indigo500 else Color(0xFFBDBDBD))
                    .size(if (isSelected) 10.dp else 8.dp)
            )
        }
    }
}
