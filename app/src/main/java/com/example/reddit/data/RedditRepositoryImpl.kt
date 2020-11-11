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