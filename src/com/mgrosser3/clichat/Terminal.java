package com.mgrosser3.clichat;

class Terminal {

	public static void clearScreen() {
		System.out.print("\033[2J");
		System.out.flush();
	}
		
}
