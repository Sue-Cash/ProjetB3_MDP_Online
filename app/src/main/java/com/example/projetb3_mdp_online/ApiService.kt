package com.example.projetb3_mdp_online.api

import com.example.projetb3_mdp_online.ApiResponse
import com.example.projetb3_mdp_online.PasswordResponse
import com.example.projetb3_mdp_online.PasswordUpdateRequest
import com.example.projetb3_mdp_online.RegisterRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Query
import retrofit2.http.Body

interface ApiService {
    @GET("passwords/index.php")
    fun getPasswordById(@Query("id") id: Int): Call<PasswordResponse>

    @GET("users/index.php")
    suspend fun getUserByEmail(@Query("email") email: String): Response<ApiResponse>

    @POST("users/index.php")
    suspend fun registerUser(@Body request: RegisterRequest): Response<ApiResponse>
    
    @PUT("passwords/index.php")
    fun updatePassword(@Body passwordUpdate: PasswordUpdateRequest): Call<PasswordResponse>
}