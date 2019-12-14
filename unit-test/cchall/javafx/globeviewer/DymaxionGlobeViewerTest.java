/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cchall.javafx.globeviewer;

import java.util.concurrent.ForkJoinPool;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Cybergnome
 */
public class DymaxionGlobeViewerTest {
	
	public DymaxionGlobeViewerTest() {
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
		ForkJoinPool.commonPool().submit(()->{JavaFXInitializer.launch(JavaFXInitializer.class);});
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
		final GlobeViewer gv = new DymaxionGlobeViewer();
		MercatorGlobeViewerTest.doCoordinateTestOn(gv);
	}

	
}
