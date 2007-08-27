package net.sf.jvix;

/** Object-oriented wrapper for the VIX Snapshot API
 * 
 * @author knoxg
 * @version $Id$
 *
 */
public class VixSnapshot {

	/** VixHandle used to represent this snapshot */
	private VixHandle snapshotHandle;

	/** Create a new VixVM object. This constructor is not public; to create a new
	 * object call one of the snapshot methods in the {@link net.sf.jvix.VixVM} class
	 * 
	 * @param snapshotHandle the VixHandle representing this snapshot
	 */ 	
	VixSnapshot(VixHandle snapshotHandle)
	{
		this.snapshotHandle = snapshotHandle;
	}

   /** This function returns the number of child snapshots of a specified snapshot
    * 
    * @return The number of child snapshots belonging to the specified snapshot
    */
	public int getNumChildren() throws VixException {
		return VixWrapper.VixSnapshot_GetNumChildren(snapshotHandle);
	}

   /** This function returns the specified child snapshot. 
    * 
    * @param index index into the list of snapshots
    * 
    * @return A reference to the child snapshot
    */
	public VixSnapshot getChild(int index) throws VixException {
		VixHandle childSnapshotHandle = VixWrapper.VixSnapshot_GetChild(snapshotHandle, index);
		return new VixSnapshot(childSnapshotHandle);
	}

   /** This function returns the parent of a snapshot.
    * 
    * @return A handle to the parent of the specified snapshot.
    */
	public VixSnapshot getParent() throws VixException {
		VixHandle parentSnapshotHandle = VixWrapper.VixSnapshot_GetParent(snapshotHandle);
		return new VixSnapshot(parentSnapshotHandle);
	}

	/** Releases the resources associated with this snapshot.
	 * 
	 */
	public void close() {
		if (snapshotHandle != null) { 
			VixWrapper.Vix_ReleaseHandle(snapshotHandle);
			snapshotHandle = null; 
		}		
	}
	
	/** Returns the vix handle associated with this snapshot 
	 * 
	 * @return the vix handle associated with this snapshot
	 */
	public VixHandle getVixHandle() {
		return snapshotHandle;
	}
	

	/** Returns true if this snapshot is equal to the supplied object
	 * 
	 * @param other object to test for equality
	 * 
	 * @return true if this snapshot is equal to the supplied object
	 */  
	public boolean equals(Object other) {
		if (other==null) { return false; }
		if (! (other instanceof VixSnapshot)) { return false; }
		return snapshotHandle.equals(((VixSnapshot) other).getVixHandle());
	}
	
	/** Finalizes this object
	 * 
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}



}
