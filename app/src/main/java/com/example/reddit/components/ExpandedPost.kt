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
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Card
import androidx.ui.material.surface.Surface
import androidx.ui.material.MaterialTheme
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
fun ExpandedPost(id: String, title: String, score: Int, author: String, comments: Int, image: String?, selftext: String?) {
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

    remember(cardColor) {
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
            PostContent(id, title, score, author, comments, voteStatus, image, selftext)
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
    image: String?,
    selftext: String?
) {
    Column {
        Container {
            MainPostCard(id = id, title = title, author = author, comments = comments, image = image, selftext = selftext)
        }

        Container(LayoutWidth.Fill, height = 40.dp) {
            // Clip the unbounded ripples for the vote icons
            Clip(RectangleShape) {
                ScoreSection(score, voteStatus)
            }
        }
    }
}

@Composable
private fun ScoreSection(score: Int, voteStatus: MutableState<Boolean?>) {
    Row(LayoutWidth.Fill + LayoutAlign.Center) {
        VoteArrow(
            LayoutFlexible(1f),
            R.drawable.ic_baseline_arrow_drop_up_24,
            voteStatus.value == true
        ) { selected ->
            voteStatus.value = if (selected && voteStatus.value != true) true else null
        }
        // Simulate actual network connection to update the score
        val adjustedScore = when(voteStatus.value) {
            null -> score
            true -> score + 1
            false -> max(score - 1, 0)
        }
        val adjustedScoreText = if (adjustedScore == 1) {
            "$adjustedScore point"
        } else {
            "$adjustedScore points"
        }
        Text(
            modifier = LayoutPadding(left = 25.dp, right = 25.dp) + LayoutGravity.Center,
            text = adjustedScoreText,
            style = MaterialTheme.typography().h6.copy(color = MaterialTheme.colors().onPrimary)
        )
        VoteArrow(
            LayoutFlexible(1f),
            R.drawable.ic_baseline_arrow_drop_down_24,
            voteStatus.value == false
        ) { selected ->
            voteStatus.value = if (selected && voteStatus.value != false) false else null
        }
    }
}


@Composable
private fun VoteArrow(
    modifier: Modifier,
    vectorResource: Int,
    selected: Boolean,
    onSelected: (Boolean) -> Unit
) {
    val vector = androidx.ui.res.vectorResource(vectorResource)
    Ripple(bounded = false) {
        Toggleable(value = selected, onValueChange = onSelected) {
            Container(modifier + LayoutSize.Fill) {
                val tintColor = if (selected) MaterialTheme.colors().onPrimary
                    else MaterialTheme.colors().fadedOnPrimary
                val animatedColor = animatedColor(tintColor)
                remember(tintColor) {
                    animatedColor.animateTo(
                        tintColor,
                        anim = TweenBuilder<Color>().apply { duration = 200 })
                }
                Container(width = 24.dp, height = 24.dp) {
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
private fun MainPostCard(id: String, title: String, author: String, comments: Int, image: String?, selftext: String?) {
    val navigator = ambient(Ambients.NavController)
    Surface(elevation = 2.dp) {
        Ripple(bounded = true) {
            val currentSubreddit = optionalNavArg<String>("subreddit") ?: "androiddev"
            Clickable({
                navigator.navigate(R.id.post_screen, bundleOf("linkId" to id, "subreddit" to currentSubreddit))
            }) {
                // Extra wrap so clickable wraps the spacing too
                Wrap {
                    Container(
                        LayoutWidth.Fill + LayoutPadding(
                            top = 5.dp,
                            bottom = 5.dp
                        )
                    ) {
                        Column(LayoutWidth.Fill) {
                            Container(
                                LayoutPadding(
                                    left = 15.dp,
                                    right = 15.dp,
                                    top = 5.dp,
                                    bottom = 5.dp
                                )
                            ) {
                                Text(title, style = MaterialTheme.typography().h6, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                            if (image != null) {
                                Image(
                                    modifier = LayoutPadding(top = 5.dp, bottom = 5.dp),
                                    url = image,
                                    aspectRatio = 16f / 9f
                                )
                            } else if (selftext != null) {
                                Container(LayoutPadding(15.dp)) {
                                    Text(selftext, style = MaterialTheme.typography().body2, maxLines = 10, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Container(LayoutPadding(left = 15.dp, right = 15.dp)) {
                                Text(
                                    text = "u/$author",
                                    style = MaterialTheme.typography().overline.copy(fontStyle = FontStyle.Italic)
                                )
                            }
                            Container(LayoutPadding(left = 15.dp, right = 15.dp)) {
                                Text(
                                    text = "$comments comments",
                                    style = MaterialTheme.typography().overline)
                            }
                        }
                    }
                }
            }
        }
    }
}
