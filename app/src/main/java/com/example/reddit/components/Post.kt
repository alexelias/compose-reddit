package com.example.reddit.components

import androidx.compose.*
import androidx.core.os.bundleOf
import androidx.ui.animation.*
import androidx.ui.core.*
import androidx.ui.foundation.*
import androidx.ui.foundation.selection.*
import androidx.ui.foundation.shape.corner.*
import androidx.ui.graphics.*
import androidx.ui.graphics.vector.*
import androidx.ui.layout.*
import androidx.ui.material.Card
import androidx.ui.material.Surface
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

enum class VoteStatus {
    UP,
    DOWN,
    UNVOTED,
}

fun Int.adjustScore(voteStatus: VoteStatus): Int {
    val score = this
    return when (voteStatus) {
        VoteStatus.UNVOTED -> score
        VoteStatus.UP -> score + 1
        VoteStatus.DOWN -> max(score - 1, 0)
    }
}

@Composable
fun Post(voteStatus: MutableState<VoteStatus>, children: @Composable () -> Unit) {
    val upvoteColor = Color(0xFFFF8B60)
    val downvoteColor = Color(0xFF9494FF)
    val fadedPrimary = MaterialTheme.colors.fadedPrimary.compositeOver(MaterialTheme.colors.surface)
    val cardColor = when (voteStatus.value) {
        VoteStatus.UNVOTED -> fadedPrimary
        VoteStatus.UP -> upvoteColor
        VoteStatus.DOWN -> downvoteColor
    }
    val animatedColor = animate(cardColor)

    Box(Modifier.padding(10.dp).fillMaxWidth()) {
        Card(color = animatedColor, shape = RoundedCornerShape(10.dp), elevation = 2.dp) {
            children()
        }
    }
}

@Composable
fun ScoreSection(
    modifier: Modifier,
    voteStatus: MutableState<VoteStatus>,
    textSection: @Composable () -> Unit
) {
    // Return voteStatus to unvoted-on if the same arrow is clicked a second time
    val onSelected = { targetValue: VoteStatus ->
        { selected: Boolean ->
            if (selected && voteStatus.value != targetValue) {
                voteStatus.value = targetValue
            } else {
                voteStatus.value = VoteStatus.UNVOTED
            }
        }
    }
    VoteArrow(
        modifier,
        R.drawable.ic_baseline_arrow_drop_up_24,
        voteStatus.value == VoteStatus.UP,
        onSelected(VoteStatus.UP)
    )
    textSection()
    VoteArrow(
        modifier,
        R.drawable.ic_baseline_arrow_drop_down_24,
        voteStatus.value == VoteStatus.DOWN,
        onSelected(VoteStatus.DOWN)
    )
}

@Composable
private fun VoteArrow(
    modifier: Modifier,
    vectorResource: Int,
    selected: Boolean,
    onSelected: (Boolean) -> Unit
) {
    val vector = androidx.ui.res.vectorResource(vectorResource)
    Toggleable(value = selected, onValueChange = onSelected) {
        val tintColor = animate(if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.fadedOnPrimary)
        Box(modifier.preferredSize(width = 24.dp, height = 24.dp).paint(VectorPainter(vector),
            colorFilter = ColorFilter.tint(tintColor))) {}
    }
}
