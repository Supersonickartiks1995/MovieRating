package com.supersonic.environment.movierating.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.supersonic.environment.movierating.domain.model.Movie

@Entity(
    tableName = "movies",
    indices = [
        androidx.room.Index(value = ["isFavorite"]),
        androidx.room.Index(value = ["isCached"])
    ]
)
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String?,
    val rating: Double,
    val voteCount: Int,
    val isFavorite: Boolean = false,
    val isCached: Boolean = false,
    val lastViewedAt: Long? = null
)

fun MovieEntity.toDomain(): Movie {
    return Movie(
        id = id,
        title = title,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        releaseDate = releaseDate,
        rating = rating,
        voteCount = voteCount
    )
}

fun Movie.toEntity(isFavorite: Boolean = false, isCached: Boolean = false): MovieEntity {
    return MovieEntity(
        id = id,
        title = title,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        releaseDate = releaseDate,
        rating = rating,
        voteCount = voteCount,
        isFavorite = isFavorite,
        isCached = isCached
    )
}
