import java.io.Serializable

class Extra(
    val idExtra: String,
    val nombre: String,
    val precio: Double,
    val descripcion: String,
    val stock: Int,
    val tipo: String
) : Serializable
