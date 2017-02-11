package com.bibler.awesome.bibnes20.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputUtilities {
	
	static Pattern memPattern = Pattern.compile("-m([ \t]*[A-Fa-f0-9]{2,4})*");
	static Pattern codePattern = Pattern.compile("-c([ \t]*[A-Fa-f0-9]{2,2})*");
	static Pattern romPattern = Pattern.compile("-r([ \t]*[A-Fa-f0-9]{2,4})*");
	static Matcher m;
	
	public static int[] parseMemoryInputRam(String inputString) {
		int[] ram = new int[0x2000];
		int address;
		String input;
		m = memPattern.matcher(inputString);
		while(m.find()) {
			input = inputString.substring(m.start(), m.end()).substring(3);
			String[] s = input.split("\\s+");
			for(int i = 0; i < s.length - 1; i += 2) {
				address = Integer.parseInt(s[i], 16);
				ram[address] = Integer.parseInt(s[i + 1], 16);
			}
		}
		return ram;
	}
	
	public static int[] parseMemoryInputRom(String inputString) {
		int[] rom = new int[0x8000];
		int address = 0;
		String input;
		m = codePattern.matcher(inputString);
		while(m.find()) {
			input = inputString.substring(m.start(), m.end()).substring(3);
			String[] s = input.split("\\s+");
			for(int i = 0; i < s.length; i++) {
				rom[address++] = Integer.parseInt(s[i], 16);
			}
		}
		m = romPattern.matcher(inputString);
		while(m.find()) {
			input = inputString.substring(m.start(), m.end()).substring(3);
			String[] s = input.split("\\s+");
			for(int i = 0; i < s.length - 1; i += 2) {
				address = Integer.parseInt(s[i], 16);
				rom[address] = Integer.parseInt(s[i + 1], 16);
			}
		}
		return rom;
	}
	

}
