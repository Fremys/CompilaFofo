package Parser;

import java.util.ArrayList;
import symbolTable.ItemTableSymbol;

public class Parser {

    private ArrayList<ItemTableSymbol> tokens;
    private int pos = 0;

    public Parser(ArrayList<ItemTableSymbol> tokens) {
        this.tokens = tokens;
    }

    // ------------------------ Utilitários ------------------------

    private ItemTableSymbol peek() {
        if (pos < tokens.size()) return tokens.get(pos);
        return new ItemTableSymbol(-1, "EOF", "EOF", 0);
    }

    private ItemTableSymbol eat(String expectedClass) throws ParseException {
        ItemTableSymbol t = peek();
        if (!t.Class.equals(expectedClass) && !t.Value.equals(expectedClass)) {
            throw new ParseException(
                "Esperado token " + expectedClass +
                " mas encontrado: " + t.Class + " (" + t.Value + ")"
            );
        }
        pos++;
        return t;
    }

    private boolean check(String className) {
        ItemTableSymbol t = peek();
        return t.Class.equals(className) || t.Value.equals(className);
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

        if (check("COMMAND") && peek().Value.equals("Imprimir")) {
            parsePrintStmt();
            eat("SYMBOL"); // ;
            return;
        }

        if (check("COMMAND") && peek().Value.equals("Se")) {
            parseIfStmt();
            return;
        }

        if (check("COMMAND") && peek().Value.equals("Enquanto")) {
            parseWhileStmt();
            return;
        }

        if (check("COMMAND") && peek().Value.equals("Para")) {
            parseForStmt();
            return;
        }

        if (check("SYMBOL") && peek().Value.equals("{")) {
            parseBlock();
            return;
        }

        throw new ParseException("Comando inválido iniciado em: " + peek().Class + " (" + peek().Value + ")");
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
        if (check("COMMAND") && peek().Value.equals("Senao")) {
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

        // Tipo opcional
        if (check("TYPE")) {
            eat("TYPE"); // consome Inteiro, Logico ou Caractere
        }

        // Variável do loop
        eat("IDENTIFY");

        eat("COMMAND"); // em
        eat("(");
        parseExpr(); // limite inicial
        eat(",");
        parseExpr(); // limite final
        eat(",");
        parseExpr(); // passo
        eat(")");
        parseBlock();
    }

    private void parseBlock() throws ParseException {
        eat("SYMBOL"); // {
        while (!check("SYMBOL") || !peek().Value.equals("}")) {
            if (check("EOF")) throw new ParseException("Fim inesperado do arquivo no bloco.");
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
        while (check("LOGIC_OPERATOR") && peek().Value.equals("^")) {
            eat("^");
            parseAnd();
        }
    }

    private void parseAnd() throws ParseException {
        parseRel();
        while (check("LOGIC_OPERATOR") && peek().Value.equals("&")) {
            eat("&");
            parseRel();
        }
    }

    private void parseRel() throws ParseException {
        parseAdd();
        if (check("LOGIC_OPERATOR") &&
            (peek().Value.equals("=") || peek().Value.equals("<>") ||
             peek().Value.equals("<") || peek().Value.equals(">") ||
             peek().Value.equals("<=") || peek().Value.equals(">="))) {
            pos++;
            parseAdd();
        }
    }

    private void parseAdd() throws ParseException {
        parseMul();
        while (check("MATH_OPERATOR") && (peek().Value.equals("+") || peek().Value.equals("-"))) {
            pos++;
            parseMul();
        }
    }

    private void parseMul() throws ParseException {
        parseExpo();
        while (check("MATH_OPERATOR") && (peek().Value.equals("*") || peek().Value.equals("/") || peek().Value.equals("%"))) {
            pos++;
            parseExpo();
        }
    }

    private void parseExpo() throws ParseException {
        parseUnary();
        if (check("MATH_OPERATOR") && peek().Value.equals("**")) {
            eat("**");
            parseExpo(); // right-associative
        }
    }

    private void parseUnary() throws ParseException {
        if (check("MATH_OPERATOR") && (peek().Value.equals("+") || peek().Value.equals("-"))) {
            pos++;
            parseUnary();
        } else {
            parsePrimary();
        }
    }

    private void parsePrimary() throws ParseException {
        ItemTableSymbol t = peek();

        if (t.Class.equals("NUMERO")) { pos++; return; }
        if (t.Class.equals("IDENTIFY")) { pos++; return; }
        if (t.Class.equals("STRING")) { pos++; return; }
        if (t.Class.equals("CARACTERE")) { pos++; return; }
        if (t.Class.equals("CONST")) { pos++; return; }

        if (t.Class.equals("SYMBOL") && t.Value.equals("(")) {
            eat("(");
            parseExpr();
            eat(")");
            return;
        }

        throw new ParseException("Expressão primária inválida: " + t.Class + " (" + t.Value + ")");
    }
}
