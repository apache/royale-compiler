package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.flex.utils.FilenameNormalization;


/**
 *  EnvProperties checks in following order for a value.
 * 
 *  1) unittest.properties 
 *  2) environment variables
 *  3) for key FLEX_HOME & PLAYERGLOBAL_HOME sets a default value.
 */
public class EnvProperties {
	
	/**
	 * FLEX_HOME
	 */
	public String SDK;
	
	/**
	 * PLAYERGLOBAL_HOME
	 */
	public String FPSDK;
	
	/**
	 * AIR_HOME
	 */
	public String AIRSDK;
	
	/**
	 * FLASHPLAYER_DEBUGGER
	 */
	public String FDBG;
	
	
	private static EnvProperties propertyReader;
	
	public static EnvProperties initiate() {
		if(propertyReader == null) {
			propertyReader = new EnvProperties();
			propertyReader.setup();
		}
		return propertyReader;
	}
	
	private void setup()
	{
		Properties p = new Properties();
		try {
			File f = new File("unittest.properties");
			p.load(new FileInputStream( f ));
		} catch (FileNotFoundException e) {
			System.out.println("unittest.properties not found");
		} catch (IOException e) {
		}
		
		SDK = p.getProperty("FLEX_HOME", System.getenv("FLEX_HOME"));
		if(SDK == null)
			SDK = FilenameNormalization.normalize("../compiler/generated/dist/sdk");		
		System.out.println("Env - FLEX_HOME = " + SDK);
		
		FPSDK = p.getProperty("PLAYERGLOBAL_HOME", System.getenv("PLAYERGLOBAL_HOME"));
		if(FPSDK == null)
			FPSDK = FilenameNormalization.normalize("../compiler/generated/dist/sdk/frameworks/libs/player");
		System.out.println("Env - PLAYERGLOBAL_HOME = " + FPSDK);

		
		AIRSDK = p.getProperty("AIR_HOME", System.getenv("AIR_HOME"));
		System.out.println("Env - AIR_HOME = " + AIRSDK);
		
		FDBG = p.getProperty("FLASHPLAYER_DEBUGGER", System.getenv("FLASHPLAYER_DEBUGGER"));
		System.out.println("Env - FLASHPLAYER_DEBUGGER = " + FDBG);
	}

}
