package com.yingrensheng.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.yingrensheng.app.ui.YingRenShengApp
import com.yingrensheng.core.designsystem.theme.YingRenShengTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            YingRenShengTheme {
                YingRenShengApp()
            }
        }
    }
}
