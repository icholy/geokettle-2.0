package org.pentaho.di.ui.trans.steps.srstransformation;

import java.util.HashMap;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TreeItem;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.trans.steps.srstransformation.SRSList;

/**
 * {@link SRSTreeView} encapsulates the creation of the <code>Tree</code> in a
 * separate {@link Thread}. {@link SRSTreeView} needs the two root
 * {@link TreeItem} which are extended by their children nodes. The list of all
 * available SRS (delivered by GeoTools library) is created only once in a
 * separate thread ({@link SRSList}). All instances of {@link SRSTreeView}
 * have to wait until this worker-thread is completed.
 * 
 * @author phobus, sgoldinger
 * @since 12-nov-2008
 */
public class SRSTreeView extends Thread {
	public static final String SELECTION_CODE = "code";
	public static final String SELECTION_FACTORY = "FACTORY";
	//	private TreeItem nodeFavorites;
	private TreeItem nodeAll;
	private SRSList srsList;
	private String selectedNodeText;
	private HashMap<String, TreeItem> selectionMap;
	private Object mutex = new Object(); 

	/**
	 * Create a new instance of {@link SRSTreeView} that is a {@link Thread} and
	 * start it.
	 * 
	 * @param srsList The {@link Thread} that creates the list with all available SRS.
	 * @param nodeFavorites The {@link TreeItem} containing the favorite SRS.
	 * @param nodeAll The {@link TreeItem} containing all SRS.
	 */
	public SRSTreeView(SRSList srsList, TreeItem nodeFavorites, TreeItem nodeAll, String selectedNodeText) {
		//		this.nodeFavorites = nodeFavorites;
		this.nodeAll = nodeAll;
		this.srsList = srsList;
		this.selectedNodeText = selectedNodeText;
		start();
	}

	public void run() {
		Display display = Display.getDefault();
		// Favorite SRS (sorted, manually added). Identical SRS have the same
		// comperator-result and will have the same Hash in the TreeSet.
		//		TreeSet<SRS> favoriteSRS = new TreeSet<SRS>();
		//		favoriteSRS.add(new SRS(DefaultGeographicCRS.WGS84));
		//		favoriteSRS.add(new SRS(DefaultGeographicCRS.WGS84_3D));
		//		favoriteSRS.add(new SRS(DefaultGeocentricCRS.CARTESIAN));
		//		favoriteSRS.add(new SRS(DefaultGeocentricCRS.SPHERICAL));
		//		favoriteSRS.add(new SRS(DefaultEngineeringCRS.CARTESIAN_2D));
		//		favoriteSRS.add(new SRS(DefaultEngineeringCRS.CARTESIAN_3D));
		//		favoriteSRS.add(new SRS(DefaultEngineeringCRS.GENERIC_2D));
		//		favoriteSRS.add(new SRS(DefaultEngineeringCRS.GENERIC_3D));
		//		favoriteSRS.add(new SRS(DefaultVerticalCRS.ELLIPSOIDAL_HEIGHT));

		// Create the TreeItems for the favorite-SRS (SYNC). Because it is a short list of
		// favorite SRS, this call does not need to be asynchronous.
		//		display.syncExec( new SRSTreeFiller(favoriteSRS, nodeFavorites, selectedNodeText) );

		// Create the TreeItems for all SRS (ASYNC)
		display.asyncExec( new SRSTreeFiller(srsList.getAllSRS(), nodeAll, selectedNodeText) );
	}

	public void markSelection(String selectedNodeText) {
		synchronized (mutex) {
			TreeItem selectedNode = selectionMap != null ? selectionMap.get(selectedNodeText) : null;
			if(!nodeAll.isDisposed()) { 
				if (selectedNode != null) {
					nodeAll.getParent().setSelection(selectedNode);
					Event e = new Event();
					e.item = selectedNode;
					e.type = SWT.Selection;
					nodeAll.getParent().notifyListeners(SWT.Selection, e);
					nodeAll.setExpanded(true);
				} else {
					nodeAll.getParent().deselectAll();
				}
			}
		}
	}

	/**
	 * This helper-class is an instance of a {@link Runnable} that can be invoked by the GUI
	 * to fill-up the the <code>Tree</code> with a list of all {@link CoordinateReferenceSystem}
	 * provided via the constructor-call.
	 * 
	 * @author phobus, sgoldinger
	 * @since 15-nov-2008
	 */
	class SRSTreeFiller implements Runnable {
		private TreeItem parent;
		private TreeSet<SRS> treeEntries;
		private String selection;

		/**
		 * Creates a new {@link SRSTreeFiller} instance that can be invoked by a GUI thread.
		 * 
		 * @param treeEntries {@link TreeItem}s that should be added to a root-{@link TreeItem}.
		 * @param parent The parent {@link TreeItem}.
		 * @param selection The text of the {@link TreeItem} that should be selected.
		 */
		public SRSTreeFiller(TreeSet<SRS> treeEntries, TreeItem parent, String selection) {
			this.treeEntries = treeEntries;
			this.parent = parent;
			this.selection = selection;
		}

		public void run() {
			// The selectionMap is used to quickly select an item. By using a HashMap
			// more memory is used, but the GUI thread is blocked for a much shorter time.
			// This trick allows selection with constant time consumption.
			boolean selected = (selection != null);

			synchronized (mutex) {
				selectionMap = selected ? new HashMap<String, TreeItem>() : null;

				// Building the list
				// synchronized (treeEntries) {
				for (SRS entry : treeEntries) {
					if (!parent.isDisposed()) {
						TreeItem child = new TreeItem(parent, SWT.NONE);
						child.setText(0, entry.description);
						child.setText(1, entry.authority + ":" + entry.srid);
						child.setData(entry);
						if (selected) {
							//synchronized(selectionMap) {
							selectionMap.put(entry.description, child);
							//}
						}
					} else break;
				}
			}

			// Select an item
			if (selected) {
				markSelection(selection);
			}
		}
	}
}
