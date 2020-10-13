package com.example.reddit.screens

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen() {
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
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
