/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cchall.javafx.globeviewer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.*;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Cybergnome
 */
public class JavaFXInitializer extends Application{

	static final AtomicInteger redudancyCounter = new AtomicInteger(0);
	
	public synchronized static void begin(){
		if(redudancyCounter.get() == 0){
			ForkJoinPool.commonPool().submit(()->{JavaFXInitializer.launch(JavaFXInitializer.class);});
		}
		redudancyCounter.incrementAndGet();
	}
	
	
	static Thread appThread = null;
	
	private static JavaFXInitializer instance = null;
	@Override
	public void start(Stage primaryStage) throws Exception {
		instance = this;
		appThread = Thread.currentThread();
	}
	public synchronized static void exit(){
		redudancyCounter.decrementAndGet();
		if(redudancyCounter.get() == 0){
			if(instance != null){
				Platform.exit();
				if(appThread.isAlive()){
					try {
						appThread.join();
					} catch (InterruptedException ex) {
						//
					}
				}
				appThread = null;
				instance = null;
			}
		}
	}
}
