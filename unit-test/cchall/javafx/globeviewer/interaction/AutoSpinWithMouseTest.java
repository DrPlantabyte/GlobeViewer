/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cchall.javafx.globeviewer.interaction;

import cchall.javafx.globeviewer.GlobeViewer;
import cchall.javafx.globeviewer.MercatorGlobeViewer;
import cchall.javafx.globeviewer.JavaFXInitializer;
import org.junit.*;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.fail;

/**
 *
 * @author Cybergnome
 */
public class AutoSpinWithMouseTest {
	
	public AutoSpinWithMouseTest() {
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
		ForkJoinPool.commonPool().submit(()->{
			JavaFXInitializer.launch(JavaFXInitializer.class);});
	}
	
	@After
	public void tearDown() {
		System.out.println("} :"+this.getClass().getSimpleName());
		JavaFXInitializer.exit();
	}

	/**
	 * Test of remove method, of class AutoSpinWithMouse.
	 */
	@Test
	public void testRemove() {
		System.out.println("remove");
		InteractionHandler instance = new AutoSpinWithMouse();
		try{
			instance.remove();// should do nothing
		} catch (Exception e){
			fail(e.toString() + Arrays.asList(e.getStackTrace()).toString().replace(",", "\n\t").replace("[","").replace("]",""));
		}
		GlobeViewer g1 = new MercatorGlobeViewer();
		GlobeViewer g2 = new MercatorGlobeViewer();
		System.out.println("\tapplyTo(g1)");
		instance.applyTo(g1);
		try{
			System.out.println("\tapplyTo(g2)");
			instance.applyTo(g2); // should throw IllegalStateException
			fail(instance.getClass().getSimpleName()+" failed to throw exception on InteractionHandler.applyTo(...) when already controlling a GlobeViewer instance");
		} catch (IllegalStateException e){
			System.out.println("\tcorrectly threw "+ e.getClass().getSimpleName());
		}
		System.out.println("\tremove()");
		instance.remove();
		try{
			System.out.println("\tapplyTo(g2)");
			instance.applyTo(g2);
		} catch (Exception e){
			fail(e.toString() + Arrays.asList(e.getStackTrace()).toString().replace(",", "\n\t").replace("[","").replace("]",""));
		}
		
	}

	
}
