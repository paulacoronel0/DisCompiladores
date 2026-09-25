@echo off
REM Obtiene la ruta de la carpeta donde esta guardado este script
set BASE_DIR=%~dp0

REM Compila por si hubo cambios y ejecuta apuntando a la carpeta pruebas
javac "%BASE_DIR%AnalizadorSintactico.java"
java -cp "%BASE_DIR%." AnalizadorSintactico "%BASE_DIR%pruebas\%1"