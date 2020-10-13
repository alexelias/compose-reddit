package com.example.reddit.components

//import androidx.compose.tooling.preview.Preview
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

@Composable
fun Image(
    modifier: Modifier = Modifier,
    url: String,
    width: Dp? = null,
    height: Dp? = null,
    aspectRatio: Float? = null
) {
    var (image, setImage) = remember { mutableStateOf<ImageAsset?>(null) }
    var (drawable, setDrawable) = remember { mutableStateOf<Drawable?>(null) }
    onCommit(url) {
        val picasso = Picasso.get()
        val target = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                setDrawable(placeHolderDrawable)
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                setDrawable(errorDrawable)
            }

            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                setImage(bitmap?.asImageAsset())
            }
        }
        picasso
            .load(url)
            .into(target)

        onDispose {
            image = null
            drawable = null
            picasso.cancelRequest(target)
        }
    }

    val imageModifier = modifier.then(when {
        width != null && height != null -> Modifier.preferredSize(width, height)
        aspectRatio != null && width != null -> Modifier.preferredWidth(width).aspectRatio(aspectRatio)
        aspectRatio != null && height != null -> Modifier.preferredHeight(height).aspectRatio(aspectRatio)
        aspectRatio != null -> Modifier.fillMaxWidth().aspectRatio(aspectRatio)
        else -> Modifier
    })

    if (image == null && drawable != null) {
        Canvas(modifier = imageModifier) {
            drawIntoCanvas { canvas -> drawable!!.draw(canvas.nativeCanvas) }
        }
    } else {
        if (image == null) {
            Box(imageModifier.background(Color.LightGray))
        } else {
            Image(image!!, imageModifier)
        }
    }
}

/*
@Preview("playground")
@Composable fun TestLayout() {
    Box(Modifier.drawBackground(color = Color.Blue).preferredSize(width = 300.dp, height = 600.dp)) {
        Column {
            Row(Modifier.drawBackground(Color.Red)) {
                Column(Modifier.drawBackground(Color.Green)) {
                    Text(text = "One")
                    Text(text = "Two")
                    Text(text = "Three")
                }
                Column(Modifier.drawBackground(Color.Yellow)) {
                    Text(text = "One")
                    Text(text = "Two")
                    Text(text = "Three")
                }
            }
                Image(
                    url = "https://loremflickr.com/640/360",
                    aspectRatio = 16f / 9f
                )
        }
    }
}
*/
