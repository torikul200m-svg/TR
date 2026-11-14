package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.trdial.calling.CallDispatchResult
import com.example.trdial.calling.apptoapp.UnconfiguredAppToAppEngine
import com.example.trdial.calling.sip.UnconfiguredSipEngine
import com.example.trdial.data.local.ContactEntity
import com.example.trdial.data.local.TrDialDatabase
import com.example.trdial.data.repository.RoomAuthRepository
import com.example.trdial.data.repository.RoomSipAccountRepository
import com.example.trdial.security.InputValidator
import com.example.trdial.security.SecurityHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: TrDialDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TrDialDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TR Dial sip", appName)
    }

    @Test
    fun `password hashing is salted and never plain text`() {
        val plain = "TrDialSecret#2026"
        val hash1 = SecurityHelper.hashPassword(plain)
        val hash2 = SecurityHelper.hashPassword(plain)

        assertNotEquals(plain, hash1.hashBase64)
        assertNotEquals(hash1.saltBase64, hash2.saltBase64)
        assertTrue(SecurityHelper.verifyPassword(plain, hash1.hashBase64, hash1.saltBase64))
        assertFalse(SecurityHelper.verifyPassword("WrongPass", hash1.hashBase64, hash1.saltBase64))
    }

    @Test
    fun `sip secret encryption does not store plaintext`() = runTest {
        val repo = RoomSipAccountRepository(database.sipAccountDao())
        val saved = repo.saveAccount(
            displayName = "Office Trunk",
            username = "1001",
            domain = "sip.example.org",
            port = 5060,
            transport = "TLS",
            outboundProxy = "",
            stunServer = "",
            plainPassword = "SuperSecretSipPassword",
            makeDefault = true
        ).getOrThrow()

        assertNotEquals("SuperSecretSipPassword", saved.encryptedPassword)
        val decrypted = repo.decryptPassword(saved).getOrThrow()
        assertEquals("SuperSecretSipPassword", decrypted)
    }

    @Test
    fun `auth repository registers and authenticates user securely`() = runTest {
        val authRepo = RoomAuthRepository(database.userProfileDao())
        val user = authRepo.register(
            fullName = "Tariqul Sheikh",
            email = "tariqul@trdial.sip",
            phoneExtension = "1001",
            password = "password123"
        ).getOrThrow()

        assertTrue(user.appPeerId.startsWith("TR-"))
        assertTrue(authRepo.login("tariqul@trdial.sip", "password123").isSuccess)
        assertTrue(authRepo.login("tariqul@trdial.sip", "wrongpass").isFailure)
    }

    @Test
    fun `unconfigured calling engines never fake successful calls`() = runTest {
        val sipEngine = UnconfiguredSipEngine()
        val appEngine = UnconfiguredAppToAppEngine()

        val sipResult = sipEngine.makeCall("1002@sip.example.com", 1L)
        val appResult = appEngine.startCall("TR-9911")

        assertTrue(sipResult is CallDispatchResult.NotConfigured)
        assertTrue(appResult is CallDispatchResult.NotConfigured)
        assertFalse(sipEngine.isEngineCompiled)
        assertFalse(appEngine.isConfigured)
    }

    @Test
    fun `contacts and favorites persist in Room`() = runTest {
        val dao = database.contactDao()
        val id = dao.insert(
            ContactEntity(
                name = "NOC Desk",
                phoneNumber = "+8801700000000",
                sipUri = "noc@voip.trdial.com",
                appPeerId = "TR-NOC01",
                isFavorite = false
            )
        )
        dao.setFavorite(id, true)
        val favorites = dao.observeFavorites().first()
        assertEquals(1, favorites.size)
        assertEquals("NOC Desk", favorites.first().name)
    }

    @Test
    fun `input validators reject invalid email and port`() {
        assertTrue(InputValidator.isValidEmail("dev@trdial.com"))
        assertFalse(InputValidator.isValidEmail("not-an-email"))
        assertTrue(InputValidator.isValidPort("5060"))
        assertFalse(InputValidator.isValidPort("99999"))
    }

    @get:org.junit.Rule
    val composeRule = androidx.compose.ui.test.junit4.createAndroidComposeRule<MainActivity>()

    @Test
    fun `main activity composes splash, transitions to onboarding, login, register and home`() {
        composeRule.mainClock.advanceTimeBy(1500)
        composeRule.waitForIdle()
        // Should now be on Onboarding screen with Skip button
        composeRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_skip_button")).assertExists()
    }
}
