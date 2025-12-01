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

    // pega o token atual e avança (sem checar)
    private ItemTableSymbol eatAny() {
        ItemTableSymbol t = peek();
        pos++;
        return t;
    }

    private boolean check(String classOrValue) {
        ItemTableSymbol t = peek();
        return t.Class.equals(classOrValue) || t.Value.equals(classOrValue);
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

    // DECLARATION -> TYPE VAR_LIST ';'
    private void parseDeclaration() throws ParseException {
        parseType();
        parseVarList();
        eat("SYMBOL"); // ;
    }

    private void parseType() throws ParseException {
        if (isType()) {
            eat("TYPE"); // consome tipo
        } else {
            throw new ParseException("Tipo esperado (Inteiro, Logico, Caractere).");
        }
    }

    // VAR_LIST -> VAR_INIT (',' VAR_INIT)*
    private void parseVarList() throws ParseException {
        parseVarInit();
        while (check(",")) {
            eat(","); // ,
            parseVarInit();
        }
    }

    // VAR_INIT -> IDENTIFIER ( '<-' EXPR )?
    private void parseVarInit() throws ParseException {
        eat("IDENTIFIER");
        if (check("<-")) {
            eat("<-");
            parseExpr();
        }
    }

    // ------------------------ STATEMENTS ------------------------

    private void parseStatement() throws ParseException {

        if (check("IDENTIFIER")) {
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

    // ASSIGNMENT -> IDENTIFIER '<-' EXPR
    private void parseAssignment() throws ParseException {
        eat("IDENTIFIER");
        eat("<-");
        parseExpr();
    }

    // PRINT_STMT -> 'Imprimir' '(' EXPR ')'
    private void parsePrintStmt() throws ParseException {
        eat("COMMAND"); // Imprimir
        eat("(");
        parseExpr();
        eat(")");
    }

    // IF_STMT -> 'Se' EXPR BLOCK ( 'Senao' BLOCK )?
    private void parseIfStmt() throws ParseException {
        eat("COMMAND"); // Se
        parseExpr();
        parseBlock();
        if (check("COMMAND") && peek().Value.equals("Senao")) {
            eat("COMMAND");
            parseBlock();
        }
    }

    // WHILE_STMT -> 'Enquanto' EXPR BLOCK
    private void parseWhileStmt() throws ParseException {
        eat("COMMAND"); // Enquanto
        parseExpr();
        parseBlock();
    }

    /* FOR_STMT -> 'Para' (TYPE)? IDENTIFIER 'em' '(' EXPR ',' EXPR ',' EXPR ')' BLOCK
       Note: lexer emits 'em' as COMMAND with value "em" */
    private void parseForStmt() throws ParseException {
        eat("COMMAND"); // Para

        // tipo opcional
        if (check("TYPE")) {
            eat("TYPE");
        }

        // variável do loop
        eat("IDENTIFIER");

        // 'em' (comando)
        eat("COMMAND"); // em
        eat("(");
        parseExpr(); // início
        eat(",");    // ,
        parseExpr(); // fim
        eat(",");    // ,
        parseExpr(); // passo
        eat(")");
        parseBlock();
    }

    // BLOCK -> '{' (TOP_ITEM)* '}'
    private void parseBlock() throws ParseException {
        eat("SYMBOL"); // {
        while (!check("SYMBOL") || !peek().Value.equals("}")) {
            if (check("EOF")) throw new ParseException("Fim inesperado do arquivo dentro de bloco.");
            parseTopItem();
        }
        eat("SYMBOL"); // }
    }

    // ------------------------ EXPRESSÕES (precedência) ------------------------

    private void parseExpr() throws ParseException { parseOr(); }

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
            // operador relacional
            eatAny(); // consume operator (LOGIC_OPERATOR)
            parseAdd();
        }
    }

    private void parseAdd() throws ParseException {
        parseMul();
        while (check("MATH_OPERATOR") && (peek().Value.equals("+") || peek().Value.equals("-"))) {
            eatAny(); // + or -
            parseMul();
        }
    }

    private void parseMul() throws ParseException {
        parseExpo();
        while (check("MATH_OPERATOR") && (peek().Value.equals("*") || peek().Value.equals("/") || peek().Value.equals("%"))) {
            eatAny();
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
            eatAny();
            parseUnary();
        } else {
            parsePrimary();
        }
    }

    private void parsePrimary() throws ParseException {
        ItemTableSymbol t = peek();

        if (t.Class.equals("NUMERO")) { eatAny(); return; }
        if (t.Class.equals("IDENTIFIER")) { eatAny(); return; }
        if (t.Class.equals("STRING")) { eatAny(); return; }
        if (t.Class.equals("CARACTERE")) { eatAny(); return; }
        if (t.Class.equals("CONST")) { eatAny(); return; }

        if (t.Class.equals("SYMBOL") && t.Value.equals("(")) {
            eat("(");
            parseExpr();
            eat(")");
            return;
        }

        throw new ParseException("Expressão primária inválida: " + t.Class + " (" + t.Value + ")");
    }
}
