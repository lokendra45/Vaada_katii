package com.gaatho.rent.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.gaatho.rent.core.designsystem.monoDataTextStyle

/**
 * Standardized text components.
 *
 * Every screen MUST use these instead of `Text(..., style = MaterialTheme.typography.*)`
 * or raw `fontSize =`. Each component maps to exactly ONE typography role, so the app
 * reads consistently everywhere (Google Pay / Apple Pay style). The correct style is the
 * only easy choice.
 *
 * Contract:
 *   ScreenTitle   -> displayMedium  (top of screen / app-bar title)
 *   SectionTitle  -> headlineSmall  (in-screen section headers)
 *   CardTitle     -> titleMedium    (titles inside cards / list rows)
 *   LabelText     -> labelLarge     (emphasized chip / label text)
 *   BodyText      -> bodyMedium     (primary body copy)
 *   BodySmallText -> bodySmall      (secondary body copy)
 *   CaptionText   -> labelMedium    (metadata, captions)
 *   MicroText     -> labelSmall     (tiny meta)
 *   AmountText    -> monoDataTextStyle (financial figures)
 */

private val UnspecifiedColor = Color.Unspecified

@Composable
private fun BaseText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = UnspecifiedColor,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = onTextLayout,
    )
}

@Composable
private fun BaseText(
    text: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = UnspecifiedColor,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    onTextLayout: ((TextLayoutResult) -> Unit) = {},
) {
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = onTextLayout,
    )
}

@Composable
fun ScreenTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = UnspecifiedColor,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) = BaseText(text, MaterialTheme.typography.displayMedium, modifier, color, fontWeight, textAlign, maxLines, overflow)

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = UnspecifiedColor,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) = BaseText(text, MaterialTheme.typography.headlineSmall, modifier, color, fontWeight, textAlign, maxLines, overflow)

@Composable
fun CardTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = UnspecifiedColor,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) = BaseText(text, MaterialTheme.typography.titleMedium, modifier, color, fontWeight, textAlign, maxLines, overflow)

@Composable
fun CardTitle(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = UnspecifiedColor,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) = BaseText(text, MaterialTheme.typography.titleMedium, modifier, color, fontWeight, textAlign, maxLines, overflow)

@Composable
fun LabelText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = UnspecifiedColor,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) = BaseText(text, MaterialTheme.typography.labelLarge, modifier, color, fontWeight, textAlign, maxLines, overflow)

@Composable
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = UnspecifiedColor,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) = BaseText(text, MaterialTheme.typography.bodyMedium, modifier, color, fontWeight, textAlign, maxLines, overflow)

@Composable
fun BodySmallText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = UnspecifiedColor,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) = BaseText(text, MaterialTheme.typography.bodySmall, modifier, color, fontWeight, textAlign, maxLines, overflow)

@Composable
fun CaptionText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = UnspecifiedColor,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) = BaseText(text, MaterialTheme.typography.labelMedium, modifier, color, fontWeight, textAlign, maxLines, overflow)

@Composable
fun MicroText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = UnspecifiedColor,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) = BaseText(text, MaterialTheme.typography.labelSmall, modifier, color, fontWeight, textAlign, maxLines, overflow)

@Composable
fun AmountText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = UnspecifiedColor,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) = BaseText(text, monoDataTextStyle(), modifier, color, fontWeight, textAlign, maxLines, overflow)
