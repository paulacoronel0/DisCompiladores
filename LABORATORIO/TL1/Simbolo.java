// entrada genérica de la tabla de símbolos: variables y parámetros usan
// esta clase directamente; procedimientos y funciones usan la subclase
// SimboloSubprograma, que agrega la información propia de un subprograma.
public class Simbolo {

    protected String nombre;
    protected CategoriaSimbolo categoria;
    protected TipoDato tipo; // null en PROCEDIMIENTO/FUNCION (ver SimboloSubprograma.tipoRetorno)
    protected int lineaDeclaracion;

    public Simbolo(String nombre, CategoriaSimbolo categoria, TipoDato tipo, int lineaDeclaracion) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.tipo = tipo;
        this.lineaDeclaracion = lineaDeclaracion;
    }

    public String getNombre() {
        return nombre;
    }

    public CategoriaSimbolo getCategoria() {
        return categoria;
    }

    public TipoDato getTipo() {
        return tipo;
    }

    public int getLineaDeclaracion() {
        return lineaDeclaracion;
    }

    @Override
    public String toString() {
        return "Simbolo{" + nombre + ", " + categoria + ", tipo=" + tipo
                + ", linea=" + lineaDeclaracion + '}';
    }
}
