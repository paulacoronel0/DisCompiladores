program testExhaustivo;

var
  a, b, c, d, counter, limit, result, f2, f3, isEven, isValid : integer;
  { Nota: Mini-Pascal trata los tipos de forma sintáctica básica, }
  { asumimos que todas las variables están declaradas arriba. }

{ ==================================================================== }
{ SECCIÓN 1: DECLARACIONES - CASOS EXTREMOS Y TRAMPAS DE LOOKAHEAD    }
{ ==================================================================== }

procedure p1 (a, b: integer);
var 
  p1a, p1b: integer;
  
  { CASO COMPLEJO ACTIVO: Función anidada vacía y correcta }
  function f1 (a, b: integer): integer;
  var p1a : integer;
  begin
     a := 3 div 5
  end;

begin
   a := 3 div 5
end;

{ --- ERRORES EN SUBRUTINAS (Descomentar para probar) --- }
{ TRAMPA 1: Falta el tipo de retorno en una función }
{ function fErrorTipo(): ; begin fErrorTipo := 1 end; }

{ TRAMPA 2: Olvidar el punto y coma final de la declaración del procedimiento }
{ procedure pErrorSemicolon() begin a := 1 end; }

{ TRAMPA 3: Lista de parámetros con una coma colgada o mal estructurada }
{ procedure pErrorParams(x, : integer); begin end; }


{ Funciones base correctas para usar en el programa }
function funcionConParams (a: integer): integer;
var variableLocal : integer;
begin
  funcionConParams := a + a
end;

function funcionVacia (): integer;
begin
  funcionVacia := 20
end;


{ ==================================================================== }
{ SECCIÓN 2: CUERPO PRINCIPAL - EXPRESIONES COMPLEJAS Y SENTENCIAS     }
{ ==================================================================== }
begin

  { --- CASOS COMPLEJOS DE ASIGNACIONES Y EXPRESIONES --- }
  
  { Caso Difícil 1: Precedencia estricta en Pascal (Relacionales vs Lógicos) }
  { En Pascal, operadores como 'and' tienen más precedencia que '>' y '='. }
  { Si tu analizador procesa esto sin paréntesis, fallará en la línea de abajo. }
  a := (5 > 3) and (c - 1 > 0); 
  
  { Caso Difícil 2: Combinación de operadores de igual precedencia de izquierda a derecha }
  b := 10 + 5 - 3 + a - variableMisteriosa; 
  
  { Caso de Éxito: Las llamadas corregidas a funciones (con y sin parámetros) como FACTOR }
  c := 1 + funcionConParams(1);
  d := 1 + funcionVacia();
  
  { --- ERRORES SINTÁCTICOS EN EXPRESIONES (Descomentar uno a la vez) --- }
  { TRAMPA 4: Operador de asignación estilo C en lugar de ':=' }
  { b = 10; }
  
  { TRAMPA 5: Operador relacional colgado sin un factor a la derecha }
  { isValid := (a > b) or (c >= ); }
  
  { TRAMPA 6: Doble operador matemático consecutivo }
  { result := a + * b; }
  
  { TRAMPA 7: Paréntesis de expresiones mal balanceados }
  { result := (a + b * (c - d); }


  { --- CASOS COMPLEJOS EN SENTENCIAS REPETITIVAS (WHILE) --- }
  
  { Caso de Éxito: Bloque while estructurado }
  while (counter < limit) do
  begin
    { Estructura Condicional Compleja Anidada (IF-THEN-ELSE) }
    if (counter div 2 = 0) then
      isEven := 1
    else
      isEven := 0;

    write(counter);
    counter := counter + 1
  end;

  { --- ERRORES EN SENTENCIAS DE CONTROL (Descomentar uno a la vez) --- }
  { TRAMPA 8: Omitir la palabra clave 'do' en el While }
  { while (counter < limit) begin counter := counter + 1 end; }
  
  { TRAMPA 9: Omitir la palabra clave 'then' en el If }
  { if (counter = 0) isEven := 1; }
  
  { TRAMPA 10: Poner punto y coma antes de un 'else' (Error clásico en Pascal) }
  { El token ';' antes de 'else' rompe la estructura del comando condicional }
  { if (a = b) then a := 1; else a := 2; }


  { --- CASOS COMPLEJOS DE LECTURA / ESCRITURA --- }
  write(result);
  read(limit);

  { --- ERRORES EN COMANDOS WRITE/READ (Descomentar uno a la vez) --- }
  { TRAMPA 11: Write vacío o sin paréntesis de cierre }
  { write(result ; }
  
  { TRAMPA 12: Read intentando leer una expresión completa en lugar de un identificador }
  { read(limit + 1); }

end.

{ --- ERROR FINAL DE CONTEXTO --- }
{ TRAMPA 13: Agregar código o tokens huérfanos después del punto final del programa }
{ var residuo: integer; }