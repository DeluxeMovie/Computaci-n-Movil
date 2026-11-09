package com.example.taller_3.utils

import android.util.Patterns

private val PASSWORD_REGEX = Regex("^(?=.*[^A-Za-z0-9\\s]).{8,}$")
private val PHONE_REGEX = Regex("^\\+?[0-9]{7,15}$")

fun isValidEmail(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

fun isValidPassword(password: String) = PASSWORD_REGEX.matches(password)

fun isValidPhone(phone: String) = PHONE_REGEX.matches(phone)
