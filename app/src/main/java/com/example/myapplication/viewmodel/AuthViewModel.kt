package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Please fill in all fields")
            return
        }
        _isLoading.value = true
        _error.value = null
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                _isLoading.value = false
                if (it.isSuccessful) {
                    _currentUser.value = auth.currentUser
                    onResult(true, null)
                } else {
                    val msg = it.exception?.message ?: "Login failed"
                    _error.value = msg
                    onResult(false, msg)
                }
            }
    }

    fun signup(email: String, password: String, name: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            onResult(false, "Please fill in all fields")
            return
        }
        _isLoading.value = true
        _error.value = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    auth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
                        _isLoading.value = false
                        _currentUser.value = auth.currentUser
                        onResult(true, null)
                    }
                } else {
                    _isLoading.value = false
                    val msg = task.exception?.message ?: "Sign up failed"
                    _error.value = msg
                    onResult(false, msg)
                }
            }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }
}
