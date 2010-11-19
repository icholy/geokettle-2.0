package org.pentaho.di.ui.core.util.geo.renderer.swt;

import java.awt.Color;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Factory to create the layers style and the map context. It deeply depends on geotools library.
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public class LayerFactory 
{
	// ---------------Constants ------------
    private final String DEFAULT_MAP_TITLE = "Geographic Preview OMM";
    private final String DEFAULT_ABSTRACT = "This is a geographic preview";
    private final String DEFAULT_POINT_LAYER_STYLE_NAME = "Point Layer Style";
    private final String DEFAULT_POLYGON_LAYER_STYLE_NAME = "Polygon Layer Style";
    private final String DEFAULT_LINE_LAYER_STYLE_NAME = "Line Layer Style";
    private final String DEFAULT_ALL_STYLE_LAYER_NAME = "All Style";
    
    private final double DEFAULT_POINT_OPACITY = 1.0;
    private final double DEFAULT_POINT_ROTATION = 0;
    private final String DEFAULT_OPACITY="1.0";
    
    //public final String DEFAULT_POLYGON_STROKE_HTML_COLOR = "#000000";
    public final String DEFAULT_POLYGON_STROKE_HTML_COLOR = "#000000"; 
    public final int DEFAULT_POLYGON_STROKE_WIDTH = 1;
    public final String DEFAULT_POLYGON_FILL_HTML_COLOR = "#FFFFFFF";//"#9966993";    

    public final double DEFAULT_POINT_SIZE = 6.0; 
    
    public final String DEFAULT_LINE_STROKE_HTML_COLOR = "#0033cc";
    public final int DEFAULT_LINE_STROKE_WIDTH = 1; 
    
	private final String DEFAULT_CRS_WKT = "PROJCS[\"WGS 84 / UPS South\"," +
    "GEOGCS[\"WGS 84\"," +
        "DATUM[\"WGS_1984\"," +
            "SPHEROID[\"WGS 84\",6378137,298.257223563, AUTHORITY[\"EPSG\",\"7030\"]]," +
            "AUTHORITY[\"EPSG\",\"6326\"]]," +
        "PRIMEM[\"Greenwich\", 0,AUTHORITY[\"EPSG\",\"8901\"]]," +
        "UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]]," +
        "AUTHORITY[\"EPSG\",\"4326\"]]," +
    "PROJECTION[\"Polar_Stereographic\"]," +
    "PARAMETER[\"latitude_of_origin\",-90]," +
    "PARAMETER[\"central_meridian\",0]," +
    "PARAMETER[\"scale_factor\",0.994]," +
    "PARAMETER[\"false_easting\",0000000]," +
    "PARAMETER[\"false_northing\",0000000]," +
    "UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]]," +
    "AUTHORITY[\"EPSG\",\"32761\"]]";
    // -------------------------------------

    // -------------- Variables ------------
    private CoordinateReferenceSystem DEFAULT_CRS;
    private StyleFactory sf;
	private FilterFactory filterFactory;

    // -------------------------------------
    

    /**
     * Constructor
     * 
     */
    public LayerFactory()
    {
		this.DEFAULT_CRS = DefaultGeographicCRS.WGS84;
		this.sf = CommonFactoryFinder.getStyleFactory(new Hints(Hints.KEY_RENDERING, Hints.VALUE_RENDER_SPEED));
		this.filterFactory = new org.geotools.filter.FilterFactoryImpl();

    }
    
	
    /**
     * Return the default coordinate system
     * 
     * @return The default CoordinateReferenceSystem
     * 
     */
    public CoordinateReferenceSystem getDefaultCRS()
	{
		return DEFAULT_CRS;
	}
	
	
    
    /**
     * Create and return a CoordinateReferenceSystem
     * 
     * @param crsWKT The coordinate system to create described in WKT
     * @return The newly created CoordinateReferenceSystem
     * 
     */
    public CoordinateReferenceSystem createCRS(String crsWkt)
	{
		CoordinateReferenceSystem crs;
		
		// Create 
		try {
			crs = ReferencingFactoryFinder.getCRSFactory(null).createFromWKT(crsWkt);
			return crs;
		}
		catch (FactoryException exception) {
			exception.printStackTrace();
			return null;
		}
		
	}
	
	
	/**
	 * Create a MapContext with the default coordinate reference system
	 * 
	 * @return The newly created map context
	 * 
	 */
    public MapContext createMapContext()
	{
		DefaultMapContext context = new DefaultMapContext(this.DEFAULT_CRS);
		
		context.setTitle(this.DEFAULT_MAP_TITLE);
		context.setAbstract(this.DEFAULT_ABSTRACT);		

		return context;
	}


    /**
     * Create the FeatureTypeStyle for a layer containing polygons
     * 
     * @return The FeatureTypeStyle
     * @param strokeHTMLColor Color of the polygon stroke (Format: #xxxxxx)
     * @param strokeWidth Width of the polygon stroke
     * @param fillHTMLColor Color inside the polygon (Format: #xxxxxx)
     * 
	 */
	private org.geotools.styling.FeatureTypeStyle createDefaultPolygonFeatureTypeStyle(String strokeHTMLColor, int strokeWidth, String fillHTMLColor, String opacityValue)
	{
		Stroke polygonStroke;
		Fill polygonFill;
		PolygonSymbolizer polysym;
		Rule polygonRule;
		FeatureTypeStyle polygonFeatureTypeStyle;
		String couleur="#"+Integer.toHexString(java.awt.Color.RED.getRGB()).substring(2);
        // Stroke java.awt.Color

		polygonStroke = sf.getDefaultStroke();
        polygonStroke.setWidth(filterFactory.literal(new Integer(strokeWidth)));
        polygonStroke.setColor(filterFactory.literal(strokeHTMLColor));

        // Fill
        polygonFill = sf.getDefaultFill();
        
        polygonFill.setColor(filterFactory.literal(fillHTMLColor));
        
        //set the fill color  opacity
        polygonFill.setOpacity(filterFactory.literal(opacityValue));
        
        //Set the line Stroke opacity
        polygonStroke.setOpacity(filterFactory.literal(opacityValue));
        
        // PolygonSymbolizer
        polysym = sf.createPolygonSymbolizer();
        polysym.setFill(polygonFill);
        polysym.setStroke(polygonStroke);
        
   	 	// From geotools javadoc: 
   		// "A rule is used to attach a condition to, and group, the individual 
   		// symbolizers used for rendering."
        polygonRule = sf.createRule();
        polygonRule.setSymbolizers(new Symbolizer[]{polysym});
        polygonFeatureTypeStyle = sf.createFeatureTypeStyle(new Rule[]{polygonRule});
        
        return polygonFeatureTypeStyle;
	}
	
	
    /**
     * Create the FeatureTypeStyle for a layer containing point
     * 
     * @return The FeatureTypeStyle
     * @param size Size of the point symbol
     * 
	 */
	private org.geotools.styling.FeatureTypeStyle createDefaultPointFeatureTypeStyle(double size)
	{
		PointSymbolizer pointSymbolizer;
		Rule pointRule;
		FeatureTypeStyle pointFeatureTypeStyle;
		Graphic graph;
		Mark square;
		
		// Line Symbolizer
		StyleBuilder sb = new StyleBuilder();
		square = sb.createMark(StyleBuilder.MARK_TRIANGLE, Color.RED);
        graph = sb.createGraphic(null, square, null, this.DEFAULT_POINT_OPACITY, size, this.DEFAULT_POINT_ROTATION);
        pointSymbolizer = sb.createPointSymbolizer(graph);		
        //pointSymbolizer = sb.createPointSymbolizer();
        
        // Rule
        pointRule = sf.createRule();
        pointRule.setSymbolizers(new Symbolizer[]{pointSymbolizer});
        pointFeatureTypeStyle = sf.createFeatureTypeStyle(new Rule[]{pointRule});
		
        return pointFeatureTypeStyle;
	}
	
	
    /**
     * Create the FeatureTypeStyle for a layer containing lines
     * 
     * @return The FeatureTypeStyle
     * @param strokeWidth Width of the stroke
     * @param strokeHTMLColor Color stroke
     * 
	 */
	private org.geotools.styling.FeatureTypeStyle createDefaultLineFeatureTypeStyle(int strokeWidth, String strokeHTMLColor)
	{
		FilterFactory filterFactory;
		StyleFactory sf;
		LineSymbolizer lineSymbolizer;
		Rule lineRule;
		Stroke lineStroke;
		FeatureTypeStyle lineFeatureTypeStyle;
		
		// Creating the map style
		sf = CommonFactoryFinder.getStyleFactory(new Hints(Hints.KEY_RENDERING, Hints.VALUE_RENDER_SPEED));
		filterFactory = new org.geotools.filter.FilterFactoryImpl();

		// Line Symbolizer
		lineSymbolizer = sf.createLineSymbolizer();
        lineStroke = sf.getDefaultStroke();
        lineStroke.setWidth(filterFactory.literal(new Integer(strokeWidth)));
        lineStroke.setColor(filterFactory.literal(strokeHTMLColor));
        lineSymbolizer.setStroke(lineStroke);
        
        // Rule
        lineRule = sf.createRule();
        lineRule.setSymbolizers(new Symbolizer[]{lineSymbolizer});
        lineFeatureTypeStyle = sf.createFeatureTypeStyle(new Rule[]{lineRule});
        
        return lineFeatureTypeStyle;
	}

	
	/**
     * Create the style for a layer containing polygons
     * 
     * @return The style
     * @param strokeHTMLColor Color of the polygon stroke (Format: #xxxxxx)
     * @param strokeWidth Width of the polygon stroke
     * @param fillHTMLColor Color inside the polygon (Format: #xxxxxx)
     * 
	 */
	public org.geotools.styling.Style createDefaultPolygonLayerStyle(String strokeHTMLColor, int strokeWidth, String fillHTMLColor,String opacityValue )
	{
		Style polygonStyle;
		FeatureTypeStyle fts;
		 //String couleur= "#"+Integer.toHexString(java.awt.Color.blue.getRGB());
		
		fts = this.createDefaultPolygonFeatureTypeStyle(strokeHTMLColor, strokeWidth, fillHTMLColor,opacityValue);

		// Create the style
        polygonStyle = sf.createStyle();
        polygonStyle.setFeatureTypeStyles(new FeatureTypeStyle[]{fts});
		polygonStyle.setName(this.DEFAULT_POLYGON_LAYER_STYLE_NAME);
		
		return polygonStyle;
	}

	
	/**
     * Create the style for a layer containing lines
     * 
     * @return The style
     * @param strokeWidth Width of the polygon stroke
     * @param strokeHTMLColor Color of the polygon stroke (Format: #xxxxxx)
     * 
	 */
	public org.geotools.styling.Style createDefaultLineLayerStyle(int strokeWidth, String strokeHTMLColor)
	{
		Style lineStyle;
		FeatureTypeStyle lineFeatureTypeStyle;

		lineFeatureTypeStyle = this.createDefaultLineFeatureTypeStyle(strokeWidth, strokeHTMLColor);

		// Create the style
        lineStyle = sf.createStyle();
        lineStyle.setFeatureTypeStyles(new FeatureTypeStyle[]{lineFeatureTypeStyle});
		lineStyle.setName(this.DEFAULT_LINE_LAYER_STYLE_NAME);
		
		return lineStyle;	
	}

	
	/**
     * Create the style for a layer containing points
     * 
     * @return The style
     * @param size Size of the point symbol
     * 
	 */
	public org.geotools.styling.Style createDefaultPointLayerStyle(double size)
	{
		Style pointStyle;
		FeatureTypeStyle pointFeatureTypeStyle;

		pointFeatureTypeStyle = this.createDefaultPointFeatureTypeStyle(size);
		
		// Create the style
        pointStyle = sf.createStyle();
        pointStyle.setFeatureTypeStyles(new FeatureTypeStyle[]{pointFeatureTypeStyle});
		pointStyle.setName(this.DEFAULT_POINT_LAYER_STYLE_NAME);
		
		return pointStyle;	
	}
	
	
	/**
     * Create a layer style for a layer containing points, lines and polygons
     * 
     * @return The style
     * 
	 */
	public Style createDefaultLayerStyles()
	{
		Style allStyle;
		FeatureTypeStyle polygonFeatureTypeStyle;
		FeatureTypeStyle pointFeatureTypeStyle;
		FeatureTypeStyle lineFeatureTypeStyle;

		
		polygonFeatureTypeStyle = this.createDefaultPolygonFeatureTypeStyle(this.DEFAULT_POLYGON_STROKE_HTML_COLOR, this.DEFAULT_POLYGON_STROKE_WIDTH, this.DEFAULT_POLYGON_FILL_HTML_COLOR, this.DEFAULT_OPACITY);
		pointFeatureTypeStyle = this.createDefaultPointFeatureTypeStyle(this.DEFAULT_POINT_SIZE);
		lineFeatureTypeStyle = this.createDefaultLineFeatureTypeStyle(this.DEFAULT_LINE_STROKE_WIDTH, this.DEFAULT_LINE_STROKE_HTML_COLOR);

		// Create the style
		allStyle = sf.createStyle();
        allStyle.setFeatureTypeStyles(new FeatureTypeStyle[]{polygonFeatureTypeStyle, pointFeatureTypeStyle, lineFeatureTypeStyle});
        allStyle.setName(this.DEFAULT_ALL_STYLE_LAYER_NAME);
		
		return allStyle;
		
	}
}
