package com.yingrensheng.core.navigation.route

data class TopLevelDestination(
    val route: String,
    val label: String,
)

val topLevelDestinations = listOf(
    TopLevelDestination(route = AppRoute.Home, label = "首页"),
    TopLevelDestination(route = AppRoute.CreateEntry, label = "创作"),
    TopLevelDestination(route = AppRoute.Works, label = "作品"),
    TopLevelDestination(route = AppRoute.Profile, label = "我"),
)
