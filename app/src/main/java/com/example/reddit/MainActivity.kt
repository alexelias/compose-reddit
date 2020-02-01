package com.example.reddit

import android.app.Activity
import android.view.Window
import androidx.animation.*
import androidx.compose.*
import androidx.core.os.bundleOf
import androidx.compose.Composable
import androidx.compose.unaryPlus
import androidx.navigation.NavGraphBuilder
import androidx.ui.animation.*
import androidx.ui.core.*
import androidx.ui.foundation.Clickable
import androidx.ui.geometry.*
import androidx.ui.graphics.Color
import androidx.ui.graphics.lerp
import androidx.ui.graphics.toArgb
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.input.ImeAction
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.unit.*
import androidx.ui.layout.Column
import androidx.ui.layout.Container
import androidx.ui.material.MaterialTheme
import androidx.ui.material.TopAppBar
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.res.vectorResource
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
        Ambients.Repository.Provider(repository) {
            Ambients.Api.Provider(api) {
                AppTheme(window) {
                    Scaffold(subreddit = optionalNavArg<String>("subreddit") ?: "androiddev") {
                        content()
                    }
                }
            }
        }
    }
}

@Model
object LinkStyle {
    var thumbnails = true
}

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
    val primary = animatedColor(SubredditTheme.accentColor)

    onCommit(SubredditTheme.accentColor) {
        primary.animateTo(SubredditTheme.accentColor, TweenBuilder<Color>().apply {
            easing = LinearEasing
            duration = 500
        })
    }

    val onPrimary = if (primary.targetValue == Color.White) Color.Black else Color.White
    val surface = Color(0xFFF1F1F1)
    val isLightStatusBar = primary.targetValue == Color.White
    val colors = lightColorPalette(
        primary = primary.value,
        onPrimary = onPrimary,
        surface = surface
    )

    window?.run {
        statusBarColor = primary.value.toArgb()
        navigationBarColor = surface.toArgb()
        decorView.run {
            val someFlags = if (isLightStatusBar) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or someFlags
        }
    }

    MaterialTheme(colors, children = children)
}

// Washed out version of primary colour used for the score surface
val ColorPalette.fadedPrimary get() = primary.copy(alpha = 0.75f)

// Washed out version of primary colour used for the vote buttons
val ColorPalette.fadedOnPrimary get() = onPrimary.copy(alpha = 0.55f)

@Composable
fun Scaffold(subreddit: String, children: @Composable () -> Unit) {
    val (state, toggleDrawer) = state { DrawerState.Closed }
    ModalDrawerLayout(
        drawerState = state,
        onStateChange = toggleDrawer,
        gesturesEnabled = false,
        drawerContent = { DrawerContent { toggleDrawer(DrawerState.Closed) } },
        bodyContent = { MainContent(subreddit, { toggleDrawer(DrawerState.Opened) }, children) }
    )
}

@Composable
fun DrawerContent(closeDrawer: () -> Unit) {
    val navigator = ambient(Ambients.NavController)
    val onNavigate = { subreddit: String ->
        navigator.navigate(R.id.home_screen, bundleOf("subreddit" to subreddit))
        closeDrawer()
    }
    Column(LayoutHeight.Fill) {
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

private fun DrawerDivider() {
    Container(padding = EdgeInsets(left = 8.dp, right = 8.dp)) {
        Divider(color = Color(0xFFCCCCCC))
    }
}

@Composable
fun LoginOrAccountItem(closeDrawer: () -> Unit) {
    val navigator = ambient(Ambients.NavController)
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
    val focusIdentifier = "subredditnavigate"
    val focusManager = ambient(FocusManagerAmbient)
    Clickable({ focusManager.requestFocusById(focusIdentifier) }) {
        Container(LayoutWidth.Fill + LayoutPadding(left = 16.dp, right = 16.dp), height = 96.dp) {
            Column {
                var text by state { "" }
                Text("Enter subreddit")
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    imeAction = ImeAction.Go,
                    focusIdentifier = focusIdentifier,
                    onImeActionPerformed = {
                        onNavigate(text)
                    }
                )
                Divider()
            }
        }
    }
}

@Composable
fun MainContent(subreddit: String, openDrawer: () -> Unit, children: @Composable () -> Unit) {
    Column(LayoutHeight.Fill) {
        TopAppBar(title = { Text("/r/$subreddit") }, navigationIcon = {
            VectorAppBarIcon(R.drawable.ic_menu, openDrawer)
        }, actionData = listOf(R.drawable.ic_baseline_view_agenda_24)) { resId ->
            VectorAppBarIcon(resId) {
                LinkStyle.thumbnails = !LinkStyle.thumbnails
            }
        }
        Container(LayoutFlexible(1f)) {
            Surface {
                Container(LayoutSize.Fill, children = children)
            }
        }
    }
}

@Composable
private fun VectorAppBarIcon(resId: Int, onClick: () -> Unit) {
    val vectorAsset = vectorResource(resId)
    // Copied from AppBarIcon which doesn't support vector resources ATM
    Ripple(bounded = false, radius = 24.dp) {
        Clickable(onClick) {
            // App bar has some default padding so touch target doesn't really work
            Container(height = 48.dp, width = 24.dp) {
                Container(width = 24.dp, height = 24.dp) {
                    DrawVector(
                        vectorImage = vectorAsset,
                        tintColor = MaterialTheme.colors().onPrimary)
                }
            }
        }
    }
}
