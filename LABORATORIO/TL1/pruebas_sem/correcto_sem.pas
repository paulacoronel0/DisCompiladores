program ejemplo;
var n, total: integer;
    activo: boolean;

function factorial(k: integer): integer;
begin
  if k <= 1 then
    factorial := 1
  else
    factorial := k * factorial(k - 1)
end;

function enRango(v, limite: integer; estricto: boolean): boolean;
begin
  if estricto then
    enRango := v < limite
  else
    enRango := v <= limite
end;

procedure acumular(veces: integer);
var activo: integer;
  procedure sumar(x: integer);
  begin
    if x > 0 then total := total + x
  end;
begin
  activo := 0;
  while activo < veces do
  begin
    sumar(activo);
    activo := activo + 1
  end
end;

begin
  read(n);
  total := 0;
  activo := enRango(n, 10, true);
  if activo and not (n = 0) then
  begin
    acumular(n);
    write(factorial(n))
  end
  else
    write(false);
  write(total)
end.
