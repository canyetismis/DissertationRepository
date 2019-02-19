// WFLO
public class main {

  public static void main(String argv[]) {
      try {
          WindScenario ws = new WindScenario("D://Dissertation//source//Scenarios//0.xml");
          KusiakLayoutEvaluator wfle = new KusiakLayoutEvaluator();
          wfle.initialize(ws);
          SRSA algorithm = new SRSA(wfle, 398625566);
          algorithm.run(); // optional, name of method 'run' provided on submission
          // algorithm can also just use constructor
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
}
