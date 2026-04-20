
/*
Un enum en Java (de enumeration) es un tipo de dato especial 
que sirve para definir un conjunto fijo de valores posibles.

Por ejemplo:
    TipoToken.IDENTIFICADOR
    TipoToken.NUMERO
    TipoToken.IF

Esto garantiza que un token solo puede tomar uno de los valores
definidos en TipoToken, evitando inconsistencias.

En el contexto del analizador léxico, el enum TipoToken representa
todos los tipos de tokens que el lenguaje puede reconocer.


*/

public enum TipoToken {
    // Palabras reservadas
    PROGRAM, VAR, PROCEDURE, FUNCTION, BEGIN, END,
    IF, THEN, ELSE, WHILE, DO, WRITE, READ,
    INTEGER, BOOLEAN, TRUE, FALSE,
    AND, OR, NOT, DIV,

    // Identificadores y números
    IDENTIFICADOR, NUMERO,

    // Operadores
    MAS, MENOS, MULTIPLICACION, DIVISION,
    ASIGNACION, // :=
    IGUAL, MENOR, MAYOR, MENOR_IGUAL, MAYOR_IGUAL, DISTINTO,

    // Símbolos
    PARENTESIS_ABRE, PARENTESIS_CIERRA,
    CORCHETE_ABRE, CORCHETE_CIERRA,
    PUNTO_Y_COMA, COMA, DOS_PUNTOS, PUNTO,

    FIN_ARCHIVO;
}
