package symbolTable;
import java.util.*;

import lexicalFinal.AnalyzerFinal;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Compile {  

    public static String readProgram(String fileName){
        String arq = "";
        String path = "./Inputs/" + fileName;

        try {
            arq = Files.readString(Paths.get(path));
        } catch(Exception e) {
            System.out.println("Erro ao ler arquivo: " + e.getMessage());
        }

        return arq;
    }

    public static void main(String[] args) {

        System.out.println("<INICIO>\n");

        // ----------------------------------------------------
        // 1. LEITURA DO ARQUIVO
        // ----------------------------------------------------
        String arquivoEntrada = readProgram("program1.cf");

        // ----------------------------------------------------
        // 2. ANÁLISE LÉXICA
        // ----------------------------------------------------
        System.out.println("<Iniciando análise léxica...>\n");

        AnalyzerFinal lexer = new AnalyzerFinal(arquivoEntrada);

        // tokens gerados
        ArrayList<ItemTableSymbol> tokens = lexer.getTableSymbols();

        // imprimir tokens
        lexer.printValueTokens();

        // verificar se existe erro léxico
        boolean lexicoOK = lexer.hasLexicalError() == false;

        if (lexicoOK) {
            System.out.println("\n<Análise léxica concluída: nenhum erro encontrado.>");
        } else {
            System.out.println("\n<Erros encontrados na análise léxica!>");
            System.out.println("<Encerrando antes da análise sintática.>\n");
            System.out.println("<FIM>");
            return; // não tenta fazer parser se o léxico falhou
        }

        // ----------------------------------------------------
        // 3. ANÁLISE SINTÁTICA
        // ----------------------------------------------------
        System.out.println("\n<Iniciando análise sintática...>\n");

        Parser pars = new Parser(tokens);

        try {
            pars.parseProgram();
            System.out.println("\n<Análise sintática concluída: sem erros.>");
        } catch (ParseException e) {
            System.out.println("\n<Erro sintático: " + e.getMessage() + ">");
        }

        System.out.println("\n<FIM>");
    }
}
