import lexical.Analyzer;

import java.util.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.EOFException;

public class Compile{  

  public static String readProgram(String fileName){
    // definir dados
    String arq = "";
    String path = "./Inputs/" + fileName;

    try{
      // definir buffer de leitura
      BufferedReader br = new BufferedReader(new FileReader(path));
      
      // ler linha a linha
      String line = br.readLine();

      while(line != null){
        arq += line + "\n";
        line = br.readLine();
      }

      br.close();
      
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

    Analyzer lexer = new Analyzer(arquivoEntrada);
    lexer.printTokens();

    System.out.println("=== FIM ===");
  }
}
