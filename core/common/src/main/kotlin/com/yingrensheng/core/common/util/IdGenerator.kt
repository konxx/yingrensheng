package com.yingrensheng.core.common.util

import java.util.UUID

object IdGenerator {
    fun newId(prefix: String): String = "${prefix}_${UUID.randomUUID().toString().take(8)}"
}

