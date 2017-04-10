package com.bibler.awesome.bibnes20.utilities;

import java.util.Arrays;

public class StringUtilities {
	
	public static String convertIntToFourPaddedHex(int number) {
		String s = "";
		if(number > 0xFFF) {
			s = "";
		} else if(number > 0xFF) {
			s = "0";
		} else if(number > 0xF) {
			s = "00";
		} else {
			s = "000";
		}
		String returnString = s + Integer.toHexString(number).toUpperCase();
		return returnString;
	}
	
	public static String convertIntToTwoPaddedHex(int number) {
		String s = "";
		if(number > 0xF) {
		} else {
			s = "0";
		}
		return (s + Integer.toHexString(number)).toUpperCase();
	}
	
	public static String padZeroes(int number, int totalLength, int base) {
		String binString = "";
		if(base == 2) {
			binString = Integer.toBinaryString(number);
		} else if(base == 10) {
			binString = "" + number;
		} else if(base == 16) {
			binString = Integer.toHexString(number);
		}
		int length = totalLength - binString.length();
		if(length <= 0) {
			return binString;
		}
		char[] padArray = new char[length];
		Arrays.fill(padArray, '0');
		String padString = new String(padArray);
		return padString + binString;
	}

}
