// Copyright 2020 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.reddit.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.reddit.R
import com.example.reddit.fadedOnPrimary
import com.example.reddit.fadedPrimary
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
    val animatedColor = animateColorAsState(cardColor)

    Box(Modifier.padding(10.dp).fillMaxWidth()) {
        Card(backgroundColor = animatedColor.value, shape = RoundedCornerShape(10.dp), elevation = 2.dp) {
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
    val vector = ImageVector.vectorResource(vectorResource)
    val tintColor = animateColorAsState(if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.fadedOnPrimary)
    val painter = rememberVectorPainter(vector)
    Box(
        modifier.toggleable(value = selected, onValueChange = onSelected)
            .size(width = 24.dp, height = 24.dp)
            .paint(painter, colorFilter = ColorFilter.tint(tintColor.value))
    )
}
