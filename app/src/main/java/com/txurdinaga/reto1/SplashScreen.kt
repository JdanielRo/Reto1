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
        super.onCreate(savedInstanceState) // Llama al método onCreate de la clase base

        setContentView(R.layout.splash_screen) // Establece el diseño de la actividad a "splash_screen"

        // Se ejecutan corrutinas para operaciones asincrónicas
        CoroutineScope(Dispatchers.IO).launch {
            val platosDeferred = async { obtenerPlatos() } // Inicia una corrutina para obtener datos de platos
            val extrasDeferred = async { obtenerExtras() } // Inicia una corrutina para obtener datos adicionales

            // Espera hasta que ambas corrutinas terminen
            val listaPlatos = platosDeferred.await()
            val listaExtras = extrasDeferred.await()

            // Cambia al contexto principal (UI thread) para realizar operaciones de interfaz de usuario
            withContext(Dispatchers.Main) {
                val intent = Intent(this@SplashScreen, Login::class.java) // Crea un intent para iniciar la actividad "Login"
                intent.putExtra("platos", listaPlatos) // Agrega la lista de platos al intent
                intent.putExtra("extras", listaExtras) // Agrega la lista de extras al intent

                startActivity(intent) // Inicia la actividad "Login" con el intent
                overridePendingTransition(0, 0) // Aplica una transición sin animación
                finish() // Finaliza la actividad actual ("SplashScreen") después de iniciar la siguiente
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
