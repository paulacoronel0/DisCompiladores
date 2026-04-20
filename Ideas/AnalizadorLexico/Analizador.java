import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Analizador.java  —  Punto de entrada del Analizador Léxico mini-Pascal
 *
 * Trabajo de Laboratorio N°2  –  Compiladores
 * Universidad Nacional del Comahue
 *
 * Uso desde línea de comandos:
 *   $ java Analizador archivodeprueba.pas
 *
 * Archivos generados automáticamente (mismo directorio que el fuente):
 *   - archivodeprueba.tokens  →  lista de tokens y tabla de símbolos
 *   - archivodeprueba.errores →  errores léxicos (vacío si no hay errores)
 *
 * Todo lo escrito en los archivos también se imprime por consola.
 */
public class Analizador {

    private static final int EXIT_ERROR_ARGS  = 1;
    private static final int EXIT_ERROR_IO    = 2;
    private static final int EXIT_ERRORES_LEX = 3;

    public static void main(String[] args) {

        // UTF-8 en consola
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        // ------------------------------------------------------------------
        // 1. Validar argumentos
        // ------------------------------------------------------------------
        if (args.length != 1) {
            System.err.println("Uso: java Analizador <archivo.pas>");
            System.exit(EXIT_ERROR_ARGS);
        }

        String nombreArchivo = args[0];
        File archivo = new File(nombreArchivo);

        if (!archivo.exists()) {
            System.err.println("Error: no se encontro el archivo '" + nombreArchivo + "'");
            System.exit(EXIT_ERROR_IO);
        }
        if (!archivo.canRead()) {
            System.err.println("Error: no se puede leer el archivo '" + nombreArchivo + "'");
            System.exit(EXIT_ERROR_IO);
        }

        // ------------------------------------------------------------------
        // 2. Calcular nombres de archivos de salida
        //    "prog.pas" → "prog.tokens" y "prog.errores"
        // ------------------------------------------------------------------
        String base = nombreArchivo;
        int punto = nombreArchivo.lastIndexOf('.');
        if (punto > 0) base = nombreArchivo.substring(0, punto);

        String archivoTokens  = base + ".tokens";
        String archivoErrores = base + ".errores";

        // ------------------------------------------------------------------
        // 3. Ejecutar el análisis léxico
        // ------------------------------------------------------------------
        TablaSimbolos tablaSimbolos = new TablaSimbolos();
        List<Token>   tokens;
        AnalizadorLexico lexer;

        try (FileInputStream fis = new FileInputStream(archivo)) {
            lexer  = new AnalizadorLexico(fis, tablaSimbolos);
            tokens = lexer.analizar();
        } catch (IOException e) {
            System.err.println("Error de E/S al leer el archivo: " + e.getMessage());
            System.exit(EXIT_ERROR_IO);
            return;
        }

        List<String> errores = lexer.getErrores();

        // ------------------------------------------------------------------
        // 4. Construir contenido del archivo de TOKENS
        // ------------------------------------------------------------------
        StringBuilder sbTokens = new StringBuilder();
        String sep = "=".repeat(68);
        String lin = "-".repeat(68);

        sbTokens.append("╔").append("═".repeat(68)).append("╗\n");
        sbTokens.append(String.format("║  %-66s║%n",
                "ANALIZADOR LEXICO  --  mini-Pascal  (TP Lab N.2)"));
        sbTokens.append("╠").append("═".repeat(68)).append("╣\n");
        sbTokens.append(String.format("║  Archivo analizado : %-47s║%n", nombreArchivo));
        sbTokens.append(String.format("║  Archivo de tokens : %-47s║%n", archivoTokens));
        sbTokens.append(String.format("║  Archivo de errores: %-47s║%n", archivoErrores));
        sbTokens.append("╚").append("═".repeat(68)).append("╝\n\n");

        sbTokens.append("── TOKENS RECONOCIDOS ").append("─".repeat(47)).append("\n");
        if (tokens.isEmpty()) {
            sbTokens.append("  (ningun token encontrado)\n");
        } else {
            for (Token t : tokens) {
                sbTokens.append("  ").append(t).append("\n");
            }
        }

        sbTokens.append("\n").append(tablaSimbolos.construirString());

        sbTokens.append("\n── RESUMEN ").append("─".repeat(57)).append("\n");
        sbTokens.append(String.format("  Tokens reconocidos : %d%n", tokens.size()));
        sbTokens.append(String.format("  Identificadores    : %d%n",
                tablaSimbolos.getTabla().size()));
        sbTokens.append(String.format("  Errores lexicos    : %d%n", errores.size()));
        if (errores.isEmpty()) {
            sbTokens.append("\n  Analisis completado sin errores.\n");
        } else {
            sbTokens.append("\n  Se encontraron errores. Consulte: ")
                    .append(archivoErrores).append("\n");
        }

        // ------------------------------------------------------------------
        // 5. Construir contenido del archivo de ERRORES
        // ------------------------------------------------------------------
        StringBuilder sbErrores = new StringBuilder();
        sbErrores.append("╔").append("═".repeat(68)).append("╗\n");
        sbErrores.append(String.format("║  %-66s║%n",
                "ERRORES LEXICOS  --  mini-Pascal  (TP Lab N.2)"));
        sbErrores.append("╠").append("═".repeat(68)).append("╣\n");
        sbErrores.append(String.format("║  Archivo analizado : %-47s║%n", nombreArchivo));
        sbErrores.append("╚").append("═".repeat(68)).append("╝\n\n");

        if (errores.isEmpty()) {
            sbErrores.append("  No se encontraron errores lexicos.\n");
        } else {
            sbErrores.append(String.format("  Total de errores: %d%n%n", errores.size()));
            sbErrores.append("── DETALLE ").append("─".repeat(57)).append("\n");
            for (int i = 0; i < errores.size(); i++) {
                sbErrores.append(String.format("  [%d] %s%n", i + 1, errores.get(i)));
            }
        }

        // ------------------------------------------------------------------
        // 6. Imprimir TODO por consola
        // ------------------------------------------------------------------
        System.out.println(sbTokens);
        System.out.println(sbErrores);

        // ------------------------------------------------------------------
        // 7. Escribir archivo .tokens
        // ------------------------------------------------------------------
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(
                    new FileOutputStream(archivoTokens), StandardCharsets.UTF_8))) {
            pw.print(sbTokens);
            System.out.println("  Archivo de tokens  guardado en: " + archivoTokens);
        } catch (IOException e) {
            System.err.println("Error al escribir '" + archivoTokens + "': " + e.getMessage());
        }

        // ------------------------------------------------------------------
        // 8. Escribir archivo .errores  (siempre se crea)
        // ------------------------------------------------------------------
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(
                    new FileOutputStream(archivoErrores), StandardCharsets.UTF_8))) {
            pw.print(sbErrores);
            System.out.println("  Archivo de errores guardado en: " + archivoErrores);
        } catch (IOException e) {
            System.err.println("Error al escribir '" + archivoErrores + "': " + e.getMessage());
        }

        System.out.println();

        // ------------------------------------------------------------------
        // 9. Código de salida
        // ------------------------------------------------------------------
        if (!errores.isEmpty()) {
            System.exit(EXIT_ERRORES_LEX);
        }
    }
}
