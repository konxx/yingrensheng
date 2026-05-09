package com.yingrensheng.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.theme.WeUiGreen
import com.yingrensheng.core.designsystem.theme.WeUiGreenPressed

@Composable
fun YrsPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = WeUiGreen,
            contentColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = WeUiGreenPressed.copy(alpha = 0.45f),
        ),
    ) {
        Text(text = text)
    }
}
