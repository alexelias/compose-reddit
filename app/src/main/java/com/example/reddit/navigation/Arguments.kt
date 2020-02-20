package com.example.reddit.navigation

import androidx.compose.ambient
import androidx.compose.Composable
import com.example.reddit.Ambients

@Composable
fun <T> navArg(name: String): T {
    val nav = Ambients.NavController.current
    val entry = nav.getBackStackEntry(nav.currentDestination!!.id)
    val args = entry.arguments
    val arg = args?.get(name) ?: error("No argument found with name $name")
    @Suppress("UNCHECKED_CAST")
    return arg as T
}

@Composable
fun <T> optionalNavArg(name: String): T? {
    val nav = Ambients.NavController.current
    val entry = nav.getBackStackEntry(nav.currentDestination!!.id)
    val args = entry.arguments
    val arg = args?.get(name)
    @Suppress("UNCHECKED_CAST")
    return arg as? T
}