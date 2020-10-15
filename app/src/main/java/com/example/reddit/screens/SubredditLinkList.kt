package com.example.reddit.screens

import androidx.compose.animation.animatedFloat
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
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
    val current = remember(data) { mutableStateOf(data.value) }

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
fun TabStrip(selectedSortIndex: MutableState<Int>) {
    Surface(elevation = 4.dp) {
        TabRow(selectedTabIndex = selectedSortIndex.value) {
            sortOptions.forEachIndexed { index, filterType ->
                Tab(
                    text = { Text(filterType.displayText) },
                    selected = selectedSortIndex.value == index,
                    onClick = { selectedSortIndex.value = index }
                )
            }
        }
    }
}

@Composable
fun SubredditLinkList(subreddit: String, pageSize: Int = 10) {
    val selectedSortIndex = remember { mutableStateOf(0) }
    val repository = Ambients.Repository.current
    val model = remember(subreddit, selectedSortIndex.value, pageSize) {
        repository.linksOfSubreddit(subreddit, sortOptions[selectedSortIndex.value], pageSize)
    }
    val info = subscribe(model.info)
    val accentColor = info?.keyColor?.let { if (!it.isBlank()) it.color else null }

    val links = subscribe(model.links)

    val networkState = subscribe(model.networkState) ?: AsyncState.LOADING

    val isLoading = networkState == AsyncState.LOADING || links == null

    Box(Modifier.fillMaxSize().wrapContentSize(Alignment.TopCenter)) {
        Column {

            // Controls fade out of the progress spinner
            val opacity = animatedFloat(1f)

            onCommit(isLoading, accentColor) {
                if (!isLoading) {
                    SubredditTheme.accentColor = accentColor ?: Color.White
                    opacity.animateTo(0f)
                }
            }

            if (isLoading) {
                if (opacity.value != 1f) {
                    opacity.snapTo(1f)
                }
            }
            if (opacity.value == 0f) {
                ScrollingContent(links!!) {
                    TabStrip(selectedSortIndex)
                }
            } else {
                TabStrip(selectedSortIndex)
                LoadingIndicator(opacity.value)
            }
        }
    }
}

val LinkPreview.imageUrl: String?
    get() = images.firstOrNull()?.source?.decodedUrl

@Composable
fun LoadingIndicator(opacity: Float) {
    Box(Modifier.drawOpacity(opacity).padding(50.dp).fillMaxWidth().wrapContentSize(Alignment.TopCenter)) {
        val color = MaterialTheme.colors.primary
        val indicatorColor = if (color == Color.White) MaterialTheme.colors.onSurface else color
        CircularProgressIndicator(color = indicatorColor)
    }
}

@Composable
@OptIn(ExperimentalLazyDsl::class)
fun ScrollingContent(links: PagedList<Link>, header: @Composable () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        item {
            header()
            Spacer(Modifier.preferredHeight(10.dp))
        }
        items(links) {
            with(it) {
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
        item {
            Spacer(Modifier.preferredHeight(10.dp))
        }
    }
}
