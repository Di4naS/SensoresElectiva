package com.example.tallerevaluativoelectiva.models

data class MotionEvent(
    val tipo: String = "",   // "leve" o "sacudida"
    val fecha: String = ""  // Fecha/hora del evento
)
