package com.example.reddit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
//import androidx.compose.ui.gestures.ExperimentalPointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.paging.PagedList
import com.example.reddit.data.Link
import com.example.reddit.screens.imageUrl

@Composable
fun ImageGrid(links: PagedList<Link>, header: @Composable () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxHeight().background(Color.Black)) {
        item {
            header()
        }
        items(links.filter { it.preview?.imageUrl != null }.chunked(4)) { row ->
            Row(Modifier.fillMaxWidth().requiredHeight(100.dp)) {
                for (l in row) {
                    CoilImage(
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        data = l.preview!!.imageUrl!!,
//                            contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }

}

/*
//@OptIn(ExperimentalPointerInput::class)
@Composable
fun DetectMultitouchGestures() {
    var angle by remember { mutableStateOf(0f) }
    var zoom by remember { mutableStateOf(1f) }
    val offsetX by remember { mutableStateOf(0f) }
    val offsetY by remember { mutableStateOf(0f) }
    Box(
        Modifier.offset { IntOffset(offsetX, offsetY) }
            .graphicsLayer(scaleX = zoom, scaleY = zoom, rotationZ = angle)
            .background(Color.Blue)
            .pointerInput(Unit) {
                detectTransformGestures(
                    onGesture { _, _pan, _zoom, _rotation ->
                        // TODO: adjust by centroid
                        pan += _pan
                        zoom += _zoom
                        angle += _rotation
                    }
                )
            }
            .fillMaxSize()
    )
}
*/
