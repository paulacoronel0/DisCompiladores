program cond;
var x, y: integer;
    ok: boolean;
begin
  x := 5;
  y := 8;
  ok := (x < y) and not (x = y);
  if ok then write(1) else write(0);
  ok := false or (x > y);
  if ok then write(1) else write(0);
  if (x <> y) and (x <= y) and (y >= x) then write(100) else write(200)
end.
