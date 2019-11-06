package com.example.reddit.screens

import androidx.animation.FastOutLinearInEasing
import androidx.animation.TweenBuilder
import androidx.compose.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.ui.animation.animatedFloat
import androidx.ui.core.Opacity
import androidx.ui.core.dp
import androidx.ui.foundation.VerticalScroller
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.Colors
import androidx.ui.material.MaterialTheme
import com.example.reddit.Ambients
import com.example.reddit.LinkStyle
import com.example.reddit.SubredditTheme
import com.example.reddit.components.ExpandedPost
import com.example.reddit.components.ThumbnailPost
import com.example.reddit.data.AsyncState
import com.example.reddit.data.Link
import com.example.reddit.data.RedditFilterType
import com.example.reddit.data.SubredditResponse
import retrofit2.Call
import retrofit2.Response

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
fun SubredditPage(subreddit: String) {
    var accentColor by +state<Color?> { null }
    val api = +ambient(Ambients.Api)

    +memo(subreddit) {
        val subredditInfo = api.getSubredditInformation(subreddit)
        subredditInfo.enqueue(object : retrofit2.Callback<SubredditResponse> {
            override fun onFailure(call: Call<SubredditResponse>, t: Throwable) {
            }

            override fun onResponse(
                call: Call<SubredditResponse>,
                response: Response<SubredditResponse>
            ) {
                if (response.isSuccessful) {
                    val subredditAccentColorString = response.body()!!.data.keyColor
                    accentColor =
                        Color(android.graphics.Color.parseColor(subredditAccentColorString))
                }
            }
        })
    }

    SubredditLinkList(subreddit, accentColor)
}

@Composable
fun SubredditLinkList(subreddit: String, accentColor: Color?, pageSize: Int = 10) {
    val selectedSortIndex = +state { 0 }
    val repository = +ambient(Ambients.Repository)
    val model = +modelFor(subreddit, selectedSortIndex.value) {
        repository.linksOfSubreddit(subreddit, sortOptions[selectedSortIndex.value], pageSize)
    }
    val links = +subscribe(model.links)

    val networkState = +subscribe(model.networkState) ?: AsyncState.LOADING

    val isLoading = networkState == AsyncState.LOADING || links == null || accentColor == null

    // Controls fade out of the progress spinner
    val opacity = +animatedFloat(1f)

    if (isLoading) {
        if (opacity.value != 1f) {
            opacity.snapTo(1f)
        }
    } else {
        +memo(accentColor!!) {
            SubredditTheme.accentColor = accentColor
            opacity.animateTo(0f, anim = TweenBuilder<Float>().apply {
                easing = FastOutLinearInEasing
                duration = 500
            })
        }
    }

    Stack(Expanded) {
        expanded {
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

@Composable
fun LoadingIndicator() {
    Container {
        val color = SubredditTheme.accentColor
        val indicatorColor = if (color == Color.White) Color.Black else color
        CircularProgressIndicator(color = indicatorColor)
    }
}

@Composable
fun ScrollingContent(links: PagedList<Link>) {
    VerticalScroller {
        Column(Expanded) {
            HeightSpacer(height = 10.dp)
            // do stuff here around PagedList...
            for (item in links.snapshot()) {
                with(item) {
                    if (LinkStyle.thumbnails) {
                        ThumbnailPost(
                            id = id,
                            title = title,
                            score = score,
                            author = author,
                            comments = numComments
                        )
                    } else {
                        ExpandedPost(
                            id = id,
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

@Composable
fun PostTheme(children: @Composable () -> Unit) {
    val colors = (+ambient(Colors)).copy(surface = Color.White)
    MaterialTheme(colors, children = children)
}
