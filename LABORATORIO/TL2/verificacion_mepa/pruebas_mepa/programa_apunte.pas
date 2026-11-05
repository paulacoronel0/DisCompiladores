program ejemplo;
   var m:integer;

function fact(a:integer):integer;
begin
  if a=1 then fact := 1
  else fact := fact(a-1)*a;
end;

begin
  read(m);
  m:= fact(m);
  write(m);
end.