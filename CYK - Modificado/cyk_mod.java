/************************************
    * TP2 FTC
    * @author Maguirrichard Oliveira e Pedro Pimenta
    * @version 1 02/2018.
    *********************************/

import java.io.FileReader;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

class cyk_mod
{

  public static void main(String[] args)
  {
     Scanner arq;
     Scanner str = new Scanner(System.in);
     String frase = "";
     String nomeArq;
     System.out.println("Informe o arquivo contendo a gramática: ");
     nomeArq = str.nextLine(); 
     Producao[] gramatica = ler(nomeArq);
     String nulavel = setarNulaveis(gramatica);
     Relacao[] unit = setUnitario(gramatica, nulavel);
     System.out.println("Forneça as sentenças (digite FIM para encerrar):");
     frase = str.nextLine();
     while(!frase.equals("FIM"))
     {
        System.out.println(parser(gramatica, unit, frase));
        frase = str.nextLine();
     }
  }

  public static String parser(Producao[] gramatica, Relacao[] unit , String palavra)
  {
     String [][] tabela = new String [palavra.length()+1][palavra.length()+1];
     String [][] tabela2 = new String [palavra.length()+1][palavra.length()+1];
   	
     for(int i = 0; i<=palavra.length(); i++)
     {
        for(int j = 0; j <= palavra.length(); j++)
        {
            tabela[i][j] = "";
            tabela2[i][j] = "";
        }
     }
   
     for(int i = 1; i <= palavra.length(); i++)
     {
        int aux = buscaPai("" + palavra.charAt(i-1),unit);
        if(aux!=-1) tabela[i][i] = unit[aux].r2;
        else tabela[i][i] = palavra.charAt(i-1) + "";
     }
   
     for(int j = 2; j <= palavra.length(); j++)
     {
        for(int i = j-1; i>0; i--)
        {
           tabela2 [i][j] = "";
           for(int h = i; h<=j-1; h++)
           {
              for(int k = 0; k < getTamGra(gramatica); k++)
              {
                 for(int p = 0; p < gramatica[k].getTamFil(gramatica[k]); p++)
                 {
                    if(gramatica[k].Filho[p].length() == 2)
                    {
                       if(tabela[i][h].contains(gramatica[k].Filho[p].charAt(0)+"") && 
                          tabela[h+1][j].contains(gramatica[k].Filho[p].charAt(1)+""))
                          tabela2[i][j] = ""+gramatica[k].Pai; 
                    }
                 }
              }
           }
         
           int aux = buscaPai(tabela2[i][j],unit);
         
            if(aux != -1) tabela[i][j] += "" + unit[aux].r2;
            else tabela[i][j] += "" + tabela2[i][j];
        }
     }
      
     if(tabela[1][palavra.length()].contains("" + gramatica[0].Pai))
        return "Pertence";

     return "Nao Pertence";
  }

  public static Producao[] ler(String nomeArq)
  {
     try{
        String str;
        ArrayList<String> ajuda = new ArrayList<String>();
        Scanner arq = new Scanner(new FileReader(nomeArq));      

        while(arq.hasNext())
        {
           str = arq.nextLine(); 
           ajuda.add(str);
        }        
           
        int nFrases = ajuda.size();        	
        Producao[] gramatica;
        gramatica = new Producao[nFrases*3];
        for(int i = 0; i < nFrases*3; i++) gramatica[i] = new Producao();
      
        String[] corte;
        for(int f = 0; f < nFrases; f++)
        {
           str = ajuda.get(f);
           corte = str.split(" ");
           gramatica[f].Pai = corte[0].charAt(0);
         
           for(int p = 1; p < corte.length; p++) gramatica[f].Filho[p-1] = corte[p];       
        }
           
        char c;  
        
        for(int i = 0; i < nFrases; i++)
        {
           for(int j = 0; j < gramatica[i].getTamFil(gramatica[i]); j++)
           {
              if(gramatica[i].Filho[j].length() > 2)
              {
                 do
                 {
                    Random rnd = new Random();
                    c = (char) (rnd.nextInt(26) + 'A'); 
                 }while(buscaPos(c,gramatica) != -1);
                
                 gramatica[nFrases] = new Producao();
                 gramatica[nFrases].Pai = c;
              
                 gramatica[nFrases].Filho[0]= gramatica[i].Filho[j].substring(1);
                 gramatica[i].Filho[j]= gramatica[i].Filho[j].charAt(0)+""+c;
                 nFrases++;
              }
           }
        }

        return gramatica;
     }catch(IOException e){
        System.err.printf("Erro na abertura do arquivo!");
     }

     return null;
  }

  public static String setarNulaveis (Producao[] gramatica)
  {
     String nulaveis = "";
     String todo = "";        
        
     for(int i = 0 ; i<getTamGra(gramatica); i++) gramatica[i].ocorre = "";
        
     for(int i = 0; i < getTamGra(gramatica); i++)
     {
        for(int j = 0; j < gramatica[i].getTamFil(gramatica[i]); j++)
        {
           int p = buscaPos(gramatica[i].Filho[j].charAt(0), gramatica);
         	
           if(gramatica[i].Filho[j].length() == 1 && p != -1)
              gramatica[p].ocorre += gramatica[i].Pai+";"; 
           else if(gramatica[i].Filho[j].length() == 2){
              int p2 = buscaPos(gramatica[i].Filho[j].charAt(1), gramatica);
              if (p != -1 && p2 != -1)
              {
                 gramatica[p].ocorre += gramatica[i].Pai + "" + gramatica[p2].Pai + ";";
                 gramatica[p2].ocorre += gramatica[i].Pai + "" + gramatica[p].Pai + ";";
              }
           }else if (gramatica[i].Filho[j].equals("-")){
              nulaveis+= gramatica[i].Pai; 
              todo+= gramatica[i].Pai; 
           }
        }
     }
        
     while(todo.length() > 0)
     {
        char c = todo.charAt(0);

        if(todo.length() == 1) todo = "";
        else todo = todo.substring(1);
            
        int p = buscaPos(c, gramatica);
      	 
        String s = gramatica[p].ocorre;
      	
        String[] s1 = s.split(";");
        for(int i = 1; i < s1.length; i++)
        {
           int flag = 0;
           String A = s1[i];
                
           if(s1[i].length() > 1)
           {
              A = "" + s1[i].charAt(0);
              if(nulaveis.contains("" + s1[i].charAt(1)) == false) flag = 1;
           }
                
           if(flag == 0 && nulaveis.contains("" + s1[i].charAt(0)) == false)
           {
              nulaveis += "" + A;
              todo += "" + A;
           }
                
         }   
     }
     
     return nulaveis;
  }

  public static Relacao[] setUnitario(Producao[] gramatica, String nulaveis)
  {
     Aresta[] grafo = new Aresta[20];

     for(int i = 0; i < 20; i++) grafo[i] = new Aresta();

     int cont = 0;
     String inicio = "";
     String aux = "";
   	
     for(int i = 0; i < getTamGra(gramatica); i++)
     {
        for(int j = 0; j < gramatica[i].getTamFil(gramatica[i]); j++)
        {
           if(gramatica[i].Filho[j].length() == 1)
           {
              if(gramatica[i].Filho[j].equals("-") == false)
              {
                 grafo[cont].a1 = "" + gramatica[i].Pai;
                 grafo[cont].a2 = "" + gramatica[i].Filho[j];
                 cont++;
              }
           }else{
              String temp = gramatica[i].Filho[j];
              for(int f = 0; f<nulaveis.length(); f++) temp = temp.replaceAll("" + nulaveis.charAt(f), "");
            	
              if(temp.length() == 1 )
              {
                 grafo[cont].a1 = "" + gramatica[i].Pai;
                 grafo[cont].a2 = "" + temp;
                 cont++;
              }
           }
        }
     }
   	
     for(int i = 0; i < getTamAre(grafo); i++)
     {
        if(inicio.contains(grafo[i].a2)==false)
        {
           inicio += "" + grafo[i].a2;
           aux += grafo[i].a1;
        }
     }

     Relacao[] resp = new Relacao[inicio.length()+nulaveis.length()];
     for(int i = 0; i < inicio.length(); i++)
     {
         resp[i] = new Relacao();
         resp[i].r1 = "" + inicio.charAt(i);
     }
      
     for(int i = 0; i < inicio.length(); i++)
     {
        String fazer = "" + inicio.charAt(i);
        while(fazer.equals("") == false)
        {
           String c = "" + fazer.charAt(0);
           if(fazer.length()==1) fazer = "";
           else fazer = fazer.substring(1);
         
           for(int j = 0; j < grafo.length; j++)
           {
              if(grafo[j].a2.equals(c))
              {
                 if(resp[i].r2.contains(c) == false ) resp[i].r2+= c+",";
               	
                 if(resp[i].r2.contains(grafo[j].a1) == false ) resp[i].r2 += grafo[j].a1+",";
               	
                 fazer+=grafo[j].a1;
              }
           }
        }
     }
   			
     for(int i = 0; i < nulaveis.length(); i++)
     {
        resp[i+inicio.length()] =  new Relacao();
        resp[i+inicio.length()].r1 = "" + nulaveis.charAt(i);
        resp[i+inicio.length()].r2 = "" + nulaveis.charAt(i);
     }
    
     return resp;
   }

  public static int buscaPos(char c, Producao[] gramatica)
  {
     for(int i = 0;i < getTamGra(gramatica); i++)
     {
        if(gramatica[i].Pai ==c) return i;
     }
      
     return -1;
  }

  public static int buscaPai(String c, Relacao[] unit)
  {
     for(int i=0;i<unit.length;i++)
     {
        if(unit[i].r1.equals(c)) return i;
     }
     
     return -1;
  }

  public static int getTamAre(Aresta[] grafo)
  {
     int resp=0;	
     for(int i = 0; i<grafo.length; i++)
     {
        if(grafo[i].a1.equals("") == false)  resp++;
     }
     
     return resp;
  }

  public static int getTamGra(Producao[] gramatica)
  {
     int resp=0;	
     for(int i = 0; i<gramatica.length;i++)
     {         	
        String s = ""+gramatica[i].Pai;
        if(s.equals("1")==false) resp++;
     }
      
     return resp;
  }
}

class Aresta
{
  String a1 = "";
  String a2 = "";
}

class Relacao
{
  String r1 = "";
  String r2 = "";
}

class Producao
{
  char Pai = '1';
  String Filho[] = new String[15];
  String ocorre="";
  
  public Producao()
  {
     for(int i = 0; i < 15; i++)
        Filho[i] = "";
  }
   
  int getTamFil(Producao gramatica)
  {
     int resp = 0;	
     for(int i = 0; i<gramatica.Filho.length; i++)
     {
        String s = "" + gramatica.Filho[i];

        if(s.equals("") == false) resp++;
     }
      
     return resp;
  }
}
