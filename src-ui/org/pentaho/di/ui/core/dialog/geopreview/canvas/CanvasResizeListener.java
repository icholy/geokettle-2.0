package org.pentaho.di.ui.core.dialog.geopreview.canvas;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.pentaho.di.ui.core.dialog.geopreview.MapMediator;

/**
 * Listen for the resize event and update MapMediator
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public class CanvasResizeListener implements Listener 
{
	// Variables
	private MapMediator mapDirector;
	

	/**
	 * Constructor
	 * 
	 * @param mapDirector The map mediator
	 */
	public CanvasResizeListener(MapMediator mapDirector)
	{
		this.mapDirector = mapDirector;
	}
	

	/**
	 * handle resize event
	 * 
	 */
	public void handleEvent (Event event) 
    {
    	this.mapDirector.setCanvasSize(((Canvas)event.widget).getSize().x, ((Canvas)event.widget).getSize().y);
    }

}
