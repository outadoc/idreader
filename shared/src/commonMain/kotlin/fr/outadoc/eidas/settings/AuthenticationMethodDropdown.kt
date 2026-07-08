package fr.outadoc.eidas.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.outadoc.eidas.settings.model.AuthenticationMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationMethodDropdown(
    selected: AuthenticationMethod,
    onSelected: (AuthenticationMethod) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    ),
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Authentication method") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AuthenticationMethod.entries.forEach { method ->
                DropdownMenuItem(
                    text = { Text(method.label) },
                    onClick = {
                        onSelected(method)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

private val AuthenticationMethod.label: String
    get() =
        when (this) {
            AuthenticationMethod.CAN -> "CAN"
            AuthenticationMethod.MRZ -> "MRZ"
            AuthenticationMethod.PIN -> "PIN"
            AuthenticationMethod.PUK -> "PUK"
        }
