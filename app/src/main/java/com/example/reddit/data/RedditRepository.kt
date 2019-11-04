package com.example.reddit.data

interface RedditRepository {
    fun linksOfSubreddit(subreddit: String, filter: RedditFilterType, pageSize: Int): SubredditModel
    fun linkDetails(linkId: String, pageSize: Int): LinkModel
}


class RedditUser {

}

typealias Callback<T> = (error: Throwable?, value: T?) -> Unit


interface AuthenticationService {
    var currentUser: RedditUser?
    var isLoggedIn: Boolean
    fun reauthenticate()
    fun login(username: String, password: String, callback: Callback<RedditUser>)
    fun signup(username: String, password: String, callback: Callback<RedditUser>)
}