package org.com.bayarair.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.data.token.TokenHandler

class ProfileViewModel(
    private val tokenHandler: TokenHandler,
    private val authRepo: AuthRepository,
) : ScreenModel
