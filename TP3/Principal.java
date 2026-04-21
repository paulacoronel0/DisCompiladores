import java.nio.file.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class Principal {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Uso: java Principal archivo.pas");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("salida.txt"))){
            String entrada = new String(Files.readAllBytes(Paths.get(args[0])));
            AnalizadorLexico analizador = new AnalizadorLexico(entrada);
            String salida = "salida.txt";
            Token token;
            do {
                token = analizador.siguienteToken();
                writer.write(token.toString());
                writer.newLine();
            } while (!token.tipo.equals("FIN_ARCHIVO"));

            

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
