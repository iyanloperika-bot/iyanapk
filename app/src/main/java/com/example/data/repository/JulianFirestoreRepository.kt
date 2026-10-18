package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.BookmarkedTool
import com.example.data.model.OperationType
import com.example.data.model.UserApiConfig
import com.example.data.model.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class JulianFirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    // CRITICAL: Always initialize with R.string.firestore_database_id
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeBookmarks(userId: String = requireUserId()): Flow<List<BookmarkedTool>> = callbackFlow {
        val path = "users/$userId/bookmarks"
        val registration = db.collection("users").document(userId).collection("bookmarks")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(
                        snapshot.toObjects(
                            BookmarkedTool::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    )
                }
            }
        awaitClose { registration.remove() }
    }.catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path = "users/$userId/bookmarks")
        throw error
    }

    suspend fun getBookmarks(userId: String = requireUserId()): Result<List<BookmarkedTool>> {
        val path = "users/$userId/bookmarks"
        return try {
            val snapshot = db.collection("users").document(userId).collection("bookmarks")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val list = snapshot.toObjects(
                BookmarkedTool::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun saveBookmark(
        toolId: String,
        category: String,
        note: String = ""
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bookmarks").document(toolId)
        return try {
            val existingSnap = docRef.get().await()
            if (existingSnap.exists()) {
                docRef.update(
                    mapOf(
                        "category" to category,
                        "note" to note.take(300),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = mapOf(
                    "toolId" to toolId,
                    "category" to category,
                    "note" to note.take(300),
                    "userId" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(toolId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun removeBookmark(toolId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bookmarks").document(toolId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getBookmarkById(userId: String, toolId: String): Result<BookmarkedTool?> {
        val docRef = db.collection("users").document(userId).collection("bookmarks").document(toolId)
        return try {
            val snap = docRef.get().await()
            Result.success(
                snap.toObject(
                    BookmarkedTool::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    fun observeApiConfig(userId: String = requireUserId()): Flow<UserApiConfig?> = callbackFlow {
        val docRef = db.collection("users").document(userId).collection("settings").document("apiConfig")
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(
                    snapshot.toObject(
                        UserApiConfig::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                )
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }.catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.GET, "users/$userId/settings/apiConfig")
        throw error
    }

    suspend fun saveApiConfig(
        baseUrl: String,
        apiKeyHeader: String,
        executionMode: String,
        enableHighThinking: Boolean
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("settings").document("apiConfig")
        val sanitizedUrl = baseUrl.trim().ifEmpty { "https://api.julian-omnicore.dev/v9" }.take(300)
        val sanitizedKey = apiKeyHeader.trim().take(200)
        val validMode = if (executionMode in listOf("HYBRID", "NATIVE_AI", "LIVE_REST")) executionMode else "HYBRID"

        return try {
            val existingSnap = docRef.get().await()
            if (existingSnap.exists()) {
                docRef.update(
                    mapOf(
                        "baseUrl" to sanitizedUrl,
                        "apiKeyHeader" to sanitizedKey,
                        "executionMode" to validMode,
                        "enableHighThinking" to enableHighThinking,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "baseUrl" to sanitizedUrl,
                        "apiKeyHeader" to sanitizedKey,
                        "executionMode" to validMode,
                        "enableHighThinking" to enableHighThinking,
                        "userId" to uid,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }
}
