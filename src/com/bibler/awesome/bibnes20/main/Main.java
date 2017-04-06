package com.bibler.awesome.bibnes20.main;

import com.bibler.awesome.bibnes20.systems.console.Console;
import com.bibler.awesome.bibnes20.systems.console.ThreadRunner;
import com.bibler.awesome.bibnes20.systems.console.output.TVOut;
import com.bibler.awesome.bibnes20.ui.debug.DebugFrame;

public class Main {
	
	public static void main(String[] args) {
		DebugFrame debug = new DebugFrame();
		debug.setVisible(true);
		TVOut tvOutFrame = new TVOut();
		Console console = new Console(tvOutFrame);
		ThreadRunner runner = new ThreadRunner();
		runner.setConsole(console);
		tvOutFrame.setRunner(runner);
		tvOutFrame.setPPU(console.getMotherboard().getPPU());
		debug.setConsole(console);
		console.getMotherboard().setDebugFrame(debug);
		runner.setDebugFrame(debug);
	}

}
