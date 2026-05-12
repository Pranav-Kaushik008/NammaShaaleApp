package com.nammashaale.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nammashaale.app.data.AppDatabase
import com.nammashaale.app.data.Asset
import com.nammashaale.app.repository.FirebaseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssetViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.assetDao()
    private val repo = FirebaseRepository(dao)

    // ─── Flows from Room (offline-first) ──────────────────────────────────
    val assets = dao.getAllAssets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val repairList = dao.getRepairList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workingCount = dao.countByCondition("Working")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val checkCount = dao.countByCondition("Needs Check")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val repairCount = dao.countByCondition("Needs Repair")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCount = dao.countAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ─── Search ───────────────────────────────────────────────────────────
    fun searchAssets(query: String) = dao.searchAssets(query)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        repo.startSync() // begin real-time Firebase → Room sync
    }

    override fun onCleared() {
        super.onCleared()
        repo.stopSync()
    }

    // ─── CRUD operations ─────────────────────────────────────────────────
    fun addAsset(asset: Asset, photoUri: Uri? = null) = viewModelScope.launch {
        val result = repo.addAsset(asset)
        result.onSuccess { firestoreId ->
            photoUri?.let {
                val photoUrl = repo.uploadPhoto(it, firestoreId)
                if (photoUrl != null) {
                    val updated = dao.getByFirestoreId(firestoreId)
                    updated?.let { a -> dao.updateAsset(a.copy(photoUrl = photoUrl)) }
                }
            }
        }
    }

    fun updateAsset(asset: Asset) = viewModelScope.launch {
        repo.updateAsset(asset)
    }

    fun deleteAsset(asset: Asset) = viewModelScope.launch {
        repo.deleteAsset(asset)
    }

    fun getAssetById(id: Int, onResult: (Asset?) -> Unit) = viewModelScope.launch {
        onResult(dao.getAssetById(id))
    }
}
