program llamadas;
var n: integer;
    ok: boolean;

function doble(x: integer): integer;
begin
  doble := x * 2
end;

procedure mostrar(v: integer; b: boolean);
begin
  write(v)
end;

begin
  mostrar(n);
  mostrar(ok, n);
  n := mostrar;
  mostrar := 1;
  doble(3);
  n := n(2);
  read(doble)
end.
