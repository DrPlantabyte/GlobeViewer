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

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;

/**
 *
 * @author CCHall <a href="mailto:hallch20@msu.edu">hallch20@msu.edu</a>
 */
public class TestApp1 extends Application {

	private static final double EARTH_RADIUS = 400;
	private static final double VIEWPORT_SIZE = 800;

	private static final double MAP_WIDTH = 8192 / 2d;
	private static final double MAP_HEIGHT = 4092 / 2d;

	private static final String DIFFUSE_MAP
			= "earth-tex_color.png";
	private static final String NORMAL_MAP
			= "earth-tex_normal.png";
	private static final String SPECULAR_MAP
			= "earth-tex_specular.png";
	private static final String AMPIENT_MAP
			= "earth-tex_night.png";
	
	private static final DoubleProperty rotation_Y = new javafx.beans.property.SimpleDoubleProperty(0.0);
	private static final DoubleProperty rotation_X = new javafx.beans.property.SimpleDoubleProperty(0.0);
	private static final DoubleProperty rotate_end_Y = new javafx.beans.property.SimpleDoubleProperty(0.0);
	private static final DoubleProperty rotate_end_X = new javafx.beans.property.SimpleDoubleProperty(0.0);
	private static final DoubleProperty pixel_start_Y = new javafx.beans.property.SimpleDoubleProperty(0.0);
	private static final DoubleProperty pixel_start_X = new javafx.beans.property.SimpleDoubleProperty(0.0);
	private static final javafx.scene.transform.Rotate axisRotation = new javafx.scene.transform.Rotate(0, new Point3D(0,1,0));
	private static final javafx.scene.transform.Rotate viewerRotation = new javafx.scene.transform.Rotate(0, new Point3D(1,0,0));

	private Group buildScene() {
		Sphere earth = new Sphere(EARTH_RADIUS);
		earth.setTranslateX(0);
		earth.setTranslateY(0);
		earth.setTranslateZ(0);

		PhongMaterial earthMaterial = new PhongMaterial();
		earthMaterial.setDiffuseMap(
				new Image(
						TestApp1.class.getResource(DIFFUSE_MAP).toExternalForm(),
						MAP_WIDTH,
						MAP_HEIGHT,
						true,
						true
				)
		);
		earthMaterial.setBumpMap(
				new Image(
						TestApp1.class.getResource(NORMAL_MAP).toExternalForm(),
						MAP_WIDTH,
						MAP_HEIGHT,
						true,
						true
				)
		);
		earthMaterial.setSpecularMap(
				new Image(
						TestApp1.class.getResource(SPECULAR_MAP).toExternalForm(),
						MAP_WIDTH,
						MAP_HEIGHT,
						true,
						true
				)
		);
		
		Image colorMap = new Image(
						TestApp1.class.getResource(DIFFUSE_MAP).toExternalForm()
		);
		PixelReader pixelReader = colorMap.getPixelReader();
		WritableImage lumenImg = new WritableImage((int)colorMap.getWidth(),
						(int)colorMap.getHeight());
		PixelWriter pixelEditor = lumenImg.getPixelWriter();
		for(int y = 0; y < lumenImg.getHeight(); y++){
			for(int x = 0; x < lumenImg.getWidth(); x++){
				final double multiplier = 0.15;
				Color color = pixelReader.getColor(x, y);
				Color newColor = new Color(
						color.getRed() * multiplier, 
						color.getGreen() * multiplier, 
						color.getBlue() * multiplier, 
						1
				);
				pixelEditor.setColor(x, y, newColor);
			}
		}
		earthMaterial.setSelfIlluminationMap(
				//lumenImg
			   new Image(
						TestApp1.class.getResource(AMPIENT_MAP).toExternalForm(),
						MAP_WIDTH,
						MAP_HEIGHT,
						true,
						true
				)
		);

		earth.setMaterial(
				earthMaterial
		);

		axisRotation.angleProperty().bind(rotation_Y);
		viewerRotation.angleProperty().bind(rotation_X);
		earth.getTransforms().addAll(viewerRotation,axisRotation);
		

		PointLight  light = new PointLight();
		final double degrees2radians = 2*Math.PI/360;
		final double distance = EARTH_RADIUS * 3;
		final double angleRotation = 235;
		final double angleElevation = 23.5; 
		final double subDist = distance * Math.cos(angleElevation*degrees2radians);
		light.setColor(Color.WHITE);
//		light.setTranslateX(subDist * Math.cos(angleRotation*degrees2radians));
//		light.setTranslateY(-distance * Math.sin(angleElevation*degrees2radians));
//		light.setTranslateZ(subDist * Math.sin(angleRotation*degrees2radians));
		
		light.getTransforms().addAll(new javafx.scene.transform.Translate(
				subDist * Math.cos(angleRotation*degrees2radians),
				-distance * Math.sin(angleElevation*degrees2radians),
				subDist * Math.sin(angleRotation*degrees2radians)
		));
		
		
		
		
		
		// Hackish workaround for the scene having irregular bounds
		Box b1 = new Box(1,1,1);
		b1.getTransforms().add(new javafx.scene.transform.Translate(-1000,-1000,-1000));
		b1.setVisible(true);
		Box b2 = new Box(1,1,1);
		b2.getTransforms().add(new javafx.scene.transform.Translate(1000,1000,1000));
		b2.setVisible(true);
		
		Group g = new Group(
				earth,
				light,
				b1,b2
		);
		
		return g;
	}

	@Override
	public void start(Stage stage) {
		Group group = buildScene();

		Scene scene = new Scene(
				new StackPane(group),
				VIEWPORT_SIZE, VIEWPORT_SIZE,
				true,
				SceneAntialiasing.BALANCED
		);

		scene.setFill(Color.rgb(10, 10, 40));

		PerspectiveCamera camera = new PerspectiveCamera(true);
		final double viewDist = 1500;
		camera.setTranslateX(EARTH_RADIUS);
		camera.setTranslateY(EARTH_RADIUS);
		camera.setTranslateZ(-1*viewDist);
		camera.setNearClip(0.1);
		camera.setFarClip(2*viewDist);
		camera.setFieldOfView(35);
		scene.setCamera(camera);
		

		stage.setScene(scene);
		stage.show();

		//stage.setFullScreen(true);

		scene.onMousePressedProperty()
				.set((MouseEvent e)->{
					pixel_start_X.set(e.getX());
					pixel_start_Y.set(e.getY());
				});
		scene.onMouseReleasedProperty()
				.set((MouseEvent e)->{
					rotate_end_X.set(rotation_X.get());
					rotate_end_Y.set(rotation_Y.get());
					System.out.println(String.format("x-rot=%s\ty-rot=%s",rotation_X.get(), rotation_Y.get()));
				});
		scene.onMouseDraggedProperty()
				.set((MouseEvent e)->{
					rotation_Y.set(toRange(
							rotate_end_Y.get() 
							- (e.getX() - pixel_start_X.get()) * (360.0/VIEWPORT_SIZE)
					,0,360));
					rotation_X.set(toRange(
							rotate_end_X.get() 
							+ (e.getY() - pixel_start_Y.get()) * (180.0/VIEWPORT_SIZE)
					,0,360));
					
				});
	}
	
	private static double toRange(double in, double min, double max){
		final double r = max - min;
		while(in < min) in += r;
		while(in > max) in -= r;
		return in;
	}
	

	public static void main(String[] args) {
		launch(args);
	}


}
