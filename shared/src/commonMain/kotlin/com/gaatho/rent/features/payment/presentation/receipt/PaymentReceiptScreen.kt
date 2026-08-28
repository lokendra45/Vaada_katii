package com.gaatho.rent.features.payment.presentation.receipt

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.ui.components.*
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*
import kotlin.random.Random

private val PaperColor = Color(0xFFFFFDF6)
private val InkColor = Color(0xFF2B2B2B)
private val FadedInk = Color(0xFF2B2B2B).copy(alpha = 0.55f)

@Composable
fun PaymentReceiptScreen(
    amount: String,
    tenantName: String,
    propertyName: String,
    date: String,
    paymentMethod: String,
    transactionId: String,
    onDone: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        bottomBar = { ReceiptActions(onDone = onDone) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ReceiptCard(
                amount = amount,
                tenantName = tenantName,
                propertyName = propertyName,
                date = date,
                paymentMethod = paymentMethod,
                transactionId = transactionId
            )
        }
    }
}

@Composable
private fun ReceiptCard(
    amount: String,
    tenantName: String,
    propertyName: String,
    date: String,
    paymentMethod: String,
    transactionId: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PaperColor,
        shadowElevation = 10.dp,
        shape = TornEdgeShape()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 26.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Success Icon
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = Color(0xFF22C55E).copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            CardTitle(
                text = stringResource(Res.string.receipt_payment_successful),
                color = Color(0xFF22C55E),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            ScreenTitle(
                text = stringResource(Res.string.currency_npr) + " $amount",
                color = InkColor
            )

            Spacer(modifier = Modifier.height(28.dp))
            PerforatedDivider()
            Spacer(modifier = Modifier.height(24.dp))

            ReceiptRow(label = stringResource(Res.string.receipt_property_label), value = propertyName)
            ReceiptRow(label = stringResource(Res.string.receipt_tenant_label), value = tenantName)
            ReceiptRow(label = stringResource(Res.string.receipt_date_label), value = date)
            ReceiptRow(label = stringResource(Res.string.receipt_method_label), value = paymentMethod)

            Spacer(modifier = Modifier.height(20.dp))
            PerforatedDivider()
            Spacer(modifier = Modifier.height(24.dp))

            ReceiptRow(label = stringResource(Res.string.receipt_transaction_id_label), value = transactionId, isMono = true)

            Spacer(modifier = Modifier.height(24.dp))

            BarcodeStrip(seed = transactionId)

            Spacer(modifier = Modifier.height(16.dp))

            CaptionText(
                text = stringResource(Res.string.receipt_thank_you),
                color = FadedInk,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, isMono: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        CaptionText(
            text = label,
            color = FadedInk
        )
        if (isMono) {
            BodySmallText(
                text = value,
                color = InkColor,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f).padding(start = 24.dp)
            )
        } else {
            BodyText(
                text = value,
                color = InkColor,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f).padding(start = 24.dp)
            )
        }
    }
}

/** A tear-line: small round "punch holes" instead of a plain dashed rule. */
@Composable
private fun PerforatedDivider() {
    Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) {
        val holeRadius = 2.dp.toPx()
        val gap = 10.dp.toPx()
        var x = holeRadius
        while (x < size.width) {
            drawCircle(
                color = Color.Black.copy(alpha = 0.18f),
                radius = holeRadius,
                center = Offset(x, size.height / 2)
            )
            x += gap
        }
    }
}

/** Purely decorative barcode-style strip — not a real scannable code. */
@Composable
private fun BarcodeStrip(seed: String, modifier: Modifier = Modifier) {
    val bars = remember(seed) {
        val random = Random(seed.hashCode())
        List(46) { random.nextFloat() * 2.4f + 0.6f }
    }
    Canvas(modifier = modifier.fillMaxWidth().height(38.dp)) {
        val totalWeight = bars.sum()
        val unit = size.width / totalWeight
        var x = 0f
        bars.forEachIndexed { index, weight ->
            val barWidth = weight * unit
            if (index % 2 == 0) {
                drawRect(
                    color = InkColor,
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, size.height)
                )
            }
            x += barWidth
        }
    }
}

/**
 * Receipt paper silhouette: straight sides, zigzag "torn" edges on the
 * top and bottom, like paper ripped off a printer roll.
 */
private class TornEdgeShape(
    private val toothWidth: androidx.compose.ui.unit.Dp = 14.dp,
    private val toothHeight: androidx.compose.ui.unit.Dp = 7.dp
) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val toothW = with(density) { toothWidth.toPx() }
        val toothH = with(density) { toothHeight.toPx() }
        val path = Path().apply {
            moveTo(0f, toothH)
            var x = 0f
            var up = true
            while (x < size.width) {
                val nextX = (x + toothW).coerceAtMost(size.width)
                lineTo(nextX, if (up) 0f else toothH)
                up = !up
                x = nextX
            }
            lineTo(size.width, size.height - toothH)
            x = size.width
            up = true
            while (x > 0f) {
                val nextX = (x - toothW).coerceAtLeast(0f)
                lineTo(nextX, if (up) size.height else size.height - toothH)
                up = !up
                x = nextX
            }
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
private fun ReceiptActions(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
    ) {
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(Res.string.done_action), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
@androidx.compose.ui.tooling.preview.Preview
private fun PaymentReceiptScreenPreview() {
    RentManagerTheme {
        PaymentReceiptScreen(
            amount = "1,24,500",
            tenantName = "Suman Shrestha",
            propertyName = "Downtown Lofts - Unit 4B",
            date = "02-Aug-2026",
            paymentMethod = "Bank Transfer",
            transactionId = "TXN-849204820",
            onDone = {}
        )
    }
}