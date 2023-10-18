package com.txurdinaga.reto1

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class SplashScreen : AppCompatActivity() {

    private val listaPlatos: ArrayList<Plato> = ArrayList()
    private val listaExtras: ArrayList<Extra> = ArrayList()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        Handler().postDelayed({
            obtenerPlatos()
            obtenerExtras()
            val intent = Intent(this, Login::class.java)
            intent.putExtra("platos", listaPlatos)

            // Aquí, deberías ver el tamaño de la lista de platos en el logcat
            Log.d("MiApp", "Cantidad de platos obtenidos: ${listaPlatos.size}")

            startActivity(intent)
        }, 2000)
    }

    private fun obtenerPlatos() {

        db.collection("Plato")
            .get()
            .addOnSuccessListener { result ->
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
            }
            .addOnFailureListener { e ->
                Log.e("MiApp", "Error al obtener platos: ${e.message}", e)
            }
    }

    private fun obtenerExtras() {
        db.collection("Extra")
            .get()
            .addOnSuccessListener { result ->
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
            }
            .addOnFailureListener { e ->
                Log.e("MiApp", "Error al obtener platos: ${e.message}", e)
            }
    }
}
