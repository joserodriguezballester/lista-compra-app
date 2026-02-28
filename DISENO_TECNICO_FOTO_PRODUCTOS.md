# 📸 DISEÑO TÉCNICO: RECONOCIMIENTO DE PRODUCTOS POR FOTO

**Enfoque:** Few-shot learning personalizado (3-5 imágenes por producto)  
**Tecnología:** TensorFlow Lite + Embeddings / Comparación de imágenes  
**Integración:** Códigos de barras (primario) + Fotos (secundario)  
**Estado:** Diseño técnico para implementación futura

---

## 🎯 OBJETIVO

Permitir al usuario añadir productos a la lista de compra mediante:
1. **Código de barras** (primario, rápido, preciso)
2. **Fotografía del producto** (secundario, para productos sin código o que el usuario ya ha "enseñado")

El sistema aprende los productos habituales del usuario con solo 3-5 fotos por producto.

---

## 🏗️ ARQUITECTURA GENERAL

```
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE PRESENTACIÓN                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ Cámara       │  │ Vista        │  │ Confirmación     │  │
│  │ Barcode/     │  │ Previa       │  │ Producto         │  │
│  │ Foto         │  │              │  │ Detectado        │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘  │
└─────────┼────────────────┼───────────────────┼──────────────┘
          │                │                   │
          ▼                ▼                   ▼
┌─────────────────────────────────────────────────────────────┐
│              CAPA DE PROCESAMIENTO DE IMAGEN                 │
│                                                              │
│  ┌─────────────────┐    ┌──────────────────────────────┐   │
│  │ ML Kit Barcode  │    │ TensorFlow Lite + Embeddings │   │
│  │ (Primario)      │    │ (Secundario - Productos      │   │
│  │                 │    │  enseñados por usuario)      │   │
│  └────────┬────────┘    └──────────────┬───────────────┘   │
│           │                            │                   │
│           │ OK                         │ Match > 90%       │
│           ▼                            ▼                   │
│  ┌─────────────────┐          ┌────────────────┐           │
│  │ Open Food Facts │          │ Producto       │           │
│  │ API Lookup      │          │ Reconocido     │           │
│  └────────┬────────┘          └───────┬────────┘           │
│           │                           │                    │
└───────────┼───────────────────────────┼────────────────────┘
            │                           │
            ▼                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE DATOS                            │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │ Room Database                                       │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌───────────┐│    │
│  │  │ Productos    │  │ ProductImages│  │Embeddings ││    │
│  │  │ (lista)      │  │ (fotos       │  │ (vectores ││    │
│  │  │              │  │  guardadas)  │  │  numéricos││    │
│  │  └──────────────┘  └──────────────┘  └───────────┘│    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 MODELOS DE DATOS

### **1. ProductImageEntity** (Nueva tabla)

```kotlin
@Entity(
    tableName = "product_images",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProductImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val productId: Long,  // FK a ProductEntity
    
    // Ruta local de la imagen (almacenada en almacenamiento interno)
    val imagePath: String,
    
    // Embedding vector (128 o 256 dimensiones) del modelo MobileNet
    // Almacenado como String JSON: "[0.12, 0.34, 0.56, ...]"
    val embedding: String?,
    
    // Tipo de foto: "FRONTAL", "ETIQUETA", "GENERAL", "CODIGO_BARRAS"
    val imageType: String,
    
    // Timestamp de cuando se guardó
    val createdAt: Long = System.currentTimeMillis()
)
```

### **2. ProductEntity** (Modificación)

```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    val aisleId: Long,
    val quantity: Float,
    val unitPrice: Float?,
    val offerId: Long?,
    val finalPrice: Float?,
    val isPurchased: Boolean,
    val notes: String,
    val orderIndex: Int,
    val shoppingListId: Long,
    
    // NUEVO: Indica si este producto tiene imágenes entrenadas
    val hasTrainedImages: Boolean = false,
    
    // NUEVO: Código de barras si lo tiene (EAN-13, UPC, etc.)
    val barcode: String? = null,
    
    // NUEVO: Fuente del producto: "MANUAL", "BARCODE", "IMAGE_RECOGNITION"
    val source: String = "MANUAL"
)
```

### **3. ProductWithImages** (Relación)

```kotlin
data class ProductWithImages(
    @Embedded
    val product: ProductEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val images: List<ProductImageEntity>
)
```

---

## 🧠 ALGORITMO DE RECONOCIMIENTO

### **Opción A: Comparación por Embeddings (Recomendada)**

**Modelo:** MobileNetV2 (pre-entrenado, descarga automática de TensorFlow Hub)

**Flujo:**

```kotlin
class ProductImageRecognizer(private val context: Context) {
    
    private lateinit var interpreter: Interpreter
    private val IMAGE_SIZE = 224
    private val EMBEDDING_SIZE = 1280  // MobileNetV2 output
    
    init {
        // Cargar modelo TFLite (incluido en assets/)
        val model = loadModelFile("mobilenet_v2_1.0_224.tflite")
        interpreter = Interpreter(model)
    }
    
    /**
     * Convierte imagen en vector numérico (embedding)
     */
    fun getEmbedding(bitmap: Bitmap): FloatArray {
        // Redimensionar a 224x224
        val resized = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true)
        
        // Normalizar píxeles [0, 255] → [-1, 1]
        val inputBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3)
        inputBuffer.order(ByteOrder.nativeOrder())
        
        val pixels = IntArray(IMAGE_SIZE * IMAGE_SIZE)
        resized.getPixels(pixels, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE)
        
        for (pixel in pixels) {
            inputBuffer.putFloat(((pixel shr 16 and 0xFF) / 255.0f - 0.5f) * 2)
            inputBuffer.putFloat(((pixel shr 8 and 0xFF) / 255.0f - 0.5f) * 2)
            inputBuffer.putFloat(((pixel and 0xFF) / 255.0f - 0.5f) * 2)
        }
        
        // Inferencia
        val outputBuffer = Array(1) { FloatArray(EMBEDDING_SIZE) }
        interpreter.run(inputBuffer, outputBuffer)
        
        return outputBuffer[0]
    }
    
    /**
     * Compara embedding de foto nueva con embeddings guardados
     * Retorna producto más similar si supera threshold (ej: 0.90 = 90%)
     */
    fun findMatchingProduct(
        newImageBitmap: Bitmap,
        trainedProducts: List<ProductWithImages>,
        threshold: Float = 0.90f
    ): ProductEntity? {
        
        val newEmbedding = getEmbedding(newImageBitmap)
        
        var bestMatch: ProductEntity? = null
        var bestSimilarity = 0f
        
        for (productWithImages in trainedProducts) {
            for (image in productWithImages.images) {
                if (image.embedding != null) {
                    val storedEmbedding = parseEmbedding(image.embedding)
                    val similarity = cosineSimilarity(newEmbedding, storedEmbedding)
                    
                    if (similarity > bestSimilarity) {
                        bestSimilarity = similarity
                        bestMatch = productWithImages.product
                    }
                }
            }
        }
        
        return if (bestSimilarity >= threshold) bestMatch else null
    }
    
    /**
     * Similitud coseno entre dos vectores
     * Resultado: 0.0 (diferente) a 1.0 (idéntico)
     */
    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        
        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        
        return dotProduct / (sqrt(normA) * sqrt(normB))
    }
}
```

**Ventajas:**
- ✅ Precisa incluso con diferente ángulo/iluminación
- ✅ Funciona offline
- ✅ Rápida (inferencia en <100ms)

---

### **Opción B: Comparación Directa de Píxeles (Más simple)**

```kotlin
class SimpleImageComparator {
    
    fun compareImages(bitmap1: Bitmap, bitmap2: Bitmap): Float {
        // Redimensionar ambas al mismo tamaño
        val size = 128
        val b1 = Bitmap.createScaledBitmap(bitmap1, size, size, true)
        val b2 = Bitmap.createScaledBitmap(bitmap2, size, size, true)
        
        var diff = 0L
        
        for (x in 0 until size) {
            for (y in 0 until size) {
                val p1 = b1.getPixel(x, y)
                val p2 = b2.getPixel(x, y)
                
                // Diferencia de color
                val r = abs((p1 shr 16 and 0xFF) - (p2 shr 16 and 0xFF))
                val g = abs((p1 shr 8 and 0xFF) - (p2 shr 8 and 0xFF))
                val b = abs((p1 and 0xFF) - (p2 and 0xFF))
                
                diff += r + g + b
            }
        }
        
        // Normalizar a 0-1 (1 = idéntico)
        val maxDiff = size * size * 3 * 255
        return 1f - (diff.toFloat() / maxDiff)
    }
}
```

**Ventajas:**
- ✅ Súper simple de implementar
- ✅ No necesita modelo ML
- ❌ Sensible a ángulo/iluminación

**Recomendación:** Usar Opción A (Embeddings) para producción, Opción B solo para prototipo rápido.

---

## 📱 FLUJO DE USUARIO DETALLADO

### **FLUJO 1: Añadir producto por primera vez ("Enseñar")**

```
Usuario:
  ↓
[+] Menú → "Enseñar producto nuevo" (o Cámara → "No reconocido")
  ↓
Paso 1: Hacer 3-5 fotos del producto
  ├─ Foto 1: Vista general
  ├─ Foto 2: Etiqueta frontal (nombre)
  ├─ Foto 3: Código de barras (si tiene)
  └─ (Opcional) Foto 4-5: Otros ángulos
  ↓
Paso 2: Escribir información
  ├─ Nombre: "Leche Pascual Entera"
  ├─ Pasillo: [Desplegable] → "Lácteos"
  ├─ Precio: 1.15€
  └─ (Opcional) Código de barras: 843123456789
  ↓
Sistema:
  ├─ Guarda fotos en almacenamiento interno
  ├─ Genera embeddings con MobileNet
  ├─ Guarda en BD: ProductEntity + ProductImageEntity
  └─ Marca: hasTrainedImages = true
  ↓
[✓] "Producto guardado. Próxima vez lo reconoceré automáticamente"
```

### **FLUJO 2: Reconocer producto (Uso diario)**

```
Usuario:
  ↓
[+] Menú → "Añadir por cámara"
  ↓
Sistema activa:
  ├─ ML Kit Barcode Scanner (primero)
  └─ TensorFlow Lite Camera (segundo plano)
  ↓
CASO A: Detecta código de barras
  ↓
Busca en BD local (código previamente guardado)
  ├─ SÍ existe → Muestra: "¿Añadir [Producto]?"
  └─ NO existe → Busca en Open Food Facts API
      ├─ API responde → Sugiere: "[Nombre] - ¿Añadir?"
      └─ API no responde → FLUJO B (foto)
  ↓
CASO B: No detecta código / Usuario hace foto
  ↓
Captura frame de cámara → getEmbedding()
  ↓
Compara con ProductImageEntity almacenados
  ├─ Match > 90% → "¿Querías decir [Producto Guardado]?"
  └─ Match < 90% → "Producto no reconocido. ¿Enseñarlo?"
      ↓
      Ir a FLUJO 1 (Enseñar producto nuevo)
  ↓
Usuario confirma
  ↓
Añade producto a lista actual con cantidad 1 (editable)
```

---

## 💾 ALMACENAMIENTO DE IMÁGENES

### **Estrategia:**

```kotlin
// Almacenamiento interno de la app (no galería pública)
// Ruta: /data/data/com.jose.listacompra/files/product_images/

class ProductImageStorage(private val context: Context) {
    
    private val imageDir = File(context.filesDir, "product_images")
    
    init {
        if (!imageDir.exists()) imageDir.mkdirs()
    }
    
    fun saveImage(bitmap: Bitmap, productId: Long, imageIndex: Int): String {
        val fileName = "product_${productId}_img_${imageIndex}_${System.currentTimeMillis()}.jpg"
        val file = File(imageDir, fileName)
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        
        return file.absolutePath
    }
    
    fun loadImage(path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }
    
    fun deleteImagesForProduct(productId: Long) {
        imageDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("product_${productId}_")) {
                file.delete()
            }
        }
    }
}
```

**Tamaño estimado:**
- 1 imagen: ~100-200KB (JPEG comprimido 85%)
- 20 productos × 4 imágenes = 80 imágenes ≈ 16MB
- **Total:** ~20-30MB (aceptable para almacenamiento interno)

---

## 🎨 INTERFAZ DE USUARIO (UI)

### **Pantalla 1: Selector de modo**

```
┌─────────────────────────────┐
│  📷 Añadir Producto          │
├─────────────────────────────┤
│                             │
│   [🔍 Escanear código]      │
│                             │
│   [📸 Hacer foto]           │
│                             │
│   [📚 Mis productos         │
│      guardados]              │
│                             │
└─────────────────────────────┘
```

### **Pantalla 2: Cámara (Modo foto)**

```
┌─────────────────────────────┐
│                             │
│                             │
│      [VISTA CÁMARA]         │
│      (con overlay           │
│       de guía)              │
│                             │
│                             │
├─────────────────────────────┤
│  [📸]  [🔄 Cambiar cámara]  │
│                             │
│  💡 Consejo: Enfoca la      │
│     etiqueta del producto   │
└─────────────────────────────┘
```

### **Pantalla 3: Confirmación (Si reconoce)**

```
┌─────────────────────────────┐
│  ¿Es este producto?         │
├─────────────────────────────┤
│                             │
│  ┌─────────────────┐        │
│  │ FOTO CAPTURADA  │        │
│  └─────────────────┘        │
│                             │
│  🎯 Detectado:              │
│  "Leche Pascual Entera"     │
│  Pasillo: Lácteos           │
│  Precio: 1.15€              │
│                             │
│  Confianza: 94% ✅          │
│                             │
│  [✅ Sí, añadir]            │
│  [❌ No, enseñar nuevo]     │
│  [🔍 Buscar manualmente]    │
│                             │
└─────────────────────────────┘
```

### **Pantalla 4: Enseñar producto nuevo**

```
┌─────────────────────────────┐
│  📚 Enseñar producto nuevo  │
├─────────────────────────────┤
│  Paso X de 4:               │
│                             │
│  ┌─────────────────┐        │
│  │ PREVIEW FOTO    │        │
│  └─────────────────┘        │
│                             │
│  [📸 Capturar]              │
│  [⏭️ Saltar este ángulo]    │
│                             │
│  Fotos: [●] [○] [○] [○]     │
│  (mínimo 3 recomendado)     │
│                             │
└─────────────────────────────┘
```

---

## 📦 DEPENDENCIAS (build.gradle)

```kotlin
dependencies {
    // ... dependencias existentes ...
    
    // ML Kit - Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    
    // ML Kit - Image Labeling (opcional, para clasificación básica)
    implementation("com.google.mlkit:image-labeling:17.0.7")
    
    // TensorFlow Lite - Para embeddings
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    
    // CameraX - Para preview de cámara
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    
    // Glide - Para cargar imágenes eficientemente
    implementation("com.github.bumptech.glide:glide:4.16.0")
}
```

---

## ⚡ PERFORMANCE CONSIDERATIONS

### **Optimizaciones:**

1. **Procesamiento asíncrono:**
   - Generación de embeddings en corutina (Dispatchers.Default)
   - UI no se bloquea mientras analiza imagen

2. **Caché de embeddings:**
   - Guardar embeddings en memoria durante sesión
   - No recalcular cada vez que se abre cámara

3. **Búsqueda optimizada:**
   - Indexar productos por categoría/pasillo
   - Primero buscar en productos del pasillo actual
   - Luego buscar en todos

4. **Calidad de imagen:**
   - Reducir a 224x224 antes de embedding (MobileNet requiere este tamaño)
   - Comprimir JPEG a 85% (buen balance calidad/tamaño)

---

## 🔒 PRIVACIDAD Y SEGURIDAD

### **Medidas implementadas:**

- ✅ **Todo procesamiento local** (sin enviar fotos a la nube)
- ✅ **Imágenes en almacenamiento privado** de la app (no aparecen en galería)
- ✅ **Sin tracking de usuarios** ni análisis de comportamiento
- ✅ **Datos propios del usuario** (sus productos, sus fotos)
- ✅ **Sin permisos de internet** necesarios para reconocimiento básico
  - Solo para: (1) Buscar código de barras en Open Food Facts, (2) Sincronización (Fase 2)

---

## 📋 CHECKLIST DE IMPLEMENTACIÓN

### **Fase 1: Infraestructura (1-2 días)**
- [ ] Añadir dependencias ML Kit + TensorFlow Lite
- [ ] Crear `ProductImageEntity` y migración BD
- [ ] Implementar `ProductImageStorage` (guardar/cargar fotos)
- [ ] Descargar modelo MobileNet TFLite (incluir en assets)

### **Fase 2: Core ML (2-3 días)**
- [ ] Implementar `ProductImageRecognizer` (embeddings)
- [ ] Función `getEmbedding()`
- [ ] Función `findMatchingProduct()`
- [ ] Tests con imágenes de ejemplo

### **Fase 3: UI (2-3 días)**
- [ ] Pantalla selector modo (código/foto)
- [ ] Integrar CameraX para preview
- [ ] Pantalla "Enseñar producto" (wizard 4 pasos)
- [ ] Pantalla confirmación (reconocimiento exitoso)
- [ ] Overlay de guía en cámara (marco producto)

### **Fase 4: Integración (1 día)**
- [ ] Modificar `AddProductDialog` (añadir botón "Cámara")
- [ ] Integrar con flujo de listas existente
- [ ] Actualizar `ProductRepository` (guardar con imágenes)
- [ ] Testing end-to-end

### **Fase 5: Optimización (1 día)**
- [ ] Caché de embeddings
- [ ] Probar con iluminación diferente
- [ ] Ajustar threshold de similitud (90%, 85%, 95%...)
- [ ] Documentación usuario (README)

**Total estimado:** 7-10 días de desarrollo

---

## 🎯 PRÓXIMOS PASOS

Para implementar esto, necesitaríamos:

1. **Decidir prioridad:** ¿Antes o después de la Fase 2 (sincronización en la nube)?
2. **Validar modelo:** ¿Quieres que primero haga un prototipo con 5-10 productos para probar precisión?
3. **Recursos:** ¿Tienes imágenes de ejemplo de tus productos habituales para probar?

---

**¿Qué te parece este diseño técnico, jefe?** ¿Algo que ajustar o profundizar? 💼
