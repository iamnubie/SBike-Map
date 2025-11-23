package com.example.sbikemap.presentation.sign_in

import android.content.IntentSender
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SignInViewModel : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onSignInSuccess() {
        _state.update { it.copy(
            isSignInSuccessful = true,
            signInError = null
        ) }
    }

    fun onSignInError(message: String?) {
        _state.update { it.copy(
            isSignInSuccessful = false,
            signInError = message
        ) }
    }

    fun resetState() {
        _state.value = SignInState()
    }
}
