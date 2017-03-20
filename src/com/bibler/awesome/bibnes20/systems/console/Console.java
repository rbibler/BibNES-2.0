package com.bibler.awesome.bibnes20.systems.console;

import com.bibler.awesome.bibnes20.systems.console.motherboard.Motherboard;
import com.bibler.awesome.bibnes20.systems.console.output.TVOut;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePak;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;

public class Console {
	
	private Motherboard motherboard;
	private GamePak currentGamePak;
	private TVOut tvOut;
	
	public Console() {
		motherboard = new Motherboard();
		tvOut = new TVOut();
	}

	public Motherboard getMotherboard() {
		return motherboard;
	}

	public void reset() {
		motherboard.reset();
	}

	public void insertGamePak(GamePak gamePak) {
		currentGamePak = gamePak;
		motherboard.acceptGamePak(gamePak);
		reset();
	}

	public void setCPURam(int[] cpuRAM) {
		motherboard.setCPURam(new RAM(cpuRAM));
		
	}

	public void displayFrame() {
		tvOut.displayFrame(motherboard.getFrame());
	}
}
