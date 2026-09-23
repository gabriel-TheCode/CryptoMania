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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thecode.cryptomania.R
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme

/**
 * Flat card: separated from the background by tone and a hairline, not by shadows,
 * which keeps dense financial screens calm.
 */
@Composable
fun CryptoManiaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CryptoManiaTheme.colors
    val border = BorderStroke(1.dp, colors.divider)
    val body: @Composable () -> Unit = { Column(Modifier.padding(CryptoManiaTheme.spacing.lg), content = content) }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            color = colors.surfaceRaised,
            border = border,
            content = body,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            color = colors.surfaceRaised,
            border = border,
            content = body,
        )
    }
}

@Composable
fun CryptoManiaTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val spacing = CryptoManiaTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .heightIn(min = 64.dp)
            .padding(start = if (onBack == null) spacing.lg else spacing.xs, end = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back))
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = if (onBack == null) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                color = CryptoManiaTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = CryptoManiaTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
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
            style = MaterialTheme.typography.titleMedium,
            color = CryptoManiaTheme.colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        if (trailing != null) Box { trailing() }
    }
}
