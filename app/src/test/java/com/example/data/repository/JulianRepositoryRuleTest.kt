package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JulianRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveBookmark_authenticatedOwner_succeedsAndEmitsUpdate() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = JulianFirestoreRepository(firestore, auth)
        val toolId = "maker_brat_${UUID.randomUUID().toString().take(8)}"

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveBookmark(toolId = toolId, category = "MAKER", note = "Brat studio preset")
        }
        assertTrue("Expected bookmark save to succeed", saveResult.isSuccess)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeBookmarks(aliceUid).first { list -> list.any { it.toolId == toolId } }
        }
        assertTrue(emitted.any { it.toolId == toolId && it.category == "MAKER" })
    }

    @Test
    fun saveApiConfig_authenticatedOwner_succeedsAndUpdates() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = JulianFirestoreRepository(firestore, auth)

        val firstSave = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveApiConfig(
                baseUrl = "https://api.julian-omnicore.dev/v9",
                apiKeyHeader = "julian-secret-1",
                executionMode = "HYBRID",
                enableHighThinking = true
            )
        }
        assertTrue("Expected initial apiConfig save to succeed", firstSave.isSuccess)

        val secondSave = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveApiConfig(
                baseUrl = "https://custom.julian.io/v9",
                apiKeyHeader = "julian-secret-2",
                executionMode = "LIVE_REST",
                enableHighThinking = false
            )
        }
        assertTrue("Expected apiConfig update to succeed", secondSave.isSuccess)

        val emittedConfig = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeApiConfig(aliceUid).first { it?.executionMode == "LIVE_REST" }
        }
        assertEquals("https://custom.julian.io/v9", emittedConfig?.baseUrl)
    }

    @Test
    fun getBookmarkById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = JulianFirestoreRepository(firestore, auth)
        val toolId = "ai_gemini_${UUID.randomUUID().toString().take(8)}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveBookmark(toolId = toolId, category = "AI", note = "Alice private bookmark").getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = JulianFirestoreRepository(firestore, auth)
        val crossUserResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getBookmarkById(aliceUid, toolId)
        }
        assertTrue("Expected cross-user read to fail", crossUserResult.isFailure)
        val exception = crossUserResult.exceptionOrNull() as? FirebaseFirestoreException
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception?.code)
    }

    @Test
    fun observeBookmarks_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val uncachedUid = "user_${UUID.randomUUID().toString().take(8)}"
        val repository = JulianFirestoreRepository(firestore, auth)

        var caughtException: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeBookmarks(uncachedUid).first()
            }
        } catch (e: Throwable) {
            caughtException = e
        }
        assertTrue("Expected FirebaseFirestoreException, got: $caughtException", caughtException is FirebaseFirestoreException)
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            (caughtException as FirebaseFirestoreException).code
        )
    }

    private companion object {
        const val ALICE_EMAIL = "alice@julian.test"
        const val BOB_EMAIL = "bob@julian.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
