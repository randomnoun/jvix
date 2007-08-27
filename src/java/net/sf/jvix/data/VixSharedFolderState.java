package net.sf.jvix.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jvix.VixVM;

/** A data object to contain values return by the 
 * {@link VixVM#getSharedFolderState(int)} method.
 *
 * @author knoxg
 * @version $Id$
 *
 */
public class VixSharedFolderState {


	/** The name of the folder */
	private String name;
	
	/** The host path this folder is mounted from */ 
	private String host;
	
	/** Flags describing the folder options VIX_SHAREDFOLDER_WRITE_ACCESS */ 
	private int flags;

	/** Create an object used to return a value from the 
	 * {@link net.sf.jvix.data.VixVM#getSharedFolderState(int)} method.
	 * 
	 * @param name the name of the folder
	 * @param host the host path this folder is mounted from
	 * @param flags flags describing the folder options VIX_SHAREDFOLDER_WRITE_ACCESS
	 * 
	 */
	public VixSharedFolderState(String name, String host, int flags)
	{
		this.name = name;
		this.host = host;
		this.flags = flags;
	}

	/** Returns the name of the folder
	 * 
	 * @return the name of the folder
	 */
	public String getName() { return name; }
	
	/** Returns the host path this folder is mounted from
	 * 
	 * @return the host path this folder is mounted from
	 */ 
	public String getHost() { return host; }
	
	/** Returns flags describing the folder options VIX_SHAREDFOLDER_WRITE_ACCESS 
	 * 
	 * @return flags describing the folder options VIX_SHAREDFOLDER_WRITE_ACCESS
	 */
	public int getFlags() { return flags; }
}
