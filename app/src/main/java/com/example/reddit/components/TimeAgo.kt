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

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.text.TextStyle
import java.util.*
import kotlin.math.roundToInt

private object CurrentTime {
    private val frequencyInMs: Long = 3000
    private val handler = Handler(Looper.getMainLooper())
    private var update by mutableStateOf<Runnable?>(null)
    fun subscribeIfNeeded() {
        if (update == null) {
            // NOTE(lmr): in real app, you might want a smarter strategy here. You could have a
            // semaphore for understanding if someone is currently using it. Additionally, you might
            // want to have exponential backoff or something. Lots of options. This is reasonable for
            // now though.
            update = Runnable {
                now = Date().time / 1000
                handler.postDelayed(update!!, frequencyInMs)
            }
            handler.postDelayed(update!!, frequencyInMs)
        }
    }
    var now by mutableStateOf<Long>(Date().time / 1000)
}

private fun timeAgoText(diff: Long): String {
    if (diff < 0) {
        return "now"
    }

    val seconds = diff.toDouble()
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val years = days / 365

    return when {
        seconds < 45 -> "now"
        minutes < 45 -> "${minutes.roundToInt()}m ago"
        hours < 24 -> "${hours.roundToInt()}h ago"
        hours < 42 -> "1d ago"
        days < 30 -> "${days.roundToInt()}d ago"
        days < 365 -> "${(days / 30).roundToInt()}mn ago"
        else -> "${years.roundToInt()}y ago"
    }
}

@Composable
fun TimeAgo(date: Long) {
    onCommit {
       CurrentTime.subscribeIfNeeded()
    }
    BasicText(timeAgoText(CurrentTime.now - date))
}