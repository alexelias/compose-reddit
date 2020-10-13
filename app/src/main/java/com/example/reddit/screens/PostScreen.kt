package com.example.reddit.screens

import androidx.compose.animation.animatedFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.ProvideEmphasis
import androidx.compose.runtime.Composable
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reddit.Ambients
import com.example.reddit.components.Image
import com.example.reddit.components.TimeAgo
import com.example.reddit.data.*

@Composable
fun PostScreen(linkId: String, pageSize: Int = 10, initialLink: Link? = null) {
    val repository = Ambients.Repository.current
    val linkModel = remember(linkId, pageSize) { repository.linkDetails(linkId, pageSize) }

    val link = subscribe(linkModel.link) ?: initialLink
    val comments = subscribe(linkModel.comments)
    val networkState = subscribe(linkModel.networkState)

    val isLoading = networkState == AsyncState.LOADING
    Box(Modifier.fillMaxSize()) {
        // Controls fade out of the progress spinner
        val opacity = animatedFloat(1f)

        onCommit(isLoading) {
            if (isLoading) {
                if (opacity.value != 1f) {
                    opacity.snapTo(1f)
                }
            } else {
                opacity.animateTo(0f)
            }
        }

        if (opacity.value == 0f) {
            ScrollingContent(link, linkModel, comments)
        } else {
            LoadingIndicator(opacity.value)
        }
    }
}

@Composable
@OptIn(ExperimentalLazyDsl::class)
fun ScrollingContent(link: Link?, linkModel: LinkModel, comments: List<HierarchicalThing>?) {
    val headerComposable: @Composable () -> Unit = {
        link?.run {
            LinkCard(
                title = title,
                image = preview?.imageUrl,
                author = author,
                score = score,
                createdAt = createdUtc,
                comments = numComments
            )
        }
    }

    if (comments == null || comments.size == 0) {
        Column(Modifier.fillMaxHeight()) {
            headerComposable()
        }
        return
   }

    LazyColumn(Modifier.fillMaxHeight()) {
        item {
            headerComposable()
        }
        itemsIndexed(comments) { index, node ->
            CommentRow(isFirst = (index == 0), node = node, onClick = {
                when (node) {
                    is RedditMore -> linkModel.loadMore(node)
                    is Comment -> linkModel.toggleCollapsedState(node)
                }
            })
        }
        item {
            CommentEndCap()
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
    Column(Modifier.padding(10.dp).fillMaxWidth()) {
        Card(shape = RoundedCornerShape(10.dp), elevation = 2.dp) {
            Column {
                Column(Modifier.padding(all = 10.dp)) {
                    Text(text = title, style = TextStyle(fontSize = 21.sp))
                }

                if (image != null) {
                    // image will at worst be square
                    Image(
                        url = image,
                        aspectRatio = 16f / 9f
                    )
                }

                Row(Modifier.padding(all = 10.dp)) {
                    ProvideEmphasis(EmphasisAmbient.current.medium) {
                        Text(text = author, style = TextStyle(fontWeight = FontWeight.Bold))
                        if (score != 0) {
                            Bullet()
                            Text(text = "$score")
                        }
                        Bullet()
                        TimeAgo(date = createdAt)
                    }
                }
            }
        }
        Column(Modifier.padding(top = 24.dp)) {
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
    Column(Modifier.padding(start = 10.dp, end = 10.dp)) {
        Card(shape = RoundedCornerShape(0.dp, 0.dp, 10.dp, 10.dp), elevation = 0.dp) {
            Column(Modifier.padding(top = 10.dp).fillMaxWidth()) {
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
    val outerpadding = when (depth) {
        0 -> Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp)
        else -> Modifier.padding(start = 10.dp, end = 10.dp)
    }
    val innerpadding = when (depth) {
        0 -> Modifier.padding(start = 10.dp, top = 10.dp, end = 10.dp)
        else -> Modifier.padding(start = 10.dp * (depth + 1), top = 10.dp, end = 10.dp)
    }
    Column(outerpadding) {
        Card(shape = shape, elevation = 2.dp) {
            Column(innerpadding.clickable(enabled = enabled, onClick = onClick).fillMaxWidth()) {
                children()
            }
        }
        DrawIndents(depth)
    }
}

@Composable
fun DrawIndents(depth: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val dist = 10.dp.toPx()

        for (i in 1..depth) {
            drawLine(
                color = Color.DarkGray.copy(alpha = 1f - (i * 10f) / 60f),
                start = Offset(i * dist, if (i == depth) dist else 0f),
                end = Offset(i * dist, size.height),
            )
        }
    }
}

val String.color get() = Color(android.graphics.Color.parseColor(this))

@Composable fun CommentAuthorLine(
    author: String,
    score: Int = 0,
    createdUtc: Long = 0,
    collapseCount: Int = 0
) {
    Row {
        ProvideEmphasis(EmphasisAmbient.current.medium) {
            Text(text = author, style = TextStyle(fontWeight = FontWeight.Bold))
            if (score != 0) {
                Bullet()
                Text(text = "$score")
            }
            Bullet()
            TimeAgo(date = createdUtc)
            // TODO(lmr): do a better job here
            if (collapseCount != 0) {
                Text(text = "$collapseCount")
            }
        }
    }
}

@Composable
fun Bullet() {
    Text(text = " · ")
}

