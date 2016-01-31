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
		//javaEditor(base, path, state, mode);
		this.mode=(GreatPrintMode)mode;
		lines=new ArrayList<Pos>();
		try{
			Field f=console.getClass().getDeclaredField("consoleTextPane");
			f.setAccessible(true);
			JTextPane consoleTextPane=(JTextPane)f.get(console);
			consoleTextPane.addCaretListener(new textPaneCangeCursor(consoleTextPane));
		}catch(Exception e){
			e.printStackTrace();
		}
		//console=new GreatPrintEditorConsole(this);
//		Box box=(Box)splitPane.getBottomComponent();
	}
	ArrayList<Pos> lines;
	public void setLine(int page,int line){
		lines.add(new Pos(page,line));
		if(lines.size()>3000){
			lines.remove(0);
		}
	}
	class Pos{
		int page;
		int line;
		Pos(int page,int line){
			this.page=page;
			this.line=line;
		}
		void warp(){
			getSketch().setCurrentCode(page);
			setSelection(getLineStartOffset(line),getLineStopOffset(line) - 1);
		}
	}
	class textPaneCangeCursor implements CaretListener{
		JTextPane textPane;
		public textPaneCangeCursor(JTextPane textPane){
			this.textPane=textPane;
		}
		public void caretUpdate(CaretEvent e){
		String str = textPane.getText();
		str = str.replaceAll("\r\n", "\n");
		int allLine=str.split("\n").length;
		str = str.substring(0, textPane.getSelectionEnd()) + "a";
		String line[] = str.split("\n");
		int index=lines.size()-(allLine-line.length+1);
		if(index<0||lines.size()<=index){
			return;
		}
		lines.get(index).warp();
		}
	}
}

