package com.yingrensheng.data.member.repository

import com.yingrensheng.core.model.member.MemberInfo
import com.yingrensheng.core.model.member.MemberPlan

interface MemberRepository {
    fun getMemberInfo(): MemberInfo

    fun getPlans(): List<MemberPlan>
}

object MemberRepositoryProvider {
    @Volatile
    var current: MemberRepository = NetworkMemberRepository.fallbackAware()
}

class FakeMemberRepository : MemberRepository {
    override fun getMemberInfo(): MemberInfo {
        return MemberInfo(
            levelName = "Lite",
            subtitle = "当前开发版默认账号从 Lite 会员开始，可继续升级到 Pro / Max。",
            benefits = listOf("1080P 导出", "故事片时长延长", "高级配音"),
            highlightLabel = "剩余导出 3 次",
            activePlanPriceLabel = "连续包月 ¥49",
        )
    }

    override fun getPlans(): List<MemberPlan> {
        return listOf(
            MemberPlan("lite", "Lite", 49, null, "适合轻量使用与日常记录整理", listOf("1x 基础生成额度", "适合小型项目与轻量创作", "基础故事草稿与预览能力", "1080P 标准导出")),
            MemberPlan("pro", "Pro", 149, "最受欢迎", "适合频繁创作与更完整叙事", listOf("5x Lite 用量额度", "优先体验新功能与模型", "更多精修能力与高级配乐", "更快生成速度")),
            MemberPlan("max", "Max", 469, "量大管饱", "适合高频深度创作与团队场景", listOf("20x Lite 用量额度", "高阶生成与批量任务能力", "专属资源优先保障", "更多并发与更长时长支持")),
        )
    }
}
