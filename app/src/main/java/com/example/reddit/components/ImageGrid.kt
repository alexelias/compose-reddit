package com.example.reddit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.drawLayer
import androidx.compose.ui.gesture.ExperimentalPointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.paging.PagedList
import com.example.reddit.data.Link
import com.example.reddit.screens.imageUrl
import dev.chrisbanes.accompanist.coil.CoilImage

import androidx.compose.foundation.gestures.*

@Composable
fun ImageGrid(links: PagedList<Link>, header: @Composable () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxHeight().background(Color.Black)) {
        item {
            header()
        }
        items(links.filter { it.preview?.imageUrl != null }.chunked(4)) { row ->
            Row(Modifier.fillMaxWidth().height(100.dp)) {
                for (l in row) {
                    Box(Modifier.weight(1f).aspectRatio(1f)) {
                        CoilImage(
                            data = l.preview!!.imageUrl!!,
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }

}


@OptIn(ExperimentalPointerInput::class)
@Composable
fun DetectMultitouchGesturesExample() {
    var angle by remember { mutableStateOf(0f) }
    var zoom by remember { mutableStateOf(1f) }
    val offsetX = remember { mutableStateOf(0f) }
    val offsetY = remember { mutableStateOf(0f) }
    Box(
        Modifier.offsetPx(offsetX, offsetY)
            .drawLayer(
                scaleX = zoom,
                scaleY = zoom,
                rotationZ = angle
            ).background(Color.Blue)
            .pointerInput {
                detectMultitouchGestures(
                    onRotate = { angle += it },
                    onZoom = { zoom *= it },
                    onPan = {
                        offsetX.value += it.x
                        offsetY.value += it.y
                    }
                )
            }
            .fillMaxSize()
    )
}