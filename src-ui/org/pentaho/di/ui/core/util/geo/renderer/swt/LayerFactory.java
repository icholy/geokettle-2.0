package org.pentaho.di.ui.core.util.geo.renderer.swt;

import java.awt.Color;
import java.util.Random;

import org.eclipse.swt.graphics.RGB;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.filter.FilterFactoryImpl;
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
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class LayerFactory 
{
    private final String DEFAULT_MAP_TITLE = "Geographic Preview OMM";
    private final String DEFAULT_ABSTRACT = "This is a geographic preview";
    private final String DEFAULT_POINT_LAYER_STYLE_NAME = "Point Layer Style";
    private final String DEFAULT_POLYGON_LAYER_STYLE_NAME = "Polygon Layer Style";
    private final String DEFAULT_LINE_LAYER_STYLE_NAME = "Line Layer Style";
    private final String DEFAULT_COLLECTION_LAYER_STYLE_NAME = "Collection Layer Style";
    private final String DEFAULT_ALL_STYLE_LAYER_NAME = "All Style";
    
    private final double DEFAULT_POINT_ROTATION = 0;
    
    public static final String DEFAULT_OPACITY="1.0";    
    public static final String DEFAULT_COLOR = "#000000"; 
    public static final String DEFAULT_STROKE_WIDTH = "1";
    public static final String DEFAULT_RADIUS = "6"; 

	/*private final String DEFAULT_CRS_WKT = "PROJCS[\"WGS 84 / UPS South\"," +
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
    "AUTHORITY[\"EPSG\",\"32761\"]]";*/

    private CoordinateReferenceSystem DEFAULT_CRS;
    private StyleFactory sf;
	private FilterFactory filterFactory;

    public LayerFactory(){
		DEFAULT_CRS = DefaultGeographicCRS.WGS84;
		sf = CommonFactoryFinder.getStyleFactory(new Hints(Hints.KEY_RENDERING, Hints.VALUE_RENDER_SPEED));
		filterFactory = new FilterFactoryImpl();
    }

    public CoordinateReferenceSystem getDefaultCRS(){
		return DEFAULT_CRS;
	}

    public CoordinateReferenceSystem createCRS(String crsWkt){
		CoordinateReferenceSystem crs;	
		try {
			crs = ReferencingFactoryFinder.getCRSFactory(null).createFromWKT(crsWkt);
			return crs;
		}catch (FactoryException exception) {
			exception.printStackTrace();
			return null;
		}		
	}

    public static RGB getRandomColor(){
    	Color color = Color.getHSBColor( (new Random()).nextFloat(), 1.0F, 1.0F );
    	return new RGB(color.getRed(), color.getGreen(), color.getBlue());
    }
    
    public static RGB getDefaultColor(){
    	Color color = Color.getHSBColor( 0.0F, 0.0F, 0.0F );
    	return new RGB(color.getRed(), color.getGreen(), color.getBlue());
    }
    
    public MapContext createMapContext(){
		DefaultMapContext context = new DefaultMapContext(this.DEFAULT_CRS);		
		context.setTitle(this.DEFAULT_MAP_TITLE);
		context.setAbstract(this.DEFAULT_ABSTRACT);		

		return context;
	}

	@SuppressWarnings("deprecation")
	private FeatureTypeStyle createDefaultPolygonFeatureTypeStyle(String strokeColor, String strokeWidth, String fillColor, String opacity){
		Stroke polygonStroke = sf.getDefaultStroke();
        polygonStroke.setWidth(filterFactory.literal(new Integer(strokeWidth)));
        polygonStroke.setColor(filterFactory.literal(strokeColor));
        polygonStroke.setOpacity(filterFactory.literal(opacity));
        
        Fill polygonFill = sf.getDefaultFill();        
        polygonFill.setColor(filterFactory.literal(fillColor));        
        polygonFill.setOpacity(filterFactory.literal(opacity));      
        
        PolygonSymbolizer polySym = sf.createPolygonSymbolizer();
        polySym.setFill(polygonFill);
        polySym.setStroke(polygonStroke);

        Rule polygonRule = sf.createRule();
        polygonRule.setSymbolizers(new Symbolizer[]{polySym});
        FeatureTypeStyle polygonFeatureTypeStyle = sf.createFeatureTypeStyle(new Rule[]{polygonRule});
        
        return polygonFeatureTypeStyle;
	}
	
	@SuppressWarnings("deprecation")
	private FeatureTypeStyle createDefaultCollectionFeatureTypeStyle(String color, String opacity){
		Stroke collectionStroke = sf.getDefaultStroke();
		collectionStroke.setWidth(filterFactory.literal(new Integer(DEFAULT_STROKE_WIDTH)));
		collectionStroke.setColor(filterFactory.literal(color));
		collectionStroke.setOpacity(filterFactory.literal(opacity));
        
        Fill collectionFill = sf.getDefaultFill();        
        collectionFill.setColor(filterFactory.literal(color));        
        collectionFill.setOpacity(filterFactory.literal(opacity));      
        
		StyleBuilder sb = new StyleBuilder();
		Mark circle = sb.createMark(StyleBuilder.MARK_CIRCLE, collectionFill,collectionStroke);
		Graphic graph = sb.createGraphic(null, circle, null,Double.parseDouble(opacity), Double.parseDouble(DEFAULT_RADIUS), DEFAULT_POINT_ROTATION);
        PointSymbolizer pointSymbolizer = sb.createPointSymbolizer(graph);
        
        LineSymbolizer lineSymbolizer = sf.createLineSymbolizer();
        lineSymbolizer.setStroke(collectionStroke);
        
        PolygonSymbolizer polySymbolizer = sf.createPolygonSymbolizer();
        polySymbolizer.setFill(collectionFill);
        polySymbolizer.setStroke(collectionStroke);

        Rule collectionRule = sf.createRule();
        collectionRule.setSymbolizers(new Symbolizer[]{pointSymbolizer, lineSymbolizer, polySymbolizer});
        FeatureTypeStyle polygonFeatureTypeStyle = sf.createFeatureTypeStyle(new Rule[]{collectionRule});
        
        return polygonFeatureTypeStyle;
	}

	@SuppressWarnings("deprecation")
	private FeatureTypeStyle createDefaultPointFeatureTypeStyle(String radius, String color, String opacity){	
		Fill pointFill = sf.getDefaultFill();        
		pointFill.setColor(filterFactory.literal(color));        
		pointFill.setOpacity(filterFactory.literal(opacity));
        
		Stroke pointStroke = sf.getDefaultStroke();
		pointStroke.setWidth(filterFactory.literal(new Integer(DEFAULT_STROKE_WIDTH)));
		pointStroke.setColor(filterFactory.literal(DEFAULT_COLOR));
		pointStroke.setOpacity(filterFactory.literal(opacity));

		StyleBuilder sb = new StyleBuilder();
		Mark circle = sb.createMark(StyleBuilder.MARK_CIRCLE, pointFill,pointStroke);
		Graphic graph = sb.createGraphic(null, circle, null, Double.parseDouble(opacity), Double.parseDouble(radius) , DEFAULT_POINT_ROTATION);
        PointSymbolizer pointSymbolizer = sb.createPointSymbolizer(graph);		
        
        // Rule
        Rule pointRule = sf.createRule();
        pointRule.setSymbolizers(new Symbolizer[]{pointSymbolizer});
        FeatureTypeStyle pointFeatureTypeStyle = sf.createFeatureTypeStyle(new Rule[]{pointRule});
		
        return pointFeatureTypeStyle;
	}

	@SuppressWarnings("deprecation")
	private FeatureTypeStyle createDefaultLineFeatureTypeStyle(String strokeWidth, String strokeColor, String opacity){		
		StyleFactory sf = CommonFactoryFinder.getStyleFactory(new Hints(Hints.KEY_RENDERING, Hints.VALUE_RENDER_SPEED));
		FilterFactory filterFactory = new FilterFactoryImpl();

		Stroke lineStroke = sf.getDefaultStroke();
        lineStroke.setWidth(filterFactory.literal(new Integer(strokeWidth)));
        lineStroke.setColor(filterFactory.literal(strokeColor));
        lineStroke.setOpacity(filterFactory.literal(opacity));
        
        LineSymbolizer lineSymbolizer = sf.createLineSymbolizer();
        lineSymbolizer.setStroke(lineStroke);
        
        Rule lineRule = sf.createRule();
        lineRule.setSymbolizers(new Symbolizer[]{lineSymbolizer});
        FeatureTypeStyle lineFeatureTypeStyle = sf.createFeatureTypeStyle(new Rule[]{lineRule});
        
        return lineFeatureTypeStyle;
	}

	@SuppressWarnings("deprecation")
	public Style createDefaultPolygonLayerStyle(String strokeColor, String strokeWidth, String fillColor,String opacity){
		FeatureTypeStyle fts = createDefaultPolygonFeatureTypeStyle(strokeColor, strokeWidth, fillColor,opacity);
		Style polygonStyle = sf.createStyle();
        polygonStyle.setFeatureTypeStyles(new FeatureTypeStyle[]{fts});
		polygonStyle.setName(DEFAULT_POLYGON_LAYER_STYLE_NAME);
		
		return polygonStyle;
	}

	@SuppressWarnings("deprecation")
	public Style createDefaultLineLayerStyle(String strokeWidth, String strokeColor, String opacity){
		FeatureTypeStyle lineFeatureTypeStyle = createDefaultLineFeatureTypeStyle(strokeWidth, strokeColor, opacity);
		Style lineStyle = sf.createStyle();
        lineStyle.setFeatureTypeStyles(new FeatureTypeStyle[]{lineFeatureTypeStyle});
		lineStyle.setName(DEFAULT_LINE_LAYER_STYLE_NAME);
		
		return lineStyle;	
	}

	@SuppressWarnings("deprecation")
	public Style createDefaultPointLayerStyle(String radius, String color, String opacity){
		FeatureTypeStyle pointFeatureTypeStyle = createDefaultPointFeatureTypeStyle(radius, color, opacity);		
		Style pointStyle = sf.createStyle();
        pointStyle.setFeatureTypeStyles(new FeatureTypeStyle[]{pointFeatureTypeStyle});
		pointStyle.setName(DEFAULT_POINT_LAYER_STYLE_NAME);
		
		return pointStyle;	
	}
	
	@SuppressWarnings("deprecation")
	public Style createDefaultCollectionLayerStyle(String color, String opacity){
		FeatureTypeStyle collectionFeatureTypeStyle = createDefaultCollectionFeatureTypeStyle(color, opacity);		
		Style collectionStyle = sf.createStyle();
		collectionStyle.setFeatureTypeStyles(new FeatureTypeStyle[]{collectionFeatureTypeStyle});
		collectionStyle.setName(DEFAULT_COLLECTION_LAYER_STYLE_NAME);
		
		return collectionStyle;	
	}

	@SuppressWarnings("deprecation")
	public Style createDefaultLayerStyles(){
		FeatureTypeStyle collectionFeatureTypeStyle = createDefaultCollectionFeatureTypeStyle(DEFAULT_COLOR, DEFAULT_OPACITY);
		FeatureTypeStyle polygonFeatureTypeStyle = createDefaultPolygonFeatureTypeStyle(DEFAULT_COLOR, DEFAULT_STROKE_WIDTH, DEFAULT_COLOR, DEFAULT_OPACITY);
		FeatureTypeStyle pointFeatureTypeStyle = createDefaultPointFeatureTypeStyle(DEFAULT_RADIUS, DEFAULT_COLOR, DEFAULT_OPACITY);
		FeatureTypeStyle lineFeatureTypeStyle = createDefaultLineFeatureTypeStyle(DEFAULT_STROKE_WIDTH, DEFAULT_COLOR, DEFAULT_OPACITY);

		Style allStyle = sf.createStyle();
        allStyle.setFeatureTypeStyles(new FeatureTypeStyle[]{polygonFeatureTypeStyle, pointFeatureTypeStyle, lineFeatureTypeStyle, collectionFeatureTypeStyle});
        allStyle.setName(DEFAULT_ALL_STYLE_LAYER_NAME);
		
		return allStyle;		
	}
}