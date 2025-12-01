package SemanticAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import symbolTable.ItemTableSymbol;

public class SemanticAnalyzer {

    private final ArrayList<ItemTableSymbol> tokens;
    private int pos = 0;

    // Tabela de símbolos com tipos das variáveis
    private final HashMap<String, String> symbolTable = new HashMap<>();

    public SemanticAnalyzer(ArrayList<ItemTableSymbol> tokens) {
        this.tokens = tokens;
    }

    private ItemTableSymbol peek() {
        return pos < tokens.size() ? tokens.get(pos) : new ItemTableSymbol(-1, "EOF", "EOF", 0);
    }

    private ItemTableSymbol eat() {
        ItemTableSymbol t = peek();
        pos++;
        return t;
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

    private void parseDeclaration() throws SemanticException {
        String type = eat().Value.toString(); // Tipo
        parseVarList(type);
        if (!peek().Value.equals(";"))
            throw new SemanticException("Esperado ';' após declaração");
        eat(); // ;
    }

    private void parseVarList(String type) throws SemanticException {
        parseVarInit(type);
        while (peek().Value.equals(",")) {
            eat(); // ,
            parseVarInit(type);
        }
    }

    private void parseVarInit(String type) throws SemanticException {
        ItemTableSymbol var = eat(); // IDENTIFY
        if (symbolTable.containsKey(var.Value.toString()))
            throw new SemanticException("Variável já declarada: " + var.Value);
        symbolTable.put(var.Value.toString(), type);

        if (peek().Value.equals("<-")) {
            eat(); // <-
            ItemTableSymbol expr = parseExpr();
            checkTypeCompatibility(type, expr);
        }
    }

    private void parseStatement() throws SemanticException {
        ItemTableSymbol t = peek();

        switch (t.Class) {
            case "IDENTIFY":
                parseAssignment();
                if (!peek().Value.equals(";"))
                    throw new SemanticException("Esperado ';' após atribuição");
                eat(); // ;
                break;

            case "COMMAND":
                switch (t.Value.toString()) {
                    case "Imprimir":
                        parsePrint();
                        if (!peek().Value.equals(";"))
                            throw new SemanticException("Esperado ';' após comando Imprimir");
                        eat(); // ;
                        break;
                    case "Se":
                        parseIf();
                        break;
                    case "Enquanto":
                        parseWhile();
                        break;
                    case "Para":
                        parseFor();
                        break;
                    default:
                        throw new SemanticException("Comando inválido: " + t.Value);
                }
                break;

            case "SYMBOL":
                if (t.Value.equals("{")) parseBlock();
                else throw new SemanticException("Comando inválido: " + t.Value);
                break;

            default:
                throw new SemanticException("Comando inválido: " + t.Value);
        }
    }

    private void parseAssignment() throws SemanticException {
        ItemTableSymbol var = eat(); // IDENTIFY
        if (!symbolTable.containsKey(var.Value.toString()))
            throw new SemanticException("Variável não declarada: " + var.Value);

        eat(); // <-
        ItemTableSymbol expr = parseExpr();

        String type = symbolTable.get(var.Value.toString());
        checkTypeCompatibility(type, expr);
    }

    private void parsePrint() throws SemanticException {
        eat(); // Imprimir
        if (!peek().Value.equals("("))
            throw new SemanticException("Esperado '(' após Imprimir");
        eat(); // (
        parseExpr();
        if (!peek().Value.equals(")"))
            throw new SemanticException("Esperado ')' após Imprimir");
        eat(); // )
    }

    private void parseIf() throws SemanticException {
        eat(); // Se
        ItemTableSymbol cond = parseExpr();
        if (!isBoolean(cond))
            throw new SemanticException("Condição do 'Se' deve ser lógica");
        parseBlock();
        if (peek().Class.equals("COMMAND") && peek().Value.equals("Senao")) {
            eat(); // Senao
            parseBlock();
        }
    }

    private void parseWhile() throws SemanticException {
        eat(); // Enquanto
        ItemTableSymbol cond = parseExpr();
        if (!isBoolean(cond))
            throw new SemanticException("Condição do 'Enquanto' deve ser lógica");
        parseBlock();
    }

    private void parseFor() throws SemanticException {
    eat(); // Para

    ItemTableSymbol tipo = null;

    // Tipo opcional
    if (peek().Class.equals("TYPE")) {
        tipo = eat(); // Inteiro, Logico ou Caractere
    }

    // Variável do loop
    ItemTableSymbol var = eat(); // IDENTIFY
    if (tipo != null) {
        // Declara variável no escopo do for
        if (symbolTable.containsKey(var.Value.toString()))
            throw new SemanticException("Variável já declarada no For: " + var.Value);
        symbolTable.put(var.Value.toString(), tipo.Value.toString());
    } else {
        // Sem tipo declarado, a variável deve existir
        if (!symbolTable.containsKey(var.Value.toString()))
            throw new SemanticException("Variável não declarada no For: " + var.Value);
    }

    // resto da sintaxe
    eat(); // em
    if (!peek().Value.equals("(")) throw new SemanticException("Esperado '(' no For");
    eat(); // (
    parseExpr(); // início
    if (!peek().Value.equals(",")) throw new SemanticException("Esperado ',' no For");
    eat(); // ,
    parseExpr(); // fim
    if (!peek().Value.equals(",")) throw new SemanticException("Esperado ',' no For");
    eat(); // ,
    parseExpr(); // passo
    if (!peek().Value.equals(")")) throw new SemanticException("Esperado ')' no For");
    eat(); // )
    parseBlock();

    // Remove variável do for do escopo se tiver sido declarada localmente
    if (tipo != null) {
        symbolTable.remove(var.Value.toString());
    }
}


    private void parseBlock() throws SemanticException {
        if (!peek().Value.equals("{")) throw new SemanticException("Esperado '{'");
        eat(); // {
        while (!peek().Value.equals("}")) {
            parseTopItem();
        }
        eat(); // }
    }

    private ItemTableSymbol parseExpr() throws SemanticException {
        // Aqui você pode adaptar para parseOr / parseAnd / parseRel etc.
        // Para simplificar, retornaremos o token atual como "tipo inferido"
        ItemTableSymbol t = eat();
        if (t.Class.equals("NUMERO")) return new ItemTableSymbol(0, "TipoInferido", t.asInteger(), 0);
        if (t.Class.equals("CONST")) return new ItemTableSymbol(0, "TipoInferido", t.asBoolean(), 0);
        if (t.Class.equals("STRING") || t.Class.equals("CARACTERE")) return new ItemTableSymbol(0, "TipoInferido", t.asString(), 0);
        if (t.Class.equals("IDENTIFY")) {
            String name = t.Value.toString();
            if (!symbolTable.containsKey(name))
                throw new SemanticException("Variável não declarada: " + name);
            String type = symbolTable.get(name);
            return new ItemTableSymbol(0, "TipoInferido", type.equals("Inteiro") ? 0 : type.equals("Logico") ? true : "", 0);
        }
        return t;
    }

    private void checkTypeCompatibility(String varType, ItemTableSymbol expr) throws SemanticException {
        switch (varType) {
            case "Inteiro":
                if (!(expr.Value instanceof Integer))
                    throw new SemanticException("Atribuição inválida: esperado Inteiro");
                break;
            case "Logico":
                if (!(expr.Value instanceof Boolean))
                    throw new SemanticException("Atribuição inválida: esperado Logico");
                break;
            case "Caractere":
                if (!(expr.Value instanceof String))
                    throw new SemanticException("Atribuição inválida: esperado Caractere");
                break;
        }
    }

    private boolean isBoolean(ItemTableSymbol expr) {
        return expr.Value instanceof Boolean;
    }
}
