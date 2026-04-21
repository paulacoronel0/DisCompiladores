// analizador lexico para un subconjunto de mini-pascal
// este codigo se encarga de leer el texto de entrada y separar los tokens

import java.util.HashMap;
import java.util.Map;

public class AnalizadorLexico {

    // string con todo el codigo fuente
    private String entrada;

    // posicion actual dentro del string
    private int posicion = 0;

    // numero de linea actual (para errores)
    private int linea = 1;

    // tabla con palabras reservadas del lenguaje
    private static final Map<String, String> PALABRAS_RESERVADAS = new HashMap();

    // constructor que recibe el texto de entrada
    public AnalizadorLexico(String var1) {
        this.entrada = var1;
    }

    // devuelve el caracter actual o fin de archivo si se termina
    private char caracterActual() {
        return this.posicion >= this.entrada.length() ? '\u0000' : this.entrada.charAt(this.posicion);
    }

    // avanza una posicion en el texto
    // si encuentra salto de linea, incrementa el contador de linea
    private void avanzar() {
        if (this.caracterActual() == '\n') {
            ++this.linea;
        }
        ++this.posicion;
    }

    // salta espacios, tabs y saltos de linea
    private void saltarEspacios() {
        while (Character.isWhitespace(this.caracterActual())) {
            this.avanzar();
        }
    }

    // salta comentarios del tipo { comentario }
    private void saltarComentario() {
        this.avanzar(); // consume '{'

        while (this.caracterActual() != '}' && this.caracterActual() != 0) {
            this.avanzar();
        }

        // si encuentra cierre, sigue, si no tira error
        if (this.caracterActual() == '}') {
            this.avanzar();
        } else {
            this.error("comentario no cerrado");
        }
    }

    // lanza error lexico con numero de linea
    private void error(String var1) {
        throw new RuntimeException("Error lexico en linea " + this.linea + ": " + var1);
    }

    // funcion principal: devuelve el siguiente token
    public Token siguienteToken() {

        // mientras no llegue al fin del archivo
        while (this.caracterActual() != 0) {

            // ignora espacios
            if (Character.isWhitespace(this.caracterActual())) {
                this.saltarEspacios();
            }

            // ignora comentarios
            else if (this.caracterActual() == '{') {
                this.saltarComentario();
            }

            else {

                // reconocimiento de identificadores o palabras reservadas
                if (Character.isLetter(this.caracterActual())) {
                    StringBuilder acum_caracteres = new StringBuilder();

                    // construye el lexema
                    while (Character.isLetterOrDigit(this.caracterActual())) {
                        acum_caracteres.append(this.caracterActual());
                        this.avanzar();
                    }

                    String lexema_final = acum_caracteres.toString().toLowerCase();

                    // si es palabra reservada la devuelve, sino identificador
                    String tipo_token = (String) PALABRAS_RESERVADAS.getOrDefault(lexema_final,  "IDENTIFICADOR");

                    //consulta hay que almacenar las variables/identificadores, que se usan varias veces

                    return new Token(tipo_token, lexema_final, this.linea);
                }

                // reconocimiento de numeros
                if (Character.isDigit(this.caracterActual())) {
                    StringBuilder numero = new StringBuilder();

                    while (Character.isDigit(this.caracterActual())) {
                        numero.append(this.caracterActual());
                        this.avanzar();
                    }

                    return new Token("NUMERO", numero.toString(), this.linea);
                }

                // reconocimiento de simbolos y operadores
                switch (this.caracterActual()) {

                    case '(':
                        this.avanzar();
                        return new Token("PARENTESIS_ABRE", "(", this.linea);

                    case ')':
                        this.avanzar();
                        return new Token( "PARENTESIS_CIERRA", ")", this.linea);

                    case '*':
                        this.avanzar();
                        return new Token( "MULTIPLICACION", "*", this.linea);

                    case '+':
                        this.avanzar();
                        return new Token( "MAS", "+", this.linea);

                    case ',':
                        this.avanzar();
                        return new Token( "COMA", ",", this.linea);

                    case '-':
                        this.avanzar();
                        return new Token( "MENOS", "-", this.linea);

                    case '.':
                        this.avanzar();
                        return new Token( "PUNTO", ".", this.linea);

                    case '/':
                        this.avanzar();
                        return new Token( "DIVISION", "/", this.linea);

                    // operador asignacion o dos puntos
                    case ':':
                        this.avanzar();
                        if (this.caracterActual() == '=') {
                            this.avanzar();
                            return new Token( "ASIGNACION", ":=", this.linea);
                        }
                        return new Token( "DOS_PUNTOS", ":", this.linea);

                    case ';':
                        this.avanzar();
                        return new Token( "PUNTO_Y_COMA", ";", this.linea);

                    // operadores relacionales
                    case '<':
                        this.avanzar();
                        if (this.caracterActual() == '=') {
                            this.avanzar();
                            return new Token( "MENOR_IGUAL", "<=", this.linea);
                        }
                        if (this.caracterActual() == '>') {
                            this.avanzar();
                            return new Token( "DISTINTO", "<>", this.linea);
                        }
                        return new Token( "MENOR", "<", this.linea);

                    case '=':
                        this.avanzar();
                        return new Token( "IGUAL", "=", this.linea);

                    case '>':
                        this.avanzar();
                        if (this.caracterActual() == '=') {
                            this.avanzar();
                            return new Token( "MAYOR_IGUAL", ">=", this.linea);
                        }
                        return new Token( "MAYOR", ">", this.linea);
                    // cualquier otro caracter es error
                    default:
                        String caracter_invalido = "caracter invalido " + this.caracterActual();
                        this.error(caracter_invalido);
                }
            }
        }

        // si termina el archivo devuelve token fin
        return new Token("FIN_ARCHIVO", "", this.linea);
    }

    // inicializacion de palabras reservadas
    static {
        PALABRAS_RESERVADAS.put("program", "PROGRAM");
        PALABRAS_RESERVADAS.put("var",  "VAR");
        PALABRAS_RESERVADAS.put("procedure",  "PROCEDURE");
        PALABRAS_RESERVADAS.put("function",  "FUNCTION");
        PALABRAS_RESERVADAS.put("begin",  "BEGIN");
        PALABRAS_RESERVADAS.put("end",  "END");
        PALABRAS_RESERVADAS.put("if",  "IF");
        PALABRAS_RESERVADAS.put("then",  "THEN");
        PALABRAS_RESERVADAS.put("else",  "ELSE");
        PALABRAS_RESERVADAS.put("while",  "WHILE");
        PALABRAS_RESERVADAS.put("do",  "DO");
        PALABRAS_RESERVADAS.put("write",  "WRITE");
        PALABRAS_RESERVADAS.put("read",  "READ");
        PALABRAS_RESERVADAS.put("integer",  "INTEGER");
        PALABRAS_RESERVADAS.put("boolean",  "BOOLEAN");
        PALABRAS_RESERVADAS.put("true",  "TRUE");
        PALABRAS_RESERVADAS.put("false",  "FALSE");
        PALABRAS_RESERVADAS.put("and",  "AND");
        PALABRAS_RESERVADAS.put("or",  "OR");
        PALABRAS_RESERVADAS.put("not",  "NOT");
        PALABRAS_RESERVADAS.put("div",  "DIV");
    }
}