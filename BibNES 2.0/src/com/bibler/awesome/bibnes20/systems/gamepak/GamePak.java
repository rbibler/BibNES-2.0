package com.bibler.awesome.bibnes20.systems.gamepak;

import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;
import com.bibler.awesome.bibnes20.systems.utilitychips.ROM;

public class GamePak {
	
	private ROM prgRom;
	private ROM chrRom;
	private RAM prgRam;
	private RAM chrRam;
	
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

}
