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

    lateinit var navigation: BottomNavigationView // Declaración de la vista BottomNavigationView
    private lateinit var sharedPref: SharedPreferences // Declaración de SharedPreferences para guardar el idioma
    private var currentFragment: Fragment? = null // Declaración de un Fragmento actual
    private var listaPlatos: ArrayList<Plato> = ArrayList()
    private var listaExtras: ArrayList<Extra> = ArrayList()
    private var usuario :Usuario = Usuario()
    private var carritoUsuario: ArrayList<Pedido> = ArrayList()

    // Listener para las selecciones de elementos del menú de navegación
    private val onNavMenuListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.itemHome -> {
                showFragment(Home()) // Mostrar el fragmento "Home" al seleccionar el elemento correspondiente
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemPedidos -> {
                showFragment(Pedidos(carritoUsuario, listaPlatos, listaExtras)) // Mostrar el fragmento "Pedidos" al seleccionar el elemento correspondiente
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemCarrito -> {
                showFragment(Carrito(carritoUsuario, listaPlatos, listaExtras)) // Mostrar el fragmento "Carrito" al seleccionar el elemento correspondiente
                return@OnNavigationItemSelectedListener true
            }
            R.id.itemMiCuenta -> {
                showFragment(MiCuenta()) // Mostrar el fragmento "MiCuenta" al seleccionar el elemento correspondiente
                return@OnNavigationItemSelectedListener true
            }
            // Puedes agregar más casos para otros elementos del menú de navegación aquí si es necesario
        }
        false // Devolver falso por defecto si no se maneja el elemento seleccionado
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main) // Establecer el diseño de la actividad

        listaPlatos = intent.getSerializableExtra("platos") as ArrayList<Plato>
        listaExtras = intent.getSerializableExtra("extras") as ArrayList<Extra>

        usuario = intent.getParcelableExtra<Usuario>("usuario")!!
        carritoUsuario= intent.getSerializableExtra("carrito") as ArrayList<Pedido>
        println("Tamaño de la lista(Main): ${carritoUsuario.size}")
        /* for (plato in listaPlatos){
             Log.d("MiApp", "datos plato ${plato.nombre}")
         }*/

        navigation = findViewById(R.id.navMenu) // Inicializar la vista BottomNavigationView
        navigation.setOnNavigationItemSelectedListener(onNavMenuListener) // Configurar el listener para elementos de navegación

        sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE) // Inicializar SharedPreferences para almacenar el idioma

        // Obtener el idioma guardado o utilizar "es" (español) como idioma predeterminado si no se ha guardado uno
        val savedLanguage = sharedPref.getString("language", "es") ?: "es"

        setAppLocale(savedLanguage) // Configurar el idioma de la aplicación

        // Llamar a la función para configurar el idioma de los elementos del menú
        setMenuItemsLanguage(savedLanguage)

        // Establecer el fragmento "Home" como fragmento inicial si no hay estado previo
        if (savedInstanceState == null) {
            showFragment(Home())
        }

        // Inicialización de botones para cambiar el idioma
        val englishButton = findViewById<ImageView>(R.id.imageViewEnglish)
        val spanishButton = findViewById<ImageView>(R.id.imageViewEspañol)
        val euskeraButton = findViewById<ImageView>(R.id.imageViewEuskera)

        // Configurar listeners para los botones de cambio de idioma
        englishButton.setOnClickListener {
            setAndApplyLanguage("en") // Cambiar y aplicar el idioma a inglés
        }

        spanishButton.setOnClickListener {
            setAndApplyLanguage("es") // Cambiar y aplicar el idioma a español
        }

        euskeraButton.setOnClickListener {
            setAndApplyLanguage("eu") // Cambiar y aplicar el idioma a euskera
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
        sharedPref.edit().putString("language", languageCode).apply() // Guardar el idioma en SharedPreferences
        setAppLocale(languageCode) // Configurar el idioma de la aplicación
        updateResources(languageCode) // Actualizar los recursos en el idioma seleccionado
        recreate() // Reiniciar la actividad para aplicar el cambio de idioma
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
        // Puedes agregar más elementos del menú aquí si es necesario
    }

    // Función para obtener los recursos en el idioma deseado
    private fun getResourcesForLocale(context: Context, languageCode: String): Resources {
        val locale = Locale(languageCode)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config).resources
    }

    fun mostrarCarrito() {
        showFragment(Carrito(carritoUsuario, listaPlatos, listaExtras))
    }


}
