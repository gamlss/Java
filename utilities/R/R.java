/*
 * Copyright (C) 2008-2010 by Holger Arndt
 *
 * This file is part of the Universal Java Matrix Package (UJMP).
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * UJMP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * UJMP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with UJMP; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */
package gamlss.utilities.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class R {

	public static String[] SEARCH = new String[] {};

	static {
		try {
			SEARCH = new String[] { System.getProperty("R"), "/usr/bin/R", "/opt/R/bin/R" };
		} catch (Exception e) {
		}
	}

	public static final int POLLINTERVAL = 100;

	public static final int MAXPOLLS = 10;

	private BufferedReader input = null;

	private BufferedWriter output = null;

	private BufferedReader error = null;

	private Process rProcess = null;

	private boolean running = false;

	private static R r = null;

	private static String pathToR = null;

	public static synchronized R getInstance()  {
		if (r == null) {
			try {
				r = getInstance(findR());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return r;
	}

	public static synchronized R getInstance(String pathToR) throws Exception {
		if (r == null) {
			r = new R(pathToR);
		}
		return r;
	}

	private R(String pathToR) throws Exception {
		rProcess = Runtime.getRuntime().exec(pathToR + " --no-save --no-readline");
		output = new BufferedWriter(new OutputStreamWriter(rProcess.getOutputStream()));
		input = new BufferedReader(new InputStreamReader(rProcess.getInputStream()));
		error = new BufferedReader(new InputStreamReader(rProcess.getErrorStream()));
		String startMessage = getFromR();
		if (startMessage != null && startMessage.length() > 0) {
			running = true;
			return;
		}
		throw new Exception("could not start R");
	}

	private synchronized String getFromR() throws Exception {
		boolean lfSeen = false;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < MAXPOLLS; i++) {
			if (!input.ready()) {
				Thread.sleep(POLLINTERVAL);
			} else {
				break;
			}
		}

		while (input.ready()) {

			char c = (char) input.read();
			sb.append(c);

			if (lfSeen) {
				if (c == '>') {
					return sb.toString();
				} else {
					lfSeen = false;
				}
			}
			if (c == '\n') {
				lfSeen = true;
			}

		}
		return sb.toString();
	}

	public String execute(String command) throws Exception {
		sendToR(command);
		String cur = "";
		String last = "";

		cur = getFromR();

		while (cur != null && cur.length() > 0) {
			last = cur;
			cur = getFromR();
		}

		return last;
	}

	public synchronized void shutdown() throws Exception {
		r = null;
		sendToR("q()");
		rProcess.waitFor();
		output.close();
		input.close();
	}

	private synchronized void sendToR(String command) throws Exception {
		if (r != null) {
			try {
				if (!command.endsWith("\n")) {
					command += "\n";
				}
				output.write(command, 0, command.length());
				output.flush();
			} catch (IOException e) {
				shutdown();
			}
		}
	}

	
	private static String findR() {
		if (pathToR == null) {
			File file = null;
			for (String s : SEARCH) {
				if (s != null) {
					file = new File(s);
					if (file.exists()) {
						pathToR = file.getAbsolutePath();
						return pathToR;
					}
				}
			}
		}
		return pathToR;
	}

	
	public static boolean isAvailable() {
		return findR() != null;
	}

	public static String toString(String[] strings) {
		if (strings.length != 0) {
			return "," + strings[0];
		} else {
			return "";
		}
	}

	/**
	 * Loads an R library
	 * @param library
	 * @throws Exception
	 */
	public void loadLibrary(String library)
	{
		try {
			execute("library("+library+")");
			execute("x11()");
			execute("hist(rnorm(10000))");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Installs an R library
	 * @param library
	 * @throws Exception
	 */
	public void installPackage(String packageName) 
	{
		try {
			execute("install.packages("+packageName+")");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void plotWP(double[] residuals)
	{
		
	}
	
	/**
	 * plots the gamlss worm plots
	 * @throws Exception
	 */
	public void wp() throws Exception 
	{
		execute("X11()");
		
	}

	public void closeLastFigure() throws Exception {
		execute("dev.off()");
	}

}
