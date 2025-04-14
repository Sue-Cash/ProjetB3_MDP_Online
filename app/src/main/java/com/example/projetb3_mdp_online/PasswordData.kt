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

data class PasswordsData(
    val records: List<PasswordData>
)

data class PasswordResponse(
    val status: Int,
    val message: String,
    val data: PasswordData?
)

data class PasswordsResponse(
    val status: Int,
    val message: String,
    val data: PasswordsData?
)

data class PasswordCreateRequest(
    val user_id: Int,
    val category_id: Int,
    val site_name: String,
    val username: String,
    val password: String,
    val biometric_protection: Int,
    val notes: String
)

data class PasswordUpdateRequest(
    val password_id: Int,
    val site_name: String,
    val username: String,
    val password: String,
    val biometric_protection: Int,
    val notes: String
)