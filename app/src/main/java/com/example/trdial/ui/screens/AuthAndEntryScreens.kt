package com.example.trdial.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SettingsInputAntenna
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.trdial.data.local.UserSettings
import com.example.trdial.ui.components.TelecomCard
import com.example.trdial.ui.components.TrDialLogoBadge
import com.example.trdial.viewmodel.AuthUiState
import com.example.ui.theme.TrBackground
import com.example.ui.theme.TrBlue
import com.example.ui.theme.TrBorder
import com.example.ui.theme.TrCard
import com.example.ui.theme.TrCyan
import com.example.ui.theme.TrGreen
import com.example.ui.theme.TrRed
import com.example.ui.theme.TrSurface
import com.example.ui.theme.TrTextPrimary
import com.example.ui.theme.TrTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    userSettings: UserSettings?,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val scale = remember { Animatable(0.84f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, tween(550, easing = FastOutSlowInEasing)) }
        launch { alpha.animateTo(1f, tween(550)) }
    }

    LaunchedEffect(userSettings) {
        if (userSettings != null) {
            delay(950)
            when {
                !userSettings.onboardingCompleted -> onNavigateToOnboarding()
                userSettings.loggedInUserId == null -> onNavigateToLogin()
                else -> onNavigateToHome()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF13254A), TrBackground)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TrDialLogoBadge(size = 88.dp, showWordmark = false)
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "TR Dial ",
                    style = MaterialTheme.typography.displayLarge,
                    color = TrTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "sip",
                    style = MaterialTheme.typography.displayLarge,
                    color = TrCyan,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = TrTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(36.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = TrCyan,
                strokeWidth = 2.5.dp
            )
        }

        Text(
            text = stringResource(R.string.developer_credit),
            style = MaterialTheme.typography.labelMedium,
            color = TrTextSecondary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        )
    }
}

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val pages = listOf(
        Triple(
            stringResource(R.string.onboarding_title_1),
            stringResource(R.string.onboarding_desc_1),
            Icons.Rounded.Phone
        ),
        Triple(
            stringResource(R.string.onboarding_title_2),
            stringResource(R.string.onboarding_desc_2),
            Icons.Rounded.SettingsInputAntenna
        ),
        Triple(
            stringResource(R.string.onboarding_title_3),
            stringResource(R.string.onboarding_desc_3),
            Icons.Rounded.Security
        )
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrDialLogoBadge(size = 42.dp, showWordmark = true)
            TextButton(
                onClick = onFinishOnboarding,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(text = stringResource(R.string.action_skip), color = TrCyan)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_telecom_hero),
                contentDescription = "Telecom network illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, TrBackground.copy(alpha = 0.72f))
                        )
                    )
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
        ) { page ->
            val (title, desc, icon) = pages[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = TrBlue.copy(alpha = 0.16f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = TrCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TrTextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TrTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) { index ->
                val active = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(if (active) 28.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (active) TrCyan else TrBorder)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val isLast = pagerState.currentPage == pages.lastIndex
        Button(
            onClick = {
                if (isLast) onFinishOnboarding()
                else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_next_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TrBlue)
        ) {
            Text(
                text = stringResource(if (isLast) R.string.action_get_started else R.string.action_next),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun LoginScreen(
    authUiState: AuthUiState,
    onClearError: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        TrDialLogoBadge(size = 58.dp, showWordmark = true)
        Spacer(modifier = Modifier.height(28.dp))

        TelecomCard(
            modifier = Modifier.widthIn(max = 520.dp),
            containerColor = TrSurface
        ) {
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
                color = TrTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TrTextSecondary
            )
            Spacer(modifier = Modifier.height(20.dp))

            AuthErrorBanner(errorCode = authUiState.errorCode)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; onClearError() },
                label = { Text(stringResource(R.string.field_email)) },
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = TrCyan) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                colors = trTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; onClearError() },
                label = { Text(stringResource(R.string.field_password)) },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = TrCyan) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = "Toggle password visibility",
                            tint = TrTextSecondary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                colors = trTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input")
            )

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = { onLogin(email, password) },
                enabled = !authUiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_submit_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TrBlue)
            ) {
                if (authUiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.btn_sign_in), style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("login_register_link"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.link_no_account), color = TrCyan)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.developer_credit),
            style = MaterialTheme.typography.labelMedium,
            color = TrTextSecondary
        )
    }
}

@Composable
fun RegistrationScreen(
    authUiState: AuthUiState,
    onClearError: () -> Unit,
    onRegister: (fullName: String, email: String, extension: String, password: String, confirm: String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneExtension by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        TrDialLogoBadge(size = 52.dp, showWordmark = true)
        Spacer(modifier = Modifier.height(20.dp))

        TelecomCard(
            modifier = Modifier.widthIn(max = 520.dp),
            containerColor = TrSurface
        ) {
            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.headlineMedium,
                color = TrTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TrTextSecondary
            )
            Spacer(modifier = Modifier.height(18.dp))

            AuthErrorBanner(errorCode = authUiState.errorCode)

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; onClearError() },
                label = { Text(stringResource(R.string.field_full_name)) },
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = TrCyan) },
                singleLine = true,
                colors = trTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_name_input")
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; onClearError() },
                label = { Text(stringResource(R.string.field_email)) },
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = TrCyan) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = trTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_email_input")
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phoneExtension,
                onValueChange = { phoneExtension = it; onClearError() },
                label = { Text(stringResource(R.string.field_extension)) },
                leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null, tint = TrCyan) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = trTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_extension_input")
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; onClearError() },
                label = { Text(stringResource(R.string.field_password)) },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = TrCyan) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = "Toggle password",
                            tint = TrTextSecondary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = trTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_password_input")
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; onClearError() },
                label = { Text(stringResource(R.string.field_confirm_password)) },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = TrGreen) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = trTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_confirm_password_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onRegister(fullName, email, phoneExtension, password, confirmPassword) },
                enabled = !authUiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("register_submit_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TrBlue)
            ) {
                if (authUiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.btn_create_account), style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.link_have_account),
                style = MaterialTheme.typography.labelLarge,
                color = TrCyan,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onNavigateToLogin)
                    .padding(vertical = 12.dp)
                    .testTag("register_login_link")
            )
        }
    }
}

@Composable
private fun AuthErrorBanner(errorCode: String?) {
    if (errorCode == null) return
    val message = when (errorCode) {
        "INVALID_EMAIL" -> stringResource(R.string.error_invalid_email)
        "SHORT_PASSWORD" -> stringResource(R.string.error_short_password)
        "PASSWORD_MISMATCH" -> stringResource(R.string.error_password_mismatch)
        "REQUIRED_FIELD" -> stringResource(R.string.error_required_field)
        "INVALID_CREDENTIALS" -> stringResource(R.string.error_invalid_credentials)
        "EMAIL_EXISTS" -> stringResource(R.string.error_email_exists)
        else -> stringResource(R.string.error_invalid_credentials)
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = TrRed.copy(alpha = 0.14f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TrRed,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
fun trTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TrCyan,
    unfocusedBorderColor = TrBorder,
    focusedLabelColor = TrCyan,
    unfocusedLabelColor = TrTextSecondary,
    cursorColor = TrCyan,
    focusedTextColor = TrTextPrimary,
    unfocusedTextColor = TrTextPrimary,
    focusedContainerColor = TrCard.copy(alpha = 0.55f),
    unfocusedContainerColor = TrCard.copy(alpha = 0.35f)
)
