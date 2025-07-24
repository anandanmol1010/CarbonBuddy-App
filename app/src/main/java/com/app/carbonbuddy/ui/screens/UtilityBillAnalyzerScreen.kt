package com.app.carbonbuddy.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.app.carbonbuddy.viewmodel.BillAnalyzerViewModel
import com.app.carbonbuddy.viewmodel.BillData
import com.app.carbonbuddy.viewmodel.BillType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilityBillAnalyzerScreen(viewModel: BillAnalyzerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onImageSelected(it)
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            uiState.selectedImageUri?.let {
                viewModel.onImageSelected(it)
            }
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8F5E9), Color(0xFFB3E5FC))
                )
            )
            .padding(16.dp)
            .padding(bottom = 90.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Smart Bill Analyzer",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF388E3C)
                ),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        
        // Image Upload Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "📄 Upload Your Bill",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1976D2)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Image preview or upload area
                    if (uiState.selectedImageUri != null) {
                        AsyncImage(
                            model = uiState.selectedImageUri,
                            contentDescription = "Selected bill image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .border(
                                    2.dp,
                                    Color(0xFF1976D2),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    contentDescription = "Upload",
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    "Tap to upload bill image",
                                    color = Color(0xFF1976D2),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Upload buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                viewModel.resetState() // Clear previous data
                                imagePickerLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery")
                        }
                        
                        OutlinedButton(
                            onClick = { 
                                viewModel.handleManualInputClick()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manual")
                        }
                    }
                }
            }
        }
        
        // Loading indicator
        if (uiState.isLoading) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF388E3C)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "🔍 Extracting text from bill...",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Using ML Kit OCR technology",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // AI Processing indicator
        if (uiState.isAiProcessing) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF9C27B0)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "📊 Calculating emissions...",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Analyzing your bill and generating eco-tips",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Calculate button for OCR (without showing extracted text)
        if (uiState.extractedText.isNotEmpty() && uiState.showCalculateButton && !uiState.isAiProcessing) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "📊 Ready to Calculate",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Text extracted successfully from your bill",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Calculate button
                        Button(
                            onClick = { viewModel.calculateWithAI() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = "Calculate",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Calculate Emissions",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        // Manual input section (hide when processing or when results are shown)
        if (uiState.showManualInput && !uiState.isAiProcessing && uiState.emissionResult.totalEmission == 0.0) {
            item {
                ManualInputSection(
                    billData = uiState.billData,
                    onBillDataChange = { viewModel.updateManualInput(it) },
                    showCalculateButton = uiState.showCalculateButton,
                    onCalculateClick = { viewModel.calculateWithAI() }
                )
            }
        }
        
        // Bill Type Mismatch Warning
        if (uiState.showMismatchWarning && uiState.detectedBillType != null && uiState.selectedBillType != null) {
            item {
                BillTypeMismatchCard(
                    selectedType = uiState.selectedBillType!!,
                    detectedType = uiState.detectedBillType!!,
                    onCorrectType = { viewModel.correctBillType() },
                    onKeepSelected = { viewModel.dismissMismatchWarning() },
                    onChangeBillType = { viewModel.changeBillType() }
                )
            }
        }
        
        // Results section (hide if mismatch warning is shown)
        if (uiState.emissionResult.totalEmission > 0 && !uiState.showMismatchWarning) {
            item {
                EmissionResultCard(
                    result = uiState.emissionResult,
                    selectedBillType = uiState.selectedBillType,
                    onChangeBillType = { viewModel.changeBillType() }
                )
            }
        }
        
        // Eco tips section (hide if mismatch warning is shown)
        if (uiState.ecoTips.isNotEmpty() && !uiState.showMismatchWarning) {
            item {
                EcoTipsCard(uiState.ecoTips)
            }
        }
        
        // Error message
        uiState.errorMessage?.let { error ->
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            error,
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
    
    // Bill Type Selection Dialog
    if (uiState.showBillTypeDialog) {
        BillTypeSelectionDialog(
            onBillTypeSelected = { billType ->
                viewModel.onBillTypeSelected(billType)
            },
            onDismiss = {
                viewModel.dismissBillTypeDialog()
            }
        )
    }
}

@Composable
fun ManualInputSection(
    billData: BillData,
    onBillDataChange: (BillData) -> Unit,
    showCalculateButton: Boolean = false,
    onCalculateClick: () -> Unit = {}
) {
    var electricity by remember { mutableStateOf(if (billData.electricityUnits == 0.0) "" else billData.electricityUnits.toString()) }
    var gas by remember { mutableStateOf(if (billData.gasConsumption == 0.0) "" else billData.gasConsumption.toString()) }
    var water by remember { mutableStateOf(if (billData.waterUsage == 0.0) "" else billData.waterUsage.toString()) }
    var internet by remember { mutableStateOf(if (billData.internetData == 0.0) "" else billData.internetData.toString()) }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "⚡ Manual Input",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF388E3C)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = electricity,
                onValueChange = { 
                    electricity = it
                    onBillDataChange(
                        billData.copy(
                            electricityUnits = it.toDoubleOrNull() ?: 0.0
                        )
                    )
                },
                label = { Text("Electricity (kWh)") },
                leadingIcon = {
                    Icon(Icons.Default.Bolt, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = gas,
                onValueChange = { 
                    gas = it
                    onBillDataChange(
                        billData.copy(
                            gasConsumption = it.toDoubleOrNull() ?: 0.0
                        )
                    )
                },
                label = { Text("Gas (Litres)") },
                leadingIcon = {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = water,
                onValueChange = { 
                    water = it
                    onBillDataChange(
                        billData.copy(
                            waterUsage = it.toDoubleOrNull() ?: 0.0
                        )
                    )
                },
                label = { Text("Water (Litres)") },
                leadingIcon = {
                    Icon(Icons.Default.Water, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = internet,
                onValueChange = { 
                    internet = it
                    onBillDataChange(
                        billData.copy(
                            internetData = it.toDoubleOrNull() ?: 0.0
                        )
                    )
                },
                label = { Text("Internet Data (GB)") },
                leadingIcon = {
                    Icon(Icons.Default.Wifi, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Calculate with AI button for manual input
            if (showCalculateButton) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onCalculateClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = "Calculate",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Calculate Emissions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmissionResultCard(
    result: com.app.carbonbuddy.viewmodel.EmissionResult,
    selectedBillType: BillType? = null,
    onChangeBillType: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📊 Emission Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1976D2)
                )
                
                selectedBillType?.let { billType ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            billType.icon,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            billType.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Individual emissions
            if (result.electricityEmission > 0) {
                EmissionRow(
                    icon = "⚡",
                    label = "Electricity",
                    value = result.electricityEmission
                )
            }
            
            if (result.gasEmission > 0) {
                EmissionRow(
                    icon = "🔥",
                    label = "Gas",
                    value = result.gasEmission
                )
            }
            
            if (result.waterEmission > 0) {
                EmissionRow(
                    icon = "💧",
                    label = "Water",
                    value = result.waterEmission
                )
            }
            
            if (result.internetEmission > 0) {
                EmissionRow(
                    icon = "🌐",
                    label = "Internet",
                    value = result.internetEmission
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Total emission
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total CO₂ Emission",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${"%.2f".format(result.totalEmission)} kg",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (result.totalEmission > 200) Color(0xFFD32F2F) else Color(0xFF388E3C)
                )
            }
        }
    }
}

@Composable
fun EmissionRow(icon: String, label: String, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                icon,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            "${"%.2f".format(value)} kg",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EcoTipsCard(tips: List<String>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "🌱 Eco Tips",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF388E3C)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            tips.forEach { tip ->
                Text(
                    tip,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun BillTypeSelectionDialog(
    onBillTypeSelected: (BillType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Bill Type",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Which type of utility bill are you uploading?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                BillType.values().forEach { billType ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onBillTypeSelected(billType) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                billType.icon,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    billType.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    when (billType) {
                                        BillType.ELECTRICITY -> "kWh consumption"
                                        BillType.GAS -> "Litre consumption"
                                        BillType.WATER -> "Litre usage"
                                        BillType.INTERNET -> "GB data usage"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BillTypeMismatchCard(
    selectedType: BillType,
    detectedType: BillType,
    onCorrectType: () -> Unit,
    onKeepSelected: () -> Unit,
    onChangeBillType: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Bill Type Mismatch Detected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "You selected ${selectedType.icon} ${selectedType.displayName}, but we detected ${detectedType.icon} ${detectedType.displayName} from your bill.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onCorrectType,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Use ${detectedType.displayName}")
            }
        }
    }
}