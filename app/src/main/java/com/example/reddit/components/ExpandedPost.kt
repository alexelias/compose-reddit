package com.example.reddit.components

import androidx.animation.TweenBuilder
import androidx.compose.*
import androidx.core.os.bundleOf
import androidx.ui.animation.animatedColor
import androidx.ui.core.Modifier
import androidx.ui.core.Text
import androidx.ui.core.dp
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
fun ExpandedPost(id: String, title: String, score: Int, author: String, comments: Int, image: String?) {
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
    voteStatus: State<Boolean?>,
    image: String?
) {
    Column {
        Container {
            MainPostCard(id = id, title = title, author = author, comments = comments, image = image)
        }

        Container(ExpandedWidth, height = 40.dp) {
            ScoreSection(score, voteStatus)
        }
    }
}

@Composable
private fun ScoreSection(score: Int, voteStatus: State<Boolean?>) {
    Row(Expanded, crossAxisAlignment = CrossAxisAlignment.Center) {
        VoteArrow(
            Flexible(1f),
            R.drawable.ic_baseline_arrow_drop_up_24,
            voteStatus.value == true
        ) { selected ->
            voteStatus.value = if (selected && voteStatus.value != true) true else null
        }
        Text(
            modifier = Spacing(left = 15.dp, right = 15.dp),
            text = "$score " + if (score > 1) "points" else "point",
            style = (+themeTextStyle { h6 }).copy(color = +themeColor { onPrimary })
        )
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
private fun VoteArrow(
    modifier: Modifier,
    vectorResource: Int,
    selected: Boolean,
    onSelected: (Boolean) -> Unit
) {
    val vector = +androidx.ui.res.vectorResource(vectorResource)
    Ripple(bounded = true) {
        Toggleable(checked = selected, onCheckedChange = onSelected) {
            Container(modifier wraps Expanded) {
                val tintColor = +themeColor {
                    if (selected) onPrimary else fadedOnPrimary
                }
                val animatedColor = +animatedColor(tintColor)
                +memo(tintColor) {
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
private fun MainPostCard(id: String, title: String, author: String, comments: Int, image: String?) {
    val navigator = +ambient(Ambients.NavController)
    Surface(elevation = 2.dp) {
        Ripple(bounded = true) {
            Clickable({
                navigator.navigate(R.id.post_screen, bundleOf("linkId" to id))
            }) {
                // Extra wrap so clickable wraps the spacing too
                Wrap {
                    Container(
                        ExpandedWidth wraps Spacing(
                            top = 5.dp,
                            bottom = 5.dp
                        )
                    ) {
                        Column(ExpandedWidth) {
                            Container(
                                Spacing(
                                    left = 15.dp,
                                    right = 15.dp,
                                    top = 5.dp,
                                    bottom = 5.dp
                                )
                            ) {
                                Text(title, style = +themeTextStyle { h6 }, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
//                            Container(
//                                ExpandedWidth wraps Spacing(top = 5.dp, bottom = 5.dp),
//                                height = 150.dp
//                            ) {
//                                ColoredRect(color = Color.Gray, height = 150.dp)
//                            }
                            if (image != null) {
                                Image(
                                    modifier = Spacing(top = 5.dp, bottom = 5.dp),
                                    url = image,
                                    aspectRatio = 16f / 9f
                                )
                            }
                            Container(Spacing(left = 5.dp, right = 5.dp)) {
                                Text(
                                    text = "u/$author",
                                    style = (+themeTextStyle { overline }).copy(fontStyle = FontStyle.Italic)
                                )
                            }
                            Container(Spacing(left = 5.dp, right = 5.dp)) {
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
}
