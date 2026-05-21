package com.yingrensheng.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsListRow
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.designsystem.theme.WeUiAdminPurple
import com.yingrensheng.core.designsystem.theme.WeUiAvatar
import com.yingrensheng.core.designsystem.theme.WeUiGold
import com.yingrensheng.core.designsystem.theme.WeUiGoldBright
import com.yingrensheng.core.designsystem.theme.WeUiLiteRed
import com.yingrensheng.core.designsystem.theme.WeUiLiquidGold
import com.yingrensheng.core.designsystem.theme.WeUiQr
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.member.repository.MemberRepositoryProvider
import com.yingrensheng.data.user.repository.UserRepositoryProvider

@Composable
fun ProfileRoute(
    onOpenProfileDetail: () -> Unit,
    onOpenQr: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenMember: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val session by UserRepositoryProvider.current.session().collectAsState()
    val memberInfo = MemberRepositoryProvider.current.getMemberInfo()
    val user = session.user
    val nickname = user?.nickname ?: "未登录用户"
    val yingId = "映ID: ${user?.username ?: "guest"}"

    YrsScaffold(
        title = "我",
        subtitle = "账号、会员权益、订单和创作偏好都放在这里。",
    ) {
        YrsSurfaceCard(
            modifier = Modifier.clickable(onClick = onOpenProfileDetail),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(WeUiAvatar),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = nickname.take(2),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = nickname,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        MemberBadge(levelName = memberInfo.levelName)
                    }
                    Text(
                        text = yingId,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = user?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(
                    modifier = Modifier.clickable(onClick = onOpenQr),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MiniQrBadge()
                    Text(
                        text = "›",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        YrsSurfaceCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            YrsListRow(
                title = "我的订单",
                subtitle = "查看购买记录与导出状态",
                trailing = "›",
                showDivider = true,
                onClick = onOpenOrders,
            )
            YrsListRow(
                title = "会员权益",
                subtitle = "查看 Lite / Pro / Max 套餐与订阅价格",
                trailing = "›",
                showDivider = true,
                onClick = onOpenMember,
            )
            YrsListRow(
                title = "设置",
                subtitle = "通知、上传与关于信息",
                trailing = "›",
                onClick = onOpenSettings,
            )
        }
    }
}

@Composable
private fun MemberBadge(levelName: String) {
    val normalizedLevel = when {
        levelName.contains("admin", ignoreCase = true) || levelName.contains("尊享") -> "尊享"
        levelName.contains("max", ignoreCase = true) -> "Max"
        levelName.contains("pro", ignoreCase = true) -> "Pro"
        else -> "Lite"
    }
    val transition = rememberInfiniteTransition(label = "memberBadge")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.68f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "memberBadgeAlpha",
    )
    val accentColor = when (normalizedLevel) {
        "尊享" -> WeUiLiquidGold
        "Max" -> WeUiGoldBright
        "Pro" -> WeUiGold
        else -> WeUiLiteRed
    }
    val badgeAlpha = if (normalizedLevel == "Max" || normalizedLevel == "尊享") pulseAlpha else 1f
    val backgroundColor = if (normalizedLevel == "尊享") {
        WeUiAdminPurple.copy(alpha = 0.94f)
    } else {
        accentColor.copy(alpha = 0.14f * badgeAlpha)
    }
    val borderColor = if (normalizedLevel == "尊享") {
        WeUiLiquidGold.copy(alpha = badgeAlpha)
    } else {
        accentColor.copy(alpha = badgeAlpha)
    }

    Text(
        text = normalizedLevel,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = accentColor,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun MiniQrBadge() {
    Box(
        modifier = Modifier
            .size(34.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(4.dp),
    ) {
        val square = Modifier
            .size(7.dp)
            .background(WeUiQr, RoundedCornerShape(2.dp))

        Box(modifier = square.align(Alignment.TopStart))
        Box(modifier = square.align(Alignment.TopEnd))
        Box(modifier = square.align(Alignment.BottomStart))
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(WeUiQr, RoundedCornerShape(2.dp))
                .align(Alignment.Center),
        )
    }
}
