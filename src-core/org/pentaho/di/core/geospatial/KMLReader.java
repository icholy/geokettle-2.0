package org.pentaho.di.core.geospatial;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.vividsolutions.jts.geom.GeometryFactory;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Container;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;

/**
 * Reads data from a KML (Keyhole Markup File) file.
 * 
 * @author tpelletierdoisy, eleprince, tbadard
 * @since 14-sep-2009
 */

//TODO Translate all comments in English
public class KMLReader {

	private Kml kml;// Objet Kml de JAK
	//private File file;
	private LogWriter log;
	private java.net.URL kmlURL;
	private boolean error;

	// Liste permettant de stocker tous les placemark receuillis et a traiter
	// pour en extraire les geometries JAK
	private List<Placemark> place = new ArrayList<Placemark>();

	// Tableaux et listes permettant de stocker les elements a retourner au
	// rangees de la BDD
	// -->

	// Tableau contenant les geometries JTS obtenues
	private com.vividsolutions.jts.geom.Geometry[] geomlistJTS = new com.vividsolutions.jts.geom.Geometry[0];
	// Listes contenant des informations de type String associees a chaque
	// placemark
	private List<String> geomname = new ArrayList<String>();// pour son champ
	// name
	private List<String> geomdesc = new ArrayList<String>();// pour son champ
	// description
	// <--
	// Objet de la librairie JAK permettant la creation des elements du KML
	private KmlFactory KmlFactory= new KmlFactory();
	// Objet de la librairie JTS permettant la creation des geometries JTS
	private GeometryFactory JTSFactory = new GeometryFactory();

	private int k;// iterateur sur la rangee a ecrire

	// constructeur d'un objet de type KMLReader
	public KMLReader(java.net.URL fileURL) {
		this.log = LogWriter.getInstance();
		this.kmlURL = fileURL; // URL permettant l'ouverture du fichier kml
		this.kml = KmlFactory.createKml();// creation d'un objet de type kml
		// destine a stocker le fichier kml
		this.k = 0;// initialisation de l'iterateur de rangee a 0

	}

	// Methode permettant l'ouverture d'un fichier kml, le stockage de ses
	// informations et leur traitements pour obtenir une liste de geometrie de
	// type JTS
	// METHODE DE BASE
	public void open() throws KettleException {
		try {
			String path1 = kmlURL.toString();
			String path2 = path1.replace("file:///", "");
			//File file = new File(path2);
			File file = new File(kmlURL.getPath());

			kml = kml.unmarshal(file,true);// Etapes permettant la lecture d'un
			// fichier kml (XML) et son stockage
			// sous la forme d'un objet Kml
			// unmarshal(file):METHODE DE JAK

			// appel de la fonction recolterGeometryJTS qui va recolter toutes
			// les geometries JAK presentes dans kml et les transformer en
			// geometries JTS lisibles par GeooKettle et stockees dans un
			// tableau:geomlistJTS
			// NB: la difference entre geometrie JAK et JTS vient du format des
			// deux objets disctincts mais les geometries obeissent aux memes
			// regles
			// Ex: un polygone qqs son format est forme d'un anneau externe et
			// de possibles anneaux internes
			geomlistJTS = this.recolterGeometryJTS(kml);

		} catch (Exception e) {
			throw new KettleException("Error opening KML file at URL: "
					+ kmlURL, e);
		}
	}

	public com.vividsolutions.jts.geom.Geometry[] recolterGeometryJTS(Kml kml)
			throws KettleException {

		// getFeature():methode de JAK qui permet d'extraire de l'objet kml ses
		// features qui peuvent etre
		// soit un placemark (qui contient une et une seule geometrie)
		// soit un container:
		// -de type document (contient un ou plusieurs placemarks)
		// -de type folder (peut contenir differents placemarks ou document)
		Feature feature = kml.getFeature();

		if (feature instanceof de.micromata.opengis.kml.v_2_2_0.Container) {
			Container cont = (Container) feature;
			this.parcourirContainer(cont);// traitement des features de type
			// container:
			// parcourirContainer() doit extraire tous les placemarks
			// et les placer dans la liste place
		}

		// en cas de placemark unique
		else if (feature instanceof de.micromata.opengis.kml.v_2_2_0.Placemark) {
			Placemark p = (Placemark) feature;
			place.add(p);// stockage des placemark dans la liste place
		}

		com.vividsolutions.jts.geom.Geometry[] geomlist = new com.vividsolutions.jts.geom.Geometry[place
				.size()];// tableau de geometrie JTS

		// --> traitement de chaque placemark de place
		int j = 0;
		for (Placemark p : place) {// boucle for sur chaque placemark de la
			// liste

			geomname.add(p.getName());// getName()(methode de JAK) permet
			// d'extraire de chaque
			// placemark le champ name et le stocke
			// dans la liste geomname

			geomdesc.add(p.getDescription());// getName() (methode de JAK)
			// permet d'extraire d'un
			// placemark le champ description et le stocke
			// dans la liste geomdesc

			Geometry g = p.getGeometry();// getGeometry() (methode de JAK)
			// permet d'un placemark sa
			// geometrie JAK associee

			geomlist[j] = this.gestionGeometryJAK(g);// gestionGeometryJAK()
			// permet de traiter la
			// geometrie JAK selon
			// son type(point,
			// polygon ...) pour la
			// transformer en
			// geometrie JTS
			j++;
		}
		// <---

		return geomlist;// retourne geomlist dans geomlistJTS
	}

	// Traitement des features de type container: parcourirContainer() doit
	// extraire tous les placemarks et les placer dans la liste place
	public void parcourirContainer(Container cont) {
		// si le container est un folder
		if (cont instanceof de.micromata.opengis.kml.v_2_2_0.Folder) {
			Folder fold = (Folder) cont;
			this.parcourirFolder(fold);// appel de la methode parcourirFolder()
		}
		// si le container est un document
		if (cont instanceof de.micromata.opengis.kml.v_2_2_0.Document) {
			Document doc = (Document) cont;
			this.parcourirDocument(doc);// appel de la methode
			// parcourirDocument()
		}
	}

	// Traitement des container de type Document (rappel: ils possedent un ou
	// plusieurs palcemark)
	public void parcourirDocument(Document doc) {
		List<Feature> listdoc = doc.getFeature();// getFeature()(methode JAK)
		// associe a un objet
		// document permet d'en
		// extraire tous les
		// feature retournes dans
		// une liste

		// Et en parcourant cette liste, si les features correspondent a un
		// placemark, on les place dans la liste de placemark a traiter
		for (Feature f : listdoc) {
			if (f != null) {
				if (f instanceof Placemark) {
					Placemark pl = (Placemark) f;
					place.add(pl);
				}
				if (f instanceof Folder) {
					Folder p = (Folder) f;
					this.parcourirFolder(p);
				}
				if(f instanceof Document){Document p = (Document) f;
				this.parcourirDocument(p);}
			}

		}

	}

	// Traitement des container de type Folder (rappel: ils possedent un ou
	// plusieurs placemark ou container)
	public void parcourirFolder(Folder fold) {
		List<Feature> listfold = fold.getFeature();// getFeature()(methode JAK)
		// associe a un objet
		// folder permet d'en
		// extraire tous les
		// feature retournes dans
		// une liste

		// Et en parcourant celle ci
		for (Feature f : listfold) {
			if (f != null) {
				if (f instanceof Placemark) {// si le feature est un placemark
					// il est place dans la liste
					// des placemark
					Placemark pl = (Placemark) f;
					place.add(pl);}

					if (f instanceof Document) {// si le feature est un document
						// on traite le document grace a
						// la methode precedente
						Document doc = (Document) f;
						this.parcourirDocument(doc);
					}

					if (f instanceof Folder) {// et si le feature est un folder
						// on reappelle cette methode
						Folder doc = (Folder) f;
						this.parcourirFolder(doc);
					}
				
			}
		}
	}

	// Traitement de chaque geometry JAK pour en ressortir la geometrie JTS
	// correpondante
	// Quelque soit la geometrie etudiee le principe est le meme, il faut
	// revenir aux coordonnees des points definissant la dite geometrie JAK,
	// attribuer les valeurs de chacun de ces coordonnees aux coordonnees de
	// type JTS, et grace a ces dernieres stockees dans un tableau et au
	// constructeur d'objet JTS: GeometryFactory JTSFactory, on construit
	// des geometries JAK.
	public com.vividsolutions.jts.geom.Geometry gestionGeometryJAK(Geometry g) {

		// En cas ou la geometrie JAK est un point
		if (g instanceof de.micromata.opengis.kml.v_2_2_0.Point) {

			Point pt = (Point) g;
			List<de.micromata.opengis.kml.v_2_2_0.Coordinate> c = pt
					.getCoordinates();// getCoordinates()(methode JAK)recolte
			// des coordonnees du point stockes dans
			// une liste c(pour le point il n'y a
			// qu'une coordonnee)

			com.vividsolutions.jts.geom.Coordinate[] d = this
					.changementCoord(c);// appel de la methode permettant la
			// creation de coordonnees JTS stockees
			// dans le tableau d

			com.vividsolutions.jts.geom.Point newpt = JTSFactory
					.createPoint(d[0]);// createPoint() (methode JTS associe au
			// GeometryFactory) permet la creation
			// d'un point JTS et ne prend donc en
			// entree qu'un seul objet coordonnee
			// (JTS)
			return newpt;

			// En cas ou la geometrie JAK est une ligne
		} else if (g instanceof de.micromata.opengis.kml.v_2_2_0.LineString) {

			LineString ls = (LineString) g;

			List<de.micromata.opengis.kml.v_2_2_0.Coordinate> c = ls
					.getCoordinates();// getCoordinates()(methode JAK)recolte
			// des coordonnees de la ligne stockes dans
			// une liste c

			com.vividsolutions.jts.geom.Coordinate[] d = this
					.changementCoord(c);// appel de la methode permettant la
			// creation de coordonnees JTS stockees
			// dans le tableau d

			com.vividsolutions.jts.geom.LineString newls = JTSFactory
					.createLineString(d);// createLineString() (methode JTS
			// associe au
			// GeometryFactory) permet la creation
			// d'une ligne JTS et accepte en entree un tableau de coordonnee
			// (JTS)

			return newls;

			// En cas ou la geometrie JAK est un anneau
		} else if (g instanceof de.micromata.opengis.kml.v_2_2_0.LinearRing) {
			de.micromata.opengis.kml.v_2_2_0.LinearRing l = (LinearRing) g;
			com.vividsolutions.jts.geom.LinearRing newln = this
					.changeLinearRingJAKenJTS(l);// on appelle une nouvelle
			// methode changeLinearRingJAKenJTS()qui va realiser le meme travail
			// que pour les autres cas, cad, creer un anneau JTS a partir d'un
			// anneau JAK.
			// NB:Pour traiter un polygone il faut auparavent traiter
			// differents anneaux d'ou la creation d'une methode a part entiere

			return newln;

			// En cas ou la geometrie JAK est un polygone
		} else if (g instanceof de.micromata.opengis.kml.v_2_2_0.Polygon) {
			Polygon pt = (Polygon) g;

			com.vividsolutions.jts.geom.Polygon poly;

			// pour le polygone, il faut d'abord extraire l'anneau JAK associe a
			// son
			// enveloppe exterieure
			de.micromata.opengis.kml.v_2_2_0.LinearRing outerbound = pt
					.getOuterBoundaryIs().getLinearRing();
			// puis on traite cette anneau JAK en anneau JTS: outer
			com.vividsolutions.jts.geom.LinearRing outer = this
					.changeLinearRingJAKenJTS(outerbound);

			// puis on regarde si il possede des anneaux JTS qui definissent des
			// enveloppes internes, auquel cas, on cree autant d'anneaux JAK que
			// stocke dans un tableau: inner

			List<Boundary> bound = pt.getInnerBoundaryIs();// extrait les
			// enveloppes
			// internes
			int j = 0;

			com.vividsolutions.jts.geom.LinearRing[] inner = new com.vividsolutions.jts.geom.LinearRing[bound
					.size()];// initialisation d'un tableau d'anneaux JTS

			for (Boundary b : bound) {
				inner[j] = this.changeLinearRingJAKenJTS(b.getLinearRing());
				j++;// pour chaque anneau interne JAK on cree un anneau JTS
			}
			poly = JTSFactory.createPolygon(outer, inner);// createPolygon()
			// (methode JTS
			// associe au
			// GeometryFactory) permet la creation
			// d'un polygone JTS en acceptant en
			// entree un anneau JTS definissant
			// son enveloppe externe et une liste
			// d'anneaux JTS internes

			return poly;

			// En cas ou la geometrie JAK est une multigeometrie
		} else if (g instanceof de.micromata.opengis.kml.v_2_2_0.MultiGeometry) {
			MultiGeometry mg = (MultiGeometry) g;
			List<de.micromata.opengis.kml.v_2_2_0.Geometry> listgeom2 = mg
					.getGeometry();// getGeometry()(methode JAK) extrait toutes
			// les geometries JAK d'une multigeometrie
			// JAK
			com.vividsolutions.jts.geom.Geometry[] listgeom3 = new com.vividsolutions.jts.geom.Geometry[listgeom2
					.size()];

			// puis chacune de ces geometries JAK est traiter en reappelant la
			// fonction gestionGeometryJAK()et les geometries JTS obtenues
			// stockees dans un tableau
			int j = 0;
			for (Geometry ng : listgeom2) {
				this.gestionGeometryJAK(ng);
				listgeom3[j] = this.gestionGeometryJAK(ng);
				j++;
			}
			com.vividsolutions.jts.geom.GeometryCollection newgc = JTSFactory
					.createGeometryCollection(listgeom3);// et grace a ce
			// tableau en entree
			// de la fonction
			// createGeometryCollection()
			// (methode JTS
			// associer a un
			// GeometryFactory)
			// on cree une
			// multigeometry JTS

			return newgc;
		}

		else
			return null;
	}

	// methode permettant de changer une liste d'objet coordonneeJAK en tableau
	// d'objet coordonnee JTS
	public com.vividsolutions.jts.geom.Coordinate[] changementCoord(
			List<de.micromata.opengis.kml.v_2_2_0.Coordinate> listJAK) {

		com.vividsolutions.jts.geom.Coordinate[] listJTS = new com.vividsolutions.jts.geom.Coordinate[listJAK
				.size()];
		int j = 0;

		// pour chaque coordonnee JAK, on cree un coordonnee JTS dont les
		// attributs x,y,z doivent correspondre au attributs longitude, latitude
		// et altitude de coordonnee JAK
		for (de.micromata.opengis.kml.v_2_2_0.Coordinate i : listJAK) {
			com.vividsolutions.jts.geom.Coordinate b = new com.vividsolutions.jts.geom.Coordinate();
			b.x = i.getLongitude();
			b.y = i.getLatitude();
			b.z = i.getAltitude();
			listJTS[j] = b;
			j = j + 1;
		}
		return listJTS;
	}

	// Methode specifique au traitement des anneaux
	public com.vividsolutions.jts.geom.LinearRing changeLinearRingJAKenJTS(
			de.micromata.opengis.kml.v_2_2_0.LinearRing l) {

		List<de.micromata.opengis.kml.v_2_2_0.Coordinate> c = l
				.getCoordinates();// getCoordinates()(methode JAK)recolte
		// des coordonnees de l'anneau stockes dans
		// une liste c

		com.vividsolutions.jts.geom.Coordinate[] d = this.changementCoord(c);
		// appel de la methode permettant la
		// creation de coordonnees JTS stockees
		// dans le tableau d

		com.vividsolutions.jts.geom.LinearRing newln = JTSFactory
				.createLinearRing(d);// createLineString() (methode JTS
		// associe au
		// GeometryFactory) permet la creation
		// d'un anneau JTS et accepte en entree un tableau de coordonnee
		// (JTS)

		return newln;
	}

	// METHODE DE BASE: retourne un RowMetaInterface contenant les differents
	// champs (ValueMetaInterface) a extraire du fichier kml pour former une BDD
	// et leurs caracteristiques
	public RowMetaInterface getFields() throws KettleException {
		String debug = "get attributes from Geotools datastore";
		RowMetaInterface row = new RowMeta();

		try {
			debug = "allocate data types";
			ValueMetaInterface value1 = null;
			ValueMetaInterface value2 = null;
			ValueMetaInterface value3 = null;

			// Class c = this.recolterGeometryJTS(kml).getClass();

			debug = "geometry attribute";

			// Ils sont au nombre de trois, deux string et une geometry JTS
			value1 = new ValueMeta("name", ValueMetaInterface.TYPE_STRING);// le
			// champ
			// name
			// issu
			// des
			// placemark
			value2 = new ValueMeta("description",
					ValueMetaInterface.TYPE_STRING);// le champ description issu
			// des placemark
			value3 = new ValueMeta("the_geom", ValueMetaInterface.TYPE_GEOMETRY);// le
			// champ
			// geometry

			if (value1 != null) {
				row.addValueMeta(value1);
			}
			if (value2 != null) {
				row.addValueMeta(value2);
			}
			if (value3 != null) {
				row.addValueMeta(value3);
			}

		} catch (Exception e) {
			throw new KettleException(
					"Error reading KML file metadata (in part " + debug + ")",
					e);
		}

		return row;
	}

	// METHODE DE BASE: appelee pour chaque ligne (entree de la BDD), cette
	// methode permet de remplir les champs (name, description et the_geom) pour
	// chacune des geometrie JTS obtenues
	public Object[] getRow(RowMetaInterface fields) throws KettleException {
		return getRow(RowDataUtil.allocateRowData(fields.size()));
	}

	public Object[] getRow(Object[] r) throws KettleException {

		String debug = "";

		try {
			if (k < geomlistJTS.length) {// on verifie donc que la methode n'a
				// pas deja parcouru toutes les geometries de la liste
				// geomlistJTS
				r[0] = geomname.get(k);// pour la geometry consideree, on
				// extrait le name de son placemark
				r[1] = geomdesc.get(k);// la description de son placemark
				r[2] = geomlistJTS[k];// et la geometrie en elle meme
				// qui sont stockes dans un tableau d'objet r, ou leur
				// emplacement designe leur champ

				k++;// on increment le nombre de fois ou la methode a ete
				// appelee ce qui correspond au numero de la prochaine ligne a
				// traiter
			} else
				return null;// acheve le step car la liste geomlistJTS a ete
			// parcourue dans son ensemble

		} catch (Exception e) {
			log.logError(toString(), "Unexpected error in part [" + debug
					+ "] : " + e.toString());
			error = true;
			throw new KettleException(
					"Unable to read row from KMLtools datastore", e);
		}

		return r;
	}

	public boolean close() {
		return false;
	}

	public boolean hasError() {
		return error;
	}

	public String toString() {
		if (kmlURL != null)
			return kmlURL.toString();
		else
			return getClass().getName();
	}

	public String getVersionInfo() {

		return null;
	}

	/**
	 * @return the gisURL
	 */
	public java.net.URL getKmlURL() {
		return kmlURL;
	}

	/**
	 * @param gisURL
	 *            the gisURL to set
	 */
	public void setKmlURL(java.net.URL kmlURL) {
		this.kmlURL = kmlURL;
	}
}
