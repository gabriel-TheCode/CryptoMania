package com.thecode.cryptomania.presentation.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thecode.cryptomania.R
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme

/**
 * Raised card with a soft, brand-tinted drop shadow (Compose `dropShadow`, drawn by the
 * render node, so it follows the rounded shape exactly and costs no extra layout).
 */
@Composable
fun CryptoManiaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CryptoManiaTheme.colors
    val shape = MaterialTheme.shapes.medium
    // Light theme: soft tinted shadow, no outline. Dark theme: shadows barely read, so a hairline separates.
    val border = if (colors.isDark) BorderStroke(1.dp, colors.divider) else null
    val elevated = modifier.dropShadow(shape, Shadow(radius = 18.dp, color = colors.shadow, offset = DpOffset(0.dp, 6.dp)))
    val body: @Composable () -> Unit = { Column(Modifier.padding(CryptoManiaTheme.spacing.lg), content = content) }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = elevated,
            shape = shape,
            color = colors.surfaceRaised,
            border = border,
            content = body,
        )
    } else {
        Surface(
            modifier = elevated,
            shape = shape,
            color = colors.surfaceRaised,
            border = border,
            content = body,
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = CryptoManiaTheme.colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        if (trailing != null) Box { trailing() }
    }
}
