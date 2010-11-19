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
import org.pentaho.di.core.row.ValueMetaInterface;

// -- Begin GeoKettle modification --
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.core.logging.LogWriter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.postgis.PGgeometry;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
// -- End GeoKettle modification --

/**
 * Contains PostgreSQL specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
// -- Begin GeoKettle modification --
public class PostgreSQLDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface, GeodatabaseInterface {
	private static final LogWriter LOGGER = LogWriter.getInstance();
	private final HashMap<Integer, Integer> wkt_to_srid_cache = new HashMap<Integer, Integer>(10);
	private final HashMap<Integer, SRS> srid_to_srs_cache = new HashMap<Integer, SRS>(10);
// -- End GeoKettle modification --
	/**
	 * Construct a new database connection.
	 * 
	 */
	public PostgreSQLDatabaseMeta(String name, String access, String host, String db, String port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public PostgreSQLDatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "POSTGRESQL";
	}

	public String getDatabaseTypeDescLong()
	{
		return "PostgreSQL";
	}
  
  /**
   * @return The extra option separator in database URL for this platform
   */
  public String getExtraOptionSeparator()
  {
      return "&";
  }
  
  /**
   * @return This indicator separates the normal URL from the options
   */
  public String getExtraOptionIndicator()
  {
      return "?";
  }
  
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_POSTGRES;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 5432;
		return -1;
	}

	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "org.postgresql.Driver";
		}
	}

    public String getURL(String hostname, String port, String databaseName)
    {
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "jdbc:odbc:"+databaseName;
		}
		else
		{
			return "jdbc:postgresql://"+hostname+":"+port+"/"+databaseName;
		}
	}

	/**
	 * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
	 * @return true is setFetchSize() is supported!
	 */
	public boolean isFetchSizeSupported()
	{
		return true;
	}

	/**
	 * @return true if the database supports bitmap indexes
	 */
	public boolean supportsBitmapIndex()
	{
		return false;
	}

	/**
	 * @return true if the database supports synonyms
	 */
	public boolean supportsSynonyms()
	{
		return false;
	}
    
    public boolean supportsSequences()
    {
        return true;
    }
    
    /**
     * Support for the serial field is only fake in PostgreSQL.
     * You can't get back the value after the inserts (getGeneratedKeys) through JDBC calls.
     * Therefor it's wiser to use the built-in sequence support directly, not the auto increment features.
     */
    public boolean supportsAutoInc()
    {
        return true;
    }
    
    public String getLimitClause(int nrRows)
    {
        return " limit "+nrRows;
    }
    
    public String getSQLQueryFields(String tableName)
    {
        return "SELECT * FROM "+tableName+getLimitClause(1);
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
        return "SELECT " + columnname + " FROM "+tableName+getLimitClause(1);
    }


    
    public boolean needsToLockAllTables()
    {
        return false;
    }
    
    /**
     * Get the SQL to get the next value of a sequence. (PostgreSQL version) 
     * @param sequenceName The sequence name
     * @return the SQL to get the next value of a sequence.
     */
    public String getSQLNextSequenceValue(String sequenceName)
    {
        return "SELECT nextval('"+sequenceName+"')";
    }
    
    /**
     * Get the SQL to get the next value of a sequence. (PostgreSQL version) 
     * @param sequenceName The sequence name
     * @return the SQL to get the next value of a sequence.
     */
    public String getSQLCurrentSequenceValue(String sequenceName)
    {
        return "SELECT last_value FROM "+sequenceName;
    }
    
    /**
     * Check if a sequence exists.
     * @param sequenceName The sequence to check
     * @return The SQL to get the name of the sequence back from the databases data dictionary
     */
    public String getSQLSequenceExists(String sequenceName)
    {
        return "SELECT relname AS sequence_name FROM pg_statio_all_sequences WHERE relname = '"+sequenceName.toLowerCase()+"'";
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
		return "ALTER TABLE "+tablename+" ADD COLUMN "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
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
		return "ALTER TABLE "+tablename+" DROP COLUMN "+v.getName()+Const.CR;
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
		String retval="";
		retval+="ALTER TABLE "+tablename+" DROP COLUMN "+v.getName()+Const.CR+";"+Const.CR;
		retval+="ALTER TABLE "+tablename+" ADD COLUMN "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
		return retval;
	}

	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
		String retval="";
		
		String fieldname = v.getName();
		int    length    = v.getLength();
		int    precision = v.getPrecision();
		
		if (add_fieldname) retval+=fieldname+" ";
		
		int type         = v.getType();
		switch(type)
		{
		case ValueMetaInterface.TYPE_DATE   : retval+="TIMESTAMP"; break;
		case ValueMetaInterface.TYPE_BOOLEAN: 
			if (supportsBooleanDataType()) {
				retval+="BOOLEAN"; 
			} else {
				retval+="CHAR(1)";
			}
			break;
		case ValueMetaInterface.TYPE_NUMBER : 
		case ValueMetaInterface.TYPE_INTEGER: 
        case ValueMetaInterface.TYPE_BIGNUMBER: 
			if (fieldname.equalsIgnoreCase(tk) || // Technical key
			    fieldname.equalsIgnoreCase(pk)    // Primary key
			    ) 
			{
				retval+="BIGSERIAL";
			} 
			else
			{
				if (length>0)
				{
					if (precision>0 || length>18)
					{
						retval+="NUMERIC("+length+", "+precision+")";
					}
					else
					{
						if (length>9)
						{
							retval+="BIGINT";
						}
						else
						{
							if (length<5)
							{
								retval+="SMALLINT";
							}
							else
							{
								retval+="INTEGER";
							}
						}
					}
					
				}
				else
				{
					retval+="DOUBLE PRECISION";
				}
			}
			break;
		case ValueMetaInterface.TYPE_STRING:
			if (length<1 || length>=DatabaseMeta.CLOB_LENGTH)
			{
				retval+="TEXT";
			}
			else
			{
				retval+="VARCHAR("+length+")"; 
			}
			break;
		// -- Begin GeoKettle modification --
		case ValueMetaInterface.TYPE_GEOMETRY:
			// TODO: complete geometry support
			// we should check if PostGIS is enabled for this DB...
			// also it is not sufficient to create a column with
			// GEOMETRY type, we should either use PostGIS' AddGeometryColumn(...)
			// function (not practical) or manually update the GEOMETRY_COLUMNS table
			// and add the necessary integrity constraints.
			// (better to do this in a separate SQL script in a production environment...)
			retval+="geometry";
			break;
		// -- End GeoKettle modification --
		default:
			retval+=" UNKNOWN";
			break;
		}
		
		if (add_cr) retval+=Const.CR;
		
		return retval;
	}
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.core.database.DatabaseInterface#getSQLListOfProcedures()
	 */
	public String getSQLListOfProcedures()
	{
		return  "select proname " +
				"from pg_proc, pg_user " +
				"where pg_user.usesysid = pg_proc.proowner " +
				"and upper(pg_user.usename) = '"+getUsername().toUpperCase()+"'"
				;
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.database.DatabaseInterface#getReservedWords()
	 */
	public String[] getReservedWords()
	{
		return new String[]
		{
			// http://www.postgresql.org/docs/8.1/static/sql-keywords-appendix.html
			// added also non-reserved key words because there is progress from the Postgre developers to add them
			"A", "ABORT", "ABS", "ABSOLUTE", "ACCESS", "ACTION", "ADA", "ADD", "ADMIN", "AFTER", "AGGREGATE", 
			"ALIAS", "ALL", "ALLOCATE", "ALSO", "ALTER", "ALWAYS", "ANALYSE", "ANALYZE", "AND", "ANY", "ARE", 
			"ARRAY", "AS", "ASC", "ASENSITIVE", "ASSERTION", "ASSIGNMENT", "ASYMMETRIC", "AT", "ATOMIC", 
			"ATTRIBUTE", "ATTRIBUTES", "AUTHORIZATION", "AVG", 
			"BACKWARD", "BEFORE", "BEGIN", "BERNOULLI", "BETWEEN", "BIGINT", "BINARY", "BIT", "BITVAR", 
			"BIT_LENGTH", "BLOB", "BOOLEAN", "BOTH", "BREADTH", "BY", 
			"C", "CACHE", "CALL", "CALLED", "CARDINALITY", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", 
			"CATALOG_NAME", "CEIL", "CEILING", "CHAIN", "CHAR", "CHARACTER", "CHARACTERISTICS", "CHARACTERS", 
			"CHARACTER_LENGTH", "CHARACTER_SET_CATALOG", "CHARACTER_SET_NAME", "CHARACTER_SET_SCHEMA", 
			"CHAR_LENGTH", "CHECK", "CHECKED", "CHECKPOINT", "CLASS", "CLASS_ORIGIN", "CLOB", "CLOSE", "CLUSTER", 
			"COALESCE", "COBOL", "COLLATE", "COLLATION", "COLLATION_CATALOG", "COLLATION_NAME", "COLLATION_SCHEMA", 
			"COLLECT", "COLUMN", "COLUMN_NAME", "COMMAND_FUNCTION", "COMMAND_FUNCTION_CODE", "COMMENT", "COMMIT", 
			"COMMITTED", "COMPLETION", "CONDITION", "CONDITION_NUMBER", "CONNECT", "CONNECTION", "CONNECTION_NAME", 
			"CONSTRAINT", "CONSTRAINTS", "CONSTRAINT_CATALOG", "CONSTRAINT_NAME", "CONSTRAINT_SCHEMA", "CONSTRUCTOR", 
			"CONTAINS", "CONTINUE", "CONVERSION", "CONVERT", "COPY", "CORR", "CORRESPONDING", "COUNT", "COVAR_POP", 
			"COVAR_SAMP", "CREATE", "CREATEDB", "CREATEROLE", "CREATEUSER", "CROSS", "CSV", "CUBE", "CUME_DIST", 
			"CURRENT", "CURRENT_DATE", "CURRENT_DEFAULT_TRANSFORM_GROUP", "CURRENT_PATH", "CURRENT_ROLE", 
			"CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_TRANSFORM_GROUP_FOR_TYPE", "CURRENT_USER", "CURSOR", 
			"CURSOR_NAME", "CYCLE", 
			"DATA", "DATABASE", "DATE", "DATETIME_INTERVAL_CODE", "DATETIME_INTERVAL_PRECISION", "DAY", 
			"DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFAULTS", "DEFERRABLE", "DEFERRED", "DEFINED", 
			"DEFINER", "DEGREE", "DELETE", "DELIMITER", "DELIMITERS", "DENSE_RANK", "DEPTH", "DEREF", "DERIVED", 
			"DESC", "DESCRIBE", "DESCRIPTOR", "DESTROY", "DESTRUCTOR", "DETERMINISTIC", "DIAGNOSTICS", "DICTIONARY", 
			"DISABLE", "DISCONNECT", "DISPATCH", "DISTINCT", "DO", "DOMAIN", "DOUBLE", "DROP", "DYNAMIC", 
			"DYNAMIC_FUNCTION", "DYNAMIC_FUNCTION_CODE", 
			"EACH", "ELEMENT", "ELSE", "ENABLE", "ENCODING", "ENCRYPTED", "END", "END-EXEC", "EQUALS", "ESCAPE", 
			"EVERY", "EXCEPT", "EXCEPTION", "EXCLUDE", "EXCLUDING", "EXCLUSIVE", "EXEC", "EXECUTE", "EXISTING", 
			"EXISTS", "EXP", "EXPLAIN", "EXTERNAL", "EXTRACT", 
			"FALSE", "FETCH", "FILTER", "FINAL", "FIRST", "FLOAT", "FLOOR", "FOLLOWING", "FOR", "FORCE", "FOREIGN", 
			"FORTRAN", "FORWARD", "FOUND", "FREE", "FREEZE", "FROM", "FULL", "FUNCTION", "FUSION", 
			"G", "GENERAL", "GENERATED", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GRANTED", "GREATEST", "GROUP", 
			"GROUPING", 
			"HANDLER", "HAVING", "HEADER", "HIERARCHY", "HOLD", "HOST", "HOUR", 
			"IDENTITY", "IGNORE", "ILIKE", "IMMEDIATE", "IMMUTABLE", "IMPLEMENTATION", "IMPLICIT", "IN", 
			"INCLUDING", "INCREMENT", "INDEX", "INDICATOR", "INFIX", "INHERIT", "INHERITS", "INITIALIZE", 
			"INITIALLY", "INNER", "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INSTANCE", "INSTANTIABLE", "INSTEAD", 
			"INT", "INTEGER", "INTERSECT", "INTERSECTION", "INTERVAL", "INTO", "INVOKER", "IS", "ISNULL", 
			"ISOLATION", "ITERATE", 
			"JOIN", 
			"K", "KEY", "KEY_MEMBER", "KEY_TYPE", 
			"LANCOMPILER", "LANGUAGE", "LARGE", "LAST", "LATERAL", "LEADING", "LEAST", "LEFT", "LENGTH", "LESS", 
			"LEVEL", "LIKE", "LIMIT", "LISTEN", "LN", "LOAD", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOCATION", 
			"LOCATOR", "LOCK", "LOGIN", "LOWER", 
			"M", "MAP", "MATCH", "MATCHED", "MAX", "MAXVALUE", "MEMBER", "MERGE", "MESSAGE_LENGTH", 
			"MESSAGE_OCTET_LENGTH", "MESSAGE_TEXT", "METHOD", "MIN", "MINUTE", "MINVALUE", "MOD", "MODE", 
			"MODIFIES", "MODIFY", "MODULE", "MONTH", "MORE", "MOVE", "MULTISET", "MUMPS", 
			"NAME", "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NESTING", "NEW", "NEXT", "NO", "NOCREATEDB", 
			"NOCREATEROLE", "NOCREATEUSER", "NOINHERIT", "NOLOGIN", "NONE", "NORMALIZE", "NORMALIZED", "NOSUPERUSER", 
			"NOT", "NOTHING", "NOTIFY", "NOTNULL", "NOWAIT", "NULL", "NULLABLE", "NULLIF", "NULLS", "NUMBER", "NUMERIC", 
			"OBJECT", "OCTETS", "OCTET_LENGTH", "OF", "OFF", "OFFSET", "OIDS", "OLD", "ON", "ONLY", "OPEN", 
			"OPERATION", "OPERATOR", "OPTION", "OPTIONS", "OR", "ORDER", "ORDERING", "ORDINALITY", "OTHERS", "OUT", 
			"OUTER", "OUTPUT", "OVER", "OVERLAPS", "OVERLAY", "OVERRIDING", "OWNER", 
			"PAD", "PARAMETER", "PARAMETERS", "PARAMETER_MODE", "PARAMETER_NAME", "PARAMETER_ORDINAL_POSITION", 
			"PARAMETER_SPECIFIC_CATALOG", "PARAMETER_SPECIFIC_NAME", "PARAMETER_SPECIFIC_SCHEMA", "PARTIAL", 
			"PARTITION", "PASCAL", "PASSWORD", "PATH", "PERCENTILE_CONT", "PERCENTILE_DISC", "PERCENT_RANK", 
			"PLACING", "PLI", "POSITION", "POSTFIX", "POWER", "PRECEDING", "PRECISION", "PREFIX", "PREORDER", 
			"PREPARE", "PREPARED", "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURAL", "PROCEDURE", "PUBLIC", 
			"QUOTE", 
			"RANGE", "RANK", "READ", "READS", "REAL", "RECHECK", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", 
			"REGR_AVGX", "REGR_AVGY", "REGR_COUNT", "REGR_INTERCEPT", "REGR_R2", "REGR_SLOPE", "REGR_SXX", 
			"REGR_SXY", "REGR_SYY", "REINDEX", "RELATIVE", "RELEASE", "RENAME", "REPEATABLE", "REPLACE", "RESET", 
			"RESTART", "RESTRICT", "RESULT", "RETURN", "RETURNED_CARDINALITY", "RETURNED_LENGTH", "RETURNED_OCTET_LENGTH", 
			"RETURNED_SQLSTATE", "RETURNS", "REVOKE", "RIGHT", "ROLE", "ROLLBACK", "ROLLUP", "ROUTINE", 
			"ROUTINE_CATALOG", "ROUTINE_NAME", "ROUTINE_SCHEMA", "ROW", "ROWS", "ROW_COUNT", "ROW_NUMBER", "RULE", 
			"SAVEPOINT", "SCALE", "SCHEMA", "SCHEMA_NAME", "SCOPE", "SCOPE_CATALOG", "SCOPE_NAME", "SCOPE_SCHEMA", 
			"SCROLL", "SEARCH", "SECOND", "SECTION", "SECURITY", "SELECT", "SELF", "SENSITIVE", "SEQUENCE", 
			"SERIALIZABLE", "SERVER_NAME", "SESSION", "SESSION_USER", "SET", "SETOF", "SETS", "SHARE", "SHOW", 
			"SIMILAR", "SIMPLE", "SIZE", "SMALLINT", "SOME", "SOURCE", "SPACE", "SPECIFIC", "SPECIFICTYPE", 
			"SPECIFIC_NAME", "SQL", "SQLCODE", "SQLERROR", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQRT", 
			"STABLE", "START", "STATE", "STATEMENT", "STATIC", "STATISTICS", "STDDEV_POP", "STDDEV_SAMP", "STDIN", 
			"STDOUT", "STORAGE", "STRICT", "STRUCTURE", "STYLE", "SUBCLASS_ORIGIN", "SUBLIST", "SUBMULTISET", 
			"SUBSTRING", "SUM", "SUPERUSER", "SYMMETRIC", "SYSID", "SYSTEM", "SYSTEM_USER", 
			"TABLE", "TABLESAMPLE", "TABLESPACE", "TABLE_NAME", "TEMP", "TEMPLATE", "TEMPORARY", "TERMINATE", 
			"THAN", "THEN", "TIES", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TOAST", 
			"TOP_LEVEL_COUNT", "TRAILING", "TRANSACTION", "TRANSACTIONS_COMMITTED", "TRANSACTIONS_ROLLED_BACK", 
			"TRANSACTION_ACTIVE", "TRANSFORM", "TRANSFORMS", "TRANSLATE", "TRANSLATION", "TREAT", "TRIGGER", 
			"TRIGGER_CATALOG", "TRIGGER_NAME", "TRIGGER_SCHEMA", "TRIM", "TRUE", "TRUNCATE", "TRUSTED", "TYPE", 
			"UESCAPE", "UNBOUNDED", "UNCOMMITTED", "UNDER", "UNENCRYPTED", "UNION", "UNIQUE", "UNKNOWN", "UNLISTEN", 
			"UNNAMED", "UNNEST", "UNTIL", "UPDATE", "UPPER", "USAGE", "USER", "USER_DEFINED_TYPE_CATALOG", 
			"USER_DEFINED_TYPE_CODE", "USER_DEFINED_TYPE_NAME", "USER_DEFINED_TYPE_SCHEMA", "USING", 
			"VACUUM", "VALID", "VALIDATOR", "VALUE", "VALUES", "VARCHAR", "VARIABLE", "VARYING", "VAR_POP", 
			"VAR_SAMP", "VERBOSE", "VIEW", "VOLATILE", 
			"WHEN", "WHENEVER", "WHERE", "WIDTH_BUCKET", "WINDOW", "WITH", "WITHIN", "WITHOUT", "WORK", "WRITE", 
			"YEAR",
			"ZONE",
			// -- Begin GeoKettle modification --
			"GEOMETRY"
			// -- End GeoKettle modification --
        };
	}
	
    /**
     * @param tableNames The names of the tables to lock
     * @return The SQL commands to lock database tables for write purposes.
     */
    public String getSQLLockTables(String tableNames[])
    {
        String sql="LOCK TABLE ";
        for (int i=0;i<tableNames.length;i++)
        {
            if (i>0) sql+=", ";
            sql+=tableNames[i]+" ";
        }
        sql+="IN ACCESS EXCLUSIVE MODE;"+Const.CR;

        return sql;
    }

    /**
     * @param tableName The name of the table to unlock
     * @return The SQL command to unlock a database table.
     */
    public String getSQLUnlockTables(String tableName[])
    {
        return null; // commit unlocks everything!
    }
    
    /**
     * @return true if the database defaults to naming tables and fields in uppercase.
     * True for most databases except for stubborn stuff like PostgreSQL ;-)
     */
    public boolean isDefaultingToUppercase()
    {
        return false;
    }
    
    public String getExtraOptionsHelpText() 
    {
    	return "http://jdbc.postgresql.org/documentation/83/connect.html#connection-parameters";
    }

    public String[] getUsedLibraries()
    {
    	// -- Begin GeoKettle modification --
        // return new String[] { "postgresql-8.2-506.jdbc3.jar" };
        return new String[] { "postgresql-8.2-506.jdbc3.jar", "postgis_1.3.3.jar" };
    	// -- End GeoKettle modification --
    }
    
    // -- Begin GeoKettle modification --
    
    /*
     * (non-Javadoc)
     * @see org.pentaho.di.core.database.GeodatabaseInterface#convertToJTSGeometry(java.lang.Object)
     */
    public Geometry convertToJTSGeometry(ValueMetaInterface vmi, Object obj, Database db) {
    	// TODO: GeoKettle: optimize conversion by using WKB as intermediate format
    	if (obj instanceof PGgeometry) {
			PGgeometry postgisGeom = (PGgeometry) obj;
			
			// Map PostgreSQL's SRID with the EPSG-SRID or take a custom SRS from WKT
			int postgis_srid = postgisGeom.getGeometry().getSrid();

			SRS epsg_srid = convertToEPSG_SRID(postgis_srid, db.getConnection());
			vmi.setGeometrySRS(epsg_srid);

			WKTReader wktReader = new WKTReader(new GeometryFactory(new PrecisionModel(), epsg_srid.getSRID()));
			
			// PostGIS geometry delivers a WKT similar to "SRID=2000;Point(...)".
			// The SRID must be manually extracted. PGgeometry#splitSRID(..) is a utility.
			String wkt;
			try {
				wkt = PGgeometry.splitSRID(postgisGeom.toString())[1];
			} catch (SQLException e) {
				// No SRID found by splitSRID(..), so the string is already valid WKT
				wkt = postgisGeom.toString();
			} 
			
			Geometry jtsGeom;
			try {
				jtsGeom = wktReader.read(wkt);
			} catch (ParseException e) {
				LOGGER.logError("GeoKettle", "Conversion from PostGIS-geometry failed.");
				jtsGeom = null;
			}
			return jtsGeom;
    	} else {
    		LOGGER.logDetailed("GeoKettle", "No PostGIS-geometry found to convert");
    		return null;
    	}
	}
	
    /*
     * (non-Javadoc)
     * @see org.pentaho.di.core.database.GeodatabaseInterface#convertToObject(com.vividsolutions.jts.geom.Geometry, java.sql.Connection)
     */
	public Object convertToObject(ValueMetaInterface vmi, Geometry geom, Database db) {
		// TODO: GeoKettle: optimize conversion by using WKB as intermediate format.
		PGgeometry postgisGeom;
		try {
			// Map the EPSG- (or custom-) SRID with PostgreSQL's SRID.
			int postgis_srid = convertToDBMS_SRID(vmi.getGeometrySRS(), db.getConnection());
			
			String wkt = PGgeometry.SRIDPREFIX + postgis_srid + ";" + geom.toText();
			postgisGeom = new PGgeometry(PGgeometry.geomFromString(wkt));
		} catch (SQLException e) {
			postgisGeom = null;
			LOGGER.logError("GeoKettle", "Conversion to PostGIS-geometry failed.");
		} 
		return postgisGeom;
	}

	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.core.database.GeodatabaseInterface#convertToDBMS_SRID(org.pentaho.di.core.geospatial.SRS, java.sql.Connection)
	 */
	public int convertToDBMS_SRID(SRS epsg_srid, Connection conn) {
		if (epsg_srid.is_custom) {
			// Try locating an already existing spatial reference system in the
			// spatial_ref_sys table (comapring the WKT-representation).
			try {
				Integer cached_srid = wkt_to_srid_cache.get(epsg_srid.hashCode());
				return (cached_srid == null) ? lookupDBMSSRID(epsg_srid.getCRS().toWKT(), conn) : cached_srid;
			} catch (Exception e) {
				LOGGER.logError("GeoKettle", "Convert to DMBS-SRID failed. Using EPSG-SRID instead!", e);
			}
			
//			// If the looked-up SRID is unknown, add new SRS to the spatial_ref_sys table
//			if (result_srid == SRS.UNKNOWN_SRID) {
//				result_srid = addNewSRStoDBMS(epsg_srid, conn);
//			}
			
		}
		
		return epsg_srid.getSRID();
	}

	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.core.database.GeodatabaseInterface#convertToEPSG_SRID(int, java.sql.Connection)
	 */
	public SRS convertToEPSG_SRID(int dbms_srid, Connection conn) {
		if (dbms_srid == SRS.UNKNOWN_SRID) {
			// Return a valid EPSG SRS
			LOGGER.logRowlevel("GeoKettle", "The read PostGIS-SRID is -1.");
			return SRS.UNKNOWN;
		} else {
			// Try to find the SRS in the cache
			SRS cached_srs = srid_to_srs_cache.get(dbms_srid);
			if (cached_srs != null) {
				LOGGER.logRowlevel("GeoKettle", "The PostGIS-SRID ("+dbms_srid+") was changed to a custom SRID (using cached result).");
				return cached_srs;
			} else {
				String wkt = "";
				try {
					// Lookup the SRS in spatial_ref_sys table and create a new SRS from the WKT.
					String sql = "SELECT srtext FROM spatial_ref_sys WHERE srid = " + dbms_srid;
					Statement statement = conn.createStatement();
					try {
						ResultSet result = statement.executeQuery(sql);
						if (result.next())
							wkt = result.getString(1);
					}
					finally {
						statement.close();
					}
				}
				catch (SQLException e) {
					LOGGER.logError("GeoKettle", "SRID-lookup: SQLException occured while retrieving SRS from SPATIAL_REF_SYS table.");
					return SRS.UNKNOWN;
				}
				if (!Const.isEmpty(wkt)) {
					try {
						SRS wktSRS = new SRS(wkt);
						
						// cache the result
						wkt_to_srid_cache.put(wktSRS.hashCode(), dbms_srid);
						srid_to_srs_cache.put(dbms_srid, wktSRS);
						
						LOGGER.logDetailed("GeoKettle", "The PostGIS-SRID ("+dbms_srid+") was changed to a custom SRID.");
						return wktSRS;
					} catch (KettleStepException e) {
						LOGGER.logError("GeoKettle", "Creation of a custom spatial reference system failed.");
						return SRS.UNKNOWN;
					}
				} else {
					LOGGER.logError("GeoKettle", "The WKT from the PostGIS SPATIAL_REF_SYS table could not be retrieved for "+dbms_srid+".");
					return SRS.UNKNOWN;
				}
			}
		}
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
		String sql = "SELECT srid FROM spatial_ref_sys WHERE srtext = '"+ wkt +"'";
		
		int srid;
		try {
			Statement statement = conn.createStatement();
			try {
				ResultSet result = statement.executeQuery(sql);
				srid = result.next() ? result.getInt(1) : SRS.UNKNOWN_SRID;
				result.close();
			}
			finally {
				statement.close();
			}
		} catch (SQLException e) {
			srid = SRS.UNKNOWN_SRID;
			LOGGER.logError("GeoKettle", "SRID-lookup: The WKT-representation was not found in SPATIAL_REF_SYS table.");
		}
		
		// cache the result
		try {
			SRS wktSRS = new SRS(wkt);
			wkt_to_srid_cache.put(wktSRS.hashCode(), srid);
			srid_to_srs_cache.put(srid, wktSRS);
		} catch (KettleStepException e) { }
		
		return srid;
    }
	
	
//	Unused code for automatic propagation of new spatial reference systems 
//	into the spatial_ref_sys table:
	
	
//	/**
//	 * Adds a new SRS to the DBMS' table where it stores its spatial reference
//	 * system definitions.
//	 * 
//	 * @param srs The {@link SRS} to add.
//	 * @param conn The {@link Connection} to the database.
//	 * @return The SRID of the newly added {@link SRS}.
//	 * @throws SQLException
//	 */
//	private int addNewSRStoDBMS(SRS srs, Connection conn) throws SQLException {
//		// allocate all necessary db connections ressources
//		String sql;
//		int newSRID = findNextAvailableSRID(conn);
//		try {
//			sql = "INSERT INTO spatial_ref_sys (srid, auth_name, auth_srid, proj4text, srtext) " +
//				  "VALUES("+ newSRID +", 'geokettle.org', "+ srs.toSRID() +", '', '"+ srs.getCRS().toWKT() +"')";
//		} catch (Exception e) {
//			e.printStackTrace();
//			return SRS.UNKNOWN_SRID;
//		}
//		Statement statement = conn.createStatement();
//		
//		// execute the query
//		ResultSet result = statement.executeQuery(sql);
//		
//		// close all connections and free ressources
//		result.close();
//		statement.close();
//		
//		return newSRID;
//	}
	
//	/**
//	 * Finds the next available SRID in the DBMS' table where it stores
//	 * its spatial reference definitions.
//	 * 
//	 * @param conn The {@link Connection} to the database.
//	 * @return The next available SRID.
//	 * @throws SQLException
//	 */
//	private int findNextAvailableSRID(Connection conn) throws SQLException {
//		final int PG_MAX_INT = 2147483647;
//		int srid = SRS.UNKNOWN_SRID;
//		
//		// Try MAX(spatial_ref_sys.srid) + 1
//		String sql = "SELECT MAX(srid) FROM spatial_ref_sys";
//		Statement statement = conn.createStatement();
//		ResultSet result = statement.executeQuery(sql);
//		srid = result.next() ? result.getInt(1)+1 : SRS.UNKNOWN_SRID;
//		result.close();
//		
//		if (srid <= SRS.UNKNOWN_SRID || srid >= PG_MAX_INT) {
//			// Try MIN(spatial_ref_sys.srid) - 1
//			sql = "SELECT MIN(srid) FROM spatial_ref_sys";
//			result = statement.executeQuery(sql);
//			srid = result.next() ? result.getInt(1)-1 : SRS.UNKNOWN_SRID;
//			result.close();
//			
//			// Ok...iterate over all srids and find next free one (bad performance)
//			if (srid <= SRS.UNKNOWN_SRID) {
//				for (int newSRID=1; newSRID <= PG_MAX_INT; newSRID++) {
//					sql = "SELECT srid FROM spatial_ref_sys WHERE srid = " + newSRID;
//					result = statement.executeQuery(sql);
//					if (!result.next()) {
//						srid = newSRID;
//						break;
//					}
//					result.close();
//				}
//			}
//		}
//		statement.close();
//		
//		return srid;
//	}
    
    // -- End GeoKettle modification --
}
