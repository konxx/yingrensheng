package com.yingrensheng.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.designsystem.theme.WeUiQr
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.user.repository.UserRepositoryProvider

@Composable
fun UserQrRoute() {
    val user = UserRepositoryProvider.current.session().value.user
    val seed = (user?.username ?: "guest").hashCode()

    YrsScaffold(
        title = "用户二维码",
        subtitle = "用于快速展示你的映ID。当前版本先提供静态展示，后续可扩展扫码加好友与分享。"
    ) {
        YrsSurfaceCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(22.dp))
                        .padding(18.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(11) { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(11) { column ->
                                    val active = isFinderCorner(row, column) || ((seed shr ((row * 5 + column) % 17)) and 1) == 1
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .background(
                                                color = if (active) WeUiQr else MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(3.dp),
                                            ),
                                    )
                                }
                            }
                        }
                    }
                }
                Text(text = user?.nickname ?: "未登录用户")
                Text(text = "映ID: ${user?.username ?: "guest"}")
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = user?.email ?: "")
            }
        }
    }
}

private fun isFinderCorner(row: Int, column: Int): Boolean {
    val topLeft = row in 0..2 && column in 0..2
    val topRight = row in 0..2 && column in 8..10
    val bottomLeft = row in 8..10 && column in 0..2
    return topLeft || topRight || bottomLeft
}
