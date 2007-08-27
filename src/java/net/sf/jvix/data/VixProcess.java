package net.sf.jvix.data;

import net.sf.jvix.VixVM;

/** A data object to contain values return by the 
 * {@link VixVM#listProcessesInGuest()} method.
 *
 * @TODO separate accessor methods for fileFlags components
 *  
 * @author knoxg
 * @version $Id$
 *
 */
public class VixProcess {

	/** The process name */
	private String name;
	
	/** The process id */
	private long pid;
	
	/** The process owner */
	private String owner;
	
	/** The process command line */
	private String command;
	
	private int guestProgramElapsedTime;
	private int guestProgramExitCode;

	/** Create an object used to return a value from the 
	 * {@link net.sf.jvix.data.VixVM#listProcessesInGuest()} method.
	 * 
	 * @param name the process name
	 * @param pid the process id
	 * @param owner the process owner
	 * @param command the process command line
	 */	
	public VixProcess(String name, long pid, String owner, String command)
	{
		this.name = name;
		this.pid = pid;
		this.owner = owner;
		this.command = command;
	}
	
	/** Create an object to return a value from the 
	 * {@link net.sf.jvix.data.VixVM#runProgramInGuest()} method.
	 * 
	 * @param name the process name
	 * @param pid the process id
	 * @param guestProgramElapsedTime program elapsed time
	 * @param guestProgramExitCode program exit code
	 */
	public VixProcess(String name, long pid, int guestProgramElapsedTime, int guestProgramExitCode) 
	{
		this.name = name;
		this.pid = pid;
		this.guestProgramElapsedTime = guestProgramElapsedTime;
		this.guestProgramExitCode = guestProgramExitCode;
	}
	
	/** Returns the process name 
	 * 
	 * @return the process name
	 */
	public String getName() { return name; }
	
	/** Returns the process id 
	 * 
	 * @return the process id
	 */
	public long  getPid() { return pid; }
	
	/** Returns the process owner
	 * 
	 * @return the process owner
	 */
	public String getOwner() { return owner; }
	
	/** Returns the process command line
	 * 
	 * @return the process command line
	 */
	public String getCommand() { return command; }
	
	
	/** Returns the guest program elapsed time
	 * 
	 * @return the guest program elapsed time
	 */
	public int getGuestProgramElapsedTime() { return guestProgramElapsedTime; }
	
	/** Returns the guest program exit code
	 * 
	 * @return the guest program exit code
	 */
	public int getGuestProgramExitCode() { return guestProgramExitCode; }
	
}
