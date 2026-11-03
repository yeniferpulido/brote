package com.example.app_brote

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel

class MetasViewModel(app: Application) : AndroidViewModel(app) {

    private val almacen = Almacen(app)

    // Lista "observable": cuando cambia, la pantalla se actualiza sola
    val metas = mutableStateListOf<Meta>()

    init {
        metas.addAll(almacen.cargar())   // al abrir la app, carga lo guardado
    }

    fun crearMeta(nombre: String, objetivo: Double) {
        metas.add(Meta(id = System.currentTimeMillis(), nombre = nombre, objetivo = objetivo))
        almacen.guardar(metas)
    }

    fun agregarAporte(id: Long, monto: Double) {
        val posicion = metas.indexOfFirst { it.id == id }
        if (posicion >= 0) {
            val meta = metas[posicion]
            metas[posicion] = meta.copy(ahorrado = meta.ahorrado + monto)
            almacen.guardar(metas)
        }
    }

    fun eliminarMeta(id: Long) {
        metas.removeAll { it.id == id }
        almacen.guardar(metas)
    }
}