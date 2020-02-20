package com.example.reddit

import androidx.compose.ambientOf
import androidx.navigation.NavController
import com.example.reddit.api.RedditApi
import com.example.reddit.data.AuthenticationService
import com.example.reddit.data.RedditRepository
import com.example.reddit.navigation.ComposableNavigator

object Ambients {
    val Repository = ambientOf<RedditRepository>()
    val Api = ambientOf<RedditApi>()
    val AuthenticationService = ambientOf<AuthenticationService>()
    val Navigator = ambientOf<ComposableNavigator>()
    val NavController = ambientOf<NavController>()
}
