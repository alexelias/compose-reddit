package com.example.reddit.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.onCommit
import androidx.compose.ui.platform.setContent
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.reddit.Ambients

abstract class ComposeActivity : ComponentActivity() {
    @Composable
    abstract fun content()

    private var navController: NavController? = null

    override fun onBackPressed() {
        if (navController?.popBackStack() != true) {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val controller = rememberNavController()
            onCommit(controller) {
                navController = controller
                onDispose {
                    navController = null
                }
            }
            Providers(Ambients.NavController provides controller) {
                content()
            }
        }
    }
}
