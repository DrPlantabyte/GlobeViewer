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

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;

/**
 * This GlobeViewer implementation creates a globe from a mercator map 
 * projection. A mercator map projection uses the x-axis of the image to represent 
 * longitude and the y-axis to represent latitude. The map becomes increasingly 
 * distorted near the poles, though the globe counters this distortion (except 
 * in the regions very close to the poles).
 * @author CCHall <a href="mailto:explosivegnome@yahoo.com">
 * explosivegnome@yahoo.com</a>
 */
public class DymaxionGlobeViewer extends GlobeViewer{
	private final int subdivisions = 3;
	private static final double icoAngle; // the angle between two vertices on an icosahedron
	private static final double icoAngleInverse;
	static{
		double root5 = Math.sqrt(5.0);
		double phi = 0.5*(1 + root5);
		Point3D ref0 = Point3D.ZERO;
		Point3D ref1 = new Point3D(0,1,phi);
		Point3D ref2 = new Point3D(0,1,-phi);
		double a = ref0.distance(ref1);
		double b = ref0.distance(ref2);
		double c = ref1.distance(ref2);
		icoAngle = Math.acos(Math.sqrt((c*c + b*b - c*c)/(2*a*b)));
		icoAngleInverse = 1.0 / icoAngle;
	}
	private static final double fiveOverTwoPi = 5d / (2*Math.PI);
	private static final double oneOverRoot3 = 1.0 / Math.sqrt(3.0);
	private static final double oneOverTwoPi = 1.0 / (2.0 * Math.PI);
	private static final double piOverFive = Math.PI / 5;
	private static final double piOverTwo = 0.5 * Math.PI;
	private static final double twoPi = 2.0 * Math.PI;
	private static final double twoPiOverFive = 2 * Math.PI / 5;
	private static final double triangleHalfWidth = 1.0 / 11.0;
	private static final double triangleHeight = 1.0 / 3.0;
	private static final double triangleHeightInverse = 1.0 / triangleHeight;
	private static final double triangleWidth = triangleHalfWidth * 2;
	private static final double upperLatitude = piOverTwo - icoAngle;
	private static final double upperY = triangleHeight;
	private static final double edgeLat = piOverTwo / 3.0 ;
	private static final double lowerLatitude = -1 * upperLatitude;
	private static final double lowerY = 1.0 - triangleHeight;
	private static final double equatorialYRangeInverse = 1.0 / (upperY - lowerY);
	private static final double inverseInnerLatitude = 1.0 / (upperLatitude - lowerLatitude);

	private static long numPolygons(long subdivs){
		return 20L * (1L << (2L * subdivs));
	}
	/**
	 * Generates the globe model with appropriate normals and texture coordinates 
	 * specified for each vertex.
	 * @param radius The radius of the globe.
	 * @return A Shape3D instance of the globe mesh model (typically a MeshVw 
	 * wrapping a TriangleMesh).
	 */
	@Override
	protected Shape3D createGlobeModel(double radius) {
		//
		List<TriangleFace> faces = new ArrayList<>(20);
		Point3D northPole = new Point3D(0,1,0);
		Point3D southPole = new Point3D(0,-1,0);
		double rPrime = Math.sin(icoAngle);
		double y3d = 1 - Math.sin(icoAngle);
		for(int i = 0; i < 5; i++){
			int nexti = i + 1;
			int nexti_wrapped = nexti % 5;
			
			double nlon = i * twoPiOverFive;
			double nextNLon = nexti_wrapped * twoPiOverFive;
			// northern 5
			Point3D n1 = (new Point3D(
					rPrime * Math.cos(nlon), 
					y3d, 
					-rPrime * Math.sin(nlon) )).normalize();
			Point3D n2 = (new Point3D(
					rPrime * Math.cos(nextNLon), 
					y3d, 
					-rPrime * Math.sin(nextNLon) )).normalize();
			Point2D n1uv = new Point2D(i * triangleWidth, triangleHeight);
			Point2D n2uv = new Point2D(Math.min(1,nexti * triangleWidth), triangleHeight);
			faces.add(new TriangleFace(
					new Vertex(northPole.multiply(radius), northPole, 
							new Point2D(i * triangleWidth + triangleHalfWidth, 0)),
					new Vertex(n1.multiply(radius), n1, n1uv),
					new Vertex(n2.multiply(radius), n2, n2uv)
			));
			// southern 5
			double slon = (i+0.5) * twoPiOverFive;
			double nextSLon = (nexti_wrapped+0.5) * twoPiOverFive;
			Point3D s1 = (new Point3D(
					rPrime * Math.cos(slon), 
					-y3d, 
					-rPrime * Math.sin(slon) )).normalize();
			Point3D s2 = (new Point3D(
					rPrime * Math.cos(nextSLon), 
					-y3d, 
					-rPrime * Math.sin(nextSLon) )).normalize();
			Point2D s1uv = new Point2D(i * triangleWidth + triangleHalfWidth, 1 - triangleHeight);
			Point2D s2uv = new Point2D(
					Math.min(1,nexti * triangleWidth + triangleHalfWidth), 
					1 - triangleHeight);
			faces.add(new TriangleFace(
					new Vertex(southPole.multiply(radius), southPole, 
							new Point2D(nexti * triangleWidth, 1)),
					new Vertex(s2.multiply(radius), s2, s2uv),
					new Vertex(s1.multiply(radius), s1, s1uv)
			));
			// equatorial 10
			faces.add(new TriangleFace(
					new Vertex(n2.multiply(radius), n2, n2uv),
					new Vertex(n1.multiply(radius), n1, n1uv),
					new Vertex(s1.multiply(radius), s1, s1uv)
			));
			faces.add(new TriangleFace(
					new Vertex(n2.multiply(radius), n2, n2uv),
					new Vertex(s1.multiply(radius), s1, s1uv),
					new Vertex(s2.multiply(radius), s2, s2uv)
			));
		}
		//
		TriangleMesh mesh = TriangleFace.createTriangleMesh(faces);
		for(int i = 0; i < subdivisions; i++){
			mesh = GlobeViewer.subdivideTriangleMesh(mesh, radius);
		}
		MeshView shape = new MeshView(mesh);
		shape.setDrawMode(DrawMode.FILL);
		return shape;
	}
	
	
	/**
	 * Converts a mercator projection map (aka UV map) into a dymaxion projection texture image
	 * @param mercatorProjection The input mercator projection, where-in the x-axis 
	 * of the image represents longitude and the y-axis of the image represents latitude.
	 * @return A dymaxion (aka icosahedron) projection texture image suitable for use as a texture layer 
	 * for this GlobeViewer class.
	 * */
	public static Image convertMercatorToDymaxion(Image mercatorProjection){
		final double rootThreeOverTwo = 0.5*Math.sqrt(3.0);
		final int triangleWidth = (int)((1.0 / 11d) * (mercatorProjection.getWidth() * 1.1));
		final int triangleHeight = (int)(triangleWidth * rootThreeOverTwo);
		final int imgWidth = 11 * triangleWidth;
		final int imgHeight = 3 * triangleHeight;
		
		WritableImage dymaxionProjection = new WritableImage(imgWidth, imgHeight);
		
		final double relXConversionFactor = 1.0 / (double)(imgWidth - 1);
		final double relYConversionFactor = 1.0 / (double)(imgHeight - 1);
		final double newLongitudeToOldRelXConversionFactor = 1.0 / (2.0 * Math.PI);
		final double newLatitudeToOldRelYConversionFactor = 1.0 / (Math.PI);
		
		for(int h = 0; h < imgHeight; h++){
			final int y = h;
			final double relY = y * relYConversionFactor;
			for(int x = 0; x < imgWidth; x++){
				double relX = x * relXConversionFactor;
				Point2D lonLat = _convertRelativePixelXYToLonLat(new Point2D(relX, relY));
				int oldX = (int)(clamp(lonLat.getX() * newLongitudeToOldRelXConversionFactor) 
						* mercatorProjection.getWidth());
				int oldY = (int)(clamp(lonLat.getY() * newLatitudeToOldRelYConversionFactor) 
						* mercatorProjection.getHeight());
				dymaxionProjection.getPixelWriter().setArgb(x, y, 
						mercatorProjection.getPixelReader().getArgb(oldX, oldY));
			}
		}
		
		return dymaxionProjection;
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
	}
	private static Point2D _convertLonLatToRelativePixelXY(Point2D lonLat) {
/*
ASCII Art Time!
0--------------------2pi
||                  ||
\/                  \/

  /\  /\  /\  /\  /\
 /__\/__\/__\/__\/__\
 \  /\  /\  /\  /\  /\
  \/__\/__\/__\/__\/__\
   \  /\  /\  /\  /\  /
    \/  \/  \/  \/  \/

                    /\
                    ||
      2pi/0 is in middle of this triangle
*/
		
		final double lon = twoPi*clamp(lonLat.getX() * oneOverTwoPi);
		final double lat = lonLat.getY();
		//
		if(lat >= upperLatitude){
			// one of the 5 north pole triangles
			double span = lon * fiveOverTwoPi;
			int trinum = (int)(span);
			double y = triangleHeight * (1 - ((lat - upperLatitude) * icoAngleInverse));
			double lineWidth = y * oneOverRoot3;
			double dx = (span - trinum) * lineWidth;
			double xOffset = triangleWidth * trinum + 0.5*(triangleWidth - lineWidth);
			double x = xOffset + dx;
			return new Point2D(x,y);
		} else if(lat <= lowerLatitude){
			// one of the 5 south pole triangles
			// note that the peak of the 5th triangle is aligned with the 0-longitude line
			// (offset by one tenth of a rotation)
			double span = 5 * clamp(lon * oneOverTwoPi - 0.1);
			int trinum = (int)(span);
			double dy = triangleHeight * (1 - ((lowerLatitude - lat) * icoAngleInverse));
			double lineWidth = dy * oneOverRoot3;
			double dx = (span - trinum) * lineWidth;
			double xOffset = triangleHalfWidth + triangleWidth * trinum + 0.5*(triangleWidth - lineWidth);
			double x = xOffset + dx;
			double y = 1 - dy;
			return new Point2D(x,y);
		} else {
			// one of the equatorial 10 triangles
			// conveniently, these are basically a mercator UV mapping
			double vspan = lat * inverseInnerLatitude; // -0.5 to 0.5
			double dy = triangleHeight * (vspan);
			double y = 0.5 - dy;
			double vspan2 = 0.5 - vspan; // 0 to 1
			double xcut = vspan2 * 0.1;//vspan2 * oneOverRoot3;
			double hspan = lon * oneOverTwoPi;
			double x;
			if(hspan < xcut){
				// on left side of left slope, move to right side
				hspan += 1;
			}
			x = 10 * triangleHalfWidth * hspan;
			return new Point2D(x,y);
		}
		
	}
	private static Point2D _convertRelativePixelXYToLonLat(Point2D relXY) {
		
		final double x = clamp(relXY.getX()), y = clamp(relXY.getY());
		double lon,lat;
		if(y < upperY){
			// one of the 5 north pole triangles
			lat = Math.min(
					piOverTwo,
					(1 - (y * triangleHeightInverse)) * icoAngle + upperLatitude
			);
			double span = x * 5.5; // 11/10 scalar x 5
			int trinum = Math.min(4, (int)(span));
			double dx = triangleWidth * Math.min(1.0, span - trinum);
			double lineWidth = y * oneOverRoot3;
			double xOffset = 0.5*(triangleWidth - lineWidth);
			double xLimit = triangleWidth - xOffset;
			double baseLongitude = trinum * twoPiOverFive;
			if(dx <= xOffset) {
				lon = baseLongitude;
			} else if(dx >= xLimit) {
				lon = baseLongitude + twoPiOverFive;
			} else {
				lon = baseLongitude + ((dx - xOffset) / lineWidth) * twoPiOverFive;
			}
		} else if(y > lowerY) {
			// one of the 5 south pole triangles
			// note that the peak of the 5th triangle is aligned with the 0-longitude line
			// (offset by one tenth of a rotation)
			lat = Math.max(
					-piOverTwo,
					lowerLatitude - ((y - lowerY) * triangleHeightInverse) * icoAngle
			);
			double span = Math.max(0, (x - triangleHalfWidth) * 5.5); // 11/10 scalar x 5
			int trinum = Math.min(4, (int)(span));
			double dx = triangleWidth * Math.min(1.0, span - trinum);
			double dy = triangleHeight - (y - lowerY);
			double lineWidth = dy * oneOverRoot3;
			double xOffset = 0.5*(triangleWidth - lineWidth);
			double xLimit = triangleWidth - xOffset;
			double baseLongitude = trinum * twoPiOverFive + piOverFive;
			if(dx <= xOffset) {
				lon = baseLongitude;
			} else if(dx >= xLimit) {
				lon = baseLongitude + twoPiOverFive;
			} else {
				lon = baseLongitude + ((dx - xOffset) / lineWidth) * twoPiOverFive;
			}
			
			
		} else {
			// one of the equatorial 10 triangles
			// conveniently, these are basically a mercator UV mapping
			double dy = (y - 0.5) * equatorialYRangeInverse; // -0.5 to 0.5
			lat = dy * 2 * upperLatitude;
			lon = x * 1.1 * twoPi;
			
		}
		if(lon > twoPi) lon -= twoPi;
		if(lon > Math.PI) lon -= twoPi;
		return new Point2D(lon,lat);
	}
	
}
