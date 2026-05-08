package com.yingrensheng.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.user.repository.UserRepositoryProvider

@Composable
fun ProfileRoute(
    onOpenMember: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenAgency: () -> Unit,
) {
    val session by UserRepositoryProvider.current.session().collectAsState()

    YrsScaffold(
        title = "我的",
        subtitle = "这里承接会员、订单、授权记录与帮助，不让底部导航变得过重。",
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = session.user?.nickname ?: "未登录用户",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(text = session.user?.phone ?: "")
            }
        }
        YrsPrimaryButton(text = "进入会员中心", onClick = onOpenMember)
        YrsPrimaryButton(text = "查看订单记录", onClick = onOpenOrders)
        YrsPrimaryButton(text = "查看机构合作入口", onClick = onOpenAgency)
    }
}
