package com.gaatho.rent.features.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gaatho.rent.core.ui.components.AppTextField
import com.gaatho.rent.core.ui.components.CardTitle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person

@Composable
fun EditProfileDialog(
    initialName: String,
    initialPhone: String,
    initialEmail: String,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }

    com.gaatho.rent.core.ui.components.AppDialog(
        icon = Icons.Default.Person,
        title = "Edit Profile",
        confirmText = "Save",
        dismissText = "Cancel",
        onConfirm = {
            if (name.isNotBlank() && phone.isNotBlank()) {
                onSave(name, phone)
            }
        },
        onDismiss = onDismiss,
        bodyContent = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    placeholder = "Enter your full name",
                    errorMessage = if (name.isBlank()) "Name cannot be empty" else null
                )

                AppTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone Number",
                    placeholder = "Enter your phone number",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                    errorMessage = if (phone.isBlank()) "Phone cannot be empty" else null
                )
            }
        }
    )
}
