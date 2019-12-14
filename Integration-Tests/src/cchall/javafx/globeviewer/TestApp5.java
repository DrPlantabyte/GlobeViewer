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
import static javafx.application.Application.launch;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author CCHall <a href="mailto:hallch20@msu.edu">hallch20@msu.edu</a>
 */
public class TestApp5 extends Application {


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

		AnchorPane mainPane = new AnchorPane();
		Scene scene = new Scene(
				mainPane,
				400, 400,
				true,
				SceneAntialiasing.BALANCED
		);
		
		
		
		final GlobeViewer globeView = new MercatorGlobeViewer();
		mainPane.getChildren().add(globeView.getScene());
		globeView.getScene().heightProperty().bind(mainPane.heightProperty());
		globeView.getScene().widthProperty().bind(mainPane.widthProperty());
		mainPane.setBackground(new Background(new BackgroundFill(Color.MIDNIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
		
		globeView.diffuseTextureProperty().set(new Image(
						TestApp1.class.getResource(DIFFUSE_MAP).toExternalForm()
				));
		globeView.normalVectorMapProperty().set(new Image(
						TestApp1.class.getResource(NORMAL_MAP).toExternalForm()
				));
		globeView.specularTextureProperty().set(new Image(
						TestApp1.class.getResource(SPECULAR_MAP).toExternalForm()
				));
		globeView.ambientTextureProperty().set(new Image(
						TestApp1.class.getResource(AMBIENT_MAP).toExternalForm()
				));
		globeView.lightDirectionVector().set(new Point3D(1, 0, 1));
		
		InteractionHandler spinner = new AutoSpinWithMouse();
		spinner.applyTo(globeView);
		
//		final AtomicReference<Point2D> dragStart = new AtomicReference<>(null);
//		final AtomicReference<Point3D> rotationStart = new AtomicReference<>(null);
//		globeView.getScene().setOnMouseDragged((MouseEvent e)->{
//			final double pi = Math.PI;
//			final double piOver2 = 0.5*Math.PI;
//			if(dragStart.get() == null){
//				dragStart.set(new Point2D(e.getSceneX(), e.getSceneY()));
//				rotationStart.set(globeView.globeRotationVector().get());
//			} else {
//				double dX = (e.getSceneX() - dragStart.get().getX()) / globeView.getScene().getWidth();
//				double dY = (dragStart.get().getY() - e.getSceneY()) / globeView.getScene().getHeight();
//				Point3D old = rotationStart.get();
//				double rx = old.getX() + (dY * piOver2);// * Math.cos(old.getY());
//				double ry = old.getY() + (dX * pi);
//				double rz = old.getZ();// + (dY * piOver2) * Math.sin(old.getY());
//				Point3D newVector = new Point3D(rx, ry, rz);
//				globeView.globeRotationVector().set(newVector);
//				rotationStart.set(newVector);
//				dragStart.set(new Point2D(e.getSceneX(), e.getSceneY()));
//			}
//		});
//		globeView.getScene().setOnMouseReleased((MouseEvent e)->{
//			dragStart.set(null);
//		});
//		globeView.getScene().setOnMouseClicked((MouseEvent e)->{
//			PickResult pick = e.getPickResult();
//			if(pick == null){
//				System.out.println("null pick");
//				return;
//			}
//			System.out.println(pick.toString());
//			Point2D texCoord = pick.getIntersectedTexCoord();
//			if(texCoord == null){
//				System.out.println("null tex coord");
//				return;
//			}
//			System.out.println(String.format("(%s, %s)", texCoord.getX(), texCoord.getY()));
//		});
		
//		Thread t = new Thread(()->{
//			final double z_rot = 23.5 * Math.PI / 180;
//			double rotation = 0;
//			final double rpm = 3;
//			final double pi2 = 2 * Math.PI;
//			final double angle_per_ms = pi2 * rpm / 60000d;
//			final long sleep_time = 10;
//			long time = System.currentTimeMillis();
//			while(true){
//				try {
//					Thread.sleep(sleep_time);
//					long time2 = System.currentTimeMillis();
//					long dt = time2 - time;
//					time = time2;
//					rotation = toRange(rotation + dt * angle_per_ms, 0, pi2);
//					final double r = rotation;
//					// NOTE: it is better to add an animation transform to the sphere node than to do this
//					Platform.runLater(()->{globeView.globeRotationVector().set(new Point3D(0, r, z_rot));});
//				} catch (InterruptedException ex) {
//					break;
//				}
//			}
//		});
//		t.setDaemon(true);
//		t.start();

		stage.setScene(scene);
		stage.show();
		

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
