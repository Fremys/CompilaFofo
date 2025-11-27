package auxStructures;

import java.util.ArrayList;

public class ItemTableSymbol {
    
    public int Id;
    public String Name; 
    public Object Value;
    public long Address;
    
    public ItemTableSymbol(){

        this.Id = 0;
        this.Name = "";
        this.Value = null;
        this.Address = 0;
    }

    public ItemTableSymbol(int Id, String Name, Object Value, long Address){
        this.Id = Id;
        this.Name = Name;
        this.Value = Value;
        this.Address = Address;
    }

    public void printItem(){
        System.out.println("Id: " + Id);
        System.out.println("Class: " + Name);
        System.out.println("Value: " + Value);
        System.out.println("Address: " + Address);
    }

    public ArrayList<ItemTableSymbol> AddList(ArrayList<ItemTableSymbol> list){
        
        if(list != null){
            
            for(int i = 0; i < list.size(); i++){
                // Deixei Name para facilitar a leitura
                ItemTableSymbol findItem = list.get(i);

                if(Name.equals(findItem.Name))
                    return list; // já existe, não adicionar
            }
            list.add(this); // adicionar novo item
        }
        return list;
    }
    
}