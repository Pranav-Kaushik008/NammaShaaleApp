package com.nammashaale.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class Asset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firestoreId: String = "",
    val name: String = "",
    val serialNumber: String = "",
    val location: String = "",
    val condition: String = "Working",   // "Working" | "Needs Check" | "Needs Repair"
    val photoPath: String? = null,
    val photoUrl: String? = null,        // Firebase Storage URL
    val lastChecked: Long = System.currentTimeMillis(),
    val issueNote: String? = null,
    val category: String = "General"     // "Lab", "Sports", "Tablet", "General"
)
