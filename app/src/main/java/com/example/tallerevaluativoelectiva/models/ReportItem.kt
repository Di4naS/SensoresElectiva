package com.example.tallerevaluativoelectiva.models

data class ReportItem (
    val tipo: String = "",    // "Proximidad" o "Movimiento"
    val estado: String = "",  // Descripción del estado registrado
    val fecha: String = "",   // Fecha/hora del evento
    val key: String = ""      // ID generado por Firebase
)