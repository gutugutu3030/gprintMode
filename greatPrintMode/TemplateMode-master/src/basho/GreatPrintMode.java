/*
 * （利用するソフトウェア名）
Basho

The MIT License

Copyright (c) 2013 kwzr

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package basho;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Enumeration;

import javax.print.DocFlavor.URL;

import processing.mode.java.*;
import processing.mode.java.runner.*;
import processing.app.*;

import com.illposed.osc.*;

/**
 * Mode Template for extending Java mode in Processing IDE 2.0 or later.
 *
 */
public class GreatPrintMode extends JavaMode implements OSCListener {

	String addCode, addSetup;
	OSCPortIn myOSCPort;
	GreatPrintEditor editor;

	public GreatPrintMode(Base base, File folder) {
		super(base, folder);

		// Fetch examples and reference from java mode
		examplesFolder = Base.getContentFile("modes/java/examples");
		referenceFolder = Base.getContentFile("modes/java/reference");
		addCode = readTXT(modePath("../addCode.txt"));
		addSetup = readTXT(modePath("../addSetup.txt"));

		try {
			myOSCPort = new OSCPortIn(9978);
			myOSCPort.addListener("/test", this);
			myOSCPort.startListening();
			System.out.println("osc ok");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return the pretty/printable/menu name for this mode. This is separate
	 * from the single word name of the folder that contains this mode. It could
	 * even have spaces, though that might result in sheer madness or total
	 * mayhem.
	 */
	@Override
	public String getTitle() {
		return "GPrint";
	}

	/**
	 * Create a new editor associated with this mode.
	 */
	public Editor createEditor(Base base, String path, EditorState state) {
		// editor = new SlideEditor(base, path, state, this);
		// editor =new JavaEditor(base, path, state, this);
		// mt.setEditor(this);
		editor = new GreatPrintEditor(base, path, state, this);
		return editor;
	}

	/**
	 * Returns the default extension for this editor setup.
	 */
	/*
	 * @Override public String getDefaultExtension() { return null; }
	 */

	/**
	 * Returns a String[] array of proper extensions.
	 */
	/*
	 * @Override public String[] getExtensions() { return null; }
	 */

	/**
	 * Get array of file/directory names that needn't be copied during "Save
	 * As".
	 */
	/*
	 * @Override public String[] getIgnorable() { return null; }
	 */
	public Runner handleRun(Sketch sketch, RunnerListener listener) throws SketchException {
		// 普通にrunボタンが押された場合
		SketchCode[] code = sketch.getCode();

		String oldC[] = new String[code.length];

		// printlnの差し替え
		big: for (int i = 0; i < code.length; i++) {
			String c = code[i].getProgram();
			oldC[i] = c;
			StringBuilder sb = new StringBuilder();
			String lines[] = c.split("\n");
			for (int j = 0; j < lines.length; j++) {
				sb.append(lines[j].replaceAll("println\\(", "println1\\(" + i + "," + j + ","));
				sb.append('\n');
			}
			code[i].setProgram(sb.toString());
		}

		// setupの中の追記
		int stack = 0;
		int setupflg = -1;
		big: for (int i = 0; i < code.length; i++) {
			String c = code[i].getProgram();
			StringBuilder sb = new StringBuilder();
			String lines[] = c.split("\n");
			for (int j = 0; j < lines.length; j++) {
				if (j >= 1 && lines[j - 1].indexOf("setup()") != -1) {
					sb.append(addSetup);
					sb.append('\n');
				}
				sb.append(lines[j]);
				sb.append('\n');
			}
			code[i].setProgram(sb.toString());
		}

		// class Bashoの追記
		String c = code[0].getProgram();
		code[0].setProgram(addCode + c);
		JavaBuild build = new JavaBuild(sketch);
		String appletClassName = build.build(false);
		if (appletClassName != null) {
			final Runner runtime = new Runner(build, listener);
			new Thread(new Runnable() {
				public void run() {
					runtime.launch(false);
				}
			}).start();
			return runtime;
		}
		return null;
	}

	/*
	 * public Runner handlePresent(Sketch sketch, RunnerListener listener)
	 * throws SketchException { //全画面モードでrunボタンが押された場合
	 * System.out.println("hundlepresent"); return super.handlePresent(sketch,
	 * listener); }
	 */

	private boolean isSketchModified(Sketch sketch) {
		for (SketchCode sc : sketch.getCode()) {
			if (sc.isModified()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieve the ClassLoader for JavaMode. This is used by Compiler to load
	 * ECJ classes. Thanks to Ben Fry.
	 *
	 * @return the class loader from java mode
	 */
	@Override
	public ClassLoader getClassLoader() {
		for (Mode m : base.getModeList()) {
			if (m.getClass() == JavaMode.class) {
				JavaMode jMode = (JavaMode) m;
				return jMode.getClassLoader();
			}
		}
		return null; // badness
	}

	public String modePath(String str) {
		return folder.getAbsolutePath().replace("\\", "/") + "/mode/" + str;
	}

	public String readTXT(String path) {
		// String str="";
		StringBuilder str = new StringBuilder();
		BufferedReader br = null;
		try {
			File file = new File(path);
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				// str += line + "\n";
				str.append(line);
				str.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// ストリームは必ず finally で close します。
				br.close();
			} catch (IOException e) {
			}
		}
		// return str;
		return new String(str);
	}

	public void acceptMessage(java.util.Date date, OSCMessage oSCMessage) {
		Object args[] = oSCMessage.getArguments();
		byte b[] = new byte[args.length];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) ((int) ((Integer) args[i]));
		}
		try {
			String data = new String(b, "UTF8");
			String dsplit[] = data.split(",");
			int page = Integer.parseInt(dsplit[0]);
			int line = Integer.parseInt(dsplit[1]);
			StringBuilder sb = new StringBuilder();
			sb.append(page);
			sb.append(",");
			sb.append(line);
			sb.append(",");
			String mes = data.substring(sb.length());
			editor.setLine(page, line, mes);
		} catch (Exception e) {
		}
	}

}
