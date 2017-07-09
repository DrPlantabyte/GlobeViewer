/*
 * The MIT License
 *
 * Copyright 2017 .
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hall.collin.christopher.javafx.globeviewer;

import java.util.Random;
import java.util.function.Function;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Cybergnome
 */
public class GlobeViewerTest {
	
	public GlobeViewerTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
		
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}

	/**
	 * Test of class GlobeViewer.
	 */
	@Test
	public void testNumericalFunctionProperty() {
		System.out.println("testNumericalFunctionProperty");
		
		SimpleDoubleProperty inputProperty = new SimpleDoubleProperty();
		SimpleDoubleProperty outputProperty = new SimpleDoubleProperty();
		Random prng = new Random(12354);
		
		System.out.println("\tDefault behavior: input == output");
		GlobeViewer.NumericalFunctionProperty instance 
				= new GlobeViewer.NumericalFunctionProperty();
		outputProperty.bind(instance);
		instance.bind(inputProperty);
		for(int i = 0; i < 10; i++){
			double in = prng.nextDouble() * 5;
			inputProperty.set(in);
			double out = outputProperty.get();
			System.out.println("\t\t"+in+" -> "+out);
			assertEquals("Equality check",in,out,0);
		}
		
		final double a = prng.nextDouble(), b = prng.nextDouble()*-5, c=prng.nextDouble()*10;
		
		System.out.println(String.format("\tTesting function f(x)=%s*x^2 + %s*x + %s"
				,a,b,c));
		Function<Double,Double> f = (Double x)->a*x*x + b*x + c;
		instance.functionProperty().set(f);
		for(int i = 0; i < 5; i++){
			double x = prng.nextDouble() * 200 - 100;
			inputProperty.set(x);
			double out = outputProperty.get();
			System.out.println("\t\tf("+x+") -> "+out);
			assertEquals("Equality check",f.apply(x),out,0);
		}
		System.out.println("\tTesting same function with reverse order of binding");
		inputProperty = new SimpleDoubleProperty();
		outputProperty = new SimpleDoubleProperty();
		instance = new GlobeViewer.NumericalFunctionProperty();
		instance.functionProperty().set(f);
		instance.bind(inputProperty);
		outputProperty.bind(instance);
		for(int i = 0; i < 5; i++){
			double x = prng.nextDouble() * 200 - 100;
			inputProperty.set(x);
			double out = outputProperty.get();
			System.out.println("\t\tf("+x+") -> "+out);
			assertEquals("Equality check",f.apply(x),out,0);
		}
		
		System.out.println("\tSanity check test 1");
		inputProperty = new SimpleDoubleProperty();
		outputProperty = new SimpleDoubleProperty();
		instance = new GlobeViewer.NumericalFunctionProperty();
		try{
			instance.bindBidirectional(inputProperty);
			fail("Invalid binding failed to throw exception");
		} catch(Exception e){
			System.out.println("\t\tPassed: Threw "+e.getClass().getSimpleName());
		}
// impossible requirement
//		System.out.println("\tSanity check test 2");
//		try{
//			outputProperty.bindBidirectional(instance);
//			fail("Invalid binding failed to throw exception");
//		} catch(Exception e){
//			System.out.println("\t\tPassed: Threw "+e.getClass().getSimpleName());
//		}
	}
	
	
	@Test
	public void testCoordinateFunctions(){
		System.out.println("testCoordinateFunctions");
		final double acceptablePrecision = 1e-7;
		GlobeViewer gv = new MercatorGlobeViewer();
		Random prng = new Random(12345);
		for(int i = 0; i < 50; i++){
			Point3D randomXYZ = (new Point3D(prng.nextDouble() * 2 - 1, prng.nextDouble() * 2 - 1, prng.nextDouble() * 2 - 1)).normalize();
			Point2D lonLat = gv.surfacePointToLonLat(randomXYZ);
			Point3D resampledXYZ = gv.lonLatToSurfacePoint(lonLat);
			System.out.println(String.format("\t%s -> %s -> %s",randomXYZ, lonLat, resampledXYZ));
			assertPoints(randomXYZ, resampledXYZ, acceptablePrecision);
			
		}
		for(int i = 0; i < 50; i++){
			Point2D randomLonLat = new Point2D((prng.nextDouble() * 2 - 1) * Math.PI, (prng.nextDouble() - 0.5) * Math.PI);
			Point3D xyz = gv.lonLatToSurfacePoint(randomLonLat);
			Point2D resampledLonLat = gv.surfacePointToLonLat(xyz);
			System.out.println(String.format("\t%s -> %s -> %s",randomLonLat, xyz, resampledLonLat));
			assertPoints(randomLonLat, resampledLonLat, acceptablePrecision);
		}
		System.out.println(String.format("\tTesting problem points"));
		Point3D[] problemPoints = {
				new Point3D(70.71067811865476, 70.71067811865474, -0.0),
				new Point3D(70.71067811865476, -0.0, 70.71067811865474),
				new Point3D(-0.0, 70.71067811865476, 70.71067811865474),
				new Point3D(0,0,0),
				new Point3D(-1,0,0),
				new Point3D(1,0,0),
				new Point3D(0,-1,0),
				new Point3D(0,1,0),
				new Point3D(0,0,-1),
				new Point3D(0,0,1),
		};
		for(int i = 0; i < problemPoints.length; i++){
			Point2D lonLat = gv.surfacePointToLonLat(problemPoints[i]);
			System.out.println(String.format("\t\t%s -> %s",problemPoints[i], lonLat));
			assertRealNumbers(lonLat.getX(), lonLat.getY());
		}
	}
	
	private static void assertPoints(Point3D p1, Point3D p2, double acceptablePrecision){
		assertEquals(p1.getX(), p2.getX(), acceptablePrecision);
		assertEquals(p1.getY(), p2.getY(), acceptablePrecision);
		assertEquals(p1.getZ(), p2.getZ(), acceptablePrecision);
	}
	private static void assertPoints(Point2D p1, Point2D p2, double acceptablePrecision){
		assertEquals(p1.getX(), p2.getX(), acceptablePrecision);
		assertEquals(p1.getY(), p2.getY(), acceptablePrecision);
	}

	private static void assertRealNumbers(double... d) {
		for(int i = 0; i < d.length; i++){
			assertTrue("Assertion failure: "+d+" is not a real number!", Double.isFinite(d[i]));
		}
	}
	
}
