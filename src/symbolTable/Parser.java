package symbolTable;
import java.util.ArrayList;

public class Parser {

    private ArrayList<ItemTableSymbol> tokens;
    private int pos = 0;

    public Parser(ArrayList<ItemTableSymbol> tokens) {
        this.tokens = tokens;
    }

    // utilitários
    private ItemTableSymbol peekToken() {
        if (pos < tokens.size()) return tokens.get(pos);
        // token sentinel de fim
        return new ItemTableSymbol(-1, "EOF", "EOF", 0);
    }

    private ItemTableSymbol consumeToken() {
        ItemTableSymbol t = peekToken();
        pos++;
        return t;
    }

    // Match por classe (ex.: "IDENTIFY") e opcionalmente por valor (ex.: "Se")
    private ItemTableSymbol match(String expectedClass) throws ParseException {
        ItemTableSymbol t = peekToken();
        if (t.Class.equals(expectedClass)) {
            return consumeToken();
        }
        throw new ParseException("Esperava token da classe '" + expectedClass + "', encontrado '" + t.Class + "' (valor='" + t.Value + "') na posição " + pos);
    }

    private ItemTableSymbol match(String expectedClass, String expectedValue) throws ParseException {
        ItemTableSymbol t = peekToken();
        if (t.Class.equals(expectedClass) && t.Value.equals(expectedValue)) {
            return consumeToken();
        }
        throw new ParseException("Esperava '" + expectedValue + "', encontrado '" + t.Value + "' (classe='" + t.Class + "') na posição " + pos);
    }

    // entry point
    public void parseProgram() throws ParseException {
        parseStmtList();
        // opcional: checar que consumiu todos tokens
        if (!peekToken().Class.equals("EOF")) {
            ItemTableSymbol t = peekToken();
            throw new ParseException("Token extra após fim do programa: '" + t.Value + "' na posição " + pos);
        }
    }

    // <stmt_list> ::= { <stmt> }
    private void parseStmtList() throws ParseException {
        while (!peekToken().Class.equals("EOF") && !peekToken().Value.equals("}")) {
            parseStmt();
        }
    }

    // <stmt> ::= <decl> | <assign> | <if_stmt> | <while_stmt> | <for_stmt> | <print_stmt> | "{" <stmt_list> "}"
    private void parseStmt() throws ParseException {
        ItemTableSymbol t = peekToken();

        if (t.Class.equals("TYPE")) {
            parseDecl();
        } else if (t.Class.equals("IDENTIFY")) {
            parseAssign();
        } else if (t.Class.equals("COMMAND")) {
            // comandos: Se, Enquanto, Para, Imprimir, Senao (Senao será tratado no if)
            if (t.Value.equals("Se")) parseIf();
            else if (t.Value.equals("Enquanto")) parseWhile();
            else if (t.Value.equals("Para")) parseFor();
            else if (t.Value.equals("Imprimir")) parsePrint();
            else throw new ParseException("Comando desconhecido: " + t.Value + " na posição " + pos);
        } else if (t.Value.equals("{")) {
            consumeToken(); // consome '{'
            parseStmtList();
            match("MATH_OPERATOR", "}"); // ou se sua tabela marca "}" de outra forma, adapte
            // se não usa MATH_OPERATOR para '}', verifique a classe real (talvez "}" seja classificada com próprio caractere)
        } else {
            throw new ParseException("Início de sentença inválido: " + t.Value + " (classe=" + t.Class + ") na posição " + pos);
        }
    }

// Declaração: <type> IDENTIFY [ "=" <expression> ] ";"
private void parseDecl() throws ParseException {

    match("TYPE");         // Inteiro / Logico / Caractere
    match("IDENTIFY");     // nome da variável

    // inicialização opcional
    if (peekToken().Value.equals("=")) {
        consumeToken();    // consume '='
        parseExpression(); // lê o valor
    }

    // exige ponto-e-vírgula
    if (peekToken().Value.equals(";")) {
        consumeToken();
    } else {
        throw new ParseException("Esperava ';' ao final da declaração na posição " + pos);
    }
}


    // Atribuição: IDENTIFY '=' <expression> ';'
    private void parseAssign() throws ParseException {
        match("IDENTIFY");
        // '=' provavelmente foi classificado como LOGIC_OPERATOR ou como outro; adapte se necessário.
        ItemTableSymbol t = peekToken();
        if (t.Value.equals("=")) consumeToken();
        else throw new ParseException("Esperava '=' em atribuição na posição " + pos);
        parseExpression();
        if (peekToken().Value.equals(";")) consumeToken();
        else throw new ParseException("Esperava ';' ao final da atribuição na posição " + pos);
    }

    // If: Se ( expr ) stmt [Senao stmt]
    private void parseIf() throws ParseException {
        match("COMMAND", "Se");
        if (!peekToken().Value.equals("(")) throw new ParseException("Esperava '(' após 'Se' na posição " + pos);
        consumeToken(); // '('
        parseExpression();
        if (!peekToken().Value.equals(")")) throw new ParseException("Esperava ')' após condição do 'Se' na posição " + pos);
        consumeToken(); // ')'
        parseStmt();
        if (peekToken().Class.equals("COMMAND") && peekToken().Value.equals("Senao")) {
            consumeToken();
            parseStmt();
        }
    }

    // While: Enquanto ( expr ) stmt
    private void parseWhile() throws ParseException {
        match("COMMAND", "Enquanto");
        if (!peekToken().Value.equals("(")) throw new ParseException("Esperava '(' após 'Enquanto' na posição " + pos);
        consumeToken();
        parseExpression();
        if (!peekToken().Value.equals(")")) throw new ParseException("Esperava ')' após condição do 'Enquanto' na posição " + pos);
        consumeToken();
        parseStmt();
    }

    // For simplificado: Para ( assign_no_semicolon expression ; assign_no_semicolon ) stmt
    private void parseFor() throws ParseException {
        match("COMMAND", "Para");
        if (!peekToken().Value.equals("(")) throw new ParseException("Esperava '(' após 'Para' na posição " + pos);
        consumeToken();
        // assign_no_semicolon: IDENTIFY = expr
        if (!peekToken().Class.equals("IDENTIFY")) throw new ParseException("Esperava identificador no for na posição " + pos);
        match("IDENTIFY");
        if (!peekToken().Value.equals("=")) throw new ParseException("Esperava '=' no for na posição " + pos);
        consumeToken();
        parseExpression();
        if (!peekToken().Value.equals(";")) throw new ParseException("Esperava ';' no for na posição " + pos);
        consumeToken();
        if (!peekToken().Class.equals("IDENTIFY")) throw new ParseException("Esperava identificador no for (2) na posição " + pos);
        match("IDENTIFY");
        if (!peekToken().Value.equals("=")) throw new ParseException("Esperava '=' no for (2) na posição " + pos);
        consumeToken();
        parseExpression();
        if (!peekToken().Value.equals(")")) throw new ParseException("Esperava ')' final do for na posição " + pos);
        consumeToken();
        parseStmt();
    }

    // Print: Imprimir ( expr ) ;
    private void parsePrint() throws ParseException {
        match("COMMAND", "Imprimir");
        if (!peekToken().Value.equals("(")) throw new ParseException("Esperava '(' após 'Imprimir' na posição " + pos);
        consumeToken();
        parseExpression();
        if (!peekToken().Value.equals(")")) throw new ParseException("Esperava ')' após expressão em Imprimir na posição " + pos);
        consumeToken();
        if (!peekToken().Value.equals(";")) throw new ParseException("Esperava ';' após Imprimir na posição " + pos);
        consumeToken();
    }

    // ---------- EXPRESSIONS ----------
    // Implementação simplificada com precedência

    private void parseExpression() throws ParseException {
        parseLogicOr();
    }

    private void parseLogicOr() throws ParseException {
        parseLogicAnd();
        while (peekToken().Value.equals("^")) {
            consumeToken();
            parseLogicAnd();
        }
    }

    private void parseLogicAnd() throws ParseException {
        parseRelExpr();
        while (peekToken().Value.equals("&")) {
            consumeToken();
            parseRelExpr();
        }
    }

    private void parseRelExpr() throws ParseException {
        parseArithmetic();
        String v = (String) peekToken().Value;
        if (v.equals("=") || v.equals("<>") || v.equals("<") || v.equals("<=") || v.equals(">") || v.equals(">=")) {
            consumeToken();
            parseArithmetic();
        }
    }

    private void parseArithmetic() throws ParseException {
        parseTerm();
        while (peekToken().Value.equals("+") || peekToken().Value.equals("-")) {
            consumeToken();
            parseTerm();
        }
    }

    private void parseTerm() throws ParseException {
        parseFactor();
        while (peekToken().Value.equals("*") || peekToken().Value.equals("/") || peekToken().Value.equals("%") || peekToken().Value.equals("**")) {
            consumeToken();
            parseFactor();
        }
    }

    private void parseFactor() throws ParseException {
        ItemTableSymbol t = peekToken();
        if (t.Class.equals("NUMERO")) {
            consumeToken();
        } else if (t.Class.equals("IDENTIFY")) {
            consumeToken();
        } else if (t.Class.equals("STRING")) {
            consumeToken();
        } else if (t.Class.equals("CARACTERE")) {
            consumeToken();
        } else if (t.Class.equals("CONST")) { // "verdade" / "mentira"
            consumeToken();
        } else if (t.Value.equals("(")) {
            consumeToken();
            parseExpression();
            if (!peekToken().Value.equals(")")) throw new ParseException("Esperava ')' na expressão na posição " + pos);
            consumeToken();
        } else {
            throw new ParseException("Fator inválido: '" + t.Value + "' (classe=" + t.Class + ") na posição " + pos);
        }
    }
}
