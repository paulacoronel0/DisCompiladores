# ============================================================
#  Makefile  —  Analizador Léxico mini-Pascal
#  Trabajo de Laboratorio N°2  –  Compiladores  –  UNCOMA
#
#  Uso:
#    make          → compila todas las clases
#    make test     → ejecuta con el archivo de prueba completo
#    make errores  → ejecuta con el archivo que tiene errores léxicos
#    make clean    → borra los .class generados
# ============================================================

JAVAC   = javac
JAVA    = java
MAIN    = Analizador
SOURCES = Token.java TablaSimbolos.java ErrorLexico.java \
          AnalizadorLexico.java Analizador.java
TEST1   = test_completo.pas
TEST2   = test_errores.pas

# ── Compilación ──────────────────────────────────────────────
all: $(SOURCES)
	$(JAVAC) $(SOURCES)
	@echo ""
	@echo "  ✓ Compilación exitosa."
	@echo "  Uso: java $(MAIN) <archivo.pas>"
	@echo ""

# ── Ejecuciones de prueba ─────────────────────────────────────
test: all
	@echo ""
	@echo "=== Prueba con $(TEST1) ==="
	$(JAVA) $(MAIN) $(TEST1)

errores: all
	@echo ""
	@echo "=== Prueba con $(TEST2) (contiene errores léxicos) ==="
	$(JAVA) $(MAIN) $(TEST2); true

# ── Limpieza ──────────────────────────────────────────────────
clean:
	rm -f *.class
	@echo "  Archivos .class eliminados."

.PHONY: all test errores clean
