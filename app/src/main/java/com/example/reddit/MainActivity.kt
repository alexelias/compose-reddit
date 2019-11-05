package com.example.reddit

import android.app.Activity
import android.view.Window
import androidx.compose.*
import androidx.navigation.NavGraphBuilder
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.foundation.Clickable
import androidx.ui.graphics.Color
import androidx.ui.graphics.toArgb
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.layout.Column
import androidx.ui.layout.Container
import androidx.ui.layout.Expanded
import androidx.ui.material.MaterialColors
import androidx.ui.material.MaterialTheme
import androidx.ui.material.TopAppBar
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.material.themeColor
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
                    Scaffold(subreddit = "androiddev") {
                        content()
                    }
                }
            }
        }
    }
}

@Model
object SubredditTheme {
    var accentColor: Color = Color.White
}

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
val MaterialColors.fadedPrimary get() = primary.copy(alpha = 0.55f)

// Washed out version of primary colour used for the vote buttons
val MaterialColors.fadedOnPrimary get() = onPrimary.copy(alpha = 0.55f)

@Composable
fun Scaffold(subreddit: String, children: @Composable () -> Unit) {
    Column {
        TopAppBar(title = { Text("/r/$subreddit") }, navigationIcon = {
            val vectorAsset = +vectorResource(R.drawable.ic_menu)
            // Copied from AppBarIcon which doesn't support vector resources ATM
            Ripple(bounded = false) {
                Clickable({ /** Drawer */ }) {
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
