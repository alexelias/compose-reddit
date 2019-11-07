package com.example.reddit.components

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.Composable
import androidx.compose.onCommit
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.ui.core.*
import androidx.ui.engine.geometry.Offset
import androidx.ui.foundation.DrawImage
import androidx.ui.foundation.shape.DrawShape
import androidx.ui.foundation.shape.RectangleShape
import androidx.ui.graphics.*
import androidx.ui.graphics.colorspace.ColorSpace
import androidx.ui.graphics.colorspace.ColorSpaces
import androidx.ui.layout.*
import androidx.ui.tooling.preview.Preview
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.lang.Exception

internal fun Bitmap.Config.toImageConfig(): ImageConfig {
    // Cannot utilize when statements with enums that may have different sets of supported
    // values between the compiled SDK and the platform version of the device.
    // As a workaround use if/else statements
    // See https://youtrack.jetbrains.com/issue/KT-30473 for details
    @Suppress("DEPRECATION")
    return if (this == Bitmap.Config.ALPHA_8) {
        ImageConfig.Alpha8
    } else if (this == Bitmap.Config.RGB_565) {
        ImageConfig.Rgb565
    } else if (this == Bitmap.Config.ARGB_4444) {
        ImageConfig.Argb8888 // Always upgrade to Argb_8888
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this == Bitmap.Config.RGBA_F16) {
        ImageConfig.F16
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this == Bitmap.Config.HARDWARE) {
        ImageConfig.Gpu
    } else {
        ImageConfig.Argb8888
    }
}


@RequiresApi(Build.VERSION_CODES.O)
internal fun android.graphics.ColorSpace.toComposeColorSpace(): ColorSpace {
    return when (this) {
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SRGB)
        -> ColorSpaces.Srgb
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.ACES)
        -> ColorSpaces.Aces
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.ACESCG)
        -> ColorSpaces.Acescg
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.ADOBE_RGB)
        -> ColorSpaces.AdobeRgb
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.BT2020)
        -> ColorSpaces.Bt2020
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.BT709)
        -> ColorSpaces.Bt709
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.CIE_LAB)
        -> ColorSpaces.CieLab
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.CIE_XYZ)
        -> ColorSpaces.CieXyz
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.DCI_P3)
        -> ColorSpaces.DciP3
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.DISPLAY_P3)
        -> ColorSpaces.DisplayP3
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.EXTENDED_SRGB)
        -> ColorSpaces.ExtendedSrgb
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.LINEAR_EXTENDED_SRGB)
        -> ColorSpaces.LinearExtendedSrgb
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.LINEAR_SRGB)
        -> ColorSpaces.LinearSrgb
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.NTSC_1953)
        -> ColorSpaces.Ntsc1953
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.PRO_PHOTO_RGB)
        -> ColorSpaces.ProPhotoRgb
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SMPTE_C)
        -> ColorSpaces.SmpteC
        else -> ColorSpaces.Srgb
    }
}


// TODO njawad expand API surface with other alternatives for Image creation?
internal class AndroidImage(val bitmap: Bitmap) : Image {

    /**
     * @see Image.width
     */
    override val width: Int
        get() = bitmap.width

    /**
     * @see Image.height
     */
    override val height: Int
        get() = bitmap.height

    override val config: ImageConfig
        get() = bitmap.config.toImageConfig()

    /**
     * @see Image.colorSpace
     */
    override val colorSpace: ColorSpace
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bitmap.colorSpace?.toComposeColorSpace() ?: ColorSpaces.Srgb
        } else {
            ColorSpaces.Srgb
        }

    /**
     * @see Image.hasAlpha
     */
    override val hasAlpha: Boolean
        get() = bitmap.hasAlpha()

    /**
     * @see Image.nativeImage
     */
    override val nativeImage: NativeImage
        get() = bitmap

    /**
     * @see
     */
    override fun prepareToDraw() {
        bitmap.prepareToDraw()
    }
}

@Composable
fun Image(
    modifier: Modifier = Modifier.None,
    url: String,
    width: Dp? = null,
    height: Dp? = null,
    aspectRatio: Float? = null
) {
    var image by +state<Image?> { null }
    var drawable by +state<Drawable?> { null }
    +onCommit(url) {
        val picasso = Picasso.get()
        val target = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                drawable = placeHolderDrawable
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                drawable = errorDrawable
            }

            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                image = bitmap?.let { AndroidImage(it) }
            }
        }
        picasso
            .load(url)
//            .resize(100, 100)
//            .centerCrop()
            .into(target)

        onDispose {
            image = null
            drawable = null
            picasso.cancelRequest(target)
        }
    }

    val imageModifier = when {
        width != null && height != null -> Size(width, height)
        aspectRatio != null && width != null -> Size(width = width) wraps AspectRatio(aspectRatio)
        aspectRatio != null && height != null -> Size(height = height) wraps AspectRatio(aspectRatio)
        aspectRatio != null -> ExpandedWidth wraps AspectRatio(aspectRatio)
        else -> Modifier.None
    }

    Row(modifier = modifier wraps imageModifier) {
        val theImage = image
        val theDrawable = drawable
        if (theImage != null) {
            DrawImage(image = theImage)
        } else if (theDrawable != null) {
            Draw { canvas, _ -> theDrawable.draw(canvas.nativeCanvas) }
        } else {
            DrawShape(shape = RectangleShape, color = Color.LightGray)
        }
    }
}

val p = Paint()

@Composable
fun DrawImage2(image: Image) {
    Draw { canvas, parentSize ->
        canvas.drawImage(image, Offset.zero, p)
    }
}
//
//@Composable
//private fun ImageLayout(
//    aspectRatio: Float,
//    children: @Composable () -> Unit
//) {
//    Layout(children) { measurables, constraints ->
//        var width = constraints.minWidth
//        val height = min(constraints.maxHeight, width * aspectRatio)
//        if (height < width * aspectRatio) {
//            width = height / aspectRatio
//        }
//
//        val childConstraints = Constraints(
//            width, width,
//            height, height
//        )
//
//        val measurable = measurables.firstOrNull()
//
//        val placeable = measurable?.measure(childConstraints)
////        placeable.height
//
//        layout(width, height) {
//            placeable?.place(0.ipx, 0.ipx)
//        }
//    }
//}


@Preview("playground")
@Composable fun TestLayout() {
    Container(width = 300.dp, height = 600.dp, alignment = Alignment.TopLeft) {
        background(color = Color.Blue)
        Column {
            Row {
                background(color = Color.Red)
                Column {
                    background(color = Color.Green)
                    Text(text = "One")
                    Text(text = "Two")
                    Text(text = "Three")
                }
                Column {
                    background(color = Color.Yellow)
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

@Composable fun background(color: Color) {
    DrawShape(shape = RectangleShape, color = color)
}

class Size(val width: Dp? = null, val height: Dp? = null) : LayoutModifier {
    override fun DensityScope.modifyConstraints(constraints: Constraints): Constraints {
        return constraints.withTight(width?.toIntPx(), height?.toIntPx())
    }

    override fun DensityScope.modifySize(
        constraints: Constraints,
        childSize: IntPxSize
    ): IntPxSize = childSize

    override fun DensityScope.minIntrinsicWidthOf(measurable: Measurable, height: IntPx): IntPx =
        measurable.minIntrinsicWidth(height)

    override fun DensityScope.maxIntrinsicWidthOf(measurable: Measurable, height: IntPx): IntPx =
        measurable.maxIntrinsicWidth(height)

    override fun DensityScope.minIntrinsicHeightOf(measurable: Measurable, width: IntPx): IntPx =
        measurable.minIntrinsicHeight(width)

    override fun DensityScope.maxIntrinsicHeightOf(measurable: Measurable, width: IntPx): IntPx =
        measurable.maxIntrinsicHeight(width)

    override fun DensityScope.modifyPosition(
        childPosition: IntPxPosition,
        childSize: IntPxSize,
        containerSize: IntPxSize
    ): IntPxPosition = childPosition

    override fun DensityScope.modifyAlignmentLine(line: AlignmentLine, value: IntPx?) = value

    override fun DensityScope.modifyParentData(parentData: Any?): Any? = parentData

}