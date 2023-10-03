package com.txurdinaga.reto1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class Home : AppCompatActivity() {

    private lateinit var home_home: ImageView
    private lateinit var home_pedidos: ImageView
    private lateinit var home_carrito: ImageView
    private lateinit var home_mi_cuenta: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Asignar vistas a las variables
        home_home = findViewById(R.id.home_home)
        home_pedidos = findViewById(R.id.home_pedidos)
        home_carrito = findViewById(R.id.home_carrito)
        home_mi_cuenta = findViewById(R.id.home_mi_cuenta)

        // Configurar clics en las imágenes
        home_home.setOnClickListener {
            abrirHome()
        }
        home_pedidos.setOnClickListener {
            abrirPedidos()
        }
        home_carrito.setOnClickListener {
            abrirCarrito()
        }
        home_mi_cuenta.setOnClickListener {
            abrirCuenta()
        }
    }

    private fun abrirHome() {
        // Reemplazar Home::class.java con la actividad de inicio deseada
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
