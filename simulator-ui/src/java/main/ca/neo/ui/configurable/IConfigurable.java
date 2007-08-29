package ca.neo.ui.configurable;

/**
 * Describes a object which can be configured by a IConfigurationManager
 * 
 * @author Shu Wu
 * 
 */
public interface IConfigurable {

	/**
	 * Called when configuration parameters have been set
	 * 
	 * @param configParameters
	 *            A set of configured parameters to be used to complete
	 *            configuration
	 */
	public void completeConfiguration(ConfigParam configParameters)
			throws ConfigException;

	/**
	 * @return An array of objects which describe what needs to be configured in
	 *         this object
	 */
	public ConfigParamDescriptor[] getConfigSchema();

	/**
	 * @return Name given to this type of object
	 */
	public String getTypeName();

}