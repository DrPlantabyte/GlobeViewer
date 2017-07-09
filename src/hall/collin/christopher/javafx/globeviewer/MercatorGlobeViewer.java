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
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;

/**
 *
 * @author CCHall <a href="mailto:explosivegnome@yahoo.com">
 * explosivegnome@yahoo.com</a>
 */
public class MercatorGlobeViewer extends GlobeViewer {

	/**
	 * Generates the globe model with appropriate normals and texture coordinates 
	 * specified for each vertex.
	 * @param radius The radius of the globe.
	 * @return A Shape3D instance of the globe mesh model (typically a MeshVw 
	 * wrapping a TriangleMesh).
	 */
	@Override
	protected Shape3D createGlobeModel(double radius) {
		final int radialSegments = 32;
		final int lateralSegments = 32;
		final double fudge = 0.984375;
		final double dLon = 2 * Math.PI / radialSegments;
		final double dLat = fudge * Math.PI / lateralSegments;
		final double dx = 1.0 / radialSegments;
		final double dy = fudge * 1.0 / lateralSegments;
		final double eq = 0.5 * lateralSegments;
		
		List<TriangleFace> faceList = new ArrayList<>(radialSegments * (lateralSegments+1) * 2);
		for(int r = 0 ; r < radialSegments; r++){
			double lon = r * dLon;
			double nextLon = ((r+1) % radialSegments) * dLon;
			double x = Math.min(1,r * dx);
			double nextx = Math.min(1,(r+1) * dx);
			List<TriangleFace> strip = new ArrayList<>((lateralSegments+1) * 2);
			for(int l = 0; l < lateralSegments; l++){
				double lat = dLat * (eq - l);
				double nextLat = dLat * (eq - (l+1));
				double y = dy * l;
				double nexty = dy * (l+1);
				
				Point3D n1 = lonLatToSurfacePoint(new Point2D(nextLon,lat));
				Point3D n2 = lonLatToSurfacePoint(new Point2D(lon,lat));
				Point3D n3 = lonLatToSurfacePoint(new Point2D(lon,nextLat));
				Point3D n4 = lonLatToSurfacePoint(new Point2D(nextLon,nextLat));
				Point2D uv1 = new Point2D(nextx, y);
				Point2D uv2 = new Point2D(x, y);
				Point2D uv3 = new Point2D(x, nexty);
				Point2D uv4 = new Point2D(nextx, nexty);
				
				strip.add(new TriangleFace(
						new Vertex(n1.multiply(radius), n1, uv1),
						new Vertex(n2.multiply(radius), n2, uv2),
						new Vertex(n3.multiply(radius), n3, uv3)
				));
				strip.add(new TriangleFace(
						new Vertex(n1.multiply(radius), n1, uv1),
						new Vertex(n3.multiply(radius), n3, uv3),
						new Vertex(n4.multiply(radius), n4, uv4)
				));
			}
			
			Point2D northPoleUV = new Point2D(0.5*(x + nextx),0);
			Point2D southPoleUV = new Point2D(0.5*(x + nextx),1);
			
			TriangleFace northFace = new TriangleFace(
					new Vertex(new Point3D(0,radius,0),new Point3D(0,1,0),northPoleUV),
					strip.get(0).vertices[1],
					strip.get(0).vertices[0]
			);
			
			TriangleFace southFace = new TriangleFace(
					new Vertex(new Point3D(0,-radius,0),new Point3D(0,-1,0),southPoleUV),
					strip.get(strip.size()-1).vertices[2],
					strip.get(strip.size()-1).vertices[1]
			);
			strip.add(northFace);
			strip.add(southFace);
			faceList.addAll(strip);
		}
		
		TriangleMesh mesh = TriangleFace.createTriangleMesh(faceList);
		MeshView shape = new MeshView(mesh);
		shape.setDrawMode(DrawMode.FILL);
		return shape;
	}
	
/**
* {@inheritDoc}
*/
	@Override
	public Point2D convertLonLatToRelativePixelXY(Point2D lonLat) {
		final double oneOverTwoPi = 1.0 / (2.0 * Math.PI);
		final double oneOverPi = 1.0 / Math.PI;
		final double piOverTwo = 0.5 * Math.PI;
		return new Point2D( clamp( lonLat.getX() * oneOverTwoPi), clamp(1.0 - ( (lonLat.getY() + piOverTwo) * oneOverPi)) );
	}

/**
* {@inheritDoc}
*/
	@Override
	public Point2D convertRelativePixelXYToLonLat(Point2D relXY) {
		final double twoPi = 2.0 * Math.PI;
		return new Point2D( 
				(clamp( relXY.getX() + 0.5 ) - 0.5) * twoPi, 
				(0.5 - clamp( relXY.getY() )) * Math.PI 
		);
	}
	
}
