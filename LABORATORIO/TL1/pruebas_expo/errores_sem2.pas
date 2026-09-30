program erroresDos;
var
    n, res: integer;
    activo: boolean;

function calcular(val: integer): integer;
begin
    calcular := val * 2
end;

procedure imprimir(id: integer; estado: boolean);
begin
    write(id)
end;

begin
    { 1. Asignaciones incompatibles directas }
    n := true;
    activo := 50;

    { 2. Asignar a una funcion fuera de su propio cuerpo }
    calcular := 100;

    { 3. Operador aritmetico: operando izquierdo invalido }
    res := activo + 10;

    { 4. Operador aritmetico: operando derecho invalido }
    res := 10 * activo;

    { 5. Operador logico unario: 'not' requiere BOOLEAN y recibe INTEGER }
    activo := not n;

    { 6. Uso indebido: procedimiento usado como valor dentro de una expresion }
    res := 5 + imprimir;

    { 7. Uso indebido: funcion invocada como si fuese sentencia/procedimiento }
    calcular(10);

    { 8. Uso indebido: variable invocada con parentesis como si fuese funcion }
    res := n(4);

    { 9. Argumentos: cantidad incorrecta (espera 2 y recibe 1) }
    imprimir(n);

    { 10. Argumentos: tipos incompatibles en cada posicion }
    imprimir(activo, n);

    { 11. Lectura invalida: read sobre un subprograma }
    read(calcular)
end.