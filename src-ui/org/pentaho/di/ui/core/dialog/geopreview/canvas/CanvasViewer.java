package org.pentaho.di.ui.core.dialog.geopreview.canvas;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * Extends canvas and act also as an observer on MapController
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public class CanvasViewer extends Canvas implements Observer
{
	
	/**
	 * Contructor
	 * @param parent
	 * @param style
	 * 
	 */
	public CanvasViewer(Composite parent, int style)
	{
		super(parent, style);
	}
	
	
	/**
	 * Called by the observable when the data observed changed
	 * 
	 * @param observable The observed class
	 * @param o The object that have changed
	 */
	public void update(Observable observable, Object o)
	{
		// On change of the data, redraw the map
		this.redraw();
	}
}
