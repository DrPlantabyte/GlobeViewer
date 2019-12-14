/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cchall.javafx.globeviewer;

import java.util.Random;

import javafx.geometry.Point2D;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Cybergnome
 */
public class MercatorGlobeViewerTest {
	
	public MercatorGlobeViewerTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	
	@Before
	public void setUp() {
		System.out.println(this.getClass().getSimpleName()+": {");
		
	}
	
	@After
	public void tearDown() {
		System.out.println("} :"+this.getClass().getSimpleName());
		JavaFXInitializer.exit();
	}


	/**
	 * Test of convertLonLatToRelativePixelXY method, of class CuboidGlobeViewer.
	 */
	@Test
	public void testCoordinateConversions() {
		final GlobeViewer gv = new MercatorGlobeViewer();
		MercatorGlobeViewerTest.doCoordinateTestOn(gv);
	}

	static void doCoordinateTestOn(GlobeViewer gv) {
		System.out.println("\t"
				+ gv.getClass().getSimpleName() + ".convertLonLatToRelativePixelXY(Point2D) & " 
				+ gv.getClass().getSimpleName() + ".convertRelativePixelXYToLonLat(Point2D)");
		
		final Random prng = new Random(12345);
		final double acceptablePrecision = 1e-7;
		
		for(int i = 0; i < 100; i++){
			Point2D randomLonLat = new Point2D(
					  (prng.nextDouble() * 2 - 1) * Math.PI * 2, 
					  Math.PI * (prng.nextDouble() - 0.5));
			Point2D xy = gv.convertLonLatToRelativePixelXY(randomLonLat);
			Point2D lonlat = gv.convertRelativePixelXYToLonLat(xy);
			System.out.println(String.format("\t\t(%s lon, %s lat) -> (%s x, %s y) -> (%s lon, %s lat)\t\tdlon = %s, dlat = %s",
					(float)randomLonLat.getX(), (float)randomLonLat.getY(),
					(float)xy.getX(), (float)xy.getY(),
					(float)lonlat.getX(), (float)lonlat.getY(),
					(float)(lonlat.getX() - randomLonLat.getX()), 
					(float)(lonlat.getY() - randomLonLat.getY()) ));
			assertTrue("x range: 0-1",xy.getX() >= 0 && xy.getX() <= 1);
			assertTrue("y range: 0-1",xy.getY() >= 0 && xy.getY() <= 1);
			assertTrue("longitued range: -pi to pi", lonlat.getX() <= Math.PI + acceptablePrecision);
			assertTrue("longitued range: -pi to pi", lonlat.getX() >= -Math.PI - acceptablePrecision);
			assertEquals(circleCorrect(randomLonLat.getX()), circleCorrect(lonlat.getX()), acceptablePrecision);
			assertEquals(randomLonLat.getY(), lonlat.getY(), acceptablePrecision);
		}
		System.out.println("\tSuccess!");
	}
	
	private static double circleCorrect(double radians){
		final double twoPi = 2.0 * Math.PI;
		final double minusPi = -Math.PI;
		double x = radians;
		while(x < minusPi) x += twoPi;
		while(x > Math.PI) x -= twoPi;
		return x;
	}
	
}
