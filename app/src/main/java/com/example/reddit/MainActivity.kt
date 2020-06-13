package com.example.reddit

import android.app.Activity
import android.view.View
import android.view.Window
import androidx.animation.AnimationBuilder
import androidx.animation.LinearEasing
import androidx.animation.TweenBuilder
import androidx.compose.*
import androidx.core.os.bundleOf
import androidx.navigation.NavGraphBuilder
import androidx.ui.animation.*
import androidx.ui.core.*
import androidx.ui.core.focus.*
import androidx.ui.foundation.*
import androidx.ui.graphics.*
import androidx.ui.graphics.vector.*
import androidx.ui.input.ImeAction
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.icons.*
import androidx.ui.material.icons.filled.*
import androidx.ui.material.Surface
import androidx.ui.res.vectorResource
import androidx.ui.unit.dp
import com.example.reddit.api.RedditApi
import com.example.reddit.data.RedditRepository
import com.example.reddit.data.RedditRepositoryImpl
import com.example.reddit.navigation.ComposeActivity
import com.example.reddit.navigation.navArg
import com.example.reddit.navigation.optionalNavArg
import com.example.reddit.navigation.route
import com.example.reddit.screens.LoginScreen
import com.example.reddit.screens.PostScreen
import com.example.reddit.screens.SubredditLinkList
import java.util.concurrent.Executors

class MainActivity : ComposeActivity() {
    private val networkExecutor = Executors.newFixedThreadPool(5)

    private val api by lazy { RedditApi.create() }

    private val repository: RedditRepository by lazy { RedditRepositoryImpl(api, networkExecutor) }

    override val initialRoute = R.id.home_screen

    override fun NavGraphBuilder.graph() {
        route(R.id.home_screen) {
            val subreddit = optionalNavArg<String>("subreddit") ?: "androiddev"
            SubredditLinkList(subreddit)
        }
        route(R.id.post_screen) {
            val linkId = navArg<String>("linkId")
            PostScreen(linkId, 10)
        }
        route(R.id.login) {
            LoginScreen()
        }
    }

    @Composable
    override fun content(content: @Composable () -> Unit) {
        Providers(Ambients.Repository provides repository, Ambients.Api provides api) {
            AppTheme(window) {
                Scaffold(subreddit = optionalNavArg<String>("subreddit") ?: "androiddev") {
                    content()
                }
            }
        }
    }
}

object LinkStyle {
    var thumbnails by mutableStateOf(true)
}

// TODO(aelias): This crashes with "setValue doesn't exist" if I change it to "by mutableStateOf"
@Suppress("DEPRECATION")
@Model
object SubredditTheme {
    var accentColor = Color.White
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
        darkColorPalette(
            primary = primary,
            onPrimary = onPrimary,
        )
    } else {
        lightColorPalette(
            primary = primary,
            onPrimary = onPrimary,
        )
    }

    window?.run {
        val sysUiColor = if (isDark) colors.surface else primary
        val animatedSysUiColor = animate(sysUiColor)
        statusBarColor = animatedSysUiColor.toArgb()
        navigationBarColor = animatedSysUiColor.toArgb()
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
val ColorPalette.fadedPrimary get() = primary.copy(alpha = 0.75f)

// Washed out version of primary colour used for the vote buttons
val ColorPalette.fadedOnPrimary get() = onPrimary.copy(alpha = 0.55f)

@Composable
fun Scaffold(subreddit: String, children: @Composable () -> Unit) {
    val scaffoldState = remember { ScaffoldState() }
    Scaffold(
        scaffoldState = scaffoldState,
        topAppBar = {
            TopAppBar(
                title = { Text("/r/$subreddit") }, navigationIcon = {
                IconButton(onClick = { scaffoldState.drawerState = DrawerState.Opened } ) {
                    Icon(Icons.Filled.Menu)
                }
            }, actions = {
                IconButton(onClick = {
                    LinkStyle.thumbnails = !LinkStyle.thumbnails
                }) {
                    Icon(Icons.Filled.ViewAgenda)
                }
            })
        },
        drawerContent = {
            DrawerContent { scaffoldState.drawerState = DrawerState.Closed }
        },
        bodyContent = {
            children()
        }
    )
}

@Composable
fun DrawerContent(closeDrawer: () -> Unit) {
    val navigator = Ambients.NavController.current
    val onNavigate = { subreddit: String ->
        navigator.navigate(R.id.home_screen, bundleOf("subreddit" to subreddit))
        closeDrawer()
    }
    Column(Modifier.fillMaxHeight()) {
        LoginOrAccountItem(closeDrawer)
        DrawerDivider()
        SubredditNavigateField(onNavigate)
        DrawerDivider()
        SubredditLink("/r/android", onNavigate)
        SubredditLink("/r/androiddev", onNavigate)
        SubredditLink("/r/diy", onNavigate)
        SubredditLink("/r/programmerhumor", onNavigate)
        SubredditLink("/r/woodworking", onNavigate)
    }
}

@Composable
private fun DrawerDivider() {
    Box(paddingStart = 8.dp, paddingEnd = 8.dp) {
        Divider(color = Color(0xFFCCCCCC))
    }
}

@Composable
fun LoginOrAccountItem(closeDrawer: () -> Unit) {
    val navigator = Ambients.NavController.current
    ListItem(text = "Log in", onClick = {
        navigator.navigate(R.id.login)
        closeDrawer()
    })
}

@Composable
fun SubredditLink(subreddit: String, onNavigate: (String) -> Unit) {
    ListItem(text = subreddit, onClick = { onNavigate(subreddit.substring(3)) })
}

@Composable
fun SubredditNavigateField(onNavigate: (String) -> Unit) {
    val focusModifier = FocusModifier()
    Box(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp).preferredHeight(96.dp)) {
        Column {
            var text by state { "" }
            FilledTextField(
                modifier = focusModifier,
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter subreddit") },
                imeAction = ImeAction.Go,
                onImeActionPerformed = { _, _ ->
                    onNavigate(text)
                }
            )
            Divider()
        }
    }
}
