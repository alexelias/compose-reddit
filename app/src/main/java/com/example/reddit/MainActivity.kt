package com.example.reddit

import android.app.Activity
import android.os.Bundle
import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.ambient
import androidx.compose.unaryPlus
import androidx.ui.core.Alignment
import androidx.ui.core.Text
import androidx.ui.core.setContent
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.Colors
import androidx.ui.material.MaterialColors
import androidx.ui.material.MaterialTheme
import androidx.ui.material.TopAppBar
import androidx.ui.material.surface.Surface
import com.example.reddit.api.RedditApi
import com.example.reddit.data.RedditRepository
import com.example.reddit.data.RedditRepositoryImpl
import com.example.reddit.screens.PostScreen
import com.example.reddit.screens.SubredditLinkList
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val networkExecutor = Executors.newFixedThreadPool(5)
    private val api by lazy {
        RedditApi.create()
    }
    private val repository: RedditRepository by lazy {
        RedditRepositoryImpl(api, networkExecutor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Ambients.Repository.Provider(repository) {
                    Ambients.Api.Provider(api) {
                        App()
                    }
                }
            }
        }
    }
}

@Model
object Navigator {
    var route: String = "/r/androiddev"
//    var route = "/comments/drnvgm"
}

@Composable
fun App() {
    Scaffold(subreddit = "androiddev") {
        val route = Navigator.route
        when {
            route.startsWith("/r/") ->
                SubredditLinkList(subreddit = route.substring(3))
            route.startsWith("/comments/") ->
                PostScreen(linkId = route.substring(10), pageSize = 10)
            else ->
                Text("Route '${route}' not found")
        }
    }
}

@Composable
fun AppTheme(children: @Composable () -> Unit) {
    val colors = MaterialColors(
        surface = Color(0xFFE9E9E9.toInt())
    )
    MaterialTheme(colors, children = children)
}


@Composable
fun Scaffold(subreddit: String, children: @Composable () -> Unit) {
    Column {
        TopAppBar(title = { Text("/r/$subreddit") })
        Container(Flexible(1f)) {
            Surface {
                Container(Expanded, children = children)
            }
        }
    }
}
