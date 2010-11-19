package org.pentaho.di.ui.core.dialog.geopreview.canvas;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.ui.core.util.geo.renderer.swt.SWTMapRenderer;

import org.pentaho.di.ui.core.dialog.geopreview.MapMediator;

/**
 *  Listen to the paint event. Method paintControl use a map renderer to draw geometries
 * 	Know the MapMediator.
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009 
 */
public class GeoPreviewPaintListener implements PaintListener 
{ 
	// Variables
	private MapMediator mapDirector;
	
	public GeoPreviewPaintListener(CanvasViewer canvas, Display display, MapMediator mapContext)
	{
		this.mapDirector = mapContext;
	}

	public void paintControl(PaintEvent event) 
    { 
    	SWTMapRenderer mapRenderer;
    	long begin;

    	System.out.println("----------------------");
    	begin = System.currentTimeMillis();
    	
    	// If no geometries to draw, do not draw anything
    	// TODO: Check if this does not cause a bug when all layers are disabled.
    	if (this.mapDirector.getGeoFeaturesSize() <= 0)
    	{
    		return;
    	}
    	
    	// Get the clientArea (where we can draw)
    	Rectangle clientArea = event.display.getClientArea(); 
    	event.gc.setClipping(clientArea);
        
        // Create the map renderer
    	mapRenderer = new SWTMapRenderer(event.gc);

        // Render the objects of the map contain within the enveloppe
        mapRenderer.render(this.mapDirector.getMapContext(), this.mapDirector.getEnvelope());

    	//System.out.println("TOTAL rendering time = " + String.valueOf((System.currentTimeMillis()-begin)) + " ms\n");
    	//System.out.println("----------------------");
    }

}
