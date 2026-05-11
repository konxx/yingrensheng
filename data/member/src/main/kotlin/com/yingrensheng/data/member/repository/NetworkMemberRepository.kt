package com.yingrensheng.data.member.repository

import com.google.gson.reflect.TypeToken
import com.yingrensheng.core.model.member.MemberInfo
import com.yingrensheng.core.model.member.MemberPlan
import com.yingrensheng.core.network.NetworkApiResponse
import com.yingrensheng.core.network.SimpleApiClient
import com.yingrensheng.core.network.YrsApiConfig
import com.yingrensheng.core.network.requireData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class NetworkMemberRepository(
    private val apiClient: SimpleApiClient,
    private val fallback: MemberRepository,
) : MemberRepository {
    override fun getMemberInfo(): MemberInfo {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                val type = object : TypeToken<NetworkApiResponse<MemberMePayload>>() {}.type
                val envelope: NetworkApiResponse<MemberMePayload> = apiClient.get("/member/me", type)
                val payload = envelope.requireData()
                MemberInfo(
                    levelName = payload.levelName,
                    subtitle = payload.subtitle,
                    benefits = emptyList(),
                    highlightLabel = "剩余导出 ${payload.remainingExportCount} 次",
                    activePlanPriceLabel = payload.activePlanPriceLabel,
                )
            }.getOrElse { fallback.getMemberInfo() }
        }
    }

    override fun getPlans(): List<MemberPlan> {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                val type = object : TypeToken<NetworkApiResponse<List<MemberPlanPayload>>>() {}.type
                val envelope: NetworkApiResponse<List<MemberPlanPayload>> = apiClient.get("/member/plans", type)
                envelope.requireData().map {
                    MemberPlan(
                        planId = it.planId,
                        name = it.name,
                        monthlyPrice = it.monthlyPrice,
                        badge = it.badge,
                        summary = it.summary,
                        features = it.features,
                    )
                }
            }.getOrElse { fallback.getPlans() }
        }
    }

    companion object {
        fun fallbackAware(): MemberRepository {
            val fake = FakeMemberRepository()
            return NetworkMemberRepository(
                apiClient = SimpleApiClient(YrsApiConfig.DefaultBaseUrl),
                fallback = fake,
            )
        }
    }
}

private data class MemberPlanPayload(
    val planId: String,
    val name: String,
    val monthlyPrice: Int,
    val badge: String?,
    val summary: String,
    val features: List<String>,
)

private data class MemberMePayload(
    val levelName: String,
    val subtitle: String,
    val remainingExportCount: Int,
    val activePlanPriceLabel: String,
)
