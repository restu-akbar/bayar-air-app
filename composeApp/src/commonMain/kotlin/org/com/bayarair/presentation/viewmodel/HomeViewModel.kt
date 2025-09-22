package org.com.bayarair.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.data.token.TokenHandler

class HomeViewModel(
    private val tokenHandler: TokenHandler,
    private val authRepo: AuthRepository,
) : ScreenModel {
    private val _onErrorMessage = MutableSharedFlow<String>()
    val onErrorMessage: SharedFlow<String> = _onErrorMessage

}
