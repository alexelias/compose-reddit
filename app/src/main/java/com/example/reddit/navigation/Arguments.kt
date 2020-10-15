package com.example.reddit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import com.example.reddit.Ambients

@Composable
fun <T> navArg(name: String): T {
    return optionalNavArg(name)!!
}

@Composable
fun <T> optionalNavArg(name: String): T? {
    val args = Ambients.NavArguments.current
    val arg = args?.get(name)
    @Suppress("UNCHECKED_CAST")
    return arg as? T
}

@Composable
fun currentSubreddit(): String {
    return optionalNavArg<String>("subreddit") ?: "android"
}