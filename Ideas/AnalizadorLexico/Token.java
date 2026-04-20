/**
 * Token.java
 * Representa un token reconocido por el Analizador Léxico.
 * Contiene el nombre del token, su lexema original y su valor de atributo.
 */
public class Token {

    /** Nombre del token (ej: "identificador", "program", "operador_aritmetico") */
    private final String nombre;

    /** Lexema original tal como aparece en el código fuente */
    private final String lexema;

    /** Valor del atributo asociado (puede ser null si no aplica) */
    private final String atributo;

    /** Número de línea en el archivo fuente donde fue encontrado */
    private final int linea;

    /**
     * Constructor completo.
     *
     * @param nombre    Nombre del token según la tabla de especificación
     * @param lexema    Texto exacto leído del archivo fuente
     * @param atributo  Valor del atributo (null si no aplica)
     * @param linea     Número de línea en el fuente
     */
    public Token(String nombre, String lexema, String atributo, int linea) {
        this.nombre   = nombre;
        this.lexema   = lexema;
        this.atributo = atributo;
        this.linea    = linea;
    }

    public String getNombre()   { return nombre; }
    public String getLexema()   { return lexema; }
    public String getAtributo() { return atributo; }
    public int    getLinea()    { return linea; }

    /**
     * Representación tabular del token para la salida del analizador.
     * Formato: <nombre_token, atributo, línea>
     */
    @Override
    public String toString() {
        String attr = (atributo != null) ? atributo : "-";
        return String.format("< %-22s , %-12s , linea: %d >  [lexema: \"%s\"]",
                nombre, attr, linea, lexema);
    }
}
