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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author CCHall <a href="mailto:hallch20@msu.edu">hallch20@msu.edu</a>
 */
public class TestApp2 extends Application {


	@Override
	public void start(final Stage stage) {

		//VBox mainPane = new VBox();
		AnchorPane mainPane = new AnchorPane();
		Scene scene = new Scene(
				mainPane,
				400, 400,
				true,
				SceneAntialiasing.BALANCED
		);
		
		
//		AnchorPane viewPane = new AnchorPane();
//		viewPane.setMaxHeight(Double.MAX_VALUE);
//		viewPane.setMaxWidth(Double.MAX_VALUE);
//		viewPane.setPrefHeight(200);
//		viewPane.setPrefWidth(200);
		
		
	//	mainPane.fillWidthProperty().set(true);
	//	mainPane.spacingProperty().set(8);
	//	mainPane.getChildren().add(viewPane);
		
		GlobeViewer globeView = new MercatorGlobeViewer();
		mainPane.getChildren().add(globeView.getScene());
		globeView.getScene().heightProperty().bind(mainPane.heightProperty());
		globeView.getScene().widthProperty().bind(mainPane.widthProperty());
		mainPane.setBackground(new Background(new BackgroundFill(Color.MIDNIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));

		stage.setScene(scene);
		stage.show();
		Thread t = new Thread(()->{
			while(!Thread.currentThread().isInterrupted()){
				try {
					Thread.sleep(1000);
					System.out.println(String.format(/*"viewPane: %sx%s,*/" mainPane: %sx%s, stage: %sx%s", 
						//	viewPane.widthProperty().get(),viewPane.heightProperty().get(), 
							mainPane.widthProperty().get(),mainPane.heightProperty().get(), 
							stage.widthProperty().get(),stage.heightProperty().get()
					));
				} catch (InterruptedException ex) {
					// exit
					break;
				}
				
			}
		});
		t.setDaemon(true);
		t.start();
		

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
