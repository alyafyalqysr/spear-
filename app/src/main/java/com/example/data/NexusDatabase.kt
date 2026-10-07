package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "monitored_assets")
data class DbAsset(
    @PrimaryKey val id: String,
    val hostname: String,
    val ip: String,
    val type: String,
    val status: String,
    val servicesString: String, // Comma separated list
    val vulnerabilityCount: Int,
    val cloudProvider: String
)

@Entity(tableName = "vulnerability_records")
data class DbVulnerability(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetId: String,
    val title: String,
    val category: String,
    val severity: String,
    val description: String,
    val cve: String,
    val recommendedFix: String,
    val detectedAt: Long
)

@Dao
interface NexusDao {
    @Query("SELECT * FROM monitored_assets")
    fun getAllAssets(): Flow<List<DbAsset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: DbAsset)

    @Delete
    suspend fun deleteAsset(asset: DbAsset)

    @Query("SELECT * FROM vulnerability_records ORDER BY detectedAt DESC")
    fun getAllVulnerabilities(): Flow<List<DbVulnerability>>

    @Query("SELECT * FROM vulnerability_records WHERE assetId = :assetId")
    fun getVulnerabilitiesForAsset(assetId: String): Flow<List<DbVulnerability>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVulnerability(vulnerability: DbVulnerability)

    @Query("DELETE FROM vulnerability_records")
    suspend fun clearAllVulnerabilities()
}

@Database(entities = [DbAsset::class, DbVulnerability::class], version = 1, exportSchema = false)
abstract class NexusDatabase : RoomDatabase() {
    abstract val dao: NexusDao

    companion object {
        @Volatile
        private var INSTANCE: NexusDatabase? = null

        fun getDatabase(context: Context): NexusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexusDatabase::class.java,
                    "nexus_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
