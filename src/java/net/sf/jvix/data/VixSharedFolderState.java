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

	private String name;
	private String host;
	private int flags;

	public VixSharedFolderState(String name, String host, int flags)
	{
		this.name = name;
		this.host = host;
		this.flags = flags;
	}
	
	public String getName() { return name; }
	public String getHost() { return host; }
	public int getFlags() { return flags; }
}
