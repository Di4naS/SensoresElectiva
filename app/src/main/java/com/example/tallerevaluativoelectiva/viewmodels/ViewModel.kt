package com.example.tallerevaluativoelectiva.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallerevaluativoelectiva.models.ProximityEvent
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProximityViewModel(app: Application) : AndroidViewModel(app) {

    // Administrador de sensores del dispositivo
    private val sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Se obtiene el sensor de proximidad (si existe en el dispositivo)
    private val proxSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    // Estado actual del sensor en un StateFlow (para observar en la UI)
    private val _estado = MutableStateFlow("Desconocido")
    val estado: StateFlow<String> = _estado

    // Referencia a la base de datos en Firebase bajo el nodo "proximidad"
    private val database = Firebase.database.reference.child("proximidad")

    // Listener para recibir cambios del sensor de proximidad
    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val distance = event.values[0] // valor leído por el sensor
            val isNear = distance < (proxSensor?.maximumRange ?: 0f)

            // Se interpreta el valor del sensor en un estado legible
            val nuevoEstado = if (isNear) "Objeto detectado cerca" else "Sin objetos cerca"

            // Actualizar el flujo de estado (para Compose)
            _estado.value = nuevoEstado

            // Preparar datos con fecha y hora
            val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val evento = ProximityEvent(nuevoEstado, fecha)

            // Guardar el evento en Firebase de manera asíncrona
            viewModelScope.launch {
                database.push().setValue(evento)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // No se utiliza en este caso
        }
    }

    // Comenzar a escuchar eventos del sensor
    fun startListening() {
        proxSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // Detener la escucha de eventos del sensor
    fun stopListening() {
        sensorManager.unregisterListener(listener)
    }
}
