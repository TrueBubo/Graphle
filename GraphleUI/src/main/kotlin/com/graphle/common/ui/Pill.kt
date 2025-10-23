package com.graphle.common.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Pill(
    texts: List<String>,
    onClick: (() -> Unit) = { },
    onRightClick: (() -> Unit) = { },
    background: Color = Color(0xFFE0E0E0),
    contentColor: Color = Color.Black,
    horizontalPadding: Int = 12,
    verticalPadding: Int = 6,
    cornerRadiusDp: Int = 999
) {
    val shape = RoundedCornerShape(cornerRadiusDp.dp)
    Row(
        modifier = Modifier
            .wrapContentHeight(Alignment.CenterVertically)
            .clip(shape)
            .background(background)
            .onClick(onClick = onClick)
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Secondary), onClick = onRightClick)
            .padding(horizontal = horizontalPadding.dp, vertical = verticalPadding.dp)
            .sizeIn(minHeight = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        texts.indices.forEach { index ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .height(20.dp)
                        .width(2.dp)
                        .background(Color.Gray.copy(alpha = 0.5f))
                )
            }
            Text(
                text = texts[index],
                color = contentColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}