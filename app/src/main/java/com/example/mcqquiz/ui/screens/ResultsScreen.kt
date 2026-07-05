package com.example.mcqquiz.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mcqquiz.model.QuizState
import com.example.mcqquiz.ui.theme.*
import kotlinx.coroutines.isActive

class ParticleState(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var alpha: Float = 1f
)

@Composable
fun ResultsScreen(
    state: QuizState,
    onRestart: () -> Unit,
    onClose: () -> Unit
) {
    val particles = remember { mutableStateListOf<ParticleState>() }

    // Spawn confetti particles on entrance
    LaunchedEffect(key1 = true) {
        val colors = listOf(AccentBlue, CorrectGreen, StreakOrange, StreakGold, Color(0xFFA855F7))
        for (i in 0..100) {
            particles.add(
                ParticleState(
                    x = 0f, // initialized in draw stage or relative to screen size
                    y = 0f,
                    vx = (-15..15).random().toFloat(),
                    vy = (-30..-5).random().toFloat(),
                    color = colors.random(),
                    size = (10..24).random().toFloat()
                )
            )
        }

        // Confetti physics update loop
        var lastTime = System.nanoTime()
        while (isActive) {
            withFrameNanos { frameTime ->
                val elapsedSeconds = (frameTime - lastTime) / 1_000_000_000f
                lastTime = frameTime
                
                particles.forEach { p ->
                    p.x += p.vx
                    p.y += p.vy
                    p.vy += 25f * elapsedSeconds // gravity
                    p.alpha = (p.alpha - 0.35f * elapsedSeconds).coerceAtLeast(0f)
                }
            }
        }
    }

    // Badge scale animation
    var triggerEntranceAnim by remember { mutableStateOf(false) }
    val badgeScale by animateFloatAsState(
        targetValue = if (triggerEntranceAnim) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "badgeScale"
    )

    LaunchedEffect(key1 = true) {
        triggerEntranceAnim = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        // Draw Confetti Particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            particles.forEach { p ->
                // Initialize particle positions at the center bottom of header on first draw
                if (p.x == 0f && p.y == 0f) {
                    p.x = width / 2
                    p.y = height / 3
                }

                if (p.alpha > 0f) {
                    drawCircle(
                        color = p.color,
                        radius = p.size / 2,
                        center = androidx.compose.ui.geometry.Offset(p.x, p.y),
                        alpha = p.alpha
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Exit Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Results",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Celebration Badge (Flame/Glow)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(badgeScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(StreakOrange.copy(alpha = 0.35f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(100)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏆",
                    fontSize = 54.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Congratulations!",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You've completed the quiz. Here's your performance summary:",
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Score Dashboard Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Correct/Total Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Correct\nAnswers",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${state.correctAnswersCount}/${state.totalQuestionsCount}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CorrectGreen
                        )
                    }
                }

                // Highest Streak Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Highest\nStreak",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${state.highestStreak}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = StreakGold,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            if (state.highestStreak >= 3) {
                                Text(text = "🔥", fontSize = 22.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details breakdown card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Skipped Questions",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Skipped and navigated past during quiz",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Text(
                        text = "${state.skippedQuestionsCount}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentBlue
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Restart Button
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(100),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Restart Quiz",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
