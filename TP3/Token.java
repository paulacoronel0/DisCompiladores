
public class Token {

    private String tipo;
    private String lexema;
    private int linea;

    public Token(String tipo, String lexema, int linea) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
    }

    public String getTipo() {
        return this.tipo;
    }

    @Override
    public String toString() {
        return "Token{" + tipo + ", '" + lexema + "', linea=" + linea + "}";
    }
}
