package com.bibler.awesome.bibnes20.controllers;

public abstract class BaseController {
	
	protected int rightMask = 1;
	protected int leftMask = 2;
	protected int downMask = 4;
	protected int upMask = 8;
	protected int startMask = 16;
	protected int selectMask = 32;
	protected int bMask = 64;
	protected int aMask = 128;
	
	
	protected int strobeRegister;
	
	public abstract void write();
	
	public abstract int read();

}
