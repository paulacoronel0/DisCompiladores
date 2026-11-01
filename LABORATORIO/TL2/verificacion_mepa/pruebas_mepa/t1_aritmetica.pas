program expr;
var a, b, c: integer;
begin
  a := 7;
  b := 3;
  c := a + b * 2 - (a - b) div 2;
  write(c);
  write(-a + b)
end.
