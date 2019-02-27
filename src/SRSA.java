
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import schedules.GeometricCooling;

public class SRSA {

	WindFarmLayoutEvaluator wfle;
	boolean[] solution;
	boolean[] prev_solution;
	int[] util_LLH;
	double obj;
	double prev_obj;
	Random rand;
	
	Random random;
	BufferedWriter out;
	
	int number_of_LLHs;
	ArrayList<double[]> grid;
	int num_of_evaluations,total_num_of_evaluations;
	
	GeometricCooling cs;
	
	//Constructor that initialises values
    public SRSA(WindFarmLayoutEvaluator evaluator, long seed) throws IOException {
		number_of_LLHs = 7;
		wfle = evaluator;
		rand = new Random(398625566);//seed value
		random = new Random(398625566);
		grid = new ArrayList<double[]>();
		obj = Double.MAX_VALUE;
		num_of_evaluations = 0;
		total_num_of_evaluations = 2000;
		out = new BufferedWriter(new FileWriter("out.txt"));
		//cs = new GeometricCooling(0.00001); //Has to be the objective funtion value of the initial solution
	}
    //Evaluation Function
	private double evaluate() {
		++num_of_evaluations;
		int nturbines=0;
		for (int i=0; i<grid.size(); i++) {
			if (solution[i]) {
				nturbines++;
			}
		}
		if (nturbines == 0) return Double.MAX_VALUE;
		
		double[][] layout = new double[nturbines][2];
		int current_turbine = 0;
		for (int i=0; i<grid.size(); i++) {
			if (solution[i]) {
				layout[current_turbine][0] = grid.get(i)[0];
				layout[current_turbine][1] = grid.get(i)[1];
				current_turbine++;
			}
		}
		
		double cost_of_energy;
		if (wfle.checkConstraint(layout)) {
			wfle.evaluate(layout);
			cost_of_energy = wfle.getEnergyCost();
		}
		else {
			cost_of_energy = Double.MAX_VALUE;
		}
		
		return cost_of_energy; //Test value *1000000000
	}
	//Low-Level Heuristics
	private void LLH0() {
		int randomSelection = rand.nextInt(grid.size());
		solution[randomSelection] = !solution[randomSelection];
	}
	
	private void LLH1() {
		int randomSelection1 = rand.nextInt(grid.size());
		int randomSelection2 = rand.nextInt(grid.size() - 1);
		if (randomSelection1 == randomSelection2) randomSelection2 = grid.size() - 1;
		boolean Temp = solution[randomSelection1];
		solution[randomSelection1] = solution[randomSelection2];
		solution[randomSelection2] = Temp;
	}
	
	private void LLH2() {
		double mut_rate = 0.10;
		for (int i=0; i<grid.size(); i++) {
			if (rand.nextDouble() < mut_rate) {
				solution[i] = !solution[i];
			}
		}
	}
	
	private void LLH3() {
		double rate = 0.30;
		if (rand.nextBoolean()) {
			for (int i=0; i<grid.size(); i++) {
				if (rand.nextDouble() < rate) {
					solution[i] = true;
				}
			}
		}
		else {
			for (int i=0; i<grid.size(); i++) {
				if (rand.nextDouble() < rate) {
					solution[i] = false;
				}
			}
		}
	}
	
	//first improvement hill climbing
	private void LLH4(){
		for (int iter=0; iter<4; iter++) {
			if (num_of_evaluations == total_num_of_evaluations - 1)
				break;
			int i = rand.nextInt(grid.size());
			solution[i] = !solution[i];
			obj = evaluate();
			if (obj < prev_obj)
				break;
			else
				solution[i] = !solution[i];
		}
	}
	
	private void LLH5(){
		int rand1 = rand.nextInt(grid.size());
		int rand2 = rand.nextInt(grid.size() - 1);
		if (rand1 == rand2) rand2 = grid.size() - 1;
		
		double xcoord1 = grid.get(rand1)[0];
		double xcoord2 = grid.get(rand2)[0];
		
		double cross_rate = 0.20;
		double ycoord = -1;
		
		for (int i=0; i<grid.size(); i++) {
			if (grid.get(i)[0] == xcoord1) {
				ycoord = grid.get(i)[1];
			}
			for (int j=0; j<grid.size(); j++) {
				if (grid.get(j)[0] == xcoord2 && grid.get(j)[1] == ycoord && rand.nextDouble()<cross_rate) {
					boolean temp = solution[i];
					solution[i] = solution[j];
					solution[j] = temp;
				}
			}
		}
	}
	
	private void LLH6(){
		int rand1 = rand.nextInt(grid.size());
		int rand2 = rand.nextInt(grid.size() - 1);
		if (rand1 == rand2) rand2 = grid.size() - 1;
		
		double ycoord1 = grid.get(rand1)[1];
		double ycoord2 = grid.get(rand2)[1];
		
		double cross_rate = 0.20;
		double xcoord = -1;
		
		for (int i=0; i<grid.size(); i++) {
			if (grid.get(i)[1] == ycoord1) {
				xcoord = grid.get(i)[0];
			}
			for (int j=0; j<grid.size(); j++) {
				if (grid.get(j)[1] == ycoord2 && grid.get(j)[0] == xcoord && rand.nextDouble()<cross_rate) {
					boolean temp = solution[i];
					solution[i] = solution[j];
					solution[j] = temp;
				}
			}
		}
	}
	
	private void applyLLH(int h) {
		if (h == 0) LLH0();
		else if (h == 1) LLH1();
		else if (h == 2) LLH2();
		else if (h == 3) LLH3();
		else if (h == 4) LLH4();
		else if (h == 5) LLH5();
		else if (h == 6) LLH6();
	}
	
	public double roundTwoDecimals(double d) {
		DecimalFormat two_d_form = new DecimalFormat("#.##");
		return Double.valueOf(two_d_form.format(d));
	}
	
	public void run() throws IOException{
		//Variables for the Modified Choice Function
		double phi = 0.50, delta = 0.50;
		int h = 0, init_flag = 0;
		int time_exp_before, time_exp_after, time_to_apply;
		double best_heuristic_score = 0.00, fitness_change = 0.00, prev_fitness_change = 0.00;
		double[] F = new double[number_of_LLHs], f1 = new double[number_of_LLHs], f3 = new double[number_of_LLHs];
		double[][] f2 = new double[number_of_LLHs][number_of_LLHs];
		int last_heuristic_called = 0;
		
		// set up grid
		// centers must be > 8*R apart
		// ensure the feasibility of the solution
		double interval = 8.001 * wfle.getTurbineRadius(); // 308.0385
		for (double x=0.0; x<wfle.getFarmWidth(); x+=interval) {
			for (double y=0.0; y<wfle.getFarmHeight(); y+=interval) {
				boolean valid = true;
				for (int o=0; o<wfle.getObstacles().length; o++) {
					double[] obs = wfle.getObstacles()[o];
					if (x>obs[0] && y>obs[1] && x<obs[2] && y<obs[3]) {
						valid = false;
					}
				}
				if (valid) {
					double[] point = {x, y};
					grid.add(point);
				}
			}
		}
		
		// initialisation method
		// randomly generate initial solution
		solution = new boolean[grid.size()];
		for (int i=0; i<grid.size(); i++) {
			solution[i] = rand.nextBoolean();
		}
		obj = evaluate();
		
		prev_solution = new boolean[grid.size()];
		for (int i=0; i<grid.size(); i++) {
			prev_solution[i] = solution[i];
		}
		prev_obj = obj;
		
		System.out.println("Initial objective " + obj);
		out.write("Initial objective " + obj);
		
		//Sets initial temperature
		cs = new GeometricCooling(obj);
		
		util_LLH = new int[number_of_LLHs];
		
		for (int i = 0; i < number_of_LLHs; ++i) {
			util_LLH[i] = 0;
		}
		
		// Hyper-heuristic 
		while(num_of_evaluations < total_num_of_evaluations) {
			//int h = rand.nextInt(number_of_LLHs);
			if (init_flag > 1) { //flag used to select heuristics randomly for the first two iterations
				best_heuristic_score = 0.0;
				for (int i = 0; i < number_of_LLHs; i++) {//update F matrix
					F[i] = phi * f1[i] + phi * f2[i][last_heuristic_called] + delta * f3[i];
					if (F[i] > best_heuristic_score) {
						h = i; 
						best_heuristic_score = F[i];
					}
				}
			}
			else {
				//select heuristics randomly
				h = rand.nextInt(number_of_LLHs);
			}
			
			System.out.print(num_of_evaluations + "\t LLH-" + h + "\t");
			
			out.newLine();
			out.write(num_of_evaluations + "\t LLH-" + h + "\t");
			
			time_exp_before = num_of_evaluations;
			applyLLH(h);
			obj = evaluate();
			time_exp_after = num_of_evaluations;
			time_to_apply = time_exp_after - time_exp_before + 1; //+1 prevents / by 0
			
			System.out.print(obj + "\t");
			out.write(obj + "\t");
			
			if (obj < prev_obj) {
				util_LLH[h]++;
			}
			
			double delta_sa = obj - prev_obj;
			double r = random.nextDouble();
			double temp = cs.getTemperature();
			
			//calculate the change in fitness from the current solution to the new solution
			fitness_change = obj - prev_obj;
			
			if (delta_sa < 0 || r < Math.exp(-delta_sa/temp)) {
				for (int i=0; i<grid.size(); i++) {
					prev_solution[i] = solution[i];
				}
				prev_obj = obj;
				
			}
			else {
				for (int i=0; i<grid.size(); i++) {
					solution[i] = prev_solution[i];
				}
				obj = prev_obj;
			}
			
			cs.changeTemperature();
			System.out.println(obj);
			out.write(String.valueOf(obj));
			//update f1, f2 and f3 values for appropriate heuristics 
			//first two iterations dealt with separately to set-up variables
			if (init_flag > 1) {
				f1[h] = fitness_change / time_to_apply + phi * f1[h];
				f2[h][last_heuristic_called] = prev_fitness_change + fitness_change / time_to_apply + phi * f2[h][last_heuristic_called];
			} else if (init_flag == 1) {
				f1[h] = fitness_change / time_to_apply;
				f2[h][last_heuristic_called] = prev_fitness_change + fitness_change / time_to_apply + prev_fitness_change;
				init_flag++;
			} else { //i.e. init_flag = 0
					f1[h] = fitness_change / time_to_apply;
			init_flag++;
			} 
			for (int i = 0; i < number_of_LLHs; i++) {
				f3[i] += time_to_apply;
			}
			f3[h] = 0.00;

			if (fitness_change < 0.00) {//in case of improvement
				phi = 0.99;
				delta = 0.01;
				prev_fitness_change = fitness_change / time_to_apply;
			} else {//non-improvement
				if (phi > 0.01) {
					phi -= 0.01;                                                                          
				}
				phi = roundTwoDecimals(phi);
				delta = 1.00 - phi;
				delta = roundTwoDecimals(delta);
				prev_fitness_change = 0.00;
			}
			last_heuristic_called = h;

		}
		out.flush();
		out.close();
		// Stats
		System.out.println("GRID-Start");
		for (int i=0; i<grid.size(); i++) {
			if (solution[i])
				System.out.println(grid.get(i)[0] + "\t" + grid.get(i)[1]);
		}
		System.out.println("GRID-End");
		System.out.println("num_of_evaluations " + num_of_evaluations);
		System.out.println("best obj " + obj);
		
		for (int i = 0; i < number_of_LLHs; ++i) {
			System.out.println("LLH" + i + "\t" + util_LLH[i]);
		}
		
	}
}