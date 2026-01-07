package com.example.cashier.presentation.screen.form

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cashier.presentation.screen.home.HomeEvent
import com.example.cashier.presentation.screen.home.HomeUiState
import com.example.cashier.presentation.screen.home.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    viewModel: HomeViewModel = koinViewModel(),
    navController: NavController,
    id: Int?
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Observe the result from the CameraScreen
    val capturedImageUri = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Uri>("capturedImageUri")?.value

    LaunchedEffect(key1 = id) {
        if (id != null && id != -1) {
            viewModel.onEvent(HomeEvent.LoadCashier(id))
        } else {
            viewModel.onEvent(HomeEvent.ClearForm)
        }
    }

    // This is the new centralized logic.
    // When a new image is captured, update the ViewModel and consume the result.
    LaunchedEffect(capturedImageUri) {
        if (capturedImageUri != null) {
            viewModel.onEvent(HomeEvent.OnStruckChanged(capturedImageUri))
            navController.currentBackStackEntry?.savedStateHandle?.remove<Uri>("capturedImageUri")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (id == null || id == -1) "Add Item" else "Edit Item") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (uiState.isLoading && id != null && id != -1) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                AddCashierFormContent(uiState, viewModel::onEvent, navController, id)
            }
        }
    }
}

@Composable
fun AddCashierFormContent(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    navController: NavController,
    id: Int?
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val isEditMode = id != null && id != -1
    val coroutine = rememberCoroutineScope()

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
            isError = uiState.nameInputError != null,
            supportingText = { uiState.nameInputError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.nameOutput,
            onValueChange = { if (it.length <= 50) onEvent(HomeEvent.OnNameOutputChanged(it)) },
            label = { Text("Name Output") },
            isError = uiState.nameOutputError != null,
            supportingText = { uiState.nameOutputError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = uiState.date,
                onValueChange = { },
                label = { Text("Date") },
                modifier = Modifier.weight(1f).clickable { datePickerDialog.show() },
                enabled = false,
                trailingIcon = { IconButton(onClick = { datePickerDialog.show() }) { Icon(Icons.Default.DateRange, "") } },
                isError = uiState.dateError != null,
                supportingText = { uiState.dateError?.let { Text(it) } }
            )
            OutlinedTextField(
                value = uiState.time,
                onValueChange = { },
                label = { Text("Time") },
                modifier = Modifier.weight(1f).clickable { timePickerDialog.show() },
                enabled = false,
                trailingIcon = { IconButton(onClick = { timePickerDialog.show() }) { Icon(Icons.Default.Edit, "") } },
                isError = uiState.timeError != null,
                supportingText = { uiState.timeError?.let { Text(it) } }
            )
        }
        OutlinedTextField(
            value = uiState.nominal,
            onValueChange = { onEvent(HomeEvent.OnNominalChanged(it)) },
            label = { Text("Nominal") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = uiState.nominalError != null,
            supportingText = { uiState.nominalError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Add Photo")
            }
            // The single source of truth is now uiState.struck
            if (uiState.struck.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(uiState.struck),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).padding(start = 16.dp)
                )
                // This now works correctly
                IconButton(onClick = { onEvent(HomeEvent.ClearStruckImage) }) {
                    Icon(Icons.Default.Clear, "Clear image")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                coroutine.launch {
                    onEvent(HomeEvent.InsertOrUpdate(id))
                    navController.popBackStack()
                }
            }
        ) {
            Text(if (isEditMode) "Update" else "Add")
        }
    }
}