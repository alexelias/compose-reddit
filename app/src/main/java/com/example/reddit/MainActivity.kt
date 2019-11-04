package com.example.reddit

import android.app.Activity
import android.os.Bundle
import androidx.compose.Composable
import androidx.ui.core.Text
import androidx.ui.core.setContent
import androidx.ui.layout.Column
import androidx.ui.layout.Container
import androidx.ui.layout.Expanded
import androidx.ui.material.MaterialTheme
import androidx.ui.material.TopAppBar
import androidx.ui.material.surface.Surface
import com.example.reddit.api.RedditApi
import com.example.reddit.data.RedditRepository
import com.example.reddit.data.RedditRepositoryImpl
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
            MaterialTheme {
                Ambients.Repository.Provider(repository) {
                    Ambients.Api.Provider(api) {
                        App()
                    }
                }
            }
        }
    }
}

@Composable
fun App() {
    Scaffold(subreddit = "androiddev") {
        SubredditLinkList(subreddit = "androiddev")
    }
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
