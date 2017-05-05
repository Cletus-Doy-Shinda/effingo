package ui;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Logging information for error checking 
 * 
 * @author Abe Friesen
 */
public class DuplicateLogger {
	
	Logger logger;
	
	public DuplicateLogger() {
		createLogger();
	}

	private void createLogger() {
		logger = Logger.getLogger("DuplicateLog");
		FileHandler fh;
		
		try {
			fh = new FileHandler("C:/Temp/DuplicateFinder.log");
			logger.addHandler(fh);
			
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Logger getLogger() {
		return this.logger;
	}
}
