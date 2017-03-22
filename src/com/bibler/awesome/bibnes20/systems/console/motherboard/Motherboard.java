package com.bibler.awesome.bibnes20.systems.console.motherboard;

import com.bibler.awesome.bibnes20.systems.console.motherboard.busses.AddressBus;
import com.bibler.awesome.bibnes20.systems.console.motherboard.cpu.CPU;
import com.bibler.awesome.bibnes20.systems.console.motherboard.ppu.PPU;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePak;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;

public class Motherboard {
	
	private CPU cpu;
	private PPU ppu;
	private RAM cpuRam;
	private RAM ppuRam;
	private AddressBus addressBus;
	
	
	public Motherboard() {
		ppu = new PPU();
		cpuRam = new RAM(0x2000);
		addressBus = new AddressBus(cpuRam, ppuRam, ppu);
		cpu = new CPU(addressBus);
	}
	
	public CPU getCPU() {
		return cpu;
	}
	
	public void reset() {
		cpu.reset();
	}

	public void acceptGamePak(GamePak gamePak) {
		addressBus.setGamePak(gamePak);
	}

	public void setCPURam(RAM cpuRam) {
		this.cpuRam = cpuRam;
		addressBus.setCPURam(cpuRam);
		
		
	}

	public void cyclePPU() {
		ppu.cycle();
		
	}
	
	public int cycleCPU() {
		return cpu.cycle();
	}

	public int[] getFrame() {
		return ppu.getFrame();
	}

}
