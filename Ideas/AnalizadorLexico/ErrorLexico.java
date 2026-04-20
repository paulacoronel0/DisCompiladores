/**
 * ErrorLexico.java
 * Excepción lanzada cuando el analizador léxico encuentra un carácter
 * o secuencia que no puede clasificar como ningún token válido.
 */
public class ErrorLexico extends Exception {

    private final int linea;
    private final char caracter;

    /**
     * @param caracter  El carácter ilegal encontrado
     * @param linea     La línea del archivo fuente donde ocurrió
     */
    public ErrorLexico(char caracter, int linea) {
        super(String.format(
            "Error léxico en línea %d: carácter inesperado '%c' (código ASCII: %d)",
            linea, caracter, (int) caracter
        ));
        this.linea    = linea;
        this.caracter = caracter;
    }

    public int  getLinea()    { return linea; }
    public char getCaracter() { return caracter; }
}
