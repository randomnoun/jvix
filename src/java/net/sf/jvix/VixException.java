package net.sf.jvix;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.jvix.util.Util;

/**
 * Exception class used to indicate error conditions returned by the VIX API.
 * 
 * @author knoxg
 * @version $Id$
 */
public class VixException extends Exception {

	private static final long serialVersionUID = 1677162231277237942L;


	/** VIX error code */
	private int errorCode;


	/** Map of constant values to constant strings */
	private static Map errorConstants;

	public final static int VIX_OK                                       = 0;
	
	   /* General errors */
	public final static int VIX_E_FAIL                                   = 1;
	public final static int VIX_E_OUT_OF_MEMORY                          = 2;
	public final static int VIX_E_INVALID_ARG                            = 3;
	public final static int VIX_E_FILE_NOT_FOUND                         = 4;
	public final static int VIX_E_OBJECT_IS_BUSY                         = 5;
	public final static int VIX_E_NOT_SUPPORTED                          = 6;
	public final static int VIX_E_FILE_ERROR                             = 7;
	public final static int VIX_E_DISK_FULL                              = 8;
	public final static int VIX_E_INCORRECT_FILE_TYPE                    = 9;
	public final static int VIX_E_CANCELLED                              = 10;
	public final static int VIX_E_FILE_READ_ONLY                         = 11;
	public final static int VIX_E_FILE_ALREADY_EXISTS                    = 12;
	public final static int VIX_E_FILE_ACCESS_ERROR                      = 13;
	public final static int VIX_E_REQUIRES_LARGE_FILES                   = 14;
	public final static int VIX_E_FILE_ALREADY_LOCKED                    = 15;
	public final static int VIX_E_NOT_SUPPORTED_ON_REMOTE_OBJECT         = 20;
	public final static int VIX_E_FILE_TOO_BIG                           = 21;
	public final static int VIX_E_FILE_NAME_INVALID                      = 22;
	public final static int VIX_E_ALREADY_EXISTS                         = 23;

	   /* Handle Errors */
	public final static int VIX_E_INVALID_HANDLE                         = 1000;
	public final static int VIX_E_NOT_SUPPORTED_ON_HANDLE_TYPE           = 1001;
	public final static int VIX_E_TOO_MANY_HANDLES                       = 1002;

	   /* XML errors */
	public final static int VIX_E_NOT_FOUND                              = 2000;
	public final static int VIX_E_TYPE_MISMATCH                          = 2001;
	public final static int VIX_E_INVALID_XML                            = 2002;

	   /* VM Control Errors */
	public final static int VIX_E_TIMEOUT_WAITING_FOR_TOOLS              = 3000;
	public final static int VIX_E_UNRECOGNIZED_COMMAND                   = 3001;
	public final static int VIX_E_OP_NOT_SUPPORTED_ON_GUEST              = 3003;
	public final static int VIX_E_PROGRAM_NOT_STARTED                    = 3004;
	public final static int VIX_E_CANNOT_START_READ_ONLY_VM              = 3005;
	public final static int VIX_E_VM_NOT_RUNNING                         = 3006;
	public final static int VIX_E_VM_IS_RUNNING                          = 3007;
	public final static int VIX_E_CANNOT_CONNECT_TO_VM                   = 3008;
	public final static int VIX_E_POWEROP_SCRIPTS_NOT_AVAILABLE          = 3009;
	public final static int VIX_E_NO_GUEST_OS_INSTALLED                  = 3010;
	public final static int VIX_E_VM_INSUFFICIENT_HOST_MEMORY            = 3011;
	public final static int VIX_E_SUSPEND_ERROR                          = 3012;
	public final static int VIX_E_VM_NOT_ENOUGH_CPUS                     = 3013;
	public final static int VIX_E_HOST_USER_PERMISSIONS                  = 3014;
	public final static int VIX_E_GUEST_USER_PERMISSIONS                 = 3015;
	public final static int VIX_E_TOOLS_NOT_RUNNING                      = 3016;
	public final static int VIX_E_GUEST_OPERATIONS_PROHIBITED            = 3017;
	public final static int VIX_E_ANON_GUEST_OPERATIONS_PROHIBITED       = 3018;
	public final static int VIX_E_ROOT_GUEST_OPERATIONS_PROHIBITED       = 3019;
	public final static int VIX_E_MISSING_ANON_GUEST_ACCOUNT             = 3023;
	public final static int VIX_E_CANNOT_AUTHENTICATE_WITH_GUEST         = 3024;
	public final static int VIX_E_UNRECOGNIZED_COMMAND_IN_GUEST          = 3025;
	public final static int VIX_E_CONSOLE_GUEST_OPERATIONS_PROHIBITED    = 3026;
	public final static int VIX_E_MUST_BE_CONSOLE_USER                   = 3027;

	   /* VM Errors */ 
	public final static int VIX_E_VM_NOT_FOUND                           = 4000;
	public final static int VIX_E_NOT_SUPPORTED_FOR_VM_VERSION           = 4001;
	public final static int VIX_E_CANNOT_READ_VM_CONFIG                  = 4002;
	public final static int VIX_E_TEMPLATE_VM                            = 4003;
	public final static int VIX_E_VM_ALREADY_LOADED                      = 4004;
	public final static int VIX_E_VM_ALREADY_UP_TO_DATE                  = 4006;
 
	   /* Property Errors */
	public final static int VIX_E_UNRECOGNIZED_PROPERTY                  = 6000;
	public final static int VIX_E_INVALID_PROPERTY_VALUE                 = 6001;
	public final static int VIX_E_READ_ONLY_PROPERTY                     = 6002;
	public final static int VIX_E_MISSING_REQUIRED_PROPERTY              = 6003;

	   /* Completion Errors */
	public final static int VIX_E_BAD_VM_INDEX                           = 8000;

	   /* Snapshot errors */
	public final static int VIX_E_SNAPSHOT_INVAL                         = 13000;
	public final static int VIX_E_SNAPSHOT_DUMPER                        = 13001;
	public final static int VIX_E_SNAPSHOT_DISKLIB                       = 13002;
	public final static int VIX_E_SNAPSHOT_NOTFOUND                      = 13003;
	public final static int VIX_E_SNAPSHOT_EXISTS                        = 13004;
	public final static int VIX_E_SNAPSHOT_VERSION                       = 13005;
	public final static int VIX_E_SNAPSHOT_NOPERM                        = 13006;
	public final static int VIX_E_SNAPSHOT_CONFIG                        = 13007;
	public final static int VIX_E_SNAPSHOT_NOCHANGE                      = 13008;
	public final static int VIX_E_SNAPSHOT_CHECKPOINT                    = 13009;
	public final static int VIX_E_SNAPSHOT_LOCKED                        = 13010;
	public final static int VIX_E_SNAPSHOT_INCONSISTENT                  = 13011;
	public final static int VIX_E_SNAPSHOT_NAMETOOLONG                   = 13012;
	public final static int VIX_E_SNAPSHOT_VIXFILE                       = 13013;
	public final static int VIX_E_SNAPSHOT_DISKLOCKED                    = 13014;
	public final static int VIX_E_SNAPSHOT_DUPLICATEDDISK                = 13015;
	public final static int VIX_E_SNAPSHOT_INDEPENDENTDISK               = 13016;
	public final static int VIX_E_SNAPSHOT_NONUNIQUE_NAME                = 13017;

	/* Guest Errors */
	public final static int VIX_E_NOT_A_FILE                             = 20001;
	public final static int VIX_E_NOT_A_DIRECTORY                        = 20002;
	public final static int VIX_E_NO_SUCH_PROCESS                        = 20003;
	public final static int VIX_E_FILE_NAME_TOO_LONG                     = 20004;


    /* JNI Errors (invented by me) */
	public final static int VIX_E_JNI_TOO_MANY_PROPERTIES                = 30001;
	public final static int VIX_E_JNI_CANNOT_RETURN_UNKNOWN_PROPERTYTYPE = 30002;
	public final static int VIX_E_JNI_INVALID_HANDLE_POSSIBLE_SERIALNUMBER_EXPIRY = 30003;

	/** Create a new VIX Exception */
	public VixException(int errorCode) {
		super("VIX Exception occurred; errorCode=" + (errorCode & 0xFFFF) + 
		  " (" + errorCodeToString(errorCode & 0xFFFF) + ")");
		this.errorCode = errorCode & 0xFFFF;
	}
	
	/** Convert a VIX_E_* constant into a string (or the value "unknown" if the
	 * supplied parameter is not a VIX_E_* constant) 
	 * 
	 * @param errorCode code to convert to string
	 * 
	 * @return string representation of this errorCode
	 */
	public static String errorCodeToString(int errorCode) {
		String result = (String) errorConstants.get(new Integer(errorCode));
		if (result==null) { result = "unknown"; }
		return result;
	}
	
	/** Return the VIX_E_* constant describing the type of exception that has occurred
	 * 
	 * @return a VIX_E_* constant
	 */ 
	public int getErrorCode() {
		return errorCode;
	}
	
	static {
		Map tempMap = Util.getConstantsMap(VixException.class, "VIX_E_");
		errorConstants = new HashMap();
		// reverse map for this class
		for (Iterator i = tempMap.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry entry = (Map.Entry) i.next();
			errorConstants.put(entry.getValue(), entry.getKey());
		}
		 
	}
	
	
}
