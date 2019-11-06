package com.example.reddit.screens

import androidx.animation.FastOutLinearInEasing
import androidx.animation.TweenBuilder
import androidx.compose.*
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.ui.animation.animatedColor
import androidx.ui.animation.animatedFloat
import androidx.ui.core.Modifier
import androidx.ui.core.Opacity
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.selection.Toggleable
import androidx.ui.foundation.shape.DrawShape
import androidx.ui.foundation.shape.RectangleShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Card
import androidx.ui.material.surface.Surface
import androidx.ui.res.vectorResource
import androidx.ui.text.font.FontStyle
import com.example.reddit.*
import com.example.reddit.R
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
            }
            else {
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
                    Post(
                        id = id,
                        title = title,
                        score = score,
                        author = author,
                        comments = numComments
                    )
                }
            }
            HeightSpacer(height = 10.dp)
        }
    }
}

@Composable
fun Post(id: String, title: String, score: Int, author: String, comments: Int) {
    val voteStatus = +state<Boolean?> { null }
    val upvoteColor = Color(0xFFFF8B60)
    val downvoteColor = Color(0xFF9494FF)
    val fadedPrimary = +themeColor { fadedPrimary }
    val cardColor = when (voteStatus.value) {
        null -> fadedPrimary
        true -> upvoteColor
        false -> downvoteColor
    }

    val animatedColor = +animatedColor(cardColor)

    +memo(cardColor) {
        animatedColor.animateTo(
            cardColor,
            anim = TweenBuilder<Color>().apply { duration = 200 })
    }

    //TODO: Shouldn't have hardcoded values for height, but no idea how to make it
    // work so that whatever item is taller decides the height of this row, and then
    // the individual parts can be flexible within that overall space.
    Container(Spacing(10.dp) wraps ExpandedWidth, height = 100.dp) {
        Card(color = Color.White, shape = RoundedCornerShape(10.dp), elevation = 2.dp) {
            DrawShape(shape = RectangleShape, color = animatedColor.value)
            PostContent(id, title, score, author, comments, voteStatus)
        }
    }
}

@Composable
fun PostContent(
    id: String,
    title: String,
    score: Int,
    author: String,
    comments: Int,
    voteStatus: State<Boolean?>
) {
    Row(
        Expanded,
        crossAxisAlignment = CrossAxisAlignment.Center,
        crossAxisSize = LayoutSize.Expand
    ) {
        Container(width = 60.dp) {
            ScoreSection(score, voteStatus)
        }
        Container(Flexible(1f)) {
            MainPostCard(id = id, title = title, author = author, comments = comments)
        }
    }
}

@Composable
fun ScoreSection(score: Int, voteStatus: State<Boolean?>) {
    Column(Expanded, crossAxisAlignment = CrossAxisAlignment.Center) {
        VoteArrow(
            Flexible(1f),
            R.drawable.ic_baseline_arrow_drop_up_24,
            voteStatus.value == true
        ) { selected ->
            voteStatus.value = if (selected && voteStatus.value != true) true else null
        }
        HeightSpacer(2.dp)
        Text(
            text = "$score",
            style = (+themeTextStyle { h6 }).copy(color = +themeColor { onPrimary })
        )
        Text(
            "points",
            style = (+themeTextStyle { overline }).copy(color = +themeColor { onPrimary })
        )
        HeightSpacer(2.dp)
        VoteArrow(
            Flexible(1f),
            R.drawable.ic_baseline_arrow_drop_down_24,
            voteStatus.value == false
        ) { selected ->
            voteStatus.value = if (selected && voteStatus.value != false) false else null
        }
    }
}

@Composable
fun VoteArrow(
    modifier: Modifier,
    vectorResource: Int,
    selected: Boolean,
    onSelected: (Boolean) -> Unit
) {
    val vector = +vectorResource(vectorResource)
    Ripple(bounded = true) {
        Toggleable(checked = selected, onCheckedChange = onSelected) {
            Container(modifier wraps ExpandedWidth) {
                Container(width = 24.dp, height = 24.dp) {
                    val tintColor = +themeColor {
                        if (selected) onPrimary else fadedOnPrimary
                    }
                    val animatedColor = +animatedColor(tintColor)
                    +memo(tintColor) {
                        animatedColor.animateTo(
                            tintColor,
                            anim = TweenBuilder<Color>().apply { duration = 200 })
                    }
                    DrawVector(
                        vectorImage = vector,
                        tintColor = animatedColor.value
                    )
                }
            }
        }
    }
}

@Composable
fun MainPostCard(id: String, title: String, author: String, comments: Int) {
    val navigator = +ambient(Ambients.NavController)
    Surface(elevation = 4.dp) {
        Ripple(bounded = true) {
            Clickable({
                navigator.navigate(R.id.post_screen, bundleOf("linkId" to id))
            }) {
                Wrap {
                    Column(
                        Spacing(
                            left = 10.dp,
                            right = 10.dp,
                            top = 5.dp,
                            bottom = 5.dp
                        ) wraps Expanded
                    ) {
                        Text(title, style = +themeTextStyle { subtitle1 })
                        Column(Flexible(1f), mainAxisAlignment = MainAxisAlignment.End) {
                            Text(
                                text = "u/$author",
                                style = (+themeTextStyle { overline }).copy(fontStyle = FontStyle.Italic)
                            )
                            Text(
                                text = "$comments comments",
                                style = +themeTextStyle { overline })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostTheme(children: @Composable () -> Unit) {
    val colors = (+ambient(Colors)).copy(surface = Color.White)
    MaterialTheme(colors, children = children)
}
