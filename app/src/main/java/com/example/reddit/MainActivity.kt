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

package com.example.reddit

import android.app.Activity
import android.view.View
import android.view.Window
import androidx.compose.animation.animate
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import com.example.reddit.api.RedditApi
import com.example.reddit.data.RedditRepository
import com.example.reddit.data.RedditRepositoryImpl
import com.example.reddit.navigation.*
import com.example.reddit.screens.LoginScreen
import com.example.reddit.screens.PostScreen
import com.example.reddit.screens.SubredditLinkList
import java.util.concurrent.Executors

sealed class Screen {
    object Subreddit : Screen()
    object Post : Screen()
    object Login : Screen()
}

class MainActivity : ComposeActivity() {
    private val networkExecutor = Executors.newFixedThreadPool(5)

    private val api by lazy { RedditApi.create() }

    private val repository: RedditRepository by lazy { RedditRepositoryImpl(api, networkExecutor) }

    @Composable
    override fun content() {
        Providers(Ambients.Repository provides repository, Ambients.Api provides api) {
            AppTheme(window) {
                Scaffold(subreddit = SubredditTheme.subredditTitle) {
                    NavHost(Ambients.NavController.current, startDestination = Screen.Subreddit) {
                        composable(Screen.Subreddit) {
                            val subreddit = navArg("subreddit", it) ?: defaultSubreddit
                            val sort = navArg("sort", it) ?: defaultSort
                            val linkStyle = navArg("linkStyle", it) ?: defaultLinkStyle
                            onCommit(subreddit, sort, linkStyle) {
                                SubredditTheme.subredditTitle = subreddit
                                LinkStyle.sort = sort
                                LinkStyle.thumbnails = linkStyle
                            }
                            SubredditLinkList(subreddit, sort,10)
                        }
                        composable(Screen.Post) { PostScreen(navArg("linkId", it)!!, 10) }
                        composable(Screen.Login) { LoginScreen() }
                    }
                }
            }
        }
    }
}


object LinkStyle {
    var sort by mutableStateOf(defaultSort)
    var thumbnails by mutableStateOf(defaultLinkStyle)
}

object SubredditTheme {
    var accentColor by mutableStateOf(Color.White)
    var subredditTitle by mutableStateOf(defaultSubreddit)
}

/**
 * Provides the [MaterialTheme] for the application and also sets statusbar / nav bar colours for
 * the [Activity]'s [Window].
 */
@Composable
fun AppTheme(window: Window? = null, children: @Composable () -> Unit) {
    val primary = animate(SubredditTheme.accentColor)
    val isDark = isSystemInDarkTheme()

    val isLightStatusBar = SubredditTheme.accentColor == Color.White && !isDark
    val onPrimary = if (isLightStatusBar) Color.Black else Color.White
    val colors = if (isDark) {
        darkColors(
            primary = primary,
            onPrimary = onPrimary,
        )
    } else {
        lightColors(
            primary = primary,
            onPrimary = onPrimary,
        )
    }

    window?.run {
        val sysUiColor = if (isDark) colors.surface else primary
        val animatedSysUiColor = animate(sysUiColor)
        statusBarColor = animatedSysUiColor.toArgb()
        navigationBarColor = animatedSysUiColor.toArgb()
        @Suppress("DEPRECATION")
        decorView.run {
            val someFlags = if (isLightStatusBar) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or someFlags
        }
    }

    MaterialTheme(colors, content = children)
}

// Washed out version of primary colour used for the score surface
val Colors.fadedPrimary get() = primary.copy(alpha = 0.75f)
// Washed out version of primary colour used for the vote buttons
val Colors.fadedOnPrimary get() = onPrimary.copy(alpha = 0.55f)

@Composable
fun Scaffold(subreddit: String, children: @Composable () -> Unit) {
    val navigator: NavController = Ambients.NavController.current
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { BasicText("/r/$subreddit") }, navigationIcon = {
                IconButton(onClick = { scaffoldState.drawerState.open() } ) {
                    Icon(Icons.Filled.Menu)
                }
            }, actions = {
                IconButton(onClick = {
                    navigator.navigate(Screen.Subreddit, bundleOf("subreddit" to subreddit, "sort" to LinkStyle.sort, "linkStyle" to !LinkStyle.thumbnails))
                }) {
                    if (LinkStyle.thumbnails == true) {
                        Icon(Icons.Filled.ViewAgenda)
                    } else {
                        Icon(Icons.Filled.ViewHeadline)
                    }
                }
            })
        },
        drawerContent = {
            DrawerContent { scaffoldState.drawerState.close() }
        },
        bodyContent = {
            children()
        }
    )
}

@Composable
fun DrawerContent(closeDrawer: () -> Unit) {
    val navigator: NavController = Ambients.NavController.current
    val onNavigate = { subreddit: String ->
        navigator.navigate(Screen.Subreddit, bundleOf("subreddit" to subreddit, "sort" to defaultSort, "linkStyle" to defaultLinkStyle))
        closeDrawer()
    }
    Column(Modifier.fillMaxHeight()) {
        LoginOrAccountItem(closeDrawer)
        SubredditNavigateField(onNavigate)

        ListItem  {
            Row {
                Icon(Icons.Filled.Star)
                Spacer(Modifier.preferredWidth(6.dp))
                BasicText(text = "Favorites:", style = TextStyle(fontWeight = FontWeight.Bold))
            }
        }
        Row {
            Spacer(Modifier.preferredWidth(50.dp))
            Column {
                SubredditLink("/r/android", onNavigate)
                SubredditLink("/r/androiddev", onNavigate)
                SubredditLink("/r/diy", onNavigate)
                SubredditLink("/r/programmerhumor", onNavigate)
                SubredditLink("/r/woodworking", onNavigate)
            }
        }
    }
}

@Composable
private fun DrawerDivider() {
    Box(Modifier.padding(start = 8.dp, end = 8.dp)) {
        Divider(color = Color(0xFFCCCCCC))
    }
}

@Composable
fun ColumnScope.LoginOrAccountItem(closeDrawer: () -> Unit) {
    val navigator = Ambients.NavController.current

    Button(
        modifier = Modifier.align(Alignment.End).padding(12.dp),
        colors = ButtonConstants.defaultButtonColors(
            backgroundColor = MaterialTheme.colors.secondary
        ),
        onClick = {
            navigator.navigate(Screen.Login)
            closeDrawer()
        }
    ) {
        Icon(Icons.Filled.Person)
        Spacer(Modifier.preferredWidth(6.dp))
        BasicText("Log in")
    }
}

@Composable
fun SubredditLink(subreddit: String, onNavigate: (String) -> Unit) {
    ListItem(text = { BasicText(subreddit) }, modifier = Modifier.clickable { onNavigate(subreddit.substring(3)) })
}

@Composable
fun SubredditNavigateField(onNavigate: (String) -> Unit) {
    Box(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp).preferredHeight(70.dp)
    ) {
        Column {
            var text by remember { mutableStateOf("") }
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { BasicText("Enter subreddit") },
//                imeAction = ImeAction.Go,
                onImeActionPerformed = { _, _ ->
                    onNavigate(text)
                }
            )
        }
    }
}
