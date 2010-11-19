package org.pentaho.di.ui.core.dialog.geopreview;


/**
 * Interface for a viewer on the LayerList class
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public interface ILayerListViewer 
{
	
	/**
	 * Update the view to reflect the fact that a layer was added 
	 * to the layer list
	 * 
	 * @param layer The layer too add
	 */
	public void addLayerEvent(Layer layer);
	

	/**
	 * Update the view to reflect the fact that a layer was removed 
	 * from the layer list
	 * 
	 * @param layer
	 */
	public void removeLayerEvent(Layer Layer);
	
	
	/**
	 * Update the view to reflect the fact that one of the layers
	 * was modified 
	 * 
	 * @param layer
	 */
	public void updateLayerEvent(Layer layer);
}
