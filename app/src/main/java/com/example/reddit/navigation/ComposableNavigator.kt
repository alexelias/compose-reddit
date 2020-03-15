package com.example.reddit.navigation

import android.os.Bundle
import androidx.compose.Composable
import androidx.compose.FrameManager
import androidx.compose.mutableStateOf
import androidx.navigation.*
import java.util.*

private typealias ComposableUnitLambda = @Composable () -> Unit
private val EmptyRoute: ComposableUnitLambda = {}

@Navigator.Name("compose")
class ComposableNavigator : Navigator<Destination>() {

    private val stack = Stack<@Composable () -> Unit>()

    var current by mutableStateOf<@Composable () -> Unit>({})
        private set

    override fun createDestination(): Destination {
        return Destination(this)
    }

    override fun popBackStack(): Boolean {
        if (stack.empty()) {
            return false
        }

        current = stack.pop()
        return true
    }

    override fun navigate(
        destination: Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ): NavDestination? {
        FrameManager.ensureStarted()
        // TODO(ralu): Handle args, navOptions and navExtras.
        // We always add the destination to back stack for now.

        // We don't want to navigate back to the Placeholder, so we don't push it to the back stack.
        if (current !== EmptyRoute) {
            stack.push(current)
        }

        current = destination.content

        return destination
    }
}

@NavDestination.ClassType(Destination::class)
class Destination(navigator: ComposableNavigator) : NavDestination(navigator) {
    var content: ComposableUnitLambda = EmptyRoute
}

fun NavGraphBuilder.route(
    id: Int,
    content: @Composable () -> Unit) {
    addDestination(
        DestinationBuilder(provider[ComposableNavigator::class], id, content)
            .build())
}

fun NavGraphBuilder.route(
    id: Int,
    content: @Composable () -> Unit,
    builder: DestinationBuilder.() -> Unit) {
    addDestination(
        DestinationBuilder(provider[ComposableNavigator::class], id, content)
            .apply(builder)
            .build()
    )
}

class DestinationBuilder(
    navigator: ComposableNavigator,
    id: Int,
    private val content: @Composable () -> Unit)
    : NavDestinationBuilder<Destination>(navigator, id) {

    override fun build(): Destination {
        return super.build().also { it.content = content }
    }
}