{ Archivo de prueba: test_completo.pas
  Ejercita todos los tokens del TP Laboratorio N°2 }

program EjemploCompleto;

var
    x, y, resultado : integer;
    bandera         : boolean;

procedure sumar(a, b : integer);
begin
    resultado := a + b;
    write(resultado)
end;

function esMayor(p, q : integer) : boolean;
begin
    if p > q then
        esMayor := true
    else
        esMayor := false
end;

begin
    { Bloque principal }
    x := 10;
    y := 3;

    while x > 0 do
    begin
        sumar(x, y);
        x := x - 1
    end;

    bandera := NOT false;
    bandera := true AND false;
    bandera := true OR false;

    if x = y then
        write(x)
    else
        read(y);

    resultado := x * y;
    resultado := x div y;
    resultado := x / y;

    { prueba de arreglo con llaves cuadradas }
    y := resultado + 1;

    if x < y then
        x := x + 1

end.
