package com.example.reddit.screens

import androidx.compose.Composable
import androidx.compose.ambient
import androidx.compose.modelFor
import androidx.compose.unaryPlus
import androidx.ui.core.Draw
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.sp
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.VerticalScroller
import androidx.ui.graphics.Color
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.layout.Spacing
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.material.themeColor
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontStyle
import androidx.ui.text.font.FontWeight
import com.example.reddit.Ambients
import com.example.reddit.components.Image
import com.example.reddit.components.TimeAgo
import com.example.reddit.data.*
import com.example.reddit.screens.Colors2.TEXT_DARK
import com.example.reddit.screens.Colors2.TEXT_MUTED
import kotlin.math.min

private val tabTitles = listOf("Comments", "Link")

@Composable
fun PostScreen(linkId: String, pageSize: Int = 10, initialLink: Link? = null) {
    val repository = +ambient(Ambients.Repository)
    val linkModel = +modelFor(linkId, pageSize) { repository.linkDetails(linkId, pageSize) }

    val link = +subscribe(linkModel.link) ?: initialLink
    val comments = +subscribe(linkModel.comments)
    val networkState = +subscribe(linkModel.networkState)
    VerticalScroller {
        Column {
            if (networkState == AsyncState.LOADING)
                Text("Loading...")

            if (link != null) {
                LinkCard(link = link)
            }

            if (comments != null) {
                Column {
                    for (node in comments) {
                        CommentRow(node, onClick = {
                            when (node) {
                                is RedditMore -> linkModel.loadMore(node)
                                is Comment -> linkModel.toggleCollapsedState(node)
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable fun LinkCard(link: Link, onClick: () -> Unit = {}) {
    Column {
        val image = link.preview?.images?.firstOrNull()
        if (image != null) {
            // image will at worst be square
            Image(
                url = image.source.decodedUrl,
                aspectRatio = min(1f, image.source.height.toFloat() / image.source.width.toFloat())
            )
        }

        // TODO: number of comments, subreddit, domain, etc.

        Column {
            Text(text = link.title, style = TextStyle(color = TEXT_DARK, fontSize = 21.sp))
            Row {
                Text(text = link.author, style = TextStyle(fontWeight = FontWeight.Bold, color = TEXT_MUTED))
                if (link.score != 0) {
                    Bullet()
                    Text(text = "${link.score}", style = TextStyle(color = TEXT_MUTED))
                }
                Bullet()
                TimeAgo(date = link.createdUtc, style = TextStyle(color = TEXT_MUTED))
            }
        }
    }
}

@Composable fun CommentRow(
    node: HierarchicalThing,
    onClick: () -> Unit
) {
    val depth = node.depth
    Column() {

    Row(Spacing(left = 8.dp * depth)) {
        ClickableContainer(onClick = onClick) {
            Column {
                HorizontalDivider()
                when (node) {
                    is Comment -> {
                        CommentAuthorLine(
                            author=node.author,
                            collapseCount=node.collapsedChildren?.size ?: 0,
                            score=node.score,
                            createdUtc=node.createdUtc
                        )
                        Text(text=node.body)
                    }
                    is RedditMore -> {
                        Text(
                            text="Load ${node.children.size} more comments",
                            style = TextStyle(fontStyle = FontStyle.Italic)
                        )
                    }
                    else -> error("unrecognized node type!")
                }
            }
        }
    }
    }
}

@Composable fun ClickableContainer(enabled: Boolean = true, onClick: () -> Unit, children: @Composable () -> Unit) {
    Surface(
//        color = color,
//        border = null,
//        elevation = 0.dp
    ) {
        Ripple(
            bounded = true,
            color = +themeColor { primary },
            enabled = enabled
        ) {
            Clickable(onClick = onClick) {
                children()
            }
        }
    }
}



object Colors2 {
    private val String.color get() = Color(android.graphics.Color.parseColor(this))

    // Brand Colors
    val WHITE = "#FFFFFF".color
    val BLACK = "#212122".color
    val LIGHT_GRAY = "#dadada".color
    val DARK_GRAY = "#888888".color
    val ORANGE_RED = "#FF4500".color
    val MINT = "#0DD3BB".color
    val BLUE = "#24A0ED".color
    val ALIEN_BLUE = "#0079D3".color
    val TEAL = "#00A6A5".color
    val ORANGE = "#FF8717".color
    val MANGO = "#FFB000".color
    val YELLOW = "#FFCA00".color

    // Semantic Colors
    val TEXT_LIGHT = WHITE
    val TEXT_DARK = BLACK
    val TEXT_MUTED = DARK_GRAY
    val PRIMARY = ORANGE_RED
    val SECONDARY = MINT
    val DIVIDER = LIGHT_GRAY
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
        if (collapseCount != 0) {
            Text(text = "$collapseCount")
        }
    }
}

@Composable
fun HorizontalDivider() {
    Row {
        Draw { canvas, parentSize ->
// TODO:
//            canvas.drawLine(Offset.zero)
        }
    }
}

@Composable
fun Bullet() {
    Text(text = " · ", style = TextStyle(color = TEXT_MUTED))
}

