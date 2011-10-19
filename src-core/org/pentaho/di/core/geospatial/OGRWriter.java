package org.pentaho.di.core.geospatial;

import java.io.File;
import java.util.Vector;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Driver;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.ogr.ogrConstants;
import org.gdal.osr.SpatialReference;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.vividsolutions.jts.geom.Geometry;


/**
 * Handles writing to an OGR data source 
 * 
 *  @author tbadard
 *  @since 11-06-2010
 *
 */
public class OGRWriter
{
	private LogWriter   log;
	private boolean     error;
	private String ogrDataDestinationPath;
	private DataSource ogrDataDestination; 
	private Layer ogrLayer;
	private String ogrLayerName;
	private String ogrDataFormat;
	private String ogrOptions;
	private int ogrGeomType;
	private Driver ogrDriver;
	private Vector<String> ogrDataDestinationOptions;
	private org.gdal.ogr.Geometry ogrGeometry;
	private SpatialReference ogrSpatialReference;
	
	public OGRWriter(String dataDestinationPath, String format, String options, int geomType)
	{
		this.log = LogWriter.getInstance();
		ogrDataDestinationPath = dataDestinationPath;
		error = false;
		ogrLayer = null;
		ogrLayerName = null;
		ogrDataFormat = format;
		ogrOptions = options;
		ogrGeomType = geomType;
		ogrDriver = null;

		ogrGeometry = null;
		ogrSpatialReference = new SpatialReference();
		ogrDataDestinationOptions = new Vector<String>();
		
		if (ogrOptions!= null) {
			String[] ogr_options = ogrOptions.trim().split(" ");
			for(int i=0;i<ogr_options.length;i++)
				ogrDataDestinationOptions.addElement(ogr_options[i]);
		}
		
	}

	public void open() throws KettleException
	{
		try {

			// try closing first
			close();
			
			ogr.RegisterAll();

			for(int i = 0; i < ogr.GetDriverCount() && ogrDriver == null; i++)
	        {
				if( ogr.GetDriver(i).GetName().equalsIgnoreCase(ogrDataFormat) )
				{
					ogrDriver = ogr.GetDriver(i);
				}
	        }
						
			if (Const.isWindows()) {
				ogrDataDestinationPath = ogrDataDestinationPath.replace('/', '\\');
			} else {
				ogrDataDestinationPath = ogrDataDestinationPath.substring(2);
			}
			
			if ((new File(ogrDataDestinationPath)).exists())
				if (ogrDriver.TestCapability( ogr.ODrCDeleteDataSource ))
					ogrDriver.DeleteDataSource(ogrDataDestinationPath);
			
			ogrDataDestination = ogrDriver.CreateDataSource(ogrDataDestinationPath, ogrDataDestinationOptions);

		}
		catch (Exception e) {
			throw new KettleException("Error opening OGR data destination: "+ogrDataDestinationPath, e);
		}
	}

	public void createLayer(RowMetaInterface fields) throws KettleException
	{
		String debug="get attributes from table";

		try
		{
			debug = "create layer";
			
			ogrLayerName = ogrDataDestination.GetName();
			
			// Works if data destination is a file
			// TODO Check if the layer name is correct for other types of data destination
			
			if (Const.isWindows()) {
				if (ogrLayerName.lastIndexOf('\\')!=-1)
					ogrLayerName = ogrLayerName.substring(ogrLayerName.lastIndexOf('\\')+1);
			} else {
				if (ogrLayerName.lastIndexOf('/')!=-1)
					ogrLayerName = ogrLayerName.substring(ogrLayerName.lastIndexOf('/')+1);
			}
	        if (ogrLayerName.lastIndexOf('.')!=-1)
	        	ogrLayerName = ogrLayerName.substring(0, ogrLayerName.lastIndexOf('.'));
	        
	        SpatialReference sr = new SpatialReference();
			for(int i = 0; i < fields.size(); i++)
			{
				ValueMetaInterface value = fields.getValueMeta(i);
				if (value.getType()==ValueMeta.TYPE_GEOMETRY) {
					SRS srs = value.getGeometrySRS();
					if (srs!=null) {
						if (srs.getCRS()!=null) {
							sr.ImportFromWkt(srs.getCRS().toWKT());
						}
					}				
					break;
				}
			}
			ogrLayer = ogrDataDestination.CreateLayer(ogrLayerName,sr,ogrGeomType,ogrDataDestinationOptions);
			
			// Fetch all field information
			//
			debug="allocate data types";
			FieldDefn ogrFieldDefinition = null;

			for(int i = 0; i < fields.size(); i++)
			{           	
				if (log.isDebug()) debug="get attribute #"+i;

				ValueMetaInterface value = fields.getValueMeta(i);
				switch(value.getType()) {
					case ValueMeta.TYPE_NUMBER:
				        ogrFieldDefinition = new FieldDefn(value.getName(), ogrConstants.OFTReal);
				        ogrLayer.CreateField(ogrFieldDefinition);
						break;
					case ValueMeta.TYPE_STRING:
				        ogrFieldDefinition = new FieldDefn(value.getName(), ogrConstants.OFTString);
				        ogrLayer.CreateField(ogrFieldDefinition);					
						break;
					case ValueMeta.TYPE_DATE:
						ogrFieldDefinition = new FieldDefn(value.getName(), ogrConstants.OFTDateTime);
						ogrLayer.CreateField(ogrFieldDefinition);            	  
						break;
					//TODO Check if OFTBinary actually matches the TYPE_BOOLEAN data type
					case ValueMeta.TYPE_BOOLEAN:
						ogrFieldDefinition = new FieldDefn(value.getName(), ogrConstants.OFTBinary);
						ogrLayer.CreateField(ogrFieldDefinition);
						break;
					case ValueMeta.TYPE_INTEGER:
						ogrFieldDefinition = new FieldDefn(value.getName(), ogrConstants.OFTInteger);
						ogrLayer.CreateField(ogrFieldDefinition);            	  
						break;
					case ValueMeta.TYPE_BIGNUMBER:
						ogrFieldDefinition = new FieldDefn(value.getName(), ogrConstants.OFTReal);
						ogrLayer.CreateField(ogrFieldDefinition);
						break;
					case ValueMeta.TYPE_GEOMETRY:
						break;					
					case ValueMeta.TYPE_NONE:
					case ValueMeta.TYPE_SERIALIZABLE:
					case ValueMeta.TYPE_BINARY:
					default:
						throw new KettleException("Wrong object type for OGR data destination field: "
							+ ValueMetaInterface.typeCodes[value.getType()]);
				}

			}            
		}
		catch (Exception e) {
			throw new KettleException("Error reading metadata (in part "+debug+")", e);
		}       
	}


	public void putRow(Object[] r, RowMetaInterface fields) throws KettleException
	{       
		String debug = "access to layer definition";
		
		try {
			
			ValueMetaInterface value = null;
	        Feature ogrFeature = new Feature(ogrLayer.GetLayerDefn());
	        int j=0;
			
			for(int i = 0; i < fields.size(); i++)
			{
				value = fields.getValueMeta(i);
				switch(value.getType()) {
					case ValueMeta.TYPE_NUMBER:
						debug = "double attribute "+i;
						ogrFeature.SetField(j, (Double)r[i]);
						j++;
						break;
					case ValueMeta.TYPE_STRING:
						debug = "string attribute "+i;
						ogrFeature.SetField(j, (String)r[i]);
						j++;
						break;
					case ValueMeta.TYPE_DATE:
						debug = "date attribute "+i;
						//TODO Handle correctly writing of OGR datetime data types 
						ogrFeature.SetField(j, (String)r[i]);
						j++;
						break;
					case ValueMeta.TYPE_BOOLEAN:
						debug = "boolean attribute "+i;
						ogrFeature.SetField(j, (Integer)r[i]);
						j++;
						break;
					case ValueMeta.TYPE_INTEGER:
						debug = "integer attribute "+i;
						ogrFeature.SetField(j, (Long)r[i]);
						j++;
						break;
					case ValueMeta.TYPE_BIGNUMBER:
						debug = "big number attribute "+i;
						ogrFeature.SetField(j, (Double)r[i]);
						j++;
						break;
					// TODO We have to handle here the case where there are more than one geometry fields! 
					case ValueMeta.TYPE_GEOMETRY:
						debug = "geometry attribute "+i;
						ogrGeometry = org.gdal.ogr.Geometry.CreateFromWkt(((Geometry)r[i]).toText());
						SRS srs = value.getGeometrySRS();
						if (srs!=null) {
							if (srs.getCRS()!=null) {
								ogrSpatialReference.ImportFromWkt(srs.getCRS().toWKT());
								ogrGeometry.AssignSpatialReference(ogrSpatialReference);
							}
						}
				        ogrFeature.SetGeometry(ogrGeometry);
						break;					
					case ValueMeta.TYPE_NONE:
					case ValueMeta.TYPE_SERIALIZABLE:
					case ValueMeta.TYPE_BINARY:
					default:
						debug = "default data type attribute "+i;
						ogrFeature.SetField(j, (String)r[i]);
						j++;
						break;
				}
			}
			ogrLayer.CreateFeature(ogrFeature);
		}
		catch (Exception e) {
			throw new KettleException("An error has occured while writing features ("+debug+"):", e);
		}    
	}

	public boolean close()
	{
		boolean retval = false;
		try
		{
			if (ogrDataDestination != null) ogrDataDestination.delete();
			retval=true;
		}
		catch (Exception e)
		{
			log.logError(toString(), "Couldn't close the OGR data destination ["+ogrDataDestinationPath+"] : "+e.toString());
			error = true;
		}

		return retval;
	}

	public boolean hasError()
	{
		return error;
	}

	public String getVersionInfo()
	{
		return null;
	}    
}
