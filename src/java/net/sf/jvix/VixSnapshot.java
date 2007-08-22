package net.sf.jvix;

/** Object-oriented wrapper for the VIX Snapshot API
 * 
 * @author knoxg
 * @version $Id$
 *
 */
public class VixSnapshot {

	private VixHandle snapshotHandle;

	VixSnapshot(VixHandle snapshotHandle)
	{
		this.snapshotHandle = snapshotHandle;
	}

	public int getNumChildren() throws VixException {
		return VixWrapper.VixSnapshot_GetNumChildren(snapshotHandle);
	}

	public VixSnapshot getChild(int index) throws VixException {
		VixHandle childSnapshotHandle = VixWrapper.VixSnapshot_GetChild(snapshotHandle, index);
		return new VixSnapshot(childSnapshotHandle);
	}

	public VixSnapshot getParent() throws VixException {
		VixHandle parentSnapshotHandle = VixWrapper.VixSnapshot_GetParent(snapshotHandle);
		return new VixSnapshot(parentSnapshotHandle);
	}

	public void close() {
		if (snapshotHandle != null) { 
			VixWrapper.Vix_ReleaseHandle(snapshotHandle);
			snapshotHandle = null; 
		}		
	}
	
	public VixHandle getVixHandle() {
		return snapshotHandle;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}



}
