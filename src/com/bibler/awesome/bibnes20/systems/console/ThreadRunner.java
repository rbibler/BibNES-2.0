package com.bibler.awesome.bibnes20.systems.console;

import com.bibler.awesome.bibnes20.communications.Notifiable;
import com.bibler.awesome.bibnes20.communications.Notifier;
import com.bibler.awesome.bibnes20.systems.console.motherboard.Motherboard;
import com.bibler.awesome.bibnes20.systems.console.motherboard.cpu.CPU;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePak;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePakFactory;
import com.bibler.awesome.bibnes20.systems.utilitychips.ROM;
import com.bibler.awesome.bibnes20.ui.debug.DebugFrame;

public class ThreadRunner extends Notifier implements Runnable, Notifiable {
	
	private Console console;
	private Motherboard motherboard;
	private DebugFrame debugFrame;
	
	
	// Control variables
	private boolean pause;
	private boolean pauseAfterStep;
	private Thread t;
	
	// Timing variables
	private long FPS = (long) (1000 / 60.0988);
	private long lastFrameTime;
	private int cpuDivider;
	private int ppuDivider;
	
	public void setConsole(Console console) {
		this.console = console;
		motherboard = console.getMotherboard();
		pause = true;
	}
	
	public void startEmulator() {
		if(t == null || !t.isAlive()) {
			t = new Thread(this);
			t.start();
			lastFrameTime = System.currentTimeMillis();
		}
	}
	
	int cycleCount = 0;
	private boolean frame;
	
	@Override
	public void run() {
		while(!Thread.interrupted()) {
			if(!pause) {
				//for(long i = 0; i < 357366; i++) {
				if(cpuDivider == 12) {
					cpuDivider = 0;
					clockCPU();
				}
				cpuDivider++;
				if(ppuDivider == 4) {
					ppuDivider = 0;
					frame = clockPPU();
				}
				ppuDivider++;
				//}
				if(frame) {
					frame = false;
					console.displayFrame();
					debugFrame.updateFrame();
				
					final long timeTaken = System.currentTimeMillis() - lastFrameTime;
					final long sleepTime = FPS - timeTaken;
				//System.out.println("Sleep time: " + sleepTime);
					if(sleepTime > 0) {
						try {
							Thread.sleep(sleepTime);
						} catch(InterruptedException e) {}
					}
					lastFrameTime = System.currentTimeMillis();
				}
			} 
		}
	}
	
	private void clockCPU() {
		motherboard.cycleCPU();
	}
	
	private boolean clockPPU() {
		return motherboard.cyclePPU();
	}
	
	private void step() {
		System.out.println("Step");
		int cycleResult = -1;
		do {
			cycleResult = motherboard.cycleCPU();
		} while(cycleResult != 0);
		notify("CPU_UPDATE", motherboard.getCPU().getStatusUpdate());
	}
	private int frameCount;
	private void nextPixel() {
		if(debug) {
			while(frameCount < 8) {
				frame = false;
			while(frame == false) {
				if(cpuDivider == 12) {
					cpuDivider = 0;
					clockCPU();
				}
				cpuDivider++;
				if(ppuDivider == 4) {
					ppuDivider = 0;
					frame = clockPPU();
					System.out.println(frame);
				}
				ppuDivider++;
				
			}
			frameCount++;
			}
			debug = false;
		}
		console.clearFrame();
		System.out.println("Y");
		clockPPU();
		cpuDivider += 4;
		if(cpuDivider == 12) {
			clockCPU();
			cpuDivider = 0;
		}
		console.displayFrame();
	}
	
	public synchronized void pause() {
		pause = true;
	}
	
	public synchronized void resume() {
		pause = false;
	}
	
	public void setDebugFrame(DebugFrame debugFrame) {
		this.debugFrame = debugFrame;
	}
	
	private boolean debug = true;

	@Override
	public void takeNotice(String message, Object messagePacket) {
		if(message.equalsIgnoreCase("LOAD_ROM")) {
			Object[] messagePacketArray = (Object[]) messagePacket;
			GamePak pak = (GamePak) messagePacketArray[0];
			console.insertGamePak(pak);
		} else if(message.equalsIgnoreCase("STEP")) {
			if(t == null || t.isAlive() == false) {
				startEmulator();
			}
			if(debug) {
				debug = false;
				for(int i = 0; i < 587; i++) {
					step();
				}
			} else {
				pauseAfterStep = true;
				resume();
			}
		} else if(message.equalsIgnoreCase("RESET")) {
			
		} else if(message.equalsIgnoreCase("RUN")) {
			startEmulator();
			resume();
		} else if(message.equalsIgnoreCase("NEXT_PIXEL")) {
			nextPixel();
		}
	}
}
