package net.sf.jvix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

/** Object-oriented wrapper for the VIX Host API
 * 
 * @author knoxg
 * @version $Id$
 */
public class VixHost {

	private VixHandle hostHandle;
	
	/** Logger instance for this class */
	public static final Logger logger = Logger.getLogger(VixHost.class);


    /** Connects to a vmware host.
     * 
	<ul>
	<li> To specify the local host (where the API client runs), pass null values
	for the hostName, hostPort, userName, and password parameters. This is
	shown in the example below.
	<li> If you are already connected to the host, a subsequent call to
	VixHost_Connect()
	succeeds if you connect as the same user and use the
	same host name. Subsequent calls return the same handle value.
	<li> When you initialize the host object, you can also control some Vix
	operations with the options parameter. The following option is supported:
	</ul>
     * 
     * @param apiVersion Must be VixWrapper.VIX_API_VERSION
     * @param hostType VIX_SERVICEPROVIDER_VMWARE_SERVER or VIX_SERVICEPROVIDER_VMWARE_WORKSTATION
     * @param hostName DNS name or IP address of remote host. Use NULL to connect to local host
     * @param hostPort TCP/IP port of remote host, typically 902. Use zero for local host
     * @param userName Username to authenticate with on remote machine. Use NULL to authenticate as current user on local host
     * @param password Password to authenticate with on remote machine. Use NULL to authenticate as current user on local host
     * 
     */
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
	
    /** This function opens a virtual machine on the host that is identified by 
     * this object. 
	<ul>
	<li>The virtual machine is identified by vmxFilePathName,
	which is a path name to the configuration file (.VMX file) for that virtual
	machine.
	<li> The format of the path name depends on the host operating system.
	For example, a path name for a Windows host requires backslash as
	a directory separator, whereas a Linux host requires a forward slash.
	If the path name includes backslash characters, you need to precede each
	one with an escape character.
	<li> For VMware Server hosts, a virtual machine must be registered before you
	can open it. You can register a virtual machine by opening it with the
	VMware Server Console, through the vmware-cmd command with the register
	parameter, or with 
	VixHost_RegisterVM().
	</ul>
     * 
     * @param vmxFilePathName The path name of the virtual machine configuration file on the local host
     */
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
	

	/** Destroys the state for this VixHost.
	 * 
	 * <p>Call this function to disconnect the host. After you call this function the object 
	 * is no longer valid and you should not use it in any Vix function. Similarly, you should 
	 * not use any objects obtained from the host while it was connected.
	 * 
	 */
	public void close() throws VixException {
		if (hostHandle != null) {
			VixWrapper.VixHost_Disconnect(hostHandle);
			VixWrapper.Vix_ReleaseHandle(hostHandle);
			hostHandle = null;
		}
	}
	
	/** This function finds Vix objects and return a list of objects found. 
	 * For example, when used to find all running 
	 * virtual machines, VixHost_FindItems() returns a series of virtual machine file 
	 * path names.
	 * 
	 * @param searchType The type of items to find. Value should be a VIX_FIND_* constant.
	 * 
	 * @return A List of found items.
	 */
	public List findItems(int searchType) throws VixException {
		final List items = new ArrayList();
		VixEventProc discoverProc = new VixEventProc() {
			public void callback(VixHandle handle, int eventType, VixHandle moreEventInfo, Object clientData) {
				
				logger.debug("VixHost.findItems.discoverProc invoked (eventType=" + eventType + ")");
				if (eventType!=VixWrapper.VIX_EVENTTYPE_FIND_ITEM) {
					return;
				}
				List findItemsCallbackProperties = new ArrayList();
				findItemsCallbackProperties.add(new Integer(VixWrapper.VIX_PROPERTY_FOUND_ITEM_LOCATION));
				List properties = VixWrapper.Vix_GetProperties(moreEventInfo, findItemsCallbackProperties);
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
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST); 
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
		return items;
	}
	

	/** This function adds a virtual machine to the host's inventory.
	 * 
	 * <ul><li>This function adds a virtual machine to the local host's inventory. 
	 * The virtual machine is identified by the vmxFilePathName, which is a path 
	 * name to the configuration file (.VMX file) for that virtual machine.
     * <li> The format of the path name depends on the host operating system. 
     * <li> This function does not apply to Workstation, which has no virtual machine 
     * inventory.
     *  
	 * @param vmxFilePath The path name of the .vmx file on the host.
	 */
	public void registerVM(String vmxFilePath) throws VixException {
		VixHandle jobHandle = VixWrapper.VixHost_RegisterVM(
		  hostHandle, vmxFilePath, null, null);
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST); 
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
		return;
	}
	
	/** This function removes a virtual machine from the host's inventory.
	 * 
	 * <ul><li>This function removes a virtual machine from the local host's inventory. 
	 * The virtual machine is identified by the vmxFilePathName, which is a path name 
	 * to the configuration file (.VMX file) for that virtual machine.
     * <li>The format of the path name depends on the host operating system. 
     * <li>This function does not apply to Workstation, which has no virtual machine 
     * inventory.
     * </ul> 
	 * 
	 * @param vmxFilePath The path name of the .vmx file on the host.
	 */
	public void unregisterVM(String vmxFilePath) throws VixException {
		VixHandle jobHandle = VixWrapper.VixHost_UnregisterVM(
		  hostHandle, vmxFilePath, null, null);
		List jobWaitProperties = new ArrayList();
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_HANDLE));
		try {
			/*List result =*/ VixWrapper.VixJob_Wait(jobHandle, jobWaitProperties); 
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);
		}
		return;
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
