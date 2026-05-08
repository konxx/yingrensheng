package com.yingrensheng.app.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.yingrensheng.core.navigation.route.AppRoute
import com.yingrensheng.core.navigation.route.TopLevelDestination
import com.yingrensheng.core.navigation.route.topLevelDestinations
import com.yingrensheng.data.user.repository.UserRepositoryProvider
import com.yingrensheng.feature.agency.ui.AgencyEntryRoute
import com.yingrensheng.feature.auth.ui.AgreementRoute
import com.yingrensheng.feature.auth.ui.BackendCheckRoute
import com.yingrensheng.feature.auth.ui.LoginRoute
import com.yingrensheng.feature.create.ui.CreateEntryRoute
import com.yingrensheng.feature.create.ui.InterviewRoute
import com.yingrensheng.feature.create.ui.MaterialImportRoute
import com.yingrensheng.feature.create.ui.MaterialReviewRoute
import com.yingrensheng.feature.create.ui.SceneSelectRoute
import com.yingrensheng.feature.create.ui.StoryDraftRoute
import com.yingrensheng.feature.create.ui.StoryGeneratingRoute
import com.yingrensheng.feature.create.ui.StyleSelectRoute
import com.yingrensheng.feature.editor.ui.ExportPlanRoute
import com.yingrensheng.feature.editor.ui.PreviewRoute
import com.yingrensheng.feature.editor.ui.StoryboardRoute
import com.yingrensheng.feature.home.ui.HomeRoute
import com.yingrensheng.feature.member.ui.MemberCenterRoute
import com.yingrensheng.feature.onboarding.ui.OnboardingRoute
import com.yingrensheng.feature.order.ui.ExportProcessingRoute
import com.yingrensheng.feature.order.ui.OrdersRoute
import com.yingrensheng.feature.order.ui.PaymentRoute
import com.yingrensheng.feature.profile.ui.ProfileRoute
import com.yingrensheng.feature.works.ui.WorksRoute

@Composable
fun YingRenShengApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val userRepository = UserRepositoryProvider.current
    val session by userRepository.session().collectAsState()
    var backendReady by remember { mutableStateOf<Boolean?>(null) }
    var backendProbeVersion by remember { mutableStateOf(0) }
    var currentRoute by remember { mutableStateOf(AppRoute.BackendCheck) }
    val routeBackStack = remember { mutableStateListOf<String>() }
    var lastHomeBackPressedAt by remember { mutableStateOf(0L) }
    val showBottomBar = currentRoute in topLevelDestinations.map(TopLevelDestination::route)

    fun resolvePostBackendRoute(): String {
        return when {
            !session.hasAcceptedAgreement -> AppRoute.Onboarding
            session.user == null -> AppRoute.Login
            else -> AppRoute.Home
        }
    }

    suspend fun refreshBackendState() {
        backendReady = null
        backendReady = withContext(Dispatchers.IO) {
            userRepository.isBackendReachable()
        }
    }

    LaunchedEffect(backendProbeVersion) {
        refreshBackendState()
        if (backendReady == true) {
            currentRoute = resolvePostBackendRoute()
        }
    }

    fun navigate(route: String) {
        if (currentRoute != route) {
            routeBackStack.add(currentRoute)
            currentRoute = route
        }
    }

    fun navigateTopLevel(route: String) {
        currentRoute = route
    }

    fun popBackTo(route: String) {
        currentRoute = route
        routeBackStack.removeAll { true }
    }

    BackHandler {
        if (routeBackStack.isNotEmpty()) {
            currentRoute = routeBackStack.removeAt(routeBackStack.lastIndex)
            return@BackHandler
        }

        if (currentRoute == AppRoute.Home) {
            val now = System.currentTimeMillis()
            if (now - lastHomeBackPressedAt < 2_000) {
                activity?.finish()
            } else {
                lastHomeBackPressedAt = now
                Toast.makeText(context, "再按一次退出应用", Toast.LENGTH_SHORT).show()
            }
            return@BackHandler
        }

        activity?.finish()
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { navigateTopLevel(destination.route) },
                            icon = { Text(destination.label.take(1)) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { _ ->
        when (currentRoute) {
            AppRoute.BackendCheck -> BackendCheckRoute(
                backendReady = backendReady,
                onRetry = {
                    backendProbeVersion += 1
                },
                onContinue = { currentRoute = resolvePostBackendRoute() },
            )

            AppRoute.Onboarding -> OnboardingRoute(
                onContinue = { currentRoute = AppRoute.Agreement },
            )

            AppRoute.Agreement -> AgreementRoute(
                onAccept = {
                    userRepository.acceptAgreement()
                    currentRoute = AppRoute.Login
                },
            )

            AppRoute.Login -> LoginRoute(
                onLogin = { username, password ->
                    userRepository.login(username, password)
                    currentRoute = AppRoute.Home
                },
                onRegister = { username, nickname, email, password ->
                    userRepository.register(username, nickname, email, password)
                    currentRoute = AppRoute.Home
                },
            )

            AppRoute.Home -> HomeRoute(
                onStartCreate = { navigate(AppRoute.CreateEntry) },
            )

            AppRoute.CreateEntry -> CreateEntryRoute(
                onModeSelected = { navigate(AppRoute.SceneSelect) },
            )

            AppRoute.SceneSelect -> SceneSelectRoute(
                onSceneSelected = { navigate(AppRoute.MaterialImport) },
            )

            AppRoute.MaterialImport -> MaterialImportRoute(
                onContinue = { navigate(AppRoute.MaterialReview) },
            )

            AppRoute.MaterialReview -> MaterialReviewRoute(
                onContinue = { navigate(AppRoute.Interview) },
            )

            AppRoute.Interview -> InterviewRoute(
                onContinue = { navigate(AppRoute.StyleSelect) },
            )

            AppRoute.StyleSelect -> StyleSelectRoute(
                onContinue = { navigate(AppRoute.StoryDraft) },
            )

            AppRoute.StoryDraft -> StoryDraftRoute(
                onContinue = { navigate(AppRoute.StoryGenerating) },
            )

            AppRoute.StoryGenerating -> StoryGeneratingRoute(
                onContinue = { navigate(AppRoute.Storyboard) },
            )

            AppRoute.Storyboard -> StoryboardRoute(
                onContinue = { navigate(AppRoute.Preview) },
            )

            AppRoute.Preview -> PreviewRoute(
                onContinue = { navigate(AppRoute.ExportPlan) },
            )

            AppRoute.ExportPlan -> ExportPlanRoute(
                onContinue = { navigate(AppRoute.Payment) },
            )

            AppRoute.Payment -> PaymentRoute(
                onContinue = { navigate(AppRoute.ExportProcessing) },
            )

            AppRoute.ExportProcessing -> ExportProcessingRoute(
                onBackToWorks = { popBackTo(AppRoute.Works) },
            )

            AppRoute.Works -> WorksRoute()

            AppRoute.Profile -> ProfileRoute(
                onOpenMember = { navigate(AppRoute.MemberCenter) },
                onOpenOrders = { navigate(AppRoute.Orders) },
                onOpenAgency = { navigate(AppRoute.AgencyEntry) },
            )

            AppRoute.MemberCenter -> MemberCenterRoute()

            AppRoute.Orders -> OrdersRoute()

            AppRoute.AgencyEntry -> AgencyEntryRoute()
        }
    }
}
