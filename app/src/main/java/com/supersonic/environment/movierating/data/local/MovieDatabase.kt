package com.supersonic.environment.movierating.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.supersonic.environment.movierating.data.local.dao.MovieDao
import com.supersonic.environment.movierating.data.local.entity.MovieEntity

@Database(entities = [MovieEntity::class], version = 2, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract val movieDao: MovieDao
}
