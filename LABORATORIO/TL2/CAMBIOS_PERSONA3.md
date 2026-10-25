# Cambios de Persona 3 — Flujo de control y subprogramas

Sobre la base de Persona 1 (`emitir`, `INPP/PARA`, `RMEM/LMEM`, `APVL/ALVL`, `SUST`, `UMEN`), se agregó la generación de código MEPA para **if, while y llamadas a subprogramas**.

No se cambió ningún chequeo semántico: los mensajes de error siguen siendo los mismos.

---

## Archivos modificados

| Archivo | Qué cambió |
|---|---|
| `SimboloSubprograma.java` | Nuevo campo `etiqueta` + `getEtiqueta()` / `setEtiqueta()` |
| `AnalizadorSintactico.java` | `comandoCondicional`, `comandoRepetitivo`, `declaracionProcedimiento`, `declaracionFuncion`, `validarLlamadaProcedimiento`, `factor` |

El generador de etiquetas `nuevaEtiqueta()` (`L1`, `L2`, …) ya existía; ahora se usa también en if, while y subprogramas.

---

## 1. IF-THEN-ELSE — `comandoCondicional()`

```
  <condición>
  DSVF Lsino        ← si es falsa, salta al else
  <then>
  DSVS Lfin         ← (solo si hay else) saltea el else
Lsino NADA
  <else>
Lfin NADA
```

Sin `else` queda solo `DSVF Lsino`, el `then` y `Lsino NADA`.

## 2. WHILE-DO — `comandoRepetitivo()`

```
Linicio NADA
  <condición>
  DSVF Lfin         ← si es falsa, sale del ciclo
  <cuerpo>
  DSVS Linicio      ← vuelve a evaluar la condición
Lfin NADA
```

## 3. Procedimientos y funciones — `declaracionProcedimiento()` / `declaracionFuncion()`

- Cada subprograma recibe su etiqueta **antes** de procesar el cuerpo (así funciona la recursión).
- Se emite `ENPR` al entrar y `RTPR` al salir. `bloque()` no se tocó: sigue emitiendo `RMEM`/`LMEM`.

```
Lp ENPR k           ← k = nivel léxico del subprograma
   RMEM m           ← (de Persona 1)
   ...cuerpo...
   LMEM m           ← (de Persona 1)
   RTPR k, n        ← n = cantidad de parámetros
```

## 4. Llamadas

| Llamada | Dónde | Código |
|---|---|---|
| Procedimiento `p(a, b)` o `p;` | `validarLlamadaProcedimiento()` | `<args>` `LLPR Lp` |
| Función `f(a, b)` | `factor()`, rama con `(` | `RMEM 1` `<args>` `LLPR Lf` |
| Función sin parámetros `g` | `factor()`, rama sin `(` | `RMEM 1` `LLPR Lg` |

En las funciones, `RMEM 1` va **antes** de los argumentos: es el lugar donde queda el resultado (desplazamiento `-(n+3)`, que ya calculaba `getDesplazamientoRetorno()`). Al terminar `RTPR`, el resultado queda en el tope de la pila, como cualquier operando.

---

## Pruebas

Nuevos programas en `pruebas_mepa/`: `if_else.pas`, `while_anid.pas`, `proc.pas`, `rec.pas`.

```bash
./ejecutar_pruebas.sh pruebas_mepa
```

- `prueba1`, `prueba3`, `prueba4` generan lo mismo que antes.
- `prueba2`, `prueba5`, `prueba6` ahora incluyen `ENPR`/`RTPR`.
- Los `.mepa` generados se ejecutaron en un intérprete y terminan con la pila vacía.

Mientras no estén las constantes y comparaciones, las pruebas usan **variables booleanas** como condición (`if c then`, `while d do`).

---

## Para Persona 2

- Lo de Persona 3 asume que **cada `expresion()` deja exactamente un valor en el tope de la pila** (lo usan `DSVF` y los argumentos de `LLPR`).
- En `factor()`, Persona 3 solo tocó el `case IDENTIFICADOR`. Los casos `NUMERO`, `TRUE`, `FALSE` y `NOT` quedan libres para `APCT` y `NEGA`.
- Prueba de integración final: el ejemplo del PDF de MEPA (pág. 12) y el de funciones (pág. 15) deberían dar la misma traducción que el apunte.
