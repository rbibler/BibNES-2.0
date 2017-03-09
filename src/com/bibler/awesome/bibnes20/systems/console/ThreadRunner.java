package com.bibler.awesome.bibnes20.systems.console;

import com.bibler.awesome.bibnes20.communications.Notifiable;
import com.bibler.awesome.bibnes20.communications.Notifier;
import com.bibler.awesome.bibnes20.systems.console.motherboard.cpu.CPU;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePakFactory;
import com.bibler.awesome.bibnes20.systems.utilitychips.ROM;

public class ThreadRunner extends Notifier implements Runnable, Notifiable {
	
	private Console console;
	private CPU cpu;
	
	private boolean pause;
	private boolean pauseAfterStep;
	private Thread t;
	
	public void setConsole(Console console) {
		this.console = console;
		cpu = console.getMotherboard().getCPU();
		pause = true;
	}
	
	public void startEmulator() {
		if(t == null || !t.isAlive()) {
			t = new Thread(this);
			t.start();
		}
	}
	
	int cycleCount = 0;
	
	@Override
	public void run() {
		while(!Thread.interrupted()) {
			if(!pause) {
				step();
				if(pauseAfterStep) {
					pause = true;
					pauseAfterStep = false;
				}
			} 
		}
	}
	
	private void step() {
		System.out.println("Step");
		int cycleResult = -1;
		do {
			cycleResult = cpu.cycle();
		} while(cycleResult != 0);
		notify("CPU_UPDATE", cpu.getStatusUpdate());
	}
	
	public synchronized void pause() {
		pause = true;
	}
	
	public synchronized void resume() {
		pause = false;
	}
	
	private boolean debug = false;

	@Override
	public void takeNotice(String message, Object messagePacket) {
		if(message.equalsIgnoreCase("LOAD")) {
			int[][] romRam = (int[][]) messagePacket;
			console.insertGamePak(GamePakFactory.createGamePakFromRoms(new ROM(romRam[0]), null));
			console.setCPURam(romRam[1]);
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
		}
	}
}
