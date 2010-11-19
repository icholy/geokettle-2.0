package org.pentaho.di.ui.core.dialog.geopreview;

//import org.geotools.feature.Feature;
import org.geotools.feature.simple.*;
import org.opengis.feature.simple.SimpleFeature;
import org.pentaho.di.ui.core.util.geo.GeometryConverter;


import com.vividsolutions.jts.geom.Geometry;

/**
 * Wrap geometry between Geotools and JTS
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public class GeometryWrapper 
{
	private Geometry jtsgeom;
	private SimpleFeature  feature;
	
	public GeometryWrapper(Geometry jtsgeom)
	{
		this.jtsgeom = jtsgeom;
		
		this.feature = GeometryConverter.JTSGeomToGeoToolsFeature(jtsgeom);
	}
	
	public Geometry getJTSGeom()
	{
		return this.jtsgeom;
	}
	
	public SimpleFeature getGeotoolsFeature()
	{
		return this.feature;
	}
}
