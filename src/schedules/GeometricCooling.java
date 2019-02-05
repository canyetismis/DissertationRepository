package schedules;

public class GeometricCooling implements CoolingSchedule {
	//local variables to store temperature and alpha values
	private double temperature;
	private double alpha;
	
	public GeometricCooling(double initialSolution) {
		double c = 1.0; //Initial c value, this will be improved by implementing Taguchi Octagon Method
		this.temperature =c * initialSolution;
		this.alpha = 0.9;
	}

	@Override
	public double getTemperature() {
		return this.temperature;
	}

	@Override
	public void increaseTemperature() {
		temperature *= alpha;
	}

}
