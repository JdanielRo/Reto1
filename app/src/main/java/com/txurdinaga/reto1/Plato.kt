import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class Plato(
    val idPlato: String,
    val nombre: String,
    val precio: Double,
    val descripcion: String,
    val stock: Int,
    val tipo: String
) : Serializable