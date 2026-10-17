Program ConductorDePrueba;
Var a: integer;
    local: integer;

function pru(r:integer):integer;
var local: boolean;
begin
  if (r= 0) then pru:= 1
  else
     local:= r-1>2; 
 
    a:= pru(a);
end;

Function factorial(r:integer):integer;
begin
   
  a:= pru(a);     {----> acá NO debería dar error}

  if r = 1 then
    factorial := 1
  else
    factorial := r * factorial(r-1);   {----> acá NO debería dar error}
end;

Begin
  factorial;

  {pru();}

  a:= pru(a);   {----> acá NO debería dar error}

  a:= local;

  {pru:= 33;}   {----> acá SÍ debería dar error}

  local:= pru(33);   {----> acá NO debería dar error} 

  a := factorial(3);  {----> acá NO debería dar error}  

  factorial := factorial(3);  {----> acá SÍ debería dar error}


  write(a);

End.