package schedules;

public class GeometricCooling implements CoolingSchedule {
	//local variables to store temperature and alpha values
	private double temperature;
	private double alpha;
	
	public GeometricCooling(double initialSolution) {
		double c = 1.0; //Initial c value, *Ask about this value
		this.temperature =c * initialSolution;
		this.alpha = 0.998; //Alpha value will be improved by implementing Taguchi Orthogon Method
		// []
	}

	@Override
	public double getTemperature() {
		return this.temperature;
	}

	@Override
	public void changeTemperature() {
		temperature *= alpha;
	}

}
