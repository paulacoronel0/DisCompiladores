import java.nio.file.*;

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
                System.out.println(token);
            } while (token.tipo != TipoToken.FIN_ARCHIVO);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
