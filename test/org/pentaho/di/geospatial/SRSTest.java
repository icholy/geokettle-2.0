package org.pentaho.di.geospatial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.geotools.referencing.CRS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.trans.steps.srstransformation.SRSList;

public class SRSTest {
	Set<SRS> allSRS;
	SRS srsWGS84;
	
	
	@Before
	public void setUp() throws Exception {
		allSRS = (new SRSList()).getAllSRS();
		srsWGS84 = SRS.createFromEPSG("4326");
	}

	@After
	public void tearDown() throws Exception {
		allSRS = null;
		srsWGS84 = null;
	}

	@Test
	public void testSRS() {
		for (SRS srs : allSRS) {
			SRS newSRS = new SRS(srs.authority, srs.srid, srs.description);
			assertTrue(srs.equals(newSRS));
			assertTrue(srs.getXML().equals(newSRS.getXML()));
			try {
				assertTrue(CRS.equalsIgnoreMetadata(srs.getCRS(), newSRS.getCRS()));
			} catch (KettleStepException e) {
				System.err.println("Couldn't create a new CoordinateReferenceInstance ("+srs.srid+")");
			}
		}
		
		fail();
	}

	@Test
	public void testSRS_WKT() {
		try {
			assertFalse(srsWGS84.getCRS().toWKT().equals(""));
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testGetSRID() {
		assertEquals(srsWGS84.getSRID(), 4326);
	}

	@Test
	public void testGetCRS() {
		try {
			srsWGS84.getCRS();
		} catch (KettleStepException e) {
			fail("Cannot create CRS");
		}
	}

	@Test
	public void testCompareTo() {
		assertEquals(srsWGS84.compareTo(srsWGS84), 0);
	}

	@Test
	public void testClone() {
		SRS clone = (SRS) srsWGS84.clone();
		assertTrue(clone.equals(srsWGS84));
	}

	@Test
	public void testEqualsObject() {
		assertTrue(SRS.createFromEPSG("4326").equals(srsWGS84));
		assertFalse(SRS.createFromEPSG("2000").equals(srsWGS84));
	}

	@Test
	public void testCreateFromEPSG() {
		assertTrue(SRS.createFromEPSG("4326").equals(srsWGS84));
	}

}
