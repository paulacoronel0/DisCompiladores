program anidado;
var a: integer;
procedure p(x: integer);
var y: integer;
  procedure q(z: integer);
  var w: integer;
  begin
    w := z - y;
    y := x - w;
    a := w
  end;
begin
  y := x
end;
begin
  a := a
end.