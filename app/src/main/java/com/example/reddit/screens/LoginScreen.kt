package com.example.reddit.screens

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

        Button(onClick = { /* TODO */ }) {
            Text("Log in (not implemented yet)")
        }
    }
}