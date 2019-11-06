package com.example.reddit.components

import androidx.animation.TweenBuilder
import androidx.compose.*
import androidx.core.os.bundleOf
import androidx.ui.animation.animatedColor
import androidx.ui.core.*
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.ColoredRect
import androidx.ui.foundation.selection.Toggleable
import androidx.ui.foundation.shape.DrawShape
import androidx.ui.foundation.shape.RectangleShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.layout.*
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Card
import androidx.ui.material.surface.Surface
import androidx.ui.material.themeColor
import androidx.ui.material.themeTextStyle
import androidx.ui.text.font.FontStyle
import androidx.ui.text.style.TextOverflow
import com.example.reddit.Ambients
import com.example.reddit.R
import com.example.reddit.fadedOnPrimary
import com.example.reddit.fadedPrimary

@Composable
fun ThumbnailPost(id: String, title: String, score: Int, author: String, comments: Int) {
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
    Container(Spacing(10.dp) wraps ExpandedWidth) {
        Card(color = Color.White, shape = RoundedCornerShape(10.dp), elevation = 2.dp) {
            DrawShape(shape = RectangleShape, color = animatedColor.value)
            PostContent(id, title, score, author, comments, voteStatus)
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
    voteStatus: State<Boolean?>
) {
    HackedRow(inflexibleWidthSection = {
        Container(width = 60.dp) {
            ScoreSection(score, voteStatus)
        }
    }, mainCard = {
        Container {
            MainPostCard(id = id, title = title, author = author, comments = comments)
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
    Layout(tempScoreSection, inflexibleWidthSection, mainCard) { measurables, constraints ->

        // Measure score placeable to figure out how much width we have left
        val tempScorePlaceable = measurables[tempScoreSection].first().measure(constraints)

        val availableWidth = constraints.maxWidth - tempScorePlaceable.width

        val postPlaceable = measurables[mainCard].first().measure(
            // Measure with loose constraints for height as we don't want the text to take up more
            // space than it needs
            constraints.withTight(width = availableWidth)
        )

        val scorePlaceable = measurables[inflexibleWidthSection].first()
            .measure(constraints.withTight(height = postPlaceable.height))

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
private fun ScoreSection(score: Int, voteStatus: State<Boolean?>) {
    Column(Expanded, crossAxisAlignment = CrossAxisAlignment.Center) {
        UpVoteArrow(
            Flexible(1f),
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
            if (score > 1) "points" else "point",
            style = (+themeTextStyle { overline }).copy(color = +themeColor { onPrimary })
        )
        HeightSpacer(2.dp)
        DownVoteArrow(
            Flexible(1f),
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
    val vector = +androidx.ui.res.vectorResource(vectorResource)
    Ripple(bounded = true) {
        Toggleable(checked = selected, onCheckedChange = onSelected) {
            Container(modifier wraps ExpandedWidth, alignment = alignment) {
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
private fun MainPostCard(id: String, title: String, author: String, comments: Int) {
    val navigator = +ambient(Ambients.NavController)
    Surface(elevation = 4.dp) {
        Ripple(bounded = true) {
            Clickable({
                navigator.navigate(R.id.post_screen, bundleOf("linkId" to id))
            }) {
                // Extra wrap so clickable wraps the spacing too
                Wrap {
                    Container(
                        modifier = ExpandedWidth wraps Spacing(
                            left = 10.dp,
                            right = 10.dp,
                            top = 5.dp,
                            bottom = 5.dp
                        )
                    ) {
                        HackedRow(inflexibleWidthSection = {
                            // Colored rect is expanded by default
                            Container(Spacing(left = 10.dp)) {
                                ColoredRect(color = Color.Gray, width = 50.dp, height = 50.dp)
                            }
                        }, mainCard = {
                            ConstrainedBox(
                                modifier = ExpandedWidth,
                                constraints = DpConstraints(minHeight = 100.dp)
                            ) {
                                Column {
                                    Text(title, style = +themeTextStyle { subtitle1 }, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                    Column(
                                        Flexible(1f),
                                        mainAxisAlignment = MainAxisAlignment.End
                                    ) {
                                        HeightSpacer(5.dp)
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
                        }, ltr = false)
                    }
                }
            }
        }
    }
}
