package com.devsoft.desktop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** Dropdown selector with optional search box. */
@Composable
fun <T> SelectField(
    label: String,
    options: List<T>,
    selected: T?,
    display: (T) -> String,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
    allowNone: String? = null,
    searchable: Boolean = false
) {
    var open by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    Box(modifier) {
        OutlinedButton(onClick = { open = true; query = "" }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Column(Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, color = Palette.Muted)
                Text(selected?.let(display) ?: allowNone ?: "Choisir…", fontSize = 14.sp, color = Palette.Text, maxLines = 1)
            }
            Icon(Icons.Filled.ArrowDropDown, null)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.widthIn(min = 260.dp).heightIn(max = 420.dp)) {
            if (searchable) {
                OutlinedTextField(
                    query, { query = it }, placeholder = { Text("Rechercher…") }, singleLine = true,
                    modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth()
                )
            }
            if (allowNone != null) DropdownMenuItem(text = { Text(allowNone, color = Palette.Muted) }, onClick = { onSelect(null); open = false })
            options.filter { query.isBlank() || display(it).contains(query, ignoreCase = true) }.take(200).forEach { o ->
                DropdownMenuItem(text = { Text(display(o)) }, onClick = { onSelect(o); open = false })
            }
        }
    }
}

/** Modal form used by the CRUD screens. */
@Composable
fun FormDialog(
    title: String,
    confirmLabel: String = "Enregistrer",
    confirmEnabled: Boolean = true,
    width: Int = 520,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = MaterialTheme.shapes.large, color = Palette.Surface, modifier = Modifier.width(width.dp)) {
            Column(Modifier.padding(22.dp)) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                Column(Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onConfirm, enabled = confirmEnabled) { Text(confirmLabel) }
                }
            }
        }
    }
}

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier.fillMaxWidth(), singleLine: Boolean = true) {
    OutlinedTextField(value, onChange, label = { Text(label) }, singleLine = singleLine, modifier = modifier)
}

@Composable
fun SearchBox(value: String, onChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier.width(320.dp)) {
    OutlinedTextField(value, onChange, placeholder = { Text(placeholder) }, singleLine = true, modifier = modifier)
}
