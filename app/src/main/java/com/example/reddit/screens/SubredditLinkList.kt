package com.example.reddit.screens

import androidx.compose.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.ui.animation.Crossfade
import androidx.ui.core.Alignment
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Card
import androidx.ui.material.surface.Surface
import androidx.ui.text.font.FontStyle
import com.example.reddit.Ambients
import com.example.reddit.Navigator
import com.example.reddit.data.AsyncState
import com.example.reddit.data.RedditFilterType

fun <T> subscribe(data: LiveData<T>) = effectOf<T?> {
    val current = +stateFor(data) { data.value }

    +onCommit(data) {
        val observer = Observer<T> {
            current.value = data.value
        }
        data.observeForever(observer)
        onDispose {
            data.removeObserver(observer)
        }
    }

    current.value
}

private val sortOptions = listOf(
    RedditFilterType.HOT,
    RedditFilterType.NEW,
    RedditFilterType.TOP
)

@Composable
fun SubredditLinkList(subreddit: String, pageSize: Int = 10) {
    val selectedSortIndex = +state { 0 }
    val repository = +ambient(Ambients.Repository)
    val model = +modelFor(subreddit, selectedSortIndex.value) {
        repository.linksOfSubreddit(subreddit, sortOptions[selectedSortIndex.value], pageSize)
    }
    val links = +subscribe(model.links)

    val networkState = +subscribe(model.networkState) ?: AsyncState.LOADING

    val isLoading = networkState == AsyncState.LOADING || links == null

    Crossfade(current = isLoading) { loading ->
        if (loading) {
            Container(expanded = true) {
                CircularProgressIndicator()
            }
        } else {
            VerticalScroller {
                Column(Expanded) {
                    HeightSpacer(height = 10.dp)
                    PostTheme {
                        // do stuff here around PagedList...
                        for (item in links!!.snapshot()) {
                            with(item) {
                                Post(
                                    id = item.id,
                                    title = title,
                                    score = score,
                                    author = author,
                                    comments = numComments
                                )
                            }
                        }
                    }
                    HeightSpacer(height = 10.dp)
                }
            }
        }
    }
}

@Composable
fun Post(id: String, title: String, score: Int, author: String, comments: Int) {
    Container(Spacing(10.dp) wraps ExpandedWidth, height = 100.dp) {
        Card(color = +themeColor { primary }, shape = RoundedCornerShape(10.dp), elevation = 2.dp) {
            Row(
                Expanded,
                mainAxisAlignment = MainAxisAlignment.SpaceBetween,
                crossAxisSize = LayoutSize.Expand
            ) {
                ScoreText(score)
                Container(Flexible(1f)) {
                    MainPostCard(id = id, title = title, author = author, comments = comments)
                }
            }
        }
    }
}

@Composable
fun ScoreText(score: Int) {
    Container(ExpandedHeight, width = 50.dp) {
        Column(crossAxisAlignment = CrossAxisAlignment.Center) {
            Text(text = "$score", style = +themeTextStyle { h6 })
            Text("points", style = +themeTextStyle { overline })
        }
    }
}

@Composable
fun MainPostCard(id: String, title: String, author: String, comments: Int) {
    Surface {
        Ripple(bounded = true) {
            Clickable({ Navigator.route = "/comments/$id" }) {
                Wrap {
                    Column(
                        Spacing(
                            left = 15.dp,
                            right = 10.dp,
                            top = 10.dp,
                            bottom = 10.dp
                        ) wraps Expanded
                    ) {
                        Text(title, style = +themeTextStyle { subtitle1 })
                        Text(
                            modifier = Spacing(top = 5.dp),
                            text = "u/$author",
                            style = (+themeTextStyle { overline }).copy(fontStyle = FontStyle.Italic)
                        )
                        Container(Flexible(1f), alignment = Alignment.BottomLeft) {
                            Text("$comments comments", style = +themeTextStyle { overline })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostTheme(children: @Composable() () -> Unit) {
    val colors = (+ambient(Colors)).copy(surface = Color.White)
    MaterialTheme(colors, children = children)
}
