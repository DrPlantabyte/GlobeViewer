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

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * This GlobeViewer implementation creates a globe from a sinusoidal map 
 * projection. Note that a sinusoidal projection will have the highest level of 
 * detail at longitude and latitude (0,0) and decrease in precision towards the 
 * edges and poles of the map.
 * @author CCHall <a href="mailto:explosivegnome@yahoo.com">
 * explosivegnome@yahoo.com</a>
 */
public class SinusoidalGlobeViewer extends GlobeViewer {

	/**
	 * Generates the globe model with appropriate normals and texture coordinates 
	 * specified for each vertex.
	 * @param radius The radius of the globe.
	 * @return A Shape3D instance of the globe mesh model (typically a MeshVw 
	 * wrapping a TriangleMesh).
	 */
	@Override
	protected Shape3D createGlobeModel(double radius) {
		// WARNING: low-level procedural mesh generation is MADNESS!
		final int radialSegments = 32;
		final int lateralSegments = 32;
		final double radialAngle = 2 * Math.PI / radialSegments;
		final double lateralAngle = Math.PI / lateralSegments;
		final double leftFudge = 1;
		final double rightFudge = 1;
		Point3D northNormal = new Point3D(0,1,0);
		Point2D northTex = new Point2D(0.5,0);
		Point3D southNormal = new Point3D(0,-1,0);
		Point2D southTex = new Point2D(0.5,1);
		
		final int numVertices = (radialSegments * (lateralSegments - 1))+2;
		final int numFaces = 2*(radialSegments * (lateralSegments - 1));
		Point3D[] normals = new Point3D[numVertices];
		Point2D[] texas = new Point2D[numVertices + (lateralSegments - 1)];
		Point3D[] points = new Point3D[numVertices];
		int[][][] faces = new int[numFaces][3][3];// [face][vertex][point/normal/tex]
		//
		int northPointIndex = normals.length - 2;
		int southPointIndex = normals.length - 1;
		normals[northPointIndex] = northNormal;
		normals[southPointIndex] = southNormal;
		texas[northPointIndex] = northTex;
		texas[southPointIndex] = southTex;
		//
		int findex = 0;
		final int fstop = lateralSegments - 1;
		final int texSwitch = radialSegments * (lateralSegments - 1);
		for(int r = 0; r < radialSegments; r++){
			double lon = r * radialAngle;
			int baseIndex = (r * (lateralSegments - 1));
			int nextBaseIndex = (((r+1)%radialSegments) * (lateralSegments - 1));
			int[][] northFace = new int[3][];
			northFace[0] = new int[] {northPointIndex, northPointIndex, northPointIndex};
			northFace[1] = new int[] {baseIndex, baseIndex, baseIndex};
			northFace[2] = new int[] {nextBaseIndex, nextBaseIndex, nextBaseIndex};
			faces[findex++] = northFace;
			for (int l = 1; l < lateralSegments; l++){ // exclude the poles
				double lat = ((0.5 * lateralSegments) - l) * lateralAngle;
				double rp = Math.cos(lat);
				double nx = Math.sin(lon) * rp;
				double ny = Math.sin(lat);
				double nz = Math.cos(lon) * rp;
				//
				int vertexIndex = baseIndex + (l - 1);
				texas[vertexIndex] = _convertLonLatToRelativePixelXY(new Point2D(lon,lat));
				if(r == 0){
					texas[vertexIndex] = new Point2D(Math.min(0, texas[vertexIndex].getX() * leftFudge), texas[vertexIndex].getY());
					double lon2 = 0.9990234375 * (2 * Math.PI);
					Point2D p = _convertLonLatToRelativePixelXY(new Point2D(lon2,lat));
					double x = Math.min(1.0, p.getX() * rightFudge);
					double y = p.getY();
					texas[texSwitch+l+1] = new Point2D(x, y);
				}
				normals[vertexIndex] = new Point3D(nx, ny, nz);
				//
				if(l < fstop){
					// faces
					int nextVertexIndex = nextBaseIndex + (l - 1);
					int v1 = vertexIndex;
					int v2 = vertexIndex+1;
					int v3 = nextVertexIndex;
					int v4 = nextVertexIndex+1;
					int[] fv1;
					int[] fv2;
					int[] fv3;
					int[] fv4;
					if (r == radialSegments - 1) {
						fv1 = new int[]{v1, v1, v1};
						fv2 = new int[]{v2, v2, v2};
						fv3 = new int[]{v3, v3, texSwitch+l+1};
						fv4 = new int[]{v4, v4, texSwitch+l+2};
					} else {
						fv1 = new int[]{v1, v1, v1};
						fv2 = new int[]{v2, v2, v2};
						fv3 = new int[]{v3, v3, v3};
						fv4 = new int[]{v4, v4, v4};
					}
					int[][] face1 = {fv1, fv2, fv3};
					int[][] face2 = {fv3, fv2, fv4};
					faces[findex++] = face1;
					faces[findex++] = face2;
				}
			}
			int ll = lateralSegments - 2;
			int[][] southFace = new int[3][];
			baseIndex += ll;
			nextBaseIndex += ll;
			southFace[0] = new int[] {southPointIndex, southPointIndex, southPointIndex};
			southFace[2] = new int[] {baseIndex, baseIndex, baseIndex};
			southFace[1] = new int[] {nextBaseIndex, nextBaseIndex, nextBaseIndex};
			faces[findex++] = southFace;
		}
		// make points using normals trick
		for(int i = 0; i < normals.length; i++){
			points[i] = normals[i].multiply(radius);
		}
		// save mesh
		float[] pointData = GlobeViewer.pointsToFloatArray(points);
		float[] normalData = GlobeViewer.pointsToFloatArray(normals);
		float[] texData = GlobeViewer.pointsToFloatArray(texas);
		int[] faceData = serialize(faces);
		
		TriangleMesh mesh = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
		mesh.getPoints().addAll(pointData);
		mesh.getNormals().addAll(normalData);
		mesh.getTexCoords().addAll(texData);
		mesh.getFaces().addAll(faceData);
		MeshView shape = new MeshView(mesh);
		shape.setDrawMode(DrawMode.FILL);
		return shape;
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
	
	public static Image convertMercatorToSinusoidal(final Image mercatorProjection, final int numThreads) throws InterruptedException{
		final double width = mercatorProjection.getWidth();
		final double height = mercatorProjection.getHeight();
		final int newHeight = (int)height;
		final int newWidth = 2 * newHeight;
		final double relYConversionFactor = 1.0 / (double)(newHeight - 1);
		final double relXConversionFactor = 1.0 / (double)(newWidth - 1);
		
		final double newLongitudeToOldRelXConversionFactor = 1.0 / (2.0 * Math.PI);
		final double newLatitudeToOldRelYConversionFactor = 1.0 / (Math.PI);
		
		final WritableImage sinusoidalProjection = new WritableImage(newWidth,newHeight);
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
					sinusoidalProjection.getPixelWriter().setArgb(x, y, 
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
						sinusoidalProjection.getPixelWriter().setArgb(x, y, 
								mercatorProjection.getPixelReader().getArgb(oldX, oldY));
					}
					return null;
				});
			}
			threadPool.invokeAll(taskList);
			threadPool.shutdown();
		}
		
		return sinusoidalProjection;
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

	/**
	 * This method converts a spherical longitude-latitude coordinate into a 
	 * texture X-Y pixel coordinate (where X and Y range from 0 to 1).
	 * @param relXY the texture pixel X and Y coordinates (range: (0,1] for each) stored in a 2D point
	 * @return longitude and latitude coordinates (in radians) stored in a 2D point
	 */
	@Override
	public Point2D convertRelativePixelXYToLonLat(Point2D relXY) {
		return _convertRelativePixelXYToLonLat(relXY);
	}/**
	 * This method converts a spherical longitude-latitude coordinate into a 
	 * texture X-Y pixel coordinate (where X and Y range from 0 to 1).
	 * @param lonLat longitude and latitude coordinates (in radians) stored in a 2D point
	 * @return the texture pixel X and Y coordinates (range: (0,1] for each) stored in a 2D point
	 */
	private static final Point2D _convertLonLatToRelativePixelXY(Point2D lonLat) {
		final double oneOverPi = 1.0 / Math.PI;
		final double oneOverTwoPi = 0.5 / Math.PI;
		double y = 0.5 - (oneOverPi * lonLat.getY());
		double width = Math.cos(lonLat.getY());
		double offset = 0.5 * (1.0 - width);
		double x = offset + width * clamp(lonLat.getX() * oneOverTwoPi);
		return new Point2D(x,y);
	}

	/**
	 * This method converts a spherical longitude-latitude coordinate into a 
	 * texture X-Y pixel coordinate (where X and Y range from 0 to 1).
	 * @param relXY the texture pixel X and Y coordinates (range: (0,1] for each) stored in a 2D point
	 * @return longitude and latitude coordinates (in radians) stored in a 2D point
	 */
	private static final Point2D _convertRelativePixelXYToLonLat(Point2D relXY) {
		final double twoPi = 2.0 * Math.PI;
		double lat = (0.5 - clamp(relXY.getY())) * Math.PI;
		double width = Math.cos(lat);
		double offset = 0.5 * (1.0 - width);
		double limit = 1.0 - offset;
		double x = clamp(relXY.getX());
		double lon = 0;
		if(x > offset && x < limit){
			lon = twoPi * (x - offset) / width;
		}
		if(lon > Math.PI) lon -= twoPi;
		return new Point2D(lon,lat);
	}
}
