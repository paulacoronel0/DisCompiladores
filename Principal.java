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

        try {
            String entrada = new String(Files.readAllBytes(Paths.get(args[0])));
            AnalizadorLexico analizador = new AnalizadorLexico(entrada);

            Token token;
            do {
                token = analizador.siguienteToken();
                System.out.println(token);//cambiarlo para que lo guarde en un archivo salida.txt
            } while (token.tipo != TipoToken.FIN_ARCHIVO);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
