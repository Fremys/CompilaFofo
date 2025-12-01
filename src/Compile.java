import java.util.*;

import auxStructures.*;
import lexicalFinal.*;
import parser.*;
import semanticAnalyzer.*;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Compile{  

  public static String readProgram(String fileName){
    // definir dados
    String arq = "";
    String path = "./Inputs/" + fileName;

    try{
      arq = Files.readString(Paths.get(path));
    } catch(Exception e){
      System.out.println("Error reading file: " + e.getMessage());
    }

    return arq;
  }

  public static void main(String[] args) {

    System.out.println("=== INICIO ===\n");

    // definir dados
    String arquivoEntrada = ""; 
    ArrayList<Token> tokens = new ArrayList<Token>();

    // ler programa
    arquivoEntrada = readProgram("program0.cf");
    boolean canGoNext = false;


    // Analyzer lexer = new Analyzer(arquivoEntrada);
    AnalyzerFinal lexer = new AnalyzerFinal(arquivoEntrada);
    try {
      lexer.LexicalAnalyzer(arquivoEntrada); // executar análise léxica
      tokens = lexer.getTokens(); // resgatar os tokens gerados
      tokens.add(new Token("EOF", "EOF")); // adicionar sentinel EOF 
      System.out.println("Analise sintática concluída: sem erros.");
      canGoNext = true;
    }
    catch (Exception e) {
      System.out.println("Erro léxico: " + e.getMessage());
      canGoNext = false;
    }

    if(canGoNext) {
      // criar parser
      Parser parser = new Parser(tokens);
      try {
        parser.parseProgram();
        System.out.println("\nAnalise sintática concluída: sem erros.");
        canGoNext = true;
      } catch (ParseException e) {
        System.out.println("\nErro sintático: " + e.getMessage());
        canGoNext = false;
      }
      
    } 

    if (canGoNext) {
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(tokens);
        try {
            semanticAnalyzer.analyze();
            System.out.println("\nAnalise semântica concluída: sem erros.");
        } catch (SemanticException e) {
            System.out.println("\nErro semântico: " + e.getMessage());
        }
    }

    System.out.println("\n=== FIM ===");
  }
}
