package com.example.projetb3_mdp_online

data class PasswordResponse(
    val status: Int,
    val message: String,
    val data: PasswordData?
)