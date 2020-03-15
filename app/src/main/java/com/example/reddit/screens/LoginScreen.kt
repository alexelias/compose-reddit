package com.example.reddit.screens

import androidx.compose.Composable
import androidx.compose.state
import androidx.ui.core.Text
import androidx.ui.core.TextField
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.unit.*
import androidx.ui.material.Button
import androidx.ui.material.Divider

@Composable
fun LoginScreen() {
    val username = state { "" }
    val password = state { "" }
    Column(LayoutHeight.Fill + LayoutPadding(16.dp)) {
        val dividerColor = Color(0xFFAAAAAA)

        Text("Username")
        TextField(
            value = username.value,
            onValueChange = { value -> username.value = value}
        )
        Divider(color = dividerColor)
        Spacer()

        Text("Password")
        TextField(
            value = password.value,
            onValueChange = { value -> password.value = value}
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
    Container(height = 16.dp) {}
}
