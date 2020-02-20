package com.example.reddit.screens

import androidx.animation.FastOutLinearInEasing
import androidx.animation.TweenBuilder
import androidx.compose.*
import androidx.ui.animation.animatedFloat
import androidx.ui.core.*
import androidx.ui.geometry.Offset
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.shape.RectangleShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.Paint
import androidx.ui.layout.*
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Card
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontStyle
import androidx.ui.text.font.FontWeight
import androidx.ui.unit.*
import com.example.reddit.Ambients
import com.example.reddit.components.Image
import com.example.reddit.components.TimeAgo
import com.example.reddit.data.*
import com.example.reddit.screens.Colors2.TEXT_DARK
import com.example.reddit.screens.Colors2.TEXT_MUTED

@Composable
fun PostScreen(linkId: String, pageSize: Int = 10, initialLink: Link? = null) {
    val repository = Ambients.Repository.current
    val linkModel = remember(linkId, pageSize) { repository.linkDetails(linkId, pageSize) }

    val link = subscribe(linkModel.link) ?: initialLink
    val comments = subscribe(linkModel.comments)
    val networkState = subscribe(linkModel.networkState)

    val isLoading = networkState == AsyncState.LOADING
    Stack(LayoutSize.Fill) {
        // Controls fade out of the progress spinner
        val opacity = animatedFloat(1f)

        onCommit(isLoading) {
            if (isLoading) {
                if (opacity.value != 1f) {
                    opacity.snapTo(1f)
                }
            } else {
                opacity.animateTo(0f, anim = TweenBuilder<Float>().apply {
                    easing = FastOutLinearInEasing
                    duration = 500
                })
            }
        }

        if (opacity.value == 0f) {
            ScrollingContent(link, linkModel, comments)
        } else {
            Opacity(opacity.value) {
                LoadingIndicator()
            }
        }
    }
}

@Composable
fun ScrollingContent(link: Link?, linkModel: LinkModel, comments: List<HierarchicalThing>?) {
    VerticalScroller {
        Column(LayoutHeight.Fill + LayoutAlign.Top) {
            if (link != null) {
                LinkCard(
                    title = link.title,
                    image = link.preview?.imageUrl,
                    author = link.author,
                    score = link.score,
                    createdAt = link.createdUtc,
                    comments = link.numComments
                )
            }

            comments?.forEachIndexed { index, node ->
                CommentRow(isFirst = index == 0, node = node, onClick = {
                    when (node) {
                        is RedditMore -> linkModel.loadMore(node)
                        is Comment -> linkModel.toggleCollapsedState(node)
                    }
                })
            }
            if ((comments?.size ?: 0) > 0) {
                CommentEndCap()
            }
        }
    }
}

@Composable fun CommentRow(isFirst: Boolean, node: HierarchicalThing, onClick: () -> Unit) {
    val enabled = when (node) {
        is Comment -> (node.collapsedChildren?.size ?: 0) != 0
        is RedditMore -> true
        else -> false
    }
    CommentBody(isFirst = isFirst, depth = node.depth, enabled = enabled, onClick = onClick) {
        when (node) {
            is Comment -> {
                CommentContent(
                    author = node.author,
                    collapseCount = node.collapsedChildren?.size ?: 0,
                    score = node.score,
                    createdUtc = node.createdUtc,
                    body = node.body
                )
            }
            is RedditMore -> {
                LoadMoreCommentsRow(count = node.count)
            }
        }
    }
}

@Composable
fun LinkCard(
    title: String,
    image: String?,
    author: String,
    score: Int,
    createdAt: Long,
    comments: Int
) {
    Column(LayoutPadding(10.dp) + LayoutWidth.Fill) {
        Card(color = Color.White, shape = RoundedCornerShape(10.dp), elevation = 2.dp) {
            Column {
                Column(LayoutPadding(all = 10.dp)) {
                    Text(text = title, style = TextStyle(color = TEXT_DARK, fontSize = 21.sp))
                }

                if (image != null) {
                    // image will at worst be square
                    Image(
                        url = image,
                        aspectRatio = 16f / 9f
                    )
                }

                Row(LayoutPadding(all = 10.dp)) {
                    Text(text = author, style = TextStyle(fontWeight = FontWeight.Bold, color = TEXT_MUTED))
                    if (score != 0) {
                        Bullet()
                        Text(text = "$score", style = TextStyle(color = TEXT_MUTED))
                    }
                    Bullet()
                    TimeAgo(date = createdAt, style = TextStyle(color = TEXT_MUTED))
                }
            }
        }
        Column(LayoutPadding(top = 24.dp)) {
            Text("Comments ($comments)", style = TextStyle(fontSize = 20.sp))
        }
    }
}

@Composable fun CommentContent(
    author: String,
    collapseCount: Int,
    score: Int,
    createdUtc: Long,
    body: String
) {
    CommentAuthorLine(
        author=author,
        collapseCount=collapseCount,
        score=score,
        createdUtc=createdUtc
    )
    Text(text=body)
}

@Composable fun LoadMoreCommentsRow(count: Int) {
    Text("Load $count more comment${if (count == 1) "" else "s"}", style = TextStyle(fontStyle = FontStyle.Italic))
}

@Composable fun CommentEndCap() {
    Column(LayoutPadding(left = 10.dp, right = 10.dp)) {
        Card(color = Color.White, shape = RoundedCornerShape(0.dp, 0.dp, 10.dp, 10.dp), elevation = 0.dp) {
            Column(LayoutPadding(top = 10.dp) + LayoutWidth.Fill) {
            }
        }
    }
}

@Composable fun CommentBody(isFirst: Boolean, depth: Int, enabled: Boolean, onClick: () -> Unit, children: @Composable () -> Unit) {
    if (depth == 0 && !isFirst) {
        CommentEndCap()
    }
    val shape = when (depth) {
        0 -> RoundedCornerShape(10.dp, 10.dp, 0.dp, 0.dp)
        else -> RectangleShape
    }
    val outerLayoutPadding = when (depth) {
        0 -> LayoutPadding(top = 10.dp, left = 10.dp, right = 10.dp)
        else -> LayoutPadding(left = 10.dp, right = 10.dp)
    }
    val innerLayoutPadding = when (depth) {
        0 -> LayoutPadding(left = 10.dp, top = 10.dp, right = 10.dp)
        else -> LayoutPadding(left = 10.dp * (depth + 1), top = 10.dp, right = 10.dp)
    }
    Column(outerLayoutPadding) {
        Card(color = Color.White, shape = shape, elevation = 0.dp) {
            Ripple(
                bounded = false,
                color = Color.Blue,
                enabled = enabled
            ) {
                Clickable(onClick = onClick) {
                    Column(innerLayoutPadding + LayoutWidth.Fill) {
                        children()
                    }
                }
            }
        }
        DrawIndents(depth)
    }
}

val indentsPaint = Paint()

@Composable fun DrawIndents(depth: Int) {
    Draw { canvas, size ->
        val dist = 10.dp.toPx().value

        for (i in 1..depth) {
            indentsPaint.color = Color.DarkGray.copy(alpha = 1f - (i * 10f) / 60f )
            canvas.drawLine(
                Offset(i * dist, if (i == depth) dist else 0f),
                Offset(i * dist, size.height.value),
                indentsPaint
            )
        }
    }
}

val String.color get() = Color(android.graphics.Color.parseColor(this))

object Colors2 {

    // Brand Colors
    val BLACK = "#212122".color
    val DARK_GRAY = "#888888".color
    val ORANGE_RED = "#FF4500".color

    // Semantic Colors
    val TEXT_DARK = BLACK
    val TEXT_MUTED = DARK_GRAY
    val PRIMARY = ORANGE_RED
}

@Composable fun CommentAuthorLine(
    author: String,
    score: Int = 0,
    createdUtc: Long = 0,
    collapseCount: Int = 0
) {
    Row {
        Text(text = author, style = TextStyle(fontWeight = FontWeight.Bold, color = TEXT_MUTED))
        if (score != 0) {
            Bullet()
            Text(text = "$score", style = TextStyle(color = TEXT_MUTED))
        }
        Bullet()
        TimeAgo(date = createdUtc, style = TextStyle(color = TEXT_MUTED))
        // TODO(lmr): do a better job here
        if (collapseCount != 0) {
            Text(text = "$collapseCount")
        }
    }
}

@Composable
fun Bullet() {
    Text(text = " · ", style = TextStyle(color = TEXT_MUTED))
}

