package net.sf.jvix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jvix.data.VixFile;
import net.sf.jvix.data.VixProcess;
import net.sf.jvix.data.VixSharedFolderState;

/** Object-oriented wrapper for the VIX virtual machine API
 * 
 * @author knoxg
 * @version $Id$
 */
public class VixVM {

	/** the containing host for this virtual machine */
	private VixHost   host;
	
	/** the VixHandle representing this virtual machine */ 
	private VixHandle vmHandle;

	/** Create a new VixVM object. This constructor is not public; to create a new
	 * object call the {@link VixHost#open(String)} method
	 * 
	 * @param host the containing host for this virtual machine
	 * @param vmHandle the VixHandle representing this virtual machine
	 */ // package-visible constructor 
	VixVM(VixHost host, VixHandle vmHandle) {
		this.host = host;
		this.vmHandle = vmHandle;
	}
	
	/** Returns the host containing this virtual machine
	 * 
	 * @return the host containing this virtual machine
	 */
	public VixHost getHost() {
		return host;
	}
	
	/** This function mounts a new shared folder in the virtual machine. 
	 * 
	 * <p><ul>
		<li> This function creates a local mount point in the guest file system and 
		mounts a shared folder exported by the host. 
		<li> Shared folders will only be accessible inside the guest operating system if
		shared folders are enabled for the virtual machine.  See the documentation 
		for 
		VixVM_EnableSharedFolders().
		<li> The folder options include:
		<ul>
		<li> VIX_SHAREDFOLDER_WRITE_ACCESS - Allow write access.
		</ul>
		<li> Only absolute paths should be used for files in the guest; the resolution of 
		relative paths is not specified.
		<li> The hostPathName argument must specify a path to a directory that exists on 
		the host, or an error will result.
		<li> If a shared folder with the same name exists before calling this function,
		the 
		job handle for this 
		function will return VIX_E_ALREADY_EXISTS.  
		<li> It is not necessary to call {@link #loginInGuest(String, String)} before calling this function.
		<li> Shared folders are not supported for the following guest operating systems:  
		Windows ME, Windows 98, Windows 95, Windows 3.x, and DOS.
		</ul>
	 * 
	 * @param shareName Specifies the guest path name of the new shared folder
	 * @param hostPathName Specifies the host path of the shared folder
	 * @param flags The folder options
	 */
	public void addSharedFolder(String shareName, String hostPathName, int flags) 
		throws VixException 
	{
		VixHandle jobHandle = VixWrapper.VixVM_AddSharedFolder(vmHandle, 
		  shareName, hostPathName, flags, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	/** Copies a file or directory from the guest operating system to the host operating system.
	 * 
		<ul>
		<li> This function copies a file from the guest operating system to the host
		operating system. The virtual machine must be running while the file is
		copied.
		<li> The format of the file name depends on the guest or host operating system.
		For example, a path name for a Microsoft Windows guest or host requires
		backslash as a directory separator, whereas a Linux guest or host requires
		a forward slash. If the path name includes backslash characters, you need
		to precede each one with an escape character.
		<li> Only absolute paths should be used for files in the guest; the resolution of 
		relative paths is not specified.
		<li> You must call 
		{@link #loginInGuest(String, String)}
		before calling this procedure.
		<li> The copy operation requires VMware Tools to be installed and running
		in the guest operating system.
		<li> If any file fails to be copied, the operation returns an error. In this
		case, Vix aborts the operation and does not attempt to copy the remaining
		files.
		</ul>
	 * 
	 * @param guestPathName The path name of a file on a file system available to the guest
	 * @param hostPathName The path name of a file on a file system available to the host
	 */
	public void copyFileFromGuestToHost(String guestPathName, String hostPathName) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_CopyFileFromGuestToHost(vmHandle, 
		  guestPathName, hostPathName, 0, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** Copies a file or directory from the host operating system to the guest operating system.
	 *
	 * <p>
		<ul>
		<li> This function copies a file from the host operating system to the guest
		operating system. The virtual machine must be running while the file is
		copied.
		<li> The format of the file name depends on the guest or host operating system.
		For example, a path name for a Microsoft Windows guest or host requires
		backslash as a directory separator, whereas a Linux guest or host requires
		a forward slash. If the path name includes backslash characters, you need
		to precede each one with an escape character.
		<li> Only absolute paths should be used for files in the guest; the resolution of 
		relative paths is not specified.
		<li> You must call 
		{@link #loginInGuest(String, String)}
		before calling this procedure.
		<li> The copy operation requires VMware Tools to be installed and running
		in the guest operating system.
		<li> If any file fails to be copied, the operation returns an error. In this
		case, Vix aborts the operation and does not attempt to copy the remaining
		files.
		</ul>
	 *  
	 * @param hostPathName The path name of a file on a file system available to the host. 
	 * @param guestPathName The path name of a file on a file system available to the guest. 
	 */ 
	public void copyFileFromHostToGuest(String hostPathName, String guestPathName) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_CopyFileFromHostToGuest(vmHandle, 
		  hostPathName, guestPathName, 0, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** This function creates a directory in the guest operating system.
	 * 
	 * <ul><li>You must call {@link #loginInGuest(String, String)} before calling this function.
     * <li>If the parent directories for the specified path do not exist, this function 
     * will create them.
     * <li>If the directory already exists, the error associated with the job handle will 
     * be set to VIX_E_FILE_ALREADY_EXISTS.
     * <li>Only absolute paths should be used for files in the guest; the resolution of 
     * relative paths is not specified. 
	 * </ul>
	 * 
	 * @param pathName The path to the directory to be created.
	 */
	public void createDirectoryInGuest(String pathName) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_CreateDirectoryInGuest(vmHandle, 
		  pathName, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** This function saves a copy of the virtual machine state as a snapshot object. 
	 * The newly created snapshot object is returned.
	 *
		<ul>
		<li> This function creates a child snapshot of the current snapshot.
		<p>
		<li> VMware Server supports only a single snapshot for each virtual machine.
		The following considerations apply to VMware Server:
		<ul>
		<li> If you call this function a second time for the same virtual machine
		without first deleting the snapshot, the second call will overwrite
		the previous snapshot.
		<li> A virtual machine imported to VMware Server from another VMware product
		might have more than one snapshot at the time it is imported.
		In that case, you can use this function to add a new snapshot to the
		series.
		<p>
		</ul>
		<li> The 'name' and 'description' parameters can be set but not retrieved
		using the VIX API.
		</ul>
	 *  
	 * @param name A user-defined name for the snapshot; need not be unique
	 * @param description A user-defined description for the snapshot
	 * @param options Flags to specify how the shapshot should be created. Any combination of the following or 0:
	 *   <attributes> 
         VIX_SNAPSHOT_INCLUDE_MEMORY - Captures the full state of a running virtual machine, including the memory.
         </attributes>
     *
	 * @return The newly created snapshot
	 */
	public VixSnapshot createSnapshot(String name, String description, int options) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_CreateSnapshot(vmHandle, 
		  name, description, options, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		List jobWaitProperties = new ArrayList();
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_ITEM_NAME)); 
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, jobWaitProperties);
			return new VixSnapshot((VixHandle) result.get(0));
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** This function creates a temporary file in the guest operating system.
	 * 
	 * <ul><li>You must call {@link #loginInGuest(String, String)} before calling this function. 
	 * </ul> 
	 * 
	 * @return the name of the temporary file
	 */
	public String createTempFileInGuest() throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_CreateTempFileInGuest(vmHandle, 
		  0, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		List jobWaitProperties = new ArrayList();
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_ITEM_NAME)); 
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, jobWaitProperties);
			return (String) result.get(0);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** This function permanently deletes a virtual machine from your host system.
	 *
	 * <ul><li>This function permanently deletes a virtual machine from your host system. 
	   You can accomplish the same effect by deleting all virtual machine files using the host file system. 
	   This function simplifies the task by deleting all VMware files in a single function call. 
	   This function does not delete other user files in the virtual machine folder. 
	 * This function does not delete other user files in the virtual machine folder. 
    * <li>This function is successful only if the virtual machine is powered off or suspended. 
    * <li>Calling delete() on a virtual machine handle does not release the virtual machine handle. 
    * You still need to call close() on this object.
    * </ul> 
	 */
	public void delete() throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_Delete(vmHandle, 
		  0, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	/** This function deletes a directory in the guest operating system. 
	 * Any files or subdirectories in the specified directory will also be deleted.
	 *
	 * <ul><li>You must call {@link #loginInGuest(String, String)} before calling this function. 
	 * <li>Only absolute paths should be used for files in the guest; the resolution of relative paths is not specified.
	 * </ul> 
	 * 
	 * @param pathName The path to the directory to be deleted. 
	 */
	public void deleteDirectoryInGuest(String pathName) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_DeleteDirectoryInGuest(vmHandle, 
		  pathName, 0, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** This function deletes a file in the guest operating system.
	 *  
	 * <ul><li>You must call {@link #loginInGuest(String, String)} before calling this function. 
     * <li>Only absolute paths should be used for files in the guest; the resolution of relative paths is not specified.
     * </ul> 
	 *  
	 * @param guestPathName The path to the file to be deleted.
	 */
	public void deleteFileInGuest(String guestPathName) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_DeleteFileInGuest(vmHandle, 
		  guestPathName, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** This function tests the existence of a directory in the guest operating system.
	 *
	 * <ul><li>You must call {@link #loginInGuest(String, String)} before calling this function. 
    * <li> Only absolute paths should be used for files in the guest; the resolution of relative paths is not specified. 
    * </ul>
	 * 
	 * @param pathName The path to the directory in the guest to be checked. 
	 *
	 * @return true if the directory exists in the guest operating system, false otherwise
	 */ 
	public boolean directoryExistsInGuest(String pathName) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_DirectoryExistsInGuest(vmHandle, 
		  pathName, null, null);
		List jobWaitProperties = new ArrayList();
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_GUEST_OBJECT_EXISTS)); 
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, jobWaitProperties);
			return ((Boolean) result.get(0)).booleanValue();
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	/** This function enables or disables all shared folders as a feature for a virtual machine. 
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param enabled TRUE if enabling shared folders is desired. FALSE otherwise
	 * @param option Must be 0
	 * @param callbackProc A callback function that will be invoked when the operation is complete
	 * @param clientData A parameter that will be passed to the callbackProc function
	 * 
	 * @return A job handle that describes the state of this asynchronous operation
	 */
	public void enableSharedFolders(boolean enabled) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_EnableSharedFolders(vmHandle, 
		  enabled, 0, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** This function tests the existence of a file in the guest operating system.
	 * 
	 * <ul><li>You must call {@link #loginInGuest(String, String)} before calling this function. 
     * <li>Only absolute paths should be used for files in the guest; the resolution of relative paths is not specified. 
     * <li>If guestPathName exists as a file system object, but is not a normal file (e.g. it is a directory, device, UNIX domain socket, etc), then VIX_OK is returned, and VIX_PROPERTY_JOB_RESULT_GUEST_OBJECT_EXISTS is set to FALSE. 
     * </ul>
	 * 
	 * @param guestPathName The path to the file to be tested.
	 * 
	 * @return true if the file exists in the guest operating system, false otherwise
	 */ 
	public boolean fileExistsInGuest(String guestPathName) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_FileExistsInGuest(vmHandle,  
		  guestPathName, null, null);
		List jobWaitProperties = new ArrayList();
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_GUEST_OBJECT_EXISTS)); 
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, jobWaitProperties);
			return ((Boolean) result.get(0)).booleanValue();
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	/** This function returns the current active snapshot.
	 * 
	    <ul>
		<li> The snapshot object returned by this function is reference counted.
		The calling application is responsible for calling the close() method 
		on this object.
		</ul>	 
	 * 
	 * @return An object representing the current snapshot. 
	 * 
	 * @throws VixException
	 */ 
	public VixSnapshot getCurrentSnapshot() throws VixException
	{
		VixHandle snapshotHandle = VixWrapper.VixVM_GetCurrentSnapshot(vmHandle);
		return new VixSnapshot(snapshotHandle);
	}

	/** This function returns the snapshot matching the given name in this virtual 
	 * machine. 
	 * 
	 * @param snapshotName Idenitfies a snapshot name.
	 *  
	 * @return a reference to the named snapshot
	 * 
	 * @throws VixException
	 */	
	public VixSnapshot getNamedSnapshot(String name) throws VixException
	{
		VixHandle snapshotHandle = VixWrapper.VixVM_GetNamedSnapshot(vmHandle, name);
		return new VixSnapshot(snapshotHandle);
	}

	/** This function returns the number of top-level (root) snapshots belonging 
	 * to a virtual machine.
	 * 
		<ul>
		<li> This function returns the number of top-level (root) snapshots 
		belonging to a virtual machine. A top-level snapshot is one that is 
		not based on any previous snapshot. If the virtual machine has more than one 
		snapshot, the snapshots can be a sequence in which each snapshot is 
		based on the previous one, leaving only a single top-level snapshot.
		However, if applications create branched trees of snapshots, a single 
		virtual machine can have several top-level snapshots.
		<li> VMware Server can manage only a single snapshot 
		for each virtual machine. All other snapshots in a sequence are 
		ignored. The value of the result parameter is always 0 or 1.
		</ul>	 
	 * 
	 * @return The number of root snapshots on this virtual machine. 
	 * 
	 * @throws VixException
	 */	
	public int getNumRootSnapshots() throws VixException
	{
		int numRootSnapshots = VixWrapper.VixVM_GetNumRootSnapshots(vmHandle);
		return numRootSnapshots;
	}
	
	/** This function returns the number of shared folders mounted in the virtual machine. 
	 * 
	 * <ul><li>This function returns the number of shared folders mounted in this 
	 * virtual machine. 
	 * <li>It is not necessary to call {@link #loginInGuest(String, String)} before calling this function. 
	 * <li>Shared folders are not supported for the following guest operating systems: 
	 * Windows ME, Windows 98, Windows 95, Windows 3.x, and DOS. 
	 * </ul>
	 * 
	 * @return the number of shared folders mounted in the virtual machine
	 */
	public int getNumSharedFolders() throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_GetNumSharedFolders(vmHandle,  
		  null, null);
		List jobWaitProperties = new ArrayList();
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_SHARED_FOLDER_COUNT)); 
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, jobWaitProperties);
			return ((Integer) result.get(0)).intValue();
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	/** This function returns the handle of the specified snapshot belonging to the 
	 * virtual machine referenced by vmHandle.
	 * 
		<ul>
		<li> This function returns the specified snapshot belonging this 
		virtual machine.
		<li> Snapshots are indexed from 0 to n-1, where n is the number of root
		snapshots. Use the function VixVM_GetNumRootSnapshots() to get the
		value of n.
		<li> VMware Server can manage only a single snapshot
		for each virtual machine. The value of index can only be 0.
		<li> The snapshot returned by this function is reference counted.
		The calling application is responsible for calling close() on this object.
		</ul>
	 * 
	 * @param index Identifies a root snapshot. See below for range of values.
	 *  
	 * @return a reference to a snapshot 
	 * 
	 * @throws VixException
	 */
	public VixSnapshot getRootSnapshot(int index) throws VixException
	{
		VixHandle snapshotHandle = VixWrapper.VixVM_GetRootSnapshot(vmHandle, index);
		return new VixSnapshot(snapshotHandle);
	}

	
	/** This function returns the state of a shared folder mounted in the virtual machine. 
	 * 
	 * <ul><li>Shared folders are indexed from 0 to <i>n</i>-1, where <i>n</i> is the number of shared folders. Use the function 
	 * {@link #getNumSharedFolders()} to get the value of <i>n</i>. 
	 * <li>It is not necessary to call {@link #loginInGuest(String, String)} before calling this function. 
	 * <li>Shared folders are not supported for the following guest operating systems: Windows ME, Windows 98, Windows 95, Windows 3.x, and DOS.
	 * </ul> 
	 * 
	 * @param index Identifies the shared folder.
	 * 
	 * @return A VixSharedFolderState describing the state of the shared folder
	 */
	public VixSharedFolderState getSharedFolderState(int index) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_GetSharedFolderState(vmHandle,
		  index, null, null);
		List jobWaitProperties = new ArrayList();
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_ITEM_NAME)); 
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_SHARED_FOLDER_HOST));
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_SHARED_FOLDER_FLAGS));
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, jobWaitProperties);
			VixSharedFolderState folderState = new VixSharedFolderState(
			  (String) result.get(0),
			  (String) result.get(1),
			  ((Integer) result.get(2)).intValue()
			);
			return folderState;
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** Installs VMware Tools on the guest operating system.
	 * 
	 * <ul><li>Installs VMware Tools on the guest operating system. 
	 * If VMware Tools is already installed, this function upgrades VMware Tools 
	 * to the version that matches the Vix library. 
	 * <li>The virtual machine must be powered on to do this operation.
	 * </ul> 
	 */
	public void installTools() throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_InstallTools(vmHandle,
		  0, null, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** This function terminates a process in the guest operating system. 
	 * 
	 * <ul><li>You must call {@link #loginInGuest(String, String)} before calling this function. 
	 * </ul>
	 * 
	 * @param pid The ID of the process to be killed. 
	 */
	public void killProcessInGuest(long pid) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_KillProcessInGuest(vmHandle,
		  pid, 0, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	/** This function lists a directory in the guest operating system. 
	 * 
	 * <ul><li>You must call {@link #loginInGuest(String, String)} before calling this function. 
	 * <li>Only absolute paths should be used for files in the guest; the resolution of relative paths is not specified.
	 * </ul> 
	 *  
	 * @param pathName The path name of a directory to be listed. 
	 * 
	 * @return A List of {@link net.sf.jvix.data.VixFile} objects describing the files in this path
	 */
	public List listDirectoryInGuest(String pathName) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_ListDirectoryInGuest(vmHandle,
		  pathName, 0, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
			int num = VixWrapper.VixJob_GetNumProperties(jobHandle, VixWrapper.VIX_PROPERTY_JOB_RESULT_ITEM_NAME);
			List nthPropertyList = new ArrayList();
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_ITEM_NAME));
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_FILE_FLAGS));
			List directories = new ArrayList();
			for (int i=0; i<num; i++) {
				List fileResult = VixWrapper.VixJob_GetNthProperties(jobHandle, i, nthPropertyList);
				directories.add(new VixFile(
				  (String) fileResult.get(0), 
				  ((Integer) fileResult.get(1)).intValue()
				));
			}
			return directories;
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** This function lists the running processes in the guest operating system. 
	 *
		<ul>
		<li> You must call {@link #loginInGuest(String, String)} before calling this function.
		</ul>
     *
	 * @return a List of {@link net.sf.jvix.data.VixProcess} objects listing the running processes. 
	 */
	public List listProcessesInGuest() throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_ListProcessesInGuest(vmHandle,
		  0, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
			int num = VixWrapper.VixJob_GetNumProperties(jobHandle, VixWrapper.VIX_PROPERTY_JOB_RESULT_ITEM_NAME);
			List nthPropertyList = new ArrayList();
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_ITEM_NAME));
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_PROCESS_ID));
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_PROCESS_OWNER));
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_PROCESS_COMMAND));
			List processes = new ArrayList();
			for (int i=0; i<num; i++) {
				List processResult = VixWrapper.VixJob_GetNthProperties(jobHandle, i, nthPropertyList);
				processes.add(new VixProcess(
				  (String) processResult.get(0), 
				  ((Long) processResult.get(1)).longValue(),
				  (String) processResult.get(2),
				  (String) processResult.get(3)
				));
			}
			return processes;
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	/** This function establishes a guest operating system authentication context 
	 * that can be used with guest functions for the given virtual machine handle.
	 *
	 * <p>
		<ul>
		<li> This function validates the account name and password in the guest OS. You must 
		call this function before calling functions to perform operations on the guest
		operating system, such as {@link #runProgramInGuest(String, String, int)}.
		If you do not call any guest functions, you do not need to call 
		<tt>loginInGuest</tt>.
		<li> The following functions require that you call <tt>loginInGuest</tt>.
		<ul>
		<li> {@link #runProgramInGuest(String, String, int)}
		<li> {@link #listProcessesInGuest()}
		<li> {@link #killProcessInGuest(long)}
		<li> {@link #runScriptInGuest(String, String, int)}
		<li> {@link #openUrlInGuest(String)}
		<li> {@link #copyFileFromHostToGuest(String, String)}
		<li> {@link #copyFileFromGuestToHost(String, String)}
		<li> {@link #deleteFileInGuest(String)}
		<li> {@link #fileExistsInGuest(String)}
		<li> {@link #renameFileInGuest(String, String)}
		<li> {@link #createTempFileInGuest()}
		<li> {@link #listDirectoryInGuest(String)}
		<li> {@link #createDirectoryInGuest(String)}
		<li> {@link #deleteDirectoryInGuest(String)}
		<li> {@link #directoryExistsInGuest(String)}
		<p>
		</ul>
		<li> All guest operations for a particular VM handle will be done using the identity
		you provide to <tt>loginInGuest()</tt>. As a result, guest operations will be 
		restricted by file system priveleges in the guest OS that apply to the user 
		specified in <tt>loginInGuest()</tt>. For example, 
		{@link #deleteDirectoryInGuest(String)} may fail if the user named in 
		<tt>loginInGuest()</tt> does not have access permissions
		to the directory in the guest OS.
		<li><tt>loginInGuest()</tt> changes the behavior of Vix functions to use a user account.
		It does not log in a user into a console session on the guest operating system. As 
		a result, you may not see the user logged in from within the guest operating system.
		Moreover, operations such as rebooting the guest do not clear the guest 
		credentials.
		<li> You must call <tt>loginInGuest()</tt> for each VM that uses guest operations.
		<li> The virtual machine must be powered on before calling this function.
		<li> VMware Tools must be installed and running on the guest operating system
		before calling this function. You can call {@ilnk #waitForToolsInGuest(int)}
		to wait for the tools to run.
		<li> You can call {@link #logoutFromGuest()} to remove the user information from the 
		VixVM object.
		<li> You can always explicitly login in the guest by providing a username and 
		password that is valid on the guest. Then you will execute all guest 
		operations as that user. This is the default mechanism and is encouraged.
		<li> Optionally, you may call <tt>loginInGuest()</tt> with the constant defined by
		{@link net.sf.jvix.VixWrapper#VIX_CONSOLE_USER_NAME} as the userName, and NULL 
		for the password. If there is a user currently logged into the guest at the 
		console (through the MKS) then all guest operations will be run as the console user. 
		This is enabled by default and must be explicitly disabled by setting 
		the "guest.commands.anonGuestCommandsRunAsConsoleUser" config value to
		false. If no user is logged in at the guest console, the call to 
		<tt>loginInGuest()</tt> will block and wait for a user to log in to the console.
		</ul>
	 *  
	 * @param username The name of a user account on the guest operating system. 
	 * @param password The password of the account identified by userName. 
	 */
	public void loginInGuest(String username, String password) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_LoginInGuest(vmHandle, username, password, 0, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);	
		}
	}

	/** This function removes any guest operating system authentication context created by 
	 * a previous call to {@link #loginInGuest(String, String)}.
	 * 
	 * <ul>
	 *  <li>This function has no effect and returns success if {@link #loginInGuest(String, String)} has 
	 *   not been called. 
     * </ul>
	 */
	public void logoutFromGuest() throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_LogoutFromGuest(vmHandle, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);	
		}
	}
	
	/** This function opens a browser window on the specified URL in the guest operating system. 
	 * 
		<ul>
		<li> This function opens the URL in the guest operating system.
		<li> You must call {@link #loginInGuest(String, String)} with 
		{net.sf.jvix.VixWrapper#VIX_CONSOLE_USER_NAME}
		as the userName and NULL as the password before calling this function.
		</ul>
	 * 
	 * @param url The URL to be opened
	 */
	public void openUrlInGuest(String url) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_OpenUrlInGuest(vmHandle, 
		  url, 0, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);	
		}
	}

 	/** This function powers off a virtual machine. 
 	 * 
 	 */
	public void powerOff() throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_PowerOff(vmHandle, 
		  VixWrapper.VIX_VMPOWEROP_NORMAL, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	
    /** Powers on a virtual machine
     * 
	<ul>
	<li> This function powers on a virtual machine.	It is an asynchronous operation,
	and the job will be signalled when the operation completes.
	<li> In Server 1.0, when you power on a virtual machine, the virtual machine is powered on
	independent of a console window. If a console window is open, it remains open.
	Otherwise, the virtual machine is powered on without a console window.
	<li> To display a virtual machine with a Workstation user interface,
	powerOnOptions must have the {@link net.sf.jvix.VixWrapper#VIX_VMPOWEROP_LAUNCH_GUI} flag.
	<li> This function can also be used to resume execution of a suspended virtual
	machine.
	</ul>

     * @param powerOpOptions VIX_VMPOWEROP_NORMAL or VIX_VMPOWEROP_LAUNCH_GUI
     * 
     */
	public void powerOn(int powerOpOptions) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_PowerOn(vmHandle, 
		  VixWrapper.VIX_VMPOWEROP_LAUNCH_GUI, VixHandle.VIX_INVALID_HANDLE, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** This function removes a shared folder in this virtual machine.
	 * 
	 * <ul><li>This function removes a shared folder in this virtual machine. 
	 * <li>It is not necessary to call {@link #loginInGuest(String, String)} before calling this function. 
	 * <li>Shared folders are not supported for the following guest operating systems: 
	 * Windows ME, Windows 98, Windows 95, Windows 3.x, and DOS.
	 * </ul> 
	 * 
	 * @param shareName Specifies the guest pathname of the shared folder to delete. 
	 */
	public void removeSharedFolder(String shareName) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_RemoveSharedFolder(vmHandle, 
		  shareName, 0, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	/** This function deletes all saved states for the specified snapshot
	 * 
	 * <ul><li>This function deletes all saved states for the specified snapshot. 
	 * If the snapshot was based on another snapshot, the base snapshot becomes the 
	 * new root snapshot. 
     * <li>A snapshot can be removed only while the associated virtual machine is 
     * powered off or suspended. 
     * <li>The Server 1.0 release of the VIX API can manage only a single snapshot for 
     * each virtual machine. A virtual machine imported from another VMware product 
     * can have more than one snapshot at the time it is imported. In that case, you can 
     * delete only a snapshot subsequently added using the VIX API.
     * </ul>
     * 
	 * @param snapshot The snapshot to remove. 
	 * @param options Flags to specify optional behavior. Any combination of the following or 0: 
	 *  <ul><li>{@link net.sf.jvix.VixWrapper#VIX_SNAPSHOT_REMOVE_CHILDREN} - Remove snapshots 
	 *    that are children of the given snapshot.
	 *  </ul> 
	 */
	public void removeSnapshot(VixSnapshot snapshot, int options) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_RemoveSnapshot(vmHandle, 
		  snapshot.getVixHandle(), options, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** This function renames a file or directory in the guest operating system.
	 * 
	 * <ul><li>You must call {@link #loginInGuest(String, String)} before calling this function. 
     * <li>Only absolute paths should be used for files in the guest; the resolution of 
     * relative paths is not specified.
     * </ul> 
	 * 
	 * @param oldName The path to the file to be renamed. 
	 * @param newName The path to the new file. 
	 */
	public void renameFileInGuest(String oldName, String newName) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_RenameFileInGuest(vmHandle, 
		  oldName, newName, 0, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** This function resets a virtual machine, which is the equivalent of pressing 
	 * the reset button on a physical machine
	 * 
	 * <p>If the virtual machine is not powered on when you call 
	 * this function, it returns an error. 
	 */
	public void reset() throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_Reset(vmHandle, 
		  VixWrapper.VIX_VMPOWEROP_NORMAL, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	/** Restores the virtual machine to the state when the specified snapshot was created.
	 * 
		<ul>
		<li> Restores the virtual machine to the state when the specified snapshot was
		created. This function can power on, power off, or suspend a virtual machine.
		The resulting power state reflects the power state when the snapshot was
		created.
		<li>When you revert a powered on virtual machine and want it to display in the 
		Workstation user interface,
		options must have the {@link net.sf.jvix.VixWrapper#VIX_VMPOWEROP_LAUNCH_GUI} flag, 
		unless the {@link net.sf.jvix.VixWrapper#VIX_VMPOWEROP_SUPPRESS_SNAPSHOT_POWERON} is used.
		<li>The {@link net.sf.jvix.VixWrapper#VIX_PROPERTY_VM_TOOLS_STATE} property of the 
		virtual machine handle is undefined after the snapshot is reverted. 
		{@link #waitForToolsInGuest(int)} must be called to refresh this property.
		</ul>
	 * 
	 * @param snapshot  The snapshot to revert to
	 * @param options Any applicable VixVMPowerOpOptions. If the virtual machine was 
	 *   powered on when the snapshot was created, then this will determine how the 
	 *   virtual machine is powered back on. To prevent the virtual machine from being 
	 *   powered on regardless of the power state when the snapshot was created, 
	 *   use the {@link net.sf.jvix.VixWrapper#VIX_VMPOWEROP_SUPPRESS_SNAPSHOT_POWERON} flag. 
	 *   {@link net.sf.jvix.VixWrapper#VIX_VMPOWEROP_SUPPRESS_SNAPSHOT_POWERON} is 
	 *   mutually exclusive to all other VixVMPowerOpOptions
	 */
	public void revertToSnapshot(VixSnapshot snapshot, int options) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_RevertToSnapshot(vmHandle, 
		  snapshot.getVixHandle(), options, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	

	/** This function runs a program in the guest operating system. The program 
	 * must be stored on a file system available to the guest before calling this function
	 * 
    <ul><li> This function runs a program in the guest operating system. The program must
    be stored on a file system available to the guest before calling this
    function.
    <li> The current working directory for the program in the guest is not defined.
    Absolute paths should be used for files in the guest, including guestProgramName
    and any command-line arguments.
    <li> You must call {@link #loginInGuest(String, String)} before calling this function.
    <li> If the program to run in the guest is intended to be visible to the user in
    the guest, such as an application with a graphical user interface, you must
    call {@link #loginInGuest(String, String)} with {@link net.sf.jvix.VixWrapper#VIX_CONSOLE_USER_NAME}
     as the userName and <tt>null</tt> as the password before calling this function. 
     This will ensure that the program is run within a graphical session that is 
     visible to the user.
    <li> If the options parameter is 0, this function will 
    report completion to the job handle when the program exits in the guest operating system.
    Alternatively, you can pass {@link net.sf.jvix.VixWrapper#VIX_RUNPROGRAM_RETURN_IMMEDIATELY}
     as the value of the options parameter, and this function 
    reports completion to the job handle as soon as the program starts in the guest.
    <li> For Windows guest operating systems, when runing a program with a graphical user
    interface, you can pass {@link net.sf.jvix.VixWrapper#VIX_RUNPROGRAM_ACTIVATE_WINDOW} 
    as the value of the options parameter. This option will ensure that the application's window is visible and not
    minimized on the guest's screen. This can be combined with the
    {@link net.sf.jvix.VixWrapper#VIX_RUNPROGRAM_RETURN_IMMEDIATELY} flag using the 
    bitwise inclusive OR operator (|). {@link net.sf.jvix.VixWrapper#VIX_RUNPROGRAM_ACTIVATE_WINDOW}
     has no effect on Linux guest operating systems.
    <li> When the job is signaled, a VixProcess object will be available on
    the returned job handle; only the id, elapsedTime and exitCode properties will
    be valid.
    If the option parameter is {@link net.sf.jvix.VixWrapper#VIX_RUNPROGRAM_RETURN_IMMEDIATELY}, 
    the latter two will both be 0.
    </ul>  
    
	 * @param guestProgramName The path name of an executable file on the guest operating system 
	 * @param commandLineArgs A string to be passed as command line arguments to the executable identified by guestProgramName
	 * @param options Run options for the program. See the remarks below
	 * 
	 * @return A VixProcess describing the program that was run or is running
	 */
	public VixProcess runProgramInGuest(String guestProgramName, String commandLineArgs, int options) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_RunProgramInGuest(vmHandle, 
		  guestProgramName, commandLineArgs, options, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			List nthPropertyList = new ArrayList();
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_PROCESS_ID));
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_GUEST_PROGRAM_ELAPSED_TIME));
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_GUEST_PROGRAM_EXIT_CODE));
			List result = VixWrapper.VixJob_Wait(jobHandle, nthPropertyList);
			VixProcess process = new VixProcess(guestProgramName, 
				((Long) result.get(0)).longValue(),
				((Integer) result.get(1)).intValue(),
				((Integer) result.get(2)).intValue());
			return process;
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	
   /** This function runs a script in the guest operating system. 
    *
    <ul>
    <li>This function runs the script in the guest operating system.
    <li>The current working directory for the script executed in the guest is not defined.
    Absolute paths should be used for files in the guest, including the path
    to the interpreter, and any files referenced in the script text.
    <li>You must call {@link #loginInGuest(String, String)} before calling this function.
    <li>If the options parameter is 0, this function will report completion to the job handle
    when the program exits in the guest operating system.
    Alternatively, you can pass {@link net.sf.jvix.VixWrapper#VIX_RUNPROGRAM_RETURN_IMMEDIATELY}
     as the value of the options parameter, and this function
    reports completion to the job handle as soon as the program starts in the guest.
    <li> When the job is signaled, a VixProcess object will be available on
    the returned job handle; only the id, elapsedTime and exitCode properties will
    be valid.
    If the option parameter is VIX_RUNPROGRAM_RETURN_IMMEDIATELY, the latter two will
    both be 0.
    </ul>  
    *  
    * @param interpreter The path to the script interpreter
    * @param scriptText The text of the script. 
    * @param option Run options for the program. See the notes below
    * 
	 * @return A VixProcess describing the script that was run or is running
    */
	public VixProcess runScriptInGuest(String interpreter, String scriptName, int options) throws VixException {
 		VixHandle jobHandle = VixWrapper.VixVM_RunScriptInGuest(vmHandle, 
		  interpreter, scriptName, options, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			List nthPropertyList = new ArrayList();
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_PROCESS_ID));
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_GUEST_PROGRAM_ELAPSED_TIME));
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_GUEST_PROGRAM_EXIT_CODE));
			List result = VixWrapper.VixJob_Wait(jobHandle, nthPropertyList);
			VixProcess process = new VixProcess(scriptName, 
				((Long) result.get(0)).longValue(),
				((Integer) result.get(1)).intValue(),
				((Integer) result.get(2)).intValue());
			return process;
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	  /** This function modifies the state of a shared folder mounted in the virtual machine.
	   * 
	   * <ul><li>This function modifies the state flags of an existing shared folder. 
	   * <li>If the shared folder specified by shareName does not exist before calling this function, the job handle for this function will return VIX_E_NOT_FOUND. 
	   * <li>It is not necessary to call {@link, #loginInGuest(String, String)} before calling this function. 
	   * <li>Shared folders are not supported for the following guest operating systems: 
	   * Windows ME, Windows 98, Windows 95, Windows 3.x, and DOS.
	   * </ul> 
	   *  
	   * @param shareName Specifies the name of the shared folder
	   * @param hostPathName Specifies the host path of the shared folder. 
	   * @param flags The new flag settings. 
	   */
	public void setSharedFolderState(String shareName, String hostPathName, int flags) throws VixException {
		// @TODO jobHandle is set to error code in some cases
		VixHandle jobHandle = VixWrapper.VixVM_SetSharedFolderState(vmHandle, 
		  shareName, hostPathName, flags, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
   /** This function suspends a virtual machine.
    * 
    * <p>If the virtual machine is not powered on when you 
    * call this function, the function returns an error. 
    */
	public void suspend() throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_Suspend(vmHandle, 0, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
   /** Upgrades the virtual hardware version of the virtual machine to match 
    * the version of the VIX library. This has no effect if the virtual machine 
    * is already at the same version or at a newer version than the VIX library. 
    * 
    * <ul><li>The virtual machine must be powered off to do this operation. 
    * <li>When the VM is already up-to-date, the job handle for this function 
    * will return VIX_E_VM_ALREADY_UP_TO_DATE.
    * </ul> 
    */
	public void upgradeVirtualHardware() throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_Suspend(vmHandle, 0, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	} 

   /** This function signals the job handle when VMware Tools has successfully started 
    * in the guest operating system. VMware Tools is a collection of services that run 
    * in the guest. 
    * 
    * <ul><li> This function signals the job when VMware Tools has successfully started
     in the guest operating system. VMware Tools is a collection of services
     that run in the guest.
     <li>VMware Tools must be installed and running for some Vix functions to operate
     correctly. If VMware Tools is not installed in the guest operating system,
     or if the virtual machine is not powered on, this function reports an error
     to the job object.
     <li> The {@link net.sf.jvix.VixWrapper#VIX_PROPERTY_VM_TOOLS_STATE} property of the 
     virtual machine handle is undefined until <tt>waitForToolsInGuest()</tt> reports that 
     VMware Tools is running.
     </ul>
    * 
    * @param timeoutInSeconds The timeout in seconds. 
    * If VMware Tools has not started by this time, the function completes with an error. 
    * If the value of this argument is zero or negative, then there will be no timeout. 
    */
	public void waitForToolsInGuest(int timeoutInSeconds) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_WaitForToolsInGuest(vmHandle, timeoutInSeconds, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	/** Releases the resources associated with this virtual machine.
	 * 
	 */
	public void close() {
		if (vmHandle != null) { 
			VixWrapper.Vix_ReleaseHandle(vmHandle); 
		}		
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
