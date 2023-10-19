import android.os.Parcel
import android.os.Parcelable

class Usuario(
    val idUsuario: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val telefono: String,
    val direccion: String,
    val fechaNacimiento: String
) : Parcelable {
    constructor() : this("", "", "", "", "", "", "")

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idUsuario)
        parcel.writeString(nombre)
        parcel.writeString(apellido)
        parcel.writeString(correo)
        parcel.writeString(telefono)
        parcel.writeString(direccion)
        parcel.writeString(fechaNacimiento)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Usuario> {
        override fun createFromParcel(parcel: Parcel): Usuario {
            return Usuario(parcel)
        }

        override fun newArray(size: Int): Array<Usuario?> {
            return arrayOfNulls(size)
        }
    }
}
