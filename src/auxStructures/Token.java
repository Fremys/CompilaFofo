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
}