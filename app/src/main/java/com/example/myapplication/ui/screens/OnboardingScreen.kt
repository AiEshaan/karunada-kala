package com.example.myapplication.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.AppBackgroundContainer
import com.example.myapplication.ui.components.bouncyClickable
import com.example.myapplication.ui.theme.KarnatakaRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Karunada Kala",
            description = "A Digital Directory of Pride. Explore the rich history and diverse art forms that define the soul of Karnataka.",
            icon = "🎨"
        ),
        OnboardingPage(
            title = "Authentic Artisans",
            description = "Locate authentic makers and legendary masters on our interactive cultural map.",
            icon = "📍"
        ),
        OnboardingPage(
            title = "Guru-Shishya Workshops",
            description = "Enroll in workshops to learn ancient crafts and attend upcoming local performances.",
            icon = "🎭"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    AppBackgroundContainer(textureAlpha = 0.04f) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // National Pride Badge
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    "National Pride",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    letterSpacing = 1.sp
                )
            }

            HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = pages[page].icon,
                    fontSize = 100.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Text(
                    text = pages[page].title,
                    color = KarnatakaRed,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 44.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = pages[page].description,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp
                )
            }
        }

        // Indicators & Button Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 64.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                repeat(pages.size) { index ->
                    val color = if (pagerState.currentPage == index) KarnatakaRed else KarnatakaRed.copy(alpha = 0.1f)
                    val width = if (pagerState.currentPage == index) 32.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(4.dp)
                            .width(width)
                            .background(color, RoundedCornerShape(2.dp))
                    )
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .height(64.dp)
                    .bouncyClickable(onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinish()
                        }
                    }),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KarnatakaRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) "ENTER THE SANGHA" else "NEXT MOMENT",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                )
            }
        }
        }
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: String
)
