program suma;
var n, i, s: integer;
begin
  read(n);
  i := 1;
  s := 0;
  while i <= n do
  begin
    s := s + i;
    i := i + 1
  end;
  write(s)
end.
