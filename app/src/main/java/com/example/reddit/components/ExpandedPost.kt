package com.example.reddit.components

import androidx.compose.runtime.*
import androidx.core.os.bundleOf
import androidx.compose.animation.*
import androidx.compose.ui.*
import androidx.compose.foundation.*
import androidx.compose.foundation.selection.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.example.reddit.Ambients
import com.example.reddit.R
import com.example.reddit.fadedOnPrimary
import com.example.reddit.fadedPrimary
import com.example.reddit.navigation.optionalNavArg
import kotlin.math.max

@Composable
@Suppress("DEPRECATION")
fun ExpandedPost(id: String, title: String, score: Int, author: String, comments: Int, image: String?, selftext: String?) {
    val voteStatus = state<VoteStatus> { VoteStatus.UNVOTED }
    Post(voteStatus) {
        ColumnPostContent(id, title, score, author, comments, voteStatus, image, selftext)
    }
}

@Composable
private fun ColumnPostContent(
    id: String,
    title: String,
    score: Int,
    author: String,
    comments: Int,
    voteStatus: MutableState<VoteStatus>,
    image: String?,
    selftext: String?
) {
    Column {
        MainPostCard(id = id, title = title, author = author, comments = comments, image = image, selftext = selftext)
        RowScoreSection(score, voteStatus)
    }
}

@Composable
@Suppress("DEPRECATION")
private fun RowScoreSection(score: Int, voteStatus: MutableState<VoteStatus>) {
    Row(Modifier.preferredHeight(40.dp).fillMaxHeight()) {
        val modifier = Modifier.gravity(Alignment.CenterVertically)
        ScoreSection(modifier.weight(1f), voteStatus) {
            // Simulate actual network connection to update the score
            val adjustedScore = score.adjustScore(voteStatus.value)
            Text(
                modifier = modifier.padding(start = 25.dp, end = 25.dp),
                text = if (adjustedScore == 1) "$adjustedScore point" else "$adjustedScore points",
                style = MaterialTheme.typography.h6.copy(color = MaterialTheme.colors.onPrimary)
            )
        }
    }
}

@Composable
private fun MainPostCard(id: String, title: String, author: String, comments: Int, image: String?, selftext: String?) {
    val navigator = Ambients.NavController.current
    Surface(elevation = 2.dp) {
        val currentSubreddit = optionalNavArg<String>("subreddit") ?: "androiddev"
        Box(
            Modifier
                .clickable { navigator.navigate(R.id.post_screen, bundleOf("linkId" to id, "subreddit" to currentSubreddit)) }
                .fillMaxWidth()
                .padding(top = 5.dp, bottom = 5.dp),
            Alignment.TopStart
        ) {
            Column(Modifier.fillMaxWidth()) {
                Box(
                    Modifier.padding(
                        start = 15.dp,
                        end = 15.dp,
                        top = 5.dp,
                        bottom = 5.dp
                    ),
                    Alignment.TopStart
                ) {
                    Text(title, style = MaterialTheme.typography.h6, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (image != null) {
                    Image(
                        modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
                        url = image,
                        aspectRatio = 16f / 9f
                    )
                } else if (selftext != null) {
                    Box(Modifier.padding(15.dp), Alignment.TopStart) {
                        Text(selftext, style = MaterialTheme.typography.body2, maxLines = 10, overflow = TextOverflow.Ellipsis)
                    }
                }
                Box(Modifier.padding(start = 15.dp, end = 15.dp), Alignment.TopStart) {
                    Text(
                        text = "u/$author",
                        style = MaterialTheme.typography.overline.copy(fontStyle = FontStyle.Italic)
                    )
                }
                Box(Modifier.padding(start = 15.dp, end = 15.dp), Alignment.TopStart) {
                    Text(
                        text = "$comments comments",
                        style = MaterialTheme.typography.overline)
                }
            }
        }
    }
}
