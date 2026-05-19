package com.yingrensheng.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.yingrensheng.core.designsystem.theme.WeUiAdminPurple
import com.yingrensheng.core.designsystem.theme.WeUiAvatar
import com.yingrensheng.core.designsystem.theme.WeUiBackground
import com.yingrensheng.core.designsystem.theme.WeUiBorder
import com.yingrensheng.core.designsystem.theme.WeUiGold
import com.yingrensheng.core.designsystem.theme.WeUiGoldBright
import com.yingrensheng.core.designsystem.theme.WeUiLiteRed
import com.yingrensheng.core.designsystem.theme.WeUiLiquidGold
import com.yingrensheng.core.designsystem.theme.WeUiQr
import com.yingrensheng.core.designsystem.theme.WeUiSurface
import com.yingrensheng.core.designsystem.theme.WeUiTextSecondary
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = WeUiBackground,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "我",
                style = MaterialTheme.typography.headlineMedium,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(WeUiSurface)
                    .border(1.dp, WeUiBorder, RoundedCornerShape(18.dp))
                    .clickable(onClick = onOpenProfileDetail)
                    .padding(horizontal = 16.dp, vertical = 18.dp),
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
                        color = WeUiTextSecondary,
                    )
                    Text(
                        text = user?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WeUiTextSecondary,
                    )
                }
                Column(
                    modifier = Modifier.clickable(onClick = onOpenQr),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MiniQrBadge()
                    Text(
                        text = ">",
                        style = MaterialTheme.typography.headlineMedium,
                        color = WeUiTextSecondary,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(WeUiSurface)
                    .border(1.dp, WeUiBorder, RoundedCornerShape(18.dp)),
            ) {
                ProfileCell(
                    title = "我的订单",
                    subtitle = "查看购买记录与导出状态",
                    onClick = onOpenOrders,
                )
                HorizontalDivider(color = WeUiBorder)
                ProfileCell(
                    title = "会员权益",
                    subtitle = "查看 Lite / Pro / Max 套餐与订阅价格",
                    onClick = onOpenMember,
                )
                HorizontalDivider(color = WeUiBorder)
                ProfileCell(
                    title = "设置",
                    subtitle = "通知、上传与关于信息",
                    onClick = onOpenSettings,
                )
            }
        }
    }
}

@Composable
private fun MemberBadge(levelName: String) {
    val normalizedLevel = when {
        levelName.contains("admin", ignoreCase = true) -> "Admin"
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
        "Admin" -> WeUiLiquidGold
        "Max" -> WeUiGoldBright
        "Pro" -> WeUiGold
        else -> WeUiLiteRed
    }
    val badgeAlpha = if (normalizedLevel == "Max" || normalizedLevel == "Admin") pulseAlpha else 1f
    val backgroundColor = if (normalizedLevel == "Admin") {
        WeUiAdminPurple.copy(alpha = 0.94f)
    } else {
        accentColor.copy(alpha = 0.14f * badgeAlpha)
    }
    val borderColor = if (normalizedLevel == "Admin") {
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
private fun ProfileCell(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(PaddingValues(horizontal = 16.dp, vertical = 16.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = WeUiTextSecondary,
            )
        }
        Text(
            text = ">",
            style = MaterialTheme.typography.titleLarge,
            color = WeUiTextSecondary,
        )
    }
}

@Composable
private fun MiniQrBadge() {
    Box(
        modifier = Modifier
            .size(34.dp)
            .border(1.dp, WeUiBorder, RoundedCornerShape(8.dp))
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
