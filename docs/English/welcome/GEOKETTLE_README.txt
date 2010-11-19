------------------------
- Welcome to GeoKettle -
------------------------

Release 3.2.0-20090608 (based on Kettle 3.2.0-stable)

A Spatially-Enabled version of Pentaho Data Integration (Kettle). 

NOTE: This release is a beta experimental version of GeoKettle. Some 
functionalities are not complete or fully tested. It is not recommended 
for use in a production environment. By using this software, you 
acknowledge and assume all the possible risks involved. The authors are 
not responsible of any consequences or losses related to the use of this 
software. 


What is GeoKettle?
------------------

GeoKettle is a "spatially-enabled" version of Pentaho Data Integration 
(Kettle). Pentaho Data Integration (Kettle) is a powerful, 
metadata-driven ETL (Extract, Transform and Load) tool dedicated to the 
integration of different data sources for building data warehouses. It 
is part of the open source BI (Business Intelligence) software suite 
designed by Pentaho. 

This special distribution of Kettle includes extensions which enable the 
use of geospatial (GIS) data. Like Kettle, GeoKettle is released under 
the GNU Lesser General Public License (LGPL) license. 

GeoKettle is a realization of the GeoSOA research group (headed by Prof. 
Thierry Badard, http://geosoa.scg.ulaval.ca) at the Department of 
Geomatics Sciences of Laval University, Quebec City, Quebec, Canada.

The GeoKettle development team is composed of:

  - Project coordinator: Thierry Badard 
  - Lead developer: Etienne Dube 
  - Developers: Pascal Hobus, Sven Goldinger, Jean Mathieu, Mamadou 
    Ouattara
  - Contributors: Mathieu Bertrand


What's new?
-----------

Since release 3.1.0-20081103 :

 - The GeoKettle extensions were ported to the new Pentaho Data
   Integration (PDI) version 3.2.0-stable. As such, this release of GeoKettle
   includes all the improvements from the new PDI version.

 - Added a "GIS File Output" step. At present, this step supports the
   writing of Shapefiles.

 - Added support for Spatial Reference Systems (SRS). SRS metadata was
   added to ValueMeta for Geometry fields. Steps allowing to set a
   SRS ("Set SRS") and transform coordinates (reproject) of geometries
   from one SRS to another ("SRS Transformation") have also been
   developed. The SRS support is based on GeoTools' implementation of
   coordinate reference systems (org.opengis.referencing package).

 - In line with SRS support, SRS metadata for PostGIS and Oracle Spatial
   DBMS is retrieved and written when reading/writing geometry columns.
   To conform to integrity constraints when writing to a geometry column,
   one must ensure that the SRS in GeoKettle matches the one defined for
   the geometry column in the DBMS.
   
 - Reading and writing SRS metadata (in the form of .PRJ files containing
   WKT definitions of SRS) is also supported when reading/writing
   Shapefiles in the GIS File Input/Output steps.
   
 - Updated GeoTools libraries to version 2.5.5 and JTS to version 1.10.
  
Since release 2.5.2-20080531 :

 - The GeoKettle extensions were ported to the new Pentaho Data
   Integration (PDI) version 3.1.0-GA. As such, this release of GeoKettle
   includes all the improvements from the new PDI version.

 - Changed the core Geometry object framework from GeOxygene to the JTS
   Topology Suite (JTS).

 - Added native support for Oracle Spatial and MySQL geospatial DBMS.

 - Speed improvements: due to the upgrade of PDI core and to the JTS
   library, GeoKettle now offers better throughput. We measured a typical
   speedup of row throughput between 15% and 60% (depending of the
   transformation) when using geospatial data.


What is geospatial data?
------------------------

Geospatial data is used to locate geographic features on a map. It is 
used mainly in Geographic Information Systems (GIS) to create maps and 
perform spatial analysis. Geospatial data can be classified in two main 
categories: raster data, which is composed of bitmap images covering an 
area on the surface of the earth (e.g. satellite or aerial imagery) and 
vector data, in which individual features are represented by 
vector-based geometric primitives (such as points, lines and polygons). 
For example, a road can be represented as a series of line segments 
(what is often called a "LineString") on a map. 

You may have to deal with geospatial data if your organization uses GIS 
software (e.g. ESRI ArcGIS or MapInfo) or has to handle spatial data in 
one or another GIS file format (e.g. Shapefile), XML encoding (e.g. GML, 
KML) or spatial DBMS (e.g. PostGIS, Oracle Spatial). In an ETL 
perspective, you may want to automate the transformation and loading of 
geospatial data from heterogeneous sources to a database. And 
increasingly, business intelligence applications rely on geospatial 
data, to enhance the user experience (e.g. map displays, end-user 
software such as Spatial OLAP) and provide location-aware analysis 
functionalities. This exposes a need for Spatially-enabled ETL tools, 
supporting the extraction of geospatial data from various sources, 
transformation of this data (including spatial analysis functions 
handling the geometry of geographic features) and loading to a 
spatially-enabled data warehouse. 
 
GeoKettle aims to fulfill these requirements. It offers the full range 
of functionality of Pentaho Data Integration (Kettle), and extends it 
with a new "Geometry" data type for geospatial vector data. It also 
features input/output support for GIS file formats and spatial DBMS, 
spatial analysis functions (e.g. topological predicates) and scripting 
support (with JavaScript) for Geometry objects. 


Using GeoKettle
---------------

GeoKettle can be used the exact same way as Pentaho Data Integration.
Please refer to the PDI user documentation included in this
distribution.

Demo transformations showing the use of the geospatial
extensions are included in this distribution, in the
samples/transformations/geokettle directory. The new features for
geospatial data are documented here:

- Geometry field type:

  In addition to the basic field types (e.g. Number, String, Date, ...) 
  existing in PDI, GeoKettle introduces a new Geometry type, supporting 
  vector geospatial data (geometries such as Point, Line and Polygon). A 
  Geometry field is automatically generated when reading a geometry column 
  from a supported spatial DBMS or GIS file format. For example, when 
  using the "Table input" step with a table (from a PostgreSQL DBMS with 
  the PostGIS extensions installed) containing a column with a GEOMETRY 
  type, the corresponding Geometry field will be part of the output for 
  this step.
  
  The capability to convert a Well-Known Text (WKT) string to a Geometry 
  is also supported: changing the value type of a String to Geometry (with 
  the "Select values" step) will yield a valid Geometry object if the 
  string contents is a valid WKT string. Conversion from Geometry to 
  String does the opposite (it converts the geometry to a WKT string). 
  Well-Known Binary (WKB) conversion is also implemented: converting a 
  Geometry field to Binary will yield the WKB representation of the 
  geometry (in a byte[] array); when doing the inverse (Binary field to 
  Geometry), GeoKettle will try to parse the binary value, and if it is 
  valid WKB it will yield a valid Geometry object as a result. Internal 
  byte order of WKB values is big endian (the same as the default byte 
  order in Java). 

  The Geometry field type is implemented using objects from the
  JTS Topology Suite API (http://www.vividsolutions.com/jts/). All
  geometry objects are represented with the Geometry abstract class.
  
  
- Access to Geometry objects in JavaScript:

  It is possible to access the JTS objects contained in Geometry fields 
  in the "Modified Java Script Value" step. This makes possible the use of 
  spatial analysis functions such as buffer calculations, overlays, metric 
  operators, etc. An example transformation using Geometry fields in 
  JavaScript is included in the distribution. 


- Input / output with supported spatial DBMS:

  This version of GeoKettle natively supports the geospatial data types 
  in PostGIS (for PostgreSQL), Oracle Spatial (or Locator) and MySQL. 
  
  PostGIS support is assured by the PostGIS JDBC driver wrapper 
  (postgis.jar), which is included in the distribution. The PostgreSQL 
  native (JDBC) connection type is used when creating the database 
  connection. If the database in question is configured with the PostGIS 
  extension, all GEOMETRY columns will transparently be read as Geometry 
  values (no need to use PostGIS' AsText() or AsBinary() geometry 
  accessors), whether one uses the "Table input", "Database lookup" or 
  "Database join" steps. Likewise, Geometry values will be transparently 
  converted to the native DBMS geometry type when written to a GEOMETRY 
  column (in any database output step, such as "Table output", "Insert / 
  Update" or "Dimension lookup/update"). 

  The same applies to Oracle Spatial (or Locator) and MySQL; please use 
  Oracle or MySQL as the connection type, with JDBC access. Geometry 
  columns will be handled transparently by GeoKettle, both for reading
  and writing.

  
- Input / output with unsupported spatial DBMS:
  
  Spatial DBMS which are not natively supported (e.g. Microsoft SQL Server
  2008 GEOMETRY type, IBM DB2 Spatial Extender, Ingres) can still be used
  with GeoKettle. Reading a geometry column can be done using the WKT
  accessor functions in SQL (e.g. ST_AsText() for OGC SFS compliant
  DBMS) in the Table Input (or similar) step. The WKT String can then be
  converted to a Geometry field using the "Select Values" step.
  
  Writing to a geometry column is more complicated: the normal "Table 
  output", "Insert / Update" or "Update" steps cannot be used for writing 
  WKT geometries because the DBMS expects a value of geometry type for 
  these colums, not a character string. Instead, after converting the 
  Geometry field to String in a "Select values" step, it is possible to 
  insert or update tables containing a geometry column using the "Execute 
  SQL script" step. The DBMS' geometry constructor function can be used to 
  instanciate a geometry value from the WKT string (passed as a parameter 
  in the INSERT or UPDATE statement). For example, a SQL statement for 
  inserting WKT geometries in a Microsoft SQL Server 2008 spatial table 
  could be: 
  
  INSERT INTO geomtable (the_geom)
    VALUES (geometry::STGeomFromText('?', 1))
  
  where the_geom is the name of the geometry column, and
  geometry::STGeomFromText() is the geometry constructor function in the
  DBMS. Be sure to check the "Execute for each row" option and to enter
  all the needed input fields in the Parameters list. Unfortunately,
  when using this method, write throughput is not as high as when using
  the "Table output" step (with supported spatial DBMS only), since batch
  inserts cannot be used this way.
  
  
- Topological predicates:

  Kettle conditions (org.pentaho.di.core.Condition class) have been
  extended with topological predicate functions, allowing the
  comparison of Geometry fields based on topological relationships. The
  new functions are: GIS_INTERSECTS, GIS_EQUALS, GIS_CONTAINS,
  GIS_CROSSES, GIS_DISJOINT, GIS_WITHIN, GIS_OVERLAPS, GIS_TOUCHES and
  GIS_ISVALID. All of them are binary predicates (comparing one field
  to another) except for GIS_ISVALID which is unary (returns true or
  false based on the validity of the geometry in a single field). For
  example, if we want to know if values for a certain "City" field are
  located within values from a "State" field, we would use the
  GIS_WITHIN predicate. If the City point is located within the State
  polygon for the current row, the expression evaluates to true
  (otherwise, false). These new topological predicates can be used in
  any step based on the Condition class, i.e. "Filter rows" and "Join
  rows (cartesian product)". A demo transformation (intersection.ktr)
  is included in the distribution.

- "GIS file input" step:

  A new "GIS file input" step is present in the Geospatial steps.
  This supports the reading of GIS data files; for now only Shapefiles
  are supported. The geometry (contained in the SHP file) is read to a
  field named "the_geom" (with a Geometry field type) and all other
  alphanumerical fields (contained in the DBF file) are read to fields
  with the corresponding name and value type. Unlike the existing
  "ESRI Shapefile Reader" step from PDI, which reads geometries
  contained in the Shapefile as X and Y numeric fields representing the
  coordinates of points, this new "GIS file input" step reads
  geometries as real Geometry objects.


Upcoming features
-----------------

The following features are not yet supported in GeoKettle, but are
planned for future releases:

- Implementation of data matching and conflation steps in order to allow
  geometric data cleansing and comparison of geospatial datasets (using
  the JCS Conflation Suite; http://www.vividsolutions.com/JCS/).
  (This feature is currently in development and is planned for the next
  release.)

- Visual preview of Geometry values (an alternative Preview dialog
  which displays Geometry fields in a map viewer, instead of as WKT
  strings like in the current Preview dialog).
  (This feature is currently in development and is planned for the next
  release.)
  
- Read/write support for other GIS file formats (e.g. MapInfo TAB, GML, ...).
  (This feature is currently in development and is planned for the next
  release.)

- Implementation of the "Spatial Analysis" step, providing for user-
  friendly spatial analysis functions (buffers, overlays, etc.).


License and copyright
---------------------

Like Pentaho Data Integration, GeoKettle is distributed under the GNU 
Lesser General Public License (LGPL). Included libraries (GeoTools, JTS, 
PostGIS driver wrapper) are also LGPL (or a compatible license). Some 
other libraries (JDBC drivers, Oracle SDOAPI) are closed source but 
included in binary form according to their respective end-user licenses. 
Please refer to the included LICENSE.txt file for details. 

The GeoKettle extensions are Copyright (C) 2007-2009, GeoSOA research
group, Department of geomatics sciences, Laval University, Quebec,
Canada.

Pentaho Data Integration (Kettle) is Copyright (C) 2007-2008, Pentaho
Corporation.


Contact and mailing lists
-------------------------

For future releases and more information, visit us at 
http://www.geokettle.org. 

All comments or questions about GeoKettle are welcome! Two mailing lists 
are available: 

  - geokettle-users, for problems, questions and comments about the
    usage of GeoKettle. 
  - geokettle-devel, for problems, questions and comments relative
    to development tasks with GeoKettle and for feature request.

To subscribe or to sign off the lists, please visit:

https://lists.sourceforge.net/lists/listinfo/geokettle-users
or
https://lists.sourceforge.net/lists/listinfo/geokettle-devel


How to get involved?
--------------------

There is a lot of work to do on a project like GeoKettle and your help 
will be greatly appreciated. So we gladly welcome any contribution to 
further development, implementation and feedback on usage of GeoKettle. 

Nevertheless, it is often hard for new developers or users to work out 
where they can help. To begin with, we suggest you to subscribe to the 
mailing lists. Listen in for a while, to learn how others make 
contributions. 

You can get your local working copy of the latest code. Review the todo 
list, choose a task or perhaps you have noticed something that needs to 
be corrected. Make the changes, do the testing, generate a patch, and 
post to the devel mailing list. 

Document writers and translators are usually the most wanted people so 
if you like to help but you're not familiar with the innermost technical 
details, don't worry: we have work for you! 


Acknowledgments
---------------

We would like to recognize the contributions to GeoKettle from the 
following organizations and people: 

The NSERC Industrial Research Chair in Geospatial Databases for Decision 
Support (held by Prof. Yvan Bedard,
http://mdspatialdb.chair.scg.ulaval.ca), for partial financial support 
to the research project in which the development of GeoKettle started. 

Professor Stefan Keller of HSR Hochschule fur Technik Rapperswil, 
Switzerland, for involving and co-supervizing two computer science 
students, Pascal Hobus and Sven Goldinger, in the development of 
GeoKettle as part of their bachelors degree final thesis. 
