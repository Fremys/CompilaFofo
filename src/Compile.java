import java.util.*;

import lexicalFinal.AnalyzerFinal;
import auxStructures.*;
import syntatic.ParseException;
import syntatic.Parser;

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
    ArrayList<ItemTableSymbol> tabelaSimbolos = new ArrayList<ItemTableSymbol>();
    ArrayList<Token> tokens = new ArrayList<Token>();

    // ler programa
    arquivoEntrada = readProgram("program1.cf");

    // Analyzer lexer = new Analyzer(arquivoEntrada);
    AnalyzerFinal lexer = new AnalyzerFinal(arquivoEntrada);

    tabelaSimbolos = lexer.getTableSymbols(); // resgatar a tabela de simbolos
    tokens = lexer.getTokens(); // resgatar os tokens gerados

    // adicionar sentinel EOF (caso não tenha)
    tokens.add(new Token("EOF", "EOF"));

    // lexer.printTokens("class");

    // criar parser
    Parser parser = new Parser(tokens);
    try {
        parser.parseProgram();
        System.out.println("Analise sintática concluída: sem erros.");
    } catch (ParseException e) {
        System.out.println("Erro sintático: " + e.getMessage());
    }

    // System.out.println("=== FIM ===");
  }
}
