package com.example.reddit.components

import android.os.Handler
import android.os.Looper
import androidx.compose.*
import androidx.ui.foundation.Text
import androidx.ui.text.TextStyle
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
    Text(timeAgoText(CurrentTime.now - date))
}