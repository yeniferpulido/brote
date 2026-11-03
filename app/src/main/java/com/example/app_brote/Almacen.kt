package com.example.app_brote

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// Guarda la lista de metas como texto dentro del celular (SharedPreferences)
class Almacen(context: Context) {

    private val prefs = context.getSharedPreferences("brote", Context.MODE_PRIVATE)

    fun guardar(metas: List<Meta>) {
        val array = JSONArray()
        for (m in metas) {
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("nombre", m.nombre)
            obj.put("objetivo", m.objetivo)
            obj.put("ahorrado", m.ahorrado)
            array.put(obj)
        }
        prefs.edit().putString("metas", array.toString()).apply()
    }

    fun cargar(): List<Meta> {
        val texto = prefs.getString("metas", null) ?: return emptyList()
        val array = JSONArray(texto)
        val lista = mutableListOf<Meta>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            lista.add(
                Meta(
                    id = obj.getLong("id"),
                    nombre = obj.getString("nombre"),
                    objetivo = obj.getDouble("objetivo"),
                    ahorrado = obj.getDouble("ahorrado")
                )
            )
        }
        return lista
    }
}

