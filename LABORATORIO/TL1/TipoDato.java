// tipos de dato del subconjunto de mini-pascal
public enum TipoDato {
    INTEGER,
    BOOLEAN,
    VOID; // se usa como "tipo de retorno" de los procedimientos (no retornan valor)

    // traduce el token léxico de tipo (INTEGER/BOOLEAN) al TipoDato semántico
    public static TipoDato desdeTipoToken(TipoToken token) {
        switch (token) {
            case INTEGER:
                return INTEGER;
            case BOOLEAN:
                return BOOLEAN;
            default:
                throw new IllegalArgumentException("Token no mapeable a TipoDato: " + token);
        }
    }
}
