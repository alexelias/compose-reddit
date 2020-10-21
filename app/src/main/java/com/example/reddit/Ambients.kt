package com.example.reddit

import androidx.compose.runtime.ambientOf
import androidx.navigation.NavHostController
import com.example.reddit.api.RedditApi
import com.example.reddit.data.AuthenticationService
import com.example.reddit.data.RedditRepository

object Ambients {
    val Repository = ambientOf<RedditRepository>()
    val Api = ambientOf<RedditApi>()
    val AuthenticationService = ambientOf<AuthenticationService>()
    val NavController = ambientOf<NavHostController>()
}