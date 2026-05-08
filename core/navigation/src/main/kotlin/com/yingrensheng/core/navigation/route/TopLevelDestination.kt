package com.yingrensheng.core.navigation.route

data class TopLevelDestination(
    val route: String,
    val label: String,
)

val topLevelDestinations = listOf(
    TopLevelDestination(route = AppRoute.Home, label = "Home"),
    TopLevelDestination(route = AppRoute.CreateEntry, label = "Create"),
    TopLevelDestination(route = AppRoute.Works, label = "Works"),
    TopLevelDestination(route = AppRoute.Profile, label = "Profile"),
)

