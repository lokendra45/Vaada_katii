package com.gaatho.rent.core.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class LineChartData(
    val points: List<Float>
) {
    init {
        require(points.isNotEmpty()) { "LineChartData must contain at least one point" }
    }
    
    val maxData: Float get() = points.maxOrNull() ?: 1f
    val minData: Float get() = points.minOrNull() ?: 0f
    val range: Float get() = if (maxData - minData == 0f) 1f else maxData - minData
}

@Composable
fun AppLineChart(
    data: LineChartData,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    var isAnimationPlayed by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isAnimationPlayed = true
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (isAnimationPlayed) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "line_chart_animation"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val pointsToDraw = if (data.points.size == 1) {
            listOf(data.points.first(), data.points.first())
        } else {
            data.points
        }
        
        val stepX = size.width / (pointsToDraw.size - 1).coerceAtLeast(1)
        
        val path = Path()
        val offsets = mutableListOf<Offset>()
        
        pointsToDraw.forEachIndexed { index, value ->
            val x = index * stepX
            val y = if (data.maxData == data.minData) {
                size.height / 2 // Draw in the middle if all values are the same
            } else {
                size.height - ((value - data.minData) / data.range) * (size.height - 20.dp.toPx())
            }
            offsets.add(Offset(x, y))
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        // Measure path to animate it
        val pathMeasure = PathMeasure()
        pathMeasure.setPath(path, false)
        
        val animatedPath = Path()
        pathMeasure.getSegment(0f, pathMeasure.length * animatedProgress, animatedPath, true)
        
        // Only draw the gradient fill once it starts drawing, clamping to current progress
        if (animatedProgress > 0f) {
            val fillPath = Path().apply {
                addPath(animatedPath)
                
                // Get the current x position for the fill based on the animated progress length
                val pathMeasure2 = PathMeasure()
                pathMeasure2.setPath(animatedPath, false)
                val pos = pathMeasure2.getPosition(pathMeasure2.length)
                
                // Drop a line down to the bottom from the current drawn position
                lineTo(pos.x, size.height)
                lineTo(0f, size.height)
                close()
            }
            
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                    startY = 0f,
                    endY = size.height
                )
            )
        }
        
        // Draw the animated line
        drawPath(
            path = animatedPath,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw the dot at the end if fully animated
        if (animatedProgress == 1f && offsets.isNotEmpty()) {
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = offsets.last()
            )
        }
    }
}
