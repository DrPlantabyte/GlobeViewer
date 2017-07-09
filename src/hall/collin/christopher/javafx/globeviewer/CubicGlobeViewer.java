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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import javafx.beans.NamedArg;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

/**
 * This GlobeViewer implementation creates a globe from a cubic map projection. 
 * The two of the cube's faces represent the poles and the other four wrap 
 * around the equator.
 * @author CCHall <a href="mailto:explosivegnome@yahoo.com">
 * explosivegnome@yahoo.com</a>
 */
public class CubicGlobeViewer extends GlobeViewer{
	
	private static final int SUBDIVISIONS = 3;

	/**
	 * Generates the globe model with appropriate normals and texture coordinates 
	 * specified for each vertex.
	 * @param radius The radius of the globe.
	 * @return A Shape3D instance of the globe mesh model (typically a MeshVw 
	 * wrapping a TriangleMesh).
	 */
	@Override
	protected Shape3D createGlobeModel(double radius) {
/*
ASCII Art Time!
  o-----o
 /  o  /|
o-----o |
| \ / |o|
|  o  | o
| / \ |/
o-----o
Cube has 8 corners and 6 faces.
Typical CG would use 2 triangles per face,
but here I use 4 triangles per face to reduce 
distortions. Each face therefore is a fan of 
4 triangles around the center of the face.
*/
		TriangleMesh baseMesh = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
		final double piOverFour = 0.25*Math.PI;
		final double piOverTwo = 0.5*Math.PI;
		// using sneaky trick to use same calculations for normals as for points
		Point3D[] normals = new Point3D[4+4+6]; // top four + bottom four + faces
		for(int i = 0; i < 4; i++){
			final double lat = piOverFour;
			final double lon = i * piOverTwo;
			normals[i] = GlobeViewer.lonLatToSurfacePoint(new Point2D(lon,lat));
			normals[4+i] = GlobeViewer.lonLatToSurfacePoint(new Point2D(lon,-lat));
			normals[9+i] = GlobeViewer.lonLatToSurfacePoint(new Point2D(lon+piOverFour,0)); // equatorial faces
		}
		normals[8] = new Point3D(0,1,0); // north pole
		normals[13] = new Point3D(0,-1,0); // south pole
		Point3D[] points = new Point3D[normals.length];
		for(int i = 0; i < points.length; i++){points[i] = normals[i].multiply(radius);}
		Point2D[] texCoords = {
				// texture coordinates require special care
				// north coordinates
				new Point2D(0,0.4921875),
				new Point2D(0.25,0.4921875),
				new Point2D(0.25,0),
				new Point2D(0,0),
				// south coordinates
				new Point2D(0.75,0),
				new Point2D(1.0,0),
				new Point2D(1.0,0.4921875),
				new Point2D(0.75,0.4921875),
				// face centers
				new Point2D(0.125,0.25), // north pole
				new Point2D(0.125,0.75),
				new Point2D(0.375,0.75),
				new Point2D(0.625,0.75),
				new Point2D(0.875,0.75),
				new Point2D(1.0-0.125,0.25),// south pole
				// equatorial north points
				new Point2D(0,0.5078125),
				new Point2D(0.25,0.5078125),
				new Point2D(0.5,0.5078125),
				new Point2D(0.75,0.5078125),
				new Point2D(1.0,0.5078125),
				// equatorial south points
				new Point2D(0,1),
				new Point2D(0.25,1),
				new Point2D(0.5,1),
				new Point2D(0.75,1),
				new Point2D(1.0,1),
		};
		// generate faces (as mesh index references)
		int[][] faces = new int[6*4][3];
		for(int i = 0; i < 4; i++){
			faces[i][0] = 8;// north pole
			faces[i][1] = i;
			faces[i][2] = (i+1)%4;
			faces[4+i][0] = 13;// south pole
			faces[4+i][2] = 4+i;
			faces[4+i][1] = 4+((i+1)%4);
			// equatorial faces
			int center = 9+i;
			int topLeft = i;
			int bottomLeft = 4+i;
			int topRight = (i+1)%4;
			int bottomRight = topRight+4;
			int[] faceCorners = {topLeft, bottomLeft, bottomRight, topRight};
			for(int n = 0; n < 4; n++){
				int faceIndex = 8 + i*4 + n;
				faces[faceIndex][0] = center;
				faces[faceIndex][1] = faceCorners[n];
				faces[faceIndex][2] = faceCorners[(n+1)%4];
			}
		}
		// serialize data
		float[] pointData = GlobeViewer.pointsToFloatArray(points);
		float[] normalData = GlobeViewer.pointsToFloatArray(normals);
		float[] texData = GlobeViewer.pointsToFloatArray(texCoords);
		int[] faceData = new int[faces.length*faces[0].length*3];
		int findex = 0;
		for(int f = 0; f < faces.length; f++){
			for(int n = 0; n < 3; n++){
				int i = faces[f][n];
				for(int z = 0; z < 3; z++){
					// note that the point, normal, and texture buffers all have identical indices (almost, textures are special)
					faceData[findex] = i;
					findex++;
				}
			}
		}
		// texture handling: custom adjustments for textures to prevent wrapping artifacts
		for(int ef = 0; ef < 4; ef++){
			// correct equatorial faces
			int baseIndex = 8+(ef*4);
			int centerIndex = 9+ef;// center of face
			int[] circumferenceIndices = {14+ef, 19+ef, 19+ef+1, 14+ef+1};
			for(int r = 0; r < 4; r++){
				int index = baseIndex + r;
				int dataIndex = index * 9;
				int tex1Index = dataIndex+2;
				int tex2Index = dataIndex+5;
				int tex3Index = dataIndex+8;
				faceData[tex1Index] = centerIndex;
				faceData[tex2Index] = circumferenceIndices[r];
				faceData[tex3Index] = circumferenceIndices[(r+1)%4];
			}
			
		}
		
		// build the mesh
		baseMesh.getPoints().addAll(pointData);
		baseMesh.getNormals().addAll(normalData);
		baseMesh.getTexCoords().addAll(texData);
		baseMesh.getFaces().addAll(faceData);
		//
		TriangleMesh finalMesh = baseMesh;
		for(int i = 0; i < SUBDIVISIONS; i++){
			finalMesh = GlobeViewer.subdivideTriangleMesh(finalMesh, radius);
		}
		MeshView shape = new MeshView(finalMesh);
		shape.setDrawMode(DrawMode.FILL);
		return shape;
	}
	
	/**
	 * Converts a mercator projection map (aka UV map) into a cuboid projection texture image
	 * @param mercatorProjection The input mercator projection, where-in the x-axis 
	 * of the image represents longitude and the y-axis of the image represents latitude.
	 * @param numThreads Number of threads to use (parallel processing). 
	 * @return A cubic projection texture image suitable for use as a texture layer 
	 * for this GlobeViewer class.
	 * @throws InterruptedException Thrown if multiple threads are specified and then 
	 * this task is interrupted while waiting for one or more parallel threads to finish.
	 */
	public static Image convertMercatorToCubic(final Image mercatorProjection, final int numThreads) throws InterruptedException{
		final double width = mercatorProjection.getWidth();
		final double height = mercatorProjection.getHeight();
		final int newHeight = (int)height;
		final int newWidth = 2 * newHeight;
		final double relYConversionFactor = 1.0 / (double)(newHeight - 1);
		final double relXConversionFactor = 1.0 / (double)(newWidth - 1);
		
		final double newLongitudeToOldRelXConversionFactor = 1.0 / (2.0 * Math.PI);
		final double newLatitudeToOldRelYConversionFactor = 1.0 / (Math.PI);
		
		final WritableImage cuboidProjection = new WritableImage(newWidth,newHeight);
		if(numThreads <= 1){
			for(int h = 0; h < newHeight; h++){
				final int y = h;
				final double relY = y * relYConversionFactor;
				for(int x = 0; x < newWidth; x++){
					double relX = x * relXConversionFactor;
					Point2D lonLat = _convertRelativePixelXYToLonLat(new Point2D(relX, relY));
					int oldX = (int)(clamp(lonLat.getX() * newLongitudeToOldRelXConversionFactor) 
							* width);
					int oldY = (int)(clamp(lonLat.getY() * newLatitudeToOldRelYConversionFactor) 
							* height);
					cuboidProjection.getPixelWriter().setArgb(x, y, 
							mercatorProjection.getPixelReader().getArgb(oldX, oldY));
				}
			}
		} else {
			final ExecutorService threadPool = java.util.concurrent.Executors.newFixedThreadPool(numThreads);
			List<Callable<Object>> taskList = new ArrayList<>(newHeight);
			for(int h = 0; h < newHeight; h++){
				final int y = h;
				final double relY = y * relYConversionFactor;
				taskList.add(()->{
					for(int x = 0; x < newWidth; x++){
						double relX = x * relXConversionFactor;
						Point2D lonLat = _convertRelativePixelXYToLonLat(new Point2D(relX, relY));
						int oldX = (int)(clamp(lonLat.getX() * newLongitudeToOldRelXConversionFactor) 
								* width);
						int oldY = (int)(clamp(lonLat.getY() * newLatitudeToOldRelYConversionFactor) 
								* height);
						cuboidProjection.getPixelWriter().setArgb(x, y, 
								mercatorProjection.getPixelReader().getArgb(oldX, oldY));
					}
					return null;
				});
			}
			threadPool.invokeAll(taskList);
			threadPool.shutdown();
		}
		
		return cuboidProjection;
	}
	/**
	 * Converts a series of 6 cube face images into a single texture image.
	 * @param faces A data container holding the face images
	 * @return A single image created from stitching together the provided faces.
	 */
	public static Image convertCubeFacesToCuboid(CubeFaces faces){
		if(faces.back == null || faces.bottom == null || faces.front == null 
				|| faces.left == null || faces.right == null || faces.top == null){
			throw new IllegalArgumentException(CubicGlobeViewer.class.getSimpleName() 
					+ ".convertCubeFacesToCuboid("+faces.getClass().getSimpleName()
					+") requires that all faces on "+faces.getClass().getSimpleName()
					+" be valid Image instances. One of more of them is null", 
					new NullPointerException());
		}
		final int size = (int)faces.front.getHeight();
		WritableImage img = new WritableImage(4*size,2*size);
		//
		pasteImage(faces.top, img,         0, 0, size);
		pasteImage(faces.bottom, img, 3*size, 0, size);
		pasteImage(faces.front, img,     0, size, size);
		pasteImage(faces.right, img,  size, size, size);
		pasteImage(faces.back, img, 2*size, size, size);
		pasteImage(faces.left, img, 3*size, size, size);
		return img;
	}
	private static void pasteImage(Image src, WritableImage dest, int xOffset, int yOffset, int size){
		for(int y = 0; y < size; y++){
			int oldY = (int)((double)y/(double)size*src.getHeight());
			for(int x = 0; x < size; x++){
				int oldX = (int)((double)x/(double)size*src.getWidth());
				dest.getPixelWriter().setArgb(
						x+xOffset, y+yOffset, 
						src.getPixelReader().getArgb(oldX, oldY)
				);
			}
		}
	}
	/**
	 * This data container class is used for generating a cubic projection from 6 images.
	 */
	public static final class CubeFaces{
		/** The front of the cube */
		public Image front;
		/** The top of the cube, such that the bottom edge aligns with the top of 
		 * the front of the cube */
		public Image top;
		/** The bottom of the cube, such that the top edge aligns with the bottom of 
		 * the front of the cube */
		public Image bottom;
		/** The left side of the cube (from the perspective of someone looking at the front of the cube) */
		public Image left;
		/** The right side of the cube (from the perspective of someone looking at the front of the cube) */
		public Image right;
		/** The back of the cube */
		public Image back;
		/**
		 * 
		 * @param left The left side of the cube (from the perspective of someone looking 
		 * at the front of the cube)
		 * @param front The front of the cube
		 * @param right The right side of the cube (from the perspective of someone looking 
		 * at the front of the cube)
		 * @param back The back of the cube
		 * @param top The top of the cube, such that the bottom edge aligns with the top of 
		 * the front of the cube
		 * @param bottom The bottom of the cube, such that the top edge aligns with the 
		 * bottom of the front of the cube
		 */
		public CubeFaces(
				  @NamedArg("left") Image left,
				  @NamedArg("front") Image front,
				  @NamedArg("right") Image right,
				  @NamedArg("back") Image back,
				  @NamedArg("top") Image top,
				  @NamedArg("bottom") Image bottom
		){
			this.top = top;
			this.front = front;
			this.back = back;
			this.left = left;
			this.right = right;
			this.bottom = bottom;
		}
		public CubeFaces(){
			this.top = null;
			this.front = null;
			this.back = null;
			this.left = null;
			this.right = null;
			this.bottom = null;
		}
	}
	

	/**
	 * This method converts a spherical longitude-latitude coordinate into a 
	 * texture X-Y pixel coordinate (where X and Y range from 0 to 1).
	 * @param lonLat longitude and latitude coordinates (in radians) stored in a 2D point
	 * @return the texture pixel X and Y coordinates (range: (0,1] for each) stored in a 2D point
	 */
	@Override
	public Point2D convertLonLatToRelativePixelXY(Point2D lonLat) {
		return _convertLonLatToRelativePixelXY(lonLat);
	}
	private static Point2D _convertLonLatToRelativePixelXY(Point2D lonLat){
		final double piOverFour = 0.25 * Math.PI;
		final double piOverTwo = 0.5 * Math.PI;
		final double threePiOverFour = 0.75 * Math.PI;
		final double minusPiOverFour = -0.25 * Math.PI;
		final double fourOverPi = 4.0 / Math.PI;
		final double oneOverTwoPi = 1.0 / (2.0 * Math.PI);
		final double twoOverPi = 2.0 / Math.PI;
		final double lon = lonLat.getX();
		final double lat = lonLat.getY();
		double x, y;
		if(minusPiOverFour < lat && lat < piOverFour){
			// equatorial faces
			double dy = lat * fourOverPi; // -1 to 1
			y = 0.75 - (dy * 0.25);
			
			double lin = clamp(lon * oneOverTwoPi);
			double dlon = (clamp(lon * twoOverPi) - 0.5) * piOverTwo;
			double dx = Math.tan(dlon);
			double xOffset = (((int)(lin * 4) % 4) + 0.5) * 0.25;
			x = clamp(dx*0.125 + xOffset);
		} else {
			final double yOffset = 0.25;
			final double latSign, xOffset, angleOffset = threePiOverFour;
			
			if(lat > 0){
				// north pole
				//  _ _
				// | . |
				// |/ _|
				// 0 longitude (increases counter-clockwise)
				latSign = 1;
				xOffset = 0.125;
			} else {
				// south pole
				// 0_longitude (increases clockwise)
				// |\. |
				// |_ _|
				// 
				latSign = -1;
				xOffset = 0.875;
			}
			double angle = latSign * (lon - angleOffset);
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);
			double sec = Math.abs(1.0 / cos);
			double csc = Math.abs(1.0 / sin);
			double hmax = Math.min(sec, csc);
			double fromPole = (piOverTwo - (latSign * lat)) * fourOverPi; // 0 to 1
			double h = fromPole * hmax;
			x = cos * h * 0.125 + xOffset;
			y = yOffset - sin * h * 0.25;
		}
		return new Point2D( x, y);
	}
	
	private static double dist(double dx, double dy){
		return Math.sqrt(dx*dx + dy*dy);
	}

	/**
	 * This method converts a spherical longitude-latitude coordinate into a 
	 * texture X-Y pixel coordinate (where X and Y range from 0 to 1).
	 * @param relXY the texture pixel X and Y coordinates (range: (0,1] for each) stored in a 2D point
	 * @return longitude and latitude coordinates (in radians) stored in a 2D point
	 */
	@Override
	public Point2D convertRelativePixelXYToLonLat(Point2D relXY) {
		return _convertRelativePixelXYToLonLat(relXY);
	
	}
	private static Point2D _convertRelativePixelXYToLonLat(Point2D relXY) {
		final double twoPi = 2.0 * Math.PI;
		final double piOverTwo = 0.5 * Math.PI;
		final double piOverFour = 0.25 * Math.PI;
		final double threePiOverFour = 0.75 * Math.PI;
		final double x = relXY.getX();
		final double y = relXY.getY();
		double lon, lat;
		if(y >= 0.5){
			// equatorial region
			double dy = (0.75 - y) * 4.0;
			lat = dy * piOverFour;
			
			double xprime = clamp(x);
			double faceOffset = (((int)(xprime * 4)) + 1.5) * piOverTwo;
			double dx = (clamp(4*xprime) - 0.5) * 2;
			lon = Math.atan2(-1, dx) + faceOffset;
		} else {
			if(0.25 <= x && x < 0.75) return Point2D.ZERO;
			final double xOffset, latSign, angleOffset = threePiOverFour;
			double dx, dy;
			dy = (0.25 - y) * 4; // range from -1 to 1
			if (x < 0.5){
				// north pole
				//  _ _
				// | . |
				// |/ _|
				// 0 longitude (increases counter-clockwise)
				xOffset = 0;
				latSign = 1;
				
				dx = (x - xOffset - 0.125) * 8.0; // range from -1 to 1
			} else {
				// south pole
				// 0_longitude (increases clockwise)
				// |\. |
				// |_ _|
				// 
				xOffset = 0.75;
				latSign = -1;
				
				dx = (x - xOffset - 0.125) * 8.0; // range from -1 to 1
			}
			
			double angle = Math.atan2(dy, dx);
			lon = angle * latSign + angleOffset;
			
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);
			double hmaxInverse = Math.max(Math.abs(cos), Math.abs(sin));
			double h = dist(dx, dy) * hmaxInverse;
			lat = latSign * (piOverTwo - (piOverFour * h));
		}
		if(lon > Math.PI) lon -= twoPi;
		return new Point2D( lon, lat );
	}
	
}

