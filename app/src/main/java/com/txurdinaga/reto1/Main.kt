package com.txurdinaga.reto1

import Extra
import Plato
import Usuario
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class Main : AppCompatActivity() {

    // Declaraciones de variables
    lateinit var navigation: BottomNavigationView
    private lateinit var sharedPref: SharedPreferences
    private var currentFragment: Fragment? = null
    private var listaPlatos: ArrayList<Plato> = ArrayList()
    private var listaExtras: ArrayList<Extra> = ArrayList()
    private var usuario: Usuario = Usuario()
    var carritoUsuario: ArrayList<Pedido> = ArrayList()

    // Listener para las selecciones de elementos del menú de navegación
    private val onNavMenuListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        // Lógica para cada elemento del menú
        when (item.itemId) {
            R.id.itemHome -> {
                showFragment(Home())
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemPedidos -> {
                showFragment(Pedidos(carritoUsuario, listaPlatos, listaExtras))
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemCarrito -> {
                showFragment(Carrito(carritoUsuario, listaPlatos, listaExtras))
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemMiCuenta -> {
                showFragment(MiCuenta(usuario))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        // Inicialización de variables y obtención de datos
        listaPlatos = intent.getSerializableExtra("platos") as ArrayList<Plato>
        listaExtras = intent.getSerializableExtra("extras") as ArrayList<Extra>
        usuario = intent.getParcelableExtra<Usuario>("usuario")!!
        carritoUsuario = intent.getSerializableExtra("carrito") as ArrayList<Pedido>

        // Configuración del BottomNavigationView y del cambio de idioma
        navigation = findViewById(R.id.navMenu)
        navigation.setOnNavigationItemSelectedListener(onNavMenuListener)
        sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "es") ?: "es"
        setAppLocale(savedLanguage)
        setMenuItemsLanguage(savedLanguage)

        // Configuración de botones para cambiar el idioma
        val englishButton = findViewById<ImageView>(R.id.imageViewEnglish)
        val spanishButton = findViewById<ImageView>(R.id.imageViewEspañol)
        val euskeraButton = findViewById<ImageView>(R.id.imageViewEuskera)

        // Configuración de listeners para los botones de cambio de idioma
        englishButton.setOnClickListener {
            setAndApplyLanguage("en")
        }

        spanishButton.setOnClickListener {
            setAndApplyLanguage("es")
        }

        euskeraButton.setOnClickListener {
            setAndApplyLanguage("eu")
        }

        // Establecer el fragmento "Home" como fragmento inicial si no hay estado previo
        if (savedInstanceState == null) {
            showFragment(Home())
        }
    }

    // Función para configurar el idioma de la aplicación
    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    // Función para cambiar y aplicar el idioma
    private fun setAndApplyLanguage(languageCode: String) {
        sharedPref.edit().putString("language", languageCode).apply()
        setAppLocale(languageCode)
        updateResources(languageCode)
        recreate()
    }

    // Función para actualizar los recursos al idioma seleccionado
    private fun updateResources(languageCode: String) {
        val locale = Locale(languageCode)
        val resources: Resources = resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        baseContext.createConfigurationContext(configuration)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    // Función para mostrar un fragmento en la actividad
    private fun showFragment(fragment: Fragment) {
        if (currentFragment != null) {
            supportFragmentManager.beginTransaction().remove(currentFragment!!).commitAllowingStateLoss()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss()
        currentFragment = fragment
    }

    // Función para configurar el idioma de los elementos del menú
    private fun setMenuItemsLanguage(languageCode: String) {
        val resources = getResourcesForLocale(this, languageCode)
        navigation.menu.findItem(R.id.itemHome).title = resources.getString(R.string.home)
        navigation.menu.findItem(R.id.itemPedidos).title = resources.getString(R.string.pedido)
        navigation.menu.findItem(R.id.itemCarrito).title = resources.getString(R.string.carrito)
        navigation.menu.findItem(R.id.itemMiCuenta).title = resources.getString(R.string.mi_cuenta)
    }

    // Función para obtener los recursos en el idioma deseado
    private fun getResourcesForLocale(context: Context, languageCode: String): Resources {
        val locale = Locale(languageCode)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config).resources
    }
}
