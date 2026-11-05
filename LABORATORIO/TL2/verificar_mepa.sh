
#!/bin/bash

# uso: ./verificar_mepa.sh [carpeta_con_pruebas]

# carpeta donde estan los archivos .pas
CARPETA="${1:-verificacion_mepa/pruebas_mepa}"

# carpeta donde estan los resultados esperados
ESPERADO="verificacion_mepa/esperado"

# compilar todos los archivos java de TL2
echo "Compilando..."
javac *.java || exit 1

# buscar archivos pascal
shopt -s nocaseglob nullglob
ARCHIVOS=("$CARPETA"/*.pas)
shopt -u nocaseglob nullglob

if [ ${#ARCHIVOS[@]} -eq 0 ]; then
    echo "No se encontraron archivos .pas en '$CARPETA'"
    exit 1
fi

OK=0
FALLAS=0

for pas in "${ARCHIVOS[@]}"; do

    # obtener el nombre del archivo sin extension
    base="$(basename "$pas")"
    base="${base%.*}"

    # definir archivos asociados a la prueba
    mepa="$CARPETA/$base.mepa"
    entrada="$CARPETA/$base.in"
    salida_esperada="$ESPERADO/$base.salida"
    mepa_esperado="$ESPERADO/$base.mepa"

    # usar entrada vacia si no existe archivo .in
    if [ ! -f "$entrada" ]; then
        entrada=/dev/null
    fi

    echo "=============================================="
    echo "Prueba: $base"

    # eliminar codigo generado anteriormente
    rm -f "$mepa"

    # 1) ejecutar el analizador y generar codigo mepa
    java -cp "$PWD" GeneradorMEPA "$pas" > /tmp/analisis_$$.txt 2>&1

    if [ ! -f "$mepa" ]; then
        echo "  FALLA: no se genero '$mepa'"
        cat /tmp/analisis_$$.txt
        FALLAS=$((FALLAS + 1))
        continue
    fi

    # 2) verificar que exista la salida esperada
    if [ ! -f "$salida_esperada" ]; then
        echo "  FALLA: no existe '$salida_esperada'"
        FALLAS=$((FALLAS + 1))
        continue
    fi

    # 3) ejecutar el codigo mepa con el interprete
    real=$(java -cp "$PWD" MepaInterprete "$mepa" < "$entrada" 2>&1 | tr -d '\r')
    
    esperado_salida=$(tr -d '\r' < "$salida_esperada")

    # 4) comparar resultados
    if [ "$real" = "$esperado_salida" ]; then
        echo "  Ejecucion: OK (imprime: $real)"
        OK=$((OK + 1))
    else
        echo "  Ejecucion: FALLA"
        echo "    esperado: $esperado_salida"
        echo "    obtenido: $real"
        FALLAS=$((FALLAS + 1))
    fi

    # 5) comparar codigo mepa (solo informativo)
    if [ -f "$mepa_esperado" ]; then
        if diff -w -q --strip-trailing-cr "$mepa" "$mepa_esperado" > /dev/null; then
            echo "  Codigo: identico al esperado"
        else
            echo "  Codigo: distinto al esperado (revisar; < generado, > esperado):"
            diff -w --strip-trailing-cr "$mepa" "$mepa_esperado" | head -12 | sed 's/^/    /'
        fi
    fi

done

# eliminar archivo temporal
rm -f /tmp/analisis_$$.txt

echo "=============================================="
echo "Ejecucion correcta: $OK   Fallas: $FALLAS"

echo 5 | java InterpreteMEPA t3_while_read.mepa