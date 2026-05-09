package com.yingrensheng.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.common.result.AppResult
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.designsystem.theme.WeUiGreen
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.user.repository.UserRepositoryProvider
import kotlinx.coroutines.launch

@Composable
fun ProfileDetailRoute() {
    val user = UserRepositoryProvider.current.session().value.user
    val scope = rememberCoroutineScope()
    var nickname by remember { mutableStateOf(user?.nickname ?: "") }
    var username by remember { mutableStateOf(user?.username ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var loading by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    val defaultStatusColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
    val errorStatusColor = MaterialTheme.colorScheme.error
    var statusColor by remember { mutableStateOf(defaultStatusColor) }

    LaunchedEffect(user?.userId) {
        nickname = user?.nickname ?: ""
        username = user?.username ?: ""
        email = user?.email ?: ""
    }

    YrsScaffold(
        title = "个人资料",
        subtitle = "可在这里修改昵称、映ID 和邮箱。保存后会直接同步当前账号信息。",
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("昵称") },
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("映ID / 账号") },
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    singleLine = true,
                )
                YrsPrimaryButton(
                    text = if (loading) "正在保存..." else "保存修改",
                    enabled = !loading && user != null,
                    onClick = {
                        val current = user ?: return@YrsPrimaryButton
                        scope.launch {
                            loading = true
                            when (
                                val result = UserRepositoryProvider.current.updateProfile(
                                    userId = current.userId,
                                    username = username,
                                    nickname = nickname,
                                    email = email,
                                )
                            ) {
                                is AppResult.Success -> {
                                    statusText = "个人资料已更新"
                                    statusColor = WeUiGreen
                                }

                                is AppResult.Error -> {
                                    statusText = result.message
                                    statusColor = errorStatusColor
                                }
                            }
                            loading = false
                        }
                    },
                )
                if (statusText != null) {
                    Text(
                        text = statusText!!,
                        color = statusColor,
                    )
                }
            }
        }
    }
}
