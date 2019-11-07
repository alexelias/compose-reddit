package com.example.reddit.navigation

import androidx.compose.ambient
import androidx.compose.effectOf
import com.example.reddit.Ambients

fun <T> navArg(name: String) = effectOf<T> {
    val nav = +ambient(Ambients.NavController)
    val entry = nav.getBackStackEntry(nav.currentDestination!!.id)
    val args = entry.arguments
    val arg = args?.get(name) ?: error("No argument found with name $name")
    @Suppress("UNCHECKED_CAST")
    arg as T
}

fun <T> optionalNavArg(name: String) = effectOf<T?> {
    val nav = +ambient(Ambients.NavController)
    val entry = nav.getBackStackEntry(nav.currentDestination!!.id)
    val args = entry.arguments
    val arg = args?.get(name)
    @Suppress("UNCHECKED_CAST")
    arg as? T
}