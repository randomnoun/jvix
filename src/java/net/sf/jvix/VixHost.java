package net.sf.jvix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** OO wraper to VIX Host API
 * @author knoxg
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class VixHost {

	private VixHandle hostHandle;

	public VixHost(int apiVersion, int hostType, String hostName, int hostPort, String userName, String password)
		throws VixException
	{
		List result;
		VixHandle jobHandle = VixWrapper.VixHost_Connect(
		  apiVersion,
		  hostType,
		  hostName,
		  hostPort,
		  "presumablyIgnoredUsername", // VMs are running on local machine
		  "presumablyIgnoredPassword",
		  VixWrapper.VIX_VMPOWEROP_NORMAL,
		  VixHandle.VIX_INVALID_HANDLE,
		  null, null);
			  
		// properties to retrieve from VixJob_Wait call
		// (maybe it would be cleaner to pass these in as an int[] )
		List jobWaitProperties = new ArrayList();
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_HANDLE));
		try {
			result = VixWrapper.VixJob_Wait(jobHandle, jobWaitProperties);	 
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);	
		}
		this.hostHandle = (VixHandle) result.get(0);
	}
	
	public VixVM open(String vmLocation) throws VixException {
		List result;
		VixHandle jobHandle = VixWrapper.VixVM_Open(
		  hostHandle,
		  vmLocation,
		  null, null);
		List jobWaitProperties = new ArrayList();
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_HANDLE));
		try {
			result = VixWrapper.VixJob_Wait(jobHandle, jobWaitProperties); 
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);	
		}
		return new VixVM(this, (VixHandle) result.get(0));
	}
	
	public void disconnect() throws VixException {
		VixWrapper.VixHost_Disconnect(hostHandle);		
	}

	public void close() {
		if (hostHandle != null) { 
			VixWrapper.Vix_ReleaseHandle(hostHandle);
			hostHandle = null; 
		}		
	}
	
	/** 
	 * 
	 * @param searchType a VixWrapper.VIX_FIND_* constant
	 * 
	 * @return
	 * @throws VixException
	 */
	public List findItems(int searchType) throws VixException {
		final List items = new ArrayList();
		VixEventProc discoverProc = new VixEventProc() {
			public void callback(VixHandle handle, int eventType, VixHandle moreEventInfo, Object clientData) {
				if (eventType!=VixWrapper.VIX_EVENTTYPE_FIND_ITEM) {
					return;
				}
				List properties = VixWrapper.Vix_GetProperties(moreEventInfo,
				  new int[] { VixWrapper.VIX_PROPERTY_FOUND_ITEM_LOCATION } );
				items.add(properties.get(0));				    
			}
		};
		VixHandle jobHandle = VixWrapper.VixHost_FindItems(
		  hostHandle,
		  searchType,
		  new VixHandle(VixWrapper.VIX_INVALID_HANDLE),
		  -1, // timeout
		  discoverProc, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST); 
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
		return items;
	}
	
	public void registerVM(String vmxFilePath) throws VixException {
		VixHandle jobHandle = VixWrapper.VixHost_RegisterVM(
		  hostHandle, vmxFilePath, null, null);
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST); 
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
		return;
	}
	
	public void unregisterVM(String vmxFilePath) throws VixException {
		VixHandle jobHandle = VixWrapper.VixHost_UnregisterVM(
		  hostHandle, vmxFilePath, null, null);
		List jobWaitProperties = new ArrayList();
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_HANDLE));
		try {
			List result = VixWrapper.VixJob_Wait(jobHandle, jobWaitProperties); 
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
		return;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}



}
