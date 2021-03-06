package net.sf.jvix;

/**
 * Wrapper for VixHandle pointer.
 *
 * @version $Id$
 * @author knoxg
 */
public class VixHandle {
	
	public static final VixHandle VIX_INVALID_HANDLE = new VixHandle(0);
	
	/** Opaque pointer to VixHandle value */
	private int value;
	
	/** Create a new VixHandle wrapper object 
	 * 
	 * @param value the raw VixHandle value
	 */
	public VixHandle(int value) {
		this.value = value;
	}
	
	/** Tests for equality 
	 * 
	 * @param other other object
	 * 
	 */
	public boolean equals(Object other) {
		if (other==null) { return false; }
		if (! (other instanceof VixHandle)) { return false; }
		return value == ((VixHandle) other).value;
	}
}
