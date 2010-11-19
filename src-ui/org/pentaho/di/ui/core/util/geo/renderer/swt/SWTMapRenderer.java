package org.pentaho.di.ui.core.util.geo.renderer.swt;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.opengis.geometry.MismatchedDimensionException;
import org.pentaho.di.ui.core.util.geo.renderer.util.GraphicsConverter;

/**
 * Renderer to a swt graphics
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public class SWTMapRenderer 
{
	// ---------------Constants ------------
	final private Color MAP_DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    // -------------------------------------

	
    // -------------- Variables ------------
	private GTRenderer renderer;
    private GC gc;
    private Image swtImage; 

	protected MapContext map;
    protected MapLayer[] layers;  // The layers to render
    // -------------------------------------


	/**
	 * Constructor
	 * 
	 * @param gc The GC to render on
	 * 
	 */
    public SWTMapRenderer(GC gc) 
    {
    	this.renderer = null;
    	this.map = null;
    	this.layers = null;
    	
		this.gc = gc;
    }
	
	
	/**
	 * This method is used to render a map on a swt GC using GeoTools Renderer
	 * 
	 * @param map
	 * 		The MapContext to render
	 * @param envelope
	 * 		The ReferencedEnvelope
	 * 		The Envelope defines a rectangular region of the 2D coordinate plane. It is often used to represent the bounding box of a Geometry.
	 */ 
	public void render(MapContext map, ReferencedEnvelope envelope)
	{
		// Graphics2D needed to use GeoTools renderer
		Graphics2D graphics;
		// GraphicsConverter use to convert awt graphics2D to swt
		GraphicsConverter graphicsConverter;
		long begin;
		
		// Initialize graphicsConverter
		graphicsConverter = new GraphicsConverter();
		graphicsConverter.prepareRendering(gc);

		// Initialize Graphics2D
		graphics = graphicsConverter.getGraphics2D();
		
		// Render on Graphics2D with GeoTools Renderer 
		try {
			graphics.setColor(this.MAP_DEFAULT_BACKGROUND_COLOR);
            graphics.fillRect(0,0,gc.getClipping().width, gc.getClipping().height);
            
            // Initialise the StreamingRenderer (from geotools)
            GTRenderer renderer = new StreamingRenderer();

            // Set the map on the renderer
            renderer.setContext(map);


            // Set the hints
            // Hints provides a way to control low-level details used by Renderers of GeoTools
            RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED));
            renderer.setJava2DHints(hints);
            // Map<String, Boolean> rendererParams = new HashMap<String, Boolean>();
            Map rendererParams = new HashMap();
            rendererParams.put("optimizedDataLoadingEnabled", Boolean.TRUE);
            renderer.setRendererHints(rendererParams);
            
            // Paint on Graphics2D
            begin = System.currentTimeMillis();
            renderer.paint(graphics, new java.awt.Rectangle(gc.getClipping().width, gc.getClipping().height), envelope);

        	//System.out.print("SWT Map Rendering done in " + String.valueOf((System.currentTimeMillis()-begin)) + " ms\n");
            
        } 
		catch (FactoryRegistryException ex) 
		{
            ex.printStackTrace();
            return;
        } 
		catch (MismatchedDimensionException ex) 
		{
            ex.printStackTrace();
            return;
        } 
		
		// Convert the Graphics2D to SWT GC
        begin = System.currentTimeMillis();
		graphicsConverter.render(gc);
    	//System.out.print("AWT Graphics converted in " + String.valueOf((System.currentTimeMillis()-begin)) + " ms\n");

		return;
	}

	
	/**
	 * Setter for gc. Change the gc to be draw on.
	 * 
	 * @param gc
	 * 		GC to draw on with this Renderer	
	 */
	public void setGC(GC gc)
	{
		this.gc = gc;
	}
}
