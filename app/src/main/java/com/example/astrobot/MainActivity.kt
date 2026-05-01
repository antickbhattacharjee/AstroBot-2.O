package com.example.astrobot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrobot.ui.theme.AstroBotTheme
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.graphics.Bitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AstroBotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0D0D2B)
                ) {
                    AstroBotApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AstroBotApp() {
    var step by remember { mutableStateOf(1) }
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var pob by remember { mutableStateOf("") }
    var tob by remember { mutableStateOf("") }
    var prediction by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var currentStage by remember { mutableStateOf("Initializing Sensor...") }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewViewReference by remember { mutableStateOf<PreviewView?>(null) }
    var showFlash by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val performReading = {
        // Shutter Effect
        capturedBitmap = previewViewReference?.bitmap
        isLoading = true
        showFlash = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        
        scope.launch {
            delay(100)
            showFlash = false
            
            currentStage = "Aligning Cosmic Grids..."
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(1000)
            
            currentStage = "Applying Canny Edge Detection..."
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(1200)
            
            currentStage = "Executing Hough Line Transform..."
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(1000)
            
            currentStage = "Mapping Major Lines (Life, Heart, Mind)..."
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(1000)
            
            val seed = (name.lowercase().trim().hashCode() + dob.hashCode()).toLong()
            val random = Random(seed)
            
            // STEP 4: MOOLANK
            val moolank = calculateMoolank(dob)
            
            // STEP 5: ZODIAC
            val zodiac = getZodiac(dob)
            
            // STEP 6: PREDICTION LOGIC (Based on Script)
            val zodiacPred = getZodiacPrediction(zodiac)
            val moolankData = getMoolankInfo(moolank)
            val palmPreds = getPalmPredictions(random)

            // STEP 7: OUTPUT
            prediction = """
                ✨ Zodiac Sign: $zodiac
                $zodiacPred

                🔢 Moolank (Birth Number): $moolank
                $moolankData

                🖐 Palm-Based Predictions:
                ${palmPreds.joinToString(" ")}
            """.trimIndent()
            
            isLoading = false
            step = 3
        }
    }
    
    // Date Picker State
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Time Picker State
    val timePickerState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        dob = sdf.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                val hour = if (timePickerState.hour < 10) "0${timePickerState.hour}" else "${timePickerState.hour}"
                val minute = if (timePickerState.minute < 10) "0${timePickerState.minute}" else "${timePickerState.minute}"
                tob = "$hour:$minute"
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0D2B), Color(0xFF1A1A40))
                )
            )
    ) {
        StarField()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Favicon and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.favicon),
                    contentDescription = "AstroBot Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 12.dp)
                )
                Text(
                    text = "AstroBot",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = "Palmistry & Astrology AI",
                fontSize = 18.sp,
                color = Color(0xFFB0B0C0),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            when (step) {
                1 -> {
                    InfoInput(
                        label = "Full Name",
                        value = name,
                        onValueChange = { name = it }
                    )
                    
                    OutlinedCard(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E1E3F)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)))
                    ) {
                        Text(
                            text = if (dob.isEmpty()) "Select Date of Birth" else "DOB: $dob",
                            modifier = Modifier.padding(16.dp),
                            color = if (dob.isEmpty()) Color(0xFFB0B0C0) else Color.White
                        )
                    }

                    InfoInput(
                        label = "Place of Birth",
                        value = pob,
                        onValueChange = { pob = it }
                    )

                    OutlinedCard(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E1E3F)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)))
                    ) {
                        Text(
                            text = if (tob.isEmpty()) "Select Time of Birth" else "Time: $tob",
                            modifier = Modifier.padding(16.dp),
                            color = if (tob.isEmpty()) Color(0xFFB0B0C0) else Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    AstroButton(text = "Next") {
                        if (name.isNotEmpty() && dob.isNotEmpty() && pob.isNotEmpty() && tob.isNotEmpty()) {
                            step = 2
                        }
                    }
                }
                2 -> {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )
                    val glowAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "glow"
                    )

                    Text(
                        text = if (isLoading) "Detecting Major Lines..." else "Place palm on the cosmic sensor",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer Glow
                        Box(
                            modifier = Modifier
                                .size(320.dp)
                                .background(
                                    color = if (isLoading) Color.Cyan.copy(alpha = glowAlpha * 0.2f) else Color.Magenta.copy(alpha = glowAlpha * 0.2f),
                                    shape = RoundedCornerShape(32.dp)
                                )
                        )
                        
                        Surface(
                            modifier = Modifier
                                .size(300.dp * scale)
                                .padding(8.dp),
                            shape = RoundedCornerShape(32.dp),
                            color = Color(0xFF1E1E3F),
                            border = BorderStroke(2.dp, if (isLoading) Color.Cyan else Color.Magenta),
                            tonalElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (cameraPermissionState.status.isGranted) {
                                    if (isLoading && capturedBitmap != null) {
                                        Image(
                                            bitmap = capturedBitmap!!.asImageBitmap(),
                                            contentDescription = "Captured Palm",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        CameraPreview(
                                            modifier = Modifier.fillMaxSize(),
                                            onPreviewViewReady = { previewViewReference = it }
                                        )
                                    }
                                    
                                    // Guide Overlay
                                    PalmGuideOverlay(
                                        modifier = Modifier.fillMaxSize(), 
                                        isScanning = isLoading,
                                        stage = currentStage
                                    )

                                    if (showFlash) {
                                        Box(modifier = Modifier.fillMaxSize().background(Color.White))
                                    }
                                } else {
                                    Text("✋", fontSize = 120.sp)
                                }
                                
                                if (isLoading) {
                                    val scanTransition = rememberInfiniteTransition(label = "scan")
                                    val scanY by scanTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 300f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(2000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "scanY"
                                    )
                                    
                                    // Simulated Edge Detection Lines
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val yPos = (scanY / 300f) * size.height
                                        drawLine(
                                            color = Color.Cyan,
                                            start = Offset(0f, yPos),
                                            end = Offset(size.width, yPos),
                                            strokeWidth = 4f,
                                            alpha = 0.8f
                                        )
                                        
                                        // "Sparkles" along the scan line
                                        val random = Random(scanY.toLong())
                                        repeat(10) {
                                            drawCircle(
                                                color = Color.Cyan,
                                                radius = 3f,
                                                center = Offset(random.nextFloat() * size.width, yPos),
                                                alpha = random.nextFloat()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    if (isLoading) {
                        Spacer(modifier = Modifier.height(24.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(8.dp).padding(horizontal = 32.dp),
                            color = Color.Cyan,
                            trackColor = Color(0xFF1E1E3F)
                        )
                        Text(
                            if (isLoading) currentStage else "Ready for Scan",
                            color = Color.Cyan,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        if (!cameraPermissionState.status.isGranted) {
                            AstroButton(text = "Grant Camera Permission") {
                                cameraPermissionState.launchPermissionRequest()
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        AstroButton(
                            text = "Scan Palm",
                            isLoading = isLoading
                        ) {
                            performReading()
                        }
                    }
                }
                3 -> {
                    Text(
                        text = prediction,
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(bottom = 32.dp),
                        textAlign = TextAlign.Start
                    )
                    AstroButton(text = "Reset Journey") {
                        step = 1
                        name = ""
                        dob = ""
                        pob = ""
                        tob = ""
                        prediction = ""
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                text = "© 2025 Antick | Intelligent Astrology System",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PalmGuideOverlay(modifier: Modifier = Modifier, isScanning: Boolean, stage: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "guide")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 1. Technical Corner Brackets
        val cornerSize = 60f
        val padding = 40f
        val strokeWidth = 4f
        val cornerColor = Color.Cyan.copy(alpha = 0.6f)

        // Corners remain the same
        drawLine(cornerColor, Offset(padding, padding), Offset(padding + cornerSize, padding), strokeWidth)
        drawLine(cornerColor, Offset(padding, padding), Offset(padding, padding + cornerSize), strokeWidth)
        drawLine(cornerColor, Offset(width - padding, padding), Offset(width - padding - cornerSize, padding), strokeWidth)
        drawLine(cornerColor, Offset(width - padding, padding), Offset(width - padding, padding + cornerSize), strokeWidth)
        drawLine(cornerColor, Offset(padding, height - padding), Offset(padding + cornerSize, height - padding), strokeWidth)
        drawLine(cornerColor, Offset(padding, height - padding), Offset(padding, height - padding - cornerSize), strokeWidth)
        drawLine(cornerColor, Offset(width - padding, height - padding), Offset(width - padding - cornerSize, height - padding), strokeWidth)
        drawLine(cornerColor, Offset(width - padding, height - padding), Offset(width - padding, height - padding - cornerSize), strokeWidth)

        // 2. Anatomically Accurate Right Hand Silhouette (Thumb on Right, Pinky on Left)
        val handPath = Path().apply {
            // Wrist base
            moveTo(width * 0.60f, height * 0.94f)
            lineTo(width * 0.40f, height * 0.94f)
            
            // Pinky side (Hypothenar eminence) - Now on the Left
            cubicTo(width * 0.12f, height * 0.90f, width * 0.05f, height * 0.75f, width * 0.10f, height * 0.58f)
            
            // Pinky finger
            cubicTo(width * 0.06f, height * 0.45f, width * 0.08f, height * 0.32f, width * 0.18f, height * 0.32f)
            cubicTo(width * 0.26f, height * 0.32f, width * 0.24f, height * 0.45f, width * 0.22f, height * 0.55f)
            
            // Ring finger
            cubicTo(width * 0.22f, height * 0.30f, width * 0.22f, height * 0.18f, width * 0.32f, height * 0.16f)
            cubicTo(width * 0.42f, height * 0.18f, width * 0.42f, height * 0.30f, width * 0.38f, height * 0.52f)
            
            // Middle finger
            cubicTo(width * 0.38f, height * 0.25f, width * 0.40f, height * 0.08f, width * 0.50f, height * 0.06f)
            cubicTo(width * 0.60f, height * 0.08f, width * 0.62f, height * 0.25f, width * 0.58f, height * 0.52f)
            
            // Index finger
            cubicTo(width * 0.58f, height * 0.28f, width * 0.60f, height * 0.15f, width * 0.70f, height * 0.16f)
            cubicTo(width * 0.80f, height * 0.18f, width * 0.78f, height * 0.40f, width * 0.72f, height * 0.55f)
            
            // Thumb (Thenar eminence) - Now on the Right
            cubicTo(width * 0.85f, height * 0.58f, width * 0.98f, height * 0.62f, width * 0.94f, height * 0.76f)
            cubicTo(width * 0.90f, height * 0.88f, width * 0.78f, height * 0.92f, width * 0.60f, height * 0.94f)
            close()
        }

        // Draw "Frosted Glass" Hand Impression
        drawPath(
            path = handPath,
            color = Color.Cyan,
            alpha = if (isScanning) 0.15f else 0.08f,
            style = Fill
        )

        // Draw Silhouette Outline
        drawPath(
            path = handPath,
            color = if (isScanning) Color.Cyan else Color.White,
            alpha = if (isScanning) alpha else 0.3f,
            style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        )

        // 3. Palm Center Alignment (Crosshair)
        val centerX = width * 0.5f
        val centerY = height * 0.65f
        drawCircle(
            color = Color.Cyan,
            radius = 120f,
            center = Offset(centerX, centerY),
            alpha = alpha * 0.2f,
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = Color.Cyan,
            radius = 10f,
            center = Offset(centerX, centerY),
            alpha = alpha
        )
        
        // Target lines for center
        drawLine(Color.Cyan.copy(alpha * 0.5f), Offset(centerX - 40, centerY), Offset(centerX + 40, centerY), 2f)
        drawLine(Color.Cyan.copy(alpha * 0.5f), Offset(centerX, centerY - 40), Offset(centerX, centerY + 40), 2f)

        if (isScanning) {
            // 4. Detected Major Lines (Corrected for Right Hand Palm - Thumb on Right)
            val lifeLine = Path().apply {
                // Starts between thumb and index (Right), curves around thumb
                moveTo(width * 0.68f, height * 0.55f)
                cubicTo(width * 0.70f, height * 0.75f, width * 0.55f, height * 0.88f, width * 0.45f, height * 0.92f)
            }
            drawPath(lifeLine, Color.Cyan, alpha = alpha, style = Stroke(width = 6f, cap = StrokeCap.Round))

            val headLine = Path().apply {
                // Starts with life line (Right), moves across towards pinky side (Left)
                moveTo(width * 0.68f, height * 0.55f)
                cubicTo(width * 0.50f, height * 0.58f, width * 0.30f, height * 0.62f, width * 0.15f, height * 0.65f)
            }
            drawPath(headLine, Color.Cyan, alpha = alpha, style = Stroke(width = 6f, cap = StrokeCap.Round))

            val heartLine = Path().apply {
                // Starts under pinky (Left), moves towards index (Right)
                moveTo(width * 0.12f, height * 0.52f)
                cubicTo(width * 0.30f, height * 0.42f, width * 0.50f, height * 0.45f, width * 0.68f, height * 0.48f)
            }
            drawPath(heartLine, Color.Cyan, alpha = alpha, style = Stroke(width = 6f, cap = StrokeCap.Round))

            // 5. Technical Stage Effects
            when {
                stage.contains("Grids") -> {
                    for (i in 0..10) {
                        val y = (height / 10) * i
                        drawLine(Color.Cyan.copy(0.1f), Offset(0f, y), Offset(width, y), 1f)
                        val x = (width / 10) * i
                        drawLine(Color.Cyan.copy(0.1f), Offset(x, 0f), Offset(x, height), 1f)
                    }
                }
                stage.contains("Hough") -> {
                    val random = Random(stage.hashCode())
                    repeat(15) {
                        val start = Offset(random.nextFloat() * width, random.nextFloat() * height)
                        val end = Offset(random.nextFloat() * width, random.nextFloat() * height)
                        drawLine(Color.Cyan.copy(alpha * 0.3f), start, end, 1f)
                    }
                }
            }

            // 6. Floating Data Points
            val random = Random(42)
            repeat(8) {
                val x = width * (0.3f + random.nextFloat() * 0.4f)
                val y = height * (0.4f + random.nextFloat() * 0.4f)
                drawCircle(Color.Cyan, 5f, Offset(x, y), alpha = alpha)
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onPreviewViewReady: (PreviewView) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Optimization: Unbind camera when composable is disposed
    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                // Ignore if not initialized or already closed
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            onPreviewViewReady(previewView)

            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, executor)
            previewView
        },
        modifier = modifier
    )
}

data class Star(val x: Float, val y: Float, val size: Float, val baseAlpha: Float)

@Composable
fun StarField() {
    val stars = remember {
        List(100) {
            Star(
                Random.nextFloat(),
                Random.nextFloat(),
                Random.nextFloat() * 2f + 1f,
                Random.nextFloat() * 0.7f + 0.3f
            )
        }
    }
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEach { star ->
            val x = (star.x + drift) % 1f
            drawCircle(
                color = Color.White,
                radius = star.size,
                center = Offset(x * size.width, star.y * size.height),
                alpha = star.baseAlpha * twinkle
            )
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel") }
        },
        text = { content() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoInput(label: String, value: String, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color(0xFFB0B0C0)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color(0xFF1E1E3F),
            cursorColor = Color.White,
            focusedIndicatorColor = Color.Magenta,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun AstroButton(text: String, isLoading: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200EE)
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(text, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- LOCAL ASTROLOGY ENGINE (BASED ON USER SCRIPT) ---

fun calculateMoolank(dob: String): Int {
    return try {
        // DOB format: dd/MM/yyyy
        val dayPart = dob.split("/")[0].toInt()
        var sum = dayPart
        while (sum > 9) {
            sum = sum.toString().map { it.toString().toInt() }.sum()
        }
        sum
    } catch (e: Exception) { 1 }
}

fun getZodiac(dob: String): String {
    return try {
        val parts = dob.split("/")
        val day = parts[0].toInt()
        val month = parts[1].toInt()
        when (month) {
            1 -> if (day < 20) "Capricorn" else "Aquarius"
            2 -> if (day < 19) "Aquarius" else "Pisces"
            3 -> if (day < 21) "Pisces" else "Aries"
            4 -> if (day < 20) "Aries" else "Taurus"
            5 -> if (day < 21) "Taurus" else "Gemini"
            6 -> if (day < 21) "Gemini" else "Cancer"
            7 -> if (day < 23) "Cancer" else "Leo"
            8 -> if (day < 23) "Leo" else "Virgo"
            9 -> if (day < 23) "Virgo" else "Libra"
            10 -> if (day < 23) "Libra" else "Scorpio"
            11 -> if (day < 22) "Scorpio" else "Sagittarius"
            12 -> if (day < 22) "Sagittarius" else "Capricorn"
            else -> "Cosmic Mystery"
        }
    } catch (e: Exception) { "Unknown" }
}

fun getZodiacPrediction(zodiac: String): String {
    val messages = mapOf(
        "Aries" to "Big opportunities will open soon. Bold steps will turn dreams into reality. Control impatience in relationships. Use these number -> 1, 9, 17, 22",
        "Taurus" to "Financial or emotional peace is near. Let go of old habits to embrace a lasting reward. Use these numbers -> 2, 6, 9, 12",
        "Gemini" to "A new social connection will unlock a major life turn. Be ready for a tough decision between two paths. Use these numbers -> 5, 7, 14, 23",
        "Cancer" to "Home and family matters will require attention. A surprising event will bring healing and clarity. Use these numbers -> 2, 7, 11, 16",
        "Leo" to "Public recognition is coming. A creative project may define your next chapter. Use these numbers -> 1, 3, 10, 19",
        "Virgo" to "A small act of service will return in abundance. Your skill will solve an important problem soon. Use these numbers -> 5, 14, 15, 23",
        "Libra" to "A relationship shift will happen. Balance will return only after honest confrontation. Use these numbers -> 6, 9, 15, 24",
        "Scorpio" to "A deep secret will be revealed soon. Transformation in your love or career is guaranteed. Use these numbers -> 4, 8, 11, 18",
        "Sagittarius" to "An unexpected journey will reshape your destiny. Stay humble during success. Use these numbers -> 3, 7, 9, 21",
        "Capricorn" to "Your dedication will be rewarded with a leadership opportunity. An emotional connection may emerge. Use these numbers -> 4, 8, 13, 22",
        "Aquarius" to "Your innovation will spark change. Your vision may inspire a community project. Use these numbers -> 4, 7, 11, 22",
        "Pisces" to "A spiritual or creative breakthrough is near. Let intuition guide you through confusing times. Use these numbers -> 3, 7, 12, 19"
    )
    return messages[zodiac] ?: "Your stars are aligning for something truly unique."
}

fun getMoolankInfo(moolank: Int): String {
    val table = mapOf(
        1 to "☀️ Sun: Leader, ambitious, confident. Great in leadership, politics, or entrepreneurship. Beware ego and embrace teamwork.",
        2 to "🌙 Moon: Sensitive, artistic, and intuitive. Best in creative, healing, or counselling roles. Mood balance brings success after 35.",
        3 to "♃ Jupiter: Wise, disciplined, spiritual. Gains in teaching, law, religion. Fortune and respect after 30.",
        4 to "☊ Rahu: Hardworking, unconventional. Sudden rise after struggle in tech or politics. Control stress and rebellion.",
        5 to "☿ Mercury: Witty, adaptable, smart. Fast success in media, business, writing. Focus needed to avoid scattered energy.",
        6 to "♀ Venus: Romantic, luxurious, charming. Growth in beauty, fashion, or partnerships. Avoid overindulgence.",
        7 to "☋ Ketu: Mysterious, spiritual, lone thinker. Research, occult, or IT fields suit you. Self-discovery is key.",
        8 to "♄ Saturn: Serious, loyal, persistent. Late bloomer. Big stability and power after 36–40 in law or administration.",
        9 to "♂ Mars: Energetic, fiery, bold. Sports, defense, leadership are your strengths. Learn patience for long-term gain."
    )
    return table[moolank] ?: "No Moolank prediction found."
}

fun getPalmPredictions(random: Random): List<String> {
    // STEP 1: Classification Logic (Based on JS mapping)
    val fateLine = listOf("present", "broken", "absent").random(random)
    val heartLine = listOf("index", "middle", "both").random(random)
    val lifeLine = listOf("long", "short", "broken").random(random)
    val mindLine = listOf("straight", "curve", "short").random(random)
    val marriageLine = listOf("bottom", "middle", "top").random(random)

    val results = mutableListOf<String>()

    // Map Classifications (Lines)
    results.add(when (fateLine) {
        "present" -> "🌟 You’re destined for success in your pursuit. The result will be fruitful soon."
        "broken" -> "🌟 A shift is required. A redirection will bring you closer to success."
        else -> "🌟 Hard work will be your only tool. Stay strong — it will pay off eventually."
    })

    results.add(when (heartLine) {
        "index" -> "🌟 An emotional storm may shake your trust. Remain alert in relationships."
        "middle" -> "🌟 A bold decision for personal gain will present itself. Choose wisely."
        else -> "🌟 You’ll soon make a selfless choice that alters your love life deeply."
    })

    results.add(when (lifeLine) {
        "long" -> "✨ Health and vitality will stay with you. Your path will be strong and long."
        "short" -> "✨ An upcoming challenge may test your strength. Prioritize wellness."
        else -> "✨ A new path awaits you. The past is gone — your future is calling."
    })

    results.add(when (mindLine) {
        "straight" -> "🌟 Your memory will help you excel in academics or career. Study and achieve."
        "curve" -> "🌟 An artistic opportunity will arrive. Express yourself and you’ll stand out."
        else -> "🌟 Hard work will be required soon — don't rely on shortcuts."
    })

    results.add(when (marriageLine) {
        "bottom" -> "🌟 An early commitment or romantic proposal may appear sooner than expected."
        "middle" -> "🌟 Marriage or serious partnership will develop around your late 20s."
        else -> "🌟 Marriage may be delayed — focus on building your future first."
    })

    // STEP 2: Sign Detection Logic (Signs)
    val signs = mutableListOf<String>()
    if (random.nextBoolean()) signs.add("✨ A surprise income source will emerge. Prepare to receive unexpected wealth.")
    if (random.nextBoolean()) signs.add("✨ A divine force will guide you. Obstacles will melt before you.")
    if (random.nextBoolean()) signs.add("✨ You may get an opportunity to settle or work abroad very soon.")
    if (random.nextBoolean()) signs.add("✨ A deep love story will unfold. You are bound to experience a rare romance.")

    results.addAll(signs)

    return results.shuffled(random)
}
