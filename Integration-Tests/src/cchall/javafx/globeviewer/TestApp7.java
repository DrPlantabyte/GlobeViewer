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
package cchall.javafx.globeviewer;

import cchall.javafx.globeviewer.interaction.AutoSpinWithMouse;
import cchall.javafx.globeviewer.interaction.InteractionHandler;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

/**
 *
 * @author CCHall <a href="mailto:hallch20@msu.edu">hallch20@msu.edu</a>
 */
public class TestApp7 extends Application {


	private static final String DIFFUSE_MAP
			= "earth-tex_color.png";
	private static final String NORMAL_MAP
			= "earth-tex_normal.png";
	private static final String SPECULAR_MAP
			= "earth-tex_specular.png";
	private static final String AMBIENT_MAP
			= "earth-tex_night.png";
	
	@Override
	public void start(final Stage stage) {
		try{
		HBox mainPane = new HBox();
		Scene scene = new Scene(
				mainPane,
				1600, 800,
				true,
				SceneAntialiasing.BALANCED
		);
		
		
		mainPane.setBackground(new Background(new BackgroundFill(Color.MIDNIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
		
		
		Image ref_diffuse = new Image(
				TestApp1.class.getResource(DIFFUSE_MAP).toExternalForm(),
				400, 200, true, true
		);
		Image ref_ambient = new Image(
				TestApp1.class.getResource(AMBIENT_MAP).toExternalForm(),
				400, 200, true, true
		);
		Image ref_specular = new Image(
				TestApp1.class.getResource(SPECULAR_MAP).toExternalForm(),
				400, 200, true, true
		);
		Image ref_normal = new Image(
				TestApp1.class.getResource(NORMAL_MAP).toExternalForm(),
				400, 200, true, true
		);
		
		
		GlobeViewer mercator = new MercatorGlobeViewer();
		VBox c1 = new VBox();
		Image mercator_diffuse = mercatorUVtoGlobeUV(ref_diffuse, mercator);
		Image mercator_ambient = mercatorUVtoGlobeUV(ref_ambient, mercator);
		Image mercator_specular = mercatorUVtoGlobeUV(ref_specular, mercator);
		Image mercator_normal = mercatorUVtoGlobeUV(ref_normal, mercator);
		setup(mercator, mercator_diffuse, mercator_ambient, mercator_specular, mercator_normal);
		c1.getChildren().addAll(mercator.getScene(), new ImageView(mercator_diffuse), 
				  new ImageView(mercator_ambient), new ImageView(mercator_specular), 
				  new ImageView(mercator_normal));
		mainPane.getChildren().add(c1);
		
		
		GlobeViewer cuboid = new CubicGlobeViewer();
		VBox c2 = new VBox();
		Image cuboid_diffuse = mercatorUVtoGlobeUV(ref_diffuse, cuboid);
		Image cuboid_ambient = mercatorUVtoGlobeUV(ref_ambient, cuboid);
		Image cuboid_specular = mercatorUVtoGlobeUV(ref_specular, cuboid);
		Image cuboid_normal = mercatorUVtoGlobeUV(ref_normal, cuboid);
		setup(cuboid, cuboid_diffuse, cuboid_ambient, cuboid_specular, cuboid_normal);
		c2.getChildren().addAll(cuboid.getScene(), new ImageView(cuboid_diffuse), 
				  new ImageView(cuboid_ambient), new ImageView(cuboid_specular), 
				  new ImageView(cuboid_normal));
		mainPane.getChildren().add(c2);
		
		
		GlobeViewer dymax = new DymaxionGlobeViewer();
		VBox c3 = new VBox();
		Image dymax_diffuse = mercatorUVtoGlobeUV(ref_diffuse, dymax);
		Image dymax_ambient = mercatorUVtoGlobeUV(ref_ambient, dymax);
		Image dymax_specular = mercatorUVtoGlobeUV(ref_specular, dymax);
		Image dymax_normal = mercatorUVtoGlobeUV(ref_normal, dymax);
		setup(dymax, dymax_diffuse, dymax_ambient, dymax_specular, dymax_normal);
		c3.getChildren().addAll(dymax.getScene(), new ImageView(dymax_diffuse), 
				  new ImageView(dymax_ambient), new ImageView(dymax_specular), 
				  new ImageView(dymax_normal));
		mainPane.getChildren().add(c3);
		
		
		GlobeViewer sine = new SinusoidalGlobeViewer();
		VBox c4 = new VBox();
		Image sine_diffuse = mercatorUVtoGlobeUV(ref_diffuse, sine);
		Image sine_ambient = mercatorUVtoGlobeUV(ref_ambient, sine);
		Image sine_specular = mercatorUVtoGlobeUV(ref_specular, sine);
		Image sine_normal = mercatorUVtoGlobeUV(ref_normal, sine);
		setup(sine, sine_diffuse, sine_ambient, sine_specular, sine_normal);
		c4.getChildren().addAll(sine.getScene(), new ImageView(sine_diffuse), 
				  new ImageView(sine_ambient), new ImageView(sine_specular), 
				  new ImageView(sine_normal));
		mainPane.getChildren().add(c4);
		

		stage.setScene(scene);
		stage.show();
		
		}catch (Exception ex){
			System.err.flush();
			ex.printStackTrace(System.out);
			System.out.flush();
			ex.printStackTrace(System.err);
			System.exit(-1);
		}
	}
	

	public static void main(String[] args) {
		launch(args);
	}

	private Image mercatorUVtoGlobeUV(final Image input, final GlobeViewer gv){
		final WritableImage output = new WritableImage((int)input.getWidth(), (int)input.getHeight());
		List<Callable<Object>> tasks = new ArrayList<>((int)output.getHeight());
		final Object locker = new Object();
		for(int row = 0; row < (int)output.getHeight(); row++){
			final int y = row;
			tasks.add(()->{
				int old_x = -1, old_y = -1, x = -1;
				try{
				final double oneOverTwoPi = 1.0/(2*Math.PI);
				final double oneOverPi = 1.0/(Math.PI);
				final double piOverTwo = Math.PI/2;
				for(x = 0; x < (int)output.getWidth(); x++){
					double rx = (double)x / output.getWidth();
					double ry = (double)y / output.getHeight();
//					System.err.printf("rx, ry = %s, %s\n", rx, ry);
					Point2D new_lonLat = gv.convertRelativePixelXYToLonLat(new Point2D(rx, ry));
//					System.err.printf("new_lonLat = %s\n", new_lonLat);
					Point2D old_XY = mercatorLonLatToRelativePixelXY(new_lonLat);
//					System.err.printf("old_XY = %s\n", old_XY);
					old_x = (int)(old_XY.getX() * input.getWidth());
					old_y = (int)(old_XY.getY() * input.getHeight());
//					System.err.printf("old_x, old_y = %s, %s\n", old_x, old_y);
					output.getPixelWriter().setArgb(x, y, 
								input.getPixelReader().getArgb(old_x, old_y)
							);
				}
				return null;
				} catch(Throwable t){
					synchronized (locker){
						System.err.printf("%s: (%s, %s) <- (%s, %s) failed for %sx%s image\n", 
								  gv.getClass().getSimpleName(), x, y, old_x, old_y, 
								  (int)input.getWidth(), (int)input.getHeight());
						t.printStackTrace(System.err);
						System.exit(t.getClass().hashCode());
					}
					return null;
				}
			});
		}
		ForkJoinPool.commonPool().invokeAll(tasks);
		return output;
	}
	
	private void setup(final GlobeViewer gv, Image d, Image a, Image s, Image n) {
		gv.getScene().heightProperty().set(200);
		gv.getScene().widthProperty().set(400);
		
		
		gv.diffuseTextureProperty().set(d);
		gv.normalVectorMapProperty().set(n);
		gv.specularTextureProperty().set(s);
		gv.ambientTextureProperty().set(a);
		gv.lightDirectionVector().set(new Point3D(1, 0, -1));
		
		InteractionHandler spinner = new AutoSpinWithMouse();
		spinner.applyTo(gv);
		
		gv.getScene().addEventHandler(MouseEvent.ANY, (MouseEvent event)->{
			if(event.isSecondaryButtonDown() && event.getEventType() == MouseEvent.MOUSE_PRESSED) {
				PickResult pickResult = event.getPickResult();
				if (pickResult == null) {
					return;
				}
				Node picked = pickResult.getIntersectedNode();
				if (picked == gv.getGlobe()) {
					Point2D pickCoord = pickResult.getIntersectedTexCoord();
					Point2D lonLat = gv.convertRelativePixelXYToLonLat(pickCoord);
					System.out.println(String.format("%s\t%s deg. Longitude, %s deg. Latitude", 
							  pickCoord,
							  (lonLat.getX())*180/Math.PI,
							  (lonLat.getY())*180/Math.PI));
				}
			}
		});
		
	}

	
	
	public static Point2D mercatorLonLatToRelativePixelXY(Point2D lonLat) {
		final double oneOverTwoPi = 1.0 / (2.0 * Math.PI);
		final double oneOverPi = 1.0 / Math.PI;
		final double piOverTwo = 0.5 * Math.PI;
//		System.err.printf("mercatorLonLatToRelativePixelXY lonLat %s\n",lonLat);
//		System.err.printf("%s\n",(lonLat.getY() + piOverTwo));
//		System.err.printf("%s\n",clamp( (lonLat.getY() + piOverTwo) * oneOverPi));
//		System.err.printf("%s\n",1.0 - clamp( (lonLat.getY() + piOverTwo) * oneOverPi));
		return new Point2D( clamp( lonLat.getX() * oneOverTwoPi), clamp(1.0 - ( (lonLat.getY() + piOverTwo) * oneOverPi)) );
	}

	public static Point2D mercatorRelativePixelXYToLonLat(Point2D relXY) {
		final double twoPi = 2.0 * Math.PI;
		return new Point2D( clamp( relXY.getX() ) * twoPi, (0.5 - clamp( relXY.getY() )) * Math.PI );
	}
	private static double clamp(double d){
		return d - Math.floor(d);
	}
}
