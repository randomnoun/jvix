package net.sf.jvix.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jvix.VixVM;

/** A data object to contain values return by the 
 * {@link VixVM#listDirectoryInGuest(String)} method.
 *
 * @TODO separate accessor methods for fileFlags components
 *  
 * @author knoxg
 * @version $Id$
 *
 */
public class VixFile {

	/** The file name */
	private String name;
	 
	/** File attribute flags */
	private int fileFlags;

	/** Create an object used to return a value from the 
	 * {@link net.sf.jvix.data.VixVM#listDirectoryInGuest(String)} method.
	 * 
	 * @param name the file name
	 * @param fileFlags file attribute flags
	 */	
	public VixFile(String name, int fileFlags)
	{
		this.name = name;
		this.fileFlags = fileFlags;
	}

	/** Returns the file name
	 * 
	 * @return the file name
	 */
	public String getName() { return name; }
	
	/** Returns the file attribute flags
	 * 
	 * @return the file attribute flags
	 */
	public int getFileFlags() { return fileFlags; }
}
