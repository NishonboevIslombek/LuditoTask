package com.ludito.task.data.bookmark.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ludito.task.data.bookmark.db.model.LocationEntity

@Dao
interface BookmarkDao {

    @Insert(
        onConflict = OnConflictStrategy.REPLACE,
        entity = LocationEntity::class
    )
    fun insertLocation(locationEntity: LocationEntity)

    @Query(
        """
        SELECT * from location_data
    """
    )
    fun getLocations(): List<LocationEntity>
}