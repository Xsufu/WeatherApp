package com.xolary.weathertraining

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText

// Мэнеджер диалогов
object DialogManager {
    // Диалог для призыва ко включению геолокации
    fun locationSettingsDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle("Геопозиция отключена")
        dialog.setMessage("Данные о местоположенни отключены. Хотите включить геолокацию?")
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Нет") { _, _ ->
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Да") { _, _ ->
            listener.onClick(null)
            dialog.dismiss()
        }
        dialog.show()
    }

    // Диалог, приглашающий ко вводу названия города
    fun searchByCityName(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val editText = EditText(context)
        builder.setView(editText)
        val dialog = builder.create()

        dialog.setTitle("Введите название города:")

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена") { _, _ ->
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ок") { _, _ ->
            listener.onClick(editText.text.toString())
            dialog.dismiss()
        }

        dialog.show()
    }

    interface Listener {
        fun onClick(newCityName: String?)
    }
}