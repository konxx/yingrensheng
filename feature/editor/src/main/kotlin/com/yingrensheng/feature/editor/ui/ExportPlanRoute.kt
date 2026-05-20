package com.yingrensheng.feature.editor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.model.creation.ExportPlan
import com.yingrensheng.core.model.creation.CreationOutputKind
import com.yingrensheng.core.model.creation.outputKind
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportPlanRoute(
    onContinue: () -> Unit,
    isAdmin: Boolean = false,
) {
    val creationRepository = CreationRepositoryProvider.current
    val outputKind = creationRepository.observeSession().value.outputKind()
    val plans = if (isAdmin) listOf(adminExportPlan(outputKind)) else creationRepository.exportPlans(outputKind)
    YrsScaffold(
        title = if (isAdmin) "Admin 权益导出" else outputKind.exportTitle(),
        subtitle = if (isAdmin) {
            "系统管理员已开放全部导出权益，不消耗次数，也不需要选择单次或会员付费方案。"
        } else {
            outputKind.exportSubtitle()
        },
    ) {
        plans.forEach { plan ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = plan.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = plan.priceLabel)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        plan.benefits.forEach { benefit ->
                            InfoPill(text = benefit)
                        }
                    }
                    YrsPrimaryButton(
                        text = if (isAdmin) "使用 Admin 权益导出" else "选择 ${plan.title}",
                        onClick = {
                            creationRepository.selectExportPlan(plan)
                            onContinue()
                        },
                    )
                }
            }
        }
    }
}

private fun adminExportPlan(outputKind: CreationOutputKind): ExportPlan = ExportPlan(
    planId = "plan_admin",
    title = "系统管理员权益导出",
    priceLabel = "已豁免",
    benefits = when (outputKind) {
        CreationOutputKind.STORY_TEXT -> listOf("小说文本包", "不消耗导出额度", "无需支付")
        CreationOutputKind.COMIC_STORYBOARD -> listOf("漫画分镜包", "不消耗导出额度", "无需支付")
        CreationOutputKind.CHARACTER_STORY -> listOf("角色故事包", "不消耗导出额度", "无需支付")
        CreationOutputKind.SHORT_VIDEO -> listOf("无水印", "1080P 高清导出", "不消耗导出额度", "无需支付")
    },
)

private fun CreationOutputKind.exportTitle(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "导出小说创作包"
        CreationOutputKind.COMIC_STORYBOARD -> "导出漫画分镜包"
        CreationOutputKind.SHORT_VIDEO -> "导出短视频预览"
        CreationOutputKind.CHARACTER_STORY -> "导出角色故事包"
    }
}

private fun CreationOutputKind.exportSubtitle(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "确认本次导出的小说正文、故事主线和结构化创作资料。"
        CreationOutputKind.COMIC_STORYBOARD -> "确认本次导出的漫画分镜、对白、旁白框和画面提示资料；这条流程不会生成视频。"
        CreationOutputKind.SHORT_VIDEO -> "用户要明确知道买到什么视频规格、有没有水印，以及会员多了哪些体验。"
        CreationOutputKind.CHARACTER_STORY -> "确认本次导出的角色设定、故事草稿和分镜方向。"
    }
}
