package com.bibler.awesome.bibnes20.main;

import com.bibler.awesome.bibnes20.systems.console.Console;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePakFactory;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;
import com.bibler.awesome.bibnes20.systems.utilitychips.ROM;
import com.bibler.awesome.bibnes20.ui.CPUDebugFrame;

public class Main {
	
	public static void main(String[] args) {
		
		CPUDebugFrame frame = new CPUDebugFrame();
		
		ROM testRom = setupTestROM();
		Console console = new Console();
		RAM testRam = setupTestRAM();
		console.getMotherboard().setCPURam(setupTestRAM());
		console.insertGamePak(GamePakFactory.createGamePakFromRoms(testRom, null));
	}
	
	private static ROM setupTestROM() {
		int[] romArray = new int[0x8000];
		romArray[0] = 0xB5;
		romArray[1] = 0xFE;
		romArray[2] = 0xA2;
		romArray[3] = 0xFD;
		romArray[4] = 0xA0;
		romArray[5] = 0xFC;
		romArray[6] = 0xA5;
		romArray[7] = 0x0E;
		romArray[0x7FFC] = 0x00;
		romArray[0x7FFD] = 0x80;
		ROM testRom = new ROM(romArray);
		return testRom;
	}
	
	private static RAM setupTestRAM() {
		int[] ramArray = new int[0x2000];
		ramArray[0x0E] = 0xFB;
		RAM testRam = new RAM(ramArray);
		return testRam;
	}

}
