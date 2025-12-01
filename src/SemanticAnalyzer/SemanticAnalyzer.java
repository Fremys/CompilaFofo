package SemanticAnalyzer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import symbolTable.ItemTableSymbol;

/**
 * Semantic analyzer:
 * - pilha de escopos (Deque<Map<String,String>>) para suportar escopos (for com declaração local, blocos)
 * - nomes de variáveis tratados case-insensitively (armazenados em lower-case)
 * - inferência de tipo nas expressões (retorna "Inteiro"|"Logico"|"Caractere")
 * - compatibilidade: aritméticos -> Inteiro; %, /, * etc -> Inteiro; relational -> Logico; &,^ -> Logico
 * - operador + permite concatenação: se qualquer lado for Caractere => Caractere; se ambos Inteiro => Inteiro.
 */
public class SemanticAnalyzer {

    private final ArrayList<ItemTableSymbol> tokens;
    private int pos = 0;

    // pilha de escopos: cada mapa: nome(lowercase) -> tipo ("Inteiro","Logico","Caractere")
    private final Deque<Map<String, String>> scopes = new ArrayDeque<>();

    public SemanticAnalyzer(ArrayList<ItemTableSymbol> tokens) {
        this.tokens = tokens;
        scopes.push(new HashMap<>()); // escopo global
    }

    private ItemTableSymbol peek() {
        return pos < tokens.size() ? tokens.get(pos) : new ItemTableSymbol(-1, "EOF", "EOF", 0);
    }

    private ItemTableSymbol eat() {
        ItemTableSymbol t = peek();
        pos++;
        return t;
    }

    private boolean accept(String clsOrVal) {
        ItemTableSymbol t = peek();
        if (t.Class.equals(clsOrVal) || t.Value.equals(clsOrVal)) {
            pos++;
            return true;
        }
        return false;
    }

    private void expect(String clsOrVal) throws SemanticException {
        ItemTableSymbol t = peek();
        if (!t.Class.equals(clsOrVal) && !t.Value.equals(clsOrVal)) {
            throw new SemanticException("Esperado " + clsOrVal + " mas encontrado: " + t.Class + " (" + t.Value + ")");
        }
        pos++;
    }

    public void analyze() throws SemanticException {
        while (!peek().Class.equals("EOF")) {
            parseTopItem();
        }
    }

    private void parseTopItem() throws SemanticException {
        ItemTableSymbol t = peek();
        if (t.Class.equals("TYPE")) {
            parseDeclaration();
        } else {
            parseStatement();
        }
    }

    // DECLARATION: TYPE VAR_LIST ';'
    private void parseDeclaration() throws SemanticException {
        String tipo = eat().Value.toString(); // "Inteiro"/"Logico"/"Caractere"
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
        ItemTableSymbol id = eat(); // IDENTIFIER
        if (!id.Class.equals("IDENTIFIER")) throw new SemanticException("Esperado identificador na declaração");
        String name = id.Value.toString().toLowerCase();
        if (currentScopeContains(name)) throw new SemanticException("Variável já declarada neste escopo: " + id.Value);
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
        ItemTableSymbol t = peek();

        if (t.Class.equals("IDENTIFIER")) {
            parseAssignment();
            expect("SYMBOL"); // ;
            return;
        }

        if (t.Class.equals("COMMAND")) {
            String cmd = t.Value.toString();
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

        if (t.Class.equals("SYMBOL") && t.Value.equals("{")) {
            parseBlock();
            return;
        }

        throw new SemanticException("Comando inválido: " + t.Value);
    }

    // IDENTIFIER '<-' EXPR
    private void parseAssignment() throws SemanticException {
        ItemTableSymbol id = eat(); // IDENTIFIER
        if (!id.Class.equals("IDENTIFIER")) throw new SemanticException("Esperado identificador na atribuição");
        String name = id.Value.toString().toLowerCase();
        String varType = lookupVariable(name);
        // consume '<-'
        if (!checkValue("<-")) throw new SemanticException("Esperado '<-' na atribuição de: " + id.Value);
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
      Para (TYPE)? IDENTIFIER em '(' EXPR ',' EXPR ',' EXPR ')' BLOCK
      se TYPE fornecido: declara variavel local no escopo do for; caso contrário var já deve existir
    */
    private void parseFor() throws SemanticException {
        eat(); // Para

        String declaredType = null;
        if (peek().Class.equals("TYPE")) {
            declaredType = eat().Value.toString();
        }

        ItemTableSymbol var = eat(); // IDENTIFIER
        if (!var.Class.equals("IDENTIFIER")) throw new SemanticException("Esperado identificador no For");
        String varName = var.Value.toString().toLowerCase();

        boolean declaredHere = false;
        if (declaredType != null) {
            if (currentScopeContains(varName)) throw new SemanticException("Variável já declarada no For: " + var.Value);
            scopes.peek().put(varName, declaredType);
            declaredHere = true;
        } else {
            // must exist in some outer scope
            if (!variableExists(varName)) throw new SemanticException("Variável não declarada no For: " + var.Value);
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
            if (peek().Class.equals("EOF")) throw new SemanticException("Fim inesperado do arquivo dentro de bloco.");
            parseTopItem();
        }
        expect("SYMBOL"); // }
        scopes.pop();
    }

    /* ------------------ EXPRESSÕES: devolvem String com tipo ("Inteiro","Logico","Caractere") ------------------ */

    private boolean checkValue(String val) {
        ItemTableSymbol t = peek();
        return t.Value != null && t.Value.equals(val);
    }

    private boolean checkCommandValue(String val) {
        ItemTableSymbol t = peek();
        return t.Class.equals("COMMAND") && t.Value != null && t.Value.equals(val);
    }

    private String parseExpr() throws SemanticException { return parseOr(); }

    private String parseOr() throws SemanticException {
        String left = parseAnd();
        while (peek().Class.equals("LOGIC_OPERATOR") && peek().Value.equals("^")) {
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
        while (peek().Class.equals("LOGIC_OPERATOR") && peek().Value.equals("&")) {
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
        if (peek().Class.equals("LOGIC_OPERATOR")) {
            String op = peek().Value.toString();
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
        while (peek().Class.equals("MATH_OPERATOR") && (peek().Value.equals("+") || peek().Value.equals("-"))) {
            String op = eat().Value.toString(); // + or -
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
        while (peek().Class.equals("MATH_OPERATOR") && (peek().Value.equals("*") || peek().Value.equals("/") || peek().Value.equals("%"))) {
            String op = eat().Value.toString();
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
        if (peek().Class.equals("MATH_OPERATOR") && peek().Value.equals("**")) {
            eat(); // **
            String right = parseExpo();
            if (!left.equals("Inteiro") || !right.equals("Inteiro"))
                throw new SemanticException("Operador '**' aplicado em tipos incompatíveis: " + left + ", " + right);
            return "Inteiro";
        }
        return left;
    }

    private String parseUnary() throws SemanticException {
        if (peek().Class.equals("MATH_OPERATOR") && (peek().Value.equals("+") || peek().Value.equals("-"))) {
            String op = eat().Value.toString();
            String v = parseUnary();
            if (!v.equals("Inteiro")) throw new SemanticException("Operador unário '" + op + "' aplicado em tipo inválido: " + v);
            return "Inteiro";
        } else {
            return parsePrimary();
        }
    }

    private String parsePrimary() throws SemanticException {
        ItemTableSymbol t = peek();
        if (t.Class.equals("NUMERO")) {
            eat();
            return "Inteiro";
        }
        if (t.Class.equals("STRING") || t.Class.equals("CARACTERE")) {
            eat();
            return "Caractere";
        }
        if (t.Class.equals("CONST")) {
            eat();
            return "Logico";
        }
        if (t.Class.equals("IDENTIFIER")) {
            ItemTableSymbol id = eat();
            String name = id.Value.toString().toLowerCase();
            String type = lookupVariable(name);
            return type;
        }
        if (t.Class.equals("SYMBOL") && t.Value.equals("(")) {
            eat(); // (
            String ty = parseExpr();
            expect("SYMBOL"); // )
            return ty;
        }

        throw new SemanticException("Expressão primária inválida: " + t.Class + " (" + t.Value + ")");
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
