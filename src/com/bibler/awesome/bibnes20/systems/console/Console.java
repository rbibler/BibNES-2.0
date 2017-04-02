package com.bibler.awesome.bibnes20.systems.console;

import com.bibler.awesome.bibnes20.systems.console.motherboard.Motherboard;
import com.bibler.awesome.bibnes20.systems.console.output.TVOut;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePak;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;
import com.bibler.awesome.bibnes20.systems.utilitychips.ROM;
import com.bibler.awesome.bibnes20.ui.debug.DebugFrame;

public class Console {
	
	private Motherboard motherboard;
	private GamePak currentGamePak;
	private TVOut tvOut;
	
	public Console(TVOut tvOut) {
		motherboard = new Motherboard();
		this.tvOut = tvOut;
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

	public void displayFrame() {
		tvOut.displayFrame(motherboard.getFrame());
	}

}
