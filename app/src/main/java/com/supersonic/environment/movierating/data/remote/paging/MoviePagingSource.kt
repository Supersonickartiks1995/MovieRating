package com.supersonic.environment.movierating.data.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.supersonic.environment.movierating.data.remote.TmdbApi
import com.supersonic.environment.movierating.domain.model.Movie

class MoviePagingSource(
    private val api: TmdbApi,
    private val apiKey: String
) : PagingSource<Int, Movie>() {

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return try {
            val response = api.getPopularMovies(apiKey, page)
            val movies = response.results.map { dto ->
                Movie(
                    id = dto.id,
                    title = dto.title,
                    overview = dto.overview,
                    posterUrl = dto.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
                    backdropUrl = dto.backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" },
                    releaseDate = dto.releaseDate,
                    rating = dto.voteAverage,
                    voteCount = dto.voteCount
                )
            }

            LoadResult.Page(
                data = movies,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (movies.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
