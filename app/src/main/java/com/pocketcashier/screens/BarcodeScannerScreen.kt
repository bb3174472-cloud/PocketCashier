package com.pocketcashier.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.pocketcashier.data.Product
import com.pocketcashier.data.ProductDao
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    productDao: ProductDao,
    cart: MutableMap<Product, Int>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val products by productDao.getAllProducts().collectAsState(initial = emptyList())
    
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    var isFlashOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    
    // متغيرات لمنع المسح المتكرر السريع لنفس الباركود
    var lastScannedCode by remember { mutableStateOf("") }
    var lastScanTime by remember { mutableStateOf(0L) }

    LaunchedEffect(isFlashOn) {
        camera?.cameraControl?.enableTorch(isFlashOn)
    }

    // دالة معالجة الباركود وإضافته للسلة
    fun handleBarcode(barcode: String) {
        val cleanCode = barcode.trim()
        var product = products.find { it.barcode?.trim() == cleanCode }
        
        if (product == null && cleanCode.length == 13 && cleanCode.startsWith("0")) {
            product = products.find { it.barcode?.trim() == cleanCode.substring(1) }
        }
        if (product == null && cleanCode.length == 12) {
            product = products.find { it.barcode?.trim() == "0$cleanCode" }
        }

        if (product != null) {
            val qtyInCart = cart[product] ?: 0
            if (product.quantity > qtyInCart) {
                cart[product] = qtyInCart + 1
                Toast.makeText(context, "تمت إضافة ${product.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "الكمية غير كافية للمنتج: ${product.name}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "باركود غير مسجل: $cleanCode", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الكاميرا (مسح مستمر)") },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.ArrowBack, contentDescription = "إنهاء المسح") 
                    } 
                },
                actions = {
                    IconButton(onClick = { isFlashOn = !isFlashOn }) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "الفلاش",
                            tint = if (isFlashOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (hasCameraPermission) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                            val imageAnalysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                            
                            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    BarcodeScanning.getClient().process(image)
                                        .addOnSuccessListener { barcodes ->
                                            if (barcodes.isNotEmpty()) {
                                                val barcode = barcodes.first().rawValue
                                                if (barcode != null) {
                                                    val currentTime = System.currentTimeMillis()
                                                    // منع التكرار: إذا كان نفس الباركود ومر أقل من 1.5 ثانية، تجاهله
                                                    if (barcode != lastScannedCode || currentTime - lastScanTime > 1500) {
                                                        lastScannedCode = barcode
                                                        lastScanTime = currentTime
                                                        handleBarcode(barcode)
                                                    }
                                                }
                                            }
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                } else {
                                    imageProxy.close()
                                }
                            }
                            try {
                                cameraProvider.unbindAll()
                                camera = cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                            } catch (exc: Exception) {
                                Log.e("CameraX", "Use case binding failed", exc)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    "وجه الكاميرا نحو الباركود (اضغط السهم للخروج)", 
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp), 
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("يجب السماح بالوصول للكاميرا لاستخدام الماسح", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
