package org.pentaho.di.trans.steps.wfsinput;

import org.geotools.factory.GeoTools;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.referencing.CRS;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GTUtils {
	public static final int TIMEOUT = 30000;

	public static final FilterFactoryImpl FF = new FilterFactoryImpl(
			GeoTools.getDefaultHints());

	public static final String[] FUNCTIONS = new String[] { "=", "<>", "<",
			"<=", ">", ">=", "IS NULL", "IS NOT NULL", "LIKE",
			"GIS_INTERSECTS", "GIS_EQUALS", "GIS_CONTAINS", "GIS_CROSSES",
			"GIS_DISJOINT", "GIS_WITHIN", "GIS_OVERLAPS", "GIS_TOUCHES" };

	public static final int FUNC_EQUAL = 0;
	public static final int FUNC_NOT_EQUAL = 1;
	public static final int FUNC_SMALLER = 2;
	public static final int FUNC_SMALLER_EQUAL = 3;
	public static final int FUNC_LARGER = 4;
	public static final int FUNC_LARGER_EQUAL = 5;
	public static final int FUNC_NULL = 6;
	public static final int FUNC_NOT_NULL = 7;
	public static final int FUNC_LIKE = 8;
	public static final int FUNC_GIS_INTERSECTS = 9;
	public static final int FUNC_GIS_EQUALS = 10;
	public static final int FUNC_GIS_CONTAINS = 11;
	public static final int FUNC_GIS_CROSSES = 12;
	public static final int FUNC_GIS_DISJOINT = 13;
	public static final int FUNC_GIS_WITHIN = 14;
	public static final int FUNC_GIS_OVERLAPS = 15;
	public static final int FUNC_GIS_TOUCHES = 16;

	public static final int getFunction(String description) {
		int function = -1;
		for (int i = 0; i < FUNCTIONS.length; i++) {
			if (FUNCTIONS[i].equalsIgnoreCase(Const.trim(description))) {
				function = i;
				break;
			}
		}
		return function;
	}

	public static int getGKBinding(AttributeType at) {
		Class<?> c = at.getBinding();
		int binding;
		if (c == java.lang.Integer.class || c == java.lang.Long.class)
			binding = ValueMetaInterface.TYPE_INTEGER;
		else if (c == java.lang.Double.class)
			binding = ValueMetaInterface.TYPE_NUMBER;
		else if (c == java.util.Date.class)
			binding = ValueMetaInterface.TYPE_DATE;
		else if (com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(c))
			binding = ValueMetaInterface.TYPE_GEOMETRY;
		else
			binding = ValueMetaInterface.TYPE_STRING;
		return binding;
	}

	public static SRS getSRS(GeometryDescriptor gd) throws KettleException {
		return new SRS(gd.getCoordinateReferenceSystem());
	}

	private static Literal getLiteral(Object o) {
		return FF.literal(o);
	}

	private static Literal getGeometryLiteral(Object o) throws ParseException {
		return FF.literal(new WKTReader(new GeometryFactory()).read(o
				.toString()));
	}

	public static Filter buildFilter(Expression e, String condition,
			Object value) throws Exception {
		Filter f;
		switch (getFunction(condition)) {
		case FUNC_EQUAL:
			f = FF.equals(e, getLiteral(value));
			break;
		case FUNC_NOT_EQUAL:
			f = FF.notEqual(e, getLiteral(value));
			break;
		case FUNC_SMALLER:
			f = FF.less(e, getLiteral(value));
			break;
		case FUNC_SMALLER_EQUAL:
			f = FF.lessOrEqual(e, getLiteral(value));
			break;
		case FUNC_LARGER:
			f = FF.greater(e, getLiteral(value));
			break;
		case FUNC_LARGER_EQUAL:
			f = FF.greaterOrEqual(e, getLiteral(value));
			break;
		case FUNC_NULL:
			f = FF.isNull(e);
			break;
		case FUNC_NOT_NULL:
			f = FF.not(FF.isNull(e));
			break;
		case FUNC_LIKE:
			f = FF.like(e, value.toString());
			break;
		case FUNC_GIS_INTERSECTS:
			f = FF.intersects(e, getGeometryLiteral(value));
			break;
		case FUNC_GIS_EQUALS:
			f = FF.equal(e, getGeometryLiteral(value));
			break;
		case FUNC_GIS_CONTAINS:
			f = FF.contains(e, getGeometryLiteral(value));
			break;
		case FUNC_GIS_CROSSES:
			f = FF.crosses(e, getGeometryLiteral(value));
			break;
		case FUNC_GIS_DISJOINT:
			f = FF.disjoint(e, getGeometryLiteral(value));
			break;
		case FUNC_GIS_WITHIN:
			f = FF.within(e, getGeometryLiteral(value));
			break;
		case FUNC_GIS_OVERLAPS:
			f = FF.overlaps(e, getGeometryLiteral(value));
			break;
		case FUNC_GIS_TOUCHES:
			f = FF.touches(e, getGeometryLiteral(value));
			break;
		default:
			f = null;
			break;
		}
		return f;
	}

	public static boolean decode(String srs) {
		boolean valid = true;
		try {
			if (!Const.isEmpty(srs))
				CRS.decode(srs);
		} catch (Exception e) {
			valid = false;
		}
		return valid;
	}

	public static Object getJavaBinding(Object value) {
		Object o;
		if (value == null)
			o = null;
		else {
			Class<?> c = value.getClass();
			if (c == java.lang.String.class)
				o = (String) value;
			else if (c == java.lang.Integer.class)
				o = new Long(((Integer) value).longValue());
			else if (c == java.lang.Long.class)
				o = (Long) value;
			else if (c == java.lang.Double.class)
				o = (Double) value;
			else if (c == java.util.Date.class)
				o = (java.util.Date) value;
			else if (com.vividsolutions.jts.geom.Geometry.class
					.isAssignableFrom(c))
				o = (Geometry) value;
			else
				o = null;
		}
		return o;
	}
}
