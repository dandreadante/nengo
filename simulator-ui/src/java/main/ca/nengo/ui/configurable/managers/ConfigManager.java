package ca.nengo.ui.configurable.managers;

import java.awt.Container;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

import ca.nengo.ui.NengoGraphics;
import ca.nengo.ui.configurable.ConfigException;
import ca.nengo.ui.configurable.ConfigResult;
import ca.nengo.ui.configurable.ConfigSchema;
import ca.nengo.ui.configurable.ConfigSchemaImpl;
import ca.nengo.ui.configurable.IConfigurable;
import ca.nengo.ui.configurable.Property;
import ca.shu.ui.lib.util.UserMessages;
import ca.shu.ui.lib.util.Util;

/**
 * Configuration Manager used to configure IConfigurable objects
 * 
 * @author Shu Wu
 */
public abstract class ConfigManager {
	/**
	 * Name of directory where to store saved configuration
	 */
	static final String SAVED_CONFIG_DIR = NengoGraphics.USER_FILE_DIR + "/Config";

	/**
	 * Creates a saved objects folder if it isn't already there
	 * 
	 * @return The Saved Objects folder
	 */
	private static File getSavedObjectsFolder() {
		File file = new File(SAVED_CONFIG_DIR);
		if (!file.exists())
			file.mkdir();
		return file;
	}

	public enum ConfigMode {
		STANDARD, TEMPLATE_NOT_CHOOSABLE, TEMPLATE_CHOOSABLE
	}

	public static Object configure(Property prop, String typeName, Container parent)
			throws ConfigException {

		ConfigResult properties = configure(new ConfigSchemaImpl(prop), typeName, null, parent,
				ConfigMode.TEMPLATE_NOT_CHOOSABLE);
		return properties.getValue(prop);
	}

	/**
	 * Convenient function to automatically wrap the PropertyDescriptors with a
	 * default Config schema
	 */
	public static ConfigResult configure(Property[] schema, String typeName, Container parent,
			ConfigMode configMode) throws ConfigException {
		return configure(new ConfigSchemaImpl(schema), typeName, null, parent, configMode);
	}

	public static ConfigResult configure(ConfigSchema schema, String typeName, String description,
			Container parent, ConfigMode configMode) throws ConfigException {
		if (description == null) {
			description = typeName;
		}
		Configureable configurable = new Configureable(schema, typeName, description);

		UserConfigurer configurer;

		if (configMode == ConfigMode.STANDARD) {
			configurer = new UserConfigurer(configurable, parent);
		} else if (configMode == ConfigMode.TEMPLATE_NOT_CHOOSABLE) {
			configurer = new UserTemplateConfigurer(configurable, parent, false);
		} else if (configMode == ConfigMode.TEMPLATE_CHOOSABLE) {
			configurer = new UserTemplateConfigurer(configurable, parent, true);
		} else {
			throw new IllegalArgumentException("Unsupported config mode");
		}

		configurer.configureAndWait();

		return configurable.getProperties();
	}

	/**
	 * @returns the file name prefix given per class
	 */
	@SuppressWarnings("unchecked")
	protected static String getFileNamePrefix(IConfigurable obj) {

		return obj.getTypeName() + "_Props_";

	}

	/**
	 * Object to be configured
	 */
	private final IConfigurable configurable;

	/**
	 * Set of attributes that will be set during configuration
	 */
	private MutableAttributeSet properties;

	/**
	 * @param configurable
	 *            Object to be configured
	 */
	public ConfigManager(IConfigurable configurable) {
		super();
		properties = new SimpleAttributeSet();
		this.configurable = configurable;

	}

	/**
	 * Configures the IConfigurable object and waits until the configuration
	 * finishes
	 */
	protected abstract void configureAndWait() throws ConfigException;

	/**
	 * @param name
	 *            filename prefix
	 */
	protected void deletePropertiesFile(String name) {
		File file = new File(getSavedObjectsFolder(), getFileNamePrefix(configurable) + name);

		System.gc();
		if (file.exists()) {
			boolean val = file.delete();
			if (val == false) {
				UserMessages.showError("Could not delete file");
			}

		}
	}

	/**
	 * @return Object to be configured
	 */
	protected IConfigurable getConfigurable() {
		return configurable;
	}

	/**
	 * @return Set of properties to be set during the configuration process
	 */
	protected MutableAttributeSet getProperties() {
		return properties;
	}

	/**
	 * @param name
	 *            Name of property
	 * @return Value of property
	 */
	protected Object getProperty(String name) {
		return getProperties().getAttribute(name);
	}

	/**
	 * @return List of fileNames which point to saved configuration files
	 */
	protected String[] getPropertyFiles() {
		File file = getSavedObjectsFolder();
		/*
		 * Gets a list of property files
		 */
		String[] files = file.list(new ConfigFilesFilter(configurable));

		/*
		 * Return the file names without the prefix
		 */
		String[] files0 = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			files0[i] = files[i].substring(getFileNamePrefix(configurable).length(), files[i]
					.length());
		}
		return files0;

	}

	/**
	 * @param name
	 *            Name of the properties set to be loaded
	 */
	protected void loadPropertiesFromFile(String name) {

		FileInputStream f_in;

		try {
			f_in = new FileInputStream(SAVED_CONFIG_DIR + "/" + getFileNamePrefix(configurable)
					+ name);

			ObjectInputStream obj_in = new ObjectInputStream(f_in);

			Object obj;

			obj = obj_in.readObject();

			if (obj == null) {
				UserMessages.showError("Could not load file: " + name);
			} else {

				this.properties = (MutableAttributeSet) obj;
			}
			obj_in.close();

		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (ClassNotFoundException e) {
			System.out.println("Class not found exception");
		} catch (InvalidClassException e) {
			System.out.println("Invalid class exception");
		} catch (NotSerializableException e) {
			Util.debugMsg("Loading properties not serializable: " + e.getMessage());
		} catch (IOException e) {
			Util.debugMsg("IO Error serializing properties: " + e.getMessage());
		}
	}

	/**
	 * @param name
	 *            name of the properties set to be saved
	 */
	protected void savePropertiesFile(String name) {

		// Write to disk with FileOutputStream
		FileOutputStream f_out;
		try {
			File objectsFolder = getSavedObjectsFolder();
			File file = new File(objectsFolder, getFileNamePrefix(configurable) + name);

			if (file.exists()) {
				Util.debugMsg("Replaced existing file: " + file.getName());
			}
			f_out = new FileOutputStream(file);

			ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
			obj_out.writeObject(properties);
			obj_out.close();

			f_out.close();

		} catch (NotSerializableException e) {
			Util.debugMsg("Did not save settings to template because " + e.getMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected void setProperty(String name, Object value) {
		getProperties().addAttribute(name, value);
	}

}

/**
 * Filters files needed by ConfigManager
 * 
 * @author Shu
 */
class ConfigFilesFilter implements FilenameFilter {
	IConfigurable parent;

	public ConfigFilesFilter(IConfigurable parent) {
		super();
		this.parent = parent;
	}

	public boolean accept(File file, String name) {

		if (name.startsWith(ConfigManager.getFileNamePrefix(parent))) {
			return true;
		} else
			return false;

	}
}

class Configureable implements IConfigurable {

	private ConfigResult properties;

	private ConfigSchema schema;
	private String typeName;
	private String description;

	public Configureable(ConfigSchema configSchema, String typeName, String description) {
		super();
		this.schema = configSchema;
		this.typeName = typeName;
		this.description = description;
	}

	public void completeConfiguration(ConfigResult props) throws ConfigException {
		properties = props;
	}

	public ConfigSchema getSchema() {
		return schema;
	}

	public ConfigResult getProperties() {
		return properties;
	}

	public String getTypeName() {
		return typeName;
	}

	public void preConfiguration(ConfigResult props) throws ConfigException {
		// do nothing
	}

	public String getDescription() {
		return description;
	}

}