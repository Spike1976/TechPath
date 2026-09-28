package com.techpath.learn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.techpath.learn.ui.TechPathApp
import com.techpath.learn.ui.TechPathTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TechPathTheme {
                TechPathApp()
            }
        }
    }
}
