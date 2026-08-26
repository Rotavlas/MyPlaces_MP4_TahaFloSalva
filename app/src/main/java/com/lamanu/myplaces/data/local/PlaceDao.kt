package com.lamanu.myplaces.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lamanu.myplaces.domain.model.PlaceOrigin
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Query("SELECT * FROM places ORDER BY created_at DESC")
    fun observeAll(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE origin = :origin ORDER BY created_at DESC")
    fun observeByOrigin(origin: PlaceOrigin): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE id = :id")
    fun observeById(id: String): Flow<PlaceEntity?>

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun findById(id: String): PlaceEntity?

    @Query("SELECT * FROM places WHERE origin = :origin ORDER BY created_at ASC")
    suspend fun listByOrigin(origin: PlaceOrigin): List<PlaceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(place: PlaceEntity)

    /**
     * Insertion d'import : un lieu deja present (meme UUID) est ignore, jamais ecrase.
     * C'est ce qui garantit qu'un import ne corrompt pas les donnees locales.
     * Retourne les rowId inseres (-1 pour chaque ligne ignoree).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnoringDuplicates(places: List<PlaceEntity>): List<Long>

    @Update
    suspend fun update(place: PlaceEntity)

    @Query("DELETE FROM places WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM places WHERE origin = :origin AND author_id = :authorId")
    suspend fun deleteImportedFrom(authorId: String, origin: PlaceOrigin = PlaceOrigin.IMPORTED)

    @Query("SELECT COUNT(*) FROM places WHERE origin = :origin")
    suspend fun countByOrigin(origin: PlaceOrigin): Int

    @Query("SELECT DISTINCT author_id || '\u001F' || author_name FROM places WHERE origin = :origin")
    fun observeImportedAuthors(origin: PlaceOrigin = PlaceOrigin.IMPORTED): Flow<List<String>>
}
