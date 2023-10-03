
package com.txurdinaga.reto1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class Mi_cuenta : AppCompatActivity() {


    private lateinit var mi_cuenta_home: ImageView
    private lateinit var mi_cuenta_pedidos: ImageView
    private lateinit var mi_cuenta_carrito: ImageView
    private lateinit var mi_cuenta_mi_cuenta: ImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mi_cuenta)

        // Asignar vistas a las variables
        mi_cuenta_home = findViewById(R.id.mi_cuenta_home)
        mi_cuenta_pedidos = findViewById(R.id.mi_cuenta_pedidos)
        mi_cuenta_carrito = findViewById(R.id.mi_cuenta_carrito)
        mi_cuenta_mi_cuenta = findViewById(R.id.mi_cuenta_mi_cuenta)

        // Configurar clics en las imágenes
        mi_cuenta_home.setOnClickListener {
            abrirHome()
        }
        mi_cuenta_pedidos.setOnClickListener {
            abrirPedidos()
        }
        mi_cuenta_carrito.setOnClickListener {
            abrirCarrito()
        }
        mi_cuenta_mi_cuenta.setOnClickListener {
            abrirCuenta()
        }
    }

    private fun abrirHome() {
        // Reemplazar mi_cuenta::class.java con la actividad de inicio deseada
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
