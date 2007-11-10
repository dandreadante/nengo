package ca.neo.ui.configurable.descriptors.functions;

import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import ca.neo.ui.configurable.ConfigException;
import ca.neo.ui.configurable.IConfigurable;
import ca.neo.ui.configurable.PropertyDescriptor;
import ca.neo.ui.configurable.PropertyInputPanel;
import ca.neo.ui.configurable.PropertySet;
import ca.neo.ui.configurable.managers.UserConfigurer;
import ca.shu.ui.lib.util.UserMessages;
import ca.shu.ui.lib.util.Util;

/**
 * Input panel for entering an Array of Functions
 * 
 * @author Shu Wu
 */
public class FunctionArrayPanel extends PropertyInputPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Function array
	 */
	private FunctionWrapper[] myFunctionsWr;

	/**
	 * Text field component for entering the dimensions of the function array
	 */
	private JTextField tf;
	private int inputDimension;

	public FunctionArrayPanel(PFunctionArray property, int inputDimension) {
		super(property);
		initPanel();
		this.inputDimension = inputDimension;
	}

	/**
	 * Edits the Function Array using a child dialog
	 */
	protected void editFunctionArray() {
		if (!isOutputDimensionsSet()) {
			UserMessages.showWarning("Output dimensions not set");
			return;
		}

		/*
		 * get the JDialog parent
		 */
		Container parent = getParent();
		while (parent != null) {
			if (parent instanceof JDialog)
				break;
			parent = parent.getParent();
		}

		if (parent != null && parent instanceof JDialog) {
			ConfigurableFunctionArray configurableFunctions = new ConfigurableFunctionArray(
					getInputDimension(), getOutputDimension(), getValue());

			UserConfigurer config = new UserConfigurer(configurableFunctions,
					(JDialog) parent);
			try {
				config.configureAndWait();
				setValue(configurableFunctions.getFunctions());
			} catch (ConfigException e) {
				e.defaultHandleBehavior();
			}

		} else {
			UserMessages.showError("Could not attach properties dialog");
		}

	}

	@Override
	public PFunctionArray getDescriptor() {
		return (PFunctionArray) super.getDescriptor();
	}

	public int getOutputDimension() {

		Integer integerValue = new Integer(tf.getText());
		return integerValue.intValue();

	}

	@Override
	public FunctionWrapper[] getValue() {
		return myFunctionsWr;
	}

	private void initPanel() {
		JLabel dimensions = new JLabel("Output Dimensions: ");
		tf = new JTextField(10);
		addToPanel(dimensions);
		addToPanel(tf);

		JButton configureFunction = new JButton(new EditFunctions());
		addToPanel(tf);
		addToPanel(configureFunction);

	}

	/**
	 * @return True if Function Array dimensions has been set
	 */
	public boolean isOutputDimensionsSet() {
		String textValue = tf.getText();

		if (textValue == null || textValue.compareTo("") == 0)
			return false;

		try {
			@SuppressWarnings("unused")
			Integer value = getOutputDimension();

		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}

	@Override
	public boolean isValueSet() {
		if (myFunctionsWr != null
				&& (myFunctionsWr.length == getOutputDimension())) {
			return true;
		} else {
			setStatusMsg("Functions not set");
		}

		return false;
	}

	/**
	 * @param dimensions
	 *            Dimensions of the function array
	 */
	public void setDimensions(int dimensions) {
		tf.setText(dimensions + "");

	}

	@Override
	public void setValue(Object value) {
		FunctionWrapper[] functions = (FunctionWrapper[]) value;

		/*
		 * Check that the functions are of the correct dimension before
		 * committing
		 */
		for (int i = 0; i < functions.length; i++) {
			if (functions[i].unwrap().getDimension() != getInputDimension()) {
				Util.debugMsg("Function values do not match, discarded");
				return;
			}
		}

		if (value != null) {
			myFunctionsWr = functions;
			setDimensions(myFunctionsWr.length);
			setStatusMsg("");
		} else {

		}
	}

	/**
	 * Edit Functions Action
	 * 
	 * @author Shu Wu
	 */
	class EditFunctions extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public EditFunctions() {
			super("Set Functions");
		}

		public void actionPerformed(ActionEvent e) {
			editFunctionArray();

		}

	}

	public int getInputDimension() {
		return inputDimension;
	}
}

/**
 * Configurable object which creates an array of functions
 * 
 * @author Shu Wu
 */
/**
 * @author Shu
 */
class ConfigurableFunctionArray implements IConfigurable {

	/**
	 * Number of functions to be created
	 */
	private int outputDimension;

	/**
	 * Dimensions of the functions to be created
	 */
	private int inputDimension;

	/**
	 * Array of functions to be created
	 */
	private FunctionWrapper[] myFunctions;

	private FunctionWrapper[] defaultValues;

	/**
	 * @param outputDimension
	 *            Number of functions to create
	 */
	public ConfigurableFunctionArray(int inputDimension, int outputDimension,
			FunctionWrapper[] defaultValues) {
		super();
		this.defaultValues = defaultValues;
		init(inputDimension, outputDimension);

	}

	/**
	 * Initializes this instance
	 * 
	 * @param outputDimension
	 *            number of functions to create
	 */
	private void init(int inputDimension, int outputDimension) {
		this.inputDimension = inputDimension;
		this.outputDimension = outputDimension;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.neo.ui.configurable.IConfigurable#completeConfiguration(ca.neo.ui.configurable.ConfigParam)
	 */
	public void completeConfiguration(PropertySet properties) {
		myFunctions = new FunctionWrapper[outputDimension];
		for (int i = 0; i < outputDimension; i++) {
			myFunctions[i] = ((FunctionWrapper) properties
					.getProperty("Function " + i));

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.neo.ui.configurable.IConfigurable#getConfigSchema()
	 */
	public PropertyDescriptor[] getConfigSchema() {
		PropertyDescriptor[] props = new PropertyDescriptor[outputDimension];

		for (int i = 0; i < outputDimension; i++) {

			FunctionWrapper defaultValue = null;

			if (defaultValues != null && i < defaultValues.length
					&& defaultValues[i] != null) {
				defaultValue = defaultValues[i];

			}
			PFunction function = new PFunction("Function " + i, inputDimension,
					false, defaultValue);

			props[i] = function;
		}

		return props;
	}

	/**
	 * @return Functions created
	 */
	public FunctionWrapper[] getFunctions() {
		return myFunctions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.neo.ui.configurable.IConfigurable#getTypeName()
	 */
	public String getTypeName() {
		return outputDimension + "x Functions";
	}

	public void preConfiguration(PropertySet props) throws ConfigException {
		// do nothing
	}

}