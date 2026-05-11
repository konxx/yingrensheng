package com.yingrensheng.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.scaffold.YrsScaffold

@Composable
fun LoginRoute(
    onLogin: (username: String, password: String) -> Unit,
    onRegister: (username: String, nickname: String, email: String, password: String) -> Unit,
    loading: Boolean,
    errorMessage: String?,
    initialUsername: String,
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var username by remember(initialUsername) { mutableStateOf(initialUsername) }
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    YrsScaffold(
        title = if (isRegisterMode) "注册后开始创作" else "登录后继续创作",
        subtitle = if (isRegisterMode) {
            "当前注册要求提供账号、邮箱、昵称和密码，先把账号体系打稳。"
        } else {
            "当前已接后端登录接口，先用账号 + 密码完成本地联调。"
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
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("账号") },
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
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("邮箱") },
                        singleLine = true,
                    )
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = AsteriskPasswordVisualTransformation,
                )
                YrsPrimaryButton(
                    text = if (isRegisterMode) "注册并进入映人生" else "登录进入映人生",
                    enabled = !loading,
                    onClick = {
                        if (isRegisterMode) {
                            onRegister(username, nickname, email, password)
                        } else {
                            onLogin(username, password)
                        }
                    },
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    )
                }
                if (loading) {
                    TextButton(
                        onClick = {},
                        enabled = false,
                    ) {
                        Text("正在请求后端...")
                    }
                }
            }
        }
    }
}

private object AsteriskPasswordVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = AnnotatedString("*".repeat(text.text.length)),
            offsetMapping = OffsetMapping.Identity,
        )
    }
}
