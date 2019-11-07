package com.example.reddit.screens

import androidx.compose.Composable
import androidx.compose.ambient
import androidx.compose.modelFor
import androidx.compose.unaryPlus
import androidx.ui.core.*
import androidx.ui.engine.geometry.Offset
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
import com.example.reddit.Ambients
import com.example.reddit.components.Image
import com.example.reddit.components.TimeAgo
import com.example.reddit.data.*
import com.example.reddit.screens.Colors2.TEXT_DARK
import com.example.reddit.screens.Colors2.TEXT_MUTED

@Composable
fun PostScreen(linkId: String, pageSize: Int = 10, initialLink: Link? = null) {
    val repository = +ambient(Ambients.Repository)
    val linkModel = +modelFor(linkId, pageSize) { repository.linkDetails(linkId, pageSize) }

    val link = +subscribe(linkModel.link) ?: initialLink
    val comments = +subscribe(linkModel.comments)
    val networkState = +subscribe(linkModel.networkState)
    VerticalScroller {
        Column(Expanded, mainAxisAlignment = MainAxisAlignment.Start) {
            if (networkState == AsyncState.LOADING)
                LoadingIndicator()

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
    Column(Spacing(10.dp) wraps ExpandedWidth) {
        Card(color = Color.White, shape = RoundedCornerShape(10.dp), elevation = 2.dp) {
            Column {
                Column(Spacing(all = 10.dp)) {
                    Text(text = title, style = TextStyle(color = TEXT_DARK, fontSize = 21.sp))
                }

                if (image != null) {
                    // image will at worst be square
                    Image(
                        url = image,
                        aspectRatio = 16f / 9f
                    )
                }

                Row(Spacing(all = 10.dp)) {
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
        Column(Spacing(top = 24.dp)) {
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
    Column(Spacing(left = 10.dp, right = 10.dp)) {
        Card(color = Color.White, shape = RoundedCornerShape(0.dp, 0.dp, 10.dp, 10.dp), elevation = 0.dp) {
            Column(Spacing(top = 10.dp) wraps ExpandedWidth) {
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
    val outerSpacing = when (depth) {
        0 -> Spacing(top = 10.dp, left = 10.dp, right = 10.dp)
        else -> Spacing(left = 10.dp, right = 10.dp)
    }
    val innerSpacing = when (depth) {
        0 -> Spacing(left = 10.dp, top = 10.dp, right = 10.dp)
        else -> Spacing(left = 10.dp * (depth + 1), top = 10.dp, right = 10.dp)
    }
    Column(outerSpacing) {
        Card(color = Color.White, shape = shape, elevation = 0.dp) {
            Ripple(
                bounded = false,
                color = Color.Blue,
                enabled = enabled
            ) {
                Clickable(onClick = onClick) {
                    Column(innerSpacing wraps ExpandedWidth) {
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

