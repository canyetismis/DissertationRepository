package schedules;

public interface CoolingSchedule {
	
	/**
	 * Getter method for the current temperature
	 * @return Returns the current temperature
	 */
	public double getTemperature();
	
	/**
	 * Changes the temperature with respect to the cooling schedule
	 */
	public void changeTemperature();
}
