package com.example.cashier.presentation.screen.home

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cashier.domain.model.Cashier
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel(), navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()

    val capturedImageUri = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<Uri>("capturedImageUri")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cashier App") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            if (uiState.errorMessage != null) {
                Text(text = uiState.errorMessage!!)
            }

            AddCashierForm(uiState, viewModel::onEvent, navController, capturedImageUri)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(uiState.cashierList) { 
                    CashierItem(cashier = it, onDelete = { viewModel.onEvent(HomeEvent.Delete(it.id)) })
                }
            }
        }
    }
}

@Composable
fun AddCashierForm(
    uiState: HomeUiState, 
    onEvent: (HomeEvent) -> Unit, 
    navController: NavController, 
    capturedImageUri: Uri?
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                navController.navigate("camera")
            }
        }
    )

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            onEvent(HomeEvent.OnDateChanged(dateFormat.format(selectedDate.time)))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onEvent(HomeEvent.OnTimeChanged(String.format("%02d:%02d", hourOfDay, minute)))
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Column {
        OutlinedTextField(
            value = uiState.nameInput,
            onValueChange = { if (it.length <= 50) onEvent(HomeEvent.OnNameInputChanged(it)) },
            label = { Text("Name Input") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.nameOutput,
            onValueChange = { if (it.length <= 50) onEvent(HomeEvent.OnNameOutputChanged(it)) },
            label = { Text("Name Output") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = uiState.date,
                onValueChange = { },
                label = { Text("Date") },
                modifier = Modifier.weight(1f).clickable { datePickerDialog.show() },
                enabled = false,
                trailingIcon = { IconButton(onClick = { datePickerDialog.show() }) { Icon(Icons.Default.DateRange, "") } }
            )
            OutlinedTextField(
                value = uiState.time,
                onValueChange = { },
                label = { Text("Time") },
                modifier = Modifier.weight(1f).clickable { timePickerDialog.show() },
                enabled = false,
                trailingIcon = { IconButton(onClick = { timePickerDialog.show() }) { Icon(Icons.Default.Edit, "") } }
            )
        }
        OutlinedTextField(
            value = uiState.nominal,
            onValueChange = { onEvent(HomeEvent.OnNominalChanged(it)) },
            label = { Text("Nominal") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Add Photo")
            }
            if (capturedImageUri != null) {
                onEvent(HomeEvent.OnStruckChanged(capturedImageUri))
                Image(
                    painter = rememberAsyncImagePainter(capturedImageUri),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).padding(start = 16.dp)
                )
            }
        }
        Button(onClick = { onEvent(HomeEvent.InsertOrUpdate()) }) {
            Text("Save")
        }
    }
}

@Composable
fun CashierItem(cashier: Cashier, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Input: ${cashier.nameInput}")
                Text(text = "Output: ${cashier.nameOutput}")
                Text(text = "Nominal: ${NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(cashier.nominal)}")
                Text(text = "${cashier.date} ${cashier.time}")
                if (cashier.struck.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(cashier.struck),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp).padding(top = 8.dp)
                    )
                }
            }
            Button(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}