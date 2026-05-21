package com.yingrensheng.core.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.theme.LocalYrsRadius

@Composable
fun InfoPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = LocalYrsRadius.current.pill,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp)),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
