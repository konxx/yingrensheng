package com.yingrensheng.feature.order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.designsystem.theme.WeUiAdminPurple
import com.yingrensheng.core.designsystem.theme.WeUiLiquidGold
import com.yingrensheng.core.model.creation.CreationOutputKind
import com.yingrensheng.core.model.creation.outputKind
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider
import kotlinx.coroutines.launch

@Composable
fun PaymentRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    val scope = rememberCoroutineScope()
    var exporting by remember { mutableStateOf(false) }
    val selectedPlan = session.selectedExportPlan
    val isAdminPlan = selectedPlan?.planId == "plan_admin"
    val outputKind = session.outputKind()

    YrsScaffold(
        title = if (isAdminPlan) "确认使用 Admin 权益" else "确认支付与权益使用",
        subtitle = if (isAdminPlan) {
            outputKind.adminSubtitle()
        } else {
            outputKind.paymentSubtitle()
        },
    ) {
        selectedPlan?.let { plan ->
            if (isAdminPlan) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WeUiAdminPurple, RoundedCornerShape(18.dp))
                        .border(1.dp, WeUiLiquidGold, RoundedCornerShape(18.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = WeUiLiquidGold,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(text = plan.priceLabel, color = WeUiLiquidGold)
                    plan.benefits.forEach { benefit ->
                        Text(text = "• $benefit", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.88f))
                    }
                }
            } else {
                YrsSurfaceCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = plan.title, style = MaterialTheme.typography.titleLarge)
                        Text(text = "价格 ${plan.priceLabel}")
                        plan.benefits.forEach { benefit ->
                            Text(text = "• $benefit")
                        }
                    }
                }
            }
        }
        YrsPrimaryButton(
            text = when {
                exporting -> outputKind.exportingText()
                isAdminPlan -> "确认使用 Admin 权益"
                else -> outputKind.confirmText()
            },
            onClick = {
                if (!exporting) {
                    scope.launch {
                        exporting = true
                        try {
                            creationRepository.startExport()
                            onContinue()
                        } finally {
                            exporting = false
                        }
                    }
                }
            },
        )
    }
}

private fun CreationOutputKind.adminSubtitle(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "系统管理员将直接整理小说创作包，订单记录为已豁免。"
        CreationOutputKind.COMIC_STORYBOARD -> "系统管理员将直接整理漫画分镜包，订单记录为已豁免。"
        CreationOutputKind.CHARACTER_STORY -> "系统管理员将直接整理角色故事包，订单记录为已豁免。"
        CreationOutputKind.SHORT_VIDEO -> "系统管理员导出会直接创建视频导出任务，订单记录为已豁免，不消耗会员次数。"
    }
}

private fun CreationOutputKind.paymentSubtitle(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "当前是开发版假下单，会整理小说创作包并回写到创作状态流。"
        CreationOutputKind.COMIC_STORYBOARD -> "当前会导出漫画分镜包并回写作品库，不会创建视频预览。"
        CreationOutputKind.CHARACTER_STORY -> "当前是开发版假下单，会整理角色故事包并回写到创作状态流。"
        CreationOutputKind.SHORT_VIDEO -> "当前是开发版假下单，但会真实创建视频导出任务并回写到创作状态流。"
    }
}

private fun CreationOutputKind.exportingText(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "正在整理小说创作包"
        CreationOutputKind.COMIC_STORYBOARD -> "正在整理漫画分镜包"
        CreationOutputKind.CHARACTER_STORY -> "正在整理角色故事包"
        CreationOutputKind.SHORT_VIDEO -> "正在创建导出任务"
    }
}

private fun CreationOutputKind.confirmText(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "确认导出小说包"
        CreationOutputKind.COMIC_STORYBOARD -> "确认导出漫画分镜"
        CreationOutputKind.CHARACTER_STORY -> "确认导出角色故事"
        CreationOutputKind.SHORT_VIDEO -> "确认导出短视频"
    }
}
