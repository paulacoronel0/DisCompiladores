program correctoDos;
var
    a, b, resultado: integer;
    flag1, flag2, finalOk: boolean;

procedure procesarDatos(num: integer; permitido: boolean);
var
    cociente, resto: integer;
    condicion: boolean;
begin
    { Lectura valida sobre variable local }
    read(cociente);
    
    { Operadores aritmeticos enteros: div, - y + }
    cociente := num div 2;
    resto := num - cociente;

    { Comparaciones validas = y <> entre enteros }
    condicion := (resto = 0) or (resto <> 1);

    { Operador unario not y operador logico and con booleanos }
    if permitido and (not condicion) then
        write(cociente)
    else
        write(resto)
end;

begin
    read(a);
    read(b);
    flag1 := true;
    flag2 := false;

    { Operador relacional de igualdad entre booleanos }
    finalOk := flag1 = flag2;

    { Expresion logica combinada valida como condicion del if }
    if (a > 0) and not finalOk then
    begin
        { Llamada a procedimiento con cantidad y orden de tipos exactos }
        procesarDatos(a, flag1);
        resultado := (a + b) div 2;
        write(resultado)
    end
    else
        procesarDatos(b, false)
end.