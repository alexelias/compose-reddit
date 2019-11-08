package com.example.reddit

import android.app.Activity
import android.view.Window
import androidx.animation.AnimatedValue
import androidx.animation.TweenBuilder
import androidx.animation.ValueHolder
import androidx.compose.*
import androidx.core.os.bundleOf
import androidx.compose.Composable
import androidx.compose.unaryPlus
import androidx.navigation.NavGraphBuilder
import androidx.ui.core.*
import androidx.ui.foundation.Clickable
import androidx.ui.graphics.Color
import androidx.ui.graphics.lerp
import androidx.ui.graphics.toArgb
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.input.ImeAction
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.layout.Column
import androidx.ui.layout.Container
import androidx.ui.layout.Expanded
import androidx.ui.material.MaterialColors
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
            val subreddit = +optionalNavArg<String>("subreddit") ?: "androiddev"
            SubredditLinkList(subreddit)
        }
        route(R.id.post_screen) {
            val linkId = +navArg<String>("linkId")
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
                    Scaffold(subreddit = +optionalNavArg<String>("subreddit") ?: "androiddev") {
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
    var accentColor: Color
        set(color) {
            if (_accentColor.targetValue != color) {
                _accentColor.animateTo(color, anim = TweenBuilder<Color>().apply { duration = 500 })
            }
        }
        get() = _accentColor.value
    private var _accentColor = AnimatedValue(AnimValueHolder(Color.White, ::lerp))
}

// copied from AnimatedValueEffects
@Model
private class AnimValueHolder<T>(
    override var value: T,
    override val interpolator: (T, T, Float) -> T
) : ValueHolder<T>


/**
 * Provides the [MaterialTheme] for the application and also sets statusbar / nav bar colours for
 * the [Activity]'s [Window].
 */
@Composable
fun AppTheme(window: Window? = null, children: @Composable () -> Unit) {
    val primary = SubredditTheme.accentColor
    val onPrimary = if (primary == Color.White) Color.Black else Color.White
    val surface = Color(0xFFF1F1F1)
    val isLightStatusBar = primary == Color.White
    val colors = MaterialColors(
        primary = primary,
        onPrimary = onPrimary,
        surface = surface
    )

    window?.run {
        statusBarColor = primary.toArgb()
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
val MaterialColors.fadedPrimary get() = primary.copy(alpha = 0.75f)

// Washed out version of primary colour used for the vote buttons
val MaterialColors.fadedOnPrimary get() = onPrimary.copy(alpha = 0.55f)

@Composable
fun Scaffold(subreddit: String, children: @Composable () -> Unit) {
    val (state, toggleDrawer) = +state { DrawerState.Closed }
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
    val navigator = +ambient(Ambients.NavController)
    val onNavigate = { subreddit: String ->
        navigator.navigate(R.id.home_screen, bundleOf("subreddit" to subreddit))
        closeDrawer()
    }
    Column(ExpandedHeight) {
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
    val navigator = +ambient(Ambients.NavController)
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
    val focusManager = +ambient(FocusManagerAmbient)
    Clickable({ focusManager.requestFocusById(focusIdentifier) }) {
        Container(ExpandedWidth wraps Spacing(left = 16.dp, right = 16.dp), height = 96.dp) {
            Column {
                var text by +state { "" }
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
    Column(Expanded) {
        TopAppBar(title = { Text("/r/$subreddit") }, navigationIcon = {
            VectorAppBarIcon(R.drawable.ic_menu, openDrawer)
        }, actionData = listOf(R.drawable.ic_baseline_view_agenda_24)) { resId ->
            VectorAppBarIcon(resId) {
                LinkStyle.thumbnails = !LinkStyle.thumbnails
            }
        }
        Container(Flexible(1f)) {
            Surface {
                Container(Expanded, children = children)
            }
        }
    }
}

@Composable
private fun VectorAppBarIcon(resId: Int, onClick: () -> Unit) {
    val vectorAsset = +vectorResource(resId)
    // Copied from AppBarIcon which doesn't support vector resources ATM
    Ripple(bounded = false, radius = 24.dp) {
        Clickable(onClick) {
            // App bar has some default padding so touch target doesn't really work
            Container(height = 48.dp, width = 24.dp) {
                Container(width = 24.dp, height = 24.dp) {
                    DrawVector(
                        vectorImage = vectorAsset,
                        tintColor = +themeColor { onPrimary })
                }
            }
        }
    }
}
