package com.bibler.awesome.bibnes20.systems.console.motherboard;

import com.bibler.awesome.bibnes20.controllers.KeyboardController;
import com.bibler.awesome.bibnes20.systems.console.motherboard.busses.AddressBus;
import com.bibler.awesome.bibnes20.systems.console.motherboard.busses.PPUAddressBus;
import com.bibler.awesome.bibnes20.systems.console.motherboard.cpu.CPU;
import com.bibler.awesome.bibnes20.systems.console.motherboard.ppu.PPU;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePak;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;
import com.bibler.awesome.bibnes20.ui.debug.DebugFrame;

public class Motherboard {
	
	private CPU cpu;
	private PPU ppu;
	private RAM cpuRam;
	private RAM ppuRam;
	private AddressBus addressBus;
	private DebugFrame debugFrame;
	
	
	public Motherboard() {
		ppu = new PPU();
		ppuRam = new RAM(0x800);
		PPUAddressBus ppuAddressBus = new PPUAddressBus(ppuRam, ppu);
		ppu.setAddressBus(ppuAddressBus);
		cpuRam = new RAM(0x800);
		KeyboardController controller = new KeyboardController();
		addressBus = new AddressBus(cpuRam, ppu, controller);
		cpu = new CPU(addressBus);
		ppu.setCPU(cpu);
	}
	
	public CPU getCPU() {
		return cpu;
	}
	
	public void reset() {
		cpu.reset();
	}

	public void acceptGamePak(GamePak gamePak) {
		addressBus.setGamePak(gamePak);
		ppu.getAddressBus().setGamePak(gamePak);
		debugFrame.setMemory(ppuRam, cpuRam, gamePak);
		debugFrame.setPPU(ppu);
		debugFrame.setChrRom(gamePak.getCHRRom());
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

	public PPU getPPU() {
		return ppu;
	}

	public void setDebugFrame(DebugFrame debugFrame) {
		this.debugFrame = debugFrame;
		
	}

}
