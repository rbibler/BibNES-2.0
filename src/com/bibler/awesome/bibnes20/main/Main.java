package com.bibler.awesome.bibnes20.main;

import com.bibler.awesome.bibnes20.systems.console.Console;
import com.bibler.awesome.bibnes20.systems.console.ThreadRunner;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePakFactory;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;
import com.bibler.awesome.bibnes20.systems.utilitychips.ROM;
import com.bibler.awesome.bibnes20.ui.cpuemulator.CPUDebugFrame;
import com.bibler.awesome.bibnes20.ui.debug.DebugFrame;
import com.bibler.awesome.bibnes20.ui.nes.NESFrame;

public class Main {
	
	public static void main(String[] args) {
		DebugFrame debug = new DebugFrame();
		debug.setVisible(true);
		NESFrame nesFrame = new NESFrame();
		Console console = new Console();
		ThreadRunner runner = new ThreadRunner();
		runner.setConsole(console);
		nesFrame.setRunner(runner);
	}

}
