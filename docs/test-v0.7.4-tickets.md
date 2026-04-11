# Test Release v0.7.4 - Sistema de Importación de Tickets

**Fecha:** 2026-04-11  
**APK:** app-debug.apk (77 MB)  
**Release:** https://github.com/joserodriguezballester/lista-compra-app/releases/tag/v0.7.4

---

## 1. Instalación

- [ ] Descargar APK desde el release
- [ ] Instalar en dispositivo (puede pedir permisos de instalación de orígenes desconocidos)
- [ ] Verificar que la app se abre sin crashear

---

## 2. Navegación a Importar Ticket

- [ ] Abrir el drawer lateral (menú hamburguesa)
- [ ] Buscar opción "Importar Ticket"
- [ ] Verificar que navega a la pantalla de importación

---

## 3. Selección de PDF

- [ ] Pulsar botón "Seleccionar PDF"
- [ ] Verificar que abre el selector de archivos
- [ ] Seleccionar un PDF de ticket de Carrefour
- [ ] Verificar que el archivo se carga correctamente

---

## 4. Proceso de OCR

- [ ] Verificar que aparece el indicador de progreso
- [ ] Esperar a que termine el OCR (puede tardar varios segundos)
- [ ] Verificar que extrae texto del PDF

---

## 5. Parsing del Ticket

- [ ] Verificar que muestra la fecha del ticket
- [ ] Verificar que muestra el nombre del supermercado (Carrefour)
- [ ] Verificar que muestra el total del ticket
- [ ] Verificar que aparecen líneas de producto

---

## 6. Matching de Productos

- [ ] Verificar que cada línea muestra el nombre original del producto
- [ ] Verificar que muestra sugerencias de matching con el catálogo
- [ ] Si hay match automático, verificar que es correcto
- [ ] Si no hay match, verificar que permite buscar manualmente

---

## 7. Crear Artículo Nuevo

- [ ] En una línea sin match, pulsar "Crear nuevo"
- [ ] Verificar que abre el diálogo de creación
- [ ] Rellenar nombre, categoría, precio
- [ ] Guardar y verificar que se asigna a la línea

---

## 8. Guardar Ticket

- [ ] Confirmar todas las líneas (o las que se quieran)
- [ ] Pulsar botón "Guardar ticket"
- [ ] Verificar que guarda correctamente
- [ ] Verificar que muestra mensaje de éxito

---

## 9. Verificar en Base de Datos

- [ ] Ir a Historial
- [ ] Verificar que aparece el ticket guardado
- [ ] Verificar que los productos se añadieron al historial

---

## Errores Conocidos / Limitaciones

1. **OCR lento**: El OCR con ML Kit puede tardar 5-15 segundos dependiendo del PDF
2. **Solo Carrefour**: Por ahora solo funciona con tickets de Carrefour
3. **PDFs escaneados**: Si el PDF está muy borroso, el OCR puede fallar
4. **Caracteres especiales**: Nombres con acentos raros pueden no matchear bien

---

## Notas para Testing

- Tener a mano un PDF de ticket de Carrefour real
- Si falla algo, anotar el error exacto y el paso donde ocurrió

---

## Resultado del Test

**Fecha test:** _______________  
**Dispositivo:** _______________  
**Android versión:** _______________  

**Estado final:** [ ] APROBADO / [ ] CON ERRORES

**Notas:**
_______________________________________
_______________________________________
_______________________________________
