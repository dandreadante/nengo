package ca.neo.ui.models.nodes;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ca.neo.model.Node;
import ca.neo.ui.configurable.ConfigException;
import ca.neo.ui.configurable.ConfigParam;
import ca.neo.ui.configurable.ConfigParamDescriptor;
import ca.neo.ui.models.PNeoNode;
import ca.neo.ui.models.icons.NeuronIcon;
import ca.shu.ui.lib.util.Util;

public class PNeuron extends PNeoNode {

	private static final long serialVersionUID = 1L;

	public PNeuron() {
		super();
		init();
	}

	public PNeuron(Node model) {
		super(model);
		init();
	}

	@Override
	public ConfigParamDescriptor[] getConfigSchema() {
		Util.UserError("not implemented yet");
		return null;

	}

	@Override
	public String getTypeName() {
		return "Neuron";
	}

	/**
	 * Initializes the PNetwork
	 */
	private void init() {

		setIcon(new NeuronIcon(this));
	}

	@Override
	protected Object configureModel(ConfigParam configuredProperties)
			throws ConfigException {
		throw new NotImplementedException();
	}

}
