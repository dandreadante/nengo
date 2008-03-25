package ca.nengo.ui.configurable.descriptors;

import ca.nengo.ui.configurable.Property;
import ca.nengo.ui.configurable.PropertyInputPanel;
import ca.nengo.ui.configurable.panels.FloatPanel;


/**
 * Config Descriptor for Floats
 * 
 * @author Shu Wu
 * 
 */
public class PFloat extends Property {

	private static final long serialVersionUID = 1L;

	public PFloat(String name) {
		super(name);
	}

	public PFloat(String name, float defaultValue) {
		super(name, defaultValue);
	}

	@Override
	protected PropertyInputPanel createInputPanel() {
		return new FloatPanel(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getTypeClass() {
		/*
		 * Return the primitive type
		 */
		return float.class;
	}

	@Override
	public String getTypeName() {
		return "Float";
	}

}