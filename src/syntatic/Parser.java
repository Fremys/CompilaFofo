package syntatic;

import java.util.ArrayList;

import auxStructures.*;

public class Parser {

    private ArrayList<Token> tokens;
    private int pos = 0;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    // utilitários
    private Token peekToken() {
        if (pos < tokens.size()) return tokens.get(pos);
        // token sentinel de fim
        return new Token( "EOF", "EOF");
    }

    private Token consumeToken() {
        Token t = peekToken();
        pos++;
        return t;
    }

    // Match por classe (ex.: "IDENTIFY") e opcionalmente por valor (ex.: "Se")
    private Token match(String expectedClass) throws ParseException {
        Token t = peekToken();
        if (t.ClassToken.equals(expectedClass)) {
            return consumeToken();
        }
        throw new ParseException("Esperava token da classe '" + expectedClass + "', encontrado '" + t.ClassToken + "' (valor='" + t.ValueToken + "') na posição " + pos);
    }

    private Token match(String expectedClass, String expectedValue) throws ParseException {
        Token t = peekToken();
        if (t.ClassToken.equals(expectedClass) && t.ValueToken.equals(expectedValue)) {
            return consumeToken();
        }
        throw new ParseException("Esperava '" + expectedValue + "', encontrado '" + t.ValueToken + "' (classe='" + t.ClassToken + "') na posição " + pos);
    }

    // entry point
    public void parseProgram() throws ParseException {
        parseStmtList();
        // opcional: checar que consumiu todos tokens
        if (!peekToken().ClassToken.equals("EOF")) {
            Token t = peekToken();
            throw new ParseException("Token extra após fim do programa: '" + t.ValueToken + "' na posição " + pos);
        }
    }

    // <stmt_list> ::= { <stmt> }
    private void parseStmtList() throws ParseException {
        while (!peekToken().ClassToken.equals("EOF") && !peekToken().ValueToken.equals("}")) {
            parseStmt();
        }
    }

    private void parseList() throws ParseException {

        boolean needContinue = false;
        do{
            Token t = peekToken();
            
            parseArithmetic();
            
            if(peekToken().ValueToken.equals(",")){
                consumeToken(); // consome a vírgula e continua
                needContinue = true;
            } else {
                needContinue = false; // sai do loop
            }
            
            
        }while (!peekToken().ClassToken.equals("EOF") && !peekToken().ValueToken.equals(")") && needContinue);
    }

    // <stmt> ::= <decl> | <assign> | <if_stmt> | <while_stmt> | <for_stmt> | <print_stmt> | "{" <stmt_list> "}"
    private void parseStmt() throws ParseException {
        Token t = peekToken();

        if (t.ClassToken.equals("TYPE")) {
            parseDecl();
        } else if (t.ClassToken.equals("IDENTIFY")) {
            parseAssign();
        } else if (t.ClassToken.equals("COMMAND")) {
            // comandos: Se, Enquanto, Para, Imprimir, Senao (Senao será tratado no if)
            if (t.ValueToken.equals("Se")) parseIf();
            else if (t.ValueToken.equals("Enquanto")) parseWhile();
            else if (t.ValueToken.equals("Para")) parseFor();
            else if (t.ValueToken.equals("Imprimir")) parsePrint();
            else throw new ParseException("Comando desconhecido: " + t.ValueToken + " na posição " + pos);
        } else if (t.ValueToken.equals("{")) {
            consumeToken(); // consome '{'
            parseStmtList();
            match("}", "}"); // ou se sua tabela marca "}" de outra forma, adapte
            // se não usa MATH_OPERATOR para '}', verifique a classe real (talvez "}" seja classificada com próprio caractere)
        } else {
            throw new ParseException("Início de sentença inválido: " + t.ValueToken + " (classe=" + t.ClassToken + ") na posição " + pos);
        }
    }

// Declaração: <type> IDENTIFY [ "=" <expression> ] ";"
private void parseDecl() throws ParseException {

    match("TYPE");         // Inteiro / Logico / Caractere
    match("IDENTIFY");     // nome da variável

    // inicialização opcional
    if (peekToken().ValueToken.equals("<-")) {
        consumeToken();    // consume '='
        parseExpression(); // lê o valor
    }

    // exige ponto-e-vírgula
    if (peekToken().ValueToken.equals(";")) {
        consumeToken();
    } else {
        throw new ParseException("Esperava ';' ao final da declaração na posição " + pos);
    }
}


    // Atribuição: IDENTIFY '=' <expression> ';'
    private void parseAssign() throws ParseException {
        match("IDENTIFY");
        // '=' provavelmente foi classificado como LOGIC_OPERATOR ou como outro; adapte se necessário.
        Token t = peekToken();
        if (t.ValueToken.equals("=")) consumeToken();
        else throw new ParseException("Esperava '=' em atribuição na posição " + pos);
        parseExpression();
        if (peekToken().ValueToken.equals(";")) consumeToken();
        else throw new ParseException("Esperava ';' ao final da atribuição na posição " + pos);
    }

    // If: Se ( expr ) stmt [Senao stmt]
    private void parseIf() throws ParseException {
        match("COMMAND", "Se");
        if (!peekToken().ValueToken.equals("(")) throw new ParseException("Esperava '(' após 'Se' na posição " + pos);
        consumeToken(); // '('
        parseExpression();
        if (!peekToken().ValueToken.equals(")")) throw new ParseException("Esperava ')' após condição do 'Se' na posição " + pos);
        consumeToken(); // ')'
        parseStmt();
        if (peekToken().ClassToken.equals("COMMAND") && peekToken().ValueToken.equals("Senao")) {
            consumeToken();
            parseStmt();
        }
    }

    // While: Enquanto ( expr ) stmt
    private void parseWhile() throws ParseException {
        match("COMMAND", "Enquanto");
        if (!peekToken().ValueToken.equals("(")) throw new ParseException("Esperava '(' após 'Enquanto' na posição " + pos);
        consumeToken();
        parseExpression();
        if (!peekToken().ValueToken.equals(")")) throw new ParseException("Esperava ')' após condição do 'Enquanto' na posição " + pos);
        consumeToken();
        parseStmt();
    }

    // For simplificado: Para ( assign_no_semicolon expression ; assign_no_semicolon ) stmt
    private void parseFor() throws ParseException {
        match("COMMAND", "Para");
        // assign_no_semicolon: IDENTIFY = expr
        if (!peekToken().ClassToken.equals("IDENTIFY")) throw new ParseException("Esperava identificador no for na posição " + pos);
        match("IDENTIFY");
        
        
        if (!peekToken().ValueToken.equals("em")) throw new ParseException("Esperava 'em' após 'Para' na posição " + pos);
        consumeToken();


        if (!peekToken().ValueToken.equals("(")) throw new ParseException("Esperava '(' após 'Para' na posição " + pos);
        consumeToken();

        parseList();

        if (!peekToken().ValueToken.equals(")")) throw new ParseException("Esperava ')' final do for na posição " + pos);
        consumeToken();
        parseStmt();
    }

    // Print: Imprimir ( expr ) ;
    private void parsePrint() throws ParseException {
        match("COMMAND", "Imprimir");
        if (!peekToken().ValueToken.equals("(")) throw new ParseException("Esperava '(' após 'Imprimir' na posição " + pos);
        consumeToken();
        parseExpression();
        if (!peekToken().ValueToken.equals(")")) throw new ParseException("Esperava ')' após expressão em Imprimir na posição " + pos);
        consumeToken();
        if (!peekToken().ValueToken.equals(";")) throw new ParseException("Esperava ';' após Imprimir na posição " + pos);
        consumeToken();
    }

    // ---------- EXPRESSIONS ----------
    // Implementação simplificada com precedência

    private void parseExpression() throws ParseException {
        parseLogicOr();
    }

    private void parseLogicOr() throws ParseException {
        parseLogicAnd();
        while (peekToken().ValueToken.equals("^")) {
            consumeToken();
            parseLogicAnd();
        }
    }

    private void parseLogicAnd() throws ParseException {
        parseRelExpr();
        while (peekToken().ValueToken.equals("&")) {
            consumeToken();
            parseRelExpr();
        }
    }

    private void parseRelExpr() throws ParseException {
        parseArithmetic();
        String v = (String) peekToken().ValueToken;
        if (v.equals("=") || v.equals("<>") || v.equals("<") || v.equals("<=") || v.equals(">") || v.equals(">=")) {
            consumeToken();
            parseArithmetic();
        }
    }

    private void parseArithmetic() throws ParseException {
        parseTerm();
        while (peekToken().ValueToken.equals("+") || peekToken().ValueToken.equals("-")) {
            consumeToken();
            parseTerm();
        }
    }

    private void parseTerm() throws ParseException {
        parseFactor();
        while (peekToken().ValueToken.equals("*") || peekToken().ValueToken.equals("/") || peekToken().ValueToken.equals("%") || peekToken().ValueToken.equals("**")) {
            consumeToken();
            parseFactor();
        }
    }

    private void parseFactor() throws ParseException {
        Token t = peekToken();
        if (t.ClassToken.equals("NUMERO")) {
            consumeToken();
        } else if (t.ClassToken.equals("IDENTIFY")) {
            consumeToken();
        } else if (t.ClassToken.equals("STRING")) {
            consumeToken();
        } else if (t.ClassToken.equals("CARACTERE")) {
            consumeToken();
        } else if (t.ClassToken.equals("CONST")) { // "verdade" / "mentira"
            consumeToken();
        } else if (t.ValueToken.equals("(")) {
            consumeToken();
            parseExpression();
            if (!peekToken().ValueToken.equals(")")) throw new ParseException("Esperava ')' na expressão na posição " + pos);
            consumeToken();
        } else {
            throw new ParseException("Fator inválido: '" + t.ValueToken + "' (classe=" + t.ClassToken + ") na posição " + pos);
        }
    }
}
