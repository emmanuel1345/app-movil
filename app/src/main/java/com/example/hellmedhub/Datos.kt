package com.example.hellmedhub

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Patterns
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.security.MessageDigest

// ---------- Modelos ----------

val ZONAS = listOf(
    "Pueblo Abandonado", "Catedral en Ruinas", "Bosque Maldito",
    "Ciudad Infernal", "Fortaleza del Demonio",
    "Puertas de Purgatorio", "El Pantano Maldito"
)

data class Nivel(
    val id: Int,
    val nombre: String,
    val autor: String,
    val descripcion: String,
    val zona: String,
    val descargas: Int,
    val likes: Int,
    val suscrito: Boolean = false,
    val gusta: Boolean = false
)

data class Post(val titulo: String, val autor: String)

// ---------- Usuarios (se guardan en el teléfono con SharedPreferences) ----------
// Cada función de validación devuelve un mensaje de error, o null si todo salió bien.

object Usuarios {
    private fun prefs(c: Context) = c.getSharedPreferences("hellmed", Context.MODE_PRIVATE)

    private fun hash(t: String) = MessageDigest.getInstance("SHA-256")
        .digest(t.toByteArray()).joinToString("") { "%02x".format(it) }

    private fun clave(correo: String) = correo.trim().lowercase()

    private fun guardar(c: Context, correo: String, campo: String, valor: String) {
        prefs(c).edit().putString(clave(correo) + "_" + campo, valor).apply()
    }

    private fun leer(c: Context, correo: String, campo: String) =
        prefs(c).getString(clave(correo) + "_" + campo, "") ?: ""

    private fun existe(c: Context, correo: String) = prefs(c).contains(clave(correo) + "_pass")

    fun registrar(c: Context, nombre: String, correo: String, pass: String, confirmar: String, secreta: String): String? {
        if (nombre.isBlank() || correo.isBlank() || pass.isBlank() || confirmar.isBlank() || secreta.isBlank())
            return "Completa todos los campos obligatorios."
        if (!Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) return "El correo no es válido."
        if (pass.length < 6) return "La contraseña debe tener al menos 6 caracteres."
        if (pass != confirmar) return "Las contraseñas no coinciden."
        if (existe(c, correo)) return "Ese correo ya está registrado."
        guardar(c, correo, "pass", hash(pass))
        guardar(c, correo, "nombre", nombre.trim())
        guardar(c, correo, "secreta", hash(secreta.trim().lowercase()))
        return null
    }

    fun entrar(c: Context, correo: String, pass: String): String? {
        if (correo.isBlank() || pass.isBlank()) return "Escribe tu correo y tu contraseña."
        if (!existe(c, correo)) return "Ese correo no está registrado."
        if (leer(c, correo, "pass") != hash(pass)) return "Contraseña incorrecta."
        return null
    }

    fun recuperar(c: Context, correo: String, secreta: String, nueva: String): String? {
        if (correo.isBlank() || secreta.isBlank() || nueva.isBlank()) return "Completa todos los campos obligatorios."
        if (!existe(c, correo)) return "Ese correo no está registrado."
        if (leer(c, correo, "secreta") != hash(secreta.trim().lowercase())) return "La palabra secreta es incorrecta."
        if (nueva.length < 6) return "La contraseña debe tener al menos 6 caracteres."
        guardar(c, correo, "pass", hash(nueva))
        return null
    }

    fun nombre(c: Context, correo: String) = leer(c, correo, "nombre")

    fun cambiarNombre(c: Context, correo: String, nuevo: String): String? {
        if (nuevo.isBlank()) return "El nombre no puede estar vacío."
        guardar(c, correo, "nombre", nuevo.trim())
        return null
    }

    fun cambiarPass(c: Context, correo: String, actual: String, nueva: String, confirmar: String): String? {
        if (actual.isBlank() || nueva.isBlank() || confirmar.isBlank()) return "Completa todos los campos obligatorios."
        if (leer(c, correo, "pass") != hash(actual)) return "La contraseña actual es incorrecta."
        if (nueva.length < 6) return "La nueva contraseña debe tener al menos 6 caracteres."
        if (nueva != confirmar) return "Las contraseñas no coinciden."
        guardar(c, correo, "pass", hash(nueva))
        return null
    }

    // Sesión iniciada
    fun sesion(c: Context): String? = prefs(c).getString("sesion", null)
    fun iniciarSesion(c: Context, correo: String) = prefs(c).edit().putString("sesion", clave(correo)).apply()
    fun cerrarSesion(c: Context) = prefs(c).edit().remove("sesion").apply()

    // Foto de perfil (se copia a la memoria interna de la app)
    private fun archivoFoto(c: Context, correo: String) = File(c.filesDir, "foto_" + hash(clave(correo)).take(16) + ".jpg")

    fun guardarFoto(c: Context, correo: String, uri: Uri) {
        c.contentResolver.openInputStream(uri)?.use { entrada ->
            archivoFoto(c, correo).outputStream().use { salida -> entrada.copyTo(salida) }
        }
    }

    fun foto(c: Context, correo: String): ImageBitmap? {
        val f = archivoFoto(c, correo)
        if (!f.exists()) return null
        val opciones = BitmapFactory.Options().apply { inSampleSize = 4 }
        return BitmapFactory.decodeFile(f.path, opciones)?.asImageBitmap()
    }
}