program fact;
var r: integer;
function fac(n: integer): integer;
begin
  if n <= 1 then
    fac := 1
  else
    fac := n * fac(n - 1)
end;
begin
  r := fac(5);
  write(r)
end.
