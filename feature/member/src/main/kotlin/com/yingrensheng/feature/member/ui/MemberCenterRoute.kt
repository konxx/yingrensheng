package com.yingrensheng.feature.member.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.member.repository.MemberRepositoryProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemberCenterRoute() {
    val memberInfo = MemberRepositoryProvider.current.getMemberInfo()

    YrsScaffold(
        title = "会员中心",
        subtitle = "会员价值不仅是更便宜，也要明显更好用。",
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = memberInfo.levelName, style = MaterialTheme.typography.titleLarge)
                Text(text = memberInfo.subtitle)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    memberInfo.benefits.forEach { benefit ->
                        InfoPill(text = benefit)
                    }
                }
                Text(text = memberInfo.activePlanPriceLabel)
            }
        }
    }
}
