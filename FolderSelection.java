package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

public class FolderSelection extends JPanel{

	private static final long serialVersionUID = 1L;
	private JScrollPane scrollTable;
	private static Font font = new Font("SansSerif", Font.PLAIN, 20);
	private Color txtBackColor = Color.white;
	private Color backgroundColor = new Color(141,182,205);
	private static Insets bottom = new Insets(0,0,20,0);
	private File[] artists;
	private File folder;
	private ArrayList<File> songs;
	private ArrayList<AudioFile> duplicates;
	private JTable target;
	private boolean folderOpened = false;
	private MainWindow main;
	Logger logger;

	
	public FolderSelection(MainWindow main) {
		this.main = main;
		createPanel();
	}
	
	/**
	 * Creates the Panel for opening the Music folder
	 * Updates Results panel with Albums and Songs
	 */
	public void createPanel() {
		
		DuplicateLogger duplicateLogger = new DuplicateLogger();
		logger = duplicateLogger.getLogger();
		logger.setLevel(Level.ALL);
		songs = new ArrayList<File>();
		
		this.setLayout(new GridBagLayout());
		
		final Text find = new Text();
		find.setText("Click 'Open Folder' to open your music folder.");
		find.setPreferredSize(new Dimension(250,70));
		find.setEditable(false);
		find.setBackground(backgroundColor);
		find.setBorder(BorderFactory.createEmptyBorder());
		
		JLabel warning = new JLabel();
		warning.setText("<html>" + "*Warning: Clicking delete will \r\n PERMANENTLY remove files from \n your computer."
		 + "</html>");
		warning.setPreferredSize(new Dimension(200, 50));
		
		//Open the folder.
		JButton findButton = new JButton("Open Folder");
		findButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				openFolder();
			}
		});
		
		JButton listButton = new JButton("List Songs");
		listButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				//Get folder location
				if(!folderOpened) 
					openFolder();

				if(!songs.isEmpty())
					songs = new ArrayList<File>();
				for(File f : artists) 
					if(f.isDirectory()) 
						getSongs(f);
				logger.info("There are " + songs.size() + " songs");
				Collections.sort(songs, SongSort.TITLE_ORDER);
				String[][] data = new String[songs.size()][2];
				int i = 0;
				for(File f : songs)
				{
					data[i][0] = f.getName();
					data[i][1] = f.getAbsolutePath();
					i++;
				}

				String[] listHeader = new String[] 
						{"Song Title", "File Location"};
				JTable resultTable = new JTable(data, listHeader);
				resultTable.addMouseListener(new MouseListener() {

					@Override
					public void mouseClicked(MouseEvent arg0) {
						target = (JTable) arg0.getSource();
					}

					@Override
					public void mouseEntered(MouseEvent arg0) {
					}

					@Override
					public void mouseExited(MouseEvent arg0) {
					}

					@Override
					public void mousePressed(MouseEvent arg0) {
					}

					@Override
					public void mouseReleased(MouseEvent arg0) {							
					}

				});
				scrollTable = new JScrollPane(resultTable);
				scrollTable.setName("scroll");
				main.updateFolderList(scrollTable);
			}
		});

		//Deletes selected duplicate songs
		JButton deleteButton = new JButton("Delete Selected*");
		deleteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ArrayList<File> deletes = new ArrayList<File>();
				if(target != null)
				{
					int[] rows = target.getSelectedRows();
					for(int i : rows)
					{
						String filePath = (String) target.getValueAt(i, 3);
						deletes.add(new File(filePath));
					}
					deleteFiles(deletes);
					DefaultTableModel mytable = (DefaultTableModel)target.getModel();
					for(int i = rows.length -1; i >= 0; i--)
						mytable.removeRow(rows[i]);
					target.revalidate();
				}
				else {
					JOptionPane.showMessageDialog(null, "Please select at least one row");
				}
			}
		});
		
		//Displays all duplicates
		JButton displayDuplicates = new JButton("Display Duplicates");
		displayDuplicates.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(!folderOpened)
				{
					openFolder();
					return;
				}
				resetDataStructures();
				for(File f : artists) 
					if(f.isDirectory()) 
						getSongs(f);
	
				logger.info("There are " + songs.size() + " songs");
				Collections.sort(songs, SongSort.TITLE_ORDER);
				long start = System.currentTimeMillis();
				getDuplicates();
				long end = System.currentTimeMillis();
				logger.info("Took " + (end - start) + " ms to find duplicates.");
				JOptionPane.showMessageDialog(null, "Found " + duplicates.size()/2 + " possible duplicates.");
				String[][] data = new String[duplicates.size()][4];
				int i = 0;
				for(AudioFile f: duplicates)
				{
					Tag tag = f.getTag();
					File p = f.getFile();
					data[i][0] = tag.getFirst(FieldKey.TITLE);
					data[i][1] = tag.getFirst(FieldKey.ARTIST);
					data[i][2] = tag.getFirst(FieldKey.ALBUM);
					data[i][3] = p.getAbsolutePath();
					i++;
				}
				String[] duplicateHeader = new String[]
						{ "Song Title", "Artist", "Album", "File Location"};
				JTable resultTable = new JTable(data, duplicateHeader);
				resultTable.setModel(new DefaultTableModel(data, duplicateHeader));
				resultTable.addMouseListener(new MouseListener() {

					@Override
					public void mouseClicked(MouseEvent arg0) {
						target = (JTable) arg0.getSource();
					}

					@Override
					public void mouseEntered(MouseEvent arg0) {
					}

					@Override
					public void mouseExited(MouseEvent arg0) {
					}

					@Override
					public void mousePressed(MouseEvent arg0) {
					}

					@Override
					public void mouseReleased(MouseEvent arg0) {							
					}
					
				});
				scrollTable = new JScrollPane(resultTable);
				scrollTable.setName("scroll");
				main.updateFolderList(scrollTable);
			}		
		});
		
		this.setPreferredSize(new Dimension(300,getHeight()));
		
		//Add components to the GUI
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets = bottom;
		this.add(find, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.insets = bottom;
		this.add(findButton, c);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		this.add(displayDuplicates, c);
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.CENTER;
		this.add(deleteButton, c);
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.CENTER;
		this.add(listButton, c);
		c.gridx = 0;
		c.gridy = 5;
//		c.gridwidth = 2;
//		c.anchor = GridBagConstraints.CENTER;
		this.add(warning, c);
		this.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		this.setBackground(backgroundColor);
	}
	
	/**
	 * Deletes all selected files in the JTable
	 * @param 
	 * 		List(Files) to delete
	 */
	private void deleteFiles(ArrayList<File> files) {
		for(File f : files)
			try {
				f.delete();
			} catch (Exception e) {
				System.out.println("Unable to delete file: " + f.getName());
			}
	}
	
	private void resetDataStructures() {
		artists = null;
		artists = folder.listFiles();
		songs = new ArrayList<File>();
		duplicates =new ArrayList<AudioFile>();
	}
	
	private void openFolder() {
		//Get folder location
		JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setPreferredSize(new Dimension(600,400));
		int returnVal = fc.showOpenDialog(FolderSelection.this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			folder = fc.getSelectedFile();
			artists = null;
			artists = folder.listFiles();
			duplicates = new ArrayList<AudioFile>();
			Text open = new Text();
			open.setText("Successfully Opened Folder: " + folder.getPath());
			folderOpened = true;
			open.setFont(font);
			open.setName("text");
			main.updateFolderList(open);
		}
	}
	
	/**
	 * 
	 * @param file
	 * 		Folder containing all songs and albums
	 */
	private void getSongs(File file) {
		for(File f : file.listFiles())
		{
			if(f.isDirectory())
				getSongs(f);
			else {
				String fileName = f.getName();
				int lastPeriod = fileName.lastIndexOf('.');
				String fileType = fileName.substring(lastPeriod+1);
				if(fileType.toLowerCase().contains("mp3") || fileType.toLowerCase().contains("m4a"))
				{
					songs.add(f);
				}
			}
		}		
	}
	
	/**
	 * Iterate through all the songs and find duplicates
	 */
	private void getDuplicates() {
		duplicates = new ArrayList<AudioFile>();
		Iterator<File> itr = songs.iterator();
		HashSet<Integer> songsInDuplicates = new HashSet<Integer>();
    	File previous = null;
    	while(itr.hasNext())
    	{
    		File curr = itr.next();
    		if(previous == null){
    			previous = curr;
    			continue;
    		}
  
    		String song1 = previous.getName();
    		String song2 = curr.getName();
    		song1 = song1.replaceAll("[0-9\\s()\\.]","");
    		song2 = song2.replaceAll("[0-9\\s()\\.]","");
    		logger.info(song1);
    		logger.info(song2);
    		
    		if(song1.equalsIgnoreCase(song2)) {
    			AudioFile aFile1 = null;
    			AudioFile aFile2 = null;
    			try {
    				aFile1 = AudioFileIO.read(previous);
					aFile2 = AudioFileIO.read(curr);
				} catch (CannotReadException e) {
					logger.severe("Cannot Read Exception");
					e.printStackTrace();
				} catch (IOException e) {
					logger.severe("IOException");
					e.printStackTrace();
				} catch (org.jaudiotagger.tag.TagException e) {
					logger.severe("TagException");
					e.printStackTrace();
				} catch (ReadOnlyFileException e) {
					logger.severe("Read Only File Exception");
					e.printStackTrace();
				} catch (InvalidAudioFrameException e) {
					logger.severe("Invalid Audio Frame Exception");
					e.printStackTrace();
				}
    			
    			Tag tag1, tag2;
    			tag1 = aFile1.getTag();
    			tag2 = aFile2.getTag();
    			String genre1, genre2, artist1, artist2;
    			try {
    				genre1 = tag1.getFirst(FieldKey.GENRE);
    				genre2 = tag2.getFirst(FieldKey.GENRE);
    				artist1 = tag1.getFirst(FieldKey.ARTIST);
    				artist2 = tag2.getFirst(FieldKey.ARTIST);
    			} catch (NullPointerException e) {
    				logger.warning(song1 + " or" + song2 + " has no Tag. Skipping");
    				previous = curr;
    				continue;
    			}

				if(artist1.equalsIgnoreCase(artist2))
				{					
					//Check if its an audio book file
					if(isAudioBook(genre1, genre2))
					{
						if(song1.equalsIgnoreCase(song2))
						{
							//Check if the song has already been added to duplicates list
							if(!songsInDuplicates.contains(previous.hashCode()))
							{
								duplicates.add(aFile1);
								duplicates.add(aFile2);
								songsInDuplicates.add(previous.hashCode());
								songsInDuplicates.add(curr.hashCode());
								previous = curr;
								continue;
							}
							else {
								duplicates.add(aFile2);
								songsInDuplicates.add(curr.hashCode());
								previous = curr;
								continue;
							}
						}
					}
					else 
					{
						//Check if the song has already been added to duplicates list
						if(!songsInDuplicates.contains(previous.hashCode()))
						{
							duplicates.add(aFile1);
							duplicates.add(aFile2);
							songsInDuplicates.add(previous.hashCode());
							songsInDuplicates.add(curr.hashCode());
							previous = curr;
							continue;
						}
						else
						{
							duplicates.add(aFile2);
							songsInDuplicates.add(curr.hashCode());
							previous = curr;
							continue;
						}
					}	
				}
    		}
    		else {
    			previous = curr;
    			continue;
    		}
    	}
	}
	
	/**
	 * Overwriting JTextArea to add custom font, line wrap 
	 * and font color
	 * 
	 * @author Abe Friesen
	 */
	private class Text extends JTextArea {

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
	 * Custom Comparator to compare just the strings of song
	 * titles, not taking into account their track numbers
	 */
	private static class SongSort { 
		static final Comparator<File> TITLE_ORDER =
				new Comparator<File>() {

			@Override
			public int compare(File f1, File f2) {
				String song1 = f1.getName();
				String song2 = f2.getName();
				song1 = song1.replaceAll("[0-9,\\s\\.]","");
				song2 = song2.replaceAll("[0-9,\\s\\.]","");

				return song1.compareToIgnoreCase(song2);
			}
		};
	}
	
	/**
	 * Checks if genre of audio file is an audio book.
	 * Used to perform more rigorous testing.
	 * @return
	 * 		True if audio file is an audio book, False otherwise
	 */
	private boolean isAudioBook(String genre1, String genre2) {
		if(genre1.toUpperCase().contains("BOOK") && genre2.toUpperCase().contains("BOOK"))
			return true;
		else
			return false;
	}
}
