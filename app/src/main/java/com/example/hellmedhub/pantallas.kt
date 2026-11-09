package com.example.hellmedhub

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- 1. INICIO ----------
@Composable
fun Inicio(nombre: String, niveles: List<Nivel>, onIrTaller: () -> Unit) {
    Lista {
        item {
            Image(
                painterResource(R.drawable.hellmed_logo),
                "Logo HELLMED",
                Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop
            )
        }
        item {
            Caja("Hola, $nombre") {
                Text(
                    "Consulta la información del juego y descubre los niveles creados por la comunidad en el Taller.",
                    color = Color.White
                )
            }
        }
        item {
            Caja("Niveles más populares") {
                niveles.sortedByDescending { it.likes }.take(3).forEach {
                    Text("${it.nombre}  ·  ♥ ${it.likes}", color = Color.White, modifier = Modifier.padding(vertical = 4.dp))
                }
                Spacer(Modifier.height(8.dp))
                BotonRojo("Ver el Taller", onIrTaller)
            }
        }
    }
}

// ---------- 2. INFO ----------
@Composable
fun Info() {
    var tema by remember { mutableStateOf("Historia") }
    Lista {
        item { Chips(listOf("Historia", "Personajes", "Enemigos", "Mapas"), tema) { tema = it } }
        when (tema) {
            "Historia" -> item {
                Caja("Contexto") {
                    Text(
                        "HELLMED: Pulse of the Damned plantea un mundo invadido por fuerzas infernales. Un sacerdote y un paramédico deben abrirse camino por escenarios devastados por las hordas demoníacas y rescatar a los sobrevivientes antes de que sea tarde.",
                        color = Color.White
                    )
                }
            }
            "Personajes" -> {
                item {
                    Caja("Cura Clásico") {
                        Text("Representa la figura del sacerdote, con vestimenta religiosa y una mitra o sombrero alto.", color = Color.White)
                    }
                }
                item {
                    Caja("Paramédico") {
                        Text("Perfil médico con lentes, uniforme y estetoscopio. Su temática está ligada a la ambulancia y las jeringas del logotipo.", color = Color.White)
                    }
                }
            }
            "Enemigos" -> item {
                Caja("Criaturas infernales") {
                    Text("Hordas infernales, criaturas del pantano y otros demonios. La lista final de enemigos está por definir.", color = Color.White)
                }
            }
            "Mapas" -> item {
                Caja("Zonas del juego") {
                    ZONAS.forEachIndexed { i, z ->
                        Text(
                            "${i + 1}. $z" + if (i >= 5) "  (DLC)" else "",
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
            else -> {}
        }
    }
}

// ---------- 3. TALLER (niveles de la comunidad) ----------
@Composable
fun Taller(niveles: MutableList<Nivel>, autor: String) {
    var busca by remember { mutableStateOf("") }
    var zona by remember { mutableStateOf("Todas") }
    var orden by remember { mutableStateOf("Populares") }
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var zonaNueva by remember { mutableStateOf(ZONAS[0]) }
    var error by remember { mutableStateOf("") }

    val filtrados = niveles
        .filter { (zona == "Todas" || it.zona == zona) && it.nombre.contains(busca, ignoreCase = true) }
        .let { if (orden == "Populares") it.sortedByDescending { n -> n.likes } else it }

    Lista {
        item {
            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                label = { Text("Buscar nivel") },
                leadingIcon = { Icon(Icons.Filled.Search, "Buscar") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { Chips(listOf("Todas") + ZONAS, zona) { zona = it } }
        item { Chips(listOf("Populares", "Recientes"), orden) { orden = it } }

        items(filtrados, key = { it.id }) { n ->
            Caja(n.nombre) {
                Text("Por ${n.autor}  ·  ${n.zona}", color = Gris, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text(n.descripcion.ifBlank { "Sin descripción." }, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        val i = niveles.indexOfFirst { it.id == n.id }
                        niveles[i] = n.copy(gusta = !n.gusta, likes = n.likes + if (n.gusta) -1 else 1)
                    }) {
                        Icon(Icons.Filled.Favorite, "Me gusta", tint = if (n.gusta) Rojo else Gris)
                    }
                    Text("${n.likes}", color = Color.White)
                    Spacer(Modifier.weight(1f))
                    Text("${n.descargas} suscriptores", color = Gris, fontSize = 12.sp)
                }
                BotonRojo(if (n.suscrito) "Suscrito" else "Suscribirse") {
                    val i = niveles.indexOfFirst { it.id == n.id }
                    niveles[i] = n.copy(
                        suscrito = !n.suscrito,
                        descargas = n.descargas + if (n.suscrito) -1 else 1
                    )
                }
            }
        }

        item {
            Caja("Publicar nivel") {
                Campo(nombre, { nombre = it }, "Nombre del nivel (obligatorio)")
                Spacer(Modifier.height(8.dp))
                Campo(descripcion, { descripcion = it }, "Descripción (opcional)")
                Spacer(Modifier.height(8.dp))
                Chips(ZONAS, zonaNueva) { zonaNueva = it }
                Aviso(error)
                Spacer(Modifier.height(8.dp))
                BotonRojo("Publicar") {
                    if (nombre.isBlank()) {
                        error = "El nombre del nivel es obligatorio."
                    } else {
                        val id = (niveles.maxOfOrNull { it.id } ?: 0) + 1
                        niveles.add(0, Nivel(id, nombre.trim(), autor, descripcion.trim(), zonaNueva, 0, 0))
                        nombre = ""
                        descripcion = ""
                        error = ""
                    }
                }
            }
        }
    }
}

// ---------- 4. COMUNIDAD ----------
@Composable
fun Comunidad(posts: MutableList<Post>, autor: String) {
    var titulo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Lista {
        item {
            Caja("Crear nueva publicación") {
                Campo(titulo, { titulo = it }, "Escribe tu publicación aquí...")
                Aviso(error)
                Spacer(Modifier.height(8.dp))
                BotonRojo("Publicar") {
                    if (titulo.isBlank()) {
                        error = "Escribe algo antes de publicar."
                    } else {
                        posts.add(0, Post(titulo.trim(), autor))
                        titulo = ""
                        error = ""
                    }
                }
            }
        }
        item {
            Caja("Últimas publicaciones") {
                posts.forEach { p ->
                    Text(p.titulo, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Por ${p.autor}", color = Gris, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

// ---------- 5. PERFIL (foto, nombre, contraseña, cerrar sesión) ----------
@Composable
fun Perfil(correo: String, nombre: String, suscritos: Int, onNombre: (String) -> Unit, onSalir: () -> Unit) {
    val ctx = LocalContext.current
    var foto by remember { mutableStateOf(Usuarios.foto(ctx, correo)) }
    var nuevoNombre by remember { mutableStateOf(nombre) }
    var errorNombre by remember { mutableStateOf("") }
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var errorPass by remember { mutableStateOf("") }

    val selector = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Usuarios.guardarFoto(ctx, correo, uri)
            foto = Usuarios.foto(ctx, correo)
        }
    }

    Lista {
        item {
            Caja("Mi cuenta") {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.size(110.dp).clip(CircleShape).background(Rojo),
                        contentAlignment = Alignment.Center
                    ) {
                        val f = foto
                        if (f != null) {
                            Image(f, "Foto de perfil", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Filled.Person, "Sin foto", tint = Color.White, modifier = Modifier.size(64.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(nombre, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(correo, color = Gris, fontSize = 12.sp)
                Text("Niveles suscritos: $suscritos", color = Color.White, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(8.dp))
                BotonRojo("Cambiar foto de perfil") {
                    selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }
        }
        item {
            Caja("Cambiar nombre") {
                Campo(nuevoNombre, { nuevoNombre = it }, "Nombre")
                Aviso(errorNombre)
                Spacer(Modifier.height(8.dp))
                BotonRojo("Guardar nombre") {
                    val e = Usuarios.cambiarNombre(ctx, correo, nuevoNombre)
                    if (e == null) {
                        onNombre(nuevoNombre.trim())
                        errorNombre = ""
                        Toast.makeText(ctx, "Nombre actualizado", Toast.LENGTH_SHORT).show()
                    } else errorNombre = e
                }
            }
        }
        item {
            Caja("Cambiar contraseña") {
                Campo(actual, { actual = it }, "Contraseña actual", clave = true)
                Spacer(Modifier.height(8.dp))
                Campo(nueva, { nueva = it }, "Nueva contraseña", clave = true)
                Spacer(Modifier.height(8.dp))
                Campo(confirmar, { confirmar = it }, "Confirmar nueva contraseña", clave = true)
                Aviso(errorPass)
                Spacer(Modifier.height(8.dp))
                BotonRojo("Cambiar contraseña") {
                    val e = Usuarios.cambiarPass(ctx, correo, actual, nueva, confirmar)
                    if (e == null) {
                        actual = ""
                        nueva = ""
                        confirmar = ""
                        errorPass = ""
                        Toast.makeText(ctx, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                    } else errorPass = e
                }
            }
        }
        item { BotonRojo("Cerrar sesión", onSalir) }
    }
}