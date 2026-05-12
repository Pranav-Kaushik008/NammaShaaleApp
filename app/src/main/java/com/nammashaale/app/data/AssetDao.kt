package com.nammashaale.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    @Query("SELECT * FROM assets ORDER BY lastChecked DESC")
    fun getAllAssets(): Flow<List<Asset>>

    @Query("SELECT * FROM assets WHERE condition = :condition ORDER BY lastChecked DESC")
    fun getByCondition(condition: String): Flow<List<Asset>>

    @Query("SELECT * FROM assets WHERE id = :id LIMIT 1")
    suspend fun getAssetById(id: Int): Asset?

    @Query("SELECT * FROM assets WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getByFirestoreId(firestoreId: String): Asset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: Asset): Long

    @Update
    suspend fun updateAsset(asset: Asset)

    @Delete
    suspend fun deleteAsset(asset: Asset)

    @Query("DELETE FROM assets WHERE firestoreId = :firestoreId")
    suspend fun deleteByFirestoreId(firestoreId: String)

    @Query("SELECT COUNT(*) FROM assets WHERE condition = :condition")
    fun countByCondition(condition: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM assets")
    fun countAll(): Flow<Int>

    @Query("SELECT * FROM assets WHERE name LIKE '%' || :query || '%' OR serialNumber LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%'")
    fun searchAssets(query: String): Flow<List<Asset>>

    @Query("SELECT * FROM assets WHERE condition = 'Needs Repair' ORDER BY lastChecked ASC")
    fun getRepairList(): Flow<List<Asset>>
}
