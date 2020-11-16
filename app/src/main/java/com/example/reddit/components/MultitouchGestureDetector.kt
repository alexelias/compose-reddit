/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.reddit.components
/*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.gesture.ExperimentalPointerInput
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastSumBy
import kotlin.contracts.ExperimentalContracts
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

import androidx.compose.foundation.gestures.*

private val NoRotateZoom: (Float) -> Unit = { }
private val NoPan: (Offset) -> Unit = { }

/**
 * A gesture detector for rotationg, panning, and zoom. Once touch slop has been reached, the
 * user can use rotation, panning and zoom gestures. [onRotate] will be called when rotation
 * occurs, passing the angle in degrees, [onZoom] will be called when zoom occurs, and [onPan]
 * will be called when a pan occurs. Each of these changes is a difference between the previous
 * call and the current gesture. This will consume all position changes after touch slop has
 * been reached.
 *
 * If [panZoomLock] is `true`, rotation is allowed only if touch slop is detected for rotation
 * before pan or zoom motions. If not, pan and zoom gestures will be detected, but rotation
 * gestures will not be. If [panZoomLock] is `false`, once touch slop is reached, all three
 * gestures are detected.
 *
 * Example Usage:
 * @sample androidx.compose.foundation.samples.DetectMultitouchGestures
 */
@ExperimentalPointerInput
suspend fun PointerInputScope.detectMultitouchGestures(
    panZoomLock: Boolean = false,
    onRotate: (rotation: Float) -> Unit = NoRotateZoom,
    onZoom: (zoom: Float) -> Unit = NoRotateZoom,
    onPan: (pan: Offset) -> Unit = NoPan
) {
    forEachGesture {
        handlePointerInput {
            var rotation = 0f
            var zoom = 1f
            var pan = Offset.Zero
            var pastTouchSlop = false
            val touchSlop = viewConfiguration.touchSlop
            var lockedToPanZoom = false

            waitForFirstDown()
            do {
                val event = awaitPointerEvent()
                val canceled = anyPositionConsumed(event)
                if (!canceled) {
                    val zoomChange = event.calculateZoom()
                    val rotationChange = event.calculateRotation()
                    val panChange = event.calculatePan()

                    if (!pastTouchSlop) {
                        zoom *= zoomChange
                        rotation += rotationChange
                        pan += panChange

                        val centroidSize = event.calculateCentroidSize(useCurrent = false)
                        val zoomMotion = abs(1 - zoom) * centroidSize
                        val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                        val panMotion = pan.getDistance()

                        if (zoomMotion > touchSlop ||
                            rotationMotion > touchSlop ||
                            panMotion > touchSlop
                        ) {
                            pastTouchSlop = true
                            lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                        }
                    }
                    if (pastTouchSlop) {
                        event.changes.fastForEach {
                            if (it.positionChanged()) {
                                it.consumeAllChanges()
                            }
                        }
                        if (!lockedToPanZoom && rotationChange != 0f) {
                            onRotate(rotationChange)
                        }
                        if (zoomChange != 1f) {
                            onZoom(zoomChange)
                        }
                        if (panChange != Offset.Zero) {
                            onPan(panChange)
                        }
                    }
                }
            } while (!canceled && event.changes.fastAny { it.current.down })
        }
    }
}

/**
 * Returns the rotation, in degrees, of the pointers between the [PointerInputChange.previous]
 * and [PointerInputChange.current] states. Only the pointers that are down in both previous
 * and current states are considered.
 *
 * Example Usage:
 * @sample androidx.compose.foundation.samples.CalculateRotation
 */
fun PointerEvent.calculateRotation(): Float {
    val pointerCount = changes.fastSumBy { if (it.previous.down && it.current.down) 1 else 0 }
    if (pointerCount < 2) {
        return 0f
    }
    val currentCentroid = calculateCentroid(useCurrent = true)
    val previousCentroid = calculateCentroid(useCurrent = false)
    var rotation = 0f
    var rotationWeight = 0f

    // We want to weigh each pointer differently so that motions farther from the
    // centroid have more weight than pointers close to the centroid. Essentially,
    // a small distance change near the centroid could equate to a large angle
    // change and we don't want it to affect the rotation as much as pointers farther
    // from the centroid, which should be more stable.

    changes.fastForEach { change ->
        if (change.current.down && change.previous.down) {
            val currentPosition = change.current.position!!
            val previousPosition = change.previous.position!!
            val previousOffset = previousPosition - previousCentroid
            val currentOffset = currentPosition - currentCentroid

            val previousAngle = previousOffset.angle()
            val currentAngle = currentOffset.angle()
            val angleDiff = currentAngle - previousAngle
            val weight = (currentOffset + previousOffset).getDistance() / 2f

            // We weigh the rotation with the distance to the centroid. This gives
            // more weight to angle changes from pointers farther from the centroid than
            // those that are closer.
            rotation += when {
                angleDiff > 180f -> angleDiff - 360f
                angleDiff < -180f -> angleDiff + 360f
                else -> angleDiff
            } * weight

            // weight its contribution by the distance to the centroid
            rotationWeight += weight
        }
    }
    return if (rotationWeight == 0f) 0f else rotation / rotationWeight
}

/**
 * Returns the angle of the [Offset] between -180 and 180, or 0 if [Offset.Zero].
 */
private fun Offset.angle(): Float =
    if (x == 0f && y == 0f) 0f else -atan2(x, y) * 180f / PI.toFloat()

/**
 * Uses the change of the centroid size between the [PointerInputChange.previous] and
 * [PointerInputChange.current] to determine how much zoom was intended.
 *
 * Example Usage:
 * @sample androidx.compose.foundation.samples.CalculateZoom
 */
fun PointerEvent.calculateZoom(): Float {
    val currentCentroidSize = calculateCentroidSize(useCurrent = true)
    val previousCentroidSize = calculateCentroidSize(useCurrent = false)
    if (currentCentroidSize == 0f || previousCentroidSize == 0f) {
        return 1f
    }
    return currentCentroidSize / previousCentroidSize
}

/**
 * Returns the change in the centroid location between the previous and the current pointers that
 * are down. Pointers that are newly down or raised are not considered in the centroid
 * movement.
 *
 * Example Usage:
 * @sample androidx.compose.foundation.samples.CalculatePan
 */
fun PointerEvent.calculatePan(): Offset {
    val currentCentroid = calculateCentroid(useCurrent = true)
    if (currentCentroid == Offset.Unspecified) {
        return Offset.Zero
    }
    val previousCentroid = calculateCentroid(useCurrent = false)
    return currentCentroid - previousCentroid
}

/**
 * Returns the average distance from the centroid for all pointers that are currently
 * and were previously down. If no pointers are down, `0` is returned.
 * If [useCurrent] is `true`, the size of the [PointerInputChange.current] is returned and if
 * `false`, the size of [PointerInputChange.previous] is returned. Only pointers that are down
 * in both the previous and current state are used to calculate the centroid size.
 *
 * Example Usage:
 * @sample androidx.compose.foundation.samples.CalculateCentroidSize
 */
fun PointerEvent.calculateCentroidSize(useCurrent: Boolean = true): Float {
    val centroid = calculateCentroid(useCurrent)
    if (centroid == Offset.Unspecified) {
        return 0f
    }

    var distanceToCentroid = 0f
    var distanceWeight = 0
    changes.fastForEach { change ->
        if (change.current.down && change.previous.down) {
            val data = if (useCurrent) change.current else change.previous
            distanceToCentroid += (data.position!! - centroid).getDistance()
            distanceWeight++
        }
    }
    return distanceToCentroid / distanceWeight.toFloat()
}

/**
 * Returns the centroid of all pointers that are down and were previously down. If no pointers
 * are down, [Offset.Unspecified] is returned. If [useCurrent] is `true`, the centroid of the
 * [PointerInputChange.current] is returned and if `false`, the centroid of the
 * [PointerInputChange.previous] is returned. Only pointers that are down in both the previous and
 * current state are used to calculate the centroid.
 *
 * Example Usage:
 * @sample androidx.compose.foundation.samples.CalculateCentroidSize
 */
@OptIn(ExperimentalContracts::class)
fun PointerEvent.calculateCentroid(
    useCurrent: Boolean = true
): Offset {
    var centroid = Offset.Zero
    var centroidWeight = 0

    changes.fastForEach { change ->
        if (change.current.down && change.previous.down) {
            val data = if (useCurrent) change.current else change.previous
            centroid += data.position!!
            centroidWeight++
        }
    }
    return if (centroidWeight == 0) {
        Offset.Unspecified
    } else {
        centroid / centroidWeight.toFloat()
    }
}
*/