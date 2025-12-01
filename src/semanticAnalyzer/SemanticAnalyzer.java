package semanticAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import auxStructures.Token;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Map;


public class SemanticAnalyzer {

    private final ArrayList<Token> tokens;
    private int pos = 0;

    // pilha de escopos: cada mapa: nome(lowercase) -> tipo ("Inteiro","Logico","Caractere")
    private final Deque<Map<String, String>> scopes = new ArrayDeque<>();

    public SemanticAnalyzer(ArrayList<Token> tokens) {
        this.tokens = tokens;
        scopes.push(new HashMap<>()); // escopo global
    }

    private Token peek() {
        return pos < tokens.size() ? tokens.get(pos) : new Token("EOF", "EOF");
    }

    private Token eat() {
        Token t = peek();
        pos++;
        return t;
    }

    private boolean accept(String clsOrVal) {
        Token t = peek();
        if (t.ClassToken.equals(clsOrVal) || t.ValueToken.equals(clsOrVal)) {
            pos++;
            return true;
        }
        return false;
    }

    private void expect(String clsOrVal) throws SemanticException {
        Token t = peek();
        if (!t.ClassToken.equals(clsOrVal) && !t.ValueToken.equals(clsOrVal)) {
            throw new SemanticException("Esperado " + clsOrVal + " mas encontrado: " + t.ClassToken + " (" + t.ValueToken + ")");
        }
        pos++;
    }

    public void analyze() throws SemanticException {
        while (!peek().ClassToken.equals("EOF")) {
            parseTopItem();
        }
    }

    private void parseTopItem() throws SemanticException {
        Token t = peek();
        if (t.ClassToken.equals("TYPE")) {
            parseDeclaration();
        } else {
            parseStatement();
        }
    }

    // DECLARATION: TYPE VAR_LIST ';'
    private void parseDeclaration() throws SemanticException {
        String tipo = eat().ValueToken.toString(); // "Inteiro"/"Logico"/"Caractere"
        parseVarList(tipo);
        expect("SYMBOL"); // ;
    }

    private void parseVarList(String tipo) throws SemanticException {
        parseVarInit(tipo);
        while (checkValue(",")) {
            eat(); // ,
            parseVarInit(tipo);
        }
    }

    private void parseVarInit(String tipo) throws SemanticException {
        Token id = eat(); // IDENTIFY
        if (!id.ClassToken.equals("IDENTIFY")) throw new SemanticException("Esperado identificador na declaração");
        String name = id.ValueToken.toString().toLowerCase();
        if (currentScopeContains(name)) throw new SemanticException("Variável já declarada neste escopo: " + id.ValueToken);
        // registra sem valor inicial (null) se não houver <-, ou com verificação se houver
        if (checkValue("<-")) {
            eat(); // <-
            String exprType = parseExpr();
            checkTypeCompatibility(tipo, exprType);
            // registra a variável com o tipo declarado
            scopes.peek().put(name, tipo);
        } else {
            // só registra o tipo
            scopes.peek().put(name, tipo);
        }
    }

    // STATEMENTS
    private void parseStatement() throws SemanticException {
        Token t = peek();

        if (t.ClassToken.equals("IDENTIFY")) {
            parseAssignment();
            expect("SYMBOL"); // ;
            return;
        }

        if (t.ClassToken.equals("COMMAND")) {
            String cmd = t.ValueToken.toString();
            switch (cmd) {
                case "Imprimir":
                    parsePrint();
                    expect("SYMBOL"); // ;
                    return;
                case "Se":
                    parseIf();
                    return;
                case "Enquanto":
                    parseWhile();
                    return;
                case "Para":
                    parseFor();
                    return;
                default:
                    throw new SemanticException("Comando inválido: " + cmd);
            }
        }

        if (t.ClassToken.equals("SYMBOL") && t.ValueToken.equals("{")) {
            parseBlock();
            return;
        }

        throw new SemanticException("Comando inválido: " + t.ValueToken);
    }

    // IDENTIFY '<-' EXPR
    private void parseAssignment() throws SemanticException {
        Token id = eat(); // IDENTIFY
        if (!id.ClassToken.equals("IDENTIFY")) throw new SemanticException("Esperado identificador na atribuição");
        String name = id.ValueToken.toString().toLowerCase();
        String varType = lookupVariable(name);
        // consume '<-'
        if (!checkValue("<-")) throw new SemanticException("Esperado '<-' na atribuição de: " + id.ValueToken);
        eat(); // <-
        String exprType = parseExpr();
        checkTypeCompatibility(varType, exprType);
    }

    // Imprimir '(' EXPR ')'
    private void parsePrint() throws SemanticException {
        eat(); // Imprimir
        expect("(");
        // permitir expressões de qualquer tipo (impressão concatenada com + resolvida no tipo)
        parseExpr();
        expect(")");
    }

    // Se EXPR BLOCK (Senao BLOCK)?
    private void parseIf() throws SemanticException {
        eat(); // Se
        String condType = parseExpr();
        if (!condType.equals("Logico")) throw new SemanticException("Condição de 'Se' deve ser Logico, encontrado: " + condType);
        parseBlock();
        if (checkCommandValue("Senao")) {
            eat(); // Senao
            parseBlock();
        }
    }

    // Enquanto EXPR BLOCK
    private void parseWhile() throws SemanticException {
        eat(); // Enquanto
        String condType = parseExpr();
        if (!condType.equals("Logico")) throw new SemanticException("Condição de 'Enquanto' deve ser Logico, encontrado: " + condType);
        parseBlock();
    }

    /*
      Para (TYPE)? IDENTIFY em '(' EXPR ',' EXPR ',' EXPR ')' BLOCK
      se TYPE fornecido: declara variavel local no escopo do for; caso contrário var já deve existir
    */
    private void parseFor() throws SemanticException {
        eat(); // Para

        String declaredType = null;
        if (peek().ClassToken.equals("TYPE")) {
            declaredType = eat().ValueToken.toString();
        }

        Token var = eat(); // IDENTIFY
        if (!var.ClassToken.equals("IDENTIFY")) throw new SemanticException("Esperado identificador no For");
        String varName = var.ValueToken.toString().toLowerCase();

        boolean declaredHere = false;
        if (declaredType != null) {
            if (currentScopeContains(varName)) throw new SemanticException("Variável já declarada no For: " + var.ValueToken);
            scopes.peek().put(varName, declaredType);
            declaredHere = true;
        } else {
            // must exist in some outer scope
            if (!variableExists(varName)) throw new SemanticException("Variável não declarada no For: " + var.ValueToken);
        }

        // 'em'
        if (!checkCommandValue("em")) throw new SemanticException("Esperado 'em' no For");
        eat(); // em

        expect("(");
        String t1 = parseExpr(); // início
        expect(","); 
        String t2 = parseExpr(); // fim
        expect(",");
        String t3 = parseExpr(); // passo
        expect(")");

        // inicio/fim/passo precisam ser Inteiro
        if (!t1.equals("Inteiro") || !t2.equals("Inteiro") || !t3.equals("Inteiro")) {
            throw new SemanticException("Parâmetros do For devem ser Inteiro (inicio, fim, passo)");
        }

        parseBlock();

        if (declaredHere) {
            // remove var local do for
            scopes.peek().remove(varName);
        }
    }

    private void parseBlock() throws SemanticException {
        expect("{");
        // novo escopo
        scopes.push(new HashMap<>());
        while (!checkValue("}")) {
            if (peek().ClassToken.equals("EOF")) throw new SemanticException("Fim inesperado do arquivo dentro de bloco.");
            parseTopItem();
        }
        expect("SYMBOL"); // }
        scopes.pop();
    }

    /* ------------------ EXPRESSÕES: devolvem String com tipo ("Inteiro","Logico","Caractere") ------------------ */

    private boolean checkValue(String val) {
        Token t = peek();
        return t.ValueToken != null && t.ValueToken.equals(val);
    }

    private boolean checkCommandValue(String val) {
        Token t = peek();
        return t.ClassToken.equals("COMMAND") && t.ValueToken != null && t.ValueToken.equals(val);
    }

    private String parseExpr() throws SemanticException { return parseOr(); }

    private String parseOr() throws SemanticException {
        String left = parseAnd();
        while (peek().ClassToken.equals("LOGIC_OPERATOR") && peek().ValueToken.equals("^")) {
            eat(); // ^
            String right = parseAnd();
            if (!left.equals("Logico") || !right.equals("Logico"))
                throw new SemanticException("Operador '^' aplicado em tipos incompatíveis: " + left + ", " + right);
            left = "Logico";
        }
        return left;
    }

    private String parseAnd() throws SemanticException {
        String left = parseRel();
        while (peek().ClassToken.equals("LOGIC_OPERATOR") && peek().ValueToken.equals("&")) {
            eat(); // &
            String right = parseRel();
            if (!left.equals("Logico") || !right.equals("Logico"))
                throw new SemanticException("Operador '&' aplicado em tipos incompatíveis: " + left + ", " + right);
            left = "Logico";
        }
        return left;
    }

    private String parseRel() throws SemanticException {
        String left = parseAdd();
        if (peek().ClassToken.equals("LOGIC_OPERATOR")) {
            String op = peek().ValueToken.toString();
            if (op.equals("=") || op.equals("<>") || op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=")) {
                eat(); // operador
                String right = parseAdd();
                // equality (=, <>) allowed for same-type operands
                if (op.equals("=") || op.equals("<>")) {
                    if (!left.equals(right))
                        throw new SemanticException("Operador '" + op + "' aplicado em tipos incompatíveis: " + left + ", " + right);
                } else {
                    // <,>,<=,>= require Inteiro operands
                    if (!left.equals("Inteiro") || !right.equals("Inteiro"))
                        throw new SemanticException("Operador '" + op + "' aplicado em tipos incompatíveis: " + left + ", " + right);
                }
                return "Logico";
            }
        }
        return left;
    }

    private String parseAdd() throws SemanticException {
        String left = parseMul();
        while (peek().ClassToken.equals("MATH_OPERATOR") && (peek().ValueToken.equals("+") || peek().ValueToken.equals("-"))) {
            String op = eat().ValueToken.toString(); // + or -
            String right = parseMul();
            if (op.equals("+")) {
                // concatenação: se qualquer lado for Caractere -> Caractere
                if (left.equals("Caractere") || right.equals("Caractere")) {
                    left = "Caractere";
                } else if (left.equals("Inteiro") && right.equals("Inteiro")) {
                    left = "Inteiro";
                } else {
                    throw new SemanticException("Operador '+' aplicado em tipos incompatíveis: " + left + ", " + right);
                }
            } else { // '-'
                if (!left.equals("Inteiro") || !right.equals("Inteiro"))
                    throw new SemanticException("Operador '-' aplicado em tipos incompatíveis: " + left + ", " + right);
                left = "Inteiro";
            }
        }
        return left;
    }

    private String parseMul() throws SemanticException {
        String left = parseExpo();
        while (peek().ClassToken.equals("MATH_OPERATOR") && (peek().ValueToken.equals("*") || peek().ValueToken.equals("/") || peek().ValueToken.equals("%"))) {
            String op = eat().ValueToken.toString();
            String right = parseExpo();
            if (!left.equals("Inteiro") || !right.equals("Inteiro")) {
                throw new SemanticException("Operador '" + op + "' aplicado em tipos incompatíveis: " + left + ", " + right);
            }
            left = "Inteiro";
        }
        return left;
    }

    private String parseExpo() throws SemanticException {
        String left = parseUnary();
        if (peek().ClassToken.equals("MATH_OPERATOR") && peek().ValueToken.equals("**")) {
            eat(); // **
            String right = parseExpo();
            if (!left.equals("Inteiro") || !right.equals("Inteiro"))
                throw new SemanticException("Operador '**' aplicado em tipos incompatíveis: " + left + ", " + right);
            return "Inteiro";
        }
        return left;
    }

    private String parseUnary() throws SemanticException {
        if (peek().ClassToken.equals("MATH_OPERATOR") && (peek().ValueToken.equals("+") || peek().ValueToken.equals("-"))) {
            String op = eat().ValueToken.toString();
            String v = parseUnary();
            if (!v.equals("Inteiro")) throw new SemanticException("Operador unário '" + op + "' aplicado em tipo inválido: " + v);
            return "Inteiro";
        } else {
            return parsePrimary();
        }
    }

    private String parsePrimary() throws SemanticException {
        Token t = peek();
        if (t.ClassToken.equals("NUMERO")) {
            eat();
            return "Inteiro";
        }
        if (t.ClassToken.equals("STRING") || t.ClassToken.equals("CARACTERE")) {
            eat();
            return "Caractere";
        }
        if (t.ClassToken.equals("CONST")) {
            eat();
            return "Logico";
        }
        if (t.ClassToken.equals("IDENTIFY")) {
            Token id = eat();
            String name = id.ValueToken.toString().toLowerCase();
            String type = lookupVariable(name);
            return type;
        }
        if (t.ClassToken.equals("SYMBOL") && t.ValueToken.equals("(")) {
            eat(); // (
            String ty = parseExpr();
            expect("SYMBOL"); // )
            return ty;
        }

        throw new SemanticException("Expressão primária inválida: " + t.ClassToken + " (" + t.ValueToken + ")");
    }

    // -------------------- Auxiliares de tipos e tabela de símbolos --------------------

    private void checkTypeCompatibility(String expected, String actual) throws SemanticException {
        if (!expected.equals(actual)) {
            throw new SemanticException("Tipo incompatível: esperado " + expected + ", encontrado " + actual);
        }
    }

    private boolean currentScopeContains(String nameLower) {
        return scopes.peek().containsKey(nameLower);
    }

    private boolean variableExists(String nameLower) {
        for (Map<String, String> s : scopes) {
            if (s.containsKey(nameLower)) return true;
        }
        return false;
    }

    private String lookupVariable(String nameLower) throws SemanticException {
        for (Map<String, String> s : scopes) {
            if (s.containsKey(nameLower)) return s.get(nameLower);
        }
        throw new SemanticException("Variável não declarada: " + nameLower);
    }
}