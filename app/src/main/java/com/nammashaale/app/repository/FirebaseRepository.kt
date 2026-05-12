package com.nammashaale.app.repository

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.nammashaale.app.data.Asset
import com.nammashaale.app.data.AssetDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseRepository(
    private val dao: AssetDao,
    private val schoolId: String = "school_001"
) {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val assetsRef = db.collection("schools").document(schoolId).collection("assets")
    private var listenerReg: ListenerRegistration? = null
    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // ─── Real-time Firestore → Room sync ─────────────────────────────────
    fun startSync() {
        listenerReg = assetsRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            repoScope.launch {
                snapshot.documents.forEach { doc ->
                    val data = doc.data ?: return@forEach
                    val existing = dao.getByFirestoreId(doc.id)
                    val asset = Asset(
                        id = existing?.id ?: 0,
                        firestoreId = doc.id,
                        name = data["name"] as? String ?: "",
                        serialNumber = data["serialNumber"] as? String ?: "",
                        location = data["location"] as? String ?: "",
                        condition = data["condition"] as? String ?: "Working",
                        photoUrl = data["photoUrl"] as? String,
                        issueNote = data["issueNote"] as? String,
                        category = data["category"] as? String ?: "General",
                        lastChecked = (data["lastChecked"] as? com.google.firebase.Timestamp)
                            ?.toDate()?.time ?: System.currentTimeMillis()
                    )
                    dao.insertAsset(asset)
                }
            }
        }
    }

    fun stopSync() {
        listenerReg?.remove()
        repoScope.cancel()
    }

    // ─── Add asset ────────────────────────────────────────────────────────
    suspend fun addAsset(asset: Asset): Result<String> {
        return try {
            val map = asset.toMap()
            val docRef = assetsRef.add(map).await()
            val withId = asset.copy(firestoreId = docRef.id)
            dao.insertAsset(withId)
            Result.success(docRef.id)
        } catch (e: Exception) {
            dao.insertAsset(asset) // save locally even if offline
            Result.failure(e)
        }
    }

    // ─── Update condition ─────────────────────────────────────────────────
    suspend fun updateAsset(asset: Asset) {
        try {
            if (asset.firestoreId.isNotEmpty()) {
                val updates = hashMapOf<String, Any>(
                    "condition" to asset.condition,
                    "issueNote" to (asset.issueNote ?: ""),
                    "lastChecked" to FieldValue.serverTimestamp()
                )
                assetsRef.document(asset.firestoreId).update(updates).await()
            }
        } catch (_: Exception) {}
        dao.updateAsset(asset)
    }

    // ─── Delete asset ─────────────────────────────────────────────────────
    suspend fun deleteAsset(asset: Asset) {
        try {
            if (asset.firestoreId.isNotEmpty()) {
                assetsRef.document(asset.firestoreId).delete().await()
            }
        } catch (_: Exception) {}
        dao.deleteAsset(asset)
    }

    // ─── Upload photo to Firebase Storage ────────────────────────────────
    suspend fun uploadPhoto(localUri: Uri, firestoreId: String): String? {
        return try {
            val ref = storage.reference.child("schools/$schoolId/assets/$firestoreId.jpg")
            ref.putFile(localUri).await()
            val url = ref.downloadUrl.await().toString()
            // Update Firestore with the photo URL
            assetsRef.document(firestoreId).update("photoUrl", url).await()
            url
        } catch (e: Exception) {
            null
        }
    }
}

fun Asset.toMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "serialNumber" to serialNumber,
    "location" to location,
    "condition" to condition,
    "photoUrl" to photoUrl,
    "issueNote" to issueNote,
    "category" to category,
    "lastChecked" to FieldValue.serverTimestamp()
)
