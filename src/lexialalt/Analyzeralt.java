package lexialalt;

import symbolTable.ItemTableSymbol;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Analyzeralt {

    // Classes
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
            "+", "-", "/", "*", "**", "%", "<", "<=", ">", ">=", "<>", "==", "=", "^", "&", "$", "(", ")", ";", ",",
            "\"", "{", "}", "$$",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
            "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z"
    };

    public int[] acceptStates = {1, 8, 15, 21, 30, 38, 40, 43, 53, 60, 61, 62, -1 };

    // Mapa dos indices da matriz de transição
    private Map<String, Integer> mapIndex = new HashMap<>();

    // Matriz de transição do automato
    private String[][] matrixTransaction;

    // Matriz
    private ArrayList<ItemTableSymbol> tableSymbols = new ArrayList<ItemTableSymbol>();

    // Dados para controle de leitura do analisador léxico
    private int initPos = 0;
    private int currentPos = 0;

    // String regex para tratamento da entrada
    private String regexString = "\n";

    ItemTableSymbol item = new ItemTableSymbol();

    public Analyzeralt(String input) {

        // definir estado inicial
        int currentState = 0;

        // String inputString = tratStringInput(input);
        mapIndex = createIndexCharacters(); // definir indices
        matrixTransaction = createTableTransaction(); // definir matriz de transição

        input += "\0\0";

        String c = "";
        String lookAHead = "";

        for (int i = 0; i < input.length() && !String.valueOf(input.charAt(currentPos)).equals("\0"); i++) {

            if (!Contains(currentState, acceptStates)) {
                // Definir dados
                c = String.valueOf(input.charAt(currentPos));
                lookAHead = String.valueOf(input.charAt(currentPos + 1));
            
                // Ignorar espaços
                if (c.equals(" ") || isBrokenLine(c) || c.equals("\t")) {
                    // pular enquanto for espaço
                    while ((c.equals(" ") || isBrokenLine(c) || c.equals("\t")) && (!c.equals("\0"))) {
                        c = String.valueOf(input.charAt(++currentPos));
                        lookAHead = String.valueOf(input.charAt(currentPos + 1));
                    }

                    initPos = currentPos;
                }

                // Ignorar os comentários
                if (c.equals("$") && lookAHead.equals("$")) {
                    do {
                        c = String.valueOf(input.charAt(++currentPos));
                        lookAHead = String.valueOf(input.charAt(currentPos + 1));
                    } while ((!c.equals("$") || !lookAHead.equals("$")) && (!c.equals("\0")));

                    // Passar para o proximo valido
                    currentPos += 2;
                    initPos = currentPos;
                    c = String.valueOf(input.charAt(currentPos));
                    lookAHead = String.valueOf(input.charAt(currentPos + 1));
                }

                if (c.equals("$")) {
                    do {
                        c = String.valueOf(input.charAt(++currentPos));
                        lookAHead = String.valueOf(input.charAt(currentPos + 1));
                    } while (!isBrokenLine(c) && (!c.equals("\0")));

                    initPos = currentPos;
                }
            }

            if (!Contains(c, specialStrings)) {

                currentState = readElementTable(currentState, c);

                switch (currentState) {
                    case 1:
                        if (c == "*" && lookAHead == "*") {
                            item = new ItemTableSymbol(tableSymbols.size(), "MATH_OPERATOR",
                                    input.substring(initPos, currentPos + 2), 0);
                            tableSymbols.add(item);

                            currentPos += 2;
                            initPos = currentPos;

                        } else {
                            item = new ItemTableSymbol(tableSymbols.size(), "MATH_OPERATOR",
                                    input.substring(initPos, currentPos), 0);
                            tableSymbols.add(item);
                            initPos = ++currentPos;
                        }

                        currentState = 0;

                        break;
                    case 8:
                        if (!Contains(lookAHead, letters)) {
                            item = new ItemTableSymbol(tableSymbols.size(), "TYPE",
                                    input.substring(initPos, currentPos), 0);
                            tableSymbols.add(item);
                            initPos = ++currentPos;
                            currentState = 0;
                        } else
                            currentPos++;

                        break;
                    case 15:
                        if (!Contains(lookAHead, letters)) {
                            item = new ItemTableSymbol(tableSymbols.size(), "COMMAND",
                                    input.substring(initPos, currentPos), 0);
                            tableSymbols.add(item);
                            initPos = ++currentPos;
                            currentState = 0;
                            currentPos++;
                        } else

                            break;
                    case 21:
                        if (!Contains(lookAHead, letters)) {
                            item = new ItemTableSymbol(tableSymbols.size(), "COMMAND",
                                    input.substring(initPos, currentPos), 0);
                            tableSymbols.add(item);
                            initPos = ++currentPos;
                            currentState = 0;
                        } else
                            currentPos++;

                        break;
                    case 30:
                        if (!Contains(lookAHead, letters)) {
                            item = new ItemTableSymbol(tableSymbols.size(), "TYPE",
                                    input.substring(initPos, currentPos), 0);
                            tableSymbols.add(item);
                            initPos = ++currentPos;
                            currentState = 0;
                        } else
                            currentPos++;

                        break;
                    case 38:
                        if (!Contains(lookAHead, letters)) {
                            item = new ItemTableSymbol(tableSymbols.size(), "COMMAND",
                                    input.substring(initPos, currentPos), 0);
                            tableSymbols.add(item);
                            initPos = ++currentPos;
                            currentState = 0;
                        } else
                            currentPos++;

                        break;
                    case 40:
                        if (lookAHead == "n") {
                            currentPos++;
                        } else if (!Contains(lookAHead, letters)) {
                            item = new ItemTableSymbol(tableSymbols.size(), "COMMAND",
                                    input.substring(initPos, currentPos), 0);
                            tableSymbols.add(item);
                            initPos = ++currentPos;
                            currentState = 0;
                        } else
                            currentPos++;

                        break;
                    case 43:
                        if (!Contains(lookAHead, letters)) {
                            item = new ItemTableSymbol(tableSymbols.size(), "COMMAND",
                                    input.substring(initPos, currentPos), 0);
                            tableSymbols.add(item);
                            initPos = ++currentPos;
                            currentState = 0;
                        } else
                            currentPos++;

                        break;
                    case 53:
                        if (!Contains(lookAHead, letters)) {
                            item = new ItemTableSymbol(tableSymbols.size(), "CONST",
                                    input.substring(initPos, currentPos), 0);
                            tableSymbols.add(item);
                            initPos = ++currentPos;
                            currentState = 0;
                        } else
                            currentPos++;

                        break;
                    case 60:
                        if (!Contains(lookAHead, letters)) {
                            item = new ItemTableSymbol(tableSymbols.size(), "CONST",
                                    input.substring(initPos, currentPos), 0);
                            tableSymbols.add(item);
                            initPos = ++currentPos;
                            currentState = 0;
                        } else
                            currentPos++;

                        break;
                    case 61:
                        item = new ItemTableSymbol(tableSymbols.size(), "LOGIC_OPERATOR",
                                input.substring(initPos, currentPos), 0);
                        tableSymbols.add(item);
                        initPos = ++currentPos;

                        currentState = 0;

                        break;
                    case 62:
                        item = new ItemTableSymbol(tableSymbols.size(), String.valueOf(input.charAt(currentPos)),
                                String.valueOf(input.charAt(currentPos)), 0);
                        tableSymbols.add(item);
                        initPos = ++currentPos;

                        currentState = 0;
                        break;
                    case 63:
                        // verificar se o nome do identificador chegou ao fim
                        if (!Contains(lookAHead, letters)) {
                            item = new ItemTableSymbol(tableSymbols.size(), "IDENTIFY",
                                    input.substring(initPos, currentPos),
                                    0);
                            tableSymbols.add(item);
                            initPos = ++currentPos;
                            currentState = 0;
                        } else
                            currentPos++;

                        break;
                    case -1:
                        System.err.println("ERRO NA ANÁLISE LÉXICA");
                        i = input.length();
                        currentState = 0;

                        break;
                    default:
                        currentPos++;
                        break;

                }

                // String teste = String.valueOf(input.charAt(currentPos));
                // resgatar proximo estado do automato
                // currentState = readElementTable(currentState, c);
            }
        }

        String fim = "fim";
    }

    public String tratStringInput(String input) {
        String result = input;

        result = result.replaceAll("\\s", "");
        result = result.replaceAll(regexString, "");
        result += '\0';

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

    private String[][] createTableTransaction() {
        // Definir dados
        int qtdStates = 64;
        int qtdSymbols = indices.length;

        return lerCSV("./TransactionTable/Transaction2.csv", new String[qtdStates][qtdSymbols]);
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

    public boolean Contains(int number, int[] array) {
        // Definir dados
        boolean resultado = false;

        // Procurar caractere no array

        for (int i = 0; !resultado && i < array.length; i++) {
            resultado = resultado || (number == array[i]);
        }

        return resultado;
    }

    public static boolean isBrokenLine(String c) {

        return c.equals("\n") || c.equals("\r");
    }
}
