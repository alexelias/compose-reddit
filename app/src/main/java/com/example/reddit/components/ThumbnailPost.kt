package com.example.reddit.components

import androidx.animation.TweenBuilder
import androidx.compose.*
import androidx.core.os.bundleOf
import androidx.ui.animation.animatedColor
import androidx.ui.core.*
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.selection.Toggleable
import androidx.ui.foundation.shape.DrawShape
import androidx.ui.foundation.shape.RectangleShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Card
import androidx.ui.material.surface.Surface
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
    val voteStatus = state<Boolean?> { null }
    val upvoteColor = Color(0xFFFF8B60)
    val downvoteColor = Color(0xFF9494FF)
    val fadedPrimary = MaterialTheme.colors().fadedPrimary
    val cardColor = when (voteStatus.value) {
        null -> fadedPrimary
        true -> upvoteColor
        false -> downvoteColor
    }

    val animatedColor = animatedColor(cardColor)

    onCommit(cardColor) {
        animatedColor.animateTo(
            cardColor,
            anim = TweenBuilder<Color>().apply { duration = 200 })
    }

    //TODO: Shouldn't have hardcoded values for height, but no idea how to make it
    // work so that whatever item is taller decides the height of this row, and then
    // the individual parts can be flexible within that overall space.
    Container(LayoutPadding(10.dp) + LayoutWidth.Fill) {
        Card(color = Color.White, shape = RoundedCornerShape(10.dp), elevation = 2.dp) {
            DrawShape(shape = RectangleShape, color = animatedColor.value)
            PostContent(id, title, score, author, comments, voteStatus, image)
        }
    }
}

@Composable
private fun PostContent(
    id: String,
    title: String,
    score: Int,
    author: String,
    comments: Int,
    voteStatus: MutableState<Boolean?>,
    image: String?
) {
    HackedRow(inflexibleWidthSection = {
        Container(width = 60.dp) {
            ScoreSection(score, voteStatus)
        }
    }, mainCard = {
        Container {
            MainPostCard(id = id, title = title, author = author, comments = comments, image = image)
        }
    }, ltr = true)
}

/**
 * We have a cyclic dependency issue where the [inflexibleWidthSection] has fixed width and flexible height,
 * and the [mainCard] has flexible height and width, but the height of the [mainCard] should
 * determine the total height of the column. So we need to first measure [inflexibleWidthSection], so we can
 * know with what width we should measure [mainCard], which then gives us a total height of the card
 * so we can then go back and measure [inflexibleWidthSection]. There's no way to express this currently /
 * without `ConstraintLayout`, hence the need for this.
 *
 * @param ltr whether the [inflexibleWidthSection] should be on the left or the right of the [mainCard]
 */
@Composable
private fun HackedRow(
    inflexibleWidthSection: @Composable () -> Unit,
    mainCard: @Composable () -> Unit,
    ltr: Boolean
) {
    val tempScoreSection = @Composable { inflexibleWidthSection() }
    Layout({
        ParentData(object : LayoutTagParentData { override val tag: Any = "tempScoreSection" }, tempScoreSection)
        ParentData(object : LayoutTagParentData { override val tag: Any = "inflexibleWidthSection" }, inflexibleWidthSection)
        ParentData(object : LayoutTagParentData { override val tag: Any = "mainCard" }, mainCard)
    }) { measurables, constraints ->
        // Measure score placeable to figure out how much width we have left
        val tempScorePlaceable = measurables.first { it.tag == "tempScoreSection" }.measure(constraints)

        val availableWidth = constraints.maxWidth - tempScorePlaceable.width

        val postPlaceable = measurables.first { it.tag == "mainCard" }.measure(
            // Measure with loose constraints for height as we don't want the text to take up more
            // space than it needs
            constraints.enforce(Constraints.fixedWidth(availableWidth))
        )

        val scorePlaceable = measurables.first { it.tag == "inflexibleWidthSection" }
            .measure(constraints.enforce(Constraints.fixedHeight(postPlaceable.height)))

        layout(width = constraints.maxWidth, height = postPlaceable.height) {
            if (ltr) {
                scorePlaceable.place(IntPx.Zero, IntPx.Zero)
                postPlaceable.place(scorePlaceable.width, IntPx.Zero)
            } else {
                postPlaceable.place(IntPx.Zero, IntPx.Zero)
                scorePlaceable.place(constraints.maxWidth - scorePlaceable.width, IntPx.Zero)
            }
        }
    }
}

@Composable
private fun ScoreSection(score: Int, voteStatus: MutableState<Boolean?>) {
    Column {
        UpVoteArrow(
            LayoutFlexible(1f),
            voteStatus.value == true
        ) { selected ->
            voteStatus.value = if (selected && voteStatus.value != true) true else null
        }
        Spacer(LayoutHeight(2.dp))
        // Simulate actual network connection to update the score
        val adjustedScore = when(voteStatus.value) {
            null -> score
            true -> score + 1
            false -> max(score - 1, 0)
        }
        Text(
            text = "$adjustedScore",
            modifier = LayoutGravity.Center,
            style = MaterialTheme.typography().h6.copy(color = MaterialTheme.colors().onPrimary)
        )
        Text(
            if (adjustedScore == 1) "point" else "points",
            modifier = LayoutGravity.Center,
            style = MaterialTheme.typography().overline.copy(color = MaterialTheme.colors().onPrimary)
        )
        Spacer(LayoutHeight(2.dp))
        DownVoteArrow(
            LayoutFlexible(1f),
            voteStatus.value == false
        ) { selected ->
            voteStatus.value = if (selected && voteStatus.value != false) false else null
        }
    }
}

@Composable
private fun UpVoteArrow(
    modifier: Modifier,
    selected: Boolean,
    onSelected: (Boolean) -> Unit
) {
    VoteArrow(
        modifier,
        Alignment.TopCenter,
        R.drawable.ic_baseline_arrow_drop_up_24,
        selected,
        onSelected
    )
}

@Composable
private fun DownVoteArrow(
    modifier: Modifier,
    selected: Boolean,
    onSelected: (Boolean) -> Unit
) {
    VoteArrow(
        modifier,
        Alignment.BottomCenter,
        R.drawable.ic_baseline_arrow_drop_down_24,
        selected,
        onSelected
    )
}

@Composable
private fun VoteArrow(
    modifier: Modifier,
    alignment: Alignment,
    vectorResource: Int,
    selected: Boolean,
    onSelected: (Boolean) -> Unit
) {
    val vector = androidx.ui.res.vectorResource(vectorResource)
    Ripple(bounded = false, radius = 30.dp) {
        Toggleable(value = selected, onValueChange = onSelected) {
            Container(modifier + LayoutWidth.Fill, alignment = alignment) {
                Container(width = 24.dp, height = 24.dp) {
                    val tintColor = if (selected) MaterialTheme.colors().onPrimary else MaterialTheme.colors().fadedOnPrimary
                    val animatedColor = animatedColor(tintColor)
                    remember(tintColor) {
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
private fun MainPostCard(id: String, title: String, author: String, comments: Int, image: String?) {
    val navigator = ambient(Ambients.NavController)
    Surface(elevation = 4.dp) {
        Ripple(bounded = true) {
            val currentSubreddit = optionalNavArg<String>("subreddit") ?: "androiddev"
            Clickable({
                navigator.navigate(R.id.post_screen, bundleOf("linkId" to id, "subreddit" to currentSubreddit))
            }) {
                // Extra wrap so clickable wraps the spacing too
                Wrap {
                    Container(modifier = LayoutWidth.Fill + LayoutPadding(left = 10.dp)) {
                        HackedRow(inflexibleWidthSection = {
                            if (image != null) {
                                Image(
                                    url = image,
                                    width = 90.dp,
                                    height = 110.dp
                                )
                            } else {
                                Container { }
                            }
                        }, mainCard = {
                            Column(LayoutWidth.Fill + LayoutHeight.Min(100.dp) + LayoutPadding(top = 5.dp, bottom = 5.dp, right = 5.dp)) {
                                Text(title, style = MaterialTheme.typography().subtitle1, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                Spacer(LayoutFlexible(1f))
                                Spacer(LayoutHeight(5.dp))
                                Text(
                                    text = "u/$author",
                                    style = (MaterialTheme.typography().overline).copy(fontStyle = FontStyle.Italic)
                                )
                                Text(
                                    text = "$comments comments",
                                    style = MaterialTheme.typography().overline)
                            }
                        }, ltr = false)
                    }
                }
            }
        }
    }
}
