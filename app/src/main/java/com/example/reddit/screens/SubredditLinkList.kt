package com.example.reddit.screens

import androidx.animation.FastOutLinearInEasing
import androidx.animation.TweenBuilder
import androidx.compose.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.ui.animation.animatedFloat
import androidx.ui.core.Alignment
import androidx.ui.core.Opacity
import androidx.ui.core.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.unit.*
import com.example.reddit.Ambients
import com.example.reddit.LinkStyle
import com.example.reddit.SubredditTheme
import com.example.reddit.components.ExpandedPost
import com.example.reddit.components.ThumbnailPost
import com.example.reddit.data.AsyncState
import com.example.reddit.data.Link
import com.example.reddit.data.LinkPreview
import com.example.reddit.data.RedditFilterType

@Composable
fun <T> subscribe(data: LiveData<T>): T? {
    val current = stateFor(data) { data.value }

    onCommit(data) {
        val observer = Observer<T> {
            current.value = data.value
        }
        data.observeForever(observer)
        onDispose {
            data.removeObserver(observer)
        }
    }

    return current.value
}

private val sortOptions = listOf(
    RedditFilterType.HOT,
    RedditFilterType.NEW,
    RedditFilterType.TOP
)

@Composable
fun SubredditLinkList(subreddit: String, pageSize: Int = 10) {
    var selectedSortIndex by state { 0 }
    val repository = Ambients.Repository.current
    val model = remember(subreddit, selectedSortIndex) {
        repository.linksOfSubreddit(subreddit, sortOptions[selectedSortIndex], pageSize)
    }
    val info = subscribe(model.info)
    val accentColor = info?.keyColor?.let { if (!it.isBlank()) it.color else null }

    val links = subscribe(model.links)

    val networkState = subscribe(model.networkState) ?: AsyncState.LOADING

    val isLoading = networkState == AsyncState.LOADING || links == null

    val tabs = listOf("Hot", "New", "Top")

    Container(LayoutSize.Fill, alignment = Alignment.TopCenter) {
        // 'fake' tabs hiding
        VerticalScroller {
            Column {
                TabRow(items = tabs, selectedIndex = selectedSortIndex) { index, name ->
                    Tab(
                        text = { Text(name) },
                        selected = selectedSortIndex == index,
                        onSelected = { selectedSortIndex = index })
                }
                // Controls fade out of the progress spinner
                val opacity = animatedFloat(1f)

                onCommit(isLoading, accentColor) {
                    if (accentColor != null) SubredditTheme.accentColor = accentColor
                    if (!isLoading) {
                        opacity.animateTo(0f, anim = TweenBuilder<Float>().apply {
                            easing = FastOutLinearInEasing
                            duration = 500
                        })
                    }
                }

                if (isLoading) {
                    if (opacity.value != 1f) {
                        opacity.snapTo(1f)
                    }
                }
                if (opacity.value == 0f) {
                    PostTheme {
                        ScrollingContent(links!!)
                    }
                } else {
                    Opacity(opacity.value) {
                        LoadingIndicator()
                    }
                }
            }
        }
    }
}

val LinkPreview.imageUrl: String?
    get() = images.firstOrNull()?.source?.decodedUrl

@Composable
fun LoadingIndicator() {
    Container(LayoutPadding(50.dp) + LayoutWidth.Fill, alignment = Alignment.TopCenter) {
        val color = SubredditTheme.accentColor
        val indicatorColor = if (color == Color.White) Color.Black else color
        CircularProgressIndicator(color = indicatorColor)
    }
}

@Composable
fun ScrollingContent(links: PagedList<Link>) {
    Column(LayoutHeight.Fill) {
        Spacer(LayoutHeight(10.dp))
        // do stuff here around PagedList...
        for (item in links.snapshot()) {
            with(item) {
                if (LinkStyle.thumbnails) {
                    ThumbnailPost(
                        id = id,
                        title = title,
                        score = score,
                        author = author,
                        comments = numComments,
                        image = if (!isSelf) preview?.imageUrl else null
                    )
                } else {
                    ExpandedPost(
                        id = id,
                        title = title,
                        score = score,
                        author = author,
                        comments = numComments,
                        image = if (!isSelf) preview?.imageUrl else null,
                        selftext = selftext
                    )
                }
            }
        }
        Spacer(LayoutHeight(10.dp))
    }
}

@Composable
fun PostTheme(children: @Composable () -> Unit) {
    // TODO(aelias): Is there a way to start from a copy of ambient MaterialColors instead of
    // copying the ambient's fields one by one onto a fresh lightColorPalette?
    val colors = lightColorPalette(primary = MaterialTheme.colors().primary,
        onPrimary = MaterialTheme.colors().onPrimary,
        surface = Color.White)
    MaterialTheme(colors, children = children)
}
