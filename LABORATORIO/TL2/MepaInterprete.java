import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// Intérprete mínimo de MEPA según el apunte (máquina de pila con display).
// Uso: java MepaInterprete archivo.mepa < entrada.txt
// Imprime un valor por línea (cada IMPR / IMLN). Los valores de entrada
// para LEER / LELN se toman de la entrada estándar.
// Al llegar a PARA verifica que la pila haya quedado balanceada (s = -1):
// si no lo está, avisa por la salida de error (indica una traducción mal
// balanceada: falta o sobra algún RMEM/LMEM, un IMPR, un argumento, etc.).
public class MepaInterprete {

    static final int TAM_PILA = 100000;
    static final long MAX_PASOS = 5_000_000L;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Uso: java MepaInterprete archivo.mepa < entrada");
            System.exit(2);
        }

        // ---- carga del programa: instrucciones y etiquetas ----
        List<String[]> programa = new ArrayList<>();
        Map<String, Integer> etiquetas = new HashMap<>();
        String etiquetaPendiente = null;
        for (String linea : Files.readAllLines(Paths.get(args[0]))) {
            String[] t = linea.trim().split("\\s+");
            if (t.length == 0 || t[0].isEmpty()) {
                continue;
            }
            if (t[0].matches("L\\d+")) { // línea con etiqueta
                etiquetas.put(t[0], programa.size());
                if (t.length == 1) {
                    continue; // etiqueta sola: apunta a la próxima instrucción
                }
                String[] resto = new String[t.length - 1];
                System.arraycopy(t, 1, resto, 0, resto.length);
                t = resto;
            }
            // operandos separados por coma y/o espacios
            StringBuilder ops = new StringBuilder();
            for (int k = 1; k < t.length; k++) {
                ops.append(t[k]).append(' ');
            }
            String[] operandos = ops.toString().trim().isEmpty()
                    ? new String[0]
                    : ops.toString().trim().split("[,\\s]+");
            String[] inst = new String[operandos.length + 1];
            inst[0] = t[0].toUpperCase();
            System.arraycopy(operandos, 0, inst, 1, operandos.length);
            programa.add(inst);
        }

        // ---- ejecución ----
        long[] M = new long[TAM_PILA];
        int[] D = new int[100];
        int s = -1;
        int i = 0;
        long pasos = 0;
        Scanner entrada = new Scanner(System.in);

        while (true) {
            if (i < 0 || i >= programa.size()) {
                error("el contador de programa salió del código (i=" + i + "); falta PARA?");
            }
            if (++pasos > MAX_PASOS) {
                error("demasiados pasos: posible ciclo infinito");
            }
            String[] in = programa.get(i);
            String op = in[0];
            try {
                switch (op) {
                    case "INPP":
                        s = -1;
                        D[0] = 0;
                        i++;
                        break;
                    case "APCT":
                        M[++s] = Long.parseLong(in[1]);
                        i++;
                        break;
                    case "SUMA":
                        M[s - 1] = M[s - 1] + M[s];
                        s--;
                        i++;
                        break;
                    case "SUST":
                        M[s - 1] = M[s - 1] - M[s];
                        s--;
                        i++;
                        break;
                    case "MULT":
                        M[s - 1] = M[s - 1] * M[s];
                        s--; // el apunte omite este decremento (errata)
                        i++;
                        break;
                    case "DIVI":
                        if (M[s] == 0) {
                            error("division por cero");
                        }
                        M[s - 1] = M[s - 1] / M[s]; // trunca hacia cero, como div de Pascal
                        s--;
                        i++;
                        break;
                    case "MODU":
                        if (M[s] == 0) {
                            error("modulo por cero");
                        }
                        M[s - 1] = M[s - 1] % M[s];
                        s--;
                        i++;
                        break;
                    case "UMEN":
                        M[s] = -M[s];
                        i++;
                        break;
                    case "CONJ":
                        M[s - 1] = (M[s - 1] == 1 && M[s] == 1) ? 1 : 0;
                        s--;
                        i++;
                        break;
                    case "DISJ":
                        M[s - 1] = (M[s - 1] == 1 || M[s] == 1) ? 1 : 0;
                        s--;
                        i++;
                        break;
                    case "NEGA":
                        M[s] = 1 - M[s];
                        i++;
                        break;
                    case "CMME":
                        M[s - 1] = M[s - 1] < M[s] ? 1 : 0;
                        s--;
                        i++;
                        break;
                    case "CMMA":
                        M[s - 1] = M[s - 1] > M[s] ? 1 : 0;
                        s--;
                        i++;
                        break;
                    case "CMIG":
                        M[s - 1] = M[s - 1] == M[s] ? 1 : 0;
                        s--;
                        i++;
                        break;
                    case "CMDG":
                        M[s - 1] = M[s - 1] != M[s] ? 1 : 0;
                        s--;
                        i++;
                        break;
                    case "CMNI":
                        M[s - 1] = M[s - 1] <= M[s] ? 1 : 0;
                        s--;
                        i++;
                        break;
                    case "CMYI":
                        M[s - 1] = M[s - 1] >= M[s] ? 1 : 0;
                        s--;
                        i++;
                        break;
                    case "DSVS":
                        i = destino(etiquetas, in[1]);
                        break;
                    case "DSVF":
                        if (M[s] == 0) {
                            i = destino(etiquetas, in[1]);
                        } else {
                            i++;
                        }
                        s--;
                        break;
                    case "NADA":
                        i++;
                        break;
                    case "LEER":
                    case "LELN":
                        if (!entrada.hasNextLong()) {
                            error("LEER sin datos de entrada disponibles");
                        }
                        M[++s] = entrada.nextLong();
                        i++;
                        break;
                    case "IMPR":
                    case "IMLN":
                        System.out.println(M[s]);
                        s--;
                        i++;
                        break;
                    case "APVL": { // APVL m, n
                        int m = Integer.parseInt(in[1]);
                        int n = Integer.parseInt(in[2]);
                        M[s + 1] = M[D[m] + n];
                        s++;
                        i++;
                        break;
                    }
                    case "ALVL": { // ALVL m, n
                        int m = Integer.parseInt(in[1]);
                        int n = Integer.parseInt(in[2]);
                        M[D[m] + n] = M[s];
                        s--;
                        i++;
                        break;
                    }
                    case "RMEM":
                        s += Integer.parseInt(in[1]);
                        i++;
                        break;
                    case "LMEM":
                        s -= Integer.parseInt(in[1]);
                        i++;
                        break;
                    case "ENPR": { // ENPR k
                        int k = Integer.parseInt(in[1]);
                        s++;
                        M[s] = D[k];
                        D[k] = s + 1;
                        i++;
                        break;
                    }
                    case "LLPR": // LLPR etiqueta
                        s++;
                        M[s] = i + 1;
                        i = destino(etiquetas, in[1]);
                        break;
                    case "RTPR": { // RTPR k, n
                        int k = Integer.parseInt(in[1]);
                        int n = Integer.parseInt(in[2]);
                        D[k] = (int) M[s];
                        i = (int) M[s - 1];
                        s -= (n + 2);
                        break;
                    }
                    case "PARA":
                        if (s != -1) {
                            System.err.println("AVISO: la pila no quedo balanceada al llegar a PARA (s=" + s
                                    + ", deberia ser -1)");
                            System.exit(3);
                        }
                        return;
                    default:
                        error("instruccion desconocida: " + op);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                error("la pila se desbalanceo: la instruccion " + op + " (posicion " + i
                        + " del codigo) leyo fuera de la pila. Falta o sobra alguna instruccion antes"
                        + " (por ejemplo un MULT/SUMA/CONJ sin emitir, o un RMEM/LMEM/RTPR mal)");
            }
            if (s >= TAM_PILA - 1 || s < -1) {
                error("pila fuera de rango (s=" + s + ") en la instruccion " + op);
            }
        }
    }

    static int destino(Map<String, Integer> etiquetas, String etiqueta) {
        Integer d = etiquetas.get(etiqueta);
        if (d == null) {
            error("etiqueta no definida: " + etiqueta);
        }
        return d;
    }

    static void error(String mensaje) {
        System.err.println("ERROR MEPA: " + mensaje);
        System.exit(1);
    }
}