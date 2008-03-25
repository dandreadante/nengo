package ca.nengo.ui.models.viewers;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import ca.nengo.model.Node;
import ca.nengo.ui.actions.SaveNodeAction;
import ca.nengo.ui.models.ModelsContextMenu;
import ca.nengo.ui.models.UINeoNode;
import ca.nengo.ui.models.nodes.UINodeViewable;
import ca.shu.ui.lib.actions.LayoutAction;
import ca.shu.ui.lib.objects.activities.TrackedStatusMsg;
import ca.shu.ui.lib.objects.models.ModelObject;
import ca.shu.ui.lib.util.UIEnvironment;
import ca.shu.ui.lib.util.Util;
import ca.shu.ui.lib.util.menus.MenuBuilder;
import ca.shu.ui.lib.util.menus.PopupMenuBuilder;
import ca.shu.ui.lib.world.Interactable;
import ca.shu.ui.lib.world.WorldObject;
import ca.shu.ui.lib.world.elastic.ElasticWorld;
import ca.shu.ui.lib.world.handlers.AbstractStatusHandler;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * Viewer for looking at NEO Node models
 * 
 * @author Shu
 */
public abstract class NodeViewer extends ElasticWorld implements Interactable {
	private static final long serialVersionUID = 1L;

	private MyNodeListener myNodeListener;

	/**
	 * Viewer Parent
	 */
	private final UINodeViewable parentOfViewer;;

	/**
	 * Children of NEO nodes
	 */
	protected final Hashtable<Node, UINeoNode> neoNodesChildren = new Hashtable<Node, UINeoNode>();

	/**
	 * @param nodeContainer
	 *            UI Object containing the Node model
	 */
	public NodeViewer(UINodeViewable nodeContainer) {
		super(nodeContainer.getName() + " (" + nodeContainer.getTypeName() + " Viewer)");
		this.parentOfViewer = nodeContainer;

		initialize();
	}

	private void initChildModelListener() {
		myNodeListener = new MyNodeListener();

		getGround().addChildrenListener(new WorldObject.ChildListener() {
			public void childAdded(WorldObject wo) {

				if (wo instanceof UINeoNode) {
					((UINeoNode) wo).addModelListener(myNodeListener);
				}
			}

			public void childRemoved(WorldObject wo) {
				if (wo instanceof UINeoNode) {
					((UINeoNode) wo).removeModelListener(myNodeListener);
				}
			}
		});
	}

	/**
	 * @param node
	 *            node to be added
	 * @param updateModel
	 *            if true, the network model is updated. this may be false, if
	 *            it is known that the network model already contains this node
	 * @param dropInCenterOfCamera
	 *            whether to drop the node in the center of the camera
	 * @param moveCameraToNode
	 *            whether to move the camera to where the node is
	 */
	protected void addUINode(UINeoNode node, boolean dropInCenterOfCamera, boolean moveCameraToNode) {

		/**
		 * Moves the camera to where the node is positioned, if it's not dropped
		 * in the center of the camera
		 */
		if (moveCameraToNode) {
			getWorld().animateToSkyPosition(node.getOffset().getX(), node.getOffset().getY());
		}

		neoNodesChildren.put(node.getModel(), node);

		if (dropInCenterOfCamera) {
			getGround().addChildFancy(node, dropInCenterOfCamera);
		} else {
			getGround().addChild(node);
		}

	}

	public Point2D localToView(Point2D localPoint) {
		localPoint = getSky().parentToLocal(localPoint);
		localPoint = getSky().localToView(localPoint);
		return localPoint;
	}

	protected abstract boolean canRemoveChildModel(Node node);

	@Override
	protected void constructLayoutMenu(MenuBuilder menu) {
		super.constructLayoutMenu(menu);
		MenuBuilder sortMenu = menu.addSubMenu("Sort");

		sortMenu.addAction(new SortNodesAction(SortMode.BY_NAME));
		sortMenu.addAction(new SortNodesAction(SortMode.BY_TYPE));
	}

	protected void initialize() {
		initChildModelListener();

		setStatusBarHandler(new NodeViewerStatus(this));

		TrackedStatusMsg msg = new TrackedStatusMsg("Building nodes in Viewer");

		updateViewFromModel(true);

		msg.finished();

	}

	protected abstract void removeChildModel(Node node);

	/**
	 * Called when the model changes. Updates the viewer based on the NEO model.
	 */
	protected abstract void updateViewFromModel(boolean isFirstUpdate);

	/**
	 * Applies the default layout
	 */
	public abstract void applyDefaultLayout();

	/**
	 * Applies a square layout which is sorted
	 * 
	 * @param sortMode
	 *            Type of sort layout to use
	 */
	@SuppressWarnings("unchecked")
	public void applySortLayout(SortMode sortMode) {
		getGround().setElasticEnabled(false);

		List<UINeoNode> nodes = getUINodes();

		switch (sortMode) {
		case BY_NAME:
			Collections.sort(nodes, new Comparator<UINeoNode>() {

				public int compare(UINeoNode o1, UINeoNode o2) {
					return (o1.getName().compareToIgnoreCase(o2.getName()));

				}

			});

			break;
		case BY_TYPE:
			Collections.sort(nodes, new Comparator<UINeoNode>() {

				public int compare(UINeoNode o1, UINeoNode o2) {
					if (o1.getClass() != o2.getClass()) {

						return o1.getClass().getSimpleName().compareToIgnoreCase(
								o2.getClass().getSimpleName());
					} else {
						return (o1.getName().compareToIgnoreCase(o2.getName()));
					}

				}

			});

			break;
		}

		/*
		 * basic rectangle layout variables
		 */
		double x = 0;
		double y = 0;

		int numberOfColumns = (int) Math.sqrt(nodes.size());
		int columnCounter = 0;

		double startX = Double.MAX_VALUE;
		double startY = Double.MAX_VALUE;
		double maxRowHeight = 0;
		double endX = Double.MIN_VALUE;
		double endY = Double.MIN_VALUE;

		if (nodes.size() > 0) {
			for (UINeoNode node : nodes) {

				node.animateToPosition(x, y, 1000);

				if (x < startX) {
					startX = x;
				} else if (x + node.getWidth() > endX) {
					endX = x + node.getWidth();
				}

				if (y < startY) {
					startY = y;
				} else if (y + node.getHeight() > endY) {
					endY = y + node.getHeight();
				}

				if (node.getFullBounds().getHeight() > maxRowHeight) {
					maxRowHeight = node.getFullBounds().getHeight();
				}

				x += node.getFullBounds().getWidth() + 50;

				if (++columnCounter > numberOfColumns) {
					x = 0;
					y += maxRowHeight + 50;
					maxRowHeight = 0;
					columnCounter = 0;
				}
			}

		}

		PBounds fullBounds = new PBounds(startX, startY, endX - startX, endY - startY);
		zoomToBounds(fullBounds);

	}

	@Override
	public void constructMenu(PopupMenuBuilder menu) {
		super.constructMenu(menu);

		/*
		 * File menu
		 */
		menu.addSection("File");
		menu.addAction(new SaveNodeAction(getViewerParent()));

	}

	/**
	 * @return NEO Model represented by the viewer
	 */
	public Node getModel() {
		return parentOfViewer.getModel();
	}

	/**
	 * @return A collection of NEO Nodes contained in this viewer
	 */
	public List<UINeoNode> getUINodes() {
		Enumeration<UINeoNode> en = neoNodesChildren.elements();

		ArrayList<UINeoNode> nodesList = new ArrayList<UINeoNode>(neoNodesChildren.size());

		while (en.hasMoreElements()) {
			nodesList.add(en.nextElement());
		}

		return nodesList;
	}

	public UINeoNode getUINode(Node node) {
		return neoNodesChildren.get(node);
	}

	@Override
	protected void constructSelectionMenu(Collection<WorldObject> selection, PopupMenuBuilder menu) {
		super.constructSelectionMenu(selection, menu);
		ArrayList<ModelObject> models = new ArrayList<ModelObject>(selection.size());

		for (WorldObject object : selection) {
			if (object instanceof ModelObject) {
				models.add((ModelObject) object);
			}
		}

		ModelsContextMenu.constructMenu(menu, models);

	}

	/**
	 * @return Parent of this viewer
	 */
	public UINodeViewable getViewerParent() {
		return parentOfViewer;
	}

	public void setOriginsTerminationsVisible(boolean visible) {
		for (UINeoNode node : getUINodes()) {
			node.setWidgetsVisible(visible);
		}
	}

	public void updateViewFromModel() {
		updateViewFromModel(false);
	}

	private class MyNodeListener implements ModelObject.ModelListener {

		public void modelDestroyed(Object model) {
			removeChildModel((Node) model);
		}

		public void modelDestroyStarted(Object model) {
			if (!canRemoveChildModel((Node) model)) {
				throw new UnsupportedOperationException("Removing nodes not supported here");
			}

		}

	}

	/**
	 * Supported types of sorting allowed in layout
	 * 
	 * @author Shu Wu
	 */
	public static enum SortMode {
		BY_NAME("Name"), BY_TYPE("Type");

		private String name;

		SortMode(String name) {
			this.name = name;
		}

		protected String getName() {
			return name;
		}
	}

	/**
	 * Action to apply a sorting layout
	 * 
	 * @author Shu Wu
	 */
	class SortNodesAction extends LayoutAction {

		private static final long serialVersionUID = 1L;
		SortMode sortMode;

		public SortNodesAction(SortMode sortMode) {
			super(NodeViewer.this, "Sort nodes by " + sortMode.getName(), sortMode.getName());
			this.sortMode = sortMode;
		}

		@Override
		protected void applyLayout() {
			applySortLayout(sortMode);
		}

	}

	/**
	 * Zooms the viewer to optimally fit all nodes
	 * 
	 * @author Shu Wu
	 */
	class ZoomToFitActivity extends PActivity {

		public ZoomToFitActivity() {
			super(0);
			UIEnvironment.getInstance().addActivity(this);
		}

		@Override
		protected void activityStarted() {
			zoomToFit();
		}

	}

}

/**
 * Handler which updates the status bar of NeoGraphics to display information
 * about the node which the mouse is hovering over.
 * 
 * @author Shu Wu
 */
class NodeViewerStatus extends AbstractStatusHandler {

	public NodeViewerStatus(NodeViewer world) {
		super(world);
	}

	@Override
	protected String getStatusMessage(PInputEvent event) {
		ModelObject wo = (ModelObject) Util.getNodeFromPickPath(event, ModelObject.class);

		StringBuilder statusStr = new StringBuilder(200);
		if (getWorld().getGround().isElasticMode()) {
			statusStr.append("Elastic layout enabled | ");
		}
		statusStr.append(getWorld().getViewerParent().getFullName() + " -> ");

		if (getWorld().getSelection().size() > 1) {
			statusStr.append(getWorld().getSelection().size() + " Objects selected");

		} else {

			if (wo != null) {
				statusStr.append(wo.getFullName());
			} else {
				statusStr.append("No Model Selected");
			}
		}
		return statusStr.toString();
	}

	@Override
	protected NodeViewer getWorld() {
		return (NodeViewer) super.getWorld();
	}

}