
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;

import schedules.GeometricCooling;

public class SRSA {

	WindFarmLayoutEvaluator wfle;
	boolean[] solution;
	boolean[] prev_solution;
	boolean[] best_solution;
	int[] util_LLH;
	double obj;
	double prev_obj;
	double best_obj;
	Random rand;
	
	ThreadMXBean bean;
	long initialTime;
	
	Random random;
	BufferedWriter out;
	long cf_cons;
	int worse_flag;
	
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
		//A constant to balance out the weight of the objective function values
		cf_cons = 1000;
		worse_flag = 10;
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
	
	public long getElapsedTime(){
	    if (bean == null) { 
	    	return 0L;
	    }
	    return (long) ((bean.getCurrentThreadCpuTime() - initialTime) / 1000000.0D);
	}
	public void run() throws IOException{
		//Variables for the Modified Choice Function
		double phi = 0.50, delta = 0.50;
		int h = 0, init_flag = 0, worse_cout = 0;
		long time_exp_before, time_exp_after, time_to_apply;
		double best_heuristic_score = 0.00, fitness_change = 0.00, prev_fitness_change = 0.00;
		double[] F = new double[number_of_LLHs], f1 = new double[number_of_LLHs], f3 = new double[number_of_LLHs];
		double[][] f2 = new double[number_of_LLHs][number_of_LLHs];
		int last_heuristic_called = 0;
		boolean duplicate = false;
		bean = ManagementFactory.getThreadMXBean();
	    initialTime = bean.getCurrentThreadCpuTime();
		
		
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
		
		best_solution = new boolean[grid.size()];
		prev_solution = new boolean[grid.size()];
		for (int i=0; i<grid.size(); i++) {
			prev_solution[i] = solution[i];
			best_solution[i] = solution[i];
		}
		prev_obj = obj;
		best_obj = obj;
		
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
			/*out.newLine();
			out.newLine();
			out.write("---BEGIN---ITERATION: " + num_of_evaluations + "\t Current Objective Function Value: " + obj);
			out.newLine();
			out.write("////DISCLAIMER//// The previous choice function scores can be observed in the previous itteration ");
			out.newLine();
			out.newLine();*/
			if (init_flag > 1) { //flag used to select heuristics randomly for the first two iterations
				best_heuristic_score = 0.0;
				for (int i = 0; i < number_of_LLHs; i++) {//update F matrix
					F[i] = phi * f1[i] + phi * f2[i][last_heuristic_called] + delta * f3[i];
					/*out.newLine();
					out.write("Selected Heuristic \t" + "LLH-" + i + "\t Score: " + F[i] );
					
					out.newLine();
					out.write("\t phi*f1 for LLH-" + i + ": \t" + phi*f1[i]);
					out.newLine();
					out.write("\t phi*f2 for LLH-" + i + ": \t" + phi*f2[i][last_heuristic_called]);
					out.newLine();
					out.write("\t delta*f3 for LLH-" + i + ": \t" + delta*f3[i]);
					out.newLine();
					out.write("\t delta value: \t" + delta);
					out.newLine();
					out.write("\t phi value: \t" + phi);*/
					//Selects the heuristic with the highest score
					if (F[i] > best_heuristic_score) {
						h = i; 
						best_heuristic_score = F[i];
					}
				}
				//checks for duplicate scores
				double[] temp_array = new double[number_of_LLHs];
				int counter = 1;
				for(int j = 0; j<number_of_LLHs; j++) {
					for(int k = j+1; k<number_of_LLHs; k++) {
						if(k!=j && F[k]==F[j]) {
							temp_array[j] = F[j];
							temp_array[k] = F[k];
							//checks if duplicate value is the highest score
							if(F[j] == best_heuristic_score) {
								duplicate = true;
							}
							counter++;
						}
					}

					if(duplicate) {
						break;
					}
				}
				//if a duplicate score exists, stores heuristics with same score
				//and selects one of them randomly
				if(duplicate) {
					//Store the heuristics with same score in an array
					int[] llh_to_apply = new int[counter];
					int llh_counter = 0;
					for(int x = 0; x<number_of_LLHs; x++) {
						if(temp_array[x] != 0) {
							llh_to_apply[llh_counter] = x;
							llh_counter++;
						}
					}
					int selection = rand.nextInt(llh_to_apply.length);
					h = llh_to_apply[selection];
					best_heuristic_score = F[h];
					duplicate = false;
				} 
				/*out.newLine();
				out.newLine();
				out.write("Heuristic to Apply \t" + "LLH-" + h + "\t Score: " + best_heuristic_score );
				out.newLine();*/
			}
			else {
				//select heuristics randomly
				h = rand.nextInt(number_of_LLHs);
				/*out.newLine();
				out.newLine();
				out.write("Randomly Selected Heuristic to Apply \t" + "LLH-" + h );
				out.newLine();*/
			}
			
			System.out.print(num_of_evaluations + "\t LLH-" + h + "\t");
			
			out.newLine();
			out.write(num_of_evaluations + "\t LLH-" + h + "\t");
			
			//Time is the iteration number in this case
			time_exp_before = num_of_evaluations;//num_of_evaluations;
			applyLLH(h);
			obj = evaluate();
			time_exp_after = num_of_evaluations;
			time_to_apply = time_exp_after - time_exp_before; //value is usually 1 
			
			System.out.print(obj + "\t");
			out.write(obj + "\t");
			
			if (obj < prev_obj) {
				util_LLH[h]++;
			}
			//Simulated Annealing
			double delta_sa = obj - prev_obj;
			double r = random.nextDouble();
			double temp = cs.getTemperature();
			
			//calculate the change in fitness from the current solution to the new solution
			fitness_change = prev_obj - obj;
			//Simulated Annealing
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
			//Simulated Annealing
			cs.changeTemperature();
			System.out.println(obj);
			out.newLine();
			out.write(String.valueOf(obj));
			
			//Keeps track of the best solution
			if(obj <= best_obj) {
				for (int i=0; i<grid.size(); i++) {
					best_solution[i] = solution[i];
				}
				best_obj = obj;
			}
			
			//update f1, f2 and f3 values for appropriate heuristics 
			//first two iterations dealt with separately to set-up variables
			//scores must be improved by 1
			if (init_flag > 1) {
				f1[h] = (fitness_change / time_to_apply)*cf_cons + phi * f1[h];
				f2[h][last_heuristic_called] = prev_fitness_change + (fitness_change / time_to_apply)*cf_cons + phi * f2[h][last_heuristic_called];
			} else if (init_flag ==1) {
				f1[h] = fitness_change / time_to_apply;
				f2[h][last_heuristic_called] = prev_fitness_change + (fitness_change / time_to_apply)*cf_cons + prev_fitness_change;
				init_flag++;
			} else { //i.e. init_flag = 0
				f1[h] = (fitness_change / time_to_apply)*cf_cons;
				init_flag++;
			} 
			for (int i = 0; i < number_of_LLHs; i++) {
				f3[i] += time_to_apply;
			}
			f3[h] = 0.00;

			if (fitness_change > 0.00) {//in case of improvement
				phi = 0.99;
				delta = 0.01;
				prev_fitness_change = (fitness_change / time_to_apply)*cf_cons;
				/*out.newLine();
				out.write("+++END+++There is improvement, new objective function value: " + obj);
				out.newLine();*/
				worse_cout = 0;
			} else {//non-improvement
				if (phi > 0.01) {
					phi -= 0.01;                                                                          
				}
				phi = roundTwoDecimals(phi);
				delta = 1.00 - phi;
				delta = roundTwoDecimals(delta);
				prev_fitness_change = 0.00;
				/*out.newLine();
				out.write("+++END+++There is NO improvement, new objective function value: " + obj);
				out.newLine();*/
				worse_cout++;
			}
			last_heuristic_called = h;
			
			if(worse_cout == worse_flag) {
				//resets the scores for the Modified Choice Function
				//resets the measures
				for (int i = 0; i < number_of_LLHs; i++) {
					F[i] = 0;
					f1[i] = 0;
					f3[i] = 0;
					for (int j = 0; j < number_of_LLHs; j++) {
						f2[i][j] = 0;
					}
				}
				worse_cout = 0;
				init_flag = 0;
			}

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
		System.out.println("best obj " + best_obj);
		
		for (int i = 0; i < number_of_LLHs; ++i) {
			System.out.println("LLH" + i + "\t" + util_LLH[i]);
		}
		
	}
}