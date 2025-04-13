package com.example.projetb3_mdp_online

data class PasswordData(
    val password_id: Int,
    val user_id: Int,
    val category_id: Int,
    val site_name: String,
    val username: String,
    val password: String,
    val biometric_protection: Int,
    val notes: String,
    val created_at: String,
    val updated_at: String
)