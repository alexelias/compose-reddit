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

package com.example.reddit.screens

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonConstants
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(Modifier.fillMaxHeight().padding(16.dp)) {
         TextField(
            value = username,
            onValueChange = { value -> username = value },
            label = { Text("Username") }
        )
        Spacer(Modifier.preferredHeight(16.dp))

        TextField(
            value = password,
            onValueChange = { value -> password = value},
            label = { Text("Password") }
        )
        Spacer(Modifier.preferredHeight(16.dp))

        Button(colors = ButtonConstants.defaultButtonColors(
            backgroundColor = MaterialTheme.colors.secondary),
            onClick = { /* TODO */ }) {
            Text("Log in (not implemented yet)")
        }
    }
}