package ca.neo.ui.models.nodes;

import ca.neo.math.Function;
import ca.neo.model.Node;
import ca.neo.model.StructuralException;
import ca.neo.model.Units;
import ca.neo.model.impl.FunctionInput;
import ca.neo.plot.Plotter;
import ca.neo.ui.configurable.ConfigException;
import ca.neo.ui.configurable.ConfigParam;
import ca.neo.ui.configurable.ConfigParamDescriptor;
import ca.neo.ui.configurable.IConfigurable;
import ca.neo.ui.configurable.managers.UserTemplateConfig;
import ca.neo.ui.configurable.struct.PTFloat;
import ca.neo.ui.configurable.struct.PTFunction;
import ca.neo.ui.configurable.struct.PTInt;
import ca.neo.ui.configurable.struct.PTString;
import ca.neo.ui.models.PNeoNode;
import ca.neo.ui.models.icons.FunctionInputIcon;
import ca.neo.ui.models.tooltips.PropertyPart;
import ca.neo.ui.models.tooltips.TooltipBuilder;
import ca.shu.ui.lib.actions.ActionException;
import ca.shu.ui.lib.actions.StandardAction;
import ca.shu.ui.lib.util.PopupMenuBuilder;

public class PFunctionInput extends PNeoNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static ConfigParamDescriptor pFunction = new PTFunction("Function Type");

	static ConfigParamDescriptor pName = new PTString("Name");

	static final String typeName = "Function Input";

	static ConfigParamDescriptor[] zProperties = { pName, pFunction };

	public PFunctionInput() {
		super();
		init();
	}

	public PFunctionInput(FunctionInput model) {
		super(model);
		init();
	}

	@Override
	public ConfigParamDescriptor[] getConfigSchema() {
		// TODO Auto-generated method stub
		return zProperties;
	}

	@Override
	public FunctionInput getModel() {

		return (FunctionInput) super.getModel();
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	private void init() {
		setIcon(new FunctionInputIcon(this));
	}

	@Override
	protected void afterModelCreated() {
		super.afterModelCreated();
		showAllOrigins();
	}

	@Override
	protected Node configureModel(ConfigParam props)
			throws ConfigException {

		Function function = (Function) props.getProperty(pFunction);
		int dimensions = function.getDimension();

		Function[] functions = new Function[dimensions];

		for (int i = 0; i < dimensions; i++) {
			functions[i] = function;
		}

		// TODO Auto-generated method stub
		try {
			// setName((String) getProperty(pName));
			return new FunctionInput((String) props.getProperty(pName),
					functions, Units.UNK);
		} catch (StructuralException e) {
			throw new ConfigException(e.getMessage());

		}

	}

	@Override
	protected PopupMenuBuilder constructMenu() {
		PopupMenuBuilder menu = super.constructMenu();
		// MenuBuilder plotMenu = menu.createSubMenu("Plot");
		menu.addSection("Function");

		menu.addAction(new PlotFunctionAction(getName(), "Plot function",
				getModel()));
		return menu;

	}

	@Override
	protected TooltipBuilder constructTooltips() {
		TooltipBuilder tooltips = super.constructTooltips();

		tooltips.addPart(new PropertyPart("Dimensions", ""
				+ getModel().getFunctions().length));

		return tooltips;
	}
}

class PlotFunctionAction extends StandardAction implements IConfigurable {
	private static final long serialVersionUID = 1L;

	static final ConfigParamDescriptor pEnd = new PTFloat("End");
	static final ConfigParamDescriptor pIncrement = new PTFloat("Increment");
	static final ConfigParamDescriptor pStart = new PTFloat("Start");
	// static final PropDescriptor pTitle = new PTString("Title");
	FunctionInput functionInput;
	ConfigParamDescriptor pFunctionIndex;
	String plotName;

	public PlotFunctionAction(String plotName, String actionName,
			FunctionInput functionInput) {
		super("Plot function input", actionName);
		this.plotName = plotName;
		this.functionInput = functionInput;

	}

	public void completeConfiguration(ConfigParam properties)
			throws ConfigException {
		String title = plotName + " - Function Plot";

		int functionIndex = (Integer) properties.getProperty(pFunctionIndex);
		float start = (Float) properties.getProperty(pStart);
		float end = (Float) properties.getProperty(pEnd);
		float increment = (Float) properties.getProperty(pIncrement);

		Function[] functions = functionInput.getFunctions();

		if (functionIndex >= functions.length) {
			throw new ConfigException("Function index out of bounds");

		}
		Function function = functionInput.getFunctions()[functionIndex];
		Plotter.plot(function, start, increment, end, title + " ("
				+ function.getClass().getSimpleName() + ")");

	}

	public ConfigParamDescriptor[] getConfigSchema() {

		ConfigParamDescriptor[] properties = { pFunctionIndex, pStart,
				pIncrement, pEnd };
		return properties;

	}

	public String getTypeName() {
		return "Function plotter";
	}

	@Override
	protected void action() throws ActionException {
		pFunctionIndex = new PTInt("Function index", 0, functionInput
				.getFunctions().length - 1);

		UserTemplateConfig config = new UserTemplateConfig(this);
		try {
			config.configureAndWait();
		} catch (ConfigException e) {
			e.defaultHandledBehavior();
		}

	}

}