package com.xolary.weathertraining.fragments

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Функция проверки начилия разрешения от пользователя
 *
 * @param permission название разрешения
 * @return результат сравнения наличия разрешения с переменной её явного наличия.
 * false - разрешение отсутсвует
 * true - разрешение дано
 */
fun Fragment.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        activity as AppCompatActivity, permission) == PackageManager.PERMISSION_GRANTED
}