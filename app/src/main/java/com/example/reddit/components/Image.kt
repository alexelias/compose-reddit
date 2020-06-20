package com.example.reddit.components

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.*
import androidx.ui.core.*
import androidx.ui.foundation.*
import androidx.ui.graphics.*
import androidx.ui.graphics.colorspace.ColorSpace
import androidx.ui.graphics.colorspace.ColorSpaces
import androidx.ui.graphics.drawscope.*
import androidx.ui.graphics.painter.ImagePainter
import androidx.ui.layout.*
//import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.*
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.lang.Exception

@Composable
fun Image(
    modifier: Modifier = Modifier,
    url: String,
    width: Dp? = null,
    height: Dp? = null,
    aspectRatio: Float? = null
) {
    var image by state<ImageAsset?> { null }
    var drawable by state<Drawable?> { null }
    onCommit(url) {
        val picasso = Picasso.get()
        val target = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                drawable = placeHolderDrawable
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                drawable = errorDrawable
            }

            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                image = bitmap?.asImageAsset()
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

    val imageModifier = modifier + when {
        width != null && height != null -> Modifier.preferredSize(width, height)
        aspectRatio != null && width != null -> Modifier.preferredWidth(width).aspectRatio(aspectRatio)
        aspectRatio != null && height != null -> Modifier.preferredHeight(height).aspectRatio(aspectRatio)
        aspectRatio != null -> Modifier.fillMaxWidth().aspectRatio(aspectRatio)
        else -> Modifier
    }

    if (image == null && drawable != null) {
        Canvas(modifier = imageModifier) {
            drawCanvas { canvas, _ -> drawable!!.draw(canvas.nativeCanvas) }
        }
    } else {
        if (image == null) {
            Box(modifier = imageModifier.drawBackground(Color.LightGray), children = emptyContent())
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
