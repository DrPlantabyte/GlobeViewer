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

import javafx.application.ConditionalFeature;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Transform;

import java.util.*;
import java.util.function.Function;


/**
 * This GUI delegate class creates a JavaFX 3D scene with a textured sphere and 
 * directional light, and also provides a number of utilities for interacting 
 * with the sphere. To add a GlobeViewer to a GUI, create a pane in your GUI 
 * reserved for the GlobeViewer, fill its background with the desired color or 
 * image (dark colors are recommended), instantiate a GlobeViewer, and then 
 * add the SubScene returned by GlobeViwer.getScene() as a child to the 
 * reserved GUI pane.<p>
 * For example:<br><pre>
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

	public void start(final Stage stage) {
		AnchorPane mainPane = new AnchorPane();
		Scene scene = new Scene(
				mainPane,
				400, 400,
				true,
				SceneAntialiasing.BALANCED
		);
		GlobeViewer globeView = new GlobeViewer();
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
		globeView.lightDirectionVector().set(new Point3D(1, 0, -1));
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
</pre>
 * @author CCHall <a href="mailto:explosivegnome@yahoo.com">
 * explosivegnome@yahoo.com</a>
 */
public abstract class GlobeViewer {
	/**
	 * Converts a longitude-latitude coordinate into a 3D spacial coordinate.
	 * @param lonLat A Point2D instance whose X value is the longitude (in radians) and the Y value is the latitude (in radians). 
	 * @return A 3D point representing the corresponding XYZ coordinate on the surface of a sphere with radius 1
	 */
	public static Point3D lonLatToSurfacePoint(Point2D lonLat) {
		double sinLat = Math.sin(lonLat.getY());
		double sinLon = Math.sin(lonLat.getX());
		double cosLon = Math.cos(lonLat.getX());
		double cosLat = Math.cos(lonLat.getY());
		return new Point3D(cosLat * cosLon, sinLat, cosLat * -sinLon);
	}
	/**
	 * Converts a 3D spacial coordinate into a longitude-latitude coordinate.
	 * @param xyz A 3D point representing the XYZ coordinate on the surface of a sphere 
	 * @return  A Point2D instance whose X value is the longitude (in radians) and the Y value is the latitude (in radians) corresponding to the input coordinate.
	 */
	public static Point2D surfacePointToLonLat(Point3D xyz) {
		Point3D pt = xyz.normalize();
		return new Point2D(Math.atan2(-pt.getZ(), pt.getX()), Math.asin(pt.getY()));
	}
	/**
	 * This method subdivides each triangle face into four smaller triangles and 
	 * normalizes the vertices to the surface of the sphere. 
	 * @param input A <code>TriangleMesh</code> instance with the 
	 * <code>VertexFormat.POINT_NORMAL_TEXCOORD</code> vertex format (untextured 
	 * meshes are not supported).
	 * @param radius Radius of the globe (needed for proper maintenance of sphere shape)
	 * @return A new <code>TriangleMesh</code> instance.
	 */
	protected static final TriangleMesh subdivideTriangleMesh(TriangleMesh input, double radius){
		if(!VertexFormat.POINT_NORMAL_TEXCOORD.equals(input.getVertexFormat())){
			throw new UnsupportedOperationException(String.format("%s.%s(%s) only supports textured %s instances (must set VertexFormat to POINT_NORMAL_TEXCOORD)",GlobeViewer.class.getCanonicalName(), "subdivideTriangleMesh", TriangleMesh.class.getSimpleName(), TriangleMesh.class.getSimpleName()));
		}
		// get old faces
		final int indicesPerFace = 9;//input.getFaceElementSize();
		TriangleFace[] oldFaces = new TriangleFace[input.getFaces().size() / indicesPerFace];
		for(int i = 0; i < oldFaces.length; i++){
			int[] indices = new int[indicesPerFace];
			indices = input.getFaces().toArray(i*indicesPerFace, indices, indicesPerFace);
			oldFaces[i] = new TriangleFace(input, indices);
		}
		// generate new faces
		List<TriangleFace> newFaces = new ArrayList<>(oldFaces.length * 4);
		for(int i = 0; i < oldFaces.length; i++){
			TriangleFace t = oldFaces[i];
			// triangle where corners are in caps and opposing edges are lower case
			Vertex A = t.vertices[0];
			Vertex B = t.vertices[1];
			Vertex C = t.vertices[2];
			Vertex a = Vertex.midPoint(B, C).rescale(radius);
			Vertex b = Vertex.midPoint(A, C).rescale(radius);
			Vertex c = Vertex.midPoint(B, A).rescale(radius);
			newFaces.add(new TriangleFace(a,b,c));
			newFaces.add(new TriangleFace(A,c,b));
			newFaces.add(new TriangleFace(B,a,c));
			newFaces.add(new TriangleFace(C,b,a));
		}
		// done
		return TriangleFace.createTriangleMesh(newFaces);
	}

	/**
	 * This is a utility function to help generate <code>TriangleMesh</code>es. 
	 * It takes a series of Point3D objects and returns a corresponding array of 
	 * float coordinates
	 * @param pointsArray points to serialize
	 * @return float array of coordinates
	 */
	protected final static float[] pointsToFloatArray(Point3D... pointsArray) {
		float[] meshPoints = new float[pointsArray.length * 3];
		for(int i = 0; i < pointsArray.length; i++){
			Point3D e = pointsArray[i];
			meshPoints[i*3] = (float)e.getX();
			meshPoints[i*3+1] = (float)e.getY();
			meshPoints[i*3+2] = (float)e.getZ();
		}
		return meshPoints;
	}
	/**
	 * This is a utility function to help generate <code>TriangleMesh</code>es. 
	 * It takes a series of Point3D objects and returns a corresponding array of 
	 * float coordinates
	 * @param pointsArray points to serialize
	 * @return float array of coordinates
	 */
	protected final static float[] pointsToFloatArray(Point2D... pointsArray) {
		float[] meshPoints = new float[pointsArray.length * 2];
		for(int i = 0; i < pointsArray.length; i++){
			Point2D e = pointsArray[i];
			meshPoints[i*2] = (float)e.getX();
			meshPoints[i*2+1] = (float)e.getY();
		}
		return meshPoints;
	}
	/**
	 * Utility class used for generating TriangleMeshes.
	 */
	protected static final class TriangleFace {

		/**
		 * The 3 vertices of this face
		 */
		public final Vertex[] vertices = new Vertex[3];

		/**
		 * Constructs a TriangleFace object with the specified vertices.
		 *
		 * @param a A Vertex
		 * @param b A Vertex
		 * @param c A Vertex
		 */
		public TriangleFace(Vertex a, Vertex b, Vertex c) {
			vertices[0] = a;
			vertices[1] = b;
			vertices[2] = c;
		}

		/**
		 * Extracts a face from a TriangleMesh by its buffer indexes. Only
		 * <code>VertexFormat.POINT_NORMAL_TEXCOORD</code> meshes are supported, 
		 * therefore the faceIndicese must have a size of 9, with the integers
		 * in the order of V1 point buffer index, V1 normal buffer index, V1
		 * texture buffer index, V2 point buffer index, etc.
		 *
		 * @param mesh A TriangleMesh
		 * @param faceIndicese an array of 9 integers
		 */
		public TriangleFace(TriangleMesh mesh, int[] faceIndicese) {
			if (faceIndicese.length != mesh.getFaceElementSize()) {
				throw new IllegalArgumentException("Invalid argument. A triangle face will have "
						+ mesh.getFaceElementSize() + " indices.");
			}
			int[] pointIndices = new int[3];
			int[] normalIndices = new int[3];
			int[] texIndices = new int[3];
			for(int n = 0; n < 3; n++){
					pointIndices[n] = faceIndicese[n*3];
					normalIndices[n] = faceIndicese[n*3+1];
					texIndices[n] = faceIndicese[n*3+2];
			}
			for(int n = 0; n < 3; n++){
				vertices[n] = new Vertex(
						new Point3D(
						mesh.getPoints().get(pointIndices[n]*3), 
						mesh.getPoints().get(pointIndices[n]*3+1), 
						mesh.getPoints().get(pointIndices[n]*3+2)),
						new Point3D(
						mesh.getNormals().get(normalIndices[n]*3), 
						mesh.getNormals().get(normalIndices[n]*3+1), 
						mesh.getNormals().get(normalIndices[n]*3+2)), 
						new Point2D(
						mesh.getTexCoords().get(texIndices[n]*2), 
						mesh.getTexCoords().get(texIndices[n]*2+1))
				);
			}
		}
		/**
		 * This function generates and optimizes the point, normal, and texture buffers for the provided collection of TriangleFaces, and then creates a TriangleMes from the data.
		 * @param faceList The faces which make up the mesh
		 * @return Returns a TriangleMesh instance constructed from the list of faces
		 */
		public static TriangleMesh createTriangleMesh( Collection<TriangleFace> faceList){
			// use hash sets to remove duplicates
			Set<Point3D> points = new HashSet<>();
		Set<Point3D> normals = new HashSet<>();
		Set<Point2D> texas = new HashSet<>();
			Iterator<TriangleFace> iterator = faceList.iterator();
		while(iterator.hasNext()){
			TriangleFace t = iterator.next();
			for(int v = 0; v < 3; v++){
				points.add(t.vertices[v].point);
				normals.add(t.vertices[v].normal);
				texas.add(t.vertices[v].texture);
			}
		}
		// re-index the vertices
		Map<Point3D,Integer> pointIndices = new HashMap<>();
		Point3D[] pointsArray = points.toArray(new Point3D[0]);
		for(int i = 0; i < pointsArray.length; i++){
			pointIndices.put(
					pointsArray[i],
					i
			);
		}
		Map<Point3D,Integer> normalIndices = new HashMap<>();
		Point3D[] normalsArray = normals.toArray(new Point3D[0]);
		for(int i = 0; i < normalsArray.length; i++){
			normalIndices.put(
					normalsArray[i],
					i
			);
		}
		Map<Point2D,Integer> texIndices = new HashMap<>();
		Point2D[] texasArray = texas.toArray(new Point2D[0]);
		for(int i = 0; i < texasArray.length; i++){
			texIndices.put(
					texasArray[i],
					i
			);
		}
		iterator = faceList.iterator();
		int[][][] faces = new int[faceList.size()][3][3];
		for(int f = 0; f < faces.length; f++){
			TriangleFace t = iterator.next();
			for(int v = 0; v < 3; v++){
				faces[f][v][0] = pointIndices.get(t.vertices[v].point);
				faces[f][v][1] = normalIndices.get(t.vertices[v].normal);
				faces[f][v][2] = texIndices.get(t.vertices[v].texture);
			}
		}
		// serialize to floats
		float[] meshPoints = pointsToFloatArray(pointsArray);
		float[] meshNorms = pointsToFloatArray(normalsArray);
		float[] texCoords = pointsToFloatArray(texasArray);
		int[] allFaces = serialize(faces);
		// make new mesh
		TriangleMesh output = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
		output.getPoints().addAll(meshPoints);
		output.getNormals().addAll(meshNorms);
		output.getTexCoords().addAll(texCoords);
		output.getFaces().addAll(allFaces);
		// done
		return output;
		}
		
	private static final int[] serialize(int[][][] arr){
		int[] output = new int[arr.length * arr[0].length * arr[0][0].length];
		int i = 0;
		for(int x = 0; x < arr.length; x++){
			for(int y = 0; y < arr[0].length; y++){
				for(int z = 0; z < arr[0][0].length; z++){
					output[i++] = arr[x][y][z];
				}
			}
		}
		return output;
	}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final TriangleFace other = (TriangleFace) obj;
			if (!Arrays.deepEquals(this.vertices, other.vertices)) {
				return false;
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			int hash = Arrays.deepHashCode(this.vertices);
			return hash;
		}
	}
	/**
	 * Utility class for generating meshes
	 */
	protected static final class Vertex {
		/** 3D coordinate in space */
		public final Point3D point;
		/** Normal vector (must be unit length) */
		public final Point3D normal;
		/** Texture coordinate */
		public final Point2D texture;
		/**
		 * Construct a vertex with specified values
		 * @param point Point in 3D space
		 * @param normal Surface normal direction (absolute, not relative to the surface)
		 * @param texture X and Y coordinates in a texture image. X and Y must be in range of 0 to 1 inclusive
		 */
		public Vertex(Point3D point, Point3D normal, Point2D texture){
			this.point = point;
			this.normal = normal;
			this.texture = texture;
		}
		/**
		 * Computes the midpoint between two vertices
		 * @param v1 A Vertex object
		 * @param v2 Another Vertex object
		 * @return A new Vertex object halfway between the inputs
		 */
		public static Vertex midPoint(Vertex v1, Vertex v2){
			return new Vertex(
					new Point3D(
							0.5*(v1.point.getX() + v2.point.getX()),
							0.5*(v1.point.getY() + v2.point.getY()),
							0.5*(v1.point.getZ() + v2.point.getZ())
					),
					new Point3D(
							0.5*(v1.normal.getX() + v2.normal.getX()),
							0.5*(v1.normal.getY() + v2.normal.getY()),
							0.5*(v1.normal.getZ() + v2.normal.getZ())
					),
					new Point2D(
							0.5*(v1.texture.getX() + v2.texture.getX()),
							0.5*(v1.texture.getY() + v2.texture.getY())
					)
			);
		}
		/**
		 * Normalizes a vertex and then scales it.
		 * @param scalar The new magnitude of the Vertex
		 * @return A new Vertex
		 */
		public Vertex rescale(double scalar){
			Point3D newNormal = point.normalize();
			Point3D newPoint = newNormal.multiply(scalar);
			Point2D newTex = texture;
			return new Vertex(newPoint, newNormal, newTex);
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			int hash = 7;
			hash = 59 * hash + point.hashCode();
			hash = 59 * hash + normal.hashCode();
			hash = 59 * hash + texture.hashCode();
			return hash;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Vertex other = (Vertex) obj;
			if (!Objects.equals(this.point, other.point)) {
				return false;
			}
			if (!Objects.equals(this.normal, other.normal)) {
				return false;
			}
			if (!Objects.equals(this.texture, other.texture)) {
				return false;
			}
			return true;
		}
	}
	
	// GUI elements
	private Parent rootPane;
	private SubScene scene;
	private Shape3D globe;
	
	// textures
	
	private final SimpleObjectProperty<Image> texture_ambient_luminosity_color
			= new SimpleObjectProperty<>(null);
	
	private final SimpleObjectProperty<Image> texture_diffuse_illumination_color
			= new SimpleObjectProperty<>(null);
	
	private final SimpleObjectProperty<Image> texture_specular_reflection_color
			= new SimpleObjectProperty<>(null);
	
	private final SimpleObjectProperty<Image> texture_normal_vector_map
			= new SimpleObjectProperty<>(null);
	
	private final SimpleObjectProperty<javafx.geometry.Point3D> illumination_vector
			= new SimpleObjectProperty<>(new javafx.geometry.Point3D(0, 0, 1));
	
	private final SimpleObjectProperty<javafx.geometry.Point3D> rotation_vector
			= new SimpleObjectProperty<>(new javafx.geometry.Point3D(0, 0, 0));
	
	// number relationships
	
	private final DoubleProperty rotation_Y 
			= new javafx.beans.property.SimpleDoubleProperty(0.0);
	private final DoubleProperty rotation_X 
			= new javafx.beans.property.SimpleDoubleProperty(0.0);
	private final DoubleProperty rotation_Z 
			= new javafx.beans.property.SimpleDoubleProperty(0.0);
	private final DoubleProperty lightPos_X 
			= new javafx.beans.property.SimpleDoubleProperty(-2000.0);
	private final DoubleProperty lightPos_Y 
			= new javafx.beans.property.SimpleDoubleProperty(-500.0);
	private final DoubleProperty lightPos_Z 
			= new javafx.beans.property.SimpleDoubleProperty(-1000.0);
	private final double radius = 100;
	
	private static final double RADIANS_TO_DEGREES = 180 / Math.PI;
	/**
	 * Default constructor, instantiating without applying any textures
	 */
	public GlobeViewer(){
		this(null,null,null,null);
	}
	/**
	 * Creates a new globe viewer with the specified texture layers. Each layer 
	 * must be in the proper map projection for the specific implementation of this 
	 * implementation (e.g. a mercator projection when using a <code>MercatorGlobeViewer</code>). 
	 * Each texture layer is optional and may be replaced with <code>null</code>.
	 * @param diffuseTex An image mapping of the globe's surface colors. Can be null.
	 * @param ambientTex An image mapping of the globe's glow colors (should be a rather dark image). Can be null.
	 * @param specularTex An image mapping of the globe's shininess. Can be null.
	 * @param normalVector An image mapping of the globe's surface normals (aka "bump-map"). Can be null.
	 * @throws UnsupportedOperationException Thrown if 3D is not enabled on this 
	 * computer/JVM (usually indicates that either the graphics drivers were not 
	 * properly installed of that this system has an obsolete graphics card).
	 */
	public GlobeViewer(
			Image diffuseTex, 
			Image ambientTex, 
			Image specularTex, 
			Image normalVector
	){
		if(javafx.application.Platform.isSupported(ConditionalFeature.SCENE3D) == false){
			throw new UnsupportedOperationException("3D graphics is not supported in this JavaFX environment. Ensure that your graphics drivers are up to date.");
		}
		//
		if(diffuseTex == null){
			diffuseTex = new WritableImage(1,1);
			((WritableImage)diffuseTex).getPixelWriter().setColor(0, 0, Color.LIGHTGRAY);
		}
		if(ambientTex == null){
			ambientTex = new WritableImage(1,1);
			((WritableImage)ambientTex).getPixelWriter().setColor(0, 0, Color.BLACK);
		}
		if(specularTex == null){
			specularTex = new WritableImage(1,1);
			((WritableImage)specularTex).getPixelWriter().setColor(0, 0, Color.WHITE);
		}
		if(normalVector == null){
			normalVector = new WritableImage(1,1);
			((WritableImage)normalVector).getPixelWriter().setColor(0, 0, Color.color(0.5d, 0.5d, 1.0d));
		}
		texture_ambient_luminosity_color.set(ambientTex);
		texture_diffuse_illumination_color.set(diffuseTex);
		texture_specular_reflection_color.set(specularTex);
		texture_normal_vector_map.set(normalVector);
		
		//
		init();
	}
	/**
	 * Implementations must override this method to generate the globe model, with 
	 * appropriate normals and texture coordinates specified for each vertex.
	 * @param radius The radius of the globe.
	 * @return A Shape3D instance of the globe mesh model (typically a MeshVw 
	 * wrapping a TriangleMesh).
	 */
	protected abstract Shape3D createGlobeModel(double radius);
	
	
	private void init() {
		
		rootPane = buildScene();
		rootPane.setPickOnBounds(false);
		scene = new SubScene(rootPane,200,200,true,SceneAntialiasing.BALANCED);

		PerspectiveCamera camera = new PerspectiveCamera(true);
		NumericalFunctionProperty xPos = new NumericalFunctionProperty((Double d)->0.5*d);
		xPos.bind(scene.widthProperty());
		NumericalFunctionProperty yPos = new NumericalFunctionProperty((Double d)->0.5*d);
		yPos.bind(scene.heightProperty());
		camera.getTransforms().add(new javafx.scene.transform.Rotate(180,new Point3D(1,0,0)));
		camera.translateXProperty().bind(xPos);
		camera.translateYProperty().bind(yPos);
		camera.translateZProperty().set(1000);
		camera.setNearClip(0.1);
		camera.setFarClip(2000);
		camera.setFieldOfView(12.5);
		scene.setCamera(camera);
	}
	
	private Parent buildScene() {
		globe = createGlobeModel(radius);
		globe.setTranslateX(0);
		globe.setTranslateY(0);
		globe.setTranslateZ(0);

		PhongMaterial globeMaterial = new PhongMaterial();
		globeMaterial.diffuseMapProperty().bind(texture_diffuse_illumination_color);
		globeMaterial.bumpMapProperty().bind(texture_normal_vector_map);
		globeMaterial.specularMapProperty().bind(texture_specular_reflection_color);
		globeMaterial.selfIlluminationMapProperty().bind(texture_ambient_luminosity_color);
		globe.setMaterial(globeMaterial);
		
		javafx.scene.transform.Rotate axisRotation_X
				= new javafx.scene.transform.Rotate(0, new Point3D(1, 0, 0));
		javafx.scene.transform.Rotate axisRotation_Y
				= new javafx.scene.transform.Rotate(0, new Point3D(0, 1, 0));
		javafx.scene.transform.Rotate axisRotation_Z
				= new javafx.scene.transform.Rotate(0, new Point3D(0, 0, 1));
		axisRotation_X.angleProperty().bind(rotation_X);
		axisRotation_Y.angleProperty().bind(rotation_Y);
		axisRotation_Z.angleProperty().bind(rotation_Z);
		rotation_vector.addListener((ObservableValue<? extends Point3D> observable, Point3D oldValue, Point3D newValue) -> {
			rotation_X.set(newValue.getX() * RADIANS_TO_DEGREES);
			rotation_Y.set(newValue.getY() * RADIANS_TO_DEGREES);
			rotation_Z.set(newValue.getZ() * RADIANS_TO_DEGREES);
		});
		rotation_vector.set(new Point3D(0,0,0));
		globe.getTransforms().addAll(axisRotation_X, axisRotation_Z, axisRotation_Y);
		

		PointLight  light = new PointLight();
		light.setColor(Color.WHITE);
		javafx.scene.transform.Translate lightPosition
				= new javafx.scene.transform.Translate();
		lightPosition.xProperty().bind(lightPos_X);
		lightPosition.yProperty().bind(lightPos_Y);
		lightPosition.zProperty().bind(lightPos_Z);
		illumination_vector.addListener((ObservableValue<? extends Point3D> observable, Point3D oldValue, Point3D newValue) -> {
			Point3D position = newValue.normalize().multiply(25 * radius);
			lightPos_X.set(-position.getX());
			lightPos_Y.set(-position.getY());
			lightPos_Z.set(-position.getZ());
		});
		illumination_vector.set(new Point3D(1,0,-1));
		light.getTransforms().addAll(lightPosition);
		
		
//		Box xbox  = new Box(10,10,10), ybox  = new Box(10,10,10), zbox  = new Box(10,10,10);// debugging
//		xbox.translateXProperty().set(radius);// debugging
//		ybox.translateYProperty().set(radius);// debugging
//		zbox.translateZProperty().set(radius);// debugging
		
		StackPane g = new StackPane(
				globe,
				light
				//,xbox,ybox,zbox // debugging
		);
		return g;
	}
	
	/**
	 * Gets the binding for the texture layer representing the color map of the globe. 
	 * Binding a null Image will remove the layer from the globe renderer.
	 * @return An Image property
	 */
	public final ObjectProperty<Image> diffuseTextureProperty(){
		return texture_diffuse_illumination_color;
	}
	/**
	 * Gets the binding for the texture layer representing the night-time color map of the globe.
	 * Binding a null Image will remove the layer from the globe renderer.
	 * @return An Image property
	 */
	public final ObjectProperty<Image> ambientTextureProperty(){
		return texture_ambient_luminosity_color;
	}
	/**
	 * Gets the binding for the texture layer representing the shininess map of the globe.
	 * Binding a null Image will remove the layer from the globe renderer.
	 * @return An Image property
	 */
	public final ObjectProperty<Image> specularTextureProperty(){
		return texture_specular_reflection_color;
	}
	/**
	 * Gets the binding for the texture layer representing the surface normal map (bump-map) of the globe.
	 * Binding a null Image will remove the layer from the globe renderer.
	 * @return An Image property
	 */
	public final ObjectProperty<Image> normalVectorMapProperty(){
		return texture_normal_vector_map;
	}
	/**
	 * Gets the binding for the light direction vector. Note that this is the 
	 * direction towards which the light is shining, and positive x is to the right, 
	 * positive y is up, and positive z points towards the camera. Thus one should 
	 * set a negative z value in order for the side of the globe facing the viewer 
	 * to be illuminated
	 * @return A <code>javafx.geometry.Point3D</code> property.
	 */
	public final SimpleObjectProperty<javafx.geometry.Point3D> lightDirectionVector(){
		return illumination_vector;
	}
	/**
	 * Gets the rotation vector as a 3D vector, where-in the X, Y, and Z 
	 * properties specify the rotation around each axis, in radians 
	 * (counter-clockwise).
	 * @return A <code>javafx.geometry.Point3D</code> property.
	 */
	public final SimpleObjectProperty<javafx.geometry.Point3D> globeRotationVector(){
		return rotation_vector;
	}
	/**
	 * Use this method to add your own custom transforms to the globe.
	 * @param t A <code>javafx.scene.transform.Transform</code> instance (e.g. Rotate)
	 */
	public final void addGlobeTransform(Transform t){
		globe.getTransforms().add(t);
	}
	/**
	 * Removes a previously added custom transform. See {@link GlobeViewer#addGlobeTransform(javafx.scene.transform.Transform) addGlobeTransform(Transform)}.
	 * @param t A <code>javafx.scene.transform.Transform</code> instance (e.g. Rotate)
	 */
	public final void removeGlobeTransform(Transform t){
		globe.getTransforms().remove(t);
	}
	/**
	 * Gets the underlying 3D model of the globe.
	 * @return The Shape3D instance returned by {@link GlobeViewer#createGlobeModel(double) }.
	 */
	public final Shape3D getGlobe(){
		return globe;
	}
	/**
	 * Gets the <code>SubScene</code> instance of the globe 3D scene. This is the 
	 * top-level GUI element of the GlobeViewer and the specific element which you 
	 * should embed in your JavaFX GUI.
	 * @return The SubScene of the GlobeViewer
	 */
	public final SubScene getScene(){
		return scene;
	}
	
	/**
	 * Creates a darkened version of the input image.
	 * @param colorMap The input image
	 * @param intensity A number from 0 to 1 specifying how dark the output 
	 * image should be (0 = black, 1 = identical to input image)
	 * @return A new Image whose dimensions are identical to the input image
	 */
	public static Image createIlluminationMapFromColorMap(
			final Image colorMap, 
			final double intensity
	){
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
		return lumenImg;
	}

	/**
	 * This method converts between the spherical longitude-latitude coordinate and the pixel position in the texture map.
	 * <p>
	 * Longitude-latitude coordinates are in radians. A longitude-latitude coordinate of (0, 0) refers to a point on the equator on the prime meridian, a longitude-latitude coordinate of (π, 0) refers to a point on the equator opposite the prime meridian, a longitude-latitude coordinate of (0, π/2) refers to a point on the equator on the north pole, and a longitude-latitude coordinate of (0, -π/2) refers to a point on the equator on the south pole.<p>
	 * Relative pixel X-Y coordinates range from 0 to 1, such that multiplying the X relative coordinate by the image width results in the absolute pixel X coordinate on the image and multiplying the Y relative coordinate by the image height results in the absolute pixel Y coordinate on the image.
	 * @param lonLat A Point2D instance whose X value is the longitude (in radians) and the Y value is the latitude (in radians). 
	 * @return A Point2D instance whose X value is the relative pixel X coordinate [0-1] on the texture image and Y value is the relative pixel Y coordinate [0-1] on the texture image.
	 */
	public abstract Point2D convertLonLatToRelativePixelXY(Point2D lonLat);
	
	/**
	 * This method converts between the spherical longitude-latitude coordinate and the pixel position in the texture map.
	 * <p>
	 * Longitude-latitude coordinates are in radians. A longitude-latitude coordinate of (0, 0) refers to a point on the equator on the prime meridian, a longitude-latitude coordinate of (π, 0) refers to a point on the equator opposite the prime meridian, a longitude-latitude coordinate of (0, π/2) refers to a point on the equator on the north pole, and a longitude-latitude coordinate of (0, -π/2) refers to a point on the equator on the south pole.<p>
	 * Relative pixel X-Y coordinates range from 0 to 1, such that multiplying the X relative coordinate by the image width results in the absolute pixel X coordinate on the image and multiplying the Y relative coordinate by the image height results in the absolute pixel Y coordinate on the image.
	 * @param relXY A Point2D instance whose X value is the relative pixel X coordinate [0-1] on the texture image and Y value is the relative pixel Y coordinate [0-1] on the texture image.
	 * @return A Point2D instance whose X value is the longitude (in radians) and the Y value is the latitude (in radians). 
	 */
	public abstract Point2D convertRelativePixelXYToLonLat(Point2D relXY);
	
	
	/* package private */
	static double clamp(double d){
		return d - Math.floor(d);
	}
	
	/** A class for binding the function of a number to another number property */
	static final class NumericalFunctionProperty extends DoublePropertyBase{
		
		private final SimpleObjectProperty<Function<Double,Double>> function
				= new SimpleObjectProperty<>((Double d)->d);

		public SimpleObjectProperty<Function<Double,Double>> functionProperty(){
			return function;
		}
		
		public NumericalFunctionProperty(Function<Double,Double> function){
			super();
			this.functionProperty().set(function);
		}
		
		public NumericalFunctionProperty(){
			super();
		}
		
		@Override
		public double get() {
			return function.get().apply(super.get());
		}

		@Override
		public Object getBean() {
			return null;
		}

		@Override
		public String getName() {
			return "";
		}

		@Override
		public void bindBidirectional(Property<Number> other) {
			throw new UnsupportedOperationException("Cannot make bidirectional binding to "+this.getClass().getSimpleName());
		}


	}
}