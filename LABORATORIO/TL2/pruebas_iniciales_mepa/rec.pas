program prec;
var m: integer; c: boolean;
function f(n: integer; b: boolean): integer;
var p: integer;
begin
  if b then f := n
  else begin
    p := f(n - m, b) - f(-n, c);
    f := p
  end
end;
function g: integer;
begin
  g := m
end;
begin
  m := f(m, c) - g
end.