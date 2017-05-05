package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MainWindow extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JScrollPane scrollPane, scrollTable, currentTable;
	JPanel folderSelection, folderList, container;
	JTextArea currentResults;
	private static Font font = new Font("SansSerif", Font.BOLD, 17);
	private Color txtBackColor = Color.white;
	private Component comp;

	
	public MainWindow() {
		super("Effingo");
		
		folderSelection = new FolderSelection(this);
		comp = folderSelection.getComponent(1);
		//Results
		Text results = new Text();
		results.setText("RESULTS WILL GO HERE");
		results.setFont(font);
		results.setEditable(false);
		currentResults = results;
		
		folderList = new JPanel(new BorderLayout());
		folderList.add(currentResults, BorderLayout.PAGE_START);
		folderList.setBackground(new Color(00,66,99));
		folderList.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		this.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				folderList.setPreferredSize(new Dimension(container.getWidth() - folderSelection.getWidth(), container.getHeight()));
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
			}
			
			@Override
			public void componentHidden(ComponentEvent arg0) {
			}
		});
		
		container = new JPanel(new BorderLayout());
		container.add(folderSelection, BorderLayout.WEST);
		container.add(folderList, BorderLayout.CENTER);
		
		scrollPane = new JScrollPane(container);
		this.add(scrollPane);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(1000, 500);
		this.setVisible(true);
		//Request focus on the input field
		this.addWindowListener(new WindowAdapter() {
		    public void windowOpened( WindowEvent e ){
		        comp.requestFocus();
		    }
		});
	}
	
	/**
	 * Updates the results portion of the GUI
	 * @param component
	 * 		The component to be added to the GUI
	 */
	public void updateFolderList(JComponent component) {
		if(component.getName().contentEquals("text"))
		{
				folderList.remove(currentResults);
				currentResults = (JTextArea) component;
				currentResults.setAlignmentY(CENTER_ALIGNMENT);
				folderList.add(currentResults, BorderLayout.PAGE_START);
				folderList.revalidate();
		}
		else {
			if(currentTable != null)
				folderList.remove(currentTable);
			currentTable = (JScrollPane) component;
			folderList.add(currentTable);
			folderList.revalidate();
		}
	}
	
	/**
	 * Overwriting JTextArea to add custom font, line wrap 
	 * and font color
	 * 
	 * @author Abe Friesen
	 */
	private class Text extends JTextArea {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private Text() {
			super();
			setFont(font);
			setLineWrap(true);
			setWrapStyleWord(true);
			setOpaque(false);
			setBackground(txtBackColor);
			setForeground(Color.BLACK);
		}
	}
	
	/**
	 * Main Method
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException e){}
		catch (InstantiationException e) {}
		catch (IllegalAccessException e) {}
		catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		new MainWindow();
	}
}
