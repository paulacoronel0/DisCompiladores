#!/bin/bash
# 1) Abrir la terminal de bash
# 2) Ejecutar: chmod +x ejecutar_pruebas.sh
# 4) Ejecutar: ./ejecutar_pruebas.sh
# Si no se pasa carpeta, usa "pruebas" por defecto.



set -e

CARPETA="${1:-pruebas}"
CARPETA_RESULTADOS="resultados"

echo "Compilando..."
javac *.java

if [ ! -d "$CARPETA" ]; then
    echo "No existe la carpeta '$CARPETA'"
    exit 1
fi

mkdir -p "$CARPETA_RESULTADOS"

# nocaseglob: que *.pas matchee también .PAS, .Pas, etc.
# nullglob: si no matchea nada, el array queda vacío en vez de pasar el
#           patrón literal "pruebas/*.pas" como si fuera un nombre de archivo
shopt -s nocaseglob nullglob
ARCHIVOS=("$CARPETA"/*.pas)
shopt -u nocaseglob nullglob

if [ ${#ARCHIVOS[@]} -eq 0 ]; then
    echo "No se encontró ningún .pas/.PAS dentro de '$CARPETA'"
    exit 1
fi

CANTIDAD=0
CON_ERROR=0

for archivo in "${ARCHIVOS[@]}"; do
    CANTIDAD=$((CANTIDAD + 1))
    echo "=============================================="
    echo "Archivo: $archivo"
    echo "=============================================="

    SALIDA=$(java AnalizadorSem "$archivo" 2>&1)
    echo "$SALIDA"
    echo

    # nombre sin ruta y sin extensión, sirva ésta .pas, .PAS o lo que sea
    NOMBRE_BASE="$(basename "$archivo")"
    NOMBRE_BASE="${NOMBRE_BASE%.*}"
    echo "$SALIDA" > "$CARPETA_RESULTADOS/resultado_${NOMBRE_BASE}.txt"

    if echo "$SALIDA" | grep -qi "error"; then
        CON_ERROR=$((CON_ERROR + 1))
    fi
done


echo "=============================================="
echo "Total de archivos: $CANTIDAD"
echo "Con algún error:   $CON_ERROR"
echo "Sin errores:       $((CANTIDAD - CON_ERROR))"
echo "Resultados guardados en '$CARPETA_RESULTADOS/'"