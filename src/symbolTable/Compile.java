package symbolTable;
import java.util.*;

import lexicalFinal.AnalyzerFinal;
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

public static void main(String[] args) throws ParseException {
    System.out.println("=== INICIO ===\n");

    String arquivoEntrada = readProgram("program1.cf");
    AnalyzerFinal lexer = new AnalyzerFinal(arquivoEntrada);

    // obter tokens
    ArrayList<ItemTableSymbol> tokens = lexer.getTableSymbols();

    // adicionar sentinel EOF (caso não tenha)
    tokens.add(new ItemTableSymbol(-1, "EOF", "EOF", 0));

    // opcional: imprimir tokens
    lexer.printValueTokens();

    // criar parser
    Parser pars = new Parser(tokens);
    try {
        pars.parseProgram();
        System.out.println("Analise sintática concluída: sem erros.");
    } catch (ParseException e) {
        System.out.println("Erro sintático: " + e.getMessage());
    }


    System.out.println("=== FIM ===");
}
}


