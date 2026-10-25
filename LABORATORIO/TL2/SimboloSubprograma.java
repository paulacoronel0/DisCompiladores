
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
    private String etiqueta; // etiqueta mepa del punto de entrada (destino de LLPR para llamada de procedimientos)

    public SimboloSubprograma(String nombre, CategoriaSimbolo categoria, int lineaDeclaracion,
            TipoDato tipoRetorno, TablaSimbolos tablaPadre) {
        super(nombre, categoria, null, lineaDeclaracion);
        this.tipoRetorno = tipoRetorno;
        this.parametros = new ArrayList<>();
        this.tablaLocal = new TablaSimbolos(tablaPadre);
        this.tablaLocal.setNombreAmbito(nombre); // así imprimirRecursivo() muestra a quién pertenece
    }

    // agrega el parametro a la lista de parametros
    public boolean agregarParametro(Simbolo parametro) {
        parametros.add(parametro); // añade el parametro al final de la lista

        boolean ok = tablaLocal.insertar(parametro); // inserta el parametro en la tabla de simbolos local

        recalcularDesplazamientosParametros(); // actualiza los desplazamientos de todos los parametros

        return ok; // devuelve si la insercion se realizo correctamente
    }

    // calcula el desplazamiento de cada parametro segun su posicion en la lista
    private void recalcularDesplazamientosParametros() {
        int n = parametros.size(); // obtiene la cantidad total de parametros

        // recorre todos los parametros de la lista
        for (int i = 0; i < n; i++) {
            parametros.get(i).setDesplazamiento(-(n + 2 - i)); // asigna el desplazamiento negativo segun la posicion
            // del parametro
        }
    }

    public void setEtiqueta(String s) {
        this.etiqueta = s;
    }

    // devuelve el desplazamiento de la variable implicita que almacena el resultado
    // de la funcion
    public int getDesplazamientoRetorno() {
        return -(parametros.size() + 3); // calcula el desplazamiento usando la cantidad de parametros
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

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return "SimboloSubprograma{" + nombre + ", " + categoria
                + ", parametros=" + parametros.size()
                + ", retorno=" + tipoRetorno
                + ", linea=" + lineaDeclaracion + '}';
    }
}
