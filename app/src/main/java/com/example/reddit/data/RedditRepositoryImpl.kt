package com.example.reddit.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import com.example.reddit.api.RedditApi
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

class RedditRepositoryImpl(
    private val redditApi: RedditApi,
    private val networkExecutor: Executor) : RedditRepository {

    override fun linksOfSubreddit(subreddit: String, filter: RedditFilterType, pageSize: Int): SubredditModel {
        val factory = SubRedditDataSourceFactory(redditApi, subreddit, filter, networkExecutor)

        val mutableInfo = MutableLiveData<SubredditInformation>()
        redditApi.getSubredditInformation(subreddit).enqueue(
            object : retrofit2.Callback<SubredditResponse> {
                override fun onFailure(call: Call<SubredditResponse>, t: Throwable) {
                    print("fail")
                }

                override fun onResponse(
                    call: Call<SubredditResponse>,
                    response: Response<SubredditResponse>
                ) {
                    if (response.isSuccessful) {
                        mutableInfo.postValue(response.body()?.data)
                    }
                }
            }
        )

        val liveLinks = LivePagedListBuilder(factory, pageSize)
            // provide custom executor for network requests, otherwise it will default to
            // Arch Components' IO pool which is also used for disk access
            .setFetchExecutor(networkExecutor)
            .build()

        return SubredditModel(
            info = mutableInfo,
            links = liveLinks,
            networkState = Transformations.switchMap(factory.liveSource) { it.networkState },
            refreshState = Transformations.switchMap(factory.liveSource) { it.initialLoad },
            retry = {
                factory.liveSource.value?.retryAllFailed()
            },
            refresh = {
                factory.liveSource.value?.invalidate()
            }
        )
    }

    override fun linkDetails(linkId: String, pageSize: Int): LinkModel {
        return LinkModel(
            api = redditApi,
            executor = networkExecutor,
            linkId = linkId,
            pageSize = pageSize
        )
    }
}