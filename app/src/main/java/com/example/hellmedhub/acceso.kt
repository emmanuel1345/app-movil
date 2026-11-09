package com.example.hellmedhub

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun Logo(modifier: Modifier) {
    Image(painterResource(R.drawable.hellmed_logo), "Logo HELLMED", modifier, contentScale = ContentScale.Fit)
}

// Contenedor de los formularios: en vertical el logo va arriba, en horizontal va a la izquierda.
@Composable
fun Formulario(content: @Composable ColumnScope.() -> Unit) {
    val horizontal = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (horizontal) {
        Row(
            Modifier.fillMaxSize().background(Negro).safeDrawingPadding().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Logo(Modifier.weight(1f).fillMaxHeight())
            Column(
                Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
            ) { content() }
        }
    } else {
        Column(
            Modifier.fillMaxSize().background(Negro).safeDrawingPadding()
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Logo(Modifier.fillMaxWidth().height(160.dp))
            content()
        }
    }
}

// Pantalla animada de inicio: el logo aparece y crece suavemente.
@Composable
fun Splash(onFin: () -> Unit) {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progreso.animateTo(1f, tween(1200))
        delay(600)
        onFin()
    }
    Box(Modifier.fillMaxSize().background(Negro).safeDrawingPadding(), contentAlignment = Alignment.Center) {
        Logo(
            Modifier.fillMaxSize(0.7f).graphicsLayer {
                alpha = progreso.value
                scaleX = 0.8f + 0.2f * progreso.value
                scaleY = 0.8f + 0.2f * progreso.value
            }
        )
    }
}

// Animación de "cargando" que se ve después de iniciar sesión o registrarse.
@Composable
fun Cargando(onFin: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800)
        onFin()
    }
    val transicion = rememberInfiniteTransition(label = "pulso")
    val brillo by transicion.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "brillo"
    )
    Column(
        Modifier.fillMaxSize().background(Negro).safeDrawingPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Rojo)
        Spacer(Modifier.height(16.dp))
        Text("Cargando tu cuenta...", color = Naranja, fontSize = 18.sp, modifier = Modifier.alpha(brillo))
    }
}

@Composable
fun Login(onEntrar: (String) -> Unit, onRegistro: () -> Unit, onRecuperar: () -> Unit) {
    val ctx = LocalContext.current
    var correo by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Formulario {
        Text("Iniciar sesión", color = Naranja, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Campo(correo, { correo = it }, "Correo")
        Campo(pass, { pass = it }, "Contraseña", clave = true)
        Aviso(error)
        BotonRojo("Entrar") {
            val e = Usuarios.entrar(ctx, correo, pass)
            if (e == null) onEntrar(correo) else error = e
        }
        TextButton(onClick = onRecuperar) { Text("¿Olvidaste tu contraseña?", color = Naranja) }
        TextButton(onClick = onRegistro) { Text("Crear cuenta nueva", color = Naranja) }
    }
}

@Composable
fun Registro(onRegistrado: (String) -> Unit, onVolver: () -> Unit) {
    val ctx = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var secreta by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Formulario {
        Text("Crear cuenta", color = Naranja, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Campo(nombre, { nombre = it }, "Nombre")
        Campo(correo, { correo = it }, "Correo")
        Campo(pass, { pass = it }, "Contraseña (mínimo 6 caracteres)", clave = true)
        Campo(confirmar, { confirmar = it }, "Confirmar contraseña", clave = true)
        Campo(secreta, { secreta = it }, "Palabra secreta (para recuperar tu contraseña)")
        Aviso(error)
        BotonRojo("Registrarme") {
            val e = Usuarios.registrar(ctx, nombre, correo, pass, confirmar, secreta)
            if (e == null) {
                Toast.makeText(ctx, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show()
                onRegistrado(correo)
            } else error = e
        }
        TextButton(onClick = onVolver) { Text("Ya tengo cuenta", color = Naranja) }
    }
}

@Composable
fun Recuperar(onVolver: () -> Unit) {
    val ctx = LocalContext.current
    var correo by remember { mutableStateOf("") }
    var secreta by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Formulario {
        Text("Recuperar contraseña", color = Naranja, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Campo(correo, { correo = it }, "Correo registrado")
        Campo(secreta, { secreta = it }, "Palabra secreta")
        Campo(nueva, { nueva = it }, "Nueva contraseña", clave = true)
        Aviso(error)
        BotonRojo("Restablecer contraseña") {
            val e = Usuarios.recuperar(ctx, correo, secreta, nueva)
            if (e == null) {
                Toast.makeText(ctx, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                onVolver()
            } else error = e
        }
        TextButton(onClick = onVolver) { Text("Volver al inicio de sesión", color = Naranja) }
    }
}