import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// ============================================================================
// Analizador sintáctico + analizador semántico para el subconjunto de
// mini-pascal.
//
// Se agrega a la versión original el uso de la Tabla de Símbolos
// (TablaSimbolos / Simbolo / SimboloSubprograma / CategoriaSimbolo /
// TipoDato) para:
//
//   1) Declarar cada nombre (variable, parámetro, procedimiento, función)
//      en el ámbito correspondiente, chequeando que no esté repetido en
//      ESE mismo ámbito (unicidad).
//   2) Crear un ámbito (TablaSimbolos) nuevo por cada procedimiento/función,
//      encadenado con el ámbito en el que fue declarado, respetando el
//      anidamiento léxico de Pascal (alcance estático).
//   3) Verificar, en cada uso de un identificador, que haya sido declarado
//      (buscando en el ámbito actual y, si no está, en los ámbitos
//      contenedores).
//   4) Chequear tipos: en asignaciones, en operadores aritméticos/lógicos/
//      relacionales, y en llamadas a procedimientos y funciones (cantidad
//      y tipo de los argumentos contra los parámetros formales).
//
// Los errores semánticos NO cortan el análisis (a diferencia de los
// errores sintácticos): se acumulan en una lista y se informan al final,
// para poder detectar varios problemas en una sola pasada.
// ============================================================================
public class AnalizadorSintactico {

    // analizador léxico que entrega tokens
    private AnalizadorLexico analizadorLexico;
    private Token preanalisis; // token actual que se está mirando

    // --- Tabla de símbolos --------------------------------------------------
    private TablaSimbolos tablaGlobal; // ámbito global (nivel 0)
    private TablaSimbolos ambitoActual; // ámbito que se está completando/consultando
    private List<String> erroresSemanticos = new ArrayList<>();

    public AnalizadorSintactico(AnalizadorLexico analizadorLexico) {
        this.analizadorLexico = analizadorLexico;
        // carga primer token
        preanalisis = analizadorLexico.siguienteToken();
        // se arranca en el ámbito global; no tiene padre
        this.tablaGlobal = new TablaSimbolos(null);
        this.ambitoActual = tablaGlobal;
    }

    // verificar si el token actual coincide con el esperado
    private void match(TipoToken esperado) {
        if (preanalisis.getTipo() == esperado) {
            avanzar();
        } else {
            error("Se esperaba " + esperado + " y se encontro " + preanalisis.getTipo());
        }
    }

    // se pide el siguiente token al léxico
    private void avanzar() {
        preanalisis = analizadorLexico.siguienteToken();
    }

    // se lanza error sintáctico (corta el análisis)
    private void error(String mensaje) {
        throw new RuntimeException("Error sintactico en linea " + preanalisis.getLinea() + ": " + mensaje);
    }

    // registra un error semantico y continúa el análisis
    private void errorSemantico(String mensaje, int linea) {
        erroresSemanticos.add("Error semantico en linea " + linea + ": " + mensaje);
    }

    // regla principal
    public void programa() {
        match(TipoToken.PROGRAM); // espera program
        Token nombrePrograma = identificador(); // procesa identificador
        // se registra el nombre del programa en el ámbito global (informativo)
        Simbolo simboloPrograma = new Simbolo(nombrePrograma.getLexema(), CategoriaSimbolo.PROGRAMA,
                null, nombrePrograma.getLinea());
        ambitoActual.insertar(simboloPrograma);

        match(TipoToken.PUNTO_Y_COMA); // espera ;
        bloque(); // procesa bloque
        match(TipoToken.PUNTO); // espera .

        if (erroresSemanticos.isEmpty()) {
            System.out.println("Programa sintactica y semanticamente correcto.");
        } else {
            System.out.println("Programa sintacticamente correcto, pero con errores semanticos:");
            for (String mensaje : erroresSemanticos) {
                System.out.println("  " + mensaje);
            }
        }
    }

    // procesar declaraciones y comandos
    private void bloque() {
        parteDeclaracionesVariablesOpcional(); // procesar variables
        parteDeclaracionesSubrutinasOpcional(); // procesar subrutinas
        comandoCompuesto(); // procesar begin end
    }

    // procesar declaraciones opcionales
    private void parteDeclaracionesVariablesOpcional() {
        // si existe var
        if (preanalisis.getTipo() == TipoToken.VAR) {
            match(TipoToken.VAR); // consume var
            declaracionVariables(); // procesar declaración
            declaracionesVariablesOpcional(); // procesar más declaraciones
        }
    }

    // procesar declaración individual: la agrega a la tabla de símbolos del
    // ámbito actual, informando error semántico si el nombre ya existía
    // en ese mismo ámbito.
    private void declaracionVariables() {
        List<Token> identificadores = listaIdentificadores(); // procesa identificadores
        match(TipoToken.DOS_PUNTOS); // espera :
        TipoToken tipoToken = tipo(); // procesa tipo
        TipoDato tipoDato = TipoDato.desdeTipoToken(tipoToken); // RETORNA SI ES INTEGER, BOOLEAN O ERROR

        for (Token idTok : identificadores) {
            Simbolo simbolo = new Simbolo(idTok.getLexema(), CategoriaSimbolo.VARIABLE, tipoDato, idTok.getLinea());
            if (!ambitoActual.insertar(simbolo)) {
                errorSemantico("El identificador '" + idTok.getLexema()
                        + "' ya habia sido declarado en este ambito", idTok.getLinea());
            }
        }
    }

    // procesar más declaraciones
    private void declaracionesVariablesOpcional() {
        // mientras haya ;
        while (preanalisis.getTipo() == TipoToken.PUNTO_Y_COMA) {
            match(TipoToken.PUNTO_Y_COMA); // consumir ;
            // si viene otra declaración
            if (preanalisis.getTipo() == TipoToken.IDENTIFICADOR) {
                declaracionVariables(); // procesar declaración
            } else {
                break;
            }
        }
    }

    // procesar lista de identificadores. Devuelve los tokens (no solo se
    // consumen) porque quien llama necesita el lexema y la línea de cada
    // uno para crear los símbolos correspondientes.
    private List<Token> listaIdentificadores() {
        List<Token> identificadores = new ArrayList<>();
        identificadores.add(identificador()); // procesa primer identificador
        // mientras haya ,
        while (preanalisis.getTipo() == TipoToken.COMA) {
            match(TipoToken.COMA); // consume ,
            identificadores.add(identificador()); // procesa identificador
        }
        return identificadores;
    }

    // procesar tipo de dato. Devuelve el TipoToken reconocido para que el
    // llamador pueda construir el TipoDato semántico correspondiente.
    private TipoToken tipo() {
        TipoToken resultado = null;
        switch (preanalisis.getTipo()) {
            case INTEGER: // consumir integer
                match(TipoToken.INTEGER);
                resultado = TipoToken.INTEGER;
                break;
            case BOOLEAN: // consumir boolean
                match(TipoToken.BOOLEAN);
                resultado = TipoToken.BOOLEAN;
                break;
            default:
                error("Tipo invalido");
        }
        return resultado;
    }

    // procesar bloque begin end
    private void comandoCompuesto() {
        match(TipoToken.BEGIN); // consumir begin
        comando(); // procesar comando
        // mientras haya ;
        while (preanalisis.getTipo() == TipoToken.PUNTO_Y_COMA) {
            match(TipoToken.PUNTO_Y_COMA); // consume ;
            // si no terminó
            if (preanalisis.getTipo() != TipoToken.END) {
                comando(); // procesa comando
            }
        }
        match(TipoToken.END); // consume end
    }

    // procesar comandos
    private void comando() {
        switch (preanalisis.getTipo()) {
            case IDENTIFICADOR: // procesar asignación o llamada
                comandoIdentificador();
                break;
            case WRITE: // procesar escritura
                comandoEscritura();
                break;
            case READ: // procesar lectura
                comandoLectura();
                break;
            case BEGIN: // procesar bloque interno
                comandoCompuesto();
                break;
            case WHILE:
                comandoRepetitivo();
                break;
            case IF:
                comandoCondicional();
                break;
            default:
                error("Comando invalido");
        }
    }

    private void comandoRepetitivo() {
        match(TipoToken.WHILE);
        int linea = preanalisis.getLinea();
        TipoDato tipoCondicion = expresion();
        chequearTipoCondicion(tipoCondicion, linea);
        match(TipoToken.DO);
        comando();
    }

    private void comandoCondicional() {
        match(TipoToken.IF);
        int linea = preanalisis.getLinea();
        TipoDato tipoCondicion = expresion();
        chequearTipoCondicion(tipoCondicion, linea);
        match(TipoToken.THEN);
        comando();
        if (preanalisis.getTipo() == TipoToken.ELSE) {
            match(TipoToken.ELSE);
            comando();
        }
    }

    // la condición de un if/while debe ser de tipo BOOLEAN (semantico)
    private void chequearTipoCondicion(TipoDato tipoCondicion, int linea) {
        if (tipoCondicion != null && tipoCondicion != TipoDato.BOOLEAN) {
            errorSemantico("La condicion debe ser de tipo BOOLEAN y es " + tipoCondicion, linea);
        }
    }

    // procesar asignaciones y llamadas a procedimiento (con y sin paréntesis)
    private void comandoIdentificador() {
        Token idTok = identificador();
        Simbolo simbolo = ambitoActual.buscar(idTok.getLexema());
        if (simbolo == null) {
            errorSemantico("El identificador '" + idTok.getLexema() + "' no fue declarado", idTok.getLinea());
        }

        if (preanalisis.getTipo() == TipoToken.ASIGNACION) {
            // asignación
            match(TipoToken.ASIGNACION);
            TipoDato tipoExpresion = expresion();
            validarAsignacion(simbolo, idTok, tipoExpresion);

        } else if (preanalisis.getTipo() == TipoToken.PARENTESIS_ABRE) {
            // llamada a procedimiento con paréntesis (con o sin argumentos)
            match(TipoToken.PARENTESIS_ABRE);
            List<TipoDato> argumentos = new ArrayList<>();
            if (preanalisis.getTipo() != TipoToken.PARENTESIS_CIERRA) {
                argumentos.add(expresion());
                while (preanalisis.getTipo() == TipoToken.COMA) {
                    match(TipoToken.COMA);
                    argumentos.add(expresion());
                }
            }
            match(TipoToken.PARENTESIS_CIERRA);
            validarLlamadaProcedimiento(simbolo, idTok, argumentos);

        } else {
            // identificador solo: llamada a procedimiento sin parámetros
            // (por ejemplo "limpiar;")
            validarLlamadaProcedimiento(simbolo, idTok, new ArrayList<TipoDato>());
        }
    }

    // valida que 'simbolo' pueda recibir una asignación y que el tipo de la
    // expresión coincida con el tipo declarado
    private void validarAsignacion(Simbolo simbolo, Token idTok, TipoDato tipoExpresion) {
        if (simbolo == null) {
            return; // ya se informó "no declarado"
        }
        if (simbolo.getCategoria() != CategoriaSimbolo.VARIABLE
                && simbolo.getCategoria() != CategoriaSimbolo.PARAMETRO) {
            errorSemantico("No se le puede asignar un valor a '" + idTok.getLexema() + "'", idTok.getLinea());
        } else if (tipoExpresion != null && simbolo.getTipo() != tipoExpresion) {
            errorSemantico("Tipos incompatibles en la asignacion a '" + idTok.getLexema()
                    + "' (se esperaba " + simbolo.getTipo() + " y se recibio " + tipoExpresion + ")",
                    idTok.getLinea());
        }
    }

    // valida que 'simbolo' sea un procedimiento y que los argumentos
    // coincidan en cantidad y tipo con los parámetros formales
    private void validarLlamadaProcedimiento(Simbolo simbolo, Token idTok, List<TipoDato> argumentos) {
        if (simbolo == null) {
            return; // ya se informó "no declarado"
        }
        if (simbolo.getCategoria() != CategoriaSimbolo.PROCEDIMIENTO) {
            errorSemantico("'" + idTok.getLexema() + "' no es un procedimiento", idTok.getLinea());
            return;
        }
        chequearArgumentos((SimboloSubprograma) simbolo, argumentos, idTok.getLinea());
    }

    // compara la lista de argumentos reales (tipos ya resueltos) contra los
    // parámetros formales del subprograma: cantidad y tipo, en orden.
    private void chequearArgumentos(SimboloSubprograma subprograma, List<TipoDato> argumentos, int lineaLlamada) {
        List<Simbolo> parametros = subprograma.getParametros();
        if (argumentos.size() != parametros.size()) {
            errorSemantico("'" + subprograma.getNombre() + "' espera " + parametros.size()
                    + " argumento(s) y se recibieron " + argumentos.size(), lineaLlamada);
            return;
        }
        for (int i = 0; i < parametros.size(); i++) {
            TipoDato tipoEsperado = parametros.get(i).getTipo();
            TipoDato tipoRecibido = argumentos.get(i);
            if (tipoRecibido != null && tipoEsperado != tipoRecibido) {
                errorSemantico("El argumento " + (i + 1) + " de '" + subprograma.getNombre()
                        + "' debe ser " + tipoEsperado + " y se recibio " + tipoRecibido, lineaLlamada);
            }
        }
    }

    // procesar write
    private void comandoEscritura() {
        match(TipoToken.WRITE); // consumir write
        match(TipoToken.PARENTESIS_ABRE); // consumir (
        expresion(); // procesar expresión (se admite INTEGER o BOOLEAN)
        match(TipoToken.PARENTESIS_CIERRA); // consumir )
    }

    // procesar read: solo tiene sentido leer sobre variables o parámetros
    private void comandoLectura() {
        match(TipoToken.READ); // consumir read
        match(TipoToken.PARENTESIS_ABRE); // consumir (
        Token idTok = identificador(); // procesar identificador
        Simbolo simbolo = ambitoActual.buscar(idTok.getLexema());
        if (simbolo == null) {
            errorSemantico("El identificador '" + idTok.getLexema() + "' no fue declarado", idTok.getLinea());
        } else if (simbolo.getCategoria() != CategoriaSimbolo.VARIABLE
                && simbolo.getCategoria() != CategoriaSimbolo.PARAMETRO) {
            errorSemantico("Solo se puede leer sobre variables o parametros, no sobre '"
                    + idTok.getLexema() + "'", idTok.getLinea());
        }
        match(TipoToken.PARENTESIS_CIERRA); // consumir )
    }

    // procesa expresiones y devuelve el TipoDato resultante (INTEGER o
    // BOOLEAN), o null si no se pudo determinar (por ejemplo, porque
    // intervino un identificador no declarado).
    private TipoDato expresion() {

        TipoDato tipoIzquierdo = expresionSimple();

        if (esOperadorRelacional(preanalisis.getTipo())) {
            int lineaOperador = preanalisis.getLinea();
            relacion();
            TipoDato tipoDerecho = expresionSimple();

            if (tipoIzquierdo != null && tipoDerecho != null && tipoIzquierdo != tipoDerecho) {
                errorSemantico("Operandos de tipos incompatibles en la comparacion ("
                        + tipoIzquierdo + " y " + tipoDerecho + ")", lineaOperador);
            }
            return TipoDato.BOOLEAN; // toda comparación da un resultado booleano
        }

        return tipoIzquierdo;
    }

    private boolean esOperadorRelacional(TipoToken t) {
        return t == TipoToken.IGUAL || t == TipoToken.DISTINTO
                || t == TipoToken.MENOR || t == TipoToken.MENOR_IGUAL
                || t == TipoToken.MAYOR || t == TipoToken.MAYOR_IGUAL;
    }

    private TipoDato expresionSimple() {
        TipoDato tipoAcumulado = termino();
        while (preanalisis.getTipo() == TipoToken.MAS || preanalisis.getTipo() == TipoToken.MENOS
                || preanalisis.getTipo() == TipoToken.OR) {
            TipoToken operador = preanalisis.getTipo();
            int lineaOperador = preanalisis.getLinea();
            if (operador == TipoToken.MAS) {
                match(TipoToken.MAS);
            } else if (operador == TipoToken.MENOS) {
                match(TipoToken.MENOS);
            } else {
                match(TipoToken.OR);
            }
            TipoDato tipoDerecho = termino();
            tipoAcumulado = chequearTipoOperacion(operador, tipoAcumulado, tipoDerecho, lineaOperador);
        }
        return tipoAcumulado;
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
                error("Operador relacional invalido");
        }
    }

    // procesar términos
    private TipoDato termino() {
        TipoDato tipoAcumulado = factor(); // procesar factor
        while (preanalisis.getTipo() == TipoToken.MULTIPLICACION
                || preanalisis.getTipo() == TipoToken.DIV
                || preanalisis.getTipo() == TipoToken.AND) {
            TipoToken operador = preanalisis.getTipo();
            int lineaOperador = preanalisis.getLinea();
            if (operador == TipoToken.MULTIPLICACION) {
                match(TipoToken.MULTIPLICACION);
            } else if (operador == TipoToken.DIV) {
                match(TipoToken.DIV);
            } else {
                match(TipoToken.AND);
            }
            TipoDato tipoDerecho = factor();
            tipoAcumulado = chequearTipoOperacion(operador, tipoAcumulado, tipoDerecho, lineaOperador);
        }
        return tipoAcumulado;
    }

    // chequea que ambos operandos sean del tipo esperado por el operador
    // (INTEGER para +,-,*,div ; BOOLEAN para and,or) y devuelve el tipo
    // resultante de la operación.
    private TipoDato chequearTipoOperacion(TipoToken operador, TipoDato izquierdo, TipoDato derecho, int linea) {
        boolean esOperadorLogico = (operador == TipoToken.AND || operador == TipoToken.OR);
        TipoDato tipoEsperado = esOperadorLogico ? TipoDato.BOOLEAN : TipoDato.INTEGER;

        if (izquierdo != null && izquierdo != tipoEsperado) {
            errorSemantico("El operando izquierdo de '" + operador + "' debe ser " + tipoEsperado
                    + " y es " + izquierdo, linea);
        }
        if (derecho != null && derecho != tipoEsperado) {
            errorSemantico("El operando derecho de '" + operador + "' debe ser " + tipoEsperado
                    + " y es " + derecho, linea);
        }
        return tipoEsperado;
    }

    // procesar factores. Devuelve el TipoDato del factor (tipo de la
    // variable/parámetro, tipo de retorno de la función, INTEGER para
    // números, BOOLEAN para "not ...", o el tipo de la subexpresión entre
    // paréntesis).
    private TipoDato factor() {
        TipoDato resultado = null;
        switch (preanalisis.getTipo()) {
            case IDENTIFICADOR:
                Token idTok = identificador(); // consumir identificador
                Simbolo simbolo = ambitoActual.buscar(idTok.getLexema());
                if (simbolo == null) {
                    errorSemantico("El identificador '" + idTok.getLexema() + "' no fue declarado",
                            idTok.getLinea());
                }

                // SI VIENE UN PARÉNTESIS, ES UNA LLAMADA A FUNCIÓN
                if (preanalisis.getTipo() == TipoToken.PARENTESIS_ABRE) {
                    match(TipoToken.PARENTESIS_ABRE);

                    List<TipoDato> argumentos = new ArrayList<>();
                    // si el siguiente token no es el de cierre, significa que hay parámetros
                    if (preanalisis.getTipo() != TipoToken.PARENTESIS_CIERRA) {
                        argumentos.add(expresion());
                        while (preanalisis.getTipo() == TipoToken.COMA) {
                            match(TipoToken.COMA);
                            argumentos.add(expresion());
                        }
                    }
                    match(TipoToken.PARENTESIS_CIERRA);

                    if (simbolo != null) {
                        if (simbolo.getCategoria() != CategoriaSimbolo.FUNCION) {
                            errorSemantico("'" + idTok.getLexema() + "' no es una funcion", idTok.getLinea());
                        } else {
                            SimboloSubprograma funcion = (SimboloSubprograma) simbolo;
                            chequearArgumentos(funcion, argumentos, idTok.getLinea());
                            resultado = funcion.getTipoRetorno();
                        }
                    }
                } else {
                    // uso de variable/parámetro (o nombre de función usado como valor,
                    // convención típica de Pascal para el retorno dentro del propio cuerpo)
                    if (simbolo != null) {
                        if (simbolo.getCategoria() == CategoriaSimbolo.PROCEDIMIENTO
                                || simbolo.getCategoria() == CategoriaSimbolo.PROGRAMA) {
                            errorSemantico("'" + idTok.getLexema() + "' no puede usarse como valor",
                                    idTok.getLinea());
                        } else {
                            resultado = simbolo.getTipo();
                        }
                    }
                }
                break;
            case NUMERO: // consumir número
                numero();
                resultado = TipoDato.INTEGER;
                break;
            case PARENTESIS_ABRE: // consumir (
                match(TipoToken.PARENTESIS_ABRE);
                resultado = expresion(); // procesar expresión
                match(TipoToken.PARENTESIS_CIERRA); // consumir )
                break;
            case NOT:
                int lineaNot = preanalisis.getLinea();
                match(TipoToken.NOT);
                TipoDato tipoOperando = factor();
                if (tipoOperando != null && tipoOperando != TipoDato.BOOLEAN) {
                    errorSemantico("El operador 'not' requiere un operando BOOLEAN y es " + tipoOperando,
                            lineaNot);
                }
                resultado = TipoDato.BOOLEAN;
                break;
            default:
                error("Factor invalido");
        }
        return resultado;
    }

    // procesar identificador. Devuelve el token consumido (lexema + línea)
    // para que quien llama pueda crear/buscar el símbolo correspondiente.
    private Token identificador() {
        Token token = preanalisis;
        match(TipoToken.IDENTIFICADOR);
        return token;
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

    // declara el procedimiento en el ámbito actual (lo que permite
    // recursión, ya que el nombre queda visible antes de procesar el
    // cuerpo) y procesa parámetros y bloque en su propio ámbito.
    private void declaracionProcedimiento() {
        match(TipoToken.PROCEDURE);
        Token nombreTok = identificador();
        List<GrupoParametros> gruposParametros = parametrosFormalesOpcional();
        match(TipoToken.PUNTO_Y_COMA);

        SimboloSubprograma simbolo = new SimboloSubprograma(nombreTok.getLexema(), CategoriaSimbolo.PROCEDIMIENTO,
                nombreTok.getLinea(), null, ambitoActual);
        registrarParametros(simbolo, gruposParametros);

        if (!ambitoActual.insertar(simbolo)) {
            errorSemantico("El identificador '" + nombreTok.getLexema()
                    + "' ya habia sido declarado en este ambito", nombreTok.getLinea());
        }

        TablaSimbolos ambitoAnterior = ambitoActual;
        ambitoActual = simbolo.getTablaLocal(); // entra al ámbito del procedimiento
        bloque();
        ambitoActual = ambitoAnterior; // vuelve al ámbito contenedor
    }

    // análogo a declaracionProcedimiento, pero además define el tipo de
    // retorno de la función.
    private void declaracionFuncion() {
        match(TipoToken.FUNCTION);
        Token nombreTok = identificador();
        List<GrupoParametros> gruposParametros = parametrosFormalesOpcional();
        match(TipoToken.DOS_PUNTOS);
        TipoToken tipoRetornoToken = tipo();
        TipoDato tipoRetorno = TipoDato.desdeTipoToken(tipoRetornoToken);
        match(TipoToken.PUNTO_Y_COMA);

        SimboloSubprograma simbolo = new SimboloSubprograma(nombreTok.getLexema(), CategoriaSimbolo.FUNCION,
                nombreTok.getLinea(), tipoRetorno, ambitoActual);
        registrarParametros(simbolo, gruposParametros);

        if (!ambitoActual.insertar(simbolo)) {
            errorSemantico("El identificador '" + nombreTok.getLexema()
                    + "' ya habia sido declarado en este ambito", nombreTok.getLinea());
        }

        TablaSimbolos ambitoAnterior = ambitoActual;
        ambitoActual = simbolo.getTablaLocal(); // entra al ámbito de la función
        bloque();
        ambitoActual = ambitoAnterior; // vuelve al ámbito contenedor
    }

    // agrega, como parámetros del subprograma, cada identificador de cada
    // grupo (ej. "a,b:integer; c:boolean" son dos grupos), chequeando
    // duplicados dentro de la propia lista de parámetros.
    private void registrarParametros(SimboloSubprograma subprograma, List<GrupoParametros> grupos) {
        for (GrupoParametros grupo : grupos) {
            for (Token idTok : grupo.identificadores) {
                if (subprograma.getTablaLocal().buscarLocal(idTok.getLexema()) != null) {
                    errorSemantico("El parametro '" + idTok.getLexema() + "' ya habia sido declarado",
                            idTok.getLinea());
                } else {
                    Simbolo parametro = new Simbolo(idTok.getLexema(), CategoriaSimbolo.PARAMETRO,
                            grupo.tipo, idTok.getLinea());
                    subprograma.agregarParametro(parametro);
                }
            }
        }
    }

    // devuelve la lista de grupos de parámetros formales (vacía si no hay
    // paréntesis). No modifica todavía la tabla de símbolos: eso lo hace
    // registrarParametros(), una vez que ya existe el SimboloSubprograma.
    private List<GrupoParametros> parametrosFormalesOpcional() {
        if (preanalisis.getTipo() == TipoToken.PARENTESIS_ABRE) {
            return parametrosFormales();
        }
        return new ArrayList<>();
    }

    private List<GrupoParametros> parametrosFormales() {
        List<GrupoParametros> grupos = new ArrayList<>();
        match(TipoToken.PARENTESIS_ABRE);
        // Verificamos si la lista de parámetros NO está vacía
        if (preanalisis.getTipo() != TipoToken.PARENTESIS_CIERRA) {
            grupos.add(grupoDeParametros());

            while (preanalisis.getTipo() == TipoToken.PUNTO_Y_COMA) {
                match(TipoToken.PUNTO_Y_COMA);
                grupos.add(grupoDeParametros());
            }
        }
        match(TipoToken.PARENTESIS_CIERRA);
        return grupos;
    }

    // procesa "id1, id2, ... : tipo" y lo devuelve como un GrupoParametros
    private GrupoParametros grupoDeParametros() {
        List<Token> identificadores = listaIdentificadores();
        match(TipoToken.DOS_PUNTOS);
        TipoToken tipoToken = tipo();

        GrupoParametros grupo = new GrupoParametros();
        grupo.identificadores = identificadores;
        grupo.tipo = TipoDato.desdeTipoToken(tipoToken);
        return grupo;
    }

    // estructura auxiliar interna: un grupo de identificadores que
    // comparten el mismo tipo dentro de una lista de parámetros formales.
    private static class GrupoParametros {
        List<Token> identificadores;
        TipoDato tipo;
    }

    // --- utilidades de depuración -------------------------------------------

    // permite inspeccionar la tabla de símbolos global una vez finalizado
    // el análisis (por ejemplo, desde tests o desde main)
    public TablaSimbolos getTablaGlobal() {
        return tablaGlobal;
    }

    public List<String> getErroresSemanticos() {
        return erroresSemanticos;
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
            // sintactico.getTablaGlobal().imprimir();
            sintactico.getTablaGlobal().imprimirRecursivo();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
