import java.io.*;
import java.util.*;

/**
 * AnalizadorLexico.java
 *
 * Implementación del analizador léxico para el subconjunto mini-Pascal
 * definido en el Trabajo de Laboratorio N°2.
 *
 * Estrategia de análisis:
 *   - Lectura carácter a carácter mediante un Reader con lookahead de 1.
 *   - Se reconocen los tokens mediante un autómata implementado en el
 *     método siguienteToken(), que despacha según el primer carácter leído.
 *   - Los comentarios ( { ... } ) se consumen silenciosamente.
 *   - Los espacios en blanco (espacio, tab, newline, CR) se ignoran.
 *
 * Tokens reconocidos (según Tabla 1 del TP2):
 *   - Palabras reservadas: program, var, procedure, function, begin, end,
 *                          if, then, else, while, do, write, read,
 *                          integer, boolean, true, false, AND, OR, NOT, div
 *   - Identificadores (cualquier secuencia letra/dígito que comience con letra)
 *   - Operadores aritméticos: + - * /
 *   - Asignación: :=
 *   - Operadores relacionales: = < >
 *   - Agrupadores: ( ) [ ]
 *   - Comentarios: { }  (inicio_comentario / final_comentario, o consumidos)
 *   - Separadores: ; , : .
 */
public class AnalizadorLexico {

    // -----------------------------------------------------------------------
    // Palabras reservadas del lenguaje mini-Pascal
    // -----------------------------------------------------------------------
    private static final Set<String> PALABRAS_RESERVADAS = new HashSet<>(Arrays.asList(
        "program", "var", "procedure", "function", "begin", "end",
        "if", "then", "else", "while", "do", "write", "read",
        "integer", "boolean", "true", "false", "AND", "OR", "NOT", "div"
    ));

    // -----------------------------------------------------------------------
    // Estado interno del lector
    // -----------------------------------------------------------------------

    /** Buffer de lectura sobre el archivo fuente. */
    private final PushbackReader reader;

    /** Tabla de símbolos compartida con el programa principal. */
    private final TablaSimbolos tablaSimbolos;

    /** Número de línea actual (inicia en 1). */
    private int lineaActual = 1;

    /** Lista acumulada de errores léxicos encontrados (no aborta al primero). */
    private final List<String> errores = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param fuente        InputStream del archivo .pas a analizar
     * @param tablaSimbolos Tabla de símbolos donde se registrarán identificadores
     * @throws IOException  Si hay problema al abrir el reader
     */
    public AnalizadorLexico(InputStream fuente, TablaSimbolos tablaSimbolos)
            throws IOException {
        this.reader        = new PushbackReader(new InputStreamReader(fuente), 2);
        this.tablaSimbolos = tablaSimbolos;
    }

    // -----------------------------------------------------------------------
    // API pública
    // -----------------------------------------------------------------------

    /**
     * Analiza todo el archivo fuente y devuelve la lista completa de tokens.
     * Los comentarios y espacios se descartan.
     * Los errores se acumulan en la lista interna (ver {@link #getErrores()}).
     *
     * @return Lista de tokens en orden de aparición
     * @throws IOException Si ocurre un error de lectura
     */
    public List<Token> analizar() throws IOException {
        List<Token> tokens = new ArrayList<>();
        Token t;
        while ((t = siguienteToken()) != null) {
            tokens.add(t);
        }
        return tokens;
    }

    /** Retorna la lista de mensajes de error léxico acumulados. */
    public List<String> getErrores() { return errores; }

    // -----------------------------------------------------------------------
    // Motor principal: obtener el próximo token
    // -----------------------------------------------------------------------

    /**
     * Lee caracteres del fuente y retorna el siguiente Token reconocido,
     * o null si se llegó al final del archivo.
     *
     * @throws IOException Si ocurre un error de lectura
     */
    private Token siguienteToken() throws IOException {

        // --- Saltar espacios en blanco y newlines ---
        int c;
        while (true) {
            c = leerChar();
            if (c == -1) return null;          // EOF
            if (c == '\n') { lineaActual++; continue; }
            if (c == '\r') continue;
            if (c == ' ' || c == '\t') continue;
            break; // carácter significativo
        }

        char ch = (char) c;
        int lineaToken = lineaActual; // guardamos línea al inicio del lexema

        // ---------------------------------------------------------------
        // COMENTARIOS: { ... }
        //   Se consumen silenciosamente.  Si se quiere emitir tokens de
        //   inicio/fin de comentario, cambiar el return por construcción
        //   del token correspondiente.
        // ---------------------------------------------------------------
        if (ch == '{') {
            consumirComentario();
            return siguienteToken(); // después del comentario, buscar siguiente
        }

        // ---------------------------------------------------------------
        // IDENTIFICADORES y PALABRAS RESERVADAS
        // ---------------------------------------------------------------
        if (Character.isLetter(ch)) {
            return reconocerIdentificadorOPalabraReservada(ch, lineaToken);
        }

        // ---------------------------------------------------------------
        // LITERALES ENTEROS (secuencia de dígitos)
        // Aunque la tabla del TP2 no los lista explícitamente, son
        // necesarios para cualquier programa mini-Pascal real.
        // ---------------------------------------------------------------
        if (Character.isDigit(ch)) {
            return reconocerEntero(ch, lineaToken);
        }

        // ---------------------------------------------------------------
        // OPERADORES Y SÍMBOLOS DE UN SOLO CARÁCTER
        // ---------------------------------------------------------------
        switch (ch) {

            // --- Operadores aritméticos ---
            case '+': return new Token("operador_aritmetico", "+", "+", lineaToken);
            case '-': return new Token("operador_aritmetico", "-", "-", lineaToken);
            case '*': return new Token("operador_aritmetico", "*", "*", lineaToken);
            case '/': return new Token("operador_aritmetico", "/", "/", lineaToken);

            // --- Operadores relacionales simples ---
            case '=': return new Token("operador_relacional", "=", "igual",  lineaToken);
            case '<': return new Token("operador_relacional", "<", "menor", lineaToken);
            case '>': return new Token("operador_relacional", ">", "mayor", lineaToken);

            // --- Asignación := vs. dos_puntos : ---
            case ':': return reconocerDosPuntosOAsignacion(lineaToken);

            // --- Agrupadores ---
            case '(': return new Token("parentesis_abre",  "(", null, lineaToken);
            case ')': return new Token("parentesis_cierra", ")", null, lineaToken);
            case '[': return new Token("llave_abre",  "[", "[", lineaToken);
            case ']': return new Token("llave_cierra", "]", "]", lineaToken);

            // --- Separadores ---
            case ';': return new Token("punto_y_coma", ";", ";", lineaToken);
            case ',': return new Token("coma",         ",", ",", lineaToken);
            case '.': return new Token("punto",        ".", ".", lineaToken);

            // ---------------------------------------------------------------
            // CARÁCTER NO RECONOCIDO → Error léxico (se acumula y se continúa)
            // ---------------------------------------------------------------
            default:
                String msg = String.format(
                    "Error léxico en línea %d: carácter inesperado '%c' (ASCII %d)",
                    lineaToken, ch, (int) ch
                );
                errores.add(msg);
                System.err.println(msg);
                // Recuperación: ignorar el carácter y buscar el siguiente token
                return siguienteToken();
        }
    }

    // -----------------------------------------------------------------------
    // Subrutinas de reconocimiento
    // -----------------------------------------------------------------------

    /**
     * Reconoce un identificador o palabra reservada.
     * Gramática:  letra (letra | dígito)*
     * Las palabras reservadas son case-sensitive según la tabla del TP2
     * (AND, OR, NOT en mayúsculas; el resto en minúsculas).
     *
     * @param primero    Primer carácter ya leído
     * @param linea      Línea donde comienza el lexema
     * @return           Token "identificador" o el token de la palabra reservada
     */
    private Token reconocerIdentificadorOPalabraReservada(char primero, int linea)
            throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append(primero);

        int c;
        while ((c = leerChar()) != -1) {
            char ch = (char) c;
            if (Character.isLetterOrDigit(ch) || ch == '_') {
                sb.append(ch);
            } else {
                devolverChar(c); // lookahead: devolver carácter que no pertenece
                break;
            }
        }

        String lexema = sb.toString();

        // ¿Es palabra reservada?
        if (PALABRAS_RESERVADAS.contains(lexema)) {
            return new Token(lexema, lexema, null, linea);
        }

        // Es identificador: insertar en tabla de símbolos
        int punteroTS = tablaSimbolos.insertar(lexema);
        return new Token("identificador", lexema, "TS[" + punteroTS + "]", linea);
    }

    /**
     * Reconoce ':=' (asignacion) o ':' (dos_puntos).
     * Se aplica lookahead de 1 carácter.
     *
     * @param linea  Línea donde fue leído el ':'
     * @return       Token correspondiente
     */
    private Token reconocerDosPuntosOAsignacion(int linea) throws IOException {
        int siguiente = leerChar();
        if (siguiente != -1 && (char) siguiente == '=') {
            return new Token("asignacion", ":=", "asignacion", linea);
        }
        // No es ':=', devolver el carácter leído de más
        if (siguiente != -1) devolverChar(siguiente);
        return new Token("dos_puntos", ":", ":", linea);
    }

    /**
     * Consume un comentario Pascal de la forma { ... }.
     * Avanza hasta encontrar '}' o EOF.
     * Actualiza lineaActual si hay saltos de línea dentro del comentario.
     *
     * @throws IOException Si ocurre error de lectura
     */
    private void consumirComentario() throws IOException {
        int c;
        while ((c = leerChar()) != -1) {
            if (c == '\n') lineaActual++;
            if ((char) c == '}') return; // fin del comentario
        }
        // Si llegamos aquí, el comentario nunca se cerró
        errores.add("Error léxico en línea " + lineaActual +
                    ": comentario no cerrado (falta '}')");
    }

    /**
     * Reconoce un literal entero: secuencia de uno o más dígitos.
     * Gramática: dígito+
     *
     * @param primero  Primer dígito ya leído
     * @param linea    Línea donde comienza el literal
     * @return         Token "numero_entero" con el valor como atributo
     */
    private Token reconocerEntero(char primero, int linea) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(primero);
        int c;
        while ((c = leerChar()) != -1) {
            char ch = (char) c;
            if (Character.isDigit(ch)) {
                sb.append(ch);
            } else {
                devolverChar(c);
                break;
            }
        }
        String lexema = sb.toString();
        return new Token("numero_entero", lexema, lexema, linea);
    }

    // -----------------------------------------------------------------------
    // Helpers de lectura
    // -----------------------------------------------------------------------

    /** Lee el próximo carácter del PushbackReader. */
    private int leerChar() throws IOException {
        return reader.read();
    }

    /** Devuelve un carácter al buffer (lookahead). */
    private void devolverChar(int c) throws IOException {
        reader.unread(c);
    }
}
