package com.example.hellmedhub

import android.graphics.Color as ColorAndroid
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(ColorAndroid.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(ColorAndroid.TRANSPARENT)
        )
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Rojo,
                    background = Negro,
                    surface = Panel,
                    onSurface = Color.White,
                    onBackground = Color.White
                )
            ) {
                HellMedApp()
            }
        }
    }
}

// Controla en qué "fase" está la app: splash -> login/registro/recuperar -> cargando -> app
@Composable
fun HellMedApp() {
    val ctx = LocalContext.current
    var fase by remember { mutableStateOf("splash") }
    var usuario by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }

    fun entrar(correo: String) {
        Usuarios.iniciarSesion(ctx, correo)
        usuario = correo.trim().lowercase()
        nombre = Usuarios.nombre(ctx, usuario)
        fase = "cargando"
    }

    BackHandler(enabled = fase == "registro" || fase == "recuperar") { fase = "login" }

    Crossfade(targetState = fase, animationSpec = tween(500), label = "fase") { f ->
        when (f) {
            "splash" -> Splash {
                val guardada = Usuarios.sesion(ctx)
                if (guardada != null) {
                    usuario = guardada
                    nombre = Usuarios.nombre(ctx, guardada)
                    fase = "app"
                } else fase = "login"
            }
            "login" -> Login(
                onEntrar = { entrar(it) },
                onRegistro = { fase = "registro" },
                onRecuperar = { fase = "recuperar" }
            )
            "registro" -> Registro(onRegistrado = { fase = "login" }, onVolver = { fase = "login" })
            "recuperar" -> Recuperar { fase = "login" }
            "cargando" -> Cargando { fase = "app" }
            "app" -> AppPrincipal(
                usuario = usuario,
                nombre = nombre,
                onNombre = { nombre = it },
                onSalir = {
                    Usuarios.cerrarSesion(ctx)
                    fase = "login"
                }
            )
        }
    }
}

// Pantalla principal: barra superior con menú, 4 pestañas abajo y el perfil.
@Composable
fun AppPrincipal(usuario: String, nombre: String, onNombre: (String) -> Unit, onSalir: () -> Unit) {
    var tab by remember { mutableStateOf("inicio") }
    var acercaDe by remember { mutableStateOf(false) }

    // Datos de ejemplo. Luego se reemplazan por los de un servidor.
    val niveles = remember {
        mutableStateListOf(
            Nivel(1, "Misa Negra", "Jugador123", "Sobrevive tres oleadas dentro de la nave principal.", "Catedral en Ruinas", 320, 58),
            Nivel(2, "Última Guardia", "GamerPro", "Defiende el hospital de campaña hasta el amanecer.", "Pueblo Abandonado", 210, 41),
            Nivel(3, "Puerta al Abismo", "Medic_99", "Cruza el puente antes de que se derrumbe.", "Puertas de Purgatorio", 150, 33),
            Nivel(4, "Pantano Rojo", "NoobSlayer", "Mucha niebla y pocos suministros.", "El Pantano Maldito", 95, 17)
        )
    }
    val posts = remember {
        mutableStateListOf(
            Post("Consejos para el jefe final", "Jugador123"),
            Post("¡Mi mejor puntuación hasta ahora!", "GamerPro"),
            Post("Buscando equipo para cooperativo", "NoobSlayer")
        )
    }
    val tabs = listOf(
        Triple("inicio", "Inicio", Icons.Filled.Home),
        Triple("info", "Info", Icons.Filled.Info),
        Triple("taller", "Taller", Icons.Filled.Star),
        Triple("comunidad", "Comunidad", Icons.Filled.Face)
    )

    // Opciones del menú ☰ (arriba a la izquierda)
    val opciones: List<Pair<String, () -> Unit>> = listOf(
        "Inicio" to { tab = "inicio" },
        "Info" to { tab = "info" },
        "Taller" to { tab = "taller" },
        "Comunidad" to { tab = "comunidad" },
        "Mi perfil" to { tab = "perfil" },
        "Acerca de" to { acercaDe = true },
        "Cerrar sesión" to onSalir
    )

    Scaffold(
        containerColor = Negro,
        bottomBar = {
            NavigationBar(containerColor = Panel) {
                tabs.forEach { (id, etiqueta, icono) ->
                    NavigationBarItem(
                        selected = tab == id,
                        onClick = { tab = id },
                        icon = { Icon(icono, etiqueta) },
                        label = { Text(etiqueta, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = Rojo,
                            unselectedIconColor = Color.White.copy(alpha = 0.85f),
                            unselectedTextColor = Color.White.copy(alpha = 0.85f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Barra(
                enPerfil = tab == "perfil",
                opciones = opciones,
                onPerfil = { tab = "perfil" },
                onCerrar = { tab = "inicio" }
            )
            // Transición animada al cambiar de pantalla
            Crossfade(targetState = tab, animationSpec = tween(350), label = "tab") { t ->
                when (t) {
                    "inicio" -> Inicio(nombre, niveles) { tab = "taller" }
                    "info" -> Info()
                    "taller" -> Taller(niveles, nombre)
                    "comunidad" -> Comunidad(posts, nombre)
                    "perfil" -> Perfil(usuario, nombre, niveles.count { it.suscrito }, onNombre, onSalir)
                }
            }
        }
    }

    if (acercaDe) {
        AlertDialog(
            onDismissRequest = { acercaDe = false },
            confirmButton = { TextButton(onClick = { acercaDe = false }) { Text("Aceptar") } },
            title = { Text("HellMed Hub") },
            text = { Text("Aplicación móvil de HELLMED: Pulse of the Damned.\nUTNAY · Desarrollo Móvil") }
        )
    }
}

@Composable
fun Barra(
    enPerfil: Boolean,
    opciones: List<Pair<String, () -> Unit>>,
    onPerfil: () -> Unit,
    onCerrar: () -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().background(Panel).padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            IconButton(onClick = { abierto = true }) {
                Icon(Icons.Filled.Menu, "Menú", tint = Color.White)
            }
            DropdownMenu(
                expanded = abierto,
                onDismissRequest = { abierto = false },
                modifier = Modifier.background(Panel)
            ) {
                opciones.forEach { (texto, accion) ->
                    DropdownMenuItem(
                        text = { Text(texto, color = Color.White) },
                        onClick = {
                            abierto = false
                            accion()
                        }
                    )
                }
            }
        }
        Text(
            "HELLMED",
            color = Naranja,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = if (enPerfil) onCerrar else onPerfil) {
            Icon(
                if (enPerfil) Icons.Filled.Close else Icons.Filled.Person,
                "Perfil",
                tint = Color.White
            )
        }
    }
}