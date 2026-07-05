package com.example.mcqquiz.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mcqquiz.model.QuizState
import com.example.mcqquiz.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuizScreen(
    state: QuizState,
    onOptionSelected: (String) -> Unit,
    onSkip: () -> Unit,
    onExit: () -> Unit
) {
    val question = state.currentQuestion ?: return
    var dragAmountSum by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Animation for streak messages
    val isStreakMilestone = state.currentStreak >= 3
    val streakText = when {
        state.currentStreak >= 5 -> "5 questions streak achieved !!"
        state.currentStreak >= 3 -> "3 questions streak achieved !!"
        else -> ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onExit,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Quiz",
                        tint = TextSecondary
                    )
                }

                Text(
                    text = "Quiz",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(
                    onClick = { /* Could show info or placeholder */ },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Quiz Rules",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Streak Flame Row
            StreakFlameRow(
                streakCount = state.currentStreak,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Streak Milestone Text Banner
            AnimatedVisibility(
                visible = isStreakMilestone,
                enter = fadeIn() + expandVertically() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + shrinkVertically()
            ) {
                StreakMilestoneBanner(text = streakText)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question Progress
            Text(
                text = "Question ${state.currentQuestionIndex + 1} of ${state.totalQuestionsCount}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Progress Bar
            QuizProgressBar(
                progress = state.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Question Box with Swipe-to-Skip gesture
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(state.currentQuestionIndex) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                // Threshold for swipe triggers skip (swiping left or right)
                                if (dragAmountSum > 200f || dragAmountSum < -200f) {
                                    if (!state.isAnswered) {
                                        onSkip()
                                    }
                                }
                                dragAmountSum = 0f
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                dragAmountSum += dragAmount
                            }
                        )
                    }
                    .graphicsLayer {
                        // Soft rotate/translate animation based on swipe drag
                        translationX = dragAmountSum * 0.15f
                        rotationZ = dragAmountSum * 0.01f
                    },
                contentAlignment = Alignment.TopStart
            ) {
                Column {
                    // Question text
                    Text(
                        text = question.questionText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 32.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    )

                    // Options list
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        question.options.forEach { option ->
                            OptionCard(
                                optionText = option,
                                isAnswered = state.isAnswered,
                                isSelected = state.selectedOption == option,
                                isCorrect = option == question.correctAnswer,
                                onClick = {
                                    if (!state.isAnswered) {
                                        onOptionSelected(option)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Bottom skip button (only visible/enabled when not answered)
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(
                visible = !state.isAnswered,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Button(
                    onClick = onSkip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurface,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Skip",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StreakFlameRow(
    streakCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // We display 5 flames representing the streak indicators
        for (i in 1..5) {
            val isLit = streakCount >= i
            val pulseScale by animateFloatAsState(
                targetValue = if (isLit) 1.2f else 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "flamePulse"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(44.dp)
                    .scale(if (isLit) pulseScale else 1f)
                    .background(
                        color = if (isLit) StreakOrange.copy(alpha = 0.2f) else DarkSurface,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = if (isLit) StreakGold else DarkBorder,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔥",
                    fontSize = 18.sp,
                    modifier = Modifier.alpha(if (isLit) 1f else 0.25f)
                )
            }
        }
    }
}

@Composable
fun StreakMilestoneBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bannerGlow")
    val glowColor by infiniteTransition.animateColor(
        initialValue = StreakOrange,
        targetValue = StreakGold,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        glowColor.copy(alpha = 0.2f),
                        glowColor.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, glowColor, glowColor, Color.Transparent)
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚡",
                fontSize = 18.sp,
                color = StreakGold,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = text,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "⚡",
                fontSize = 18.sp,
                color = StreakGold,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
fun QuizProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        modifier = modifier
            .background(DarkSurface, shape = RoundedCornerShape(100))
            .border(1.dp, DarkBorder, shape = RoundedCornerShape(100))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(AccentBlue, AccentBlue.copy(alpha = 0.8f))
                    ),
                    shape = RoundedCornerShape(100)
                )
        )
    }
}

@Composable
fun OptionCard(
    optionText: String,
    isAnswered: Boolean,
    isSelected: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val targetBgColor = when {
        isAnswered && isCorrect -> CorrectGreen
        isAnswered && isSelected && !isCorrect -> IncorrectRed
        else -> DarkSurface
    }

    val targetBorderColor = when {
        isAnswered && isCorrect -> CorrectGreen
        isAnswered && isSelected && !isCorrect -> IncorrectRed
        isSelected -> AccentBlue
        else -> DarkBorder
    }

    val targetAlpha = if (isAnswered && !isCorrect && !isSelected) 0.4f else 1f

    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(containerColor = targetBgColor),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, targetBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
            .alpha(targetAlpha)
            .clickable(enabled = !isAnswered) {
                coroutineScope.launch {
                    scale.animateTo(0.96f, animationSpec = tween(100))
                    scale.animateTo(1f, animationSpec = tween(100))
                    onClick()
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = optionText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isAnswered && (isCorrect || (isSelected && !isCorrect))) Color.White else TextPrimary,
                modifier = Modifier.weight(1f)
            )

            // Optional Icon indicating correctness
            if (isAnswered) {
                if (isCorrect) {
                    Text(text = "✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                } else if (isSelected) {
                    Text(text = "✗", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}
