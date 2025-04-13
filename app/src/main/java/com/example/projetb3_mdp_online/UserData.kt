package com.example.projetb3_mdp_online

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    val status: Int,
    val message: String,
    val data: UserData?
)

data class UserData(
    val user_id: Int,
    val email: String?,
    val password_hash: String?,
    val created_at: String?
)