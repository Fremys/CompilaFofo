package auxStructures;

public class Token{
    
    public String ClassToken;
    public Object ValueToken;

    public Token(){
        this.ClassToken = "";
        this.ValueToken = null;
    }

    public Token(String ClassToken, Object ValueToken){
        this.ClassToken = ClassToken;
        this.ValueToken = ValueToken;
    }

    public void printToken(){
        System.out.println("ClassToken: " + ClassToken);
        System.out.println("ValueToken: " + ValueToken);
    }


    // --- métodos utilitários para o semantic analyzer ---
    public String asString() {
        if (ValueToken instanceof String) return (String) ValueToken;
        if (ValueToken instanceof Integer) return Integer.toString((Integer) ValueToken);
        if (ValueToken instanceof Boolean) return ((Boolean) ValueToken) ? "verdadeiro" : "falso";
        return ValueToken == null ? "" : ValueToken.toString();
    }

    public Integer asInteger() {
        if (ValueToken instanceof Integer) return (Integer) ValueToken;
        if (ValueToken instanceof String) {
            try { return Integer.parseInt((String) ValueToken); }
            catch (Exception e) { return null; }
        }
        return null;
    }

    public Boolean asBoolean() {
        if (ValueToken instanceof Boolean) return (Boolean) ValueToken;
        if (ValueToken instanceof String) {
            String s = ((String) ValueToken).toLowerCase();
            if (s.equals("verdadeiro")) return true;
            if (s.equals("falso")) return false;
        }
        return null;
    }
}