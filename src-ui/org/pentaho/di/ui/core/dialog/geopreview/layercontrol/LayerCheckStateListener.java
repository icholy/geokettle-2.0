package org.pentaho.di.ui.core.dialog.geopreview.layercontrol;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;

import org.pentaho.di.ui.core.dialog.geopreview.Layer;

/**
 * Listen to the click on the checkbox contained in a TableViewer 
 *
 * @author mouattara & tbadard
 * @since 22-03-2009 
 */
public class LayerCheckStateListener implements ICheckStateListener 
{
	
	/**
	 * Occurs when the checkstate change
	 * 
	 * @param event The event
	 * 
	 */
	public void checkStateChanged(CheckStateChangedEvent event) 
	{
		Layer layer = null;
		
		// Set the layer visible or not depending on the checkmark
		if (event.getElement() instanceof Layer) {
		  layer = (Layer)event.getElement();
		  layer.setVisible(event.getChecked());
	    }
	}

}
