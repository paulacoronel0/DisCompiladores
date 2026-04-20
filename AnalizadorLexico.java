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
    private static final Map<String, TipoToken> PALABRAS_RESERVADAS = new HashMap();

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
        throw new RuntimeException("error lexico en linea " + this.linea + ": " + var1);
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
                    StringBuilder var4 = new StringBuilder();

                    // construye el lexema
                    while (Character.isLetterOrDigit(this.caracterActual())) {
                        var4.append(this.caracterActual());
                        this.avanzar();
                    }

                    String var2 = var4.toString().toLowerCase();

                    // si es palabra reservada la devuelve, sino identificador
                    TipoToken var3 = (TipoToken) PALABRAS_RESERVADAS.getOrDefault(var2, TipoToken.IDENTIFICADOR);

                    return new Token(var3, var2, this.linea);
                }

                // reconocimiento de numeros
                if (Character.isDigit(this.caracterActual())) {
                    StringBuilder var1 = new StringBuilder();

                    while (Character.isDigit(this.caracterActual())) {
                        var1.append(this.caracterActual());
                        this.avanzar();
                    }

                    return new Token(TipoToken.NUMERO, var1.toString(), this.linea);
                }

                // reconocimiento de simbolos y operadores
                switch (this.caracterActual()) {

                    case '(':
                        this.avanzar();
                        return new Token(TipoToken.PARENTESIS_ABRE, "(", this.linea);

                    case ')':
                        this.avanzar();
                        return new Token(TipoToken.PARENTESIS_CIERRA, ")", this.linea);

                    case '*':
                        this.avanzar();
                        return new Token(TipoToken.MULTIPLICACION, "*", this.linea);

                    case '+':
                        this.avanzar();
                        return new Token(TipoToken.MAS, "+", this.linea);

                    case ',':
                        this.avanzar();
                        return new Token(TipoToken.COMA, ",", this.linea);

                    case '-':
                        this.avanzar();
                        return new Token(TipoToken.MENOS, "-", this.linea);

                    case '.':
                        this.avanzar();
                        return new Token(TipoToken.PUNTO, ".", this.linea);

                    case '/':
                        this.avanzar();
                        return new Token(TipoToken.DIVISION, "/", this.linea);

                    // operador asignacion o dos puntos
                    case ':':
                        this.avanzar();
                        if (this.caracterActual() == '=') {
                            this.avanzar();
                            return new Token(TipoToken.ASIGNACION, ":=", this.linea);
                        }
                        return new Token(TipoToken.DOS_PUNTOS, ":", this.linea);

                    case ';':
                        this.avanzar();
                        return new Token(TipoToken.PUNTO_Y_COMA, ";", this.linea);

                    // operadores relacionales
                    case '<':
                        this.avanzar();
                        if (this.caracterActual() == '=') {
                            this.avanzar();
                            return new Token(TipoToken.MENOR_IGUAL, "<=", this.linea);
                        }
                        if (this.caracterActual() == '>') {
                            this.avanzar();
                            return new Token(TipoToken.DISTINTO, "<>", this.linea);
                        }
                        return new Token(TipoToken.MENOR, "<", this.linea);

                    case '=':
                        this.avanzar();
                        return new Token(TipoToken.IGUAL, "=", this.linea);

                    case '>':
                        this.avanzar();
                        if (this.caracterActual() == '=') {
                            this.avanzar();
                            return new Token(TipoToken.MAYOR_IGUAL, ">=", this.linea);
                        }
                        return new Token(TipoToken.MAYOR, ">", this.linea);

                    case '[':
                        this.avanzar();
                        return new Token(TipoToken.CORCHETE_ABRE, "[", this.linea);

                    case ']':
                        this.avanzar();
                        return new Token(TipoToken.CORCHETE_CIERRA, "]", this.linea);

                    // cualquier otro caracter es error
                    default:
                        this.error("caracter invalido: " + this.caracterActual());
                }
            }
        }

        // si termina el archivo devuelve token fin
        return new Token(TipoToken.FIN_ARCHIVO, "", this.linea);
    }

    // inicializacion de palabras reservadas
    static {
        PALABRAS_RESERVADAS.put("program", TipoToken.PROGRAM);
        PALABRAS_RESERVADAS.put("var", TipoToken.VAR);
        PALABRAS_RESERVADAS.put("procedure", TipoToken.PROCEDURE);
        PALABRAS_RESERVADAS.put("function", TipoToken.FUNCTION);
        PALABRAS_RESERVADAS.put("begin", TipoToken.BEGIN);
        PALABRAS_RESERVADAS.put("end", TipoToken.END);
        PALABRAS_RESERVADAS.put("if", TipoToken.IF);
        PALABRAS_RESERVADAS.put("then", TipoToken.THEN);
        PALABRAS_RESERVADAS.put("else", TipoToken.ELSE);
        PALABRAS_RESERVADAS.put("while", TipoToken.WHILE);
        PALABRAS_RESERVADAS.put("do", TipoToken.DO);
        PALABRAS_RESERVADAS.put("write", TipoToken.WRITE);
        PALABRAS_RESERVADAS.put("read", TipoToken.READ);
        PALABRAS_RESERVADAS.put("integer", TipoToken.INTEGER);
        PALABRAS_RESERVADAS.put("boolean", TipoToken.BOOLEAN);
        PALABRAS_RESERVADAS.put("true", TipoToken.TRUE);
        PALABRAS_RESERVADAS.put("false", TipoToken.FALSE);
        PALABRAS_RESERVADAS.put("and", TipoToken.AND);
        PALABRAS_RESERVADAS.put("or", TipoToken.OR);
        PALABRAS_RESERVADAS.put("not", TipoToken.NOT);
        PALABRAS_RESERVADAS.put("div", TipoToken.DIV);
    }
}