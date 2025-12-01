package symbolTable;

public class ItemTableSymbol {
    public int Id;
    public String Class; 
    public Object Value;
    public long Address;

    public ItemTableSymbol() {
        this.Id = 0;
        this.Class = "";
        this.Value = null;
        this.Address = 0;
    }

    public ItemTableSymbol(int Id, String Class, Object Value, long Address){
        this.Id = Id;
        this.Class = Class;
        this.Value = Value;
        this.Address = Address;
    }

    public void printItem(){
        System.out.println("Id: " + Id);
        System.out.println("Class: " + Class);
        System.out.println("Value: " + Value);
        System.out.println("Address: " + Address);
    }

    // --- métodos utilitários para o semantic analyzer ---
    public String asString() {
        if (Value instanceof String) return (String) Value;
        if (Value instanceof Integer) return Integer.toString((Integer) Value);
        if (Value instanceof Boolean) return ((Boolean) Value) ? "verdadeiro" : "falso";
        return Value == null ? "" : Value.toString();
    }

    public Integer asInteger() {
        if (Value instanceof Integer) return (Integer) Value;
        if (Value instanceof String) {
            try { return Integer.parseInt((String) Value); }
            catch (Exception e) { return null; }
        }
        return null;
    }

    public Boolean asBoolean() {
        if (Value instanceof Boolean) return (Boolean) Value;
        if (Value instanceof String) {
            String s = ((String) Value).toLowerCase();
            if (s.equals("verdadeiro")) return true;
            if (s.equals("falso")) return false;
        }
        return null;
    }
}
