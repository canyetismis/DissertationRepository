package schedules;

public interface CoolingSchedule {
	
	/**
	 * Getter method for the current temperature
	 * @return Returns the current temperature
	 */
	public double getTemperature();
	
	/**
	 * Increases the temperature with respect to the cooling schedule
	 */
	public void increaseTemperature();
}
