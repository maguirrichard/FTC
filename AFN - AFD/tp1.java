/**********************************
    * TP1 FTC
    * @author Maguirrichard Oliveira e Pedro Pimenta
    * @version 1 02/2018.
    *********************************/
import java.util.Scanner;
import java.util.Formatter;
import java.io.FileReader;

public class tp1
{
  public static Scanner in = new Scanner(System.in);
  public static String str = in.nextLine();
  public static Formatter saida = null;
  public static Scanner afd = null;

  public static int [][] transitionMap; // 2D array which is used to store state transitions. transitionMap[i][j] is the state reached when state i is given symbol j
  public static int [][] partitionTransitionMap; // same as transitionMap, except row indices represent partition numbers, not state numbers
  public static int startState = 0; // The starting state. This is used as the root for DFS to eliminate unreachable states
  public static long reachable = 0; // A bitset to represent states that are reachable
  public static long allStates = 0; // A bitset to represent all states in the FSM
  public static long finalStates = 0; // A bitset to represent final states in the FSM
  public static long nonFinalStates = 0; // A bitset to represent non-final states in the FSM
  public static long [] p; // array of partitions. Each partition is a bitset of states


  public static void main(String[] args)
  {
     String inicio = str;
     String nomeArq = "afd_inicial.txt";
     String ISO = "ISO-8859-1";

     for(int i = 0; i < 3; i++){
           str = in.nextLine();
           inicio = inicio + "\n" + str;
     }
     //System.out.println(inicio);

     try{
        //Grava AFD do XML no arquivo
        saida = new Formatter(nomeArq, ISO);
        str = in.nextLine();
        if(states(str) == false) System.out.println("ERRO! AFD NÃO PERMITIDO COM 2 OU MAIS ESTADOS INICIAIS!!");
        if(transitions(str) == false) System.out.println("ERRO! AFN NÃO PERMITIDO!!");
        saida.close();

        //Lê o arquivo do AFD para iniciar a minimização
        afd = new Scanner(new FileReader(nomeArq));
        str = afd.nextLine();
        minimizer();
        afd.close();
     }catch (Exception e){ 
        System.out.println("ERRO! NÃO FOI POSSÍVEL ABRIR O ARQUIVO!!"); 
     }
  }

  public static void minimizer()
  {
     //We start off with no states
     finalStates = 0;
	allStates = 0;

	//Initialize our transition maps. We set transition[i][j] to be -1 in order to indicate that state/partition i does not transition when given symbol j
	transitionMap = new int[64][];
	for(int i = 0; i < 64; i++)
     {
        transitionMap[i] = new int[26];
        for(int j = 0; j < 26; j++)
           transitionMap[i][j] = -1;
	}

     partitionTransitionMap = new int[64][];
     for(int i = 0; i < 64; i++)
     {
        partitionTransitionMap[i] = new int[26];
        for(int j = 0; j < 26; j++)
           partitionTransitionMap[i][j] = -1;
	}

     //Read start state
     startState = Integer.parseInt(str);

     //Read final state
     str = afd.nextLine();
     String [] finals = str.split(" ");
     for(int i = 0; i <  finals.length; i++) 
        finalStates |= 1 << (Integer.parseInt(finals[i]));

     System.out.println(startState);
     System.out.println(finalStates);

     //Read transitions
     while(afd.hasNext())
     {
        str = afd.nextLine();
        String [] tran = str.split(" ");
        int from = Integer.parseInt(tran[0]);
        int symbol = ((int)(tran[1].charAt(0)))-97;
	   int to = Integer.parseInt(tran[2]);

        //Add transition
        transitionMap[from][symbol] = to;

        //Add from and to states to the allStates bitset
	   allStates |= (1 << from); 
	   allStates |= (1 << to);
	}
     
     dfs(startState);

     //Filter unreachable states
	allStates &= reachable;
	finalStates &= reachable;

     p = new long[64];
     for(int i = 0; i < 64 ; i++)
	   p[i] = 0; //No partition exists

     nonFinalStates = allStates & ~finalStates;
	p[0] = finalStates;
	p[1] = nonFinalStates;

     //Store how many partitions have been added already
     int nextPartitionIndex = 2;

     //There will be at most 64 partitions. At each iteration, we operate on a partition and add at most 1 more partition 
	for (int i = 0; i < 64; i++)
     {
        // A bitset for a new partition. This partition will include all states that are distinct from the state corresponding to the leftmost bit in P[i]
        long newPartition = 0;

        // Done partitioning
        if (p[i] == 0) break;

        // Try to find leftmost bit in the bitset. This loop will only run to its entirety once when that bit is found
        for (int j = 63; j >=  0; j--)
        {
           //Potential leftmost bit. If found, this bit will remain in the bit set.
           long staticState = (long) 1 << j;

           //Check if this state is in the current bitset
           if ((p[i] & (staticState)) != 0)
           {
              //The lestmost bit state will be associated with this partition. Therefore, we must copy over the transitions for this state to the transitions for
              //The corresponding partition
              partitionTransitionMap[i] = transitionMap[j];

              //Check for states that should be removed from this partition. All states will be bits right of the staticState bit
              for(int k = j - 1; k >= 0; k--)
              {
                 //Potential state to remove
                 long otherState = (long) 1 << k;
                 //Check if this state is in the current bitset
                 if((p[i] & (otherState)) != 0)
                 {
                    //Iterate across the entire alphabet and check if staticState and otherState can transition to different partitions.
                    for (int l  = 0; l < 26; l++)
                    {
                       int staticNext = -1; //next partition for static
                       int otherNext = -1; //next partition for other
                       for (int m = 0; m < nextPartitionIndex; m++)
                       {
                          if((p[m] & (1 << transitionMap[j][l])) != 0)
                             staticNext = m;	//found static next
                          if((p[m] & (1 << transitionMap[k][l])) != 0)
                             otherNext = m; //found other next
                       }
                       //If partitions differ, remove the other state and add it to the new partition. Then break, since we are done with this partition	
				   if(transitionMap[j][l] != transitionMap[k][l] && (staticNext != otherNext))
                       {
                          p[i] &= ~(1 << k);
                          newPartition |= (1 << k);
                          break;
                       } 
                    }	
		       }
              }   
              break;
           }
        }

        //New partition exists. Add it to P and increment nextPartitionIndex
        if(newPartition != 0)
        {
           p[nextPartitionIndex] = newPartition;
           nextPartitionIndex++;
	   }
     }

     //Find and print start partition
	int startPartition = 0;
	for (int i = 0; i < nextPartitionIndex; i ++)
     {
        if((p[i] & (1 << startState)) != 0)
        {
           startPartition = i;
           break;
        }
     }

	System.out.println(startPartition);

     //Find and print final partitions
	for(int i = 0; i < nextPartitionIndex; i++)
     {
        if((p[i] & finalStates) != 0)
			System.out.println(i);
	}

     //Find and print all transitions
	for(int i = 0; i < nextPartitionIndex; i++)
     {
        for(int j = 0; j < 26; j++)
        {
           if(partitionTransitionMap[i][j] != -1)
           {
              for(int k = 0; k < nextPartitionIndex; k++)
              {
                 if((p[k] & (1 << partitionTransitionMap[i][j])) != 0)
						System.out.println(i + " " + (char)(j+97) + " " + k);
              }
           }
        }
     }
  }

  public static void dfs(int v)
  {
     reachable |= (1 << v);
	
	//Try exploring all paths..
     for(int i = 0; i < 26; i++)
     {
        if((transitionMap[v][i] != -1) && ((reachable & (1 << transitionMap[v][i])) == 0))
        {
           dfs(transitionMap[v][i]);
        }
     }
  }

  public static boolean states(String s)
  {
     str = s;
     String state = "";
     String initial = "";
     String fim = "";
     byte x = 0;
     while(!str.contains("<transition>"))
     {
        if(str.contains("<state"))
        {
           str = str.substring(13, 15);
           if(str.contains("\"")) str = str.substring(0, 1);
           state = str;

           for(int i = 0; i < 3; i++) 
              str = in.nextLine();

           if(x == 0 && str.contains("initial")) 
           {
              initial = state;
              x++;
              str = in.nextLine();
           }else if(x <= 1 && str.contains("final")){
              fim = fim + " " + state;
              str = in.nextLine();
           }
           else if(!str.contains("</st")) return false;
        }
        str = in.nextLine();
     }

     //System.out.println(initial);
     //System.out.println(fim.trim());
     saida.format("%s\n", initial);
     saida.format("%s\n", fim.trim());

     return true;
  }

  public static boolean transitions(String x)
  {
     str = x;
     String from = "";
     String to = "";
     String read = "";
     String transitions = "";
     while(str.contains("transition>")){
        str = in.nextLine();
        from = str.substring(9, 11);
        if(from.contains("<")) from = from.substring(0, 1);

        str = in.nextLine();
        to = str.substring(7, 9);
        if(to.contains("<")) to = to.substring(0, 1);

        str = in.nextLine();
        if(!str.contains("<read>")) return false;
        read = str.substring(9, 11);
        if(read.contains("<")) read = read.substring(0, 1);
     
        transitions = transitions + from + " " + read + " " + to + "\n";
        str = in.nextLine();
        str = in.nextLine();
     }
     
     //System.out.println(transitions.trim());
     saida.format("%s", transitions.trim());
     return true;
  }
}
