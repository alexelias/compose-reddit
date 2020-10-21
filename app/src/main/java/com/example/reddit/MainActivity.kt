package com.example.reddit

import android.app.Activity
import android.view.View
import android.view.Window
import androidx.compose.animation.animate
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewAgenda
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
import androidx.navigation.NavGraphBuilder
import com.example.reddit.api.RedditApi
import com.example.reddit.data.RedditRepository
import com.example.reddit.data.RedditRepositoryImpl
import com.example.reddit.navigation.*
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
            val subreddit = currentSubreddit()
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
                Scaffold(subreddit = currentSubreddit()) {
                    content()
                }
            }
        }
    }
}

object LinkStyle {
    var thumbnails by mutableStateOf(true)
}

object SubredditTheme {
    var accentColor by mutableStateOf(Color.White)
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
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("/r/$subreddit") }, navigationIcon = {
                IconButton(onClick = { scaffoldState.drawerState.open() } ) {
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
            DrawerContent { scaffoldState.drawerState.close() }
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
        SubredditNavigateField(onNavigate)

        ListItem  {
            Row {
                Icon(Icons.Filled.Star)
                Spacer(Modifier.preferredWidth(6.dp))
                Text(text = "Favorites:", style = TextStyle(fontWeight = FontWeight.Bold))
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
/*        colors = ButtonConstants.defaultButtonColors(
            backgroundColor = MaterialTheme.colors.secondary
        ), */ // should work in alpha06 to use generic reddit color instead of subreddit color
        onClick = {
            navigator.navigate(R.id.login)
            closeDrawer()
        }
    ) {
        Icon(Icons.Filled.Person)
        Spacer(Modifier.preferredWidth(6.dp))
        Text("Log in")
    }
}

@Composable
fun SubredditLink(subreddit: String, onNavigate: (String) -> Unit) {
    ListItem(text = { Text(subreddit) }, modifier = Modifier.clickable { onNavigate(subreddit.substring(3)) })
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
                label = { Text("Enter subreddit") },
                imeAction = ImeAction.Go,
                onImeActionPerformed = { _, _ ->
                    onNavigate(text)
                }
            )
        }
    }
}
