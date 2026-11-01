program anidado;
var a: integer;
procedure p(x: integer);
var y: integer;
  procedure q(z: integer);
  var w: integer;
  begin
    w := z + y;
    y := x - w;
    a := w
  end;
begin
  y := 10;
  q(x);
  write(y)
end;
begin
  a := 0;
  p(3);
  write(a)
end.
