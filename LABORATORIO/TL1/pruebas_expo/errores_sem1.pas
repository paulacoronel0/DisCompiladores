program erroresUno;
var
    x, total: integer;
    flag: boolean;
    x: boolean; { Error: El identificador 'x' ya habia sido declarado en este ambito }

procedure rutina(p1: integer; p1: boolean); { Error: El parametro 'p1' ya habia sido declarado }
var
    interna: integer;
begin
    interna := 10
end;

begin
    { 1. Error de ambito: 'interna' pertenece a rutina y no es visible aca }
    x := interna + 1;

    { 2. Error de declaracion: identificador totalmente desconocido }
    total := variableInexistente;

    { 3. Error de condicion en if: se espera BOOLEAN y se recibe INTEGER }
    if total then
        x := 5;

    { 4. Error de condicion en while: se espera BOOLEAN y se recibe INTEGER }
    while x do
        x := x - 1;

    { 5. Error en operador de comparacion: INTEGER y BOOLEAN son incompatibles }
    flag := x = true;

    { 6. Error en operador logico: 'and' requiere BOOLEAN en ambos operandos }
    flag := true and x;

    { 7. Error de asignacion invalida: no se le puede asignar valor a un procedimiento }
    rutina := 20;

    { 8. Error de asignacion invalida: no se le puede asignar valor al programa principal }
    erroresUno := 0
end.