package com.example.reddit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.ui.core.Text
import androidx.ui.core.setContent
import androidx.ui.material.MaterialTheme
import androidx.ui.tooling.preview.Preview
import com.example.reddit.api.RedditApi
import com.example.reddit.data.RedditRepository
import com.example.reddit.data.RedditRepositoryImpl
import com.example.reddit.screens.SubredditLinkList
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
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
    SubredditLinkList(subreddit = "androiddev")
}
