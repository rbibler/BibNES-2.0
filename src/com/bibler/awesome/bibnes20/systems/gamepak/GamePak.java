package com.bibler.awesome.bibnes20.systems.gamepak;

import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;
import com.bibler.awesome.bibnes20.systems.utilitychips.ROM;

public class GamePak {
	
	private ROM prgRom;
	private ROM chrRom;
	private RAM prgRam;
	private RAM chrRam;
	private int mirroring;
	
	public GamePak() {
		
	}
	
	public void setPrgRom(ROM prgRom) {
		this.prgRom = prgRom;
	}
	
	public void setChrRom(ROM chrRom) {
		this.chrRom = chrRom;
	}
	
	public int readPrgRom(int address) {
		return prgRom.read(address);
	}

	public void writePrgRom(int address, int data) {
		prgRom.write(address, data);
		
	}

	public int readChrRom(int address) {
		return(chrRom.read(address));
	}
	
	public void writeChrRom(int address, int data) {
		return;
	}
	
	public void setMirroring(int mirroring) {
		this.mirroring = mirroring;
	}

	public int getMirroring() {
		return mirroring;
	}

	public ROM getCHRRom() {
		return chrRom;
	}
	
	public ROM getPRGRom() {
		return prgRom;
	}

}
