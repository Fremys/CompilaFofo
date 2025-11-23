package symbolTable;

public class ItemTableSymbol {
    
    public int Id;
    public String Class; 
    public Object Value;
    public long Address;
    
    public ItemTableSymbol(){

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
    
}
