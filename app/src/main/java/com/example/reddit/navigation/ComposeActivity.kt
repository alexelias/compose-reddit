package com.example.reddit.navigation

import androidx.activity.ComponentActivity
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.platform.setContent
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.createGraph
import androidx.navigation.plusAssign
import com.example.reddit.Ambients

abstract class ComposeActivity : ComponentActivity() {
    @Composable
    abstract fun content(content: @Composable () -> Unit)
    abstract fun NavGraphBuilder.graph()
    abstract val initialRoute: Int

    private val graphId = 100
    private var navController: NavController? = null
    private val navigator = ComposableNavigator()

    override fun onBackPressed() {
        if (navController?.popBackStack() == true) {
            Log.v("Navigation", "Successfully navigated back")
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val controller = NavController(this).also {
            it.navigatorProvider += navigator
            it.graph = it.createGraph(graphId, initialRoute) {
                graph()
            }
        }

        navController = controller

        setContent {
            Providers(Ambients.NavController provides controller, Ambients.NavArguments provides navigator.args) {
                content(navigator.current)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        navController = null
    }
}
