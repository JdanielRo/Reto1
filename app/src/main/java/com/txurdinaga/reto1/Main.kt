package com.txurdinaga.reto1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.bottomnavigation.BottomNavigationView

class Main : AppCompatActivity() {

    lateinit var navigation: BottomNavigationView

    private val onNavMenuListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {

            R.id.itemHome -> {
                supportFragmentManager.commit {
                    replace(R.id.frameContainer, Home())
                    addToBackStack(null)

                }
                return@OnNavigationItemSelectedListener true
            }


            R.id.itemPedidos -> {
                supportFragmentManager.commit {
                    replace(R.id.frameContainer, Pedidos())
                    addToBackStack(null)
                }
                return@OnNavigationItemSelectedListener true
            }


            R.id.itemCarrito -> {
                supportFragmentManager.commit {
                    replace(R.id.frameContainer, Carrito())
                    addToBackStack(null)
                }
                return@OnNavigationItemSelectedListener true
            }

            // Agrega más casos para otros elementos del menú de navegación si es necesario

        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        navigation = findViewById(R.id.navMenu)
        navigation.setOnNavigationItemSelectedListener(onNavMenuListener)

        supportFragmentManager.commit {
            replace(R.id.frameContainer, Home())
            addToBackStack(null)
        }
    }

}