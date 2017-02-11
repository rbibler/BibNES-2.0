package com.bibler.awesome.bibnes20.systems.console.motherboard.busses;

import com.bibler.awesome.bibnes20.systems.gamepak.GamePak;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;

public class AddressBus {
	
	private DataBus dataBus;
	private RAM cpuRam;
	private RAM ppuRam;
	private GamePak gamePak;
	
	
	private int address;
	
	
	public AddressBus(DataBus dataBus, RAM cpuRam, RAM ppuRam) {
		this.dataBus = dataBus;
		this.cpuRam = cpuRam;
		this.ppuRam = ppuRam;
	}


	public void assertAddress(int address) {
		this.address = address;
		if(address < 0x2000) {
			dataBus.latch(cpuRam.read(address));
		} else if(address >= 0x8000) {
			dataBus.latch(gamePak.readPrgRom(address));
		}
	}
	
	public void assertAddressAndWrite(int address) {
		this.address = address;
		final int data = dataBus.read();
		if(address < 0x2000) {
			cpuRam.write(address, data);
		} else if(address >= 0x8000) {
			gamePak.writePrgRom(address, data);
		}
	}


	public void setGamePak(GamePak gamePak) {
		this.gamePak = gamePak;
	}


	public void setCPURam(RAM cpuRam) {
		this.cpuRam = cpuRam;
	}

}
