package basho;

import java.awt.*;
import java.lang.reflect.Field;

import javax.swing.*;
import processing.app.*;
import processing.mode.java.*;
import java.util.*;
import javax.swing.event.*;

public class GreatPrintEditor extends JavaEditor {
	public GreatPrintMode mode;

	protected GreatPrintEditor(Base base, String path, EditorState state, Mode mode) {
		super(base, path, state, mode);
		// javaEditor(base, path, state, mode);
		this.mode = (GreatPrintMode) mode;
		lines = new ArrayList<Pos>();
		try {
			Field f = console.getClass().getDeclaredField("consoleTextPane");
			f.setAccessible(true);
			JTextPane consoleTextPane = (JTextPane) f.get(console);
			consoleTextPane.addCaretListener(new textPaneCangeCursor(consoleTextPane));
		} catch (Exception e) {
			e.printStackTrace();
		}
		// console=new GreatPrintEditorConsole(this);
		// Box box=(Box)splitPane.getBottomComponent();
	}

	ArrayList<Pos> lines;

	public void setLine(int page, int line, String mes) {
		lines.add(new Pos(page, line, mes));
		if (lines.size() > 3000) {
			lines.remove(0);
		}
	}

	class Pos {
		int page;
		int line;
		String mes;

		Pos(int page, int line, String mes) {
			this.page = page;
			this.line = line;
			this.mes = mes;
		}

		boolean is(String str) {
			return mes.equals(str);
		}

		void warp() {
			getSketch().setCurrentCode(page);
			setSelection(getLineStartOffset(line), getLineStopOffset(line) - 1);
		}
	}

	class textPaneCangeCursor implements CaretListener {
		JTextPane textPane;

		public textPaneCangeCursor(JTextPane textPane) {
			this.textPane = textPane;
		}

		public void caretUpdate(CaretEvent ev) {
			try {
				String str = textPane.getText();
				str = str.replaceAll("\r\n", "\n");
				String allLine[] = str.split("\n");
				str = str.substring(0, textPane.getSelectionEnd()) + "a";
				int index=str.split("\n").length-1;
				if(allLine.length<=index){
					return;
				}
				String mes = allLine[index].substring(0,allLine[index].length()-1);
				for (int i = lines.size() - 1; i >= 0; i--) {
					Pos pos = lines.get(i);
					if (pos.is(mes)) {
						pos.warp();
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
