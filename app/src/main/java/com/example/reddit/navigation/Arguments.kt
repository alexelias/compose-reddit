// Copyright 2020 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.reddit.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import com.example.reddit.Ambients

fun NavController.navigate(route: String, b: Bundle) {
    var s: String = "$route?"
    for (k in b.keySet()) {
        s += "$k=${b.get(k).toString()}&"
    }
    s = s.dropLast(1)
    navigate(s)
}

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
val defaultSort = 0
val defaultLinkStyle = true

@Composable
fun currentSubreddit(): String {
    return navArg("subreddit") ?: defaultSubreddit
}
