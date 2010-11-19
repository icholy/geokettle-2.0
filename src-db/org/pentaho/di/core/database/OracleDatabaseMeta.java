/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.ValueMetaInterface;

//-- Begin GeoKettle modification --
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.core.logging.LogWriter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.sql.Connection;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.ByteOrder;
import oracle.spatial.util.WKB;
import oracle.sql.STRUCT;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
//-- End GeoKettle modification --

/**
 * Contains Oracle specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
// -- Begin GeoKettle modification --
public class OracleDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface, GeodatabaseInterface
{
	private static final LogWriter LOGGER = LogWriter.getInstance();
	private final static int UNKNOWN_ORACLE_SRID = 0;
	/** specify the Oracle-SRID and get the EPSG-SRID as result */
	private final HashMap<Integer, Integer> oracle_to_epsg_cache = new HashMap<Integer, Integer>(10);
	/** specify the EPSG-srid and get the Oracle-SRID as result */
	private final HashMap<Integer, Integer> epsg_to_oracle_cache = new HashMap<Integer, Integer>(10);
	
	// -- End GeoKettle modification --
	/**
	 * Construct a new database connections.  Note that not all these parameters are not allways mandatory.
	 * 
	 * @param name The database name
	 * @param access The type of database access
	 * @param host The hostname or IP address
	 * @param db The database name
	 * @param port The port on which the database listens.
	 * @param user The username
	 * @param pass The password
	 */
	public OracleDatabaseMeta(String name, String access, String host, String db, String port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public OracleDatabaseMeta()
	{
	}

	public String getDatabaseTypeDesc()
	{
		return "ORACLE";
	}

	public String getDatabaseTypeDescLong()
	{
		return "Oracle";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_ORACLE;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_OCI, DatabaseMeta.TYPE_ACCESS_JNDI };
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 1521;
		return -1;
	}
	
	/**
	 * @return Whether or not the database can use auto increment type of fields (pk)
	 */
	public boolean supportsAutoInc()
	{
		return false;
	}
	
	/**
	 * @see org.pentaho.di.core.database.DatabaseInterface#getLimitClause(int)
	 */
	public String getLimitClause(int nrRows)
	{
		return " WHERE ROWNUM <= "+nrRows;
	}
	
	/**
	 * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
	 * @param tableName The name of the table to determine the layout for
	 * @return The SQL to launch.
	 */
	public String getSQLQueryFields(String tableName)
	{
	    return "SELECT /*+FIRST_ROWS*/ * FROM "+tableName+" WHERE ROWNUM < 1";
	}

    public String getSQLTableExists(String tablename)
    {
        return getSQLQueryFields(tablename);
    }
    
    public String getSQLColumnExists(String columnname, String tablename)
    {
        return  getSQLQueryColumnFields(columnname, tablename);
    }
    public String getSQLQueryColumnFields(String columnname, String tableName)
    {
        return "SELECT /*+FIRST_ROWS*/ " + columnname + " FROM "+tableName +" WHERE ROWNUM < 1";
    }


    
    public boolean needsToLockAllTables()
    {
        return false;
    }
	
	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "oracle.jdbc.driver.OracleDriver";
		}
	}


	
    public String getURL(String hostname, String port, String databaseName) throws KettleDatabaseException
    {
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "jdbc:odbc:"+databaseName;
		}
		else
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE)
		{
			// the database name can be a SID (starting with :) or a Service (starting with /)
			//<host>:<port>/<service>
			//<host>:<port>:<SID>
			if (databaseName != null && databaseName.length()>0 && 
					(databaseName.startsWith("/") || databaseName.startsWith(":"))) {
				return "jdbc:oracle:thin:@"+hostname+":"+port+databaseName;
			}
			else if (Const.isEmpty(getHostname()) && 
					(Const.isEmpty(getDatabasePortNumberString()) || getDatabasePortNumberString().equals("-1"))) {  //-1 when file based stored connection
				// support RAC with a self defined URL in databaseName like
				// (DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = host1-vip)(PORT = 1521))(ADDRESS = (PROTOCOL = TCP)(HOST = host2-vip)(PORT = 1521))(LOAD_BALANCE = yes)(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = db-service)(FAILOVER_MODE =(TYPE = SELECT)(METHOD = BASIC)(RETRIES = 180)(DELAY = 5))))
				// or (DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=PRIMARY_NODE_HOSTNAME)(PORT=1521))(ADDRESS=(PROTOCOL=TCP)(HOST=SECONDARY_NODE_HOSTNAME)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=DATABASE_SERVICENAME)))
				// or (DESCRIPTION=(FAILOVER=ON)(ADDRESS_LIST=(LOAD_BALANCE=ON)(ADDRESS=(PROTOCOL=TCP)(HOST=xxxxx)(PORT=1526))(ADDRESS=(PROTOCOL=TCP)(HOST=xxxx)(PORT=1526)))(CONNECT_DATA=(SERVICE_NAME=somesid)))
				return "jdbc:oracle:thin:@"+getDatabaseName();
			}
			else {
				// by default we assume a SID
				return "jdbc:oracle:thin:@"+hostname+":"+port+":"+databaseName;
			}
		}
		else // OCI
		{
		    // Let's see if we have an database name
            if (getDatabaseName()!=null && getDatabaseName().length()>0)
            {
                // Has the user specified hostname & port number?
                if (getHostname()!=null && getHostname().length()>0 && getDatabasePortNumberString()!=null && getDatabasePortNumberString().length()>0) {
                    // User wants the full url
                    return "jdbc:oracle:oci:@(description=(address=(host="+getHostname()+")(protocol=tcp)(port="+getDatabasePortNumberString()+"))(connect_data=(sid="+getDatabaseName()+")))";
                } else {
                    // User wants the shortcut url
                    return "jdbc:oracle:oci:@"+getDatabaseName();
                }               
            }
            else
            {
                throw new KettleDatabaseException("Unable to construct a JDBC URL: at least the database name must be specified");
            }
		}
	}
    
    /**
     * Oracle doesn't support options in the URL, we need to put these in a Properties object at connection time...
     */
    public boolean supportsOptionsInURL()
    {
        return false;
    }

	/**
	 * @return true if the database supports sequences
	 */
	public boolean supportsSequences()
	{
		return true;
	}

    /**
     * Check if a sequence exists.
     * @param sequenceName The sequence to check
     * @return The SQL to get the name of the sequence back from the databases data dictionary
     */
    public String getSQLSequenceExists(String sequenceName)
    {
        return "SELECT * FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '"+sequenceName.toUpperCase()+"'";
    }
    
    /**
     * Get the current value of a database sequence
     * @param sequenceName The sequence to check
     * @return The current value of a database sequence
     */
    public String getSQLCurrentSequenceValue(String sequenceName)
    {
        return "SELECT "+sequenceName+".currval FROM DUAL";
    }

    /**
     * Get the SQL to get the next value of a sequence. (Oracle only) 
     * @param sequenceName The sequence name
     * @return the SQL to get the next value of a sequence. (Oracle only)
     */
    public String getSQLNextSequenceValue(String sequenceName)
    {
        return "SELECT "+sequenceName+".nextval FROM dual";
    }


	/**
	 * @return true if we need to supply the schema-name to getTables in order to get a correct list of items.
	 */
	public boolean useSchemaNameForTableList()
	{
		return true;
	}

	/**
	 * @return true if the database supports synonyms
	 */
	public boolean supportsSynonyms()
	{
		return true;
	}

	/**
	 * Generates the SQL statement to add a column to the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to add a column to the specified table
	 */
	public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" ADD ( "+getFieldDefinition(v, tk, pk, use_autoinc, true, false)+" ) ";
	}

	/**
	 * Generates the SQL statement to drop a column from the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to drop a column from the specified table
	 */
	public String getDropColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" DROP ( "+v.getName()+" ) "+Const.CR;
	}

	/**
	 * Generates the SQL statement to modify a column in the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to modify a column in the specified table
	 */
	public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
        ValueMetaInterface tmpColumn = v.clone(); 
        int threeoh = v.getName().length()>=30 ? 30 : v.getName().length();
        
        tmpColumn.setName(v.getName().substring(0,threeoh)+"_KTL"); // should always be less then 35
        
        String sql="";
        
        // Create a new tmp column
        sql+=getAddColumnStatement(tablename, tmpColumn, tk, use_autoinc, pk, semicolon)+";"+Const.CR;
        // copy the old data over to the tmp column
        sql+="UPDATE "+tablename+" SET "+tmpColumn.getName()+"="+v.getName()+";"+Const.CR;
        // drop the old column
        sql+=getDropColumnStatement(tablename, v, tk, use_autoinc, pk, semicolon)+";"+Const.CR;
        // create the wanted column
        sql+=getAddColumnStatement(tablename, v, tk, use_autoinc, pk, semicolon)+";"+Const.CR;
        // copy the data from the tmp column to the wanted column (again)  
        // All this to avoid the rename clause as this is not supported on all Oracle versions
        sql+="UPDATE "+tablename+" SET "+v.getName()+"="+tmpColumn.getName()+";"+Const.CR;
        // drop the temp column
        sql+=getDropColumnStatement(tablename, tmpColumn, tk, use_autoinc, pk, semicolon);
        
        return sql;
	}

	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
		StringBuffer retval=new StringBuffer(128);
		
		String fieldname = v.getName();
		int    length    = v.getLength();
		int    precision = v.getPrecision();
		
		if (add_fieldname) retval.append(fieldname).append(' ');
		
		int type         = v.getType();
		switch(type)
		{
		case ValueMetaInterface.TYPE_DATE   : retval.append("DATE"); break;
		case ValueMetaInterface.TYPE_BOOLEAN: retval.append("CHAR(1)"); break;
		case ValueMetaInterface.TYPE_NUMBER : 
        case ValueMetaInterface.TYPE_BIGNUMBER: 
			retval.append("NUMBER"); 
			if (length>0)
			{
				retval.append('(').append(length);
				if (precision>0)
				{
					retval.append(", ").append(precision);
				}
				retval.append(')');
			}
			break;
		case ValueMetaInterface.TYPE_INTEGER:  
			retval.append("INTEGER"); 
			break;			
		case ValueMetaInterface.TYPE_STRING:
			if (length>=DatabaseMeta.CLOB_LENGTH)
			{
				retval.append("CLOB");
			}
			else
			{
				if (length==1) {
					retval.append("CHAR(1)");
				} else if (length>0 && length<=2000)
				{
					retval.append("VARCHAR2(").append(length).append(')');
				}
				else
				{
                    if (length<=0)
                    {
                        retval.append("VARCHAR2(2000)"); // We don't know, so we just use the maximum...
                    }
                    else
                    {
                        retval.append("CLOB"); 
                    }
				}
			}
			break;
        case ValueMetaInterface.TYPE_BINARY: // the BLOB can contain binary data.
            {
                retval.append("BLOB");
            }
            break;
        // -- Begin GeoKettle modification --
		case ValueMetaInterface.TYPE_GEOMETRY:
			// TODO: see PostGIS implementation for more todos
			retval.append("MDSYS.SDO_GEOMETRY");
			break;
        // -- End GeoKettle modification --
		default:
			retval.append(" UNKNOWN");
			break;
		}
		
		if (add_cr) retval.append(Const.CR);
		
		return retval.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.ibridge.kettle.core.database.DatabaseInterface#getReservedWords()
	 */
	public String[] getReservedWords()
	{
		return new String[] 
	     {
			"ACCESS", "ADD", "ALL", "ALTER", "AND", "ANY", "ARRAYLEN", "AS", "ASC", "AUDIT", "BETWEEN",
			"BY", "CHAR", "CHECK", "CLUSTER", "COLUMN", "COMMENT", "COMPRESS", "CONNECT", "CREATE", "CURRENT", "DATE",
			"DECIMAL", "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP", "ELSE", "EXCLUSIVE", "EXISTS", "FILE", "FLOAT",
			"FOR", "FROM", "GRANT", "GROUP", "HAVING", "IDENTIFIED", "IMMEDIATE", "IN", "INCREMENT", "INDEX", "INITIAL",
			"INSERT", "INTEGER", "INTERSECT", "INTO", "IS", "LEVEL", "LIKE", "LOCK", "LONG", "MAXEXTENTS", "MINUS",
			"MODE", "MODIFY", "NOAUDIT", "NOCOMPRESS", "NOT", "NOTFOUND", "NOWAIT", "NULL", "NUMBER", "OF", "OFFLINE",
			"ON", "ONLINE", "OPTION", "OR", "ORDER", "PCTFREE", "PRIOR", "PRIVILEGES", "PUBLIC", "RAW", "RENAME",
			"RESOURCE", "REVOKE", "ROW", "ROWID", "ROWLABEL", "ROWNUM", "ROWS", "SELECT", "SESSION", "SET", "SHARE",
			"SIZE", "SMALLINT", "SQLBUF", "START", "SUCCESSFUL", "SYNONYM", "SYSDATE", "TABLE", "THEN", "TO", "TRIGGER",
			"UID", "UNION", "UNIQUE", "UPDATE", "USER", "VALIDATE", "VALUES", "VARCHAR", "VARCHAR2", "VIEW", "WHENEVER",
			"WHERE", "WITH", 
			// -- Begin GeoKettle modification --
			"MDSYS.SDO_GEOMETRY"
	        // -- End GeoKettle modification --
		 };
	}
	
	/**
	 * @return The SQL on this database to get a list of stored procedures.
	 */
	public String getSQLListOfProcedures()
	{
		return  "SELECT DISTINCT DECODE(package_name, NULL, '', package_name||'.')||object_name FROM user_arguments"; 
	}

    public String getSQLLockTables(String tableNames[])
    {
        StringBuffer sql=new StringBuffer(128);
        for (int i=0;i<tableNames.length;i++)
        {
            sql.append("LOCK TABLE ").append(tableNames[i]).append(" IN EXCLUSIVE MODE;").append(Const.CR);
        }
        return sql.toString();
    }
    
    public String getSQLUnlockTables(String tableNames[])
    {
        return null; // commit handles the unlocking!
    }
    
    /**
     * @return extra help text on the supported options on the selected database platform.
     */
    public String getExtraOptionsHelpText()
    {
        return  "http://download.oracle.com/docs/cd/B19306_01/java.102/b14355/urls.htm#i1006362";
    }

    public String[] getUsedLibraries()
    {
    	// -- Begin GeoKettle modification --
        return new String[] { "ojdbc14.jar", "orai18n.jar", "sdoapi.jar", "sdoutl.jar" };
        // -- End GeoKettle modification --
    }
    
    // -- Begin GeoKettle modification --

    /*
     * (non-Javadoc)
     * @see org.pentaho.di.core.database.GeodatabaseInterface#convertToJTSGeometry(java.lang.Object)
     */
	public Geometry convertToJTSGeometry(ValueMetaInterface vmi, Object obj, Database db) {
		if (obj instanceof oracle.sql.STRUCT) {
			try {
				// Map Oracle's SRID with the EPSG-SRID or take a custom SRS from WKT
				int oracle_srid = JGeometry.load((STRUCT)obj).getSRID();
				// see bug 2845785; disabled reading of SRS metadata as temporary workaround,
				// meanwhile one must assign SRS manually using Set SRS step
				// TODO: review everything!
				// SRS epsg_srid = convertToEPSG_SRID(oracle_srid, db.getConnection());
				SRS epsg_srid = SRS.UNKNOWN;
				vmi.setGeometrySRS(epsg_srid);
				
				WKB wkb = new WKB(ByteOrder.BIG_ENDIAN);		// Create empty WKB representation
				byte[] b = wkb.fromSTRUCT( (STRUCT) obj );		// convert: Object -> STRUCT -> byte[]
				Geometry jtsGeom = (new WKBReader()).read(b);	// convert: byte[] -> JTS-Geometry
				jtsGeom.setSRID(epsg_srid.getSRID());			// set the SRID of the JTS-Geometry
				return jtsGeom;
			} catch (Exception e) {
				LOGGER.logError("GeoKettle", "Conversion from Oracle-geometry failed.");
				return null;
			}
		} else {
			LOGGER.logDetailed("GeoKettle", "No Oracle-geometry found to convert.");
			return null;
		}
	}

    /*
	 * (non-Javadoc)
	 * @seeorg.pentaho.di.core.database.GeodatabaseInterface#convertToObject(com.vividsolutions.jts.geom.Geometry, java.sql.Connection)
	 */
	public Object convertToObject(ValueMetaInterface vmi, Geometry jtsGeom, Database db) {
		WKBWriter wkbWriter = new WKBWriter();
		byte[] b = wkbWriter.write(jtsGeom);
		WKB wkb = new WKB(ByteOrder.BIG_ENDIAN);
		try {
			// Map the EPSG- (or custom-) SRID with Oracle's SRID.
			// TODO: review this, probably has some of the same issues as when reading the SRID
			int oracle_srid = convertToDBMS_SRID(vmi.getGeometrySRS(), db.getConnection());
			
			STRUCT oracleStruct = wkb.toSTRUCT(b, db.getConnection());			// convert: byte[] --> STRUCT (using DB connection)
			JGeometry oracleGeom = JGeometry.load(oracleStruct);				// convert: STRUCT --> JGeometry
			oracleGeom.setSRID(oracle_srid);									// set the SRID
			oracleStruct = JGeometry.store(db.getConnection(), oracleGeom);		// convert: JGeometry --> STRUCT (using DB connection)
			return oracleStruct;
		} catch (Exception e) {
			LOGGER.logError("GeoKettle", "Conversion to Oracle-geometry failed.");
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.core.database.GeodatabaseInterface#convertToDBMS_SRID(int, java.sql.Connection)
	 */
	public int convertToDBMS_SRID(SRS epsg_srid, Connection conn) {
		if (epsg_srid.is_custom) {
			// Try locating an already existing spatial reference system in the
			// MDSYS.SDO_CS_SRS table (comapring the WKT-representation).
			try {
				return lookupDBMSSRID(epsg_srid.getCRS().toWKT(), conn);
			} catch (Exception e) {
				LOGGER.logError("GeoKettle", "WKT-lookup of custom spatial reference system failed!", e);
			}
		}
		
		Integer cached_oracle_srid = epsg_to_oracle_cache.get(epsg_srid);
		// cache the result, if necessary
		if ( cached_oracle_srid == null )  {
			cached_oracle_srid = translateSRID(epsg_srid.getSRID(), true, conn);
			cache(cached_oracle_srid, epsg_srid.getSRID());
		}
		// Map GeoKettles "UNKNOWN_SRID" to Oracle's "UNKNOWN SRID" (zero)
		return cached_oracle_srid != SRS.UNKNOWN_SRID ? cached_oracle_srid : UNKNOWN_ORACLE_SRID; 
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.core.database.GeodatabaseInterface#convertToEPSG_SRID(int, java.sql.Connection)
	 */
	public SRS convertToEPSG_SRID(int dbms_srid, Connection conn) {
		Integer cached_epsg_srid = oracle_to_epsg_cache.get(dbms_srid);
		// cache the result, if necessary
		if ( cached_epsg_srid == null )  {
			cached_epsg_srid = translateSRID(dbms_srid, false, conn);
    		cache(dbms_srid, cached_epsg_srid);
		}

		// Lookup the SRS in spatial_ref_sys table and create a new SRS from the WKT
		SRS epsg_srs = SRS.createFromEPSG(Integer.toString(cached_epsg_srid != UNKNOWN_ORACLE_SRID ? cached_epsg_srid : SRS.UNKNOWN_SRID));
		if (epsg_srs.equals(SRS.UNKNOWN) && dbms_srid != SRS.UNKNOWN_SRID && dbms_srid != UNKNOWN_ORACLE_SRID) {
			String sql = "SELECT wktext FROM MDSYS.SDO_CS_SRS WHERE srid = " + dbms_srid;
			Statement statement;
			String wkt = "";
			try {
				statement = conn.createStatement();
				ResultSet result = statement.executeQuery(sql);
				if (result.next())
					wkt = result.getString(1);
			} catch (SQLException e) { }
			if (!Const.isEmpty(wkt)) {
				try {
					SRS wktSRS = new SRS(wkt);
					LOGGER.logDetailed("GeoKettle", "The Oracle-SRID ("+dbms_srid+") was changed to a custom SRID.");
					return wktSRS;
				} catch (KettleStepException e) {
					LOGGER.logError("GeoKettle", "Creation of a custom spatial reference system failed.");
					return SRS.UNKNOWN;
				}
			} else {
				LOGGER.logError("GeoKettle", "The WKT from the Oracle MDSYS.SDO_CS_SRS table could not be retrieved for "+dbms_srid+".");
				return SRS.UNKNOWN;
			}
		}
		// Return a valid EPSG SRS
		else {
			LOGGER.logDetailed("GeoKettle", "The read Oracle-SRID ("+dbms_srid+") was transformed to EPSG-SRID "+epsg_srs.srid+".");
			return epsg_srs;
		}
	}
	
	/**
	 * Caches the SRIDs.
	 * 
	 * @param oracle_srid The Oracle SRID to cache.
	 * @param epsg_srid The EPSG SRID to cache.
	 */
	private void cache(int oracle_srid, int epsg_srid) {
		oracle_to_epsg_cache.put(new Integer(oracle_srid), new Integer(epsg_srid));
		epsg_to_oracle_cache.put(new Integer(epsg_srid), new Integer(oracle_srid));
	}
	
	/**
	 * Oracle uses a native SRID so the EPSG-SRID must be converted to Oracle's format.
     * This operation uses a cache to avoid unnecessary database connections.
     * 
	 * @param newSRID SRID to lookup.
	 * @param epsg_to_oracle Specifies the direction of mapping.
	 * @throws SQLException 
	 */
    private int translateSRID(int newSRID, boolean epsg_to_oracle, Connection conn) {
    	// allocate all necessary db connections ressources
		String sql = epsg_to_oracle 
			? "SELECT MDSYS.SDO_CS.MAP_EPSG_SRID_TO_ORACLE("+newSRID+") FROM DUAL" 
			: "SELECT MDSYS.SDO_CS.MAP_ORACLE_SRID_TO_EPSG("+newSRID+") FROM DUAL";
		
		int srid;
		try {
			Statement statement = conn.createStatement();
			ResultSet result = statement.executeQuery(sql);
			srid = result.next() ? result.getInt(1) : SRS.UNKNOWN_SRID;
			result.close();
			statement.close();
		} catch (SQLException e) {
			srid = SRS.UNKNOWN_SRID;
			LOGGER.logBasic("GeoKettle", "No Oracle-SRID was found in the database. Using -1 instead of Oracle:" + newSRID);
		}
		
		return srid;
    }
    
    /**
	 * Looks-up a SRID in the DBMS's table where the spatial references are stored.
	 * The lookup is done by delivering the WKT-representation of the spatial ref-
	 * erence system.
	 * 
	 * @param wkt The WKT-representation of the spatial reference system.
	 * @param conn The {@link Connection} to the database.
	 * @return The DBMS's SRID for the WKT-representation. SRS.UNKNOWN_SRID if none is found.
	 * @throws SQLException
	 */
	private int lookupDBMSSRID(String wkt, Connection conn) {
    	// allocate all necessary db connections ressources
		String sql = "SELECT srid FROM MDSYS.SDO_CS_SRS WHERE wktext = '"+ wkt +"'";
		
		int srid;
		try {
			Statement statement = conn.createStatement();
			ResultSet result = statement.executeQuery(sql);
			srid = result.next() ? result.getInt(1) : SRS.UNKNOWN_SRID;
			result.close();
			statement.close();
		} catch (SQLException e) {
			srid = SRS.UNKNOWN_SRID;
			LOGGER.logError("GeoKettle", "SRID-lookup: The WKT-representation was not found in MDSYS.SDO_CS_SRS table.");
		}
		
		return srid;
    }
	// -- End GeoKettle modification --
	
}
