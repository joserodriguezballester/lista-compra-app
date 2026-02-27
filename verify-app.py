#!/usr/bin/env python3
"""
Tests de validación para la app Lista Compra
Verifica errores comunes antes de compilar
"""

import os
import re
import sys

BASE_DIR = os.path.expanduser("~/private-users/Jose/proyectos/lista-compra-app")
ERRORS = []
WARNINGS = []

def check_file_exists(path, description):
    """Verifica que un archivo exista"""
    full_path = os.path.join(BASE_DIR, path)
    if not os.path.exists(full_path):
        ERRORS.append(f"❌ Falta {description}: {path}")
        return False
    return True

def check_syntax_kotlin(filepath):
    """Verifica sintaxis básica de Kotlin"""
    with open(filepath, 'r') as f:
        content = f.read()
    
    # Verificar llaves balanceadas
    open_braces = content.count('{')
    close_braces = content.count('}')
    if open_braces != close_braces:
        ERRORS.append(f"❌ Llaves desbalanceadas en {filepath}: {open_braces} abiertas, {close_braces} cerradas")
    
    # Verificar paréntesis balanceados (simple)
    open_parens = content.count('(')
    close_parens = content.count(')')
    if open_parens != close_parens:
        ERRORS.append(f"❌ Paréntesis desbalanceados en {filepath}")
    
    # Verificar imports sin usar (básico)
    imports = re.findall(r'^import (.+)$', content, re.MULTILINE)
    for imp in imports:
        class_name = imp.split('.')[-1]
        if class_name not in content.split('import')[0] and class_name not in content.split('import')[1]:
            WARNINGS.append(f"⚠️ Import posiblemente sin usar: {imp} en {filepath}")

def check_database_entities():
    """Verifica que las entidades de Room tengan @Entity"""
    db_file = os.path.join(BASE_DIR, "app/src/main/java/com/jose/listacompra/data/local/Database.kt")
    if os.path.exists(db_file):
        with open(db_file, 'r') as f:
            content = f.read()
        
        if "@Entity" not in content:
            ERRORS.append("❌ Database.kt no tiene @Entity definidas")
        if "@Dao" not in content:
            ERRORS.append("❌ Database.kt no tiene @Dao definidos")
        if "abstract class ShoppingListDatabase" not in content:
            ERRORS.append("❌ Falta clase ShoppingListDatabase")

def check_gradle_dependencies():
    """Verifica dependencias críticas en build.gradle"""
    gradle_file = os.path.join(BASE_DIR, "app/build.gradle.kts")
    if os.path.exists(gradle_file):
        with open(gradle_file, 'r') as f:
            content = f.read()
        
        required_deps = [
            ("room", "Room Database"),
            ("compose", "Jetpack Compose"),
            ("navigation", "Navigation"),
            ("gson", "Gson JSON"),
        ]
        
        for dep, name in required_deps:
            if dep not in content.lower():
                ERRORS.append(f"❌ Falta dependencia: {name}")

def check_missing_imports_in_files():
    """Verifica imports faltantes comunes"""
    files_to_check = [
        "app/src/main/java/com/jose/listacompra/ui/screens/MainScreen.kt",
        "app/src/main/java/com/jose/listacompra/ui/viewmodel/ShoppingListViewModel.kt",
    ]
    
    for filepath in files_to_check:
        full_path = os.path.join(BASE_DIR, filepath)
        if os.path.exists(full_path):
            with open(full_path, 'r') as f:
                content = f.read()
            
            # Verificar que uses Material3 y no Material2 antiguo
            if "androidx.compose.material3" not in content and "MaterialTheme" in content:
                WARNINGS.append(f"⚠️ Posible uso de Material2 en lugar de Material3: {filepath}")

def main():
    print("🔍 Verificando app Lista Compra...\n")
    
    # 1. Verificar estructura de archivos críticos
    print("1. Verificando archivos esenciales...")
    critical_files = [
        ("app/build.gradle.kts", "build.gradle"),
        ("app/src/main/AndroidManifest.xml", "AndroidManifest"),
        ("app/src/main/java/com/jose/listacompra/ui/MainActivity.kt", "MainActivity"),
        ("app/src/main/java/com/jose/listacompra/data/local/Database.kt", "Database"),
        ("app/src/main/java/com/jose/listacompra/domain/model/Product.kt", "Product model"),
    ]
    
    for path, desc in critical_files:
        check_file_exists(path, desc)
    
    # 2. Verificar sintaxis Kotlin
    print("2. Verificando sintaxis Kotlin...")
    kotlin_files = []
    for root, dirs, files in os.walk(os.path.join(BASE_DIR, "app/src/main/java")):
        for file in files:
            if file.endswith('.kt'):
                kotlin_files.append(os.path.join(root, file))
    
    for kt_file in kotlin_files[:10]:  # Limitar a 10 archivos
        try:
            check_syntax_kotlin(kt_file)
        except Exception as e:
            ERRORS.append(f"❌ Error leyendo {kt_file}: {e}")
    
    # 3. Verificar configuración Room
    print("3. Verificando configuración Room...")
    check_database_entities()
    
    # 4. Verificar Gradle
    print("4. Verificando dependencias Gradle...")
    check_gradle_dependencies()
    
    # 5. Verificar imports
    print("5. Verificando imports...")
    check_missing_imports_in_files()
    
    # RESULTADO
    print("\n" + "="*50)
    print("RESULTADO DE VERIFICACIÓN")
    print("="*50)
    
    if not ERRORS and not WARNINGS:
        print("✅ Todo OK! No se encontraron problemas.")
        return 0
    
    if ERRORS:
        print(f"\n❌ ERRORES ({len(ERRORS)}):")
        for error in ERRORS:
            print(f"   {error}")
    
    if WARNINGS:
        print(f"\n⚠️  ADVERTENCIAS ({len(WARNINGS)}):")
        for warning in WARNINGS:
            print(f"   {warning}")
    
    print("\n" + "="*50)
    
    if ERRORS:
        print("❌ Corrige los errores antes de compilar.")
        return 1
    else:
        print("✅ Puedes compilar, pero revisa las advertencias.")
        return 0

if __name__ == "__main__":
    exit(main())
