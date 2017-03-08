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
	private boolean step;
	
	public void setConsole(Console console) {
		this.console = console;
		cpu = console.getMotherboard().getCPU();
		pause = true;
	}
	
	public void startEmulator() {
		//Thread t = new Thread(this);
		//t.start();
	}
	
	int cycleCount = 0;
	
	@Override
	public void run() {
		while(!Thread.interrupted()) {
			if(!pause) {
				runCycle();
			} 
		}
	}
	
	private void runCycle() {
		final int cycleResult = cpu.cycle();
		System.out.println(cycleResult);
		if(cycleResult == 0) {
			notify("CPU_UPDATE", cpu.getStatusUpdate());
			if(step) {
				step = false;
				pause();
			}
		}
	}
	
	private void step() {
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
	
	private boolean debug = true;

	@Override
	public void takeNotice(String message, Object messagePacket) {
		if(message.equalsIgnoreCase("RUN")) {
			int[][] romRam = (int[][]) messagePacket;
			console.insertGamePak(GamePakFactory.createGamePakFromRoms(new ROM(romRam[0]), null));
			console.setCPURam(romRam[1]);
			startEmulator();
		} else if(message.equalsIgnoreCase("STEP")) {
			//step = true;
			//resume();
			if(debug) {
				debug = false;
				for(int i = 0; i < 541; i++) {
					step();
				}
			} else {
				step();
			}
		} else if(message.equalsIgnoreCase("RESET")) {}
	}
}
