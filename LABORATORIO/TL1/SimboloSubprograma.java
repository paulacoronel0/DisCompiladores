import java.util.ArrayList;
import java.util.List;

// entrada de la tabla de símbolos para PROCEDIMIENTO o FUNCION.
// guarda la firma (parámetros, en orden, y tipo de retorno) necesaria para
// chequear cada llamada, y una TablaSimbolos propia donde se declaran los
// parámetros y las variables locales del subprograma.
public class SimboloSubprograma extends Simbolo {

    // lista y no mapa: el orden importa para comparar contra los argumentos
    // reales en cada invocación (cantidad y tipos, en la posición correcta)
    private List<Simbolo> parametros;
    private TipoDato tipoRetorno; // null si es PROCEDIMIENTO
    private TablaSimbolos tablaLocal;

    public SimboloSubprograma(String nombre, CategoriaSimbolo categoria, int lineaDeclaracion,
            TipoDato tipoRetorno, TablaSimbolos tablaPadre) {
        super(nombre, categoria, null, lineaDeclaracion);
        this.tipoRetorno = tipoRetorno;
        this.parametros = new ArrayList<>();
        this.tablaLocal = new TablaSimbolos(tablaPadre);
    }

    public void agregarParametro(Simbolo parametro) {
        parametros.add(parametro);
        tablaLocal.insertar(parametro);
    }

    public List<Simbolo> getParametros() {
        return parametros;
    }

    public TipoDato getTipoRetorno() {
        return tipoRetorno;
    }

    public TablaSimbolos getTablaLocal() {
        return tablaLocal;
    }

    @Override
    public String toString() {
        return "SimboloSubprograma{" + nombre + ", " + categoria
                + ", parametros=" + parametros.size()
                + ", retorno=" + tipoRetorno
                + ", linea=" + lineaDeclaracion + '}';
    }
}
