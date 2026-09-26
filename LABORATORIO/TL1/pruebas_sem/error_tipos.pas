program tipos;
var n: integer;
    ok: boolean;
begin
  n := true;
  ok := n + 1;
  n := ok * 2;
  n := n - ok;
  ok := n = true;
  if n then
    n := 0;
  while ok < true do
    ok := not n
end.
