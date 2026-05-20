package com.yingrensheng.app.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.yingrensheng.app.data.LoginPreferenceStore
import com.yingrensheng.core.navigation.route.AppRoute
import com.yingrensheng.core.navigation.route.TopLevelDestination
import com.yingrensheng.core.navigation.route.topLevelDestinations
import com.yingrensheng.core.common.result.AppResult
import com.yingrensheng.core.model.creation.SceneTemplate
import com.yingrensheng.core.model.work.Work
import com.yingrensheng.core.network.AuthSessionManager
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider
import com.yingrensheng.data.member.repository.MemberRepositoryProvider
import com.yingrensheng.data.user.repository.UserRepositoryProvider
import com.yingrensheng.data.work.repository.WorkRepositoryProvider
import com.yingrensheng.feature.agency.ui.AgencyEntryRoute
import com.yingrensheng.feature.auth.ui.AgreementRoute
import com.yingrensheng.feature.auth.ui.BackendCheckRoute
import com.yingrensheng.feature.auth.ui.LoginRoute
import com.yingrensheng.feature.create.ui.CreateEntryRoute
import com.yingrensheng.feature.create.ui.InterviewRoute
import com.yingrensheng.feature.create.ui.MaterialImportRoute
import com.yingrensheng.feature.create.ui.MaterialReviewRoute
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
import com.yingrensheng.feature.profile.ui.ProfileDetailRoute
import com.yingrensheng.feature.profile.ui.ProfileRoute
import com.yingrensheng.feature.profile.ui.SettingsRoute
import com.yingrensheng.feature.profile.ui.UserQrRoute
import com.yingrensheng.feature.works.ui.WorkDetailRoute
import com.yingrensheng.feature.works.ui.WorksRoute

@Composable
fun YingRenShengApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val userRepository = UserRepositoryProvider.current
    val creationRepository = CreationRepositoryProvider.current
    val session by userRepository.session().collectAsState()
    val loginPreferenceStore = remember(context) { LoginPreferenceStore(context) }
    var backendReady by remember { mutableStateOf<Boolean?>(null) }
    var backendProbeVersion by remember { mutableStateOf(0) }
    var currentRoute by remember { mutableStateOf(AppRoute.BackendCheck) }
    val routeBackStack = remember { mutableStateListOf<String>() }
    var lastHomeBackPressedAt by remember { mutableStateOf(0L) }
    var authLoading by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var lastLoginUsername by remember { mutableStateOf(loginPreferenceStore.lastUsername()) }
    var selectedWork by remember { mutableStateOf<Work?>(null) }
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
        if (loginPreferenceStore.hasAcceptedAgreement()) {
            userRepository.acceptAgreement()
        }
        userRepository.restoreSession()
        AuthSessionManager.setOnSessionExpired {
            scope.launch {
                userRepository.logout()
                currentRoute = AppRoute.Login
            }
        }
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

    fun firstRouteForScene(scene: SceneTemplate): String {
        return when (scene.sceneId) {
            "scene_character_xiyou" -> AppRoute.MaterialImport
            "scene_character_honglou" -> AppRoute.MaterialImport
            "scene_outline_history" -> AppRoute.MaterialImport
            "scene_outline_original" -> AppRoute.MaterialImport
            "scene_media_comic" -> AppRoute.MaterialImport
            "scene_media_short_video" -> AppRoute.MaterialImport
            else -> AppRoute.MaterialImport
        }
    }

    fun routeAfterMaterialImport(): String {
        return when (creationRepository.observeSession().value.selectedScene?.sceneId) {
            "scene_character_xiyou" -> AppRoute.StoryDraft
            "scene_character_honglou" -> AppRoute.MaterialReview
            "scene_outline_history" -> AppRoute.StyleSelect
            "scene_outline_original" -> AppRoute.Interview
            "scene_media_comic" -> AppRoute.StoryGenerating
            "scene_media_short_video" -> AppRoute.StyleSelect
            else -> AppRoute.StoryDraft
        }
    }

    fun routeAfterMaterialReview(): String {
        return when (creationRepository.observeSession().value.selectedScene?.sceneId) {
            "scene_character_honglou" -> AppRoute.Interview
            else -> AppRoute.StoryDraft
        }
    }

    fun routeAfterInterview(): String {
        return when (creationRepository.observeSession().value.selectedScene?.sceneId) {
            "scene_outline_original" -> AppRoute.StoryDraft
            "scene_character_honglou" -> AppRoute.StyleSelect
            else -> AppRoute.StoryDraft
        }
    }

    fun routeAfterStyleSelect(): String {
        return when (creationRepository.observeSession().value.selectedScene?.sceneId) {
            "scene_outline_history" -> AppRoute.StoryGenerating
            "scene_media_short_video" -> AppRoute.StoryGenerating
            "scene_character_honglou" -> AppRoute.StoryDraft
            else -> AppRoute.StoryDraft
        }
    }

    fun routeAfterStoryDraft(): String {
        return when (creationRepository.observeSession().value.selectedScene?.sceneId) {
            "scene_outline_original" -> AppRoute.StoryGenerating
            else -> AppRoute.StoryGenerating
        }
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
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
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
                    loginPreferenceStore.saveAcceptedAgreement()
                    currentRoute = AppRoute.Login
                },
            )

            AppRoute.Login -> LoginRoute(
                onLogin = { username, password ->
                    scope.launch {
                        authLoading = true
                        authErrorMessage = null
                        when (val result = userRepository.login(username, password)) {
                            is AppResult.Success -> {
                                loginPreferenceStore.saveLastUsername(username)
                                lastLoginUsername = username
                                currentRoute = AppRoute.Home
                            }
                            is AppResult.Error -> authErrorMessage = result.message
                        }
                        authLoading = false
                    }
                },
                onRegister = { username, nickname, email, password ->
                    scope.launch {
                        authLoading = true
                        authErrorMessage = null
                        when (val result = userRepository.register(username, nickname, email, password)) {
                            is AppResult.Success -> {
                                loginPreferenceStore.saveLastUsername(username)
                                lastLoginUsername = username
                                currentRoute = AppRoute.Home
                            }
                            is AppResult.Error -> authErrorMessage = result.message
                        }
                        authLoading = false
                    }
                },
                loading = authLoading,
                errorMessage = authErrorMessage,
                initialUsername = lastLoginUsername,
            )

            AppRoute.Home -> HomeRoute(
                onStartCreate = { navigate(AppRoute.CreateEntry) },
            )

            AppRoute.CreateEntry -> CreateEntryRoute(
                onFlowStarted = { scene -> navigate(firstRouteForScene(scene)) },
            )

            AppRoute.MaterialImport -> MaterialImportRoute(
                onContinue = { navigate(routeAfterMaterialImport()) },
            )

            AppRoute.MaterialReview -> MaterialReviewRoute(
                onContinue = {
                    navigate(routeAfterMaterialReview())
                },
            )

            AppRoute.Interview -> InterviewRoute(
                onContinue = { navigate(routeAfterInterview()) },
            )

            AppRoute.StyleSelect -> StyleSelectRoute(
                onContinue = { navigate(routeAfterStyleSelect()) },
            )

            AppRoute.StoryDraft -> StoryDraftRoute(
                onContinue = { navigate(routeAfterStoryDraft()) },
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
                isAdmin = MemberRepositoryProvider.current.getMemberInfo().levelName.equals("Admin", ignoreCase = true),
            )

            AppRoute.Payment -> PaymentRoute(
                onContinue = { navigate(AppRoute.ExportProcessing) },
            )

            AppRoute.ExportProcessing -> ExportProcessingRoute(
                onBackToWorks = { popBackTo(AppRoute.Works) },
            )

            AppRoute.Works -> WorksRoute(
                onOpenWork = { work ->
                    selectedWork = work
                    navigate(AppRoute.WorkDetail)
                },
            )

            AppRoute.WorkDetail -> WorkDetailRoute(
                work = selectedWork,
                onDeleteWork = { work ->
                    scope.launch {
                        val deleted = withContext(Dispatchers.IO) {
                            WorkRepositoryProvider.current.deleteWork(work.workId)
                        }
                        selectedWork = null
                        popBackTo(AppRoute.Works)
                        Toast.makeText(
                            context,
                            if (deleted) "作品已删除" else "作品记录不存在",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
            )

            AppRoute.Orders -> OrdersRoute()

            AppRoute.Profile -> ProfileRoute(
                onOpenProfileDetail = { navigate(AppRoute.ProfileDetail) },
                onOpenQr = { navigate(AppRoute.ProfileQr) },
                onOpenOrders = { navigate(AppRoute.Orders) },
                onOpenMember = { navigate(AppRoute.MemberCenter) },
                onOpenSettings = { navigate(AppRoute.Settings) },
            )

            AppRoute.ProfileDetail -> ProfileDetailRoute()

            AppRoute.ProfileQr -> UserQrRoute()

            AppRoute.Settings -> SettingsRoute(
                onLogout = {
                    userRepository.logout()
                    popBackTo(AppRoute.Login)
                },
            )

            AppRoute.MemberCenter -> MemberCenterRoute()

            AppRoute.AgencyEntry -> AgencyEntryRoute()
            }
        }
    }
}
