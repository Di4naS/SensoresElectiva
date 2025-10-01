package com.example.tallerevaluativoelectiva.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallerevaluativoelectiva.models.MotionEvent
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class MotionViewModel(app: Application) : AndroidViewModel(app) {

    // Administrador de sensores del dispositivo
    private val sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Se obtiene el sensor de acelerómetro (detecta movimiento en los 3 ejes: X, Y, Z)
    private val accelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Contador de sacudidas (cuántas veces se detecta un movimiento fuerte)
    private val _contadorSacudidas = MutableStateFlow(0)
    val contadorSacudidas: StateFlow<Int> = _contadorSacudidas

    // Estado actual del movimiento (reposo, leve o sacudida)
    private val _estadoMovimiento = MutableStateFlow("Inactivo")
    val estadoMovimiento: StateFlow<String> = _estadoMovimiento

    // Referencia en Firebase bajo el nodo "movimiento"
    private val database = Firebase.database.reference.child("movimiento")

    // Para evitar contar múltiples sacudidas en un instante muy corto
    private var lastShakeTime = 0L

    // Listener que recibe eventos del acelerómetro
    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            // Normalizamos los valores en relación con la gravedad terrestre (~9.8 m/s^2)
            val x = event.values[0] / SensorManager.GRAVITY_EARTH
            val y = event.values[1] / SensorManager.GRAVITY_EARTH
            val z = event.values[2] / SensorManager.GRAVITY_EARTH

            // Calculamos la fuerza resultante (magnitud del vector)
            val gForce = sqrt(x * x + y * y + z * z)
            val now = System.currentTimeMillis()

            when {
                // Si la fuerza supera 2.7 → se considera una "sacudida fuerte"
                gForce > 2.7f -> {
                    // Para evitar registrar sacudidas duplicadas en menos de 500 ms
                    if (now - lastShakeTime > 500) {
                        lastShakeTime = now
                        _contadorSacudidas.value += 1
                        _estadoMovimiento.value = "Sacudida detectada"
                        guardarEvento("sacudida") // Se guarda en Firebase
                    }
                }
                // Si supera 1.2 → movimiento leve
                gForce > 1.2f -> {
                    _estadoMovimiento.value = "Movimiento leve"
                    guardarEvento("leve")
                }
                // Si está por debajo → se considera en reposo
                else -> {
                    _estadoMovimiento.value = "En reposo"
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // No se utiliza en este caso
        }
    }

    // Activa la escucha del acelerómetro
    fun startListening() {
        accelSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // Detiene la escucha del acelerómetro
    fun stopListening() {
        sensorManager.unregisterListener(listener)
    }

    // Función auxiliar para registrar un evento en Firebase
    private fun guardarEvento(tipo: String) {
        val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val evento = MotionEvent(tipo, fecha)

        // Guardar de manera asíncrona usando corutinas
        viewModelScope.launch {
            database.push().setValue(evento)
        }
    }
}
