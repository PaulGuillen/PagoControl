package com.devpaul.features.auth.data.datasource.mapper

import com.devpaul.features.auth.domain.entity.AuthUser
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser.toDomain(
    fallbackName: String = "",
    preferredName: String = "",
) = AuthUser(
    userID = uid,
    name = preferredName.ifBlank { displayName.orEmpty().ifBlank { fallbackName } },
    email = email.orEmpty(),
)
