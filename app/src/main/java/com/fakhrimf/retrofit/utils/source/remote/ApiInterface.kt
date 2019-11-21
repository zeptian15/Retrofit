package com.fakhrimf.retrofit.utils.source.remote

import com.fakhrimf.retrofit.model.MovieModel
import com.fakhrimf.retrofit.model.MovieResponse
import com.fakhrimf.retrofit.model.ShowModel
import com.fakhrimf.retrofit.model.ShowResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    //For Movies
    @GET("movie/latest")
    fun getLatestMovie(@Query("api_key") apiKey:String, @Query("language") language:String) : Call<MovieModel>
    @GET("movie/popular")
    fun getPopularMovie(@Query("api_key") apiKey:String, @Query("language") language:String) : Call<MovieResponse>

    //For Shows
    @GET("tv/latest")
    fun getLatestShow(@Query("api_key") apiKey: String, @Query("language") language: String) : Call<ShowModel>
    @GET("tv/popular")
    fun getPopularShow(@Query("api_key") apiKey: String, @Query("language") language: String) : Call<ShowResponse>
}