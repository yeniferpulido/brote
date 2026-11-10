package com.example.app_brote

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// Guarda las metas y flores desbloqueadas en SharedPreferences
class Almacen(context: Context) {

    private val prefs = context.getSharedPreferences("brote", Context.MODE_PRIVATE)

    // Guardar lista de metas
    fun guardar(metas: List<Meta>) {
        val array = JSONArray()
        for (m in metas) {
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("nombre", m.nombre)
            obj.put("objetivo", m.objetivo)
            obj.put("ahorrado", m.ahorrado)
            obj.put("florTipo", m.florTipo)
            array.put(obj)
        }
        prefs.edit().putString("metas", array.toString()).apply()
    }

    // Cargar lista de metas (con compatibilidad para metas antiguas sin florTipo)
    fun cargar(): List<Meta> {
        val texto = prefs.getString("metas", null) ?: return emptyList()
        val array = JSONArray(texto)
        val lista = mutableListOf<Meta>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val id = obj.getLong("id")
            val defaultFlorTipo = ((Math.abs(id) % FLORES_DISPONIBLES.size) + 1).toInt()
            lista.add(
                Meta(
                    id = id,
                    nombre = obj.getString("nombre"),
                    objetivo = obj.getDouble("objetivo"),
                    ahorrado = obj.getDouble("ahorrado"),
                    florTipo = obj.optInt("florTipo", defaultFlorTipo)
                )
            )
        }
        return lista
    }

    // Guardar flores desbloqueadas
    fun guardarFlores(flores: List<FlorDesbloqueada>) {
        val array = JSONArray()
        for (f in flores) {
            val obj = JSONObject()
            obj.put("id", f.id)
            obj.put("metaId", f.metaId)
            obj.put("florTipo", f.florTipo)
            obj.put("pagina", f.pagina)
            obj.put("parcela", f.parcela)
            obj.put("fechaDesbloqueo", f.fechaDesbloqueo)
            obj.put("metaNombre", f.metaNombre)
            array.put(obj)
        }
        prefs.edit().putString("flores", array.toString()).apply()
    }

    // Cargar flores desbloqueadas
    fun cargarFlores(): List<FlorDesbloqueada> {
        val texto = prefs.getString("flores", null) ?: return emptyList()
        val array = JSONArray(texto)
        val lista = mutableListOf<FlorDesbloqueada>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val florTipo = obj.getInt("florTipo")
            lista.add(
                FlorDesbloqueada(
                    id = obj.getLong("id"),
                    metaId = obj.getLong("metaId"),
                    florTipo = florTipo,
                    florResId = recursoFlorPorTipo(florTipo),
                    pagina = obj.getInt("pagina"),
                    parcela = obj.getInt("parcela"),
                    fechaDesbloqueo = obj.optLong("fechaDesbloqueo", System.currentTimeMillis()),
                    metaNombre = obj.optString("metaNombre", "")
                )
            )
        }
        return lista
    }
}
