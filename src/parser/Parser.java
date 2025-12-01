package parser;

import java.util.ArrayList;

import auxStructures.Token;

public class Parser {

    private ArrayList<Token> tokens;
    private int pos = 0;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    // ------------------------ Utilitários ------------------------

    private Token peek() {
        if (pos < tokens.size()) return tokens.get(pos);
        return new Token( "EOF", "EOF");
    }

    private Token eat(String expectedClass) throws ParseException {
        Token t = peek();
        if (!t.ClassToken.equals(expectedClass) && !t.ValueToken.equals(expectedClass)) {
            throw new ParseException(
                "Esperado token " + expectedClass +
                " mas encontrado: " + t.ClassToken + " (" + t.ClassToken + ")"
            );
        }
        pos++;
        return t;
    }

    private boolean check(String className) {
        Token t = peek();
        return t.ClassToken.equals(className) || t.ValueToken.equals(className);
    }

    // ------------------------ PROGRAM ------------------------

    public void parseProgram() throws ParseException {
        while (!check("EOF")) {
            parseTopItem();
        }
        eat("EOF");
    }

    // ------------------------ TOP_ITEM ------------------------

    private void parseTopItem() throws ParseException {
        if (isType()) {
            parseDeclaration();
        } else {
            parseStatement();
        }
    }

    private boolean isType() {
        return check("TYPE");
    }

    // ------------------------ DECLARATION ------------------------

    private void parseDeclaration() throws ParseException {
        parseType();
        parseVarList();
        eat("SYMBOL"); // ;
    }

    private void parseType() throws ParseException {
        if (isType()) {
            pos++;
        } else {
            throw new ParseException("Tipo esperado (Inteiro, Logico, Caractere).");
        }
    }

    private void parseVarList() throws ParseException {
        parseVarInit();
        while (check(",")) {
            eat(",");
            parseVarInit();
        }
    }

    private void parseVarInit() throws ParseException {
        eat("IDENTIFY");
        if (check("<-")) {
            eat("<-");
            parseExpr();
        }
    }

    // ------------------------ STATEMENTS ------------------------

    private void parseStatement() throws ParseException {

        if (check("IDENTIFY")) {
            parseAssignment();
            eat("SYMBOL"); // ;
            return;
        }

        if (check("COMMAND") && peek().ValueToken.equals("Imprimir")) {
            parsePrintStmt();
            eat("SYMBOL"); // ;
            return;
        }

        if (check("COMMAND") && peek().ValueToken.equals("Se")) {
            parseIfStmt();
            return;
        }

        if (check("COMMAND") && peek().ValueToken.equals("Enquanto")) {
            parseWhileStmt();
            return;
        }

        if (check("COMMAND") && peek().ValueToken.equals("Para")) {
            parseForStmt();
            return;
        }

        if (check("SYMBOL") && peek().ValueToken.equals("{")) {
            parseBlock();
            return;
        }

        throw new ParseException("Comando inválido iniciado em: " + peek().ClassToken + " (" + peek().ValueToken + ")");
    }

    private void parseAssignment() throws ParseException {
        eat("IDENTIFY");
        eat("<-");
        parseExpr();
    }

    private void parsePrintStmt() throws ParseException {
        eat("COMMAND"); // Imprimir
        eat("(");
        parseExpr();
        eat(")");
    }

    private void parseIfStmt() throws ParseException {
        eat("COMMAND"); // Se
        parseExpr();
        parseBlock();
        if (check("COMMAND") && peek().ValueToken.equals("Senao")) {
            eat("COMMAND");
            parseBlock();
        }
    }

    private void parseWhileStmt() throws ParseException {
        eat("COMMAND"); // Enquanto
        parseExpr();
        parseBlock();
    }

    private void parseForStmt() throws ParseException {
        eat("COMMAND"); // Para
        eat("IDENTIFY");
        eat("COMMAND"); // em
        eat("(");
        parseExpr();
        eat(",");
        parseExpr();
        eat(",");
        parseExpr();
        eat(")");
        parseBlock();
    }

    private void parseBlock() throws ParseException {
        eat("SYMBOL"); // {
        while (!check("SYMBOL") || !peek().ValueToken.equals("}")) {
            parseTopItem();
        }
        eat("SYMBOL"); // }
    }

    // ------------------------ EXPRESSÕES ------------------------

    private void parseExpr() throws ParseException {
        parseOr();
    }

    private void parseOr() throws ParseException {
        parseAnd();
        while (check("LOGIC_OPERATOR") && peek().ValueToken.equals("^")) {
            eat("^");
            parseAnd();
        }
    }

    private void parseAnd() throws ParseException {
        parseRel();
        while (check("LOGIC_OPERATOR") && peek().ValueToken.equals("&")) {
            eat("&");
            parseRel();
        }
    }

    private void parseRel() throws ParseException {
        parseAdd();
        if (check("LOGIC_OPERATOR") &&
            (peek().ValueToken.equals("=") || peek().ValueToken.equals("<>") ||
             peek().ValueToken.equals("<") || peek().ValueToken.equals(">") ||
             peek().ValueToken.equals("<=") || peek().ValueToken.equals(">="))) {
            pos++;
            parseAdd();
        }
    }

    private void parseAdd() throws ParseException {
        parseMul();
        while (check("MATH_OPERATOR") && (peek().ValueToken.equals("+") || peek().ValueToken.equals("-"))) {
            pos++;
            parseMul();
        }
    }

    private void parseMul() throws ParseException {
        parseExpo();
        while (check("MATH_OPERATOR") && (peek().ValueToken.equals("*") || peek().ValueToken.equals("/") || peek().ValueToken.equals("%"))) {
            pos++;
            parseExpo();
        }
    }

    private void parseExpo() throws ParseException {
        parseUnary();
        if (check("MATH_OPERATOR") && peek().ValueToken.equals("**")) {
            eat("**");
            parseExpo(); // right-associative
        }
    }

    private void parseUnary() throws ParseException {
        if (check("MATH_OPERATOR") && (peek().ValueToken.equals("+") || peek().ValueToken.equals("-"))) {
            pos++;
            parseUnary();
        } else {
            parsePrimary();
        }
    }

    private void parsePrimary() throws ParseException {
        Token t = peek();

        if (t.ClassToken.equals("NUMERO")) { pos++; return; }
        if (t.ClassToken.equals("IDENTIFY")) { pos++; return; }
        if (t.ClassToken.equals("STRING")) { pos++; return; }
        if (t.ClassToken.equals("CARACTERE")) { pos++; return; }
        if (t.ClassToken.equals("CONST")) { pos++; return; }

        if (t.ClassToken.equals("SYMBOL") && t.ValueToken.equals("(")) {
            eat("(");
            parseExpr();
            eat(")");
            return;
        }

        throw new ParseException("Expressão primária inválida: " + t.ClassToken + " (" + t.ValueToken + ")");
    }
}
