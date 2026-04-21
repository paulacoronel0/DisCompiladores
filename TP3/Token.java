public class Token {
    public String tipo;
    public String lexema;
    public int linea;

    public Token(String tipo, String lexema, int linea) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
    }

    @Override
    public String toString() {
        return "Token{" + tipo + ", '" + lexema + "', linea=" + linea + "}";
    }
}
