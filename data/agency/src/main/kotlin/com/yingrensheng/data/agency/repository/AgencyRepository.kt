package com.yingrensheng.data.agency.repository

import com.yingrensheng.core.model.agency.AgencyOverview

interface AgencyRepository {
    fun getAgencyOverview(): AgencyOverview
}

object AgencyRepositoryProvider {
    @Volatile
    var current: AgencyRepository = FakeAgencyRepository()
}

class FakeAgencyRepository : AgencyRepository {
    override fun getAgencyOverview(): AgencyOverview {
        return AgencyOverview(
            title = "机构批量影像服务",
            summary = "为学校、景区、摄影工作室提供统一模板、批量生成与授权留痕。",
            capabilities = listOf("批量任务", "专属模板", "授权记录"),
        )
    }
}
