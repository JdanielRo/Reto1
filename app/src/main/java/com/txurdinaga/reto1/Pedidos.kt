package com.txurdinaga.reto1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class Pedidos : AppCompatActivity() {

    private lateinit var pedidos_home: ImageView
    private lateinit var pedidos_pedidos: ImageView
    private lateinit var pedidos_carrito: ImageView
    private lateinit var pedidos_mi_cuenta: ImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidos)

        // Asignar vistas a las variables
        pedidos_home = findViewById(R.id.pedidos_home)
        pedidos_pedidos = findViewById(R.id.pedidos_pedidos)
        pedidos_carrito = findViewById(R.id.pedidos_carrito)
        pedidos_mi_cuenta = findViewById(R.id.pedidos_mi_cuenta)

        // Configurar clics en las imágenes
        pedidos_home.setOnClickListener {
            abrirHome()
        }
        pedidos_pedidos.setOnClickListener {
            abrirPedidos()
        }
        pedidos_carrito.setOnClickListener {
            abrirCarrito()
        }
        pedidos_mi_cuenta.setOnClickListener {
            abrirCuenta()
        }
    }

    private fun abrirHome() {
        // Reemplazar pedidos::class.java con la actividad de inicio deseada
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
