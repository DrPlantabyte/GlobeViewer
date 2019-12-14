/*
 * The MIT License
 *
 * Copyright 2017 Christopher C. Hall.
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *
 * @author CCHall <a href="mailto:hallch20@msu.edu">hallch20@msu.edu</a>
 */
public class GlobeViewerIT {
	
	public GlobeViewerIT() {
	}

	@BeforeClass
	public static void setUpClass() {
		try {
			Reader in = new InputStreamReader(GlobeViewerIT.class.getResourceAsStream("version.properties"));
			int c;
			while((c = in.read()) >= 0){
				System.out.print((char)c);
			}
		} catch (IOException ex) {
			Logger.getLogger(GlobeViewerIT.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	@Test
	public void testApp() {
		Application.launch(TestApp7.class);
	}
	
	public static void main(String[] o){
		GlobeViewerIT p = new GlobeViewerIT();
		p.testApp();
	}
}
