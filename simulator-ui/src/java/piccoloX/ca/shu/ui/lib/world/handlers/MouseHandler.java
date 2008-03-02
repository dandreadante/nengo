package ca.shu.ui.lib.world.handlers;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JPopupMenu;

import ca.shu.ui.lib.Style.Style;
import ca.shu.ui.lib.util.Util;
import ca.shu.ui.lib.world.Interactable;
import ca.shu.ui.lib.world.WorldObject;
import ca.shu.ui.lib.world.piccolo.WorldImpl;
import ca.shu.ui.lib.world.piccolo.objects.SelectionBorder;
import ca.shu.ui.lib.world.piccolo.objects.Window;
import ca.shu.ui.lib.world.piccolo.primitives.PiccoloNodeInWorld;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Handles mouse events. Passes double click and mouse context button events to
 * World Objects. Displays a frame around interactable objects as the mouse
 * moves.
 * 
 * @author Shu Wu
 */
public class MouseHandler extends PBasicInputEventHandler {

	/**
	 * Hand cursor
	 */
	private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);

	/**
	 * Maximum distance that the mouse is allowed to drag before the handler
	 * gives up on the context menu
	 */
	private static final double MAX_CONTEXT_MENU_DRAG_DISTANCE = 20;

	private SelectionBorder frame;

	private boolean handCursorShown = false;

	private Interactable interactableObj;

	// private int mouseButtonPressed = -1;

	private Point2D mouseCanvasPositionPressed;

	private WorldImpl world;

	public MouseHandler(WorldImpl world) {
		super();
		frame = new SelectionBorder(world);
		frame.setFrameColor(Style.COLOR_TOOLTIP_BORDER);
		this.world = world;
	}

	/**
	 * @return Interactable object
	 */
	private Interactable getInteractableFromEvent(PInputEvent event) {
		Interactable obj = (Interactable) Util.getNodeFromPickPath(event, Interactable.class);

		if (obj == null || !world.isAncestorOf(obj)) {
			return null;
		} else {
			return obj;
		}
	}

	/**
	 * @param event
	 * @return Was Popup Triggered?
	 */
	private boolean maybeTriggerPopup(PInputEvent event) {
		if (event.isPopupTrigger()) {
			Util.debugMsg("Context Menu: Popup Trigger detected");
			JPopupMenu menuToShow = world.getSelectionMenu(world.getSelection());

			if (menuToShow == null && (interactableObj != null)
					&& (interactableObj == getInteractableFromEvent(event))) {
				menuToShow = interactableObj.getContextMenu();
			}

			if (menuToShow != null) {
				Util.debugMsg("Context Menu: shown");
				MouseEvent e = (MouseEvent) event.getSourceSwingEvent();

				menuToShow.show(e.getComponent(), e.getPoint().x, e.getPoint().y);
				menuToShow.setVisible(true);
			} else {
				Util.debugMsg("Context Menu: Unable to find menu to show");
			}
			return true;
		}
		return false;
	}

	@Override
	public void mouseClicked(PInputEvent event) {
		if (event.getClickCount() == 2) {
			PNode node = event.getPickedNode();

			while (node != null) {
				if (node instanceof PiccoloNodeInWorld) {

					WorldObject wo = ((PiccoloNodeInWorld) node).getWorldObject();

					if (wo.isSelectable()) {
						wo.doubleClicked();
						break;
					}

				}
				node = node.getParent();
			}

		}
		super.mouseClicked(event);

	}

	@Override
	public void mouseMoved(PInputEvent event) {

		Interactable obj = getInteractableFromEvent(event);

		/*
		 * Show cursor and frame around interactable objects NOTE: Do not show
		 * cursor and frame around Windows or Worlds
		 */
		if (obj == null || (obj instanceof Window) || (obj instanceof WorldImpl)) {
			if (handCursorShown) {
				handCursorShown = false;
				event.getComponent().popCursor();
			}

			frame.setSelected(null);
		} else {
			if (!handCursorShown) {
				handCursorShown = true;
				event.getComponent().pushCursor(HAND_CURSOR);
			}

			frame.setSelected((WorldObject) obj);

		}

	}

	@Override
	public void mousePressed(PInputEvent event) {
		super.mousePressed(event);

		mouseCanvasPositionPressed = event.getCanvasPosition();
		interactableObj = getInteractableFromEvent(event);

		maybeTriggerPopup(event);
	}

	@Override
	public void mouseReleased(PInputEvent event) {
		super.mouseReleased(event);

		/*
		 * Check the mouse hasn't moved too far off from it's pressed position
		 */
		if (mouseCanvasPositionPressed.distance(event.getCanvasPosition()) < MAX_CONTEXT_MENU_DRAG_DISTANCE) {
			maybeTriggerPopup(event);
		}

	}
}
