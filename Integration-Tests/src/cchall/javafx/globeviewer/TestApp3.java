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
public class TestApp3 extends Application {


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
		
		
		
		GlobeViewer globeView = new MercatorGlobeViewer();
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

		stage.setScene(scene);
		stage.show();
		

	}
	

	public static void main(String[] args) {
		launch(args);
	}


}
