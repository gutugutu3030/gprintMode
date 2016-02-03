package basho;

import java.awt.*;

import java.lang.reflect.Field;

import javax.swing.*;
import processing.app.*;
import processing.mode.java.*;
import java.util.*;
import javax.swing.event.*;

import org.omg.Messaging.SyncScopeHelper;
import javax.swing.text.*;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

public class GreatPrintEditor extends JavaEditor {
	public GreatPrintMode mode;
	JTextPane consoleTextPane = null;
	DefaultHighlightPainter highlightPainter;

	protected GreatPrintEditor(Base base, String path, EditorState state, Mode mode) {
		super(base, path, state, mode);
		// javaEditor(base, path, state, mode);
		this.mode = (GreatPrintMode) mode;
		lines = new ArrayList<Pos>();
		highlightPainter=new DefaultHighlighter.DefaultHighlightPainter(new Color(155,118,19));
		this.textarea.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				setConsoleLine(sketch.getCurrentCodeIndex(), getCurrentLineIndex());
			}
		});
		try {
			Field f = console.getClass().getDeclaredField("consoleTextPane");
			f.setAccessible(true);
			consoleTextPane = (JTextPane) f.get(console);
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

	long searched=0;
	public void setConsoleLine(int page, int line) {
		if(System.currentTimeMillis()-searched<=2){
			return;
		}
		Highlighter highlighter=consoleTextPane.getHighlighter();
		highlighter.removeAllHighlights();
		for (int i = lines.size() - 1; i >= 0; i--) {
			Pos pos = lines.get(i);
			if (pos.is(page, line)) {
				String str = consoleTextPane.getText();
				str = str.replaceAll("\r\n", "\n");
				String allLine[] = str.split("\n");
				int fe[]=getCaretOfLine(str,allLine.length-(lines.size()-i));
				try {
					searched=System.currentTimeMillis();
	                highlighter.addHighlight(fe[0]+1,fe[1], highlightPainter);
	            } catch (BadLocationException e) {
	                e.printStackTrace();
	            }
			}
		}
	}

	int[] getCaretOfLine(String str, int x) {
		int first = 0;
		int end = 0;
		char c[] = str.toCharArray();
		int line = 0;
		for (int i = 0; i < c.length; i++) {
			if (c[i] != '\n') {
				continue;
			}
			line++;
			if (line == x) {
				first = i;
				continue;
			}
			if (line == x+1) {
				end = i;
				return new int[]{first,end};
			}
		}
		return new int[]{first,c.length};
	}

	int getCurrentLineIndex() {
		int index = getCaretOffset();
		for (int i = 0, n = getLineCount(); i < n; i++) {
			if (index <= getLineStopOffset(i)) {
				return i;
			}
		}
		return -1;
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

		boolean is(int page, int line) {
			return this.page == page && this.line == line;
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
			if(System.currentTimeMillis()-searched<=2){
				return;
			}
			textPane.getHighlighter().removeAllHighlights();

			try {
				String str = textPane.getText();
				str = str.replaceAll("\r\n", "\n");
				String allLine[] = str.split("\n");
				str = str.substring(0, textPane.getSelectionEnd()) + "a";
				int index = str.split("\n").length - 1;
				if (allLine.length <= index) {
					return;
				}
				String mes = allLine[index].substring(0, allLine[index].length() - 1);
				for (int i = lines.size() - 1; i >= 0; i--) {
					Pos pos = lines.get(i);
					if (pos.is(mes)) {
						searched=System.currentTimeMillis();
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
