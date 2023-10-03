
package com.txurdinaga.reto1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class Carrito : AppCompatActivity() {


    private lateinit var carrito_home: ImageView
    private lateinit var carrito_pedidos: ImageView
    private lateinit var carrito_carrito: ImageView
    private lateinit var carrito_mi_cuenta: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.carrito)

        // Asignar vistas a las variables
        carrito_home = findViewById(R.id.carrito_home)
        carrito_pedidos = findViewById(R.id.carrito_pedidos)
        carrito_carrito = findViewById(R.id.carrito_carrito)
        carrito_mi_cuenta = findViewById(R.id.carrito_mi_cuenta)

        // Configurar clics en las imágenes
        carrito_home.setOnClickListener {
            abrirHome()
        }
        carrito_pedidos.setOnClickListener {
            abrirPedidos()
        }
        carrito_carrito.setOnClickListener {
            abrirCarrito()
        }
        carrito_mi_cuenta.setOnClickListener {
            abrirCuenta()
        }
    }

    private fun abrirHome() {
        // Reemplazar carrito::class.java con la actividad de inicio deseada
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
    }

    private fun abrirPedidos() {
        val intent = Intent(this, Pedidos::class.java)
        startActivity(intent)
    }

    private fun abrirCarrito() {
        val intent = Intent(this, Carrito::class.java)
        startActivity(intent)
    }

    private fun abrirCuenta() {
        val intent = Intent(this, Mi_cuenta::class.java)
        startActivity(intent)
    }
}
