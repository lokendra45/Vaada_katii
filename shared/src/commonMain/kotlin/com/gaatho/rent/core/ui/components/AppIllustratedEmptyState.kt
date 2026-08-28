package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun AppIllustratedEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(600)) + 
                slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(600)) + 
                scaleIn(initialScale = 0.95f, animationSpec = tween(600)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        PeekingGhostIllustration(modifier = Modifier.padding(bottom = 16.dp))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        CardTitle(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        BodyText(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        if (buttonText != null && onButtonClick != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onButtonClick,
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun PeekingGhostIllustration(modifier: Modifier = Modifier) {
    var isVisible by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val ghostOffsetY by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isVisible) 0f else 60f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
    )

    androidx.compose.foundation.Canvas(modifier = modifier.size(180.dp).clipToBounds()) {
        val width = size.width
        val height = size.height
        
        // 1. Draw cactuses (in background)
        val cactusColor = androidx.compose.ui.graphics.Color(0xFFCBD5E1)
        
        // Right cactus
        drawRoundRect(
            color = cactusColor,
            topLeft = androidx.compose.ui.geometry.Offset(width * 0.75f, height * 0.35f),
            size = androidx.compose.ui.geometry.Size(width * 0.08f, height * 0.45f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = cactusColor,
            topLeft = androidx.compose.ui.geometry.Offset(width * 0.65f, height * 0.45f),
            size = androidx.compose.ui.geometry.Size(width * 0.18f, height * 0.08f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = cactusColor,
            topLeft = androidx.compose.ui.geometry.Offset(width * 0.65f, height * 0.35f),
            size = androidx.compose.ui.geometry.Size(width * 0.08f, height * 0.18f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )

        // Left cactus
        drawRoundRect(
            color = cactusColor,
            topLeft = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.55f),
            size = androidx.compose.ui.geometry.Size(width * 0.06f, height * 0.25f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = cactusColor,
            topLeft = androidx.compose.ui.geometry.Offset(width * 0.1f, height * 0.65f),
            size = androidx.compose.ui.geometry.Size(width * 0.11f, height * 0.06f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = cactusColor,
            topLeft = androidx.compose.ui.geometry.Offset(width * 0.1f, height * 0.58f),
            size = androidx.compose.ui.geometry.Size(width * 0.06f, height * 0.13f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        
        // 2. Ghost (Animated Y offset)
        val ghostColor = androidx.compose.ui.graphics.Color(0xFFFB923C)
        val ghostWidth = width * 0.45f
        val ghostHeight = height * 0.55f
        val ghostLeft = (width - ghostWidth) / 2
        val ghostTop = height * 0.15f + ghostOffsetY
        
        // Ghost body
        drawRoundRect(
            color = ghostColor,
            topLeft = androidx.compose.ui.geometry.Offset(ghostLeft, ghostTop),
            size = androidx.compose.ui.geometry.Size(ghostWidth, ghostHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(ghostWidth / 2, ghostWidth / 2)
        )
        drawRect(
            color = ghostColor,
            topLeft = androidx.compose.ui.geometry.Offset(ghostLeft, ghostTop + ghostWidth / 2),
            size = androidx.compose.ui.geometry.Size(ghostWidth, ghostHeight - ghostWidth / 2)
        )

        val faceColor = androidx.compose.ui.graphics.Color(0xFF1E293B)
        
        // Left eye
        rotate(degrees = 20f, pivot = androidx.compose.ui.geometry.Offset(ghostLeft + ghostWidth * 0.25f, ghostTop + ghostHeight * 0.3f)) {
            drawOval(
                color = faceColor,
                topLeft = androidx.compose.ui.geometry.Offset(ghostLeft + ghostWidth * 0.2f, ghostTop + ghostHeight * 0.25f),
                size = androidx.compose.ui.geometry.Size(ghostWidth * 0.15f, ghostHeight * 0.12f)
            )
        }
        
        // Right eye
        rotate(degrees = -20f, pivot = androidx.compose.ui.geometry.Offset(ghostLeft + ghostWidth * 0.75f, ghostTop + ghostHeight * 0.3f)) {
            drawOval(
                color = faceColor,
                topLeft = androidx.compose.ui.geometry.Offset(ghostLeft + ghostWidth * 0.65f, ghostTop + ghostHeight * 0.25f),
                size = androidx.compose.ui.geometry.Size(ghostWidth * 0.15f, ghostHeight * 0.12f)
            )
        }

        // Mouth (open oval)
        drawOval(
            color = faceColor,
            topLeft = androidx.compose.ui.geometry.Offset(ghostLeft + ghostWidth * 0.42f, ghostTop + ghostHeight * 0.45f),
            size = androidx.compose.ui.geometry.Size(ghostWidth * 0.16f, ghostHeight * 0.2f)
        )
        
        // Wavy line for hands/squiggly mouth
        val squigglyColor = androidx.compose.ui.graphics.Color(0xFFEA580C)
        drawRoundRect(
            color = squigglyColor,
            topLeft = androidx.compose.ui.geometry.Offset(ghostLeft + ghostWidth * 0.1f, ghostTop + ghostHeight * 0.7f),
            size = androidx.compose.ui.geometry.Size(ghostWidth * 0.8f, ghostHeight * 0.08f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )

        // 3. Snow/hill foreground
        // Use a very light variant or surface color
        drawOval(
            color = androidx.compose.ui.graphics.Color(0xFFF8FAFC),
            topLeft = androidx.compose.ui.geometry.Offset(-width * 0.5f, height * 0.7f),
            size = androidx.compose.ui.geometry.Size(width * 2f, height)
        )
    }
}
