package com.bibler.awesome.bibnes20.systems.console.motherboard.busses;

import com.bibler.awesome.bibnes20.controllers.BaseController;
import com.bibler.awesome.bibnes20.systems.console.motherboard.ppu.PPU;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePak;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;

public class AddressBus {
	
	private RAM cpuRam;
	private GamePak gamePak;
	private PPU ppu;
	private BaseController controllerPort;
	private int gamePakAddressStart = 0x8000;			// NORMALLY 0x8000
	
	public static final int PPU_BUS = 0x03;
	public static final int PRG_BUS = 0x04;
	public static final int RAM_BUS = 0x05;
	public static final int CNTR_BUS = 0x06;
	
	
	private int address;
	private int latchedData;
	private int selector;
	
	public AddressBus(RAM cpuRam, PPU ppu, BaseController controller) {
		this.cpuRam = cpuRam;
		this.ppu = ppu;
		this.controllerPort = controller;
	}


	public void assertAddress(int address) {
		this.address = address;
		if(address < 0x2000) {
			selector = RAM_BUS;
			latch(cpuRam.read(address));
		} else if(address >= 0x2000 && address <= 0x2007) {
			selector = PPU_BUS;
		} else if(address == 0x4016) {
			selector = CNTR_BUS;
		} else if(address >= gamePakAddressStart) {
			selector = PRG_BUS;
			latch(gamePak.readPrgRom(address));
		}
	}
	
	public void assertAddressAndWrite(int address) {
		this.address = address;
		if(address < 0x2000) {
			cpuRam.write(address, latchedData);
		} else if(address >= 0x2000 && address <= 0x2007) {
			ppu.write(address - 0x2000, latchedData);
		} else if(address == 0x4016) {
			controllerPort.write();
		}else if(address >= gamePakAddressStart) {
			gamePak.writePrgRom(address, latchedData);
		}
	}
	
	public void latch(int dataToLatch) {
		latchedData = dataToLatch;
	}
	
	public int readLatchedData() {
		if(selector == PPU_BUS) {
			return ppu.read(address - 0x2000);
		} else if(selector == CNTR_BUS) {
			return controllerPort.read();
		}
		return latchedData;
	}


	public void setGamePak(GamePak gamePak) {
		this.gamePak = gamePak;
	}


	public void setCPURam(RAM cpuRam) {
		this.cpuRam = cpuRam;
	}

}
