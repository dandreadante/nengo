package ca.shu.ui.lib.world.piccolo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import ca.shu.ui.lib.world.WorldLayer;
import ca.shu.ui.lib.world.WorldObject;
import ca.shu.ui.lib.world.piccolo.objects.Window;
import ca.shu.ui.lib.world.piccolo.primitives.PiccoloNodeInWorld;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PNodeFilter;

public class WorldLayerImpl extends WorldObjectImpl implements WorldLayer {

	/**
	 * World this layer belongs to
	 */
	protected WorldImpl world;

	/**
	 * Create a new ground layer
	 * 
	 * @param world
	 *            World this layer belongs to
	 */
	public WorldLayerImpl(String name, PiccoloNodeInWorld node) {
		super(name, node);
	}

	public Collection<Window> getAllWindows() {
		PNodeFilter filter = new PNodeFilter() {

			public boolean accept(PNode node) {
				if (node instanceof PiccoloNodeInWorld) {
					if (((PiccoloNodeInWorld) node).getWorldObject() instanceof Window) {
						return true;
					}
				}
				return false;
			}

			public boolean acceptChildrenOf(PNode node) {
				return accept(node);
			}

		};
		LinkedList<PNode> filteredNodes = new LinkedList<PNode>();
		getPiccolo().getAllNodes(filter, filteredNodes);

		ArrayList<Window> windows = new ArrayList<Window>(filteredNodes.size());

		for (PNode node : filteredNodes) {
			windows.add((Window) ((PiccoloNodeInWorld) node).getWorldObject());
		}

		return windows;
	}

	/**
	 * Removes and destroys children
	 */
	public void clearLayer() {
		for (WorldObject wo : getChildren()) {
			wo.destroy();
		}
	}

	@Override
	public WorldImpl getWorld() {
		return world;
	}

	public void setWorld(WorldImpl world) {
		this.world = world;
	}

}