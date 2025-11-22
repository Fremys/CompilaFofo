import lexical.Analyzer;

import java.util.*;

import lexialalt.Analyzeralt;
import lexicalFinal.AnalyzerFinal;

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

    // ler programa
    arquivoEntrada = readProgram("program1.cf");

    // Analyzer lexer = new Analyzer(arquivoEntrada);
    AnalyzerFinal lexer = new AnalyzerFinal(arquivoEntrada);

    lexer.printTokens();

    System.out.println("=== FIM ===");
  }
}
