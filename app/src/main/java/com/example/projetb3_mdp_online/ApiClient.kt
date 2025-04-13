package com.example.projetb3_mdp_online

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "https://k2na.alwaysdata.net/api/"

    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create()) // Utilisation de Gson
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}