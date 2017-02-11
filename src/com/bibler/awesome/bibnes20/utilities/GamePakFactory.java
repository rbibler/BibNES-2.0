package com.bibler.awesome.bibnes20.utilities;

import com.bibler.awesome.bibnes20.systems.gamepak.GamePak;
import com.bibler.awesome.bibnes20.systems.utilitychips.ROM;

public class GamePakFactory {

	public static GamePak createGamePakFromRoms(ROM prgROM, ROM chrROM) {
		GamePak gp = new GamePak();
		gp.setPrgRom(prgROM);
		gp.setChrRom(chrROM);
		return gp;
	}

}
