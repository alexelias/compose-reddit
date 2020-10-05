package com.example.reddit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.state
import androidx.compose.ui.*
import androidx.compose.foundation.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.*
import androidx.compose.material.*

@Composable
@Suppress("DEPRECATION")
fun LoginScreen() {
    val username = state { "" }
    val password = state { "" }
    Column(Modifier.fillMaxHeight().padding(16.dp)) {
        val dividerColor = Color(0xFFAAAAAA)

        TextField(
            value = username.value,
            onValueChange = { value -> username.value = value},
            label = { Text("Username") }
        )
        Divider(color = dividerColor)
        Spacer()

        TextField(
            value = password.value,
            onValueChange = { value -> password.value = value},
            label = { Text("Password") }
        )
        Divider(color = dividerColor)
        Spacer()

        Button(onClick = { /* TODO */ }) {
            Text("Log in")
        }
    }
}

@Composable
private fun Spacer() {
    Spacer(Modifier.preferredHeight(16.dp) )
}
