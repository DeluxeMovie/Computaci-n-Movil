package com.example.taller_3.navigation

import com.example.taller_3.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.taller_3.data.UserInfo
import com.example.taller_3.ui.components.UserPortrait
import com.example.taller_3.ui.screens.ForgotPasswordScreen
import com.example.taller_3.ui.screens.HomeScreen
import com.example.taller_3.ui.screens.LoginScreen
import com.example.taller_3.ui.screens.ProfileScreen
import com.example.taller_3.ui.screens.RegisterScreen
import com.example.taller_3.utils.DateUtils
import com.example.taller_3.viewmodels.AuthViewModel
import com.example.taller_3.viewmodels.FirestoreViewModel
import com.example.taller_3.viewmodels.LocationViewModel
import kotlinx.coroutines.launch

private fun NavBackStack<NavKey>.navigate(route: Routes, clearStack: Boolean = false) {
    if (lastOrNull() == route) return
    if (clearStack) clear()
    add(route)
}

/**
 * Root navigation host. Owns the back stack and displays the
 * current entry using [NavDisplay].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavHost(modifier: Modifier = Modifier) {
    val authViewModel: AuthViewModel = viewModel()
    val firestoreViewModel: FirestoreViewModel = viewModel()
    val locationViewModel: LocationViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val profile by firestoreViewModel.state.collectAsState()

    // El perfil del usuario con sesión activa se escucha en tiempo real para mostrarlo en el drawer
    DisposableEffect(currentUser?.uid) {
        currentUser?.uid?.let(firestoreViewModel::observeUser)
        onDispose { firestoreViewModel.stopObserving() }
    }

    val backStack = rememberNavBackStack(
        if (currentUser != null) Routes.Home else Routes.Login
    )
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val publicRoutes = listOf(Routes.Login, Routes.Register, Routes.ForgotPassword)
    val isPublicRoute = backStack.lastOrNull() in publicRoutes

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (authViewModel.currentUser.value == null && backStack.lastOrNull() !in publicRoutes) {
            backStack.navigate(Routes.Login, clearStack = true)
        }
    }

    LaunchedEffect(isPublicRoute) {
        if (isPublicRoute) drawerState.close()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Los gestos solo funcionan con el drawer abierto (para cerrarlo); se abre con el botón del menú,
        // así deslizar sobre el mapa no lo abre
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        UserPortrait(
                            user = profile.user ?: UserInfo(
                                displayName = currentUser?.email
                                    ?: stringResource(R.string.nav_host_user_fallback_text),
                            ),
                            email = currentUser?.email,
                            lastLogin = currentUser?.metadata?.lastSignInTimestamp?.let(DateUtils::format) ?: stringResource(R.string.nav_host_last_login_fallback_text),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        NavigationDrawerItem(
                            label = { Text(text = stringResource(R.string.nav_host_profile_item)) },
                            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                            selected = backStack.lastOrNull() == Routes.Profile,
                            onClick = {
                                backStack.navigate(Routes.Profile)
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                    HorizontalDivider()
                    TextButton(
                        onClick = {
                            // Primero se marca al usuario fuera de línea, mientras la sesión sigue activa
                            locationViewModel.stopLocationUpdates()
                            locationViewModel.clearRoute()
                            val uid = currentUser?.uid
                            if (uid != null) firestoreViewModel.goOffline(uid) { authViewModel.logout() }
                            else authViewModel.logout()
                            scope.launch { drawerState.close() }
                            backStack.navigate(Routes.Login, clearStack = true)
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(vertical = 8.dp)
                    ) {
                        Text(text = stringResource(R.string.nav_host_logout_button))
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null
                        )
                    }
                }
            }
        },
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                AnimatedVisibility(
                    backStack.lastOrNull() != Routes.Login &&
                            backStack.lastOrNull() != Routes.Register &&
                            backStack.lastOrNull() != Routes.ForgotPassword,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { -it }
                ) {
                    TopAppBar(
                        title = { },
                        navigationIcon = {
                            if (backStack.lastOrNull() == Routes.Profile) {
                                // En el perfil se vuelve a la pantalla principal
                                IconButton(onClick = { backStack.removeLastOrNull() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.nav_host_back_icon)
                                    )
                                }
                            } else {
                                IconButton(onClick = {
                                    scope.launch {
                                        drawerState.apply {
                                            if (isClosed) open() else close()
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = stringResource(R.string.nav_host_open_menu_icon)
                                    )
                                }
                            }
                        },
                        actions = {
                            if (backStack.lastOrNull() != Routes.Profile) {
                                IconButton(onClick = { backStack.navigate(Routes.Profile) }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = stringResource(R.string.nav_host_open_profile_icon)
                                    )
                                }
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )
                }
            },
        ) { paddingValues ->
            NavDisplay(
                modifier = Modifier.padding(paddingValues),
                backStack = backStack,
                onBack = {
                    backStack.removeLastOrNull()
                },
                transitionSpec = {
                    slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it })
                },
                predictivePopTransitionSpec = {
                    slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(
                        targetOffsetX = { it })
                },
                popTransitionSpec = {
                    slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(
                        targetOffsetX = { it })
                },
                entryProvider = { key ->
                    when (key) {
                        is Routes.Login -> NavEntry(key) {
                            LoginScreen(
                                goHome = {
                                    backStack.navigate(Routes.Home, clearStack = true)
                                },
                                goRegister = { backStack.navigate(Routes.Register) },
                                goForgotPassword = { backStack.navigate(Routes.ForgotPassword) },
                                viewModel = authViewModel
                            )
                        }

                        is Routes.Home -> NavEntry(key) {
                            HomeScreen()
                        }

                        is Routes.Register -> NavEntry(key) {
                            RegisterScreen(
                                goHome = { backStack.navigate(Routes.Home, clearStack = true) },
                                authViewModel = authViewModel
                            )
                        }

                        is Routes.ForgotPassword -> NavEntry(key) {
                            ForgotPasswordScreen()
                        }

                        is Routes.Profile -> NavEntry(key) {
                            ProfileScreen(viewModel = authViewModel)
                        }

                        else -> {
                            error("Unknown route: $key")
                        }
                    }
                }
            )
        }
    }
}