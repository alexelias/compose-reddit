package com.example.reddit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.reddit.Ambients

@Composable
fun <T> navArg(name: String, navController: NavHostController? = null): T? {
    val args = navController ?: Ambients.NavController.current
    return navArg(name, args.getCurrentBackStackEntry())
}

fun <T> navArg(name: String, backStackEntry: NavBackStackEntry?): T? {
    val arg = backStackEntry?.getArguments()?.get(name)
    @Suppress("UNCHECKED_CAST")
    return arg as? T
}

val defaultSubreddit = "android"

@Composable
fun currentSubreddit(): String {
    return navArg("subreddit") ?: defaultSubreddit
}
