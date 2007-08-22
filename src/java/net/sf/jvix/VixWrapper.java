package net.sf.jvix;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

/** Wrapper for native calls into the VIX API. 
 * 
 * <p>Do not call these functions directly !
 * 
 * <p>The jvix.dll needs to be contained in a folder set by the 
 * java.library.path system property.
 * 
 * @TODO this wrapper is still missing some functions ... need to implement the rest
 * at some point
 * 
 * @author knoxg
 * @version $Id$
 */
public class VixWrapper {
	
	// constants from vix.h
	
	public final static int VIX_API_VERSION = 2;
	
	public final static int VIX_INVALID_HANDLE = 0;
	
	public final static int VIX_HANDLETYPE_NONE                 = 0;
	public final static int VIX_HANDLETYPE_HOST                 = 2;
	public final static int VIX_HANDLETYPE_VM                   = 3;
	public final static int VIX_HANDLETYPE_NETWORK              = 5;
	public final static int VIX_HANDLETYPE_JOB                  = 6;
	public final static int VIX_HANDLETYPE_SNAPSHOT             = 7;
	public final static int VIX_HANDLETYPE_METADATA_CONTAINER   = 11;
	
	
	public final static int VIX_SERVICEPROVIDER_DEFAULT = 1;
	public final static int VIX_SERVICEPROVIDER_VMWARE_SERVER = 2;
	public final static int VIX_SERVICEPROVIDER_VMWARE_WORKSTATION = 3; 
	
	public final static int VIX_HOSTOPTION_USE_EVENT_PUMP = 0x0008;

	public final static int VIX_PROPERTYTYPE_ANY             = 0;
	public final static int VIX_PROPERTYTYPE_INTEGER         = 1;
	public final static int VIX_PROPERTYTYPE_STRING          = 2;
	public final static int VIX_PROPERTYTYPE_BOOL            = 3;
	public final static int VIX_PROPERTYTYPE_HANDLE          = 4;
	public final static int VIX_PROPERTYTYPE_INT64           = 5;
	public final static int VIX_PROPERTYTYPE_BLOB            = 6;
	
	public final static int VIX_PROPERTY_NONE                                  = 0;

	/* Properties used by several handle types. */
	public final static int VIX_PROPERTY_META_DATA_CONTAINER                   = 2;

	/* VIX_HANDLETYPE_HOST properties */
	public final static int VIX_PROPERTY_HOST_HOSTTYPE                         = 50;
	public final static int VIX_PROPERTY_HOST_API_VERSION                      = 51;

	/* VIX_HANDLETYPE_VM properties */
	public final static int VIX_PROPERTY_VM_NUM_VCPUS                          = 101;
	public final static int VIX_PROPERTY_VM_VMX_PATHNAME                       = 103; 
	public final static int VIX_PROPERTY_VM_VMTEAM_PATHNAME                    = 105; 
	public final static int VIX_PROPERTY_VM_MEMORY_SIZE                        = 106;
	public final static int VIX_PROPERTY_VM_READ_ONLY                          = 107;
	public final static int VIX_PROPERTY_VM_IN_VMTEAM                          = 128;
	public final static int VIX_PROPERTY_VM_POWER_STATE                        = 129;
	public final static int VIX_PROPERTY_VM_TOOLS_STATE                        = 152;
	public final static int VIX_PROPERTY_VM_IS_RUNNING                         = 196;
	public final static int VIX_PROPERTY_VM_SUPPORTED_FEATURES                 = 197;

	/* Result properties; these are returned by various procedures */
	public final static int VIX_PROPERTY_JOB_RESULT_ERROR_CODE                 = 3000;
	public final static int VIX_PROPERTY_JOB_RESULT_VM_IN_GROUP                = 3001;
	public final static int VIX_PROPERTY_JOB_RESULT_USER_MESSAGE               = 3002;
	public final static int VIX_PROPERTY_JOB_RESULT_EXIT_CODE                  = 3004;
	public final static int VIX_PROPERTY_JOB_RESULT_COMMAND_OUTPUT             = 3005;
	public final static int VIX_PROPERTY_JOB_RESULT_HANDLE                     = 3010;
	public final static int VIX_PROPERTY_JOB_RESULT_GUEST_OBJECT_EXISTS        = 3011;
	public final static int VIX_PROPERTY_JOB_RESULT_GUEST_PROGRAM_ELAPSED_TIME = 3017;
	public final static int VIX_PROPERTY_JOB_RESULT_GUEST_PROGRAM_EXIT_CODE    = 3018;
	public final static int VIX_PROPERTY_JOB_RESULT_ITEM_NAME                  = 3035;
	public final static int VIX_PROPERTY_JOB_RESULT_FOUND_ITEM_DESCRIPTION     = 3036;
	public final static int VIX_PROPERTY_JOB_RESULT_SHARED_FOLDER_COUNT        = 3046;
	public final static int VIX_PROPERTY_JOB_RESULT_SHARED_FOLDER_HOST         = 3048;
	public final static int VIX_PROPERTY_JOB_RESULT_SHARED_FOLDER_FLAGS        = 3049;
	public final static int VIX_PROPERTY_JOB_RESULT_PROCESS_ID                 = 3051;
	public final static int VIX_PROPERTY_JOB_RESULT_PROCESS_OWNER              = 3052;
	public final static int VIX_PROPERTY_JOB_RESULT_PROCESS_COMMAND            = 3053;
	public final static int VIX_PROPERTY_JOB_RESULT_FILE_FLAGS                 = 3054;
	public final static int VIX_PROPERTY_JOB_RESULT_PROCESS_START_TIME         = 3055;
	public final static int VIX_PROPERTY_JOB_RESULT_VM_VARIABLE_STRING         = 3056;
	public final static int VIX_PROPERTY_JOB_RESULT_PROCESS_BEING_DEBUGGED     = 3057;

	/* Event properties; these are sent in the moreEventInfo for some events. */
	public final static int VIX_PROPERTY_FOUND_ITEM_LOCATION                   = 4010;

	/* VIX_HANDLETYPE_SNAPSHOT properties */
	public final static int VIX_PROPERTY_SNAPSHOT_DISPLAYNAME                  = 4200;   
	public final static int VIX_PROPERTY_SNAPSHOT_DESCRIPTION                  = 4201;
	public final static int VIX_PROPERTY_SNAPSHOT_POWERSTATE                   = 4205;
	
	public final static int VIX_EVENTTYPE_JOB_COMPLETED          = 2;
	public final static int VIX_EVENTTYPE_JOB_PROGRESS           = 3;
	public final static int VIX_EVENTTYPE_FIND_ITEM              = 8;
	public final static int VIX_EVENTTYPE_CALLBACK_SIGNALLED     = 2;  // Deprecated - Use VIX_EVENTTYPE_JOB_COMPLETED instead.
	
	public final static int VIX_FILE_ATTRIBUTES_DIRECTORY       = 0x0001;
	public final static int VIX_FILE_ATTRIBUTES_SYMLINK         = 0x0002;
	
	public final static int VIX_PUMPEVENTOPTION_NONE = 0;
	
	public final static int VIX_VMPOWEROP_NORMAL                      = 0;
	public final static int VIX_VMPOWEROP_SUPPRESS_SNAPSHOT_POWERON   = 0x0080;
	public final static int VIX_VMPOWEROP_LAUNCH_GUI                  = 0x0200;
	
	/** VixVMDeleteOptions constant for use in {@link #VixVM_Delete(VixHandle, int, VixEventProc, Object)} */ 
	public final static int VIX_VMDELETE_DISK_FILES     = 0x0002;     

	public final static int VIX_POWERSTATE_POWERING_OFF    = 0x0001;
	public final static int VIX_POWERSTATE_POWERED_OFF     = 0x0002;
	public final static int VIX_POWERSTATE_POWERING_ON     = 0x0004;
	public final static int VIX_POWERSTATE_POWERED_ON      = 0x0008;
	public final static int VIX_POWERSTATE_SUSPENDING      = 0x0010;
	public final static int VIX_POWERSTATE_SUSPENDED       = 0x0020;
	public final static int VIX_POWERSTATE_TOOLS_RUNNING   = 0x0040;
	public final static int VIX_POWERSTATE_RESETTING       = 0x0080;
	public final static int VIX_POWERSTATE_BLOCKED_ON_MSG  = 0x0100;
	
	public final static int VIX_TOOLSSTATE_UNKNOWN           = 0x0001;
	public final static int VIX_TOOLSSTATE_RUNNING           = 0x0002;
	public final static int VIX_TOOLSSTATE_NOT_INSTALLED     = 0x0004;
	
	public final static int VIX_VM_SUPPORT_SHARED_FOLDERS       = 0x0001;
	public final static int VIX_VM_SUPPORT_MULTIPLE_SNAPSHOTS   = 0x0002;
	public final static int VIX_VM_SUPPORT_TOOLS_INSTALL        = 0x0004;
	public final static int VIX_VM_SUPPORT_HARDWARE_UPGRADE     = 0x0008;	

	public final static String VIX_ANONYMOUS_USER_NAME        = "__VMware_Vix_Guest_User_Anonymous__";
	public final static String VIX_ADMINISTRATOR_USER_NAME    = "__VMware_Vix_Guest_User_Admin__";
	public final static String VIX_CONSOLE_USER_NAME          = "__VMware_Vix_Guest_Console_User__";

	/** VixFindItemType constant for use in {@link #VixHost_FindItems(VixHandle, VixFindItemType, VixHandle, int, VixEventProc, Object)} */
	public final static int VIX_FIND_RUNNING_VMS         = 1;
	
	/** VixFindItemType constant for use in {@link #VixHost_FindItems(VixHandle, VixFindItemType, VixHandle, int, VixEventProc, Object)} */
	public final static int VIX_FIND_REGISTERED_VMS      = 4;

	/** VixMsgSharedFolderOptions constant for use in {@link #VixVM_SetSharedFolderState(VixHandle, String, String, int, VixEventProc, Object)} */
	public final static int VIX_SHAREDFOLDER_WRITE_ACCESS = 4;
	
    /** Creates a host handle.
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
	<ul>
	<li> VIX_HOSTOPTION_USE_EVENT_PUMP . All asynchronous event processing
	happens when the client calls Vix_PumpEvents(). The client is
	responsible for regularly calling Vix_PumpEvents(), such as in
	an event loop.
	</ul>
     * 
     * @param apiVersion Must be VIX_API_VERSION
     * @param hostType VIX_SERVICEPROVIDER_VMWARE_SERVER or VIX_SERVICEPROVIDER_VMWARE_WORKSTATION
     * @param hostName DNS name or IP address of remote host. Use NULL to connect to local host
     * @param hostPort TCP/IP port of remote host, typically 902. Use zero for local host
     * @param userName Username to authenticate with on remote machine. Use NULL to authenticate as current user on local host
     * @param password Password to authenticate with on remote machine. Use NULL to authenticate as current user on local host
     * @param options Optionally VIX_HOSTOPTION_USE_EVENT_PUMP (See Remarks section), otherwise zero
     * @param propertyListHandle Must be VIX_INVALID_HANDLE
     * @param callbackproc Optional callback of type VixEventProc
     * @param clientData Optional user supplied opaque data to be passed to optional callback
     * 
     * @return A job handle. When the job completes, retrieve the Host handle from the job handle using the VIX_PROPERTY_JOB_RESULT_HANDLE property
     * 
     */
    public static native VixHandle VixHost_Connect(int apiVersion, int hostType, 
      String hostName, int hostPort, String userName, String password, int options, 
      VixHandle propertyListHandle, VixEventProc callbackProc, Object clientData);
      
    /** This function opens a virtual machine on the host that is identified by the hostHandle parameter and returns a context to that machine as a 
     * virtual machine handle.
	<ul>
	<li> This function opens a virtual machine on the host that is identified by the
	hostHandle parameter. The virtual machine is identified by vmxFilePathName,
	which is a path name to the configuration file (.VMX file) for that virtual
	machine.
	<li> The format of the path name depends on the host operating system.
	For example, a path name for a Windows host requires backslash as
	a directory separator, whereas a Linux host requires a forward slash.
	If the path name includes backslash characters, you need to precede each
	one with an escape character.
	<li> This function is asynchronous, and uses a job object to report when the
	operation is complete. The function returns a handle to the job object
	immediately. When the job is signaled, the virtual machine handle is
	stored as the VIX_PROPERTY_JOB_RESULT_HANDLE property of the job object.
	<li> For VMware Server hosts, a virtual machine must be registered before you
	can open it. You can register a virtual machine by opening it with the
	VMware Server Console, through the vmware-cmd command with the register
	parameter, or with 
	VixHost_RegisterVM().
	</ul>
     * 
     * @param hostHandle The handle of a host object, typically returned from VixHost_Connect()
     * @param vmxFilePathName The path name of the virtual machine configuration file on the local host
     * @param callbackProc A callback function that will be invoked when the operation is complete
     * @param clientData A parameter that will be passed to the callbackProc procedure
     * 
     * @return VixHandle. A job handle that describes the state of this asynchronous call
     */
	public static native VixHandle VixVM_Open(VixHandle hostHandle, String vmxFilePathName, 
	  VixEventProc callbackProc, Object clientData);

    /** Powers on a virtual machine
     * 
	<ul>
	<li> This function powers on a virtual machine.
	It is an asynchronous operation,
	and the job will be signalled when the operation completes.
	<li> In Server 1.0, when you power on a virtual machine, the virtual machine is powered on
	independent of a console window. If a console window is open, it remains open.
	Otherwise, the virtual machine is powered on without a console window.
	<li> To display a virtual machine with a Workstation user interface,
	powerOnOptions must have the VIX_VMPOWEROP_LAUNCH_GUI flag.
	<li> This function can also be used to resume execution of a suspended virtual
	machine.
	</ul>

     * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle
     * @param powerOpOptions VIX_VMPOWEROP_NORMAL or VIX_VMPOWEROP_LAUNCH_GUI
     * @param propertyListHandle Must be VIX_INVALID_HANDLE
     * @param callbackProc A callback function that will be invoked when the power operation is complete
     * @param clientData A parameter that will be passed to the callbackProc function
     * 
     * @return VixHandle. A job handle that describes the state of this asynchronous operation
     * 
     */
	public static native VixHandle VixVM_PowerOn(VixHandle vmHandle, int powerOpOptions, 
	  VixHandle propertyListHandle, VixEventProc callbackproc, Object clientData);
	        
 	/** This function powers off a virtual machine. 
 	 * 
 	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle
 	 * @param powerOffOptions Must be 0.
 	 * @param callbackproc A callback function that will be invoked when the power operation is complete
 	 * @param clientData A parameter that will be passed to the callbackProc function. 
 	 */
	public static native VixHandle VixVM_PowerOff(VixHandle vmHandle, int powerOffOptions,
	  VixEventProc callbackProc, Object clientData);


 	/** This function decrements the reference count for a handle and destroys the handle when there are no references. 
 	 *
 	 * <p>This function decrements the reference count for a handle. You should no
		longer use the handle once it has been released. When the last reference
		to a handle has been released, the runtime state for the handle is destroyed.
 	 *
 	 * @param handle Any handle returned by a Vix function 
 	 */
	public static native void Vix_ReleaseHandle(VixHandle handle);

	/** Wait for a particular job to complete. 
	 * 
	 * @param jobHandle The handle of a job object, returned from any asynchronous Vix function
	 * @param propertyIds A list of requested properties, identified by VIX_PROPERTY_* constants
	 *   (these constants will need to be wrapped in Integer classes). 
	 * 
	 * @return a list of property values
	 * 
	 * @throws VixException if the job did not return VIX_OK
	 */	
	public static native List VixJob_Wait(VixHandle jobHandle, List propertyIds) throws VixException;
	
	/** Wait for a particular job to complete. 
	 * 
	 * @param jobHandle The handle of a job object, returned from any asynchronous Vix function
	 * @param propertyIds A list of requested properties, identified by VIX_PROPERTY_* constants
	 * 
	 * @return a list of property values
	 * 
	 * @throws VixException if the job did not return VIX_OK
	 */	
	public static native List VixJob_Wait(VixHandle jobHandle, int[] propertyIds) throws VixException;
	

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
		<li> It is not necessary to call 
		VixVM_LoginInGuest().
		before calling this function.
		<li> Shared folders are not supported for the following guest operating systems:  
		Windows ME, Windows 98, Windows 95, Windows 3.x, and DOS.
		</ul>
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle
	 * @param shareName Specifies the guest path name of the new shared folder
	 * @param hostPathName Specifies the host path of the shared folder
	 * @param flags The folder options
	 * @param callback A callback function that will be invoked when the operation is complete
	 * @param clientData A parameter that will be passed to the callbackProc function
	 * 
	 * @return A job handle that describes the state of this asynchronous operation. 
	 */
	public static native VixHandle VixVM_AddSharedFolder(VixHandle vmHandle, 
	  String shareName, String hostPathName, int flags, VixEventProc callbackProc, Object clientData);
	  
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
		VixVM_LoginInGuest().
		before calling this procedure.
		<li> The copy operation requires VMware Tools to be installed and running
		in the guest operating system.
		<li> If any file fails to be copied, the operation returns an error. In this
		case, Vix aborts the operation and does not attempt to copy the remaining
		files.
		</ul>
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle
	 * @param guestPathName The path name of a file on a file system available to the guest
	 * @param hostPathName The path name of a file on a file system available to the host
	 * @param options Must be 0
	 * @param propertyListHandle Must be VIX_INVALID_HANDLE
	 * @param callbackProc A callback function that will be invoked when the operation is complete
	 * @param clientData A parameter that will be passed to the callbackProc function
	 * 
	 * @return A job handle that describes the state of this asynchronous operation
	 */
	public static native VixHandle VixVM_CopyFileFromGuestToHost(VixHandle vmHandle,
	  String guestPathName, String hostPathName, int options, VixHandle propertyListHandle,
	  VixEventProc callbackProc, Object clientData);


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
		VixVM_LoginInGuest().
		before calling this procedure.
		<li> The copy operation requires VMware Tools to be installed and running
		in the guest operating system.
		<li> If any file fails to be copied, the operation returns an error. In this
		case, Vix aborts the operation and does not attempt to copy the remaining
		files.
		</ul>
	 *  
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle.
	 * @param hostPathName The path name of a file on a file system available to the host. 
	 * @param guestPathName The path name of a file on a file system available to the guest. 
	 * @param options Must be 0. 
	 * @param propertyListHandle Must be VIX_INVALID_HANDLE. 
	 * @param callbackProc A  callback function that will be invoked when the operation is complete. 
	 * @param clientData A parameter that will be passed to the callbackProc function
	 * 
	 * @return A job handle that describes the state of this asynchronous operation
	 */ 
	public static native VixHandle VixVM_CopyFileFromHostToGuest(VixHandle vmHandle,
	  String hostPathName, String guestPathName, int options, VixHandle propertyListHandle,
	  VixEventProc callbackProc, Object clientData);

	 

	/** This function establishes a guest operating system authentication context that can be used 
	 * with guest functions for the given virtual machine handle.
	 *
	 * <p>
		<ul>
		<li> This function validates the account name and password in the guest OS. You must 
		call this function before calling functions to perform operations on the guest
		operating system, such as 
		VixVM_RunProgramInGuest().
		If you do not call
		any guest functions, you do not need to call 
		VixVM_LoginInGuest().
		<li> The following functions require that you call VixVM_LoginInGuest().
		<ul>
		<li> VixVM_RunProgramInGuest()
		<li> VixVM_ListProcessesInGuest()
		<li> VixVM_KillProcessInGuest()
		<li> VixVM_RunScriptInGuest()
		<li> VixVM_OpenUrlInGuest()
		<li> VixVM_CopyFileFromHostToGuest()
		<li> VixVM_CopyFileFromGuestToHost()
		<li> VixVM_DeleteFileInGuest()
		<li> VixVM_FileExistsInGuest()
		<li> VixVM_RenameFileInGuest()
		<li> VixVM_CreateTempFileInGuest()
		<li> VixVM_ListDirectoryInGuest()
		<li> VixVM_CreateDirectoryInGuest()
		<li> VixVM_DeleteDirectoryInGuest()
		<li> VixVM_DirectoryExistsInGuest()
		<li> VixVM_WriteVariable() when writing a VIX_GUEST_ENVIRONMENT_VARIABLE value
		<p>
		</ul>
		<li> All guest operations for a particular VM handle will be done using the identity
		you provide to 
		VixVM_LoginInGuest().
		As a result, guest operations will be 
		restricted by file system priveleges in the guest OS that apply to the user 
		specified in 
		VixVM_LoginInGuest().
		For example,
		VixVM_DeleteDirectoryInGuest().
		may fail if the user named in 
		VixVM_LoginInGuest().
		does not have access permissions
		to the directory in the guest OS.
		<p>
		<li> 
		VixVM_LoginInGuest()
		changes the behavior of Vix functions to use a user account.
		It does not log in a user into a console session on the guest operating system. As 
		a result, you may not see the user logged in from within the guest operating system.
		Moreover, operations such as rebooting the guest do not clear the guest 
		credentials.
		<p>
		<li> You must call 
		VixVM_LoginInGuest()
		for each VM handle that uses guest operations.
		<p>
		<li> The virtual machine must be powered on before calling this function.
		<p>
		<li> VMware Tools must be installed and running on the guest operating system
		before calling this function. You can call 
		VixVM_WaitForToolsInGuest()
		to wait for the tools to run.
		<p>
		<li> You can call 
		VixVM_LogoutFromGuest()
		to remove the user information from the VMHandle.
		<p>
		<li> You can always explicitly login in the guest by providing a username and 
		password that is valid on the guest. Then you will execute all guest 
		operations as that user. This is the default mechanism and is encouraged.
		<p>
		<li> Optionally, you may call 
		VixVM_LoginInGuest()
		with the constant defined by
		VIX_CONSOLE_USER_NAME in vix.h as the userName, and NULL for the password.
		If there is a user currently logged into the guest at the console (through the MKS)
		then all guest operations will be run as the console user. This is enabled
		by default and must be explicitly disabled by setting 
		the "guest.commands.anonGuestCommandsRunAsConsoleUser" config value to
		false. If no user is logged in at the guest console, the call to 
		VixVM_LoginInGuest()
		will block and wait for a user to log in to the console.
		</ul>
	 *  
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param username The name of a user account on the guest operating system. 
	 * @param password The password of the account identified by userName. 
	 * @param options Must be 0. 
	 * @param callbackProc A callback function that will be invoked when the operation is complete. 
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 *  
	 * @return A job handle that describes the state of this asynchronous operation
	 */
	public static native VixHandle VixVM_LoginInGuest(VixHandle vmHandle,
	  String username, String password, int options, VixEventProc callbackProc,
	  Object clientData); 

	/** This function removes any guest operating system authentication context created by 
	 * a previous call to VixVM_LoginInGuest().
	 * 
	 * <ul>
	 *  <li>This function has no effect and returns success if VixVM_LoginInGuest() has 
	 *   not been called. 
     * </ul>
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param callbackProc A callback function that will be invoked when the operation is complete. 
	 * @param clientData A parameter that will be passed to the callbackProc function. 
     *
	 * @return A job handle that describes the state of this asynchronous operation. 
	 */
	public static native VixHandle VixVM_LogoutFromGuest(VixHandle vmHandle,
	  VixEventProc callbackProc, Object clientData);
	
	/** This function lists the running processes in the guest operating system. 
	 *
		<ul>
		<li> You must call 
		VixVM_LoginInGuest().
		before calling this function.
		<li> VixJob_GetNumProperties() should be used to determine the number of results.
		<li> VixJob_GetNthProperties() can be used to get each result.
		<li> When the job is signaled, the following list of properties will be available
		</ul>
		on the returned job handle:
		<ul>
		<li> VIX_PROPERTY_JOB_RESULT_ITEM_NAME: the process name
		<li> VIX_PROPERTY_JOB_RESULT_PROCESS_ID: the process id
		<li> VIX_PROPERTY_JOB_RESULT_PROCESS_OWNER: the process owner
		<li> VIX_PROPERTY_JOB_RESULT_PROCESS_COMMAND: the process command line
		</ul>
	 *  
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param options Must be 0. 
	 * @param callbackProc A callback function that will be invoked when the operation is complete. 
	 * @param clientData A parameter that will be passed to the callbackProc function. 
     *
	 * @return A job handle that describes the state of this asynchronous operation. 
	 */
	public static native VixHandle VixVM_ListProcessesInGuest(VixHandle vmHandle,
	  int options, VixEventProc callbackProc, Object clientData);

	/** This function terminates a process in the guest operating system. 
	 * 
	 * <p>You must call VixVM_LoginInGuest(). before calling this function. 
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param pid The ID of the process to be killed. 
	 * @param options Must be 0. 
	 * @param callbackProc A callback function that will be invoked when the operation is complete. 
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 *  
	 * @return A job handle that describes the state of this asynchronous operation. 
	 */
	public static native VixHandle VM_KillProcessInGuest(VixHandle vmHandle,
	  long pid, int options, VixEventProc callbackProc, Object clientData); 

	/** This function returns the handle of the current active snapshot belonging to 
	 * the virtual machine referenced by vmHandle.
	 * 
	    <ul>
		<li> This function returns the handle of the current active snapshot belonging to
		the virtual machine referenced by vmHandle.
		<li> The snapshotHandle returned by this function is reference counted.
		The calling application is responsible for releasing the handle.
		</ul>	 
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle.
	 *  
	 * @return A handle to the current snapshot. 
	 * 
	 * @throws VixException
	 */ 
	public static native VixHandle VixVM_GetCurrentSnapshot(VixHandle vmHandle) throws VixException;

	/** This function returns the handle of the snapshot matching the given name in the virtual machine referenced by vmHandle. 
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param snapshotName Idenitfies a snapshot name.
	 *  
	 * @return a handle to the named snapshot
	 * 
	 * @throws VixException
	 */	
	public static native VixHandle VixVM_GetNamedSnapshot(VixHandle vmHandle,
      String snapshotName) throws VixException;


	/** This function returns the number of top-level (root) snapshots belonging to a virtual machine.
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
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle.
	 * 
	 * @return The number of root snapshots on this virtual machine. 
	 * 
	 * @throws VixException
	 */	
	public static native int VixVM_GetNumRootSnapshots(VixHandle vmHandle) throws VixException;

 
	/** This function returns the handle of the specified snapshot belonging to the 
	 * virtual machine referenced by vmHandle.
	 * 
		<ul>
		<li> This function returns the handle of the specified snapshot belonging to
		the virtual machine referenced by vmHandle.
		<li> Snapshots are indexed from 0 to n-1, where n is the number of root
		snapshots. Use the function 
		VixVM_GetNumRootSnapshots()
		to get the
		value of n.
		<li> VMware Server can manage only a single snapshot
		for each virtual machine. The value of index can only be 0.
		<li> The snapshotHandle returned by this function is reference counted.
		The calling application is responsible for releasing the handle.
		</ul>
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param index Identifies a root snapshot. See below for range of values.
	 *  
	 * @return a handle to a snapshot 
	 * 
	 * @throws VixException
	 */
	public static native VixHandle VixVM_GetRootSnapshot(VixHandle vmHandle,
	  int index) throws VixException;
						  
	/** This function opens a browser window on the specified URL in the guest operating system. 
	 * 
		<ul>
		<li> This function opens the URL in the guest operating system.
		<li> You must call VixVM_LoginInGuest() with VIX_CONSOLE_USER_NAME
		as the userName and NULL as the password before calling this function.
		</ul>
	 * 
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param url The URL to be opened
	 * @param windowState Must be 0. 
	 * @param propertyListHandle Must be VIX_INVALID_HANDLE. 
	 * @param callbackProc A callback function that will be invoked when the operation is complete. 
	 * @param clientData A  parameter that will be passed to the callbackProc function.
	 *  
	 * @return A job handle that describes the state of this asynchronous operation. 
	 */
	public static native VixHandle VixVM_OpenUrlInGuest(VixHandle vmHandle,
	  String url, int windowState, VixHandle propertyListHandle, VixEventProc callbackProc,
	  Object clientData);
	  
	  
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
	public static native VixHandle VixVM_EnableSharedFolders(VixHandle vmHandle,
	  boolean enabled, int option, VixEventProc callbackProc, Object clientData);

	/** This function returns the specified child snapshot. 
	 * 
	 * @param parentSnapshotHandle A snapshot handle. 
	 * @param index Index into the list of snapshots
	 * 
	 * @return A handle to the child snapshot
	 * 
	 * @throws VixException
	 */
	public static native VixHandle VixSnapshot_GetChild(VixHandle parentSnapshotHandle,
	 int index) throws VixException;					 

	/** This function returns the number of child snapshots of a specified snapshot
	 * 
	 * @param parentSnapshotHandle A snapshot handle.
	 * 
	 * @return The number of child snapshots belonging to the specified snapshot
	 * 
	 * @throws VixException
	 */
	public static native int VixSnapshot_GetNumChildren(VixHandle parentSnapshotHandle)
	  throws VixException;

	/** This function returns the parent of a snapshot.
	 * 
	 * @param snapshotHandle A snapshot handle. 
	 *  
	 * @return A handle to the parent of the specified snapshot.
	 * 
	 * @throws VixExcpetion
	 */
	public static native VixHandle VixSnapshot_GetParent(VixHandle snapshotHandle) throws VixException;
	
	
	/** This function saves a copy of the virtual machine state as a snapshot object. 
	 * The handle of the snapshot object is returned in the job object properties.
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
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle 
	 * @param name A user-defined name for the snapshot; need not be unique
	 * @param description A user-defined description for the snapshot
	 * @param options Flags to specify how the shapshot should be created. Any combination of the following or 0:
	 *   <attributes> 
         VIX_SNAPSHOT_INCLUDE_MEMORY - Captures the full state of a running virtual machine, including the memory.
         </attributes>
	 * @param VixHandle Must be VIX_INVALID_HANDLE. 
     *
	 * @return A job handle that describes the state of this asynchronous operation
	 */
	public static native VixHandle VixVM_CreateSnapshot(VixHandle vmHandle,
	  String name, String description, int options, VixHandle propertyListHandle,
	  VixEventProc callbackProc, Object clientData);

	/** Restores the virtual machine to the state when the specified snapshot was created.
	 * 
		<ul>
		<li> Restores the virtual machine to the state when the specified snapshot was
		created. This function can power on, power off, or suspend a virtual machine.
		The resulting power state reflects the power state when the snapshot was
		created.
		<li> When you revert a powered on virtual machine and want it to display in the 
		Workstation user interface,
		options must have the VIX_VMPOWEROP_LAUNCH_GUI flag, unless the 
		VIX_VMPOWEROP_SUPPRESS_SNAPSHOT_POWERON is used.
		<li> The VIX_PROPERTY_VM_TOOLS_STATE property of the virtual machine handle is
		undefined after the snapshot is reverted. 
		VixVM_WaitForToolsInGuest()
		must be called to refresh this property.
		</ul>
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle
	 * @param snapshotHandle  A handle to a snapshot. Call VixVM_GetRootSnapshot() to get a snapshot handle
	 * @param options Any applicable VixVMPowerOpOptions. If the virtual machine was 
	 *   powered on when the snapshot was created, then this will determine how the 
	 *   virtual machine is powered back on. To prevent the virtual machine from being 
	 *   powered on regardless of the power state when the snapshot was created, 
	 *   use the VIX_VMPOWEROP_SUPPRESS_SNAPSHOT_POWERON flag. 
	 *   VIX_VMPOWEROP_SUPPRESS_SNAPSHOT_POWERON is mutually exclusive to all 
	 *   other VixVMPowerOpOptions
	 * @param propertyListHandle Must be VIX_INVALID_HANDLE
	 * @param callbackproc A callback function that will be invoked when the operation is complete
	 * @param clientData A parameter that will be passed to the callbackProc function 
	 * 
	 * @return A job handle that describes the state of this asynchronous operation
	 */
	public static native VixHandle VixVM_RevertToSnapshot(VixHandle vmHandle,
	  VixHandle snapshotHandle, int options, VixHandle propertyListHandle,
	  VixEventProc callbackproc, Object clientData);


	/** This function runs a program in the guest operating system. The program must be stored on a 
	 * file system available to the guest before calling this function
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle
	 * @param guestProgramName The path name of an executable file on the guest operating system 
	 * @param commandLineArgs A string to be passed as command line arguments to the executable identified by guestProgramName
	 * @param options Run options for the program. See the remarks below
	 * @param propertyListHandle Must be VIX_INVALID_HANDLE
	 * @param callbackProc A callback function that will be invoked when the operation is complete
	 * @param clientData A parameter that will be passed to the callbackProc function
	 * 
	 * @return A job handle that describes the state of this asynchronous operation
	 */
	public static native VixHandle VixVM_RunProgramInGuest(VixHandle vmHandle,
	  String guestProgramName, String commandLineArgs,
	  int options, VixHandle propertyListHandle, VixEventProc callbackProc,
	  Object clientData);

	/** This function runs a script in the guest operating system. 
	 *
		<ul>
		<li> This function runs the script in the guest operating system.
		<li> The current working directory for the script executed in the guest is not defined.
		Absolute paths should be used for files in the guest, including the path
		to the interpreter, and any files referenced in the script text.
		<li> You must call
		VixVM_LoginInGuest()
		before calling this function.
		<li> If the options parameter is 0, this function will
		report completion to the job handle
		when the program exits in the guest operating system.
		Alternatively, you can pass
		VIX_RUNPROGRAM_RETURN_IMMEDIATELY
		as the
		value of the options parameter, and this function
		reports completion to the job handle
		as soon as the program starts in the guest.
		<li> When the job is signaled, the following properties will be available on
		the returned job handle:
		<ul>
		<li> VIX_PROPERTY_JOB_RESULT_PROCESS_ID: the process id;
		<li> VIX_PROPERTY_JOB_RESULT_GUEST_PROGRAM_ELAPSED_TIME: the process elapsed time;
		<li> VIX_PROPERTY_JOB_RESULT_GUEST_PROGRAM_EXIT_CODE: the process exit code.
		</ul>
		If the option parameter is VIX_RUNPROGRAM_RETURN_IMMEDIATELY, the latter two will
		both be 0.
		</ul>	 
	 *  
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param interpreter The path to the script interpreter
	 * @param scriptText The text of the script. 
	 * @param option Run options for the program. See the notes below
	 * @param propertyListHandle Must be VIX_INVALID_HANDLE. 
	 * @param callbackProc A callback function that will be invoked when the operation is complete. 
	 * @param clientData A parameter that will be passed to the callbackProc function. 
     * 
	 * @return A job handle that describes the state of this asynchronous operation
	 */
	public static native VixHandle VixVM_RunScriptInGuest(VixHandle vmHandle,
	  String interpreter, String scriptText, int option, VixHandle propertyListHandle,
	  VixEventProc callbackProc, Object clientData);


	/** This function signals the job handle when VMware Tools has successfully started 
	 * in the guest operating system. VMware Tools is a collection of services that run 
	 * in the guest. 
	 * 
	 * <ul>
		<li> This function 
		signals the job
		when VMware Tools has successfully started
		in the guest operating system. VMware Tools is a collection of services
		that run in the guest.
		<li> VMware Tools must be installed and running for some Vix functions to operate
		correctly. If VMware Tools is not installed in the guest operating system,
		or if the virtual machine is not powered on, this function reports an error
		to the job object.
		<li> The VIX_PROPERTY_VM_TOOLS_STATE property of the virtual machine handle is
		undefined until 
		VixVM_WaitForToolsInGuest()
		reports that VMware Tools is running.
		</ul>
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param timeoutInSeconds The timeout in seconds. If VMware Tools has not started by this time, the function completes with an error. If the value of this argument is zero or negative, then there will be no timeout. 
	 * @param callbackProc A callback function that will be invoked when the operation is complete. 
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 *  
	 * @return A job handle that describes the state of this asynchronous operation. 
	 */
	public static native VixHandle VixVM_WaitForToolsInGuest(VixHandle vmHandle,
	  int timeoutInSeconds, VixEventProc callbackProc, Object clientData);


	/** Destroys the state for a particular host handle.
	 * 
	 * <p>Call this function to disconnect the host. After you call this function the handle 
	 * is no longer valid and you should not use it in any Vix function. Similarly, you should 
	 * not use any handles obtained from the host while it was connected.
	 * 
	 * @param hostHandle The host handle returned by VixHost_Connect().
	 */
	public static native void VixHost_Disconnect(VixHandle hostHandle);

	/** This function asynchronously finds Vix objects and calls the application's callback 
	 * function to report each object found. For example, when used to find all running 
	 * virtual machines, VixHost_FindItems() returns a series of virtual machine file 
	 * path names.
	 * 
	 * @param hostHandle The host handle returned by VixHost_Connect().
	 * @param searchType The type of items to find. Value should be a VIX_FIND_* constant.
	 * @param searchCriteria Must be VIX_INVALID_HANDLE.
	 * @param timeout Must be -1.
	 * @param callbackProc A function to be invoked when VixHost_FindItems() completes.
	 * @param clientData A user-supplied parameter to be passed to the callback function.
	 * 
	 * @return A job handle that describes the state of this asynchronous call.
	 */
	public static native VixHandle VixHost_FindItems(VixHandle hostHandle,
		int searchType, VixHandle searchCriteria, int timeout,
		VixEventProc callbackProc, Object clientData);


	/** This function adds a virtual machine to the host's inventory.
	 * 
	 * <ul><li>This function adds a virtual machine to the local host's inventory. 
	 * The virtual machine is identified by the vmxFilePathName, which is a path 
	 * name to the configuration file (.VMX file) for that virtual machine.
     * <li> The format of the path name depends on the host operating system. 
     * <li> This function does not apply to Workstation, which has no virtual machine 
     * inventory.
     * <li> This function is asynchronous. Completion is reported by a job handle.
     *  
	 * @param hostHandle The host handle returned by VixHost_Connect().
	 * @param vmxFilePath The path name of the .vmx file on the host.
	 * @param callbackProc A function to be invoked when VixHost_RegisterVM() completes.
	 * @param clientData A user-supplied parameter to be passed to the callback function.
	 * 
	 * @return A job handle that describes the state of this asynchronous call.
	 */
	public static native VixHandle VixHost_RegisterVM(VixHandle hostHandle,
	    String vmxFilePath, VixEventProc callbackProc, Object clientData);

	/** This function removes a virtual machine from the host's inventory.
	 * 
	 * <ul><li>This function removes a virtual machine from the local host's inventory. 
	 * The virtual machine is identified by the vmxFilePathName, which is a path name 
	 * to the configuration file (.VMX file) for that virtual machine.
     * <li>The format of the path name depends on the host operating system. 
     * <li>This function does not apply to Workstation, which has no virtual machine 
     * inventory.
     * <li>This function is asynchronous. Completion is reported by a job handle.
     * </ul> 
	 * 
	 * @param hostHandle The host handle returned by VixHost_Connect().
	 * @param vmxFilePath The path name of the .vmx file on the host.
	 * @param callbackProc A function to be invoked when VixHost_UnregisterVM() completes.
	 * @param clientData A user-supplied parameter to be passed to the callback function.
	 * 
	 * @return A job handle that describes the state of this asynchronous call.
	 */
	public static native VixHandle VixHost_UnregisterVM(VixHandle hostHandle,
		String vmxFilePath, VixEventProc callbackProc, Object clientData);

	/** This function performs a non-blocking test for completion of an asynchronous operation.
	 * 
	 * @param jobHandle The handle of a job object, returned from any asynchronous Vix function.
	 * 
	 * @return An indicator of whether the job has completed.
	 */
	public static native boolean VixJob_CheckCompletion(VixHandle jobHandle);
		/** This function creates a directory in the guest operating system.
	 * 
	 * <ul><li>You must call VixVM_LoginInGuest(). before calling this function.
     * <li>If the parent directories for the specified path do not exist, this function 
     * will create them.
     * <li>If the directory already exists, the error associated with the job handle will 
     * be set to VIX_E_FILE_ALREADY_EXISTS.
     * <li>Only absolute paths should be used for files in the guest; the resolution of 
     * relative paths is not specified. 
	 * </ul>
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle.
	 * @param pathName The path to the directory to be created.
	 * @param propertyListHandle Must be VIX_INVALID_HANDLE.
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientDataA parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation. 
	 */
	public static native VixHandle VixVM_CreateDirectoryInGuest(VixHandle vmHandle,
		String pathName, VixHandle propertyListHandle, VixEventProc callbackProc,
		Object clientData);
	
	/** This function creates a temporary file in the guest operating system.
	 * 
	 * <ul><li>You must call VixVM_LoginInGuest(). before calling this function. 
	 * <li>The result of the call is in the property VIX_PROPERTY_JOB_RESULT_ITEM_NAME on the returning jobHandle.
	 * </ul> 
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle.
	 * @param options Must be 0.
	 * @param propertyListHandle Must be VIX_INVALID_HANDLE.
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientDataA parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_CreateTempFileInGuest(VixHandle vmHandle,
		int options, VixHandle propertyListHandle, VixEventProc callbackProc,
		Object clientData);

	/** This function permanently deletes a virtual machine from your host system.
	 *
	 * <ul><li>This function permanently deletes a virtual machine from your host system. You can accomplish the same effect by deleting all virtual machine files using the host file system. This function simplifies the task by deleting all VMware files in a single function call. This function does not delete other user files in the virtual machine folder. 
     * <li>This function is successful only if the virtual machine is powered off or suspended. 
     * <li>This< function is asynchronous. It uses a job handle to report when it is complete. 
     * <li>Calling VixVM_Delete() on a virtual machine handle does not release the virtual machine handle. You still need to call Vix_ReleaseHandle() on the virtual machine handle.
     * </ul> 
	 *  
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle.
	 * @param deleteOptions Must be 0.
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientDataA parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_Delete(VixHandle vmHandle, 
	    int deleteOptions, VixEventProc callbackProc,
		Object clientData);
		
	/** This function deletes a directory in the guest operating system. Any files or subdirectories in the specified directory will also be deleted.
	 *
	 * <ul><li>You must call VixVM_LoginInGuest() before calling this function. 
	 * <li>Only absolute paths should be used for files in the guest; the resolution of relative paths is not specified.
	 * </ul> 
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle.
	 * @param pathName The path to the directory to be deleted. 
	 * @param options Must be 0.
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_DeleteDirectoryInGuest(VixHandle vmHandle,
		String pathName, int options, VixEventProc callbackProc, Object clientData);

	/** This function deletes a file in the guest operating system.
	 *  
	 * <ul><li>You must call VixVM_LoginInGuest(). before calling this function. 
     * <li>Only absolute paths should be used for files in the guest; the resolution of relative paths is not specified.
     * </ul> 
	 *  
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param guestPathName The path to the file to be deleted.
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_DeleteFileInGuest(VixHandle vmHandle,
		String guestPathName, VixEventProc callbackProc, Object clientData);
		
	/** This function tests the existence of a directory in the guest operating system.
	 *
	 * <ul><li>You must call VixVM_LoginInGuest(). before calling this function. 
     * <li> The result of the call is in the property VIX_PROPERTY_JOB_RESULT_GUEST_OBJECT_EXISTS on the returning jobHandle. 
     * <li> Only absolute paths should be used for files in the guest; the resolution of relative paths is not specified. 
     * </ul>
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param pathName The path to the directory in the guest to be checked. 
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */ 
	public static native VixHandle VixVM_DirectoryExistsInGuest(VixHandle vmHandle,
		String pathName, VixEventProc callbackProc, Object clientData);

	/** This function tests the existence of a file in the guest operating system.
	 * 
	 * <ul><li>You must call VixVM_LoginInGuest(). before calling this function. 
     * <li>The result of the call is in the property VIX_PROPERTY_JOB_RESULT_GUEST_OBJECT_EXISTS on the returning jobHandle. 
     * <li>Only absolute paths should be used for files in the guest; the resolution of relative paths is not specified. 
     * <li>If guestPathName exists as a file system object, but is not a normal file (e.g. it is a directory, device, UNIX domain socket, etc), then VIX_OK is returned, and VIX_PROPERTY_JOB_RESULT_GUEST_OBJECT_EXISTS is set to FALSE. 
     * </ul>
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle.
	 * @param guestPathName The path to the file to be tested.
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */ 
	public static native VixHandle VixVM_FileExistsInGuest(VixHandle vmHandle,
		String guestPathName, VixEventProc callbackProc, Object clientData);

	/** This function returns the number of shared folders mounted in the virtual machine. 
	 * 
	 * <ul><li>This function returns the number of shared folders mounted in the virtual machine referenced by 
	 * vmHandle. 
	 * <li>When the job is signaled, the property VIX_PROPERTY_JOB_RESULT_SHARED_FOLDER_COUNT will be available on the returned job handle 
	 * <li>It is not necessary to call VixVM_LoginInGuest() before calling this function. 
	 * <li>Shared folders are not supported for the following guest operating systems: Windows ME, Windows 98, Windows 95, Windows 3.x, and DOS. 
	 * </ul>
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_GetNumSharedFolders(VixHandle vmHandle,
	    VixEventProc callbackProc, Object clientData);
	    
	/** This function returns the state of a shared folder mounted in the virtual machine. 
	 * 
	 * <ul>Shared folders are indexed from 0 to n-1, where n is the number of shared folders. Use the function VixVM_GetNumSharedFolders() to get the value of n. 
	 * <li>When the job is signaled, the following properties will be available on the returned job handle: 
	 * <ul><li>VIX_PROPERTY_JOB_RESULT_ITEM_NAME the name of the folder 
	 * <li>VIX_PROPERTY_JOB_RESULT_SHARED_FOLDER_HOST the host path its mounted from 
	 * <li>VIX_PROPERTY_JOB_RESULT_SHARED_FOLDER_FLAGS flags describing the folder options VIX_SHAREDFOLDER_WRITE_ACCESS
	 * </ul> 
	 * <li>It is not necessary to call VixVM_LoginInGuest() before calling this function. 
	 * <li>Shared folders are not supported for the following guest operating systems: Windows ME, Windows 98, Windows 95, Windows 3.x, and DOS.
	 * </ul> 
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle.
	 * @param index Identifies the shared folder.
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_GetSharedFolderState(VixHandle vmHandle,
	    int index, VixEventProc callbackProc, Object clientData);
	
	/** Installs VMware Tools on the guest operating system.
	 * 
	 * <ul><li>Installs VMware Tools on the guest operating system. If VMware Tools is already installed, this function upgrades VMware Tools to the version that matches the Vix library. 
	 * <li>The virtual machine must be powered on to do this operation.
	 * </ul> 
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param options Must be 0. 
	 * @param commandLineArgs Must be null.
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_InstallTools(VixHandle vmHandle, int options,
		String commandLineArgs, VixEventProc callbackProc, Object clientData);
	
	/** This function terminates a process in the guest operating system. 
	 * 
	 * <ul><li>You must call VixVM_LoginInGuest(). before calling this function. 
	 * </ul>
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle.
	 * @param pid The ID of the process to be killed. 
	 * @param options Must be 0.
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_KillProcessInGuest(VixHandle vmHandle,
		long pid, int options, VixEventProc callbackProc, Object clientData);
	
	/** This function lists a directory in the guest operating system. 
	 * 
	 * <ul><li>You must call VixVM_LoginInGuest(). before calling this function. 
	 * <li>VixJob_GetNumProperties() should be used to determine the number of results 
	 * <li>VixJob_GetNthProperties() can be used to get each result 
	 * <li>When the job is signaled, the following list of properties will be available on the returned job handle: 
	 * <ul><li>VIX_PROPERTY_JOB_RESULT_ITEM_NAME: the file name 
	 * <li>VIX_PROPERTY_JOB_RESULT_FILE_FLAGS: file attribute flags
	 * </ul> 
	 * <li>Only absolute paths should be used for files in the guest; the resolution of relative paths is not specified.
	 * </ul> 
	 *  
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param pathName The path name of a directory to be listed. 
	 * @param options Must be 0. 
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_ListDirectoryInGuest(VixHandle vmHandle,
	    String pathName, int options, VixEventProc callbackProc, Object clientData);

	/** This function removes a shared folder in the virtual machine.
	 * 
	 * <ul><li>This function removes a shared folder in the virtual machine referenced by vmHandle. 
	 * <li>It is not necessary to call VixVM_LoginInGuest() before calling this function. 
	 * <li>Shared folders are not supported for the following guest operating systems: Windows ME, Windows 98, Windows 95, Windows 3.x, and DOS.
	 * </ul> 
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle.
	 * @param shareName Specifies the guest pathname of the shared folder to delete. 
	 * @param flags Must be 0. 
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_RemoveSharedFolder(VixHandle vmHandle,
		String shareName, int flags, VixEventProc callbackProc, Object clientData);
		
	/** This function deletes all saved states for the specified snapshot
	 * 
	 * <ul><li>This function deletes all saved states for the specified snapshot. If the snapshot was based on another snapshot, the base snapshot becomes the new root snapshot. 
     * <li>A snapshot can be removed only while the associated virtual machine is powered off or suspended. 
     * <li>The Server 1.0 release of the VIX API can manage only a single snapshot for each virtual machine. A virtual machine imported from another VMware product can have more than one snapshot at the time it is imported. In that case, you can delete only a snapshot subsequently added using the VIX API.
     * </ul>
     * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle
	 * @param snapshotHandle A handle to a snapshot. Call VixVM_GetRootSnapshot() to get a snapshot handle
	 * @param options Flags to specify optional behavior. Any combination of the following or 0: 
	 *  <ul><li>VIX_SNAPSHOT_REMOVE_CHILDREN - Remove snapshots that are children of the given snapshot.
	 *  </ul> 
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_RemoveSnapshot(VixHandle vmHandle,
		VixHandle snapshotHandle, int options, VixEventProc callbackProc, Object clientData);
	
	/** This function renames a file or directory in the guest operating system
	 * 
	 * <ul><li>You must call VixVM_LoginInGuest() before calling this function. 
     * <li>Only absolute paths should be used for files in the guest; the resolution of relative paths is not specified.
     * </ul> 
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param oldName The path to the file to be renamed. 
	 * @param newName The path to the new file. 
	 * @param options Must be 0
	 * @param propertyListHandle Must be VIX_INVALID_HANDLE. 
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_RenameFileInGuest(VixHandle vmHandle,
		String oldName, String newName, int options, VixHandle propertyListHandle,
		VixEventProc callbackProc, Object clientData);

	/** This function resets a virtual machine, which is the equivalent of pressing the reset button on a physical machine
	 * 
	 * <p>The reset is an asynchronous operation, and the job will be signalled when the operation 
	 * completes. If the virtual machine is not powered on when you call 
	 * this function, it returns an error. 
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param powerOnOptions Must be VIX_VMPOWEROP_NORMAL. 
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_Reset(VixHandle vmHandle,
		int powerOnOptions, VixEventProc callbackProc, Object clientData);
	
	/** This function modifies the state of a shared folder mounted in the virtual machine.
	 * 
	 * <ul><li>This function modifies the state flags of an existing shared folder. 
	 * <li>If the shared folder specified by shareName does not exist before calling this function, the job handle for this function will return VIX_E_NOT_FOUND. 
	 * <li>It is not necessary to call VixVM_LoginInGuest() before calling this function. 
	 * <li>Shared folders are not supported for the following guest operating systems: Windows ME, Windows 98, Windows 95, Windows 3.x, and DOS.
	 * </ul> 
	 *  
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param shareName Specifies the name of the shared folder
	 * @param hostPathName Specifies the host path of the shared folder. 
	 * @param flags The new flag settings. 
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_SetSharedFolderState(VixHandle vmHandle,
	    String shareName, String hostPathName, int flags, VixEventProc callbackProc,
	    Object clientData);
	     
	/** This function suspends a virtual machine.
	 * 
	 * <p>It is an asynchronous operation, and the job will be signaled when 
	 * the operation completes. If the virtual machine is not powered on when you 
	 * call this function, the function returns an error. 
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param powerOffOptions Must be 0.
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_Suspend(VixHandle vmHandle,
	    int powerOffOptions, VixEventProc callbackProc, Object clientData);
	    
	/** Upgrades the virtual hardware version of the virtual machine to match 
	 * the version of the VIX library. This has no effect if the virtual machine 
	 * is already at the same version or at a newer version than the VIX library. 
	 * 
	 * <ul><li>The virtual machine must be powered off to do this operation. 
	 * <li>When the VM is already up-to-date, the job handle for this function will return VIX_E_VM_ALREADY_UP_TO_DATE.
	 * </ul> 
	 * 
	 * @param vmHandle Identifies a virtual machine. Call VixVM_Open() to create a virtual machine handle. 
	 * @param options Must be 0.
	 * @param callbackProc A callback function that will be invoked when the operation is complete.
	 * @param clientData A parameter that will be passed to the callbackProc function.
	 * 
	 * @return A job handle that describes the state of this asynchronous operation.
	 */
	public static native VixHandle VixVM_UpgradeVirtualHardware(VixHandle vmHandle,
		int options, VixEventProc callbackProc, Object clientData);
		
	/** This function allows you to get one or more properties from a handle. 
	 * 
	 * <ul><li>This function allows you to get one or more properties from a handle. You may use this function on any type of handle, but only specific properties are defined for each handle. 
	 * <li>This procedure accepts a variable number of parameters, so you can retrieve any number of properties with a single call. The parameters must be in a series of pairs of property IDs and result pointers. Each result pointer will accept the value of the property identified by the property ID in the previous parameter. The type of the pointer depends on the type of the property. You end the variable list of parameters with a single ID value of VIX_PROPERTY_NONE. 
	 * <li>When Vix_GetProperties() returns an error, the values of the output parameters are indeterminate. 
	 * <li>If you retrieve a string property, the Programming API allocates space for that string. You are responsible for calling Vix_FreeBuffer() to free the string. 
	 * <li>The value of VIX_PROPERTY_VM_TOOLS_STATE is valid only after calling VixVM_WaitForToolsInGuest().
	 * </ul>
	 * 
	 * @see #Vix_GetProperties(VixHandle, int[])
	 * 
	 * @param handle Any handle returned by a Vix function. 
	 * @param propertyIds A list of property IDs. See above for valid values.
	 *  
	 * @return the List of properties requested
	 */	
	public static native List Vix_GetProperties(VixHandle handle, List propertyIds);
	
	/** This function allows you to get one or more properties from a handle. 
	 * 
	 * <ul><li>This function allows you to get one or more properties from a handle. You may use this function on any type of handle, but only specific properties are defined for each handle. 
	 * <li>This procedure accepts a variable number of parameters, so you can retrieve any number of properties with a single call. The parameters must be in a series of pairs of property IDs and result pointers. Each result pointer will accept the value of the property identified by the property ID in the previous parameter. The type of the pointer depends on the type of the property. You end the variable list of parameters with a single ID value of VIX_PROPERTY_NONE. 
	 * <li>When Vix_GetProperties() returns an error, the values of the output parameters are indeterminate. 
	 * <li>If you retrieve a string property, the Programming API allocates space for that string. You are responsible for calling Vix_FreeBuffer() to free the string. 
	 * <li>The value of VIX_PROPERTY_VM_TOOLS_STATE is valid only after calling VixVM_WaitForToolsInGuest().
	 * </ul>
	 * 
	 * @see #Vix_GetProperties(VixHandle, List)
	 * 
	 * @param handle Any handle returned by a Vix function. 
	 * @param propertyIds An array of property IDs. See above for valid values.
	 *  
	 * @return the List of properties requested
	 */
	public static native List Vix_GetProperties(VixHandle handle, int[] propertyIds);
	
	/** Given a property ID, this function returns the type of that property. 
	 * 
	 * For a list of property data types, refer to the 
	 * Types Reference topic.
	 * 
	 * @param handle Any handle returned by a VIX function
	 * @param propertyId A property ID. See below for valid values. 
	 *
	 * @return The type of the data stored by the property
	 */
	public static native int Vix_GetPropertyType(VixHandle handle, int propertyId); 

	/** Vix_PumpEvents is used in single threaded applications that require the Vix 
	 * library to be single threaded. Tasks that would normally be executed in a 
	 * separate thread by the Vix library will be executed when Vix_PumpEvents() is called.
	 * 
	 * @param handle The handle to the local host object.
	 * @param options Must be 0.
	 */
	public static native void Vix_PumpEvents(VixHandle handle, int options);
	
	/** Returns a human-readable string that describes the error. 
	 * 
	 * @param vixError A Vix error code returned by any other Vix function
	 * @param locale Must be NULL.
	 */
	public static native void Vix_GetErrorText(int vixError, String locale);
	
	/** Retrieves the property at a specific index in a list. You can use 
	 * this to iterate through returned property lists.
	 * 
	 * @see #Vix_GetNthProperties(VixHandle, int, int[])
	 * 
	 * @param handle The handle of a job object, returned from any asynchronous Vix function.
	 * @param index index into the property list of the job object.
	 * @param propertyIds a list of property Ids
	 * 
	 * @return the request properties
	 */
	public static native List Vix_GetNthProperties(VixHandle handle, int index, List propertyIds);

	/** Retrieves the property at a specific index in a list. You can use 
	 * this to iterate through returned property lists.
	 * 
	 * @see #Vix_GetNthProperties(VixHandle, int, List)
	 * 
	 * @param handle The handle of a job object, returned from any asynchronous Vix function.
	 * @param index index into the property list of the job object.
	 * @param propertyIds an array of property Ids
	 * 
	 * @return the request properties
	 */
	public static native List Vix_GetNthProperties(VixHandle handle, int index, int[] propertyIds);

	/** Retrieves the number of instances of the specified property. 
	 * Used to work with returned property lists.
	 * 
	 * @param handle The handle of a job object, returned from any asynchronous Vix function.
	 * @param resultPropertyId A property ID.
	 * 
	 * @return the number of properties with an ID of resultPropertyID.
	 */
	public static native int Vix_GetNumProperties(VixHandle handle, int resultPropertyId);

	/* static intitialiser */
	static {
		System.out.println(System.getProperty("java.library.path"));
		System.loadLibrary("jvix");
		String buildId = "(custom build)";
		ClassLoader classLoader = VixWrapper.class.getClassLoader();
		InputStream is = classLoader.getResourceAsStream("./resources/jvixBuild.properties");
		if (is==null) {
			is = classLoader.getResourceAsStream("/resources/jvixBuild.properties");
		}
		if (is!=null) {
			Properties props = new Properties();
			try {
				props.load(is);
				buildId = props.getProperty("build.combinedTag");
			} catch (IOException ioe) {
				// ignore exceptions
			}
		}
		if (is==null) {
			Logger logger = Logger.getLogger(VixWrapper.class);
			logger.info("jvix initialised [" + buildId + "]");
		}
	}

}
