package com.txurdinaga.reto1

import Extra
import Plato
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class SplashScreen : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        CoroutineScope(Dispatchers.IO).launch {
            val platosDeferred = async { obtenerPlatos() }
            val extrasDeferred = async { obtenerExtras() }

            val listaPlatos = platosDeferred.await()
            val listaExtras = extrasDeferred.await()

            withContext(Dispatchers.Main) {
                val intent = Intent(this@SplashScreen, Login::class.java)
                intent.putExtra("platos", listaPlatos)
                intent.putExtra("extras", listaExtras)

                startActivity(intent)
                overridePendingTransition(0, 0)

                finish() // Finaliza la actividad actual después de iniciar la siguiente
            }
        }
    }

    private suspend fun obtenerPlatos(): ArrayList<Plato> {
        val listaPlatos: ArrayList<Plato> = ArrayList()
        val result = db.collection("Plato").get().await()
        for (document in result) {
            val idPlato = document.id
            val nombre = document.getString("nombre") ?: ""
            val precio = document.getDouble("precio") ?: 0.0
            val descripcion = document.getString("descripcion") ?: ""
            val stock = document.getLong("stock")?.toInt() ?: 0
            val tipo = document.getString("tipo") ?: ""
            val plato = Plato(idPlato, nombre, precio, descripcion, stock, tipo)

            listaPlatos.add(plato)
        }
        return listaPlatos
    }

    private suspend fun obtenerExtras(): ArrayList<Extra> {
        val listaExtras: ArrayList<Extra> = ArrayList()
        val result = db.collection("Extra").get().await()
        for (document in result) {
            val idPlato = document.id
            val nombre = document.getString("nombre") ?: ""
            val precio = document.getDouble("precio") ?: 0.0
            val descripcion = document.getString("descripcion") ?: ""
            val stock = document.getLong("stock")?.toInt() ?: 0
            val tipo = document.getString("tipo") ?: ""
            val extra = Extra(idPlato, nombre, precio, descripcion, stock, tipo)

            listaExtras.add(extra)
        }
        return listaExtras
    }
}
