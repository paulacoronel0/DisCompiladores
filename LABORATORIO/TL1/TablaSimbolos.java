import java.util.HashMap;
import java.util.Map;

// una TablaSimbolos representa UN ámbito (el global, o el propio de un
// procedimiento/función). Los ámbitos se encadenan mediante "padre",
// siguiendo el anidamiento léxico del programa fuente (alcance estático
// de Pascal): el ámbito de un subprograma anidado enlaza con el ámbito
// en el que fue declarado.
public class TablaSimbolos {

    private Map<String, Simbolo> simbolos;
    private TablaSimbolos padre; // null únicamente en la tabla global
    private int nivel;
    private String nombreAmbito = "global"; // lo pisa SimboloSubprograma al crear su tablaLocal

    public TablaSimbolos(TablaSimbolos padre) {
        this.simbolos = new HashMap<>();
        this.padre = padre;
        this.nivel = (padre == null) ? 0 : padre.nivel + 1;
    }

    // inserta un símbolo nuevo en ESTE ámbito.
    // devuelve false si el nombre ya existía en el mismo ámbito
    // (esto es exactamente el "chequeo de unicidad" del laboratorio anterior).
    public boolean insertar(Simbolo simbolo) {
        if (simbolos.containsKey(simbolo.getNombre())) {
            return false;
        }
        simbolos.put(simbolo.getNombre(), simbolo);
        return true;
    }

    // busca solo en este ámbito, sin subir a los contenedores.
    // se usa para decidir si una nueva declaración colisiona con una existente.
    public Simbolo buscarLocal(String nombre) {
        return simbolos.get(nombre);
    }

    // busca el nombre en este ámbito y, si no está, en los ámbitos
    // contenedores en cadena. Así se resuelve una referencia a una variable
    // declarada en un bloque exterior (visibilidad léxica).
    public Simbolo buscar(String nombre) {
        Simbolo simbolo = simbolos.get(nombre);
        if (simbolo != null) {
            return simbolo;
        }
        if (padre != null) {
            return padre.buscar(nombre);
        }
        return null;
    }

    public TablaSimbolos getPadre() {
        return padre;
    }

    public int getNivel() {
        return nivel;
    }

    // lo llama SimboloSubprograma al construir su tablaLocal, para que la
    // impresión diga de qué procedimiento/función es cada ámbito
    public void setNombreAmbito(String nombreAmbito) {
        this.nombreAmbito = nombreAmbito;
    }

    // vuelca SOLO los símbolos de este ámbito puntual
    public void imprimir() {
        System.out.println("--- Ambito '" + nombreAmbito + "' (nivel " + nivel + ") ---");
        for (Simbolo s : simbolos.values()) {
            System.out.println("  " + s);
        }
    }

    // vuelca este ámbito y, recursivamente, el ámbito local de cada
    // procedimiento/función declarado en él (y los que ESOS a su vez
    // contengan, si hay subprogramas anidados). Así se ve, al terminar el
    // análisis, la foto completa del árbol de ámbitos construido.
    public void imprimirRecursivo() {
        imprimir();
        for (Simbolo s : simbolos.values()) {
            if (s instanceof SimboloSubprograma) {
                ((SimboloSubprograma) s).getTablaLocal().imprimirRecursivo();
            }
        }
    }
}