package com.example.tallerevaluativoelectiva.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.tallerevaluativoelectiva.models.MotionEvent
import com.example.tallerevaluativoelectiva.models.ProximityEvent
import com.example.tallerevaluativoelectiva.models.ReportItem
import com.google.firebase.Firebase
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class ReportsViewModel(app: Application) : AndroidViewModel(app) {

    // Referencia raíz de la base de datos
    private val database = Firebase.database.reference
    // Nodos de Firebase específicos para cada sensor
    private val proximidadRef = database.child("proximidad")
    private val movimientoRef = database.child("movimiento")

    // Lista reactiva de reportes, expuesta a la UI como StateFlow
    private val _reportes = MutableStateFlow<List<ReportItem>>(emptyList())
    val reportes: StateFlow<List<ReportItem>> = _reportes

    // ------------------- LISTENER DE PROXIMIDAD -------------------
    private val proximidadListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            // Convierte el nodo de Firebase a un ProximityEvent
            snapshot.getValue(ProximityEvent::class.java)?.let { evt ->
                // Obtiene la clave única del evento o genera una nueva
                val key = snapshot.key ?: UUID.randomUUID().toString()
                // Crea un ReportItem con la info
                addReport(ReportItem("Proximidad", evt.estado, evt.fecha, key))
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            snapshot.getValue(ProximityEvent::class.java)?.let { evt ->
                val key = snapshot.key ?: return
                // Actualiza un reporte existente si cambia en Firebase
                updateReport(key, ReportItem("Proximidad", evt.estado, evt.fecha, key))
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val key = snapshot.key ?: return
            // Elimina un reporte si se borra de Firebase
            removeReport(key)
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
    }

    // ------------------- LISTENER DE MOVIMIENTO -------------------
    private val movListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            snapshot.getValue(MotionEvent::class.java)?.let { evt ->
                val key = snapshot.key ?: UUID.randomUUID().toString()
                addReport(ReportItem("Movimiento", evt.tipo, evt.fecha, key))
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            snapshot.getValue(MotionEvent::class.java)?.let { evt ->
                val key = snapshot.key ?: return
                updateReport(key, ReportItem("Movimiento", evt.tipo, evt.fecha, key))
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val key = snapshot.key ?: return
            removeReport(key)
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
    }

    // ------------------- INICIALIZACIÓN -------------------
    init {
        // Se suscribe a los cambios en ambos nodos de Firebase
        proximidadRef.addChildEventListener(proximidadListener)
        movimientoRef.addChildEventListener(movListener)
    }

    // ------------------- FUNCIONES AUXILIARES -------------------

    // Agrega un nuevo reporte a la lista y la ordena por fecha (más reciente primero)
    private fun addReport(item: ReportItem) {
        val newList = (_reportes.value + item).sortedByDescending { it.fecha }
        _reportes.value = newList
    }

    // Reemplaza un reporte existente identificado por su clave
    private fun updateReport(key: String, newItem: ReportItem) {
        val updated = _reportes.value
            .map { if (it.key == key) newItem else it }
            .sortedByDescending { it.fecha }
        _reportes.value = updated
    }

    // Elimina un reporte de la lista según su clave
    private fun removeReport(key: String) {
        _reportes.value = _reportes.value.filterNot { it.key == key }
    }

    // Limpieza: se eliminan los listeners cuando la ViewModel ya no se usa
    override fun onCleared() {
        proximidadRef.removeEventListener(proximidadListener)
        movimientoRef.removeEventListener(movListener)
        super.onCleared()
    }
}
