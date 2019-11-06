package com.example.reddit

import android.app.Activity
import android.util.Log
import android.view.Window
import androidx.animation.AnimatedValue
import androidx.animation.TweenBuilder
import androidx.animation.ValueHolder
import androidx.compose.*
import androidx.core.os.bundleOf
import androidx.navigation.NavGraphBuilder
import androidx.ui.core.FocusManagerAmbient
import androidx.ui.core.Text
import androidx.ui.core.TextField
import androidx.ui.core.dp
import androidx.ui.core.input.FocusManager
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.shape.DrawShape
import androidx.ui.foundation.shape.RectangleShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.lerp
import androidx.ui.graphics.toArgb
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.input.ImeAction
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.res.vectorResource
import com.example.reddit.api.RedditApi
import com.example.reddit.data.RedditRepository
import com.example.reddit.data.RedditRepositoryImpl
import com.example.reddit.navigation.ComposeActivity
import com.example.reddit.navigation.route
import com.example.reddit.screens.PostScreen
import com.example.reddit.screens.SubredditPage
import java.util.concurrent.Executors

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

class MainActivity : ComposeActivity() {
    private val networkExecutor = Executors.newFixedThreadPool(5)

    private val api by lazy { RedditApi.create() }

    private val repository: RedditRepository by lazy { RedditRepositoryImpl(api, networkExecutor) }

    override val initialRoute = R.id.home_screen

    override fun NavGraphBuilder.graph() {
        route(R.id.home_screen) {
            val subreddit = +optionalNavArg<String>("subreddit") ?: "androiddev"
            SubredditPage(subreddit)
        }
        route(R.id.post_screen) {
            val linkId = +navArg<String>("linkId")
            PostScreen(linkId, 10)
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
fun AppTheme(window: Window, children: @Composable () -> Unit) {
    val primary = SubredditTheme.accentColor
    val onPrimary = if (primary == Color.White) Color.Black else Color.White
    val surface = Color(0xFFF1F1F1)
    val isLightStatusBar = primary == Color.White
    val colors = MaterialColors(
        primary = primary,
        onPrimary = onPrimary,
        surface = surface
    )
    window.statusBarColor = primary.toArgb()
    window.navigationBarColor = surface.toArgb()
    window.decorView.run {
        systemUiVisibility = if (isLightStatusBar) {
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
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
        SubredditNavigateField(onNavigate)
        SubredditLink("/r/android", onNavigate)
        SubredditLink("/r/androiddev", onNavigate)
        SubredditLink("/r/programmerhumor", onNavigate)
    }
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
            val vectorAsset = +vectorResource(R.drawable.ic_menu)
            // Copied from AppBarIcon which doesn't support vector resources ATM
            Ripple(bounded = false) {
                Clickable(openDrawer) {
                    Container(width = 24.dp, height = 24.dp) {
                        DrawVector(
                            vectorImage = vectorAsset,
                            tintColor = +themeColor { onPrimary })
                    }
                }
            }
        })
        Container(Flexible(1f)) {
            Surface {
                Container(Expanded, children = children)
            }
        }
    }
}
