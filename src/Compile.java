import java.util.*;

import auxStructures.*;
// import Backup.Parser;
import lexicalFinal.AnalyzerFinal;
// import syntatic.ParseException;
import parser.*;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.EOFException;

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
    arquivoEntrada = readProgram("program1.cf");


    // Analyzer lexer = new Analyzer(arquivoEntrada);
    AnalyzerFinal lexer = new AnalyzerFinal(arquivoEntrada);
    try {
      lexer.LexicalAnalyzer(arquivoEntrada); // executar análise léxica
      tokens = lexer.getTokens(); // resgatar os tokens gerados
      tokens.add(new Token("EOF", "EOF")); // adicionar sentinel EOF 
    }
    catch (Exception e) {
      System.out.println("Erro léxico: " + e.getMessage());
    }

    if(tokens.size() > 0 && !tokens.get(0).ClassToken.equals("ERROR")) {
      // criar parser
      Parser parser = new Parser(tokens);
      try {
        parser.parseProgram();
        System.out.println("Analise sintática concluída: sem erros.");
      } catch (ParseException e) {
        System.out.println("Erro sintático: " + e.getMessage());
      }
      
    } 
    System.out.println("\n=== FIM ===");
  }
}
