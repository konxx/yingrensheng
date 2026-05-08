package com.yingrensheng.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.scaffold.YrsScaffold

@Composable
fun LoginRoute(
    onLogin: (phone: String, password: String) -> Unit,
    onRegister: (phone: String, nickname: String, password: String) -> Unit,
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("13800138000") }
    var nickname by remember { mutableStateOf("新用户") }
    var password by remember { mutableStateOf("123456") }

    YrsScaffold(
        title = if (isRegisterMode) "注册后开始创作" else "登录后继续创作",
        subtitle = if (isRegisterMode) {
            "先用手机号 + 昵称 + 密码完成最小注册闭环，后续再扩成验证码注册。"
        } else {
            "当前已接后端登录接口，先用手机号 + 密码完成本地联调。"
        },
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = !isRegisterMode,
                        onClick = { isRegisterMode = false },
                        label = { Text("登录") },
                    )
                    FilterChip(
                        selected = isRegisterMode,
                        onClick = { isRegisterMode = true },
                        label = { Text("注册") },
                    )
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("手机号") },
                    singleLine = true,
                )
                if (isRegisterMode) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("昵称") },
                        singleLine = true,
                    )
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    singleLine = true,
                )
                YrsPrimaryButton(
                    text = if (isRegisterMode) "注册并进入映人生" else "登录进入映人生",
                    onClick = {
                        if (isRegisterMode) {
                            onRegister(phone, nickname, password)
                        } else {
                            onLogin(phone, password)
                        }
                    },
                )
            }
        }
    }
}
