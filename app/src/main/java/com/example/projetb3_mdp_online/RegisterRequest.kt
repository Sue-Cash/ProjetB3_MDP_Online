package com.example.projetb3_mdp_online

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val email: String,
    @SerializedName("password_hash") val passwordHash: String
)