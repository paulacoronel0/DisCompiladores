import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class AnalizadorSintactico {

    private AnalizadorLexico analizadorLexico;
    private Token preanalisis;

    public AnalizadorSintactico(AnalizadorLexico analizadorLexico) {
        this.analizadorLexico = analizadorLexico;
        preanalisis = analizadorLexico.siguienteToken();
    }

    // =====================================================
    // MATCH
    // =====================================================

    private void match(TipoToken esperado) {
        if (preanalisis.getTipo() == esperado) {
            avanzar();
        } else {
            error("Se esperaba " + esperado + " y se encontró " + preanalisis.getTipo());
        }
    }

    private void avanzar() {
        preanalisis = analizadorLexico.siguienteToken();
    }

    private void error(String mensaje) {
        throw new RuntimeException("Error sintáctico en línea " + preanalisis.getLinea() + ": " + mensaje);
    }

    // =====================================================
    // PROGRAMA
    // =====================================================

    public void programa() {

        match(TipoToken.PROGRAM);

        identificador();

        match(TipoToken.PUNTO_Y_COMA);

        bloque();

        match(TipoToken.PUNTO);

        System.out.println("Programa sintácticamente correcto.");
    }

    // =====================================================
    // BLOQUE
    // =====================================================

    private void bloque() {

        parteDeclaracionesVariablesOpcional();

        comandoCompuesto();
    }

    // =====================================================
    // VARIABLES
    // =====================================================

    private void parteDeclaracionesVariablesOpcional() {

        if (preanalisis.getTipo() == TipoToken.VAR) {

            match(TipoToken.VAR);

            declaracionVariables();

            declaracionesVariablesOpcional();
        }
    }

    private void declaracionVariables() {

        listaIdentificadores();

        match(TipoToken.DOS_PUNTOS);

        tipo();
    }

    private void declaracionesVariablesOpcional() {

        while (preanalisis.getTipo() == TipoToken.PUNTO_Y_COMA) {

            match(TipoToken.PUNTO_Y_COMA);

            if (preanalisis.getTipo() == TipoToken.IDENTIFICADOR) {
                declaracionVariables();
            }
        }
    }

    private void listaIdentificadores() {

        identificador();

        while (preanalisis.getTipo() == TipoToken.COMA) {

            match(TipoToken.COMA);

            identificador();
        }
    }

    private void tipo() {

        switch (preanalisis.getTipo()) {

            case INTEGER:
                match(TipoToken.INTEGER);
                break;

            case BOOLEAN:
                match(TipoToken.BOOLEAN);
                break;

            default:
                error("Tipo inválido");
        }
    }

    // =====================================================
    // COMANDO COMPUESTO
    // =====================================================

    private void comandoCompuesto() {

        match(TipoToken.BEGIN);

        comando();

        while (preanalisis.getTipo() == TipoToken.PUNTO_Y_COMA) {

            match(TipoToken.PUNTO_Y_COMA);

            if (preanalisis.getTipo() != TipoToken.END) {
                comando();
            }
        }

        match(TipoToken.END);
    }

    // =====================================================
    // COMANDOS
    // =====================================================

    private void comando() {

        switch (preanalisis.getTipo()) {

            case IDENTIFICADOR:
                comandoIdentificador();
                break;

            case WRITE:
                comandoEscritura();
                break;

            case READ:
                comandoLectura();
                break;

            case BEGIN:
                comandoCompuesto();
                break;

            default:
                error("Comando inválido");
        }
    }

    private void comandoIdentificador() {

        identificador();

        if (preanalisis.getTipo() == TipoToken.ASIGNACION) {

            match(TipoToken.ASIGNACION);

            expresion();
        }
    }

    // =====================================================
    // WRITE / READ
    // =====================================================

    private void comandoEscritura() {

        match(TipoToken.WRITE);

        match(TipoToken.PARENTESIS_ABRE);

        expresion();

        match(TipoToken.PARENTESIS_CIERRA);
    }

    private void comandoLectura() {

        match(TipoToken.READ);

        match(TipoToken.PARENTESIS_ABRE);

        identificador();

        match(TipoToken.PARENTESIS_CIERRA);
    }

    // =====================================================
    // EXPRESIONES
    // =====================================================

    private void expresion() {

        termino();

        while (preanalisis.getTipo() == TipoToken.MAS
                || preanalisis.getTipo() == TipoToken.MENOS) {

            if (preanalisis.getTipo() == TipoToken.MAS) {
                match(TipoToken.MAS);
            } else {
                match(TipoToken.MENOS);
            }

            termino();
        }
    }

    private void termino() {

        factor();

        while (preanalisis.getTipo() == TipoToken.MULTIPLICACION) {

            match(TipoToken.MULTIPLICACION);

            factor();
        }
    }

    private void factor() {

        switch (preanalisis.getTipo()) {

            case IDENTIFICADOR:
                identificador();
                break;

            case NUMERO:
                numero();
                break;

            case PARENTESIS_ABRE:

                match(TipoToken.PARENTESIS_ABRE);

                expresion();

                match(TipoToken.PARENTESIS_CIERRA);
                break;

            default:
                error("Factor inválido");
        }
    }

    // =====================================================
    // TERMINALES
    // =====================================================

    private void identificador() {

        match(TipoToken.IDENTIFICADOR);
    }

    private void numero() {

        match(TipoToken.NUMERO);
    }

    public static void main(String[] args) {

        if (args.length != 1) {

            System.out.println("Uso: java AnalizadorSintactico archivo.pas");

            return;
        }

        try {

            String entrada = new String(Files.readAllBytes(Paths.get(args[0])));

            AnalizadorLexico lexico = new AnalizadorLexico(entrada);

            AnalizadorSintactico sintactico = new AnalizadorSintactico(lexico);

            sintactico.programa();

        } catch (Exception e) {

            System.out.println(e.getMessage());
        }
    }
}