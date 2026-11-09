package com.example.hellmedhub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Paleta del documento de diseño
val Negro = Color(0xFF0D0D0D)
val Panel = Color(0xFF2B0000)
val Rojo = Color(0xFFC8102E)
val Naranja = Color(0xFFFFA726)
val Gris = Color(0xFFB0B0B0)
val Alerta = Color(0xFFFF6B6B)

@Composable
fun Lista(content: LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
fun Caja(titulo: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Panel)
            .border(1.dp, Rojo, RoundedCornerShape(4.dp))
            .padding(16.dp)
    ) {
        Text(titulo, color = Naranja, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
fun BotonRojo(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Rojo, contentColor = Color.White),
        border = BorderStroke(1.dp, Naranja)
    ) {
        Text(texto.uppercase(), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Chips(opciones: List<String>, actual: String, onElegir: (String) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opciones.forEach { o ->
            FilterChip(
                selected = actual == o,
                onClick = { onElegir(o) },
                label = { Text(o) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Rojo,
                    selectedLabelColor = Color.White,
                    labelColor = Color.White
                )
            )
        }
    }
}

// Campo de texto. Si clave = true oculta el texto y muestra el botón Ver / Ocultar.
@Composable
fun Campo(valor: String, onCambio: (String) -> Unit, etiqueta: String, clave: Boolean = false) {
    var ver by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (clave && !ver) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (clave) {
            { TextButton(onClick = { ver = !ver }) { Text(if (ver) "Ocultar" else "Ver", color = Naranja) } }
        } else null
    )
}

// Mensaje de advertencia (solo se ve si hay texto)
@Composable
fun Aviso(texto: String) {
    if (texto.isNotEmpty()) Text(texto, color = Alerta, fontSize = 14.sp)
}