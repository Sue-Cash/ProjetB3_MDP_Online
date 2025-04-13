package com.example.projetb3_mdp_online

import com.example.projetb3_mdp_online.UserData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("users/index.php")
    suspend fun getUserByEmail(@Query("email") email: String): Response<ApiResponse>
}