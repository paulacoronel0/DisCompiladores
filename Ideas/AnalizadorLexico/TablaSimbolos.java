import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collection;

/**
 * TablaSimbolos.java
 * Implementa la Tabla de Símbolos (TS) del analizador léxico.
 *
 * Almacena los identificadores encontrados durante el análisis,
 * asignando a cada uno un índice numérico único que actúa como
 * "puntero a la TS" (valor de atributo del token identificador).
 */
public class TablaSimbolos {

    private final Map<String, Integer> tabla = new LinkedHashMap<>();
    private int contador = 0;

    /**
     * Inserta un identificador. Si ya existe devuelve su índice previo.
     */
    public int insertar(String lexema) {
        if (!tabla.containsKey(lexema)) {
            tabla.put(lexema, contador++);
        }
        return tabla.get(lexema);
    }

    public boolean contiene(String lexema)   { return tabla.containsKey(lexema); }
    public int obtenerIndice(String lexema)  { return tabla.getOrDefault(lexema, -1); }
    public Collection<String> getLexemas()   { return tabla.keySet(); }
    public Map<String, Integer> getTabla()   { return tabla; }

    /**
     * Construye y retorna la tabla de símbolos como String.
     * Usado tanto para consola como para escritura en archivo.
     */
    public String construirString() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════╗\n");
        sb.append("║         TABLA DE SIMBOLOS            ║\n");
        sb.append("╠════════╦═════════════════════════════╣\n");
        sb.append("║ Indice ║ Identificador               ║\n");
        sb.append("╠════════╬═════════════════════════════╣\n");
        if (tabla.isEmpty()) {
            sb.append("║        ║ (vacia)                     ║\n");
        } else {
            for (Map.Entry<String, Integer> entry : tabla.entrySet()) {
                sb.append(String.format("║  %-5d  ║ %-27s ║%n",
                        entry.getValue(), entry.getKey()));
            }
        }
        sb.append("╚════════╩═════════════════════════════╝\n");
        return sb.toString();
    }

    /** Imprime la tabla de símbolos en consola. */
    public void imprimir() {
        System.out.println(construirString());
    }
}
