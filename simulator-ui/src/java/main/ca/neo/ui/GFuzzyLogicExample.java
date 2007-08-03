package ca.neo.ui;

import ca.neo.examples.FuzzyLogicExample;
import ca.neo.model.Network;
import ca.neo.model.StructuralException;
import ca.neo.ui.models.nodes.PNetwork;
import ca.shu.ui.lib.objects.widgets.TrackedTask;
import ca.shu.ui.lib.util.Util;

/**
 * In this example, the FuzzyLogic network is constructed from an existing
 * Network Model
 * 
 * @author Shu
 * 
 */
public class GFuzzyLogicExample {


	public static void main(String[] args) {

		
		NeoGraphics neoGraphics = new NeoGraphics("FuzzyLogic Example");

		try {
			TrackedTask task = new TrackedTask(
					"Creating FuzzyLogic NEO Network model");
			Network network = FuzzyLogicExample.createNetwork();
			task.finished();

			task = new TrackedTask("Constructing UI");
			PNetwork networkUI = new PNetwork(network);

			neoGraphics.addWorldObject(networkUI);

			networkUI.openViewer();
			task.finished();

		} catch (StructuralException e) {
			Util.Error("Could not create network: " + e.toString());
		}
	}

}