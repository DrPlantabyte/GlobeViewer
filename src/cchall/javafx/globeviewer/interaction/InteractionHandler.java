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

/**
 * Interface implemented by classes which specify interaction with a GlobeViewer.
 * @author CCHall <a href="mailto:explosivegnome@yahoo.com">
 * explosivegnome@yahoo.com</a>
 */
public interface InteractionHandler {
	/**
	 * Causes this object to control the behavior of a given GlobeViewer
	 * @param globeViewer The GlobeViewer to control
	 * @throws IllegalStateException Thrown if you try to apply this instance to 
	 * more than one GlobeViewer
	 */
	public abstract void applyTo(GlobeViewer globeViewer) throws IllegalStateException;
	/**
	 * Causes this object to revoke its control of the GlobeViewer
	 */
	public abstract void remove();
}
