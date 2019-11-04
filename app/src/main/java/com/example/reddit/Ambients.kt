package com.example.reddit

import androidx.compose.Ambient
import com.example.reddit.api.RedditApi
import com.example.reddit.data.AuthenticationService
import com.example.reddit.data.RedditRepository

object Ambients {
    val Repository = Ambient.of<RedditRepository>()
    val Api = Ambient.of<RedditApi>()
    val AuthenticationService = Ambient.of<AuthenticationService>()
}