package symbolTable;

import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import lexicalFinal.AnalyzerFinal;
import Parser.Parser;
import Parser.ParseException;
import SemanticAnalyzer.SemanticAnalyzer;
import SemanticAnalyzer.SemanticException;

public class Compile {

    public static String readProgram(String fileName) {
        String path = "./Inputs/" + fileName;
        try {
            return Files.readString(Paths.get(path));
        } catch (Exception e) {
            System.out.println("Erro ao ler arquivo: " + e.getMessage());
            return "";
        }
    }

    public static void main(String[] args) {
        System.out.println("<INICIO>\n");

        // 1. Leitura do arquivo
        String arquivoEntrada = readProgram("program1.cf");

        // 2. Análise léxica
        System.out.println("<Iniciando análise léxica...>\n");
        AnalyzerFinal lexer = new AnalyzerFinal(arquivoEntrada);
        ArrayList<ItemTableSymbol> tokens = lexer.getTableSymbols();

        System.out.println("Tokens gerados:");
        lexer.printValueTokens();

        if (lexer.hasLexicalError()) {
            System.out.println("\n<Erros encontrados na análise léxica!>");
            System.out.println("<Encerrando antes da análise sintática.>\n");
            System.out.println("<FIM>");
            return;
        } else {
            System.out.println("\n<Análise léxica concluída: nenhum erro encontrado.>");
        }

        // 3. Análise sintática
        System.out.println("\n<Iniciando análise sintática...>\n");
        Parser pars = new Parser(tokens);
        boolean sintaticoOK = false;
        try {
            pars.parseProgram();
            sintaticoOK = true;
            System.out.println("\n<Análise sintática concluída: sem erros.>");
        } catch (ParseException e) {
            System.out.println("\n<Erro sintático: " + e.getMessage() + ">");
        }

        // 4. Análise semântica
        if (sintaticoOK) {
            System.out.println("\n<Iniciando análise semântica...>\n");
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(tokens);
            try {
                semanticAnalyzer.analyze();
                System.out.println("\n<Análise semântica concluída: sem erros.>");
            } catch (SemanticException e) {
                System.out.println("\n<Erro semântico: " + e.getMessage() + ">");
            }
        }

        System.out.println("\n<FIM>");
    }
}
