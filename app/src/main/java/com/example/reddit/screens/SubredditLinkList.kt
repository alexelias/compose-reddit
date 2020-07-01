package com.example.reddit.screens

import androidx.compose.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.ui.animation.animatedFloat
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.drawOpacity
import androidx.ui.foundation.*
//import androidx.ui.foundation.lazy.*
import com.example.reddit.lazy.*
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
fun TabStrip(selectedSortIndexState: MutableState<Int>) {
    val tabs = listOf("HOT", "NEW", "TOP")

    val (selectedSortIndex, setIndex) = selectedSortIndexState

    // TODO(lpf): maybe TabRow needs to expose elevation?
    Surface(color = MaterialTheme.colors.primarySurface, elevation = 4.dp) {
        TabRow(
            items = tabs, selectedIndex = selectedSortIndex,
            indicatorContainer = { tabPositions ->
                TabRow.IndicatorContainer(tabPositions, selectedSortIndex) {
                    val colors = MaterialTheme.colors
                    val indicatorColor = if (colors.isLight) {
                        contentColor()
                    } else {
                        colors.primary
                    }
                    TabRow.Indicator(color = indicatorColor)
                }
            }
        ) { index, name ->
            Tab(
                text = { Text(name) },
                selected = selectedSortIndex == index,
                onSelected = { setIndex(index) })
        }
    }
}

@Composable
fun SubredditLinkList(subreddit: String, pageSize: Int = 10) {
    var selectedSortIndex = mutableStateOf(0)
    val repository = Ambients.Repository.current
    val model = remember(subreddit, selectedSortIndex.value) {
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
// TODO(aelias): Fix accent color (as of June 30 dev15, crashes with setAccentColor not found)
//                        SubredditTheme.accentColor = accentColor ?: Color.White
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
fun ScrollingContent(links: PagedList<Link>, header: @Composable () -> Unit) {
    LazyColumnItems(modifier = Modifier.fillMaxHeight(), items = links) { item ->
        with(item) {
            if (isFirstItem()) {
                header()
                Spacer(Modifier.preferredHeight(10.dp))
            }
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
            if (isLastItem()) {
                Spacer(Modifier.preferredHeight(10.dp))
            }
        }
    }
}
