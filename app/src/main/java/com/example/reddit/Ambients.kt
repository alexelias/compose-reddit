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

package com.example.reddit

import androidx.compose.runtime.ambientOf
import androidx.navigation.NavHostController
import com.example.reddit.api.RedditApi
import com.example.reddit.data.AuthenticationService
import com.example.reddit.data.RedditRepository

object Ambients {
    val Repository = ambientOf<RedditRepository>()
    val Api = ambientOf<RedditApi>()
    val AuthenticationService = ambientOf<AuthenticationService>()
    val NavController = ambientOf<NavHostController>()
}