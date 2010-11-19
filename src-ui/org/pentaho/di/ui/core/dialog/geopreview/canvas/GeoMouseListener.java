package org.pentaho.di.ui.core.dialog.geopreview.canvas;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.pentaho.di.ui.core.dialog.geopreview.toolbar.ToolbarStates;

import org.pentaho.di.ui.core.dialog.geopreview.MapMediator;

/**
 * Listen to the mouse move and click made on the canvas. Update the MapMediator.
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 * 
 */
public class GeoMouseListener implements org.eclipse.swt.events.MouseListener 
{
	// Variables
	private MapMediator mapDirector;
	private Point startingPoint;

	// Constants
	private final int MOVE_TOLERANCE = 4;
	
	
	
	/**
	 * Constructor 
	 * 
	 * @param mapDirector The MapMediator 
	 * 
	 */
	public GeoMouseListener(MapMediator mapDirector)
	{
		this.mapDirector = mapDirector;
		this.startingPoint = null;
	}
	
	
	/**
	 * Sent when a mouse button is pressed.
	 * 
	 * @param event The event associated with the mouseUp
	 * 
	 */
	public void mouseDown(MouseEvent event) 
	{
		this.startingPoint = new Point(event.x, event.y);

		// If left click and ZoomIn
		if (event.button == 1 && 
			this.mapDirector.getToolbarState() == ToolbarStates.ZOOMIN)
		{
			this.mapDirector.zoomInOnPoint(event.x, event.y);
			this.startingPoint = null;
		}
		// If left click and ZoomOut
		else if (event.button == 1 && this.mapDirector.getToolbarState() == ToolbarStates.ZOOMOUT)
		{
			this.mapDirector.zoomOutOnPoint(event.x, event.y);
			this.startingPoint = null;
		}else if (event.button == 1 && this.mapDirector.getToolbarState() == ToolbarStates.GETINFOS){
			this.mapDirector.getFeatureInformations(event.x, event.y);
		}
	}

	
	/**
	 * Sent when a mouse button is released.
	 *
	 * @param event The event associated with the mouseUp
	 * 
	 */
	public void mouseUp(MouseEvent event) 
	{
		if (event.button == 1 &&
			this.mapDirector.getToolbarState() == ToolbarStates.PAN &&
			this.startingPoint != null)
		{
			
			// Pan only if greater than the tolerance
			if (Math.abs(event.x - startingPoint.x) > MOVE_TOLERANCE &&
				Math.abs(event.y - startingPoint.y) > MOVE_TOLERANCE)
			{
				this.mapDirector.setMove(event.x - startingPoint.x, event.y - startingPoint.y);
				this.startingPoint = null;
			}
		}
	}

	
	/**
	 * Sent when a mouse button is pressed twice within the 
	 * (operating system specified) double click period.
	 * 
	 * @param event The event associated with the doubleClick
	 * 
	 */
	public void mouseDoubleClick(MouseEvent event) 
	{
		// Recenter the map where the double click have been done
		// (Only if in pan mode or no mode selected)
		if (event.button == 1 &&
			this.mapDirector.getToolbarState() == ToolbarStates.PAN ||
			this.mapDirector.getToolbarState() == ToolbarStates.NOSELECTION)
		{
			this.mapDirector.recenter(event.x, event.y);
		}
	}
	public void mouseClick(MouseEvent event){
		//this.mapDirector.getFeatureInformations(event.x, event.y);
	}
	
}
