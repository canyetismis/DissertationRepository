package schedules;

public class LundyAndMeesCooling implements CoolingSchedule {
	//local variables to store temperature and beta values
	private double temperature;
	private double beta;

	public LundyAndMeesCooling(double initialSolution) {
		double c = 1.0;
		this.temperature = c*initialSolution;
		this.beta = 0.00001; //Alpha value will be improved by implementing Taguchi Orthogon Method
	}
	
	@Override
	public double getTemperature() {
		return this.temperature;
	}

	@Override
	public void changeTemperature() {
		temperature *= temperature / (1 + beta * temperature);
	}

}
