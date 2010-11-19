package org.pentaho.di.ui.core.util.geo;

import org.opengis.feature.simple.SimpleFeatureType;  
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature; 
import org.geotools.feature.simple.SimpleFeatureBuilder; 
//import org.geotools.feature.IllegalAttributeException;
import org.pentaho.di.ui.core.util.geo.renderer.swt.LayerFactory;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public class GeometryConverter 
{
	public static SimpleFeatureType featureType;
	private static LayerFactory	layerFactory; 

	private static final String FEATURE_ATTRIBUTE_1_GEOM = "the_geom";
	//private static final String FEATURE_ATTRIBUTE_2_NAME = "NAME";
	private static final String FEATURE_TYPE_NAME = "MyFeature";

	// Static initializer
	static
	{
		layerFactory = new LayerFactory();
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		
		builder.setCRS(GeometryConverter.layerFactory.getDefaultCRS());
		//builder.add( "id", Integer.class );
		//builder.add( "stringProperty", String.class );
		//builder.minOccurs(0).maxOccurs(2).nillable(false).add("doubleProperty",Double.class);
		builder.add(GeometryConverter.FEATURE_ATTRIBUTE_1_GEOM, com.vividsolutions.jts.geom.Geometry.class);

		builder.setName(FEATURE_TYPE_NAME);
    	// Attribute of the feature schema
		//AttributeDescriptor[] types = {
			// (GeometryAttributeType )AttributeDescriptor.newAttributeType(GeometryConverter.FEATURE_ATTRIBUTE_1_GEOM, com.vividsolutions.jts.geom.Geometry.class, true, 1, null, GeometryConverter.layerFactory.getDefaultCRS())
			 // Ici on pourrait ajouter d'autres attributs comme le nom de la feature ou des attributs descriptifs.
		//};
		//********List<AttributeDescriptor> types = builder.buildFeatureType().getAttributeDescriptors();
		// Create the feature schema and feature collection
		//try {
	 		featureType = builder.buildFeatureType();
		//}
		//catch (SchemaException exception) {
		//	exception.printStackTrace();
		//}
		
	}
	
	
	
	/*
	public static GM_Object JTSGeomToGMObject(Geometry jtsGeom) throws Exception
	{
		GM_Object gmGeom;
		
		try {
			gmGeom = JtsGeOxygene.makeGeOxygeneGeom(jtsGeom);
		}
		catch (Exception exception)	{
			throw exception;
		}
		
		return gmGeom;
	}
	
	public static Geometry GMObjectToJTSGeom (GM_Object gmGeom) throws Exception
	{
		Geometry jtsGeom;
		
		try {
			jtsGeom = JtsGeOxygene.makeJtsGeom(gmGeom);
		}
		catch (Exception exception) {
			throw exception;
		}
		
		return jtsGeom;
	}
	*/
	/*
	public static Geometry GeoToolFeatureToJTS(SimpleFeature geOxyGeom)
	throws Exception
	{
		GeometryFactory jtsGeomFactory=new GeometryFactory(JtsGeOxygene.jtsPrecision,layerFactory.getDefaultCRS());
		String wktGeom=new WKBWriter().write(geOxyGeom);
			WktGeOxygene.makeWkt(geOxyGeom);
		WKTReader jtsWktReader=new WKTReader(jtsGeomFactory);
	    
	    Geometry jtsGeom=jtsWktReader.read(wktGeom);
	    return jtsGeom;    
	}*/
	public static SimpleFeature JTSGeomToGeoToolsFeature(Geometry jtsgeom)
	{
		//try {
			String featureId;
						 
			//SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
			featureId=SimpleFeatureBuilder.createDefaultFeatureId();
			//return featureType.create(new Object[] {jtsgeom});
			return SimpleFeatureBuilder.build(featureType,new Object[] {jtsgeom},featureId);
			
		//}
		//catch (IllegalAttributeException exception) {
			//exception.printStackTrace();
			//return null;
		//}
	}
	
}
