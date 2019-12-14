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
package cchall.javafx.globeviewer.interaction;

import cchall.javafx.globeviewer.GlobeViewer;
import java.util.concurrent.atomic.AtomicReference;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.input.MouseEvent;

/**
 * This InteractionHandler lets the user spin and tilt the globe by dragging the 
 * mouse
 * @author CCHall <a href="mailto:explosivegnome@yahoo.com">
 * explosivegnome@yahoo.com</a>
 */
public class SpinWithMouse implements InteractionHandler{

	
	private static final double pi = Math.PI;
	private static final double piOver2 = 0.5*Math.PI;
	
	private GlobeViewer target = null;
	private final AtomicReference<Point2D> dragStart = new AtomicReference<>(null);
	private final AtomicReference<Point3D> rotationStart = new AtomicReference<>(null);
	private final DragListener dl;
	private final ReleaseListener rl;
	
	public SpinWithMouse(){
		dl = new DragListener();
		rl = new ReleaseListener();
	}
	
	@Override
	public void applyTo(GlobeViewer globeViewer) throws IllegalStateException {
		if(target != null){
			throw new IllegalStateException(String.format("An %s instance can only control one %s",
					InteractionHandler.class.getSimpleName(), 
					GlobeViewer.class.getSimpleName()));
		}
		target = globeViewer;
		if(target.getScene().onMouseDraggedProperty().get() != null
				|| target.getScene().onMouseReleasedProperty().get() != null
				|| target.getScene().onMouseExitedProperty().get() != null){
			throw new IllegalStateException(String.format("Cannot control instance of %s because it already has handlers bound to the requred mouse events.",
					GlobeViewer.class.getSimpleName()));
		}
		target.getScene().setOnMouseDragged(dl);
		target.getScene().setOnMouseReleased(rl);
		target.getScene().setOnMouseExited(rl);
		clearCoords();
	}

	@Override
	public void remove() {
		if(target == null) return;
		target.getScene().setOnMouseDragged(null);
		target.getScene().setOnMouseReleased(null);
		target.getScene().setOnMouseExited(null);
		clearCoords();
		target = null;
	}
	
	private void clearCoords(){
		dragStart.set(null);
		rotationStart.set(null);
	}

	private class DragListener implements EventHandler<MouseEvent>{

		@Override
		public void handle(MouseEvent e) {
			if(!e.isPrimaryButtonDown()) return;
			
			if(dragStart.get() == null){
				dragStart.set(new Point2D(e.getSceneX(), e.getSceneY()));
				rotationStart.set(target.globeRotationVector().get());
			} else {
				double dX = (e.getSceneX() - dragStart.get().getX()) / target.getScene().getWidth();
				double dY = (dragStart.get().getY() - e.getSceneY()) / target.getScene().getHeight();
				Point3D old = rotationStart.get();
				double rx = old.getX() - (dY * piOver2);
				double ry = old.getY() + (dX * pi);
				double rz = old.getZ();
				Point3D newVector = new Point3D(rx, ry, rz);
				target.globeRotationVector().set(newVector);
				rotationStart.set(newVector);
				dragStart.set(new Point2D(e.getSceneX(), e.getSceneY()));
			}
		}
	}

	private class ReleaseListener implements EventHandler<MouseEvent>{

		@Override
		public void handle(MouseEvent e) {
			clearCoords();
		}
	}
}
