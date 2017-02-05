package com.bibler.awesome.bibnes20.systems.console.motherboard;

import com.bibler.awesome.bibnes20.systems.console.motherboard.busses.AddressBus;
import com.bibler.awesome.bibnes20.systems.console.motherboard.busses.DataBus;
import com.bibler.awesome.bibnes20.systems.console.motherboard.cpu.CPU;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePak;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;

public class Motherboard implements Runnable {
	
	private CPU cpu;
	private RAM cpuRam;
	private RAM ppuRam;
	private AddressBus addressBus;
	private DataBus dataBus;
	private Thread t;
	
	public Motherboard() {
		cpuRam = new RAM(0x2000);
		dataBus = new DataBus();
		addressBus = new AddressBus(dataBus, cpuRam, ppuRam);
		cpu = new CPU(addressBus, dataBus);
	}
	
	public void reset() {
		cpu.reset();
		t = new Thread(this);
		t.start();
	}
	

	@Override
	public void run() {
		while(!Thread.interrupted()) {
			cpu.cycle();
			cpu.printStatus();
			try {
				Thread.sleep(250);
			} catch(InterruptedException e) {}
		}
	}

	public DataBus getDataBus() {
		return dataBus;
	}

	public void acceptGamePak(GamePak gamePak) {
		addressBus.setGamePak(gamePak);
	}

	public void setCPURam(RAM cpuRam) {
		this.cpuRam = cpuRam;
		addressBus.setCPURam(cpuRam);
		
		
	}
	
	

}
