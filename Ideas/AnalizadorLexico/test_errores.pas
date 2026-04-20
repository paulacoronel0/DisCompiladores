{ test_errores.pas: prueba de recuperación ante errores léxicos }

program PruebaErrores;

var
    x : integer;

begin
    x := 5;
    x := x + @;    { '@' es carácter ilegal }
    x := x # 2;    { '#' es carácter ilegal }
    write(x)
end.
