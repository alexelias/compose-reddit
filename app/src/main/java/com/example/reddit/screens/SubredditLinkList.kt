package com.example.reddit.screens

import androidx.compose.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.ui.core.Text
import androidx.ui.layout.Column
import androidx.ui.layout.Expanded
import com.example.reddit.Ambients
import com.example.reddit.data.AsyncState
import com.example.reddit.data.RedditFilterType

fun <T> subscribe(data: LiveData<T>) = effectOf<T?> {
    val current = +stateFor(data) { data.value }

    +onCommit(data) {
        val observer = Observer<T> {
            current.value = data.value
        }
        data.observeForever(observer)
        onDispose {
            data.removeObserver(observer)
        }
    }

    current.value
}

private val sortOptions = listOf(
    RedditFilterType.HOT,
    RedditFilterType.NEW,
    RedditFilterType.TOP
)

@Composable
fun SubredditLinkList(subreddit: String, pageSize: Int = 10) {
    val selectedSortIndex = +state { 0 }
    val repository = +ambient(Ambients.Repository)
    val model = +modelFor(subreddit, selectedSortIndex.value) {
        repository.linksOfSubreddit(subreddit, sortOptions[selectedSortIndex.value], pageSize)
    }
    val links = +subscribe(model.links)

    val networkState = +subscribe(model.networkState) ?: AsyncState.LOADING

    if (networkState == AsyncState.LOADING || links == null)
        Text("Loading...")
    else {
        Column(Expanded) {
            Text("Done Loading...")
            // do stuff here around PagedList...
            for (item in links.snapshot()) {
                Text("${item.title}")
            }
        }
    }
}