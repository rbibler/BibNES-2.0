package com.bibler.awesome.bibnes20.systems.gamepak;

import com.bibler.awesome.bibnes20.systems.utilitychips.ROM;

public class GamePakFactory {
	
	public static GamePak createGamePakFromRoms(ROM prgRom, ROM chrRom) {
		GamePak gp = new GamePak();
		gp.setPrgRom(prgRom);
		gp.setChrRom(chrRom);
		return gp;
	}

}
