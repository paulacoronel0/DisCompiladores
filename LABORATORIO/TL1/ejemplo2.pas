program Ejemplo2;
var total: integer;
function doble(x: integer): integer;
var y: integer;
begin
  y := x * 2;
  doble := y
end;
begin
  total := doble(5);
end.