package net.sf.jvix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jvix.data.VixFile;
import net.sf.jvix.data.VixProcess;
import net.sf.jvix.data.VixSharedFolderState;

/** OO wraper to VIX Host API
 * @author knoxg
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class VixVM {

	private VixHost   host;
	private VixHandle vmHandle;

	// package-visible constructor 
	VixVM(VixHost host, VixHandle vmHandle) {
		this.host = host;
		this.vmHandle = vmHandle;
	}
	
	public void addSharedFolder(String shareName, String hostPathName, int flags) 
		throws VixException 
	{
		VixHandle jobHandle = VixWrapper.VixVM_AddSharedFolder(vmHandle, 
		  shareName, hostPathName, flags, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public void copyFileFromGuestToHost(String guestPathName, String hostPathName) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_CopyFileFromGuestToHost(vmHandle, 
		  guestPathName, hostPathName, 0, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	public void copyFileFromHostToGuest(String hostPathName, String guestPathName) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_CopyFileFromGuestToHost(vmHandle, 
		  hostPathName, guestPathName, 0, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public void createDirectoryInGuest(String pathName) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_CreateDirectoryInGuest(vmHandle, 
		  pathName, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	public void createSnapshot(String name, String description, int options) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_CreateSnapshot(vmHandle, 
		  name, description, options, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

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
	
	public void delete() throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_Delete(vmHandle, 
		  0, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public void deleteDirectoryInGuest(String pathName) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_DeleteDirectoryInGuest(vmHandle, 
		  pathName, 0, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public boolean directoryExistsInGuest(String pathName) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_DirectoryExistsInGuest(vmHandle, 
		  pathName, null, null);
		List jobWaitProperties = new ArrayList();
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_ITEM_NAME)); 
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, jobWaitProperties);
			return ((Boolean) result.get(0)).booleanValue();
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public void enableSharedFolders(boolean enabled) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_EnableSharedFolders(vmHandle, 
		  enabled, 0, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
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
	
	public VixSnapshot getCurrentSnapshot() throws VixException
	{
		VixHandle snapshotHandle = VixWrapper.VixVM_GetCurrentSnapshot(vmHandle);
		return new VixSnapshot(snapshotHandle);
	}

	public VixSnapshot getNamedSnapshot(String name) throws VixException
	{
		VixHandle snapshotHandle = VixWrapper.VixVM_GetNamedSnapshot(vmHandle, name);
		return new VixSnapshot(snapshotHandle);
	}
	
	public int getNumRootSnapshots() throws VixException
	{
		int numRootSnapshots = VixWrapper.VixVM_GetNumRootSnapshots(vmHandle);
		return numRootSnapshots;
	}
	
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
	
	public VixSnapshot getRootSnapshot(int index) throws VixException
	{
		VixHandle snapshotHandle = VixWrapper.VixVM_GetRootSnapshot(vmHandle, index);
		return new VixSnapshot(snapshotHandle);
	}

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
	
	public void installTools() throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_InstallTools(vmHandle,
		  0, null, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public void killProcessInGuest(long pid) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_KillProcessInGuest(vmHandle,
		  pid, 0, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public List listDirectoryInGuest(String pathName) throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_ListDirectoryInGuest(vmHandle,
		  pathName, 0, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
			int num = VixWrapper.Vix_GetNumProperties(jobHandle, VixWrapper.VIX_PROPERTY_JOB_RESULT_ITEM_NAME);
			List nthPropertyList = new ArrayList();
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_ITEM_NAME));
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_FILE_FLAGS));
			List directories = new ArrayList();
			for (int i=0; i<num; i++) {
				List fileResult = VixWrapper.Vix_GetNthProperties(jobHandle, nthPropertyList);
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
	
	public List listProcessesInGuest() throws VixException
	{
		VixHandle jobHandle = VixWrapper.VixVM_ListProcessesInGuest(vmHandle,
		  0, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
			int num = VixWrapper.Vix_GetNumProperties(jobHandle, VixWrapper.VIX_PROPERTY_JOB_RESULT_ITEM_NAME);
			List nthPropertyList = new ArrayList();
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_ITEM_NAME));
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_PROCESS_ID));
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_PROCESS_OWNER));
			nthPropertyList.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_PROCESS_COMMAND));
			List processes = new ArrayList();
			for (int i=0; i<num; i++) {
				List processResult = VixWrapper.Vix_GetNthProperties(jobHandle, nthPropertyList);
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
	
	public void loginInGuest(String username, String password) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_LoginInGuest(vmHandle, username, password, 0, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);	
		}
	}

	public void logoutFromGuest(String username, String password) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_LogoutFromGuest(vmHandle, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);	
		}
	}
	
	public void openUrlInGuest(String url) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_OpenUrlInGuest(vmHandle, 
		  url, 0, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);	
		}
	}

	public void powerOff() throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_PowerOff(vmHandle, 
		  VixWrapper.VIX_VMPOWEROP_NORMAL, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	
	public void powerOn(int powerOpOptions) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_PowerOn(vmHandle, 
		  VixWrapper.VIX_VMPOWEROP_LAUNCH_GUI, VixHandle.VIX_INVALID_HANDLE, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	public void removeSharedFolder(String shareName) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_RemoveSharedFolder(vmHandle, 
		  shareName, 0, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public void removeSnapshot(VixSnapshot snapshot, int options) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_RemoveSnapshot(vmHandle, 
		  snapshot.getVixHandle(), options, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}

	public void renameFileInGuest(String oldName, String newName) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_RenameFileInGuest(vmHandle, 
		  oldName, newName, 0, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public void reset() throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_Reset(vmHandle, 
		  VixWrapper.VIX_VMPOWEROP_NORMAL, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public void revertToSnapshot(VixSnapshot snapshot, int options) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_RevertToSnapshot(vmHandle, 
		  snapshot.getVixHandle(), options, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public void runProgramInGuest(String guestProgramName, String commandLineArgs, int options) throws VixException {
		// @TODO return program details
		VixHandle jobHandle = VixWrapper.VixVM_RunProgramInGuest(vmHandle, 
		  guestProgramName, commandLineArgs, options, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public void runScriptInGuest(String interpreter, String scriptName, int options) throws VixException {
		// @TODO return program details
		VixHandle jobHandle = VixWrapper.VixVM_RunScriptInGuest(vmHandle, 
		  interpreter, scriptName, options, new VixHandle(VixWrapper.VIX_INVALID_HANDLE), null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public void setSharedFolderState(String shareName, String hostPathName, int flags) throws VixException {
		// @TODO jobHandle is set to error code in some cases
		VixHandle jobHandle = VixWrapper.VixVM_SetSharedFolderState(vmHandle, 
		  shareName, hostPathName, flags, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public void VixVM_Suspend() throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_Suspend(vmHandle, 0, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	}
	
	public void upgradeVirtualHardware() throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_Suspend(vmHandle, 0, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);		  	
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
	} 

	public void waitForToolsInGuest(int timeoutInSeconds) throws VixException {
		VixHandle jobHandle = VixWrapper.VixVM_WaitForToolsInGuest(vmHandle, timeoutInSeconds, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
				 
	}

	public void close() {
		if (vmHandle != null) { 
			VixWrapper.Vix_ReleaseHandle(vmHandle); 
		}		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}



}
