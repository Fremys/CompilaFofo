package lexical;

import java.lang.reflect.Array;
import java.util.ArrayList;
public class Analyzer {

    private String[] number = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
                              "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private String[] arithmeticOperators = {"+", "-", "*", "/", "%", "**"};
    private String[] relationalOperators = {"<", "<=", ">", ">=", "==", "<>", "=", "&", "^"};
    private String[] otherSymbols = {";", "{", "}", "$", "$$", "\"", "(", ")", ","};
    private String[] booleanValues = {"verdade", "mentira"};
    private String[] command = {"Enquanto", "Se", "Senao", "Para", "Imprimir"};
    private String[] types = {"Inteiro", "Logico", "Caractere"};
    
    // dados para controle de leitura do analisador léxico
    private int initPos = 0;
    private int currentPos = 0;

    // string regex para tratamento da entrada
    private String regexString = "\n";
    
    // resultado do analisador
    private ArrayList<String> tokens = new ArrayList<String>();
    
    public Analyzer(String input){
        // definir dados
        String inputString = tratStringInput(input);
        
        while(initPos < inputString.length()-1) {
            
            String tmp = ""; 
            char charAnalytic = inputString.charAt(currentPos); 
            
            if(ContainsFirstElement(charAnalytic, types)){
                
                System.out.println("The first element can be contained in \"Types\" ");

            } else if(Contains(charAnalytic, otherSymbols)){
                
                tmp = String.valueOf(charAnalytic); // Inseri-los na analise direto como um terminal
                tokens.add(tmp);

            } else if(Contains(charAnalytic, arithmeticOperators)){

                tmp = "ARITHMETIC_OPERATOR: " + charAnalytic;
                tokens.add(tmp);
                
            } else if(Contains(charAnalytic, relationalOperators)){
                
                tmp = "RELATIONAL_OPERATOR:" + charAnalytic;
                tokens.add(tmp);

            } else if(Contains(charAnalytic, arithmeticOperators)){
                
                tmp = "OPERATORS: " + charAnalytic; 
                tokens.add(tmp);
            }
             else if(Contains(charAnalytic, number)){
                
                tmp = "NUMBER: " + ReadInteiro(inputString); 
                tokens.add(tmp);

            }
            else{
                
                System.err.println("ERRO: " + inputString.charAt(currentPos));
                initPos = inputString.length();
            }
            
            initPos = currentPos++;
        }
    }
    
    
    public String ReadInteiro(String input){ 
        String numero = ""; 
        char charAnalytic = input.charAt(currentPos);
        
        do{ 
            numero += charAnalytic; 
            charAnalytic = input.charAt(++currentPos); // atualizar o caractere analisado 
        }while(currentPos < input.length() && Contains(charAnalytic, number));
        
        // Voltar para o ultimo caractere lido
        currentPos--;

        return numero;
    }

    public String TryReadTypes(String input){

        // definir dados
        boolean success = false;
        String result = "";

        for(int i=0; i< input.length(); i++){
            // resgatar o Tipo analisado
            String typeAnalytic = types[i]; 
            
            do{
                // resgatar o caractere tipo analisado
               char charAnalytic = typeAnalytic.charAt(currentPos);
                
               

            }while();

        }
        
        
        // Voltar para o ultimo caractere lido
        currentPos--;

        return result;
    }

    public boolean ContainsFirstElement(char c, String arrayString[]){
        // Definir dados
        boolean resultado = false;
        String character = String.valueOf(c);
        
        // Procurar caractere no array
        for(int i = 0; !resultado && i < arrayString.length; i++){
            String firstCharacterForString = String.valueOf(arrayString[i].charAt(0)); 
            resultado = resultado || (character.equals(firstCharacterForString));
        }
        
        return resultado;
    }

    public boolean Contains(char c, String arrayString[]){
        // Definir dados
        boolean resultado = false;
        String character = String.valueOf(c);
        
        // Procurar caractere no array
        for(int i = 0; !resultado && i < arrayString.length; i++){ 
            resultado = resultado || (character.equals(arrayString[i]));
        }
        
        return resultado;
    }

    public String tratStringInput(String input){
        String result = input;
        
        result = result.replaceAll("\\s", "");
        result = result.replaceAll(regexString, "");
        result += '\0';

        return result;
    }

    public void printTokens(){
        for(int i = 0; i < tokens.size(); i++)
            System.out.println(tokens.get(i));
    }
}
