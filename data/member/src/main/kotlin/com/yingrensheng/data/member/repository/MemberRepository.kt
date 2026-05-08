package com.yingrensheng.data.member.repository

import com.yingrensheng.core.model.member.MemberInfo

interface MemberRepository {
    fun getMemberInfo(): MemberInfo
}

object MemberRepositoryProvider {
    @Volatile
    var current: MemberRepository = FakeMemberRepository()
}

class FakeMemberRepository : MemberRepository {
    override fun getMemberInfo(): MemberInfo {
        return MemberInfo(
            levelName = "映人生会员",
            subtitle = "解锁高清导出、胶片情绪模板与优先渲染",
            benefits = listOf("1080P 导出", "故事片时长延长", "高级配音"),
            highlightLabel = "本月 3 次导出剩余",
            activePlanPriceLabel = "年卡 ¥168",
        )
    }
}
