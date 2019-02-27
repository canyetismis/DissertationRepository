import java.util.Random;

// WFLO
public class main {

  public static void main(String argv[]) {
      try {
          WindScenario ws = new WindScenario("D://Dissertation//source//Scenarios//0.xml");
          KusiakLayoutEvaluator wfle = new KusiakLayoutEvaluator();
          wfle.initialize(ws);
          Random random = new Random(398625566); //New random number generator for generating seeds
          for (int i = 0; i<10; i++) {
        	  int seed = random.nextInt((400000000-100000000)+1)+100000000;//additional statement to ensure the range of seed variables
        	  SRSA algorithm = new SRSA(wfle, seed, i);
        	  System.out.println(seed);
        	  algorithm.run();
          }
          //algorithm.run(); // optional, name of method 'run' provided on submission
          // algorithm can also just use constructor
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
}
