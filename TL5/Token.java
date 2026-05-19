public class Token {

    private TipoToken tipo;
    private String lexema;
    private int linea;

    public Token(TipoToken tipo, String lexema, int linea) {

        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getLinea() {
        return linea;
    }
}