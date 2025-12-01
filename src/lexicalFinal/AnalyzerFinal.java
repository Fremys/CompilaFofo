package lexicalFinal;

import java.util.ArrayList;
import symbolTable.ItemTableSymbol;

public class AnalyzerFinal {

    private final String src;
    private int pos = 0;
    private int line = 1;
    private int col = 1;

    private ArrayList<ItemTableSymbol> tableSymbols = new ArrayList<>();

    public AnalyzerFinal(String input) {
        this.src = input == null ? "" : input;
        tokenizeAll();
    }

    // -------------------- Helpers --------------------

    private char peek() {
        return pos < src.length() ? src.charAt(pos) : '\0';
    }

    private char peekNext() {
        return (pos + 1 < src.length()) ? src.charAt(pos + 1) : '\0';
    }

    private char advance() {
        char c = peek();
        pos++;
        if (c == '\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
        return c;
    }

    private boolean match(char expected) {
        if (peek() == expected) {
            advance();
            return true;
        }
        return false;
    }

    private void addToken(String cls, String val) {
        tableSymbols.add(new ItemTableSymbol(tableSymbols.size(), cls, val, 0));
    }

    // -------------------- Tokenização principal --------------------

    private void 
    
    
tokenizeAll() {

        while (true) {

            skipWhitespaceAndComments();

            char c = peek();
            if (c == '\0')
                break;

            // símbolos simples
            if (c == ';') { advance(); addToken("SYMBOL", ";"); continue; }
            if (c == '{') { advance(); addToken("SYMBOL", "{"); continue; }
            if (c == '}') { advance(); addToken("SYMBOL", "}"); continue; }
            if (c == '(') { advance(); addToken("SYMBOL", "("); continue; }
            if (c == ')') { advance(); addToken("SYMBOL", ")"); continue; }
            if (c == ',') { advance(); addToken("SYMBOL", ","); continue; }

            // STRING
            if (c == '"') {
                String s = readStringLiteral();
                addToken("STRING", s);
                continue;
            }

            // CHAR literal
            if (c == '\'') {
                String ch = readCharLiteral();
                addToken("CARACTERE", ch);
                continue;
            }

            // '<-' , '<>' , '<=' , '<'
            if (c == '<') {
                advance();
                if (match('-')) { addToken("SYMBOL", "<-"); continue; }
                if (match('>')) { addToken("LOGIC_OPERATOR", "<>"); continue; }
                if (match('=')) { addToken("LOGIC_OPERATOR", "<="); continue; }
                addToken("LOGIC_OPERATOR", "<");
                continue;
            }

            // '>' , '>='
            if (c == '>') {
                advance();
                if (match('=')) { addToken("LOGIC_OPERATOR", ">="); continue; }
                addToken("LOGIC_OPERATOR", ">");
                continue;
            }

            // '='
            if (c == '=') {
                advance();
                addToken("LOGIC_OPERATOR", "=");
                continue;
            }

            // operadores aritméticos
            if (c == '+') { advance(); addToken("MATH_OPERATOR", "+"); continue; }
            if (c == '-') { 
                advance();

                char cNext = peek();
                if(Character.isDigit(cNext)) {
                    String number = readNumber();
                    addToken("NUMERO", "-" + number);
                    continue;

                }
                
                addToken("MATH_OPERATOR", "-");
                continue;
             }
            if (c == '/') { advance(); addToken("MATH_OPERATOR", "/"); continue; }
            if (c == '%') { advance(); addToken("MATH_OPERATOR", "%"); continue; }

            // '*' ou '**'
            if (c == '*') {
                advance();
                if (match('*')) { addToken("MATH_OPERATOR", "**"); continue; }
                addToken("MATH_OPERATOR", "*");
                continue;
            }

            // operadores lógicos
            if (c == '&') { advance(); addToken("LOGIC_OPERATOR", "&"); continue; }
            if (c == '^') { advance(); addToken("LOGIC_OPERATOR", "^"); continue; }

            // números
            if (Character.isDigit(c)) {
                addToken("NUMERO", readNumber());
                continue;
            }

            // IDENTIFICADORES e PALAVRAS-CHAVE
            if (Character.isLetter(c)) {
                String word = readWord();
                String low = word.toLowerCase();

                // TYPE
                if (low.equals("inteiro")) { addToken("TYPE", "Inteiro"); continue; }
                if (low.equals("logico")) { addToken("TYPE", "Logico"); continue; }
                if (low.equals("caractere")) { addToken("TYPE", "Caractere"); continue; }

                // COMMAND
                if (low.equals("enquanto")) { addToken("COMMAND", "Enquanto"); continue; }
                if (low.equals("se")) { addToken("COMMAND", "Se"); continue; }
                if (low.equals("senao") || low.equals("senão")) { addToken("COMMAND", "Senao"); continue; }
                if (low.equals("para")) { addToken("COMMAND", "Para"); continue; }
                if (low.equals("imprimir")) { addToken("COMMAND", "Imprimir"); continue; }
                if (low.equals("em")) { addToken("COMMAND", "em"); continue; }

                // BOOLEANOS
                if (low.equals("verdade")) { addToken("CONST", "verdadeiro"); continue; }
                if (low.equals("mentira")) { addToken("CONST", "falso"); continue; }

                // IDENTIFIER (preserva o case original)
                addToken("IDENTIFIER", word);
                continue;
            }

            // caractere inválido
            addToken("ERRO", Character.toString(c));
            advance();
        }

        addToken("EOF", "EOF");
    }

    // -------------------- Leitura de componentes --------------------

    private String readWord() {
        StringBuilder sb = new StringBuilder();
        while (Character.isLetter(peek())) {
            sb.append(advance());
        }
        return sb.toString();
    }

    private String readNumber() {
    StringBuilder sb = new StringBuilder();

    // captura sinal negativo se estiver antes de um dígito
    if (peek() == '-' && Character.isDigit(peekNext())) {
        sb.append(advance()); // adiciona o '-'
    }

    while (Character.isDigit(peek())) {
        sb.append(advance());
    }

    return sb.toString();
}


    private String readStringLiteral() {
        advance(); // abre "
        StringBuilder sb = new StringBuilder();
        while (peek() != '"' && peek() != '\0') {
            sb.append(advance());
        }
        advance(); // fecha "
        return sb.toString();
    }

    private String readCharLiteral() {
        advance(); // abre '
        char c = advance();
        advance(); // fecha '
        return Character.toString(c);
    }

    // comentários e espaços
    private void skipWhitespaceAndComments() {

        while (true) {
            char c = peek();

            // espaços
            if (Character.isWhitespace(c)) {
                advance();
                continue;
            }

            // $$ ... $$
            if (c == '$' && peekNext() == '$') {
                advance(); advance(); // abre $$
                while (!(peek() == '$' && peekNext() == '$') && peek() != '\0')
                    advance();
                if (peek() != '\0') { advance(); advance(); } // fecha $$
                continue;
            }

            // $ ... \n
            if (c == '$') {
                advance();
                while (peek() != '\n' && peek() != '\0')
                    advance();
                continue;
            }

            break;
        }
    }

    // -------------------- Saída --------------------

    public ArrayList<ItemTableSymbol> getTableSymbols() {
        return tableSymbols;
    }

    public void printValueTokens() {
        for (ItemTableSymbol it : tableSymbols)
            System.out.println(it.Value);
    }

    public void printClassTokens() {
        for (ItemTableSymbol it : tableSymbols)
            System.out.println(it.Class);
    }

    // Verifica se algum token gerado é de classe "ERRO"
public boolean hasLexicalError() {
    for (ItemTableSymbol t : tableSymbols) {
        if ("ERRO".equals(t.Class)) {
            return true;
        }
    }
    return false;
}

}
