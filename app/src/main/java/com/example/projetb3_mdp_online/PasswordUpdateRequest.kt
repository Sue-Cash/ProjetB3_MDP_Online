package com.example.projetb3_mdp_online

data class PasswordUpdateRequest(
    val password_id: Int,
    val site_name: String,
    val username: String,
    val password: String,
    val biometric_protection: Int,
    val notes: String
)