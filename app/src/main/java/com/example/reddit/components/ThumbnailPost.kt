package com.example.reddit.components

import androidx.compose.*
import androidx.core.os.bundleOf
import androidx.ui.animation.*
import androidx.ui.core.*
import androidx.ui.foundation.*
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.geometry.*
import androidx.ui.graphics.*
import androidx.ui.graphics.vector.*
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Card
import androidx.ui.material.Surface
import androidx.ui.text.font.FontStyle
import androidx.ui.text.style.TextOverflow
import androidx.ui.unit.*
import com.example.reddit.Ambients
import com.example.reddit.R
import com.example.reddit.fadedOnPrimary
import com.example.reddit.fadedPrimary
import com.example.reddit.navigation.optionalNavArg
import kotlin.math.max

@Composable
fun ThumbnailPost(id: String, title: String, score: Int, author: String, comments: Int, image: String?) {
    val voteStatus = state<VoteStatus> { VoteStatus.UNVOTED }
    Post(voteStatus) {
        RowPostContent(id, title, score, author, comments, voteStatus, image)
    }
}

@Composable
private fun RowPostContent(
    id: String,
    title: String,
    score: Int,
    author: String,
    comments: Int,
    voteStatus: MutableState<VoteStatus>,
    image: String?
) {
    CrossFlexibleRow(inflexibleWidthSection = {
        ColumnScoreSection(score, voteStatus)
    }, mainCard = {
        MainPostCard(id = id, title = title, author = author, comments = comments, image = image)
    }, ltr = true)
}

/**
 * We have a cyclic dependency issue where the [inflexibleWidthSection] has
 * fixed width and flexible height, and the [mainCard] has flexible height and
 * width, but the height of the [mainCard] should determine the total height of
 * the column. So we need to first measure [inflexibleWidthSection], so we can
 * know with what width we should measure [mainCard], which then gives us a
 * total height of the card so we can then go back and measure
 * [inflexibleWidthSection]. There's no way to express this currently / without
 * `ConstraintLayout`, hence the need for this.
 *
 * @param ltr whether the [inflexibleWidthSection] should be on the left or the
 * right of the [mainCard]
 */
@Composable
private fun CrossFlexibleRow(
    inflexibleWidthSection: @Composable () -> Unit,
    mainCard: @Composable () -> Unit,
    ltr: Boolean
) {
    val tempScoreSection = @Composable { inflexibleWidthSection() }
    Layout({
        Box(Modifier.layoutId("tempScoreSection"), children = tempScoreSection)
        Box(Modifier.layoutId("inflexibleWidthSection"), children = inflexibleWidthSection)
        Box(Modifier.layoutId("mainCard"), children = mainCard)
    }) { measurables, constraints ->
        // Measure score placeable to figure out how much width we have left
        val tempScorePlaceable = measurables.first { it.id == "tempScoreSection" }.measure(constraints)

        val availableWidth = constraints.maxWidth - tempScorePlaceable.width

        val postPlaceable = measurables.first { it.id == "mainCard" }.measure(
            // Measure with loose constraints for height as we don't want the text to take up more
            // space than it needs
            constraints.enforce(Constraints.fixedWidth(availableWidth))
        )

        val scorePlaceable = measurables.first { it.id == "inflexibleWidthSection" }
            .measure(constraints.enforce(Constraints.fixedHeight(postPlaceable.height)))

        layout(width = constraints.maxWidth, height = postPlaceable.height) {
            if (ltr) {
                scorePlaceable.place(0, 0)
                postPlaceable.place(scorePlaceable.width, 0)
            } else {
                postPlaceable.place(0, 0)
                scorePlaceable.place(constraints.maxWidth - scorePlaceable.width, 0)
            }
        }
    }
}

@Composable
private fun ColumnScoreSection(score: Int, voteStatus: MutableState<VoteStatus>) {
    Column(Modifier.preferredWidth(60.dp).fillMaxWidth()) {
        val modifier = Modifier.gravity(Alignment.CenterHorizontally)
        ScoreSection(modifier.weight(1f), voteStatus) {
            // Simulate actual network connection to update the score
            val adjustedScore = score.adjustScore(voteStatus.value)
            Spacer(modifier.preferredHeight(2.dp))
            Text(
                modifier = modifier,
                text = "$adjustedScore",
                style = MaterialTheme.typography.h6.copy(color = MaterialTheme.colors.onPrimary)
            )
            Text(
                modifier = modifier,
                text = if (adjustedScore == 1) "point" else "points",
                style = MaterialTheme.typography.overline.copy(color = MaterialTheme.colors.onPrimary)
            )
            Spacer(modifier.preferredHeight(2.dp))
        }
    }
}

@Composable
private fun MainPostCard(id: String, title: String, author: String, comments: Int, image: String?) {
    val navigator = Ambients.NavController.current
    Surface(elevation = 4.dp) {
        val currentSubreddit = optionalNavArg<String>("subreddit") ?: "androiddev"
        Box(
            Modifier
                .clickable { navigator.navigate(R.id.post_screen, bundleOf("linkId" to id, "subreddit" to currentSubreddit)) }
                .fillMaxWidth()
                .padding(start = 10.dp)) {
            CrossFlexibleRow(inflexibleWidthSection = {
                if (image != null) {
                    Image(
                        url = image,
                        width = 90.dp,
                        height = 110.dp
                    )
                } else {
                    Box {}
                }
            }, mainCard = {
                Column(
                    Modifier.fillMaxWidth().preferredHeightIn(minHeight = 100.dp).padding(
                        top = 5.dp,
                        bottom = 5.dp,
                        end = 5.dp
                    )
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.subtitle1
                    )
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.preferredHeight(5.dp))
                    Text(
                        text = "u/$author",
                        style = (MaterialTheme.typography.overline).copy(fontStyle = FontStyle.Italic)
                    )
                    Text(
                        text = "$comments comments",
                        style = MaterialTheme.typography.overline
                    )
                }
            }, ltr = false)
        }
    }
}
