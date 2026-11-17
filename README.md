# Analizador y Generador de Código para Pascal (DisCompiladores)

Este proyecto forma parte del Laboratorio de Diseño de Compiladores e Intérpretes (UNCo). Consiste en la construcción de un compilador de un subconjunto del lenguaje Pascal, desarrollado íntegramente en Java, que traduce el código fuente a código intermedio máquina de pila (MEPA).

El compilador funciona mediante un enfoque "Single-Pass" (una sola pasada), realizando el análisis léxico, sintáctico, semántico y la generación de código sin construir un Árbol de Sintaxis Abstracta (AST) en memoria.

## Tecnologías y Herramientas
*   **Lenguaje:** Java (JDK 8 o superior).
*   **Lenguaje Objetivo:** MEPA (Máquina de Pila).
*   **Estructuras de datos principales:** `HashMap` (para la Tabla de Símbolos y palabras reservadas)[cite: 31, 62], `ArrayList`.
*   **Automatización:** Scripts en Bash (`ejecutar_pruebas.sh`).

## Fases del Compilador Implementadas

1.  **Analizador Léxico (`AnalizadorLexico.java`):** Identifica lexemas, genera tokens (como palabras reservadas, identificadores, operadores y delimitadores) e ignora espacios y comentarios. Detecta errores como caracteres inválidos y comentarios no cerrados.
2.  **Analizador Sintáctico (`AnalizadorSintactico.java`):** Implementa un analizador descendente recursivo predictivo (basado en una gramática $LL(1)$ factorizada y sin recursión a izquierda). Verifica la estructura jerárquica del código.
3.  **Analizador Semántico (`AnalizadorSem.java`, `TablaSimbolos.java`):** 
    *   Gestiona los ámbitos léxicos mediante una Tabla de Símbolos jerárquica.
    *   Verifica unicidad de identificadores, compatibilidad de tipos (en asignaciones, expresiones y operaciones lógicas).
    *   Controla el pasaje de parámetros (por valor) y los llamados a funciones/procedimientos.
4.  **Generador de Código Intermedio (`GeneradorMEPA.java`):** Si el análisis semántico es exitoso, traduce el programa a instrucciones MEPA. 
    *   Gestiona reservas de memoria (`RMEM`, `LMEM`) y desplazamientos relativos ($D[m]+n$) para variables y parámetros.
    *   Controla el flujo de ejecución mediante etiquetas autogeneradas y saltos condicionales (`DSVF`, `DSVS`).

## Cómo ejecutar y probar el proyecto

Asegúrate de tener un entorno Java (JDK) instalado y una terminal Bash.

### 1. Compilación de los archivos fuente
En el directorio raíz del proyecto donde se encuentran los archivos `.java`, ejecuta:
```bash
javac *.java
```
Esto generará los archivos `.class` correspondientes.   

### 2. Ejecutar Análisis Léxico y Sintáctico

Para verificar únicamente la sintaxis de un archivo `.pas`:
```bash
java AnalizadorSintactico nombre_archivo.pas
```
Si el programa es válido, devolverá: `"Programa sintácticamente correcto."`   

### 3. Ejecutar Análisis Semántico

Para verificar declaraciones, tipos y ámbitos, ejecutando en conjunto los analizadores léxico, sintáctico y semántico:
```bash
java AnalizadorSem nombre_archivo.pas
```
Si es correcto, devolverá: `"Programa sintactica y semanticamente correcto."` seguido de la Tabla de Símbolos organizada por ámbitos. En caso de errores, listará todas las inconsistencias encontradas.   

### 4. Generación de Código Intermedio (MEPA)

Para analizar el programa completo y generar su código de salida:
```bash
java GeneradorMEPA nombre_archivo.pas
```
Si el código no tiene errores, se creará un archivo con extensión `.mepa` en el mismo directorio del archivo fuente y las instrucciones se mostrarán en la terminal.

### 5. Ejecutar la Batería de Pruebas Completa

Para ejecutar automáticamente todos los análisis sobre los archivos ubicados en la carpeta `pruebas/`, utiliza el script bash incluido:
```bash
./ejecutar_pruebas.sh
```
Este script compila, procesa cada archivo y guarda los resultados en la carpeta `resultados/`.
