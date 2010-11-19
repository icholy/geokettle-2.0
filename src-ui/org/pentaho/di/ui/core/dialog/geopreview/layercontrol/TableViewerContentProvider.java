package org.pentaho.di.ui.core.dialog.geopreview.layercontrol;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import org.pentaho.di.ui.core.dialog.geopreview.ILayerListViewer;
import org.pentaho.di.ui.core.dialog.geopreview.Layer;
import org.pentaho.di.ui.core.dialog.geopreview.LayerCollection;

/**	 
*
* 
* @author mouattara & tbadard
* @since 22-03-2009
*/
public class TableViewerContentProvider implements IStructuredContentProvider, ILayerListViewer 
{
	// ---------------- Variables ----------------------
	private LayerCollection layerList;
	private TableViewer tableViewer;
	// --------------------------------------------------
	
	
	/**
	 * Constructor
	 *  
	 * @param layerList
	 * @param tableViewer
	 */
	public TableViewerContentProvider(LayerCollection layerList, TableViewer tableViewer)
	{
		this.layerList = layerList;
		this.tableViewer = tableViewer;
	}
	

	/**
	 * Notifies this content provider that the given viewer's input has been switched to a different element.
	 * 
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) 
	{
		if (newInput != null)
			((LayerCollection) newInput).addLayerListViewer(this);
		if (oldInput != null)
			((LayerCollection) oldInput).removeLayerListViewer(this);
	}

	
	/**
	 * Dispose method
	 */
	public void dispose() 
	{
		layerList.removeLayerListViewer(this);
	}

	
	/**
	 * Return the layers as an array of Objects
	 * Returns the elements to display in the viewer when its input is set to the given element.
	 * 
	 * @param parent Parent element (display child only). DO NOT WORK FOR THIS CLASS
	 * 
	 */
	public Object[] getElements(Object parent) 
	{
		return layerList.getLayers().toArray();
	}

	
	/* (non-Javadoc)
	 * @see ILayerListViewer#addLayer(Layer)
	 */
	public void addLayerEvent(Layer layer) 
	{
		tableViewer.add(layer);
	}

	
	/* (non-Javadoc)
	 * @see ILayerListViewer#removeLayer(Layer)
	 */
	public void removeLayerEvent(Layer layer) 
	{
		tableViewer.remove(layer);
	}

	
	/* (non-Javadoc)
	 * @see ILayerListViewer#updateLayer(ExampleLayer)
	 */
	public void updateLayerEvent(Layer layer) 
	{
		tableViewer.update(layer, null);
	}
	
}
