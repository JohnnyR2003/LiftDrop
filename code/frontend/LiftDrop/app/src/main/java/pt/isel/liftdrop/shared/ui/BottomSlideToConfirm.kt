package pt.isel.liftdrop.shared.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun BottomSlideToConfirm(
    text: String,
    onConfirmed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        SlideToConfirmButton(
            text = text,
            onConfirmed = onConfirmed,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun SlideToConfirmButton(
    text: String,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var maxWidth by remember { mutableFloatStateOf(0f) }
    val thumbSize = 56.dp
    val density = LocalDensity.current
    val thumbSizePx = with(density) { thumbSize.toPx().takeIf { it > 0f } ?: 1f }
    val scope = rememberCoroutineScope()
    val canDrag = maxWidth > 0f && thumbSizePx > 0f && maxWidth > thumbSizePx
    val progress = if (canDrag) (offsetX / (maxWidth - thumbSizePx)).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "slide-progress")

    Box(
        modifier = modifier
            .height(thumbSize)
            .fillMaxWidth()
            .onGloballyPositioned { maxWidth = it.size.width.toFloat() }
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF384259),
                        Color(0xFF4B4F6B),
                        Color(0xFF23243A)
                    ),
                    startX = 0f,
                    endX = maxWidth
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        if (canDrag) {
                            offsetX = (offsetX + dragAmount).coerceIn(0f, maxWidth - thumbSizePx)
                        }
                    },
                    onDragEnd = {
                        if (canDrag) {
                            val currentProgress = (offsetX / (maxWidth - thumbSizePx)).coerceIn(0f, 1f)
                            if (currentProgress > 0.85f) {
                                scope.launch {
                                    onConfirmed()
                                    offsetX = 0f
                                }
                            } else {
                                offsetX = 0f
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White.copy(alpha = (1f - animatedProgress * 0.7f)),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .size(thumbSize)
                .shadow(8.dp, CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF6C63FF), Color(0xFF384259)),
                        center = Offset(thumbSizePx / 2, thumbSizePx / 2),
                        radius = thumbSizePx
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}