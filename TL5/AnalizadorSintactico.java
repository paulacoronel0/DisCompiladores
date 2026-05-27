
import java.nio.file.Files;
import java.nio.file.Paths;

public class AnalizadorSintactico {

    // analizador léxico que entrega tokens
    private AnalizadorLexico analizadorLexico;
    private Token preanalisis;// token actual que se está mirando

    public AnalizadorSintactico(AnalizadorLexico analizadorLexico) {
        this.analizadorLexico = analizadorLexico;
        // carga primer token
        preanalisis = analizadorLexico.siguienteToken();
    }

    // verificar si el token actual coincide con el esperado
    private void match(TipoToken esperado) {
        if (preanalisis.getTipo() == esperado) {
            avanzar();
        } else {
            error("Se esperaba " + esperado + " y se encontró " + preanalisis.getTipo());
        }
    }

    // se pide el siguiente token al léxico
    private void avanzar() {
        preanalisis = analizadorLexico.siguienteToken();
    }

    // se lanza error sintáctico
    private void error(String mensaje) {
        throw new RuntimeException("Error sintáctico en línea " + preanalisis.getLinea() + ": " + mensaje);
    }

    // regla principal
    public void programa() {
        match(TipoToken.PROGRAM);// espera program
        identificador();// procesa identificador
        match(TipoToken.PUNTO_Y_COMA); // espera ;
        bloque();// procesa bloque
        match(TipoToken.PUNTO);// espera .
        System.out.println("Programa sintácticamente correcto.");
    }

    // procesar declaraciones y comandos
    private void bloque() {
        parteDeclaracionesVariablesOpcional(); // procesar variables
        parteDeclaracionesSubrutinasOpcional(); //procesar subrutinas
        comandoCompuesto();// procesar begin end
    }

    // procesar declaraciones opcionales
    private void parteDeclaracionesVariablesOpcional() {
        // si existe var
        if (preanalisis.getTipo() == TipoToken.VAR) {
            match(TipoToken.VAR);// consume var
            declaracionVariables();// procesar declaración
            declaracionesVariablesOpcional();// procesar más declaraciones
        }
    }

    // procesar declaración individual
    private void declaracionVariables() {
        listaIdentificadores();// procesar identificadores
        match(TipoToken.DOS_PUNTOS);// espera :
        tipo();// procesa tipo
    }

    // procesar más declaraciones
    private void declaracionesVariablesOpcional() {
        // mientras haya ;
        while (preanalisis.getTipo() == TipoToken.PUNTO_Y_COMA) {
            match(TipoToken.PUNTO_Y_COMA); // consumir ;
            // si viene otra declaración
            if (preanalisis.getTipo() == TipoToken.IDENTIFICADOR) {
                declaracionVariables();// procesar declaración
            } else {
                break;
            }
        }
    }

    // procesar lista de identificadores
    private void listaIdentificadores() {
        identificador();// procesa primer identificador
        // mientras haya ,
        while (preanalisis.getTipo() == TipoToken.COMA) {
            match(TipoToken.COMA); // consume ,
            identificador();// procesa identificador
        }
    }

    // procesar tipo de dato
    private void tipo() {
        switch (preanalisis.getTipo()) {
            case INTEGER:// consumir integer
                match(TipoToken.INTEGER);
                break;
            case BOOLEAN:// consumir boolean
                match(TipoToken.BOOLEAN);
                break;
            default:
                error("Tipo inválido");
        }
    }

    // procesar bloque begin end
    private void comandoCompuesto() {
        match(TipoToken.BEGIN); // consumir begin
        comando();// procesar comando
        // mientras haya ;
        while (preanalisis.getTipo() == TipoToken.PUNTO_Y_COMA) {
            match(TipoToken.PUNTO_Y_COMA);// consume ;
            // si no terminó
            if (preanalisis.getTipo() != TipoToken.END) {
                comando();// procesa comando
            }
        }
        match(TipoToken.END);// consume end
    }

    // procesar comandos
    private void comando() {
        switch (preanalisis.getTipo()) {
            case IDENTIFICADOR:// procesar asignación
                comandoIdentificador();
                break;
            case WRITE:// procesar escritura
                comandoEscritura();
                break;
            case READ:// procesar lectura
                comandoLectura();
                break;
            case BEGIN:// procesar bloque interno
                comandoCompuesto();
                break;
            case WHILE:
                comandoRepetitivo();
                break;
            case IF:
                comandoCondicional();
                break;
            default:
                error("Comando inválido");
        }
    }

    private void comandoRepetitivo() {
        match(TipoToken.WHILE);
        expresion();
        match(TipoToken.DO);
        comando();
    }

    private void comandoCondicional() {
        match(TipoToken.IF);
        expresion();
        match(TipoToken.THEN);
        comando();
        if (preanalisis.getTipo() == TipoToken.ELSE) {
            match(TipoToken.ELSE);
            comando();
        }
    }

    // procesar asignaciones
    private void comandoIdentificador() {
        identificador();
        // asignación
        if (preanalisis.getTipo() == TipoToken.ASIGNACION) {
            match(TipoToken.ASIGNACION);
            expresion();
        } // llamada a procedimiento
        else if (preanalisis.getTipo() == TipoToken.PARENTESIS_ABRE) {
            match(TipoToken.PARENTESIS_ABRE);
            expresion();
            while (preanalisis.getTipo() == TipoToken.COMA) {
                match(TipoToken.COMA);
                expresion();
            }
            match(TipoToken.PARENTESIS_CIERRA);
        }
    }

    // procesar write
    private void comandoEscritura() {
        match(TipoToken.WRITE);// consumir write
        match(TipoToken.PARENTESIS_ABRE);// consumir (
        expresion();// procesar expresión
        match(TipoToken.PARENTESIS_CIERRA);// consumir )
    }

    // procesar read
    private void comandoLectura() {
        match(TipoToken.READ);// consumir read
        match(TipoToken.PARENTESIS_ABRE);// consumir (
        identificador();// procesar identificador
        match(TipoToken.PARENTESIS_CIERRA); // consumir )
    }

    // procesae expresiones
    private void expresion() {

        expresionSimple();

        if (preanalisis.getTipo() == TipoToken.IGUAL
                || preanalisis.getTipo() == TipoToken.DISTINTO
                || preanalisis.getTipo() == TipoToken.MENOR
                || preanalisis.getTipo() == TipoToken.MENOR_IGUAL
                || preanalisis.getTipo() == TipoToken.MAYOR
                || preanalisis.getTipo() == TipoToken.MAYOR_IGUAL) {

            relacion();

            expresionSimple();
        }
    }

    private void expresionSimple() {
        termino();
        while (preanalisis.getTipo() == TipoToken.MAS || preanalisis.getTipo() == TipoToken.MENOS
                || preanalisis.getTipo() == TipoToken.OR) {
            if (preanalisis.getTipo() == TipoToken.MAS) {
                match(TipoToken.MAS);
            } else if (preanalisis.getTipo() == TipoToken.MENOS) {
                match(TipoToken.MENOS);
            } else {
                match(TipoToken.OR);
            }
            termino();
        }
    }

    private void relacion() {

        switch (preanalisis.getTipo()) {

            case IGUAL:
                match(TipoToken.IGUAL);
                break;

            case DISTINTO:
                match(TipoToken.DISTINTO);
                break;

            case MENOR:
                match(TipoToken.MENOR);
                break;

            case MENOR_IGUAL:
                match(TipoToken.MENOR_IGUAL);
                break;

            case MAYOR:
                match(TipoToken.MAYOR);
                break;

            case MAYOR_IGUAL:
                match(TipoToken.MAYOR_IGUAL);
                break;

            default:
                error("Operador relacional inválido");
        }
    }

    // procesar términos
    private void termino() {
        factor();// procesar factor
        while (preanalisis.getTipo() == TipoToken.MULTIPLICACION
                || preanalisis.getTipo() == TipoToken.DIV
                || preanalisis.getTipo() == TipoToken.AND) {
            if (preanalisis.getTipo() == TipoToken.MULTIPLICACION) {
                match(TipoToken.MULTIPLICACION);
            } else if (preanalisis.getTipo() == TipoToken.DIV) {
                match(TipoToken.DIV);
            } else {
                match(TipoToken.AND);
            }
            factor();
        }
    }

    // procesar factores
    private void factor() {
        switch (preanalisis.getTipo()) {
            case IDENTIFICADOR:
                identificador();// consumir identificador
                break;
            case NUMERO:// consumir número
                numero();
                break;
            case PARENTESIS_ABRE:// consumir (
                match(TipoToken.PARENTESIS_ABRE);
                expresion();// procesar expresión
                match(TipoToken.PARENTESIS_CIERRA);// consumir )
                break;
            case NOT:
                match(TipoToken.NOT);
                factor();
                break;
            default:
                error("Factor inválido");
        }
    }

    // procesar identificador
    private void identificador() {
        match(TipoToken.IDENTIFICADOR);
    }

    // procesar número
    private void numero() {
        match(TipoToken.NUMERO);
    }

    private void parteDeclaracionesSubrutinasOpcional() {
        while (preanalisis.getTipo() == TipoToken.PROCEDURE || preanalisis.getTipo() == TipoToken.FUNCTION) {
            if (preanalisis.getTipo() == TipoToken.PROCEDURE) {
                declaracionProcedimiento();
            } else {
                declaracionFuncion();
            }
            match(TipoToken.PUNTO_Y_COMA);
        }
    }

    private void declaracionProcedimiento() {
        match(TipoToken.PROCEDURE);
        identificador();
        parametrosFormalesOpcional();
        match(TipoToken.PUNTO_Y_COMA);
        bloque();
    }

    private void declaracionFuncion() {
        match(TipoToken.FUNCTION);
        identificador();
        parametrosFormalesOpcional();
        match(TipoToken.DOS_PUNTOS);
        tipo();
        match(TipoToken.PUNTO_Y_COMA);
        bloque();
    }

    private void parametrosFormalesOpcional() {
        if (preanalisis.getTipo() == TipoToken.PARENTESIS_ABRE) {
            parametrosFormales();
        }
    }

    private void parametrosFormales() {
        match(TipoToken.PARENTESIS_ABRE);
        listaIdentificadores();
        match(TipoToken.DOS_PUNTOS);
        tipo();
        while (preanalisis.getTipo() == TipoToken.PUNTO_Y_COMA) {
            match(TipoToken.PUNTO_Y_COMA);
            listaIdentificadores();
            match(TipoToken.DOS_PUNTOS);
            tipo();
        }
        match(TipoToken.PARENTESIS_CIERRA);
    }

    public static void main(String[] args) {
        // verificar argumentos
        if (args.length != 1) {
            System.out.println("Uso: java AnalizadorSintactico archivo.pas");
            return;
        }

        try {
            // leer archivo completo
            String entrada = new String(Files.readAllBytes(Paths.get(args[0])));
            // crear léxico
            AnalizadorLexico lexico = new AnalizadorLexico(entrada);
            // crear sintáctico
            AnalizadorSintactico sintactico = new AnalizadorSintactico(lexico);
            // iniciar análisis
            sintactico.programa();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
