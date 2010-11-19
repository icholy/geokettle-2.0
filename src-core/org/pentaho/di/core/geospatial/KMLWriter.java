package org.pentaho.di.core.geospatial;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

/**
 * Writes data to a KML (Keyhole Markup File) file.
 * 
 * @author tpelletierdoisy, eleprince, tbadard
 * @since 21-oct-2009
 * 
 */

//TODO Translate all comments in English
public class KMLWriter {
	private LogWriter log;
	private boolean error;
	private java.net.URL kmlURL;
	public File file;
	public Kml kml;
	private Document doc;
	private int namecolumn;
	private boolean testname;
	private int desccolumn;
	private boolean testdesc;
	private int geomcolumn;

	public KMLWriter(java.net.URL fileURL) {
		this.log = LogWriter.getInstance();
		this.kmlURL = fileURL;
		String path1 = kmlURL.toString();
		String path2 = path1.replace("file:///", "");
		//this.file = new File(path2);
		this.file = new File(kmlURL.getPath());
		error = false;
		
		this.kml = KmlFactory.createKml();
		this.doc = kml.createAndSetDocument();
	
		this.testdesc=false;
		this.testname=false;
	}

	public void open() throws KettleException {
		try {

			// try closing first
			close();

			// TODO: detect file type and instanciate the right type of
			// DataStore
			// implementation (to support file formats other than Shapefile)

			// TODO: make charset configurable (in the step dialog box?)
			if (!kmlURL.toString().substring(kmlURL.toString().length() - 3,
					kmlURL.toString().length()).equalsIgnoreCase("KML")) {
				// TODO: internationalize error message
				throw new KettleException(
						"The output specified is not in kml file format (.kml)");
			}

		} catch (Exception e) {
			throw new KettleException("Error opening KML file at URL: "
					+ kmlURL, e);
		}
	}
//en fonction de la geometrie JTS en entree cette metode cree un placemark contenant la geometrie JAK correspondante.
	public void createNewPlacemark(Object[] r) throws FileNotFoundException {
		//extraction de la geometrie JTS
		com.vividsolutions.jts.geom.Geometry geomJTS = (Geometry) r[geomcolumn];
		//creation du placemark dans un document
		Placemark pl = doc.createAndAddPlacemark();
		//si il existe des champ name et desciption on attribut leur valeur  au placemark
		if(testname==true)pl.withName((String) r[namecolumn]);
		if(testdesc==true)pl.withDescription((String) r[desccolumn]);
		
		//si la geometrie JTS est un point
		if (geomJTS instanceof com.vividsolutions.jts.geom.Point) {
			com.vividsolutions.jts.geom.Point ptJTS = (com.vividsolutions.jts.geom.Point) geomJTS;
		//on extrait ses coordonnees que l'on change en coordionnees JAK
			List<de.micromata.opengis.kml.v_2_2_0.Coordinate> coord = this
					.changerCoordinates(ptJTS.getCoordinate());
			//on cree un point JAK que l'on place dans le placemark
			pl.createAndSetPoint().withCoordinates(coord);

			//si la geometrie est une ligne
		} else if (geomJTS instanceof com.vividsolutions.jts.geom.LineString) {
			com.vividsolutions.jts.geom.LineString lsJTS = (com.vividsolutions.jts.geom.LineString) geomJTS;
			List<de.micromata.opengis.kml.v_2_2_0.Coordinate> coord = this
					.changerCoordinates(lsJTS.getCoordinates());

			pl.createAndSetLineString().withCoordinates(coord);

			//si la geometrie est un anneau
		} else if (geomJTS instanceof com.vividsolutions.jts.geom.LinearRing) {
			com.vividsolutions.jts.geom.LinearRing lrJTS = (com.vividsolutions.jts.geom.LinearRing) geomJTS;
			List<de.micromata.opengis.kml.v_2_2_0.Coordinate> coord = this
					.changerCoordinates(lrJTS.getCoordinates());

			pl.createAndSetLinearRing().withCoordinates(coord);

			//si la geometrie est un polygone (on passe par les anneaux qui le constituent)
		} else if (geomJTS instanceof com.vividsolutions.jts.geom.Polygon) {
			com.vividsolutions.jts.geom.Polygon polyJTS = (com.vividsolutions.jts.geom.Polygon) geomJTS;
			com.vividsolutions.jts.geom.LineString extlsJTS = polyJTS
					.getExteriorRing();
			com.vividsolutions.jts.geom.LinearRing extlrJTS = this
					.changerLineStringenLineRing(extlsJTS);
			de.micromata.opengis.kml.v_2_2_0.LinearRing extlrJAK = KmlFactory
					.createLinearRing().withCoordinates(
							this.changerCoordinates(extlrJTS.getCoordinates()));

			de.micromata.opengis.kml.v_2_2_0.Polygon polyJAK = pl
					.createAndSetPolygon();
			polyJAK.createAndSetOuterBoundaryIs().withLinearRing(extlrJAK);
			int noir = polyJTS.getNumInteriorRing();
			for (int i = 0; i < noir; i++) {
				com.vividsolutions.jts.geom.LineString intlsJTS = polyJTS
						.getInteriorRingN(i);
				com.vividsolutions.jts.geom.LinearRing intlrJTS = this
						.changerLineStringenLineRing(intlsJTS);
				de.micromata.opengis.kml.v_2_2_0.LinearRing intlrJAK = KmlFactory
						.createLinearRing().withCoordinates(
								this.changerCoordinates(intlrJTS
										.getCoordinates()));
				polyJAK.createAndAddInnerBoundaryIs().withLinearRing(intlrJAK);

			}
		}

		//si la geometrie est une collection de geometries
		else if (geomJTS instanceof com.vividsolutions.jts.geom.GeometryCollection) {
			com.vividsolutions.jts.geom.GeometryCollection gc = (GeometryCollection) geomJTS;

			MultiGeometry mg = pl.createAndSetMultiGeometry();

			int nog = gc.getNumGeometries();
			for (int j = 0; j < nog; j++) {
				Geometry gJTS = gc.getGeometryN(j);
				if (gJTS instanceof Point) {
					com.vividsolutions.jts.geom.Point ptJTS = (com.vividsolutions.jts.geom.Point) gJTS;
					List<de.micromata.opengis.kml.v_2_2_0.Coordinate> coord = this
							.changerCoordinates(ptJTS.getCoordinate());
					mg.createAndAddPoint().withCoordinates(coord);
				} else if (gJTS instanceof com.vividsolutions.jts.geom.LineString) {
					com.vividsolutions.jts.geom.LineString lsJTS = (com.vividsolutions.jts.geom.LineString) gJTS;
					List<de.micromata.opengis.kml.v_2_2_0.Coordinate> coord = this
							.changerCoordinates(lsJTS.getCoordinates());
					mg.createAndAddLineString().withCoordinates(coord);
				} else if (gJTS instanceof com.vividsolutions.jts.geom.LinearRing) {
					com.vividsolutions.jts.geom.LinearRing lrJTS = (com.vividsolutions.jts.geom.LinearRing) gJTS;
					List<de.micromata.opengis.kml.v_2_2_0.Coordinate> coord = this
							.changerCoordinates(lrJTS.getCoordinates());
					mg.createAndAddLinearRing().withCoordinates(coord);
				} else if (gJTS instanceof com.vividsolutions.jts.geom.Polygon) {
					com.vividsolutions.jts.geom.Polygon polyJTS = (com.vividsolutions.jts.geom.Polygon) gJTS;
					com.vividsolutions.jts.geom.LineString extlsJTS = polyJTS
							.getExteriorRing();
					com.vividsolutions.jts.geom.LinearRing extlrJTS = this
							.changerLineStringenLineRing(extlsJTS);
					de.micromata.opengis.kml.v_2_2_0.LinearRing extlrJAK = KmlFactory
							.createLinearRing().withCoordinates(
									this.changerCoordinates(extlrJTS
											.getCoordinates()));

					de.micromata.opengis.kml.v_2_2_0.Polygon polyJAK = mg
							.createAndAddPolygon();
					polyJAK.createAndSetOuterBoundaryIs().withLinearRing(
							extlrJAK);
					int noir = polyJTS.getNumInteriorRing();
					for (int l = 0; l < noir; l++) {
						com.vividsolutions.jts.geom.LineString intlsJTS = polyJTS
								.getInteriorRingN(l);
						com.vividsolutions.jts.geom.LinearRing intlrJTS = this
								.changerLineStringenLineRing(intlsJTS);
						de.micromata.opengis.kml.v_2_2_0.LinearRing intlrJAK = KmlFactory
								.createLinearRing().withCoordinates(
										this.changerCoordinates(intlrJTS
												.getCoordinates()));
						polyJAK.createAndAddInnerBoundaryIs().withLinearRing(
								intlrJAK);

					}
				}

			}
		}
		

	}

	//fonction speciale compte tenu des methode de JTS qui ne retourne pas les anneaux des polygones mais plutot les lignes correspondantes qu'il nous faut passer en anneau
	private LinearRing changerLineStringenLineRing(LineString ls) {
		GeometryFactory JTSFactory = new GeometryFactory();
		LinearRing lr;
		com.vividsolutions.jts.geom.Coordinate[] coordls = ls.getCoordinates();
		com.vividsolutions.jts.geom.Coordinate[] coordlr = new com.vividsolutions.jts.geom.Coordinate[coordls.length];
		for (int i = 0; i < coordls.length; i++) {
			coordlr[i] = coordls[i];
		}

		lr = JTSFactory.createLinearRing(coordlr);
		// TODO Auto-generated method stub
		return lr;
	}
	
	//methode de changement de coordonee de JTS a JAK
	private List<Coordinate> changerCoordinates(
			com.vividsolutions.jts.geom.Coordinate[] coord) {
		List<Coordinate> list = new ArrayList<Coordinate>();
		for (int i = 0; i < coord.length; i++) {
			Coordinate c = KmlFactory.createCoordinate(0, 0, 0);
			c.setLongitude(coord[i].x);
			c.setLatitude(coord[i].y);
			c.setAltitude(coord[i].z);
			list.add(c);
		}
		return list;
	}
	
	//idem
	private List<Coordinate> changerCoordinates(
			com.vividsolutions.jts.geom.Coordinate coord) {
		List<Coordinate> list = new ArrayList<Coordinate>();
		Coordinate c = KmlFactory.createCoordinate(0, 0, 0);
		c.setLongitude(coord.x);
		c.setLatitude(coord.y);
		c.setAltitude(coord.z);
		list.add(c);
		return list;
	}

	//verifie la presence d'un champ de type geometry
	public boolean checkKmlPossibility(RowMetaInterface fields) {
		boolean test = false;
		for (int i = 0; i < fields.size(); i++) {
			ValueMetaInterface value = fields.getValueMeta(i);
			if (value.getType() == ValueMeta.TYPE_GEOMETRY) {
				test = true;
			}
		}
		return test;
	}
	
	//verifie qu'il y a bien un chmap de type geometrie
	//et repere la position des champs interessants
	public void visionTable(RowMetaInterface fields) throws KettleException {
		boolean testgeom = this.checkKmlPossibility(fields);

		if (testgeom == true) {
			for (int i = 0; i < fields.size(); i++) {
				ValueMetaInterface value = fields.getValueMeta(i);
				if (value.getType() == ValueMeta.TYPE_GEOMETRY) {
					geomcolumn = i;
				}
			}
		} else
			throw new KettleException(
					"There is no geometric attribute, so there is no possibility to create a Kml file");
		for (int i = 0; i < fields.size(); i++) {
			ValueMetaInterface value = fields.getValueMeta(i);

			if (value.getType() == ValueMeta.TYPE_STRING
					&& value.getName() == "name") {
				namecolumn = i; testname=true;
			}
			if (value.getType() == ValueMeta.TYPE_STRING
					&& value.getName() == "description") {
				desccolumn = i;testdesc=true;
			}

		}
		

	}

	
	public void write() throws KettleException {
		try {

		} catch (Exception e) {
			throw new KettleException("An error has occured", e);
		}
	}

	public boolean close() {
		boolean retval = false;
		try {
		} catch (Exception e) {
			log.logError(toString(), "Couldn't close iterator for datastore ["
					+ kmlURL + "] : " + e.toString());
			error = true;
		}

		return retval;
	}

	public boolean hasError() {
		return error;
	}

	public String getVersionInfo() {
		// return reader.getHeader().getSignatureDesc();
		return null;
	}
}
