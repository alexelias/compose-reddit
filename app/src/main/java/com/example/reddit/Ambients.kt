package com.example.reddit

import androidx.compose.Ambient
import androidx.navigation.NavController
import com.example.reddit.api.RedditApi
import com.example.reddit.data.AuthenticationService
import com.example.reddit.data.RedditRepository
import com.example.reddit.navigation.ComposableNavigator

object Ambients {
    val Repository = Ambient.of<RedditRepository>()
    val Api = Ambient.of<RedditApi>()
    val AuthenticationService = Ambient.of<AuthenticationService>()
    val Navigator = Ambient.of<ComposableNavigator>()
    val NavController = Ambient.of<NavController>()
}