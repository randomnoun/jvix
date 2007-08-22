package net.sf.jvix.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jvix.VixVM;

/** A data object to contain values return by the 
 * {@link VixVM#listProcessesInGuest()} method
 *
 * @TODO separate accessor methods for fileFlags components
 *  
 * @author knoxg
 * @version $Id$
 *
 */
public class VixProcess {

	private String name;
	private long pid;
	private String owner;
	private String command;

	public VixProcess(String name, long pid, String owner, String command)
	{
		this.name = name;
		this.pid = pid;
		this.owner = owner;
		this.command = command;
	}
	
	public String getName() { return name; }
	public long  getPid() { return pid; }
	public String getOwner() { return owner; }
	public String getCommand() { return command; }
	
}
