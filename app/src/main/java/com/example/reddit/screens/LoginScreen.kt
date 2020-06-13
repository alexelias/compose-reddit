package com.example.reddit.screens

import androidx.compose.Composable
import androidx.compose.state
import androidx.ui.core.*
import androidx.ui.foundation.*
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.unit.*
import androidx.ui.material.*

@Composable
fun LoginScreen() {
    val username = state { "" }
    val password = state { "" }
    Column(Modifier.fillMaxHeight().padding(16.dp)) {
        val dividerColor = Color(0xFFAAAAAA)

        FilledTextField(
            value = username.value,
            onValueChange = { value -> username.value = value},
            label = { Text("Username") }
        )
        Divider(color = dividerColor)
        Spacer()

        FilledTextField(
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
    Box(Modifier.preferredHeight(16.dp)) {}
}
