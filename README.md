# GlobeViewer
A JavaFX control for displaying textured 3D spheres using one of several map projection techniques. Supports mouse interaction and provides an API for adding your own custom insteraction handlers. You can even get the location of a mouse click (see *Coordinate Picking* below).

## Map Projections
GlobeViewer supports **Mercator** (aka "uv-mapping"), **Sinusoidal**, **Cube**, and **Dymaxion** (aka "icosahedron") map projections. Of these, the *Dymaxion* map will give you the best quality appearance, but *Mercator* is more commonly used.

## Example Usage
The following is a complete java program (not including the texture files earth-tex_*.png) using the GlobeViewer
```java
import hall.collin.christopher.javafx.globeviewer.*;
import hall.collin.christopher.javafx.globeviewer.interaction.*;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TestApp extends Application {
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
		
		// load bundled image resources (you will have to supply your own images)
		globeView.diffuseTextureProperty().set(new Image(
						TestApp.class.getResource(DIFFUSE_MAP).toExternalForm()
				));
		globeView.normalVectorMapProperty().set(new Image(
						TestApp.class.getResource(NORMAL_MAP).toExternalForm()
				));
		globeView.specularTextureProperty().set(new Image(
						TestApp.class.getResource(SPECULAR_MAP).toExternalForm()
				));
		globeView.ambientTextureProperty().set(new Image(
						TestApp.class.getResource(AMBIENT_MAP).toExternalForm()
				));
		globeView.lightDirectionVector().set(new Point3D(1, 0, -1));
		
		InteractionHandler spinner = new AutoSpinWithMouse();
		spinner.applyTo(globeView);

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

```

## Coordinate Picking
The following code example showshow to get the longitude-latitude coordinate of a right-button mouse click on the globe:
```java
globeView.getScene().addEventHandler(MouseEvent.ANY, (MouseEvent event)->{
	if(event.isSecondaryButtonDown() && event.getEventType() == MouseEvent.MOUSE_PRESSED) {
		PickResult pickResult = event.getPickResult();
		if (pickResult == null) {
			return;
		}
		Node picked = pickResult.getIntersectedNode();
		if (picked == globeView.getGlobe()) {
			Point2D pickCoord = pickResult.getIntersectedTexCoord();
			Point2D lonLat = globeView.convertRelativePixelXYToLonLat(pickCoord);
			System.out.println(String.format("User clicked on (%s deg. Longitude, %s deg. Latitude)", 
					  (lonLat.getX())*180/Math.PI,
					  (lonLat.getY())*180/Math.PI));
		}
	}
});
```

## Building this Library
This is a Gradle Java project. Simply run *gradle build* to build this library (which will appear in the *build/jar* folder. To run the included test app, use *gradle integrationTest*.
