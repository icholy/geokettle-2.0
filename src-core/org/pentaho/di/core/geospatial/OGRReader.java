package org.pentaho.di.core.geospatial;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Driver;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.ogr.ogrConstants;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Handles file reading from a OGR data source
 * 
 *  @author tbadard
 *  @since 21-03-2007
 *
 */
public class OGRReader
{
    private LogWriter   log;
    //private java.net.URL gisURL;
    private String ogrDataSourcePath;
    private boolean     error;
    
    //private DataStore gtDataStore;
    private DataSource ogrDataSource;
    //private FeatureSource<SimpleFeatureType, SimpleFeature> featSrc;
    private Layer ogrLayer;
    private FeatureDefn ogrLayerDefinition;
    //private FeatureCollection<SimpleFeatureType, SimpleFeature> featColl;
    //private FeatureIterator<SimpleFeature> featIter;
    private int ogrFeatureIndex;

    public OGRReader(String dataSourcePath)
    {
        this.log      = LogWriter.getInstance();
        //this.gisURL = fileURL;
        this.ogrDataSourcePath = dataSourcePath; 
        error         = false;
        //gtDataStore = null;
        ogrDataSource = null;
        //featSrc = null;
        ogrLayer = null;
        ogrLayerDefinition = null;
        //featColl = null;
        //featIter = null;
        ogrFeatureIndex = -1;
    }
    
    public void open() throws KettleException
    {
 		try {
 			
 			// try closing first
 			close();
 			
 			// TODO: detect file type and instanciate the right type of DataStore
 			// implementation (to support file formats other than Shapefile)
 			
			// Don't use a memory-mapped file reader (3rd arg) because this
 			// causes out of memory errors with large files (~500mb).
 			// 4th argument is the charset.
 			// TODO: make charset configurable (in the step dialog box?)
 			// gtDataStore = new ShapefileDataStore(gisURL, null, false, Charset.defaultCharset());
 			
 			// ShapefileDataStore defaults to ISO-8859-1 charset (not always the same as Charset.defaultCharset()
 			// which is often UTF-8 on linux!)
 			//gtDataStore = new ShapefileDataStore(gisURL, null, false);
 			
 			// All OGR drivers are registered
 			ogr.RegisterAll();
 			// Try to open the data source in read only mode
 			ogrDataSource = ogr.Open(ogrDataSourcePath, true);
 			// If it fails, the data source is opened in read/write mode
 	        if (ogrDataSource == null)
 	        {
 	        	ogrDataSource = ogr.Open(ogrDataSourcePath, false);
 	        }
 	        // Try to find the suitable driver for this data source
 	        Driver ogrDriver = ogrDataSource.GetDriver();

 			/*
 			if(gisURL.toString().substring(gisURL.toString().length()-3,gisURL.toString().length()).equalsIgnoreCase("SHP"))
 	    	{
 	    		gtDataStore = new ShapefileDataStore(gisURL, null, false);
 	    	}
 			if(gisURL.toString().substring(gisURL.toString().length()-3,gisURL.toString().length()).equalsIgnoreCase("GML"))
 	    	{
 				String encodedURL = gisURL.toString().replace(" ", "%20");
 				URI gisURI = new URI(encodedURL);
 				gtDataStore = new FileGMLDataStore(gisURI,256,Integer.MAX_VALUE);	
 	    	}
 	    	*/
 	    	
 	    	
			//String name = gtDataStore.getTypeNames()[0];
			//featSrc = gtDataStore.getFeatureSource(name);
 	        // TODO Here we assume that each data source has at least one layer and we process the first one only
 	        //log.println(log.LOG_LEVEL_BASIC, "--> # of Layers: "+ogrDataSource.GetLayerCount());
 	        ogrLayer =  ogrDataSource.GetLayer(0);
			ogrLayerDefinition = ogrLayer.GetLayerDefn();
			
			//featColl = featSrc.getFeatures();
            //featIter = featColl.features(); 

		}
		catch(Exception e) {
			throw new KettleException("Error opening the OGR data source: "+ogrDataSourcePath, e);
		}
    }
        
    public RowMetaInterface getFields() throws KettleException
    {
        String debug="get attributes from an OGR data source";
        RowMetaInterface row = new RowMeta();
        
        try
        {
            // Fetch all field information
            //
            debug="allocate data types";
        	// datatype = new byte[reader.getFieldCount()];
            /* SimpleFeatureType ft = featSrc.getSchema();
            List<AttributeDescriptor> attrDescriptors = ft.getAttributeDescriptors();
            
            int i = 0;
            for(AttributeDescriptor ad : attrDescriptors)
            {
              if (log.isDebug()) debug="get attribute #"+i;

              ValueMetaInterface value = null;

              AttributeType at = ad.getType();
              Class<?> c = at.getBinding();
              
              if(c == java.lang.String.class) {
            	  // String
            	  debug = "string attribute";
            	  value = new ValueMeta(ad.getName().getLocalPart(), ValueMetaInterface.TYPE_STRING);
            	  // value.setLength(); // TODO: check if there is a way to get max string length from AttributeType
              }
              else if(c == java.lang.Integer.class || c == java.lang.Long.class) {
            	  // Integer
            	  debug = "integer attribute";
            	  value = new ValueMeta(ad.getName().getLocalPart(), ValueMetaInterface.TYPE_INTEGER);
              }
              else if(c == java.lang.Double.class) {
            	  // Double
            	  debug = "double attribute";
            	  value = new ValueMeta(ad.getName().getLocalPart(), ValueMetaInterface.TYPE_NUMBER);
              }
              else if(c == java.util.Date.class) {
            	  // Date
            	  debug = "date attribute";
            	  value = new ValueMeta(ad.getName().getLocalPart(), ValueMetaInterface.TYPE_DATE);
              }
              else if( com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(c) )
              {
            	  // Geometry
            	  debug = "geometry attribute";
            	  value = new ValueMeta(ad.getName().getLocalPart(), ValueMetaInterface.TYPE_GEOMETRY);

            	  // set the SRS
            	  value.setGeometrySRS(getSRS());
              }
              // TODO: add other attribute types (logical, blob, etc.)
              else {
            	  //unknown
            	  value = new ValueMeta(ad.getName().getLocalPart(), ValueMetaInterface.TYPE_STRING);
              }
              
              if (value!=null)
              {
                  row.addValueMeta(value);
              }
              
              i++;
            }*/
            
            int nbrFieldCount = ogrLayerDefinition.GetFieldCount();
            FieldDefn ogrFieldDefinition = null;
            ValueMetaInterface value;
            int ogrFieldType;
            String ogrFieldName;
            
            for (int j = 0; j < nbrFieldCount; j++) {
				ogrFieldDefinition = ogrLayerDefinition.GetFieldDefn(j);
				
	              if (log.isDebug()) debug="get attribute #"+j;

	              value = null;
	              ogrFieldType = ogrFieldDefinition.GetFieldType();
	              ogrFieldName = ogrFieldDefinition.GetNameRef();
	              
				  switch (ogrFieldType) {
					case ogrConstants.OFTInteger:
						debug = "integer attribute";
		            	value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_INTEGER);
						break;
					case ogrConstants.OFTReal:
						debug = "double attribute";
		            	value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_NUMBER);
						break;
					case ogrConstants.OFTString:
						debug = "string attribute";
		            	value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_STRING);
						break;
					case ogrConstants.OFTWideString:
						debug = "string attribute";
		            	value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_STRING);
						break;
					case ogrConstants.OFTDate:
						debug = "date attribute";
		            	value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_DATE);
						break;
					case ogrConstants.OFTTime:
						debug = "time attribute";
		            	value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_DATE);
						break;
					case ogrConstants.OFTDateTime:
						debug = "datetime attribute";
		            	value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_DATE);
						break;
					//TODO Check if OGR OFTBinary data type actually matches TYPE_BOOLEAN?
					case ogrConstants.OFTBinary:
						debug = "binary attribute";
		            	value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_BOOLEAN);
						break;
					// TODO Add other possible OGR data types (string|real|integer lists, etc.)
					default:
						value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_STRING);
						break;
				  }
				  
				  // TODO Check if the geometry column is processed by the default switch case when a geometry column exists
	              if (value!=null)
	              {
	                  row.addValueMeta(value);
	              }
			}
            // Add the geometry column
			if (ogrLayer.GetGeometryColumn().length()>0)
				value = new ValueMeta(ogrLayer.GetGeometryColumn(), ValueMetaInterface.TYPE_GEOMETRY);
			else value = new ValueMeta("the_geom", ValueMetaInterface.TYPE_GEOMETRY);
			row.addValueMeta(value);
        }
        catch(Exception e)
        {
            throw new KettleException("Error reading OGR data source metadata (in part "+debug+")", e);
        }
        
        return row;
    }
    
    public Object[] getRow(RowMetaInterface fields) throws KettleException
    {
    	return getRow( RowDataUtil.allocateRowData(fields.size()) );
    }
    
    public Object[] getRow(Object[] r) throws KettleException
    {
        
    	String debug = "";
    	
        try
        {
        	// Read the next record
            
            // Are we at the end yet?
            //if (!featIter.hasNext()) return null;
            
            // Copy the default row for speed...
        	// debug = "copy the default row for speed!";
        	// r = new Row(fields);
        	
        	debug = "set the values in the row";
        	// Set the values in the row...
        	
        	/*SimpleFeature f = featIter.next();
        	List<Object> attributeValues = f.getAttributes();

        	int i = 0;
        	for(Object val : attributeValues)
			{
	        	debug = "getting value #"+i;
				
				if(val == null) {
					debug = "null attribute";
					r[i] = null;
				}
				else {

					Class<?> c = val.getClass();

					if(c == java.lang.String.class) {
						debug = "string attribute";
						r[i] = (String) val;
					}
					else if(c == java.lang.Integer.class) {
						debug = "integer attribute";
						r[i] = new Long( ((Integer)val).longValue() );

					}
					else if(c == java.lang.Long.class) {
						debug = "long integer attribute";
						// TODO: check if this is supported:
						r[i] = (Long) val;
					}
					else if(c == java.lang.Double.class) {
						debug = "double attribute";
						r[i] = (Double) val;
					}
					else if(c == java.util.Date.class) {
						debug = "date attribute";
						r[i] = (java.util.Date) val;
					}				
					else if( com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(c) )
					{
						// Geometry
						debug = "geometry attribute";
						Geometry jts_geom = (Geometry) val;

						// TODO: add logic to convert simple MultiPolygons to Polygons
						// (needed for JCS)
						// could we put this in another step instead??

						r[i] = jts_geom;
					}
					// TODO: add other attribute types? (numeric, float, logical, date, etc.)
					else {
						// unknown
						r[i] = null;
					}
				}
				
				i++;
			}*/
        	
			//for (int j = 0; j < ogrLayer.GetFeatureCount(); j++) {
        		
        	Feature ogrFeature = null;
        	//log.println(log.LOG_LEVEL_BASIC, "--> ogrLayer:"+ogrLayer);
        	//log.println(log.LOG_LEVEL_BASIC, "--> ogrLayer.GetFeatureCount():"+ogrLayer.GetFeatureCount());
        	//log.println(log.LOG_LEVEL_BASIC, "--> ogrLayer.GetLayerDefn().GetFieldCount():"+ogrLayer.GetLayerDefn().GetFieldCount());
        	
        	while ((ogrFeature == null) && (ogrFeatureIndex < ogrLayer.GetFeatureCount()-1)) {
				ogrFeature = ogrLayer.GetFeature(++ogrFeatureIndex);
        	}
        	//ogrFeature = ogrLayer.GetNextFeature();
        	//log.println(log.LOG_LEVEL_BASIC, "--> ogrFeature:"+ogrFeature);
        	
        	int ogrFieldsCount = ogrFeature.GetFieldCount();
			org.gdal.ogr.Geometry ogrGeometry = ogrFeature.GetGeometryRef();
			int k = 0;

			for (k = 0; k < ogrFieldsCount; k++) {
				debug = "getting value #"+k;
				int ogrFieldType = ogrFeature.GetFieldType(k);

				if (ogrFeature.IsFieldSet(k)) {

					switch (ogrFieldType) {
						case ogrConstants.OFTInteger:
							debug = "integer attribute";
							r[k] = new Long(((Integer)ogrFeature.GetFieldAsInteger(k)).longValue());
							break;
						case ogrConstants.OFTReal:
							debug = "double attribute";
							r[k] = ogrFeature.GetFieldAsDouble(k);
							break;
						case ogrConstants.OFTString:
							debug = "string attribute";
							r[k] = ogrFeature.GetFieldAsString(k);
							break;
						case ogrConstants.OFTWideString:
							debug = "widestring attribute";
							r[k] = ogrFeature.GetFieldAsString(k);
							break;
						// TODO Add the cases of OGR datetime (date, time, datetime) data types and other datatypes (binary, integer|real|string lists, etc.)
//						case ogrConstants.OFTDate:
//							System.out.print(ogrFeature.GetFieldAsDateTime(id, pnYear, pnMonth, pnDay, pnHour, pnMinute, pnSecond, pnTZFlag);
//									+ ",");
//							break;
						default:
							debug = "default datatype attribute";
							r[k] = ogrFeature.GetFieldAsString(k);
							break;
					}
				} else {
					r[k] = null;
				}

			}
				
			if (ogrGeometry != null) {
				debug = "geometry attribute";
				Geometry jts_geom = new WKTReader().read(ogrGeometry.ExportToWkt());
				r[k] = jts_geom;
			}
			else r[k] = null;
			//}        	
        }
        catch(Exception e)
		{
            log.logError(toString(), "Unexpected error in part ["+debug+"] : "+e.toString());
            error = true;
            throw new KettleException("Unable to read row from the OGR data source", e);
		}
        
        return r;
    }
    
    private SRS getSRS() throws KettleException {
        if (ogrLayer != null) {
        	if(ogrLayer.GetSpatialRef() != null) {
        		try {
        			CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);
        			CoordinateReferenceSystem crs = crsFactory.createFromWKT(ogrLayer.GetSpatialRef().ExportToWkt());
        	
        			return new SRS(crs);
        		}
        		catch (FactoryException fe) {
        			// TODO Do we have to do anything else here, i.e. when a FactoryException occurs while parsing the CRS WKT of the data source?
        			return null;
        		}
        	}
        	else {
        		// TODO Do we have to do anything else here, i.e. when OGR SRS is set to unknown?
        		return null;
        	}
        }
        else {
        	throw new KettleException("OGR data source is not open");
        }
    }
    
    public boolean close()
    {
        /*boolean retval = false;
        try
        {
        	if(featIter != null) featIter.close();
        	// if(gtDataStore != null) gtDataStore.close();

            retval=true;
        }
        catch(Exception e)
        {
            log.logError(toString(), "Couldn't close iterator for datastore ["+gisURL+"] : "+e.toString());
            error = true;
        }
        
        return retval;*/
    	return true;
    }
    
    public boolean hasError()
    {
    	return error;
    }

    public String toString()
    {
    	if (ogrDataSourcePath != null)
    		return ogrDataSourcePath;
    	else return getClass().getName();
    }
    
    public String getVersionInfo()
    {
    	// return reader.getHeader().getSignatureDesc();
    	return null;
    }

	public String getOgrDataSourcePath() {
		return ogrDataSourcePath;
	}

	public void setOgrDataSourcePath(String ogrDataSourcePath) {
		this.ogrDataSourcePath = ogrDataSourcePath;
	}

}
