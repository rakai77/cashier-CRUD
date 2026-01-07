package com.example.cashier.presentation.screen.form

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cashier.presentation.screen.camera.CameraScreen
import com.example.cashier.presentation.screen.home.HomeEvent
import com.example.cashier.presentation.screen.home.HomeUiState
import com.example.cashier.presentation.screen.home.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    viewModel: HomeViewModel = koinViewModel(),
    navController: NavController,
    mode: String,
    id: Int?
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCamera by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = mode, key2 = id) {
        if (mode == "update" && id != null) {
            viewModel.onEvent(HomeEvent.LoadCashier(id))
        } else if (uiState.cashier == null && uiState.nameInput.isEmpty()) {
            viewModel.onEvent(HomeEvent.ClearForm)
        }
    }

    if (showCamera) {
        CameraScreen(
            onImageCaptured = { uri ->
                viewModel.onEvent(HomeEvent.OnStruckChanged(uri))
                showCamera = false
            },
            onError = {
                showCamera = false
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(if (mode == "add") "Add Item" else "Edit Item")
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (uiState.isLoading && mode == "update") {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    AddCashierFormContentWithCamera(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        navController = navController,
                        mode = mode,
                        id = id,
                        onOpenCamera = { showCamera = true },
                        onShowBottomSheet = { showBottomSheet = true }
                    )
                }
            }
        }
        if (showBottomSheet) {
            ImagePickerBottomSheet(
                sheetState = sheetState,
                onDismiss = { showBottomSheet = false },
                onCameraClick = {
                    showBottomSheet = false
                    showCamera = true
                },
                onGalleryClick = { uri ->
                    showBottomSheet = false
                    viewModel.onEvent(HomeEvent.OnStruckChanged(uri))
                }
            )
        }
    }
}


@Composable
fun AddCashierFormContentWithCamera(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    navController: NavController,
    mode: String,
    id: Int?,
    onOpenCamera: () -> Unit,
    onShowBottomSheet: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val coroutine = rememberCoroutineScope()

    val datePickerDialog = remember {
        DatePickerDialog(
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
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onEvent(HomeEvent.OnTimeChanged(String.format("%02d:%02d", hourOfDay, minute)))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = uiState.nameInput,
            onValueChange = {
                if (it.length <= 50) onEvent(HomeEvent.OnNameInputChanged(it))
            },
            label = { Text("Masuk Ke") },
            isError = uiState.nameInputError != null,
            supportingText = { uiState.nameInputError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.nameOutput,
            onValueChange = {
                if (it.length <= 50) onEvent(HomeEvent.OnNameOutputChanged(it))
            },
            label = { Text("Dari") },
            isError = uiState.nameOutputError != null,
            supportingText = { uiState.nameOutputError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            OutlinedTextField(
                value = uiState.date,
                onValueChange = { },
                label = { Text("Tanggal") },
                modifier = Modifier
                    .weight(1f)
                    .clickable { datePickerDialog.show() },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.DateRange, "Select date")
                    }
                },
                isError = uiState.dateError != null,
                supportingText = { uiState.dateError?.let { Text(it) } }
            )

            OutlinedTextField(
                value = uiState.time,
                onValueChange = { },
                label = { Text("Waktu") },
                modifier = Modifier
                    .weight(1f)
                    .clickable { timePickerDialog.show() },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                trailingIcon = {
                    IconButton(onClick = { timePickerDialog.show() }) {
                        Icon(Icons.Default.Edit, "Select time")
                    }
                },
                isError = uiState.timeError != null,
                supportingText = { uiState.timeError?.let { Text(it) } }
            )
        }
        OutlinedTextField(
            value = uiState.nominal,
            onValueChange = {
                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                    onEvent(HomeEvent.OnNominalChanged(it))
                }
            },
            label = { Text("Jumlah") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = uiState.nominalError != null,
            supportingText = { uiState.nominalError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            prefix = { Text("Rp ") }
        )
        OutlinedTextField(
            value = uiState.description,
            onValueChange = {
                onEvent(HomeEvent.OnDescChanged(it))
            },
            label = { Text("Keterangan") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = uiState.nominalError != null,
            supportingText = { uiState.nominalError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onShowBottomSheet,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Foto Struck")
            }
            if (uiState.struck.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = uiState.struck,
                                onError = { error ->
                                    Log.e("FormContent", "Image load error: ${error.result.throwable}")
                                }
                            ),
                            contentDescription = "Captured receipt",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        IconButton(
                            onClick = { onEvent(HomeEvent.ClearStruckImage) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    CircleShape
                                )
                        ) {
                            Icon(Icons.Default.Clear, "Remove image", tint = Color.Red)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                coroutine.launch {
                    onEvent(HomeEvent.InsertOrUpdate(mode, id))
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text(if (mode == "update") "Update" else "Add")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: (Uri) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onCameraClick()
            } else {
                Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {

            } else {
                Toast.makeText(context, "Storage permission required", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val copiedUri = copyImageToInternalStorage(context, it)
                if (copiedUri != null) {
                    onGalleryClick(copiedUri)
                } else {
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Pilih Sumber Foto",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        coroutineScope.launch {
                            sheetState.hide()
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Camera",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Kamera",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Ambil foto dengan kamera",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        coroutineScope.launch {
                            sheetState.hide()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            } else {
                                galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                            delay(200)
                            galleryLauncher.launch("image/*")
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Gallery",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Galeri",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Pilih dari galeri foto",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun copyImageToInternalStorage(context: Context, sourceUri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(sourceUri)
        val file = File(context.filesDir, "IMG_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        Uri.fromFile(file)
    } catch (e: Exception) {
        Log.e("ImageCopy", "Failed to copy image", e)
        null
    }
}