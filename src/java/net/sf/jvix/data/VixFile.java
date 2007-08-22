package net.sf.jvix.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jvix.VixVM;

/** A data object to contain values return by the 
 * {@link VixVM#listDirectoryInGuest(String)} method
 *
 * @TODO separate accessor methods for fileFlags components
 *  
 * @author knoxg
 * @version $Id$
 *
 */
public class VixFile {

	private String name;
	private int fileFlags;

	public VixFile(String name, int fileFlags)
	{
		this.name = name;
		this.fileFlags = fileFlags;
	}
	
	public String getName() { return name; }
	public int getFileFlags() { return fileFlags; }
}
