package lexicalFinal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import auxStructures.*;

public class AnalyzerFinal {

    private String[] number = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
    private String[] letters = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q",
            "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z" };
    private String[] arithmeticOperators = { "+", "-", "*", "/", "%", "**" };
    private String[] relationalOperators = { "<", "<=", ">", ">=", "==", "<>", "=", "&", "^" };
    private String[] otherSymbols = { ";", "{", "}", "$", "$$", "\"", "(", ")", "," };
    private String[] booleanValues = { "verdade", "mentira" };
    private String[] command = { "Enquanto", "Se", "Senao", "Para", "Imprimir" };
    private String[] types = { "Inteiro", "Logico", "Caractere" };

    private String[] specialStrings = { "\0", "\t", " ", "\n", "\r" };

    public String[] indices = {
            "+", "-", "/", "*", "%", "<", ">", "=", "^", "&", "$", "(", ")", ";", ",",
            "\"", "{", "}",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
            "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z", "\r", " ", "\t", "\n", "\'", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "\0" 
    };

    // Mapa dos indices da matriz de transição
    private Map<String, Integer> mapIndex = new HashMap<>();

    // Matriz de transição do automato
    private String[][] matrixTransaction;

    // Matriz
    private ArrayList<ItemTableSymbol> tableSymbols = new ArrayList<ItemTableSymbol>();
    private ArrayList<Token> tokens = new ArrayList<Token>(); 

    // Definir tabela de símbolos
    ItemTableSymbol item = new ItemTableSymbol();
    Token token = new Token();

    // Dados para controle de leitura do analisador léxico
    private int initPos = 0;
    private int currentPos = 0;

    public AnalyzerFinal(String input) {
        // Definir dados
        int currentState = 0;
        input += "\0\0";

        mapIndex = createIndexCharacters(); // definir indices
        matrixTransaction = createTableTransaction("TransactionFinal2.csv", 83); // definir matriz de transição

        String c = "";
        String lookAHead = "";

        for (int i = 0; i < input.length() && !c.equals("\0"); i++) {
            // resgatar char para analisar
            c = getChar(input, currentPos);
            lookAHead = getChar(input, currentPos + 1);

            // Ignorar espaços
            if ((c.equals(" ") || c.equals("\r") || c.equals("\t") || c.equals("\n")) && currentState == 0) {

                // Ignorar enquanto for espaço
                while ((c.equals(" ") || c.equals("\r") || c.equals("\t") || c.equals("\n")) && (!c.equals("\0"))) {
                    c = String.valueOf(input.charAt(++currentPos));
                    lookAHead = String.valueOf(input.charAt(currentPos + 1));
                }

                initPos = currentPos;
            }


            // Executar automato
            currentState = readElementTable(currentState, c);

            switch (currentState) {
                case 0:
                    initPos = ++currentPos;
                    break;
                case 1:
                    // item = new ItemTableSymbol(tableSymbols.size(), "MATH_OPERATOR",
                    // input.substring(initPos, currentPos + 1), 0);

                    // tableSymbols.add(item);

                    // salvar token
                    token = new Token("MATH_OPERATOR", input.substring(initPos, currentPos + 1));
                    tokens.add(token);
                    
                    initPos = ++currentPos; // Passar para o próximo caractere
                    currentState = 0;

                    break;
                case 8:
                    if (!Contains(lookAHead, letters)) {
                        // item = new ItemTableSymbol(tableSymbols.size(), "TYPE",
                        //         input.substring(initPos, currentPos + 1), 0);

                        // tableSymbols.add(item);

                        // salvar token
                        token = new Token("TYPE", input.substring(initPos, currentPos + 1));
                        tokens.add(token);

                        initPos = ++currentPos;
                        currentState = 0;
                    } else
                        currentPos++;

                    break;
                case 15:
                    if (!Contains(lookAHead, letters)) {
                        // item = new ItemTableSymbol(tableSymbols.size(), "COMMAND",
                        //         input.substring(initPos, currentPos + 1), 0);
                        
                        // tableSymbols.add(item);
                        
                        // salvar token
                        token = new Token("COMMAND", input.substring(initPos, currentPos + 1));
                        tokens.add(token);
                        
                        initPos = ++currentPos;
                        currentState = 0;                           
                    } else
                            currentPos++;
                        break;
                case 21:
                    if (!Contains(lookAHead, letters)) {

                        // item = new ItemTableSymbol(tableSymbols.size(), "TYPE",
                        //         input.substring(initPos, currentPos + 1), 0);
                        
                        // tableSymbols.add(item);

                        // salvar token
                        token = new Token("TYPE", input.substring(initPos, currentPos + 1));
                        tokens.add(token); 
                        
                        initPos = ++currentPos;
                        currentState = 0;
                    } else
                        currentPos++;

                    break;
                case 30:
                    if (!Contains(lookAHead, letters)) {
                        // item = new ItemTableSymbol(tableSymbols.size(), "TYPE",
                        //         input.substring(initPos, currentPos + 1), 0);
                        // tableSymbols.add(item);

                        // salvar token
                        token = new Token("TYPE", input.substring(initPos, currentPos + 1));
                        tokens.add(token);

                        initPos = ++currentPos;
                        currentState = 0;
                    } else
                        currentPos++;

                    break;
                case 38:
                    if (!Contains(lookAHead, letters)) {
                        // item = new ItemTableSymbol(tableSymbols.size(), "COMMAND",
                        //         input.substring(initPos, currentPos + 1), 0);
                        // tableSymbols.add(item);

                        // salvar token
                        token = new Token("COMMAND", input.substring(initPos, currentPos + 1));
                        tokens.add(token);
                        
                        initPos = ++currentPos;
                        currentState = 0;
                    } else
                        currentPos++;

                    break;
                case 40:
                    if (lookAHead == "n") {
                        currentPos++;
                    } else if (!Contains(lookAHead, letters)) {
                        // item = new ItemTableSymbol(tableSymbols.size(), "COMMAND",
                        //         input.substring(initPos, currentPos + 1), 0);
                        // tableSymbols.add(item);
                        
                        // salvar token
                        token = new Token("COMMAND", input.substring(initPos, currentPos + 1));
                        tokens.add(token);

                        initPos = ++currentPos;
                        currentState = 0;
                    } else
                        currentPos++;

                    break;
                case 43:
                    if (!Contains(lookAHead, letters)) {
                        // item = new ItemTableSymbol(tableSymbols.size(), "COMMAND",
                        //         input.substring(initPos, currentPos + 1), 0);
                        // tableSymbols.add(item);

                        // salvar token
                        token = new Token("COMMAND", input.substring(initPos, currentPos + 1));
                        tokens.add(token);

                        initPos = ++currentPos;
                        currentState = 0;
                    } else
                        currentPos++;

                    break;
                case 53:
                    if (!Contains(lookAHead, letters)) {
                        // item = new ItemTableSymbol(tableSymbols.size(), "CONST",
                        //         input.substring(initPos, currentPos + 1), 0);
                        // tableSymbols.add(item);

                        // salvar token
                        token = new Token("CONST", input.substring(initPos, currentPos + 1));
                        tokens.add(token);

                        initPos = ++currentPos;
                        currentState = 0;
                    } else
                        currentPos++;

                    break;
                case 60:
                    if (!Contains(lookAHead, letters)) {
                        // item = new ItemTableSymbol(tableSymbols.size(), "CONST",
                        //         input.substring(initPos, currentPos + 1), 0);
                        // tableSymbols.add(item);
                        
                        // salvar token
                        token = new Token("CONST", input.substring(initPos, currentPos + 1));
                        tokens.add(token);

                        initPos = ++currentPos;
                        currentState = 0;
                    } else
                        currentPos++;

                    break;
                case 61:
                    // item = new ItemTableSymbol(tableSymbols.size(), "LOGIC_OPERATOR",
                    //         input.substring(initPos, currentPos + 1), 0);
                    // tableSymbols.add(item);

                    // salvar token
                    token = new Token("LOGIC_OPERATOR", input.substring(initPos, currentPos + 1));
                    tokens.add(token);

                    initPos = ++currentPos;
                    currentState = 0;

                    break;
                case 62:
                    // item = new ItemTableSymbol(tableSymbols.size(), getChar(input, currentPos),
                    //         getChar(input, currentPos), 0);
                    // tableSymbols.add(item);

                    // salvar token
                    token = new Token(getChar(input, currentPos), getChar(input, currentPos));  
                    tokens.add(token);

                    initPos = ++currentPos;
                    currentState = 0;
                    break;
                case 63:
                    // verificar se o nome do identificador chegou ao fim
                    if (!Contains(lookAHead, letters)) {

                        // Salvar identificador na tabela de símbolos
                        item = new ItemTableSymbol(tableSymbols.size(), "IDENTIFY",
                                input.substring(initPos, currentPos + 1),
                                0);

                        tableSymbols = item.AddList(tableSymbols); // adicionar identificador na tabela de símbolos

                        // salvar token
                        token = new Token("IDENTIFY", input.substring(initPos, currentPos + 1));
                        tokens.add(token);

                        initPos = ++currentPos;
                        currentState = 0;
                    } else
                        currentPos++;

                    break;
                case 64:
                    if (  !lookAHead.equals("*") || getChar(input, currentPos-1).equals("*") && getChar(input, currentPos-2).equals("*") ) {
                        // item = new ItemTableSymbol(tableSymbols.size(), "MATH_OPERATOR",
                        //         input.substring(initPos, currentPos + 1), 0);
                        // tableSymbols.add(item);

                        // salvar token
                        token = new Token("MATH_OPERATOR", input.substring(initPos, currentPos + 1));
                        tokens.add(token);

                        initPos = ++currentPos;
                        currentState = 0;
                    } else
                        currentPos++;
                    break;
                case 66:
                    if (!(lookAHead.equals(">") || lookAHead.equals("=") || lookAHead.equals("-"))   ) {
                        // item = new ItemTableSymbol(tableSymbols.size(), "LOGIC_OPERATOR",
                        //         input.substring(initPos, currentPos + 1), 0);
                        // tableSymbols.add(item);

                        // salvar token
                        token = new Token("LOGIC_OPERATOR", input.substring(initPos, currentPos + 1));
                        tokens.add(token);

                        initPos = ++currentPos;
                        currentState = 0;
                    } else
                        currentPos++;
                    break;
                case 69:
                    if (!lookAHead.equals("=")) {
                        // item = new ItemTableSymbol(tableSymbols.size(), "LOGIC_OPERATOR",
                        //         input.substring(initPos, currentPos + 1), 0);
                        // tableSymbols.add(item);

                        // salvar token
                        token = new Token("LOGIC_OPERATOR", input.substring(initPos, currentPos + 1));
                        tokens.add(token);

                        initPos = ++currentPos;
                        currentState = 0;
                    } else
                        currentPos++;

                    break;
                case 70:
                    // item = new ItemTableSymbol(tableSymbols.size(), input.substring(initPos, currentPos + 1),
                    //         input.substring(initPos, currentPos + 1), 0);
                    // tableSymbols.add(item);

                    // salvar token
                    token = new Token(input.substring(initPos, currentPos + 1), input.substring(initPos, currentPos + 1));
                    tokens.add(token);

                    initPos = ++currentPos;
                    currentState = 0;

                    break;
                case 72:
                        // item = new ItemTableSymbol(tableSymbols.size(), "STRING",
                        // input.substring(initPos, currentPos + 1), 0);
                        // tableSymbols.add(item);

                        // salvar token
                        token = new Token("STRING", input.substring(initPos, currentPos + 1));
                        tokens.add(token);
                        
                        initPos = ++currentPos;
                        currentState = 0;
                break;
                case 73:
                    if (!Contains(lookAHead, number)) {

                        // item = new ItemTableSymbol(tableSymbols.size(), "NUMERO",
                        // input.substring(initPos, currentPos + 1), 0);
                        // tableSymbols.add(item);
                        
                        // salvar token
                        token = new Token("NUMERO", input.substring(initPos, currentPos + 1));
                        tokens.add(token);                        

                        initPos = ++currentPos;
                        currentState = 0;
                    }else
                        currentPos++;
                break;
                case 76:
                    // item = new ItemTableSymbol(tableSymbols.size(), "CARACTERE",
                    //             input.substring(initPos, currentPos + 1), 0);
                    // tableSymbols.add(item);

                    // salvar token
                    token = new Token("CARACTERE", input.substring(initPos, currentPos + 1));
                    tokens.add(token);

                    initPos = ++currentPos;
                    currentState = 0;
                break;
                case 80:
                    // item = new ItemTableSymbol(tableSymbols.size(), "COMMAND",
                    //             input.substring(initPos, currentPos + 1), 0);
                    // tableSymbols.add(item);

                    // salvar token
                    token = new Token("COMMAND", input.substring(initPos, currentPos + 1)); 
                    tokens.add(token);

                    initPos = ++currentPos;
                    currentState = 0;
                break;
                case 82:
                    // item = new ItemTableSymbol(tableSymbols.size(), input.substring(initPos, currentPos + 1),
                    //         input.substring(initPos, currentPos + 1), 0);
                    // tableSymbols.add(item);

                    // salvar token
                    token = new Token(input.substring(initPos, currentPos + 1), input.substring(initPos, currentPos + 1));
                    tokens.add(token);

                    initPos = ++currentPos;
                    currentState = 0;
                    
                    break;
                case -1:
                    // item = new ItemTableSymbol(tableSymbols.size(), "ERRO NA ANÁLISE LÉXICA",
                    //             "-1", 0);
                    // tableSymbols.add(item);
                    
                    // salvar token
                    token = new Token("ERRO NA ANÁLISE LÉXICA", "-1");;
                    tokens.add(token);  

                    i = input.length();
                    currentState = 0;

                    break;
                default:
                    currentPos++;
                    break;
            }
        }
    }

    private String getChar(String text, int pos) {
        String result = "";

        result = String.valueOf(text.charAt(pos));

        return result;
    }

    private Map<String, Integer> createIndexCharacters() {
        // definir dados
        final Map<String, Integer> indexes = new HashMap<>();

        for (int i = 0; i < indices.length; i++) {
            indexes.put(indices[i], i);
        }

        return indexes;
    }

    private String[][] createTableTransaction(String nameAqr, int pQtdStates) {
        // Definir dados
        int qtdStates = pQtdStates;
        int qtdSymbols = indices.length;

        return lerCSV("./TransactionTable/" + nameAqr, new String[qtdStates][qtdSymbols]);
    }

    public static String[][] lerCSV(String caminhoArquivo, String[][] matrix) {
        // Definir dados
        String[][] result = matrix;

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {

            String linha;

            int i = 0;
            while ((linha = br.readLine()) != null) {

                String[] valores = linha.split(";");

                matrix[i] = valores;

                i++;
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo CSV: " + e.getMessage());
        }

        return result;
    }

    public int readElementTable(int state, String symbol) {
        // Definir dados
        int result = -1;

        int i = state;
        int j = mapIndex.get(symbol);

        result = Integer.parseInt(matrixTransaction[i][j]);

        return result;
    }

    public boolean Contains(String character, String arrayString[]) {
        // Definir dados
        boolean resultado = false;

        // Procurar caractere no array

        for (int i = 0; !resultado && i < arrayString.length; i++) {
            resultado = resultado || (character.equals(arrayString[i]));
        }

        return resultado;
    }

    public void printTokens(String typePrint){
        if(typePrint.equals("class")){
            printClassTokens();
        } else if(typePrint.equals("value")){
            printValueTokens();
        }
    }

    private void printClassTokens(){
        for(int i=0; i<tokens.size(); i++){
            Token item = tokens.get(i);
            System.out.println(item.ClassToken);
        }

    }

    private void printValueTokens(){
        for(int i=0; i<tokens.size(); i++){
            Token item = tokens.get(i);
            System.out.println(item.ValueToken);
        }

    }

    public ArrayList<ItemTableSymbol> getTableSymbols(){
        return tableSymbols;
    }

    public ArrayList<Token> getTokens(){
        return tokens;
    }

}
