------------------------
- Welcome to GeoKettle -
------------------------

Release 2.0

Open source spatial ETL tool for corporate data integration. 


What is GeoKettle?
------------------

GeoKettle is a powerful, metadata-driven Spatial ETL tool dedicated to 
the integration of different spatial (or not) data sources for building 
and updating geospatial files, databases, data warehouses and web services. 
GeoKettle enables the Extraction of data from data sources, the 
Transformation of data in order to correct errors, make some data cleansing, 
change the data structure, make them compliant to defined standards, and 
the Loading of transformed data into a target DataBase Management System 
(DBMS) in OLTP or OLAP/SOLAP mode, GIS file or Geospatial Web Service.

GeoKettle is a spatially-enabled version of the generic ETL tool Kettle 
(Pentaho Data Integration). GeoKettle also benefits from Geospatial 
capabilities from mature, robust and well know Open Source libraries like 
JTS, GeoTools, deegree, OGR and, via a plugin, Sextante.

GeoKettle is particularly useful when a user wants to automate complex and 
repetitive data processing without producing any specific code, to make 
conversions between various data formats, to migrate data from one DBMS to 
another, to perform some data feeding tasks into various DBMS, to populate 
analytical data warehouses for decision support purposes, etc.

This special distribution of Kettle includes extensions which enable the 
use of geospatial (GIS) data. Like Kettle, GeoKettle is released under 
the GNU Lesser General Public License (LGPL) license. 

GeoKettle is a past realization of the GeoSOA research group (headed by 
Prof. Thierry Badard, http://geosoa.scg.ulaval.ca) at the Department of 
Geomatics Sciences of Laval University, Quebec City, Quebec, Canada. It is 
now developed and professionaly supported by Spatialytics 
(http://www.spatialytics.com), a company specialized in GeoBI (Geospatial 
Business Intelligence) software development. 


About GeoKettle versions numbering:
-----------------------------------

Even if the previous version of GeoKettle is numbered 3.2.0-20090609, the 
newest release is 2.0. 

3.2.0 was a reference to the version of Kettle on which GeoKettle was based. 
Current version is an important milestone for the project as it provides an 
important amount of new features, better performance and robustness. The 
previous numbering system did not allow to translate this matter of fact. That 
is why it has been decided to change the numbering of the versions and to name 
the new version as 2.0. It emphasizes more the important work performed to 
provide this new version.

However, it is important to note that versions 2.x will be the last versions of 
GeoKettle based on the Kettle 3.2 code base. Thanks to the tremendous work of 
the Kettle developers, future version of GeoKettle will be more pluggable with 
Kettle and will not be anymore a friendly and spatially enabled fork of Kettle. 
Hence, it will be possible to add spatial extensions provided by GeoKettle to 
any Kettle/PDI 4.x installation.


What's new?
-----------

Since release 3.2.0-20090609:

  Please see details at:
  http://wiki.spatialytics.org/doku.php?id=projects:geokettle:documentation:what_is_new_in_version_2.0

Since release 3.1.0-20081103:

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
  
Since release 2.5.2-20080531:

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
features input/output support for GIS file formats, spatial DBMS and
OGC compliant web services such as SOS, CSW. It also provides 
spatial analysis functions (e.g. topological predicates), scripting 
support (with JavaScript) for Geometry objects and advanced geoprocessing
capabilities.


Using GeoKettle
---------------

GeoKettle can be used the exact same way as Pentaho Data Integration.
Please refer to the PDI user documentation included in this
distribution but also to the wiki dedicated to the GeoKettle project 
(http://wiki.spatialytics.org/doku.php?id=projects:geokettle).

Demo transformations showing the use of the geospatial extensions are 
included in this distribution, in the samples/transformations/geokettle 
directory. 

If you face a bug or want to see a new feature added to GeoKettle, please 
do not hesitate to post a ticket on the bug/issue tracking system available 
at http://trac.spatialytics.com/geokettle.  


Upcoming features
-----------------

A roadmap is available at http://trac.spatialytics.com/geokettle.


License and copyright
---------------------

Like Pentaho Data Integration, GeoKettle is distributed under the GNU 
Lesser General Public License (LGPL). Included libraries (GeoTools, JTS, 
PostGIS driver wrapper) are also LGPL (or a compatible license). Some 
other libraries (JDBC drivers, Oracle SDOAPI) are closed source but 
included in binary form according to their respective end-user licenses. 
Please refer to the included LICENSE.txt file for details. 

The GeoKettle extensions are Copyright (C) 2009- Spatialytics, 
(C) 2007-2009, GeoSOA research group, Department of geomatics sciences, 
Laval University, Quebec, Canada.

Pentaho Data Integration (Kettle) is Copyright (C) 2007-2008, Pentaho
Corporation.


Contact and mailing lists
-------------------------

For future releases and more information, visit us at 
http://www.geokettle.org. 

All comments or questions about GeoKettle are welcome! A forum is available 
at http://www.spatialytics.com/forum. Three sections are dedicated to 
GeoKettle: 

  - users-spatial_etl, for problems, questions and comments about the
    usage of GeoKettle. 
  - dev-spatial_etl, for problems, questions and comments relative
    to development tasks with GeoKettle and for feature request.
  - international_francais, for French users that are not confortable with
    English language. They can ask for help in French in this section.

To subscribe or to sign off the lists, please visit:

http://www.spatialytics.com/forum


How to get involved?
--------------------

There is a lot of work to do on a project like GeoKettle and your help 
will be greatly appreciated. So we would gladly welcome any contribution 
to further development, implementation and feedback on usage of GeoKettle.

Nevertheless, it is often hard for new developers or users to work out 
where they can help. To begin with, we suggest you to subscribe to the 
GeoKettle forums (http://www.spatialytics.com/forum). Listen-in for a while, 
to hear how others make contributions.

You can get your local working copy of the latest code by checking out the 
GeoKettle's svn repository. Review the todo list, choose a task or perhaps 
you have noticed something that needs to be corrected. Make the changes, do 
the testing, generate a patch, and post to the GeoKettle developers forum.

Document writers and translators are usually the most wanted people so if 
you like to help but you're not familiar with the innermost technical details, 
don't worry: we have work for you! ;-)

Contributors to GeoKettle must sign a Contributor License Agreement 
(http://dev.spatialytics.com/cla/contributor_license_agreement.pdf).


Acknowledgments
---------------

We would like to recognize the past contributions to GeoKettle from the 
following organizations and people: 

The NSERC Industrial Research Chair in Geospatial Databases for Decision 
Support (held by Prof. Yvan Bedard,
http://mdspatialdb.chair.scg.ulaval.ca), for partial financial support 
to the research project in which the development of GeoKettle started. 

Professor Stefan Keller of HSR Hochschule fur Technik Rapperswil, 
Switzerland, for involving and co-supervizing two computer science 
students, Pascal Hobus and Sven Goldinger, in the development of 
GeoKettle as part of their bachelors degree final thesis. 
