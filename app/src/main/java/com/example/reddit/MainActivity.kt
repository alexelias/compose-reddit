package com.example.reddit

import androidx.compose.Composable
import androidx.compose.ambient
import androidx.compose.effectOf
import androidx.compose.unaryPlus
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.ui.core.Text
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.MaterialColors
import androidx.ui.material.MaterialTheme
import androidx.ui.material.TopAppBar
import androidx.ui.material.surface.Surface
import com.example.reddit.api.RedditApi
import com.example.reddit.data.RedditRepository
import com.example.reddit.data.RedditRepositoryImpl
import com.example.reddit.navigation.ComposeActivity
import com.example.reddit.navigation.route
import com.example.reddit.screens.PostScreen
import com.example.reddit.screens.SubredditLinkList
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
            SubredditLinkList(subreddit, 10)
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
                AppTheme {
                    Scaffold(subreddit = "androiddev") {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
fun AppTheme(children: @Composable () -> Unit) {
    val colors = MaterialColors(
        surface = Color(0xFFE9E9E9.toInt())
    )
    MaterialTheme(colors, children = children)
}


@Composable
fun Scaffold(subreddit: String, children: @Composable () -> Unit) {
    Column {
        TopAppBar(title = { Text("/r/$subreddit") })
        Container(Flexible(1f)) {
            Surface {
                Container(Expanded, children = children)
            }
        }
    }
}
