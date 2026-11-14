package com.example.trdial.navigation

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.trdial.calling.CallingMode
import com.example.trdial.ui.screens.AboutDeveloperScreen
import com.example.trdial.ui.screens.ActiveCallScreen
import com.example.trdial.ui.screens.CallHistoryScreen
import com.example.trdial.ui.screens.ContactsScreen
import com.example.trdial.ui.screens.DialPadScreen
import com.example.trdial.ui.screens.FavoritesScreen
import com.example.trdial.ui.screens.HomeDashboardScreen
import com.example.trdial.ui.screens.IncomingCallScreen
import com.example.trdial.ui.screens.LoginScreen
import com.example.trdial.ui.screens.OnboardingScreen
import com.example.trdial.ui.screens.ProfileScreen
import com.example.trdial.ui.screens.RegistrationScreen
import com.example.trdial.ui.screens.SettingsScreen
import com.example.trdial.ui.screens.SipAccountManagerScreen
import com.example.trdial.ui.screens.SipConfigurationScreen
import com.example.trdial.ui.screens.SplashScreen
import com.example.trdial.viewmodel.AuthViewModel
import com.example.trdial.viewmodel.ContactsHistoryViewModel
import com.example.trdial.viewmodel.DialerViewModel
import com.example.trdial.viewmodel.SipAccountsSettingsViewModel
import com.example.ui.theme.TrBackground
import com.example.ui.theme.TrBlue
import com.example.ui.theme.TrCyan
import com.example.ui.theme.TrSurface
import com.example.ui.theme.TrTextPrimary
import com.example.ui.theme.TrTextSecondary
import java.util.Locale

object TrRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val DIAL_PAD = "dial_pad"
    const val CONTACTS = "contacts"
    const val FAVORITES = "favorites"
    const val HISTORY = "history"
    const val INCOMING_CALL = "incoming_call"
    const val ACTIVE_CALL = "active_call"
    const val SIP_MANAGER = "sip_manager"
    const val SIP_CONFIG = "sip_config"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val ABOUT_DEVELOPER = "about_developer"
}

private data class TopLevelNavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val tag: String
)

private val COMPACT_NAV_ITEMS = listOf(
    TopLevelNavItem(TrRoutes.HOME, R.string.nav_home, Icons.Rounded.Home, "nav_home"),
    TopLevelNavItem(TrRoutes.DIAL_PAD, R.string.nav_dial_pad, Icons.Rounded.Dialpad, "nav_dial_pad"),
    TopLevelNavItem(TrRoutes.CONTACTS, R.string.nav_contacts, Icons.Rounded.Contacts, "nav_contacts"),
    TopLevelNavItem(TrRoutes.FAVORITES, R.string.nav_favorites, Icons.Rounded.Star, "nav_favorites"),
    TopLevelNavItem(TrRoutes.HISTORY, R.string.nav_history, Icons.Rounded.History, "nav_history")
)

private val RAIL_NAV_ITEMS = COMPACT_NAV_ITEMS + listOf(
    TopLevelNavItem(TrRoutes.SETTINGS, R.string.nav_settings, Icons.Rounded.Settings, "nav_settings")
)

@Composable
fun TrDialApp(
    authViewModel: AuthViewModel,
    dialerViewModel: DialerViewModel,
    contactsHistoryViewModel: ContactsHistoryViewModel,
    sipSettingsViewModel: SipAccountsSettingsViewModel
) {
    val userSettingsNullable by authViewModel.userSettings.collectAsStateWithLifecycle()
    val settings by sipSettingsViewModel.settings.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val authUiState by authViewModel.authUiState.collectAsStateWithLifecycle()

    val isOnline by dialerViewModel.isOnline.collectAsStateWithLifecycle()
    val sipAccounts by dialerViewModel.sipAccounts.collectAsStateWithLifecycle()
    val sipRegistrationState by dialerViewModel.sipRegistrationState.collectAsStateWithLifecycle()
    val dialInput by dialerViewModel.dialInput.collectAsStateWithLifecycle()
    val callingMode by dialerViewModel.callingMode.collectAsStateWithLifecycle()
    val selectedSipAccountId by dialerViewModel.selectedSipAccountId.collectAsStateWithLifecycle()
    val dialerNotice by dialerViewModel.notice.collectAsStateWithLifecycle()
    val previewIncomingCall by dialerViewModel.previewIncomingCall.collectAsStateWithLifecycle()
    val activeCallSession by dialerViewModel.activeCallSession.collectAsStateWithLifecycle()

    val searchQuery by contactsHistoryViewModel.searchQuery.collectAsStateWithLifecycle()
    val contacts by contactsHistoryViewModel.contacts.collectAsStateWithLifecycle()
    val favorites by contactsHistoryViewModel.favorites.collectAsStateWithLifecycle()
    val recentCalls by contactsHistoryViewModel.recentCalls.collectAsStateWithLifecycle()
    val allCalls by contactsHistoryViewModel.allCalls.collectAsStateWithLifecycle()
    val sipConfigStatus by sipSettingsViewModel.sipConfigStatus.collectAsStateWithLifecycle()

    val baseContext = LocalContext.current
    val localizedContext = remember(baseContext, settings.languageCode) {
        if (settings.languageCode == "en") baseContext
        else baseContext.withLocale(settings.languageCode)
    }
    val layoutDirection = if (settings.languageCode == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalLayoutDirection provides layoutDirection
    ) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        // Automatically bring up Incoming / Active Call screens when preview state changes
        LaunchedEffect(previewIncomingCall) {
            if (previewIncomingCall != null && currentRoute != TrRoutes.INCOMING_CALL) {
                navController.navigate(TrRoutes.INCOMING_CALL) { launchSingleTop = true }
            }
        }
        LaunchedEffect(activeCallSession) {
            if (activeCallSession != null && currentRoute != TrRoutes.ACTIVE_CALL) {
                navController.navigate(TrRoutes.ACTIVE_CALL) { launchSingleTop = true }
            }
        }

        val showTopLevelNav = currentRoute in setOf(
            TrRoutes.HOME,
            TrRoutes.DIAL_PAD,
            TrRoutes.CONTACTS,
            TrRoutes.FAVORITES,
            TrRoutes.HISTORY
        )

        fun navigateTopLevel(route: String) {
            navController.navigate(route) {
                popUpTo(TrRoutes.HOME) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Scaffold(
                containerColor = TrBackground,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (showTopLevelNav && !useRail) {
                        NavigationBar(containerColor = TrSurface) {
                            COMPACT_NAV_ITEMS.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(item.route) },
                                    icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                                    label = { Text(stringResource(item.labelRes), maxLines = 1) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = TrCyan,
                                        selectedTextColor = TrCyan,
                                        indicatorColor = TrBlue.copy(alpha = 0.22f),
                                        unselectedIconColor = TrTextSecondary,
                                        unselectedTextColor = TrTextSecondary
                                    ),
                                    modifier = Modifier.testTag(item.tag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (showTopLevelNav && useRail) {
                        NavigationRail(containerColor = TrSurface) {
                            RAIL_NAV_ITEMS.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = {
                                        if (item.route == TrRoutes.SETTINGS) {
                                            navController.navigate(TrRoutes.SETTINGS)
                                        } else {
                                            navigateTopLevel(item.route)
                                        }
                                    },
                                    icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                                    label = { Text(stringResource(item.labelRes)) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = TrCyan,
                                        selectedTextColor = TrCyan,
                                        indicatorColor = TrBlue.copy(alpha = 0.22f),
                                        unselectedIconColor = TrTextSecondary,
                                        unselectedTextColor = TrTextSecondary
                                    ),
                                    modifier = Modifier.testTag(item.tag)
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = TrRoutes.SPLASH
                        ) {
                            // 1. Splash Screen
                            composable(TrRoutes.SPLASH) {
                                SplashScreen(
                                    userSettings = userSettingsNullable,
                                    onNavigateToOnboarding = {
                                        navController.navigate(TrRoutes.ONBOARDING) {
                                            popUpTo(TrRoutes.SPLASH) { inclusive = true }
                                        }
                                    },
                                    onNavigateToLogin = {
                                        navController.navigate(TrRoutes.LOGIN) {
                                            popUpTo(TrRoutes.SPLASH) { inclusive = true }
                                        }
                                    },
                                    onNavigateToHome = {
                                        navController.navigate(TrRoutes.HOME) {
                                            popUpTo(TrRoutes.SPLASH) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // 2. Onboarding
                            composable(TrRoutes.ONBOARDING) {
                                OnboardingScreen(
                                    onFinishOnboarding = {
                                        authViewModel.completeOnboarding()
                                        navController.navigate(TrRoutes.LOGIN) {
                                            popUpTo(TrRoutes.ONBOARDING) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // 3. Login
                            composable(TrRoutes.LOGIN) {
                                LoginScreen(
                                    authUiState = authUiState,
                                    onClearError = authViewModel::clearError,
                                    onLogin = { email, password ->
                                        authViewModel.login(email, password) {
                                            navController.navigate(TrRoutes.HOME) {
                                                popUpTo(TrRoutes.LOGIN) { inclusive = true }
                                            }
                                        }
                                    },
                                    onNavigateToRegister = {
                                        authViewModel.clearError()
                                        navController.navigate(TrRoutes.REGISTER)
                                    }
                                )
                            }

                            // 4. Registration
                            composable(TrRoutes.REGISTER) {
                                RegistrationScreen(
                                    authUiState = authUiState,
                                    onClearError = authViewModel::clearError,
                                    onRegister = { fullName, email, ext, password, confirm ->
                                        authViewModel.register(fullName, email, ext, password, confirm) {
                                            navController.navigate(TrRoutes.HOME) {
                                                popUpTo(TrRoutes.LOGIN) { inclusive = true }
                                            }
                                        }
                                    },
                                    onNavigateToLogin = {
                                        authViewModel.clearError()
                                        navController.popBackStack()
                                    }
                                )
                            }

                            // 5. Home Dashboard
                            composable(TrRoutes.HOME) {
                                HomeDashboardScreen(
                                    currentUser = currentUser,
                                    isOnline = isOnline,
                                    sipAccounts = sipAccounts,
                                    sipRegistrationState = sipRegistrationState,
                                    favorites = favorites,
                                    recentCalls = recentCalls,
                                    onOpenDialPad = { navigateTopLevel(TrRoutes.DIAL_PAD) },
                                    onOpenContacts = { navigateTopLevel(TrRoutes.CONTACTS) },
                                    onOpenFavorites = { navigateTopLevel(TrRoutes.FAVORITES) },
                                    onOpenHistory = { navigateTopLevel(TrRoutes.HISTORY) },
                                    onOpenSipManager = { navController.navigate(TrRoutes.SIP_MANAGER) },
                                    onOpenProfile = { navController.navigate(TrRoutes.PROFILE) },
                                    onOpenSettings = { navController.navigate(TrRoutes.SETTINGS) },
                                    onQuickCallContact = { contact ->
                                        val target = contact.sipUri.ifBlank {
                                            contact.phoneNumber.ifBlank { contact.appPeerId }
                                        }
                                        dialerViewModel.setDialInput(target)
                                        navigateTopLevel(TrRoutes.DIAL_PAD)
                                    },
                                    onRedialHistory = { entry ->
                                        dialerViewModel.setDialInput(entry.targetAddress)
                                        dialerViewModel.setCallingMode(CallingMode.fromKey(entry.callingMode))
                                        navigateTopLevel(TrRoutes.DIAL_PAD)
                                    }
                                )
                            }

                            // 6. Dial Pad
                            composable(TrRoutes.DIAL_PAD) {
                                DialPadScreen(
                                    dialInput = dialInput,
                                    callingMode = callingMode,
                                    sipAccounts = sipAccounts,
                                    selectedSipAccountId = selectedSipAccountId,
                                    notice = dialerNotice,
                                    onInputChange = dialerViewModel::setDialInput,
                                    onAppendDigit = dialerViewModel::appendDigit,
                                    onBackspace = dialerViewModel::backspace,
                                    onClearInput = dialerViewModel::clearDialInput,
                                    onSelectMode = dialerViewModel::setCallingMode,
                                    onSelectSipAccount = dialerViewModel::selectSipAccount,
                                    onCallClick = { dialerViewModel.initiateCall() },
                                    onDismissNotice = dialerViewModel::dismissNotice,
                                    onOpenSipConfig = { navController.navigate(TrRoutes.SIP_CONFIG) },
                                    onPreviewActiveCall = { dialerViewModel.openActiveCallPreview() },
                                    onPreviewIncomingCall = { dialerViewModel.openIncomingCallPreview() }
                                )
                            }

                            // 7. Contacts
                            composable(TrRoutes.CONTACTS) {
                                ContactsScreen(
                                    contacts = contacts,
                                    searchQuery = searchQuery,
                                    onSearchChange = contactsHistoryViewModel::updateSearchQuery,
                                    onSaveContact = contactsHistoryViewModel::saveContact,
                                    onDeleteContact = contactsHistoryViewModel::deleteContact,
                                    onToggleFavorite = contactsHistoryViewModel::toggleFavorite,
                                    onCallContact = { contact, mode ->
                                        val target = if (mode == CallingMode.APP_TO_APP) {
                                            contact.appPeerId.ifBlank { contact.phoneNumber }
                                        } else {
                                            contact.sipUri.ifBlank { contact.phoneNumber }
                                        }
                                        dialerViewModel.setDialInput(target)
                                        dialerViewModel.setCallingMode(mode)
                                        dialerViewModel.initiateCall(target, contact.name, mode)
                                        navigateTopLevel(TrRoutes.DIAL_PAD)
                                    }
                                )
                            }

                            // 8. Favorites
                            composable(TrRoutes.FAVORITES) {
                                FavoritesScreen(
                                    favorites = favorites,
                                    onToggleFavorite = contactsHistoryViewModel::toggleFavorite,
                                    onCallContact = { contact, mode ->
                                        val target = if (mode == CallingMode.APP_TO_APP) {
                                            contact.appPeerId.ifBlank { contact.phoneNumber }
                                        } else {
                                            contact.sipUri.ifBlank { contact.phoneNumber }
                                        }
                                        dialerViewModel.setDialInput(target)
                                        dialerViewModel.setCallingMode(mode)
                                        dialerViewModel.initiateCall(target, contact.name, mode)
                                        navigateTopLevel(TrRoutes.DIAL_PAD)
                                    }
                                )
                            }

                            // 9. Call History
                            composable(TrRoutes.HISTORY) {
                                CallHistoryScreen(
                                    calls = allCalls,
                                    onRedial = { entry ->
                                        val mode = CallingMode.fromKey(entry.callingMode)
                                        dialerViewModel.setDialInput(entry.targetAddress)
                                        dialerViewModel.setCallingMode(mode)
                                        dialerViewModel.initiateCall(entry.targetAddress, entry.contactName, mode)
                                        navigateTopLevel(TrRoutes.DIAL_PAD)
                                    },
                                    onClearHistory = contactsHistoryViewModel::clearCallHistory
                                )
                            }

                            // 10. Incoming Call
                            composable(TrRoutes.INCOMING_CALL) {
                                IncomingCallScreen(
                                    incomingCall = previewIncomingCall,
                                    onAnswer = {
                                        dialerViewModel.answerPreviewIncomingCall()
                                    },
                                    onReject = {
                                        dialerViewModel.rejectPreviewIncomingCall()
                                        navController.popBackStack()
                                    }
                                )
                            }

                            // 11. Active Call
                            composable(TrRoutes.ACTIVE_CALL) {
                                ActiveCallScreen(
                                    session = activeCallSession,
                                    onToggleMute = dialerViewModel::toggleMute,
                                    onToggleSpeaker = dialerViewModel::toggleSpeaker,
                                    onToggleHold = dialerViewModel::toggleHold,
                                    onSendDtmf = dialerViewModel::sendDtmf,
                                    onEndCall = {
                                        dialerViewModel.endActiveCall()
                                        navController.popBackStack()
                                    }
                                )
                            }

                            // 12. SIP Account Manager
                            composable(TrRoutes.SIP_MANAGER) {
                                SipAccountManagerScreen(
                                    accounts = sipAccounts,
                                    registrationState = sipRegistrationState,
                                    configStatus = sipConfigStatus,
                                    onBack = { navController.popBackStack() },
                                    onAddAccount = {
                                        sipSettingsViewModel.clearStatus()
                                        navController.navigate(TrRoutes.SIP_CONFIG)
                                    },
                                    onEditAccount = { account ->
                                        sipSettingsViewModel.clearStatus()
                                        navController.navigate("${TrRoutes.SIP_CONFIG}?accountId=${account.id}")
                                    },
                                    onSetDefault = sipSettingsViewModel::setDefaultAccount,
                                    onTestRegistration = sipSettingsViewModel::testRegistration,
                                    onDeleteAccount = sipSettingsViewModel::deleteAccount,
                                    onClearNotice = sipSettingsViewModel::clearStatus
                                )
                            }

                            // 13. SIP Configuration
                            composable(
                                route = "${TrRoutes.SIP_CONFIG}?accountId={accountId}",
                                arguments = listOf(
                                    navArgument("accountId") {
                                        type = NavType.LongType
                                        defaultValue = 0L
                                    }
                                )
                            ) { entry ->
                                val editId = entry.arguments?.getLong("accountId") ?: 0L
                                val editingAccount = sipAccounts.firstOrNull { it.id == editId }
                                SipConfigurationScreen(
                                    editingAccount = editingAccount,
                                    configStatus = sipConfigStatus,
                                    onBack = { navController.popBackStack() },
                                    onSaveAccount = { id, display, user, domain, portText, transport, proxy, stun, pass, makeDefault ->
                                        sipSettingsViewModel.saveAndTestSipAccount(
                                            editingId = id,
                                            displayName = display,
                                            username = user,
                                            domain = domain,
                                            portText = portText,
                                            transport = transport,
                                            outboundProxy = proxy,
                                            stunServer = stun,
                                            password = pass,
                                            makeDefault = makeDefault,
                                            onSaved = { navController.popBackStack() }
                                        )
                                    }
                                )
                            }

                            // 14. Profile
                            composable(TrRoutes.PROFILE) {
                                ProfileScreen(
                                    currentUser = currentUser,
                                    sipAccounts = sipAccounts,
                                    onBack = { navController.popBackStack() },
                                    onUpdateProfile = authViewModel::updateProfile,
                                    onOpenSipManager = { navController.navigate(TrRoutes.SIP_MANAGER) },
                                    onSignOut = {
                                        authViewModel.signOut {
                                            navController.navigate(TrRoutes.LOGIN) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                    }
                                )
                            }

                            // 15. Settings
                            composable(TrRoutes.SETTINGS) {
                                SettingsScreen(
                                    settings = settings,
                                    onBack = { navController.popBackStack() },
                                    onLanguageSelected = sipSettingsViewModel::setLanguage,
                                    onPreferredModeSelected = sipSettingsViewModel::setPreferredCallingMode,
                                    onEchoCancellationChanged = sipSettingsViewModel::setEchoCancellation,
                                    onNoiseSuppressionChanged = sipSettingsViewModel::setNoiseSuppression,
                                    onProximityChanged = sipSettingsViewModel::setAutoProximitySpeaker,
                                    onWifiOnlyChanged = sipSettingsViewModel::setWifiOnlyCalls,
                                    onPreviewIncomingCall = { dialerViewModel.openIncomingCallPreview() },
                                    onPreviewActiveCall = { dialerViewModel.openActiveCallPreview() },
                                    onOpenAboutDeveloper = { navController.navigate(TrRoutes.ABOUT_DEVELOPER) }
                                )
                            }

                            // 16. About Developer
                            composable(TrRoutes.ABOUT_DEVELOPER) {
                                AboutDeveloperScreen(onBack = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Context.withLocale(languageCode: String): Context {
    val locale = Locale.forLanguageTag(languageCode)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}
