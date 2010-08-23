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
	public final static int VIX_E_VMX_MSG_DIALOG_AND_NO_UI               = 3028;
	public final static int VIX_E_NOT_ALLOWED_DURING_VM_RECORDING        = 3029;
	public final static int VIX_E_NOT_ALLOWED_DURING_VM_REPLAY           = 3030;
	public final static int VIX_E_OPERATION_NOT_ALLOWED_FOR_LOGIN_TYPE   = 3031;
	public final static int VIX_E_LOGIN_TYPE_NOT_SUPPORTED               = 3032;
	public final static int VIX_E_EMPTY_PASSWORD_NOT_ALLOWED_IN_GUEST    = 3033;
	public final static int VIX_E_INTERACTIVE_SESSION_NOT_PRESENT        = 3034;
	public final static int VIX_E_INTERACTIVE_SESSION_USER_MISMATCH      = 3035;
	public final static int VIX_E_UNABLE_TO_REPLAY_VM                    = 3039;
	public final static int VIX_E_CANNOT_POWER_ON_VM                     = 3041;
	public final static int VIX_E_NO_DISPLAY_SERVER                      = 3043;
	public final static int VIX_E_VM_NOT_RECORDING                       = 3044;
	public final static int VIX_E_VM_NOT_REPLAYING                       = 3045;


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
	public final static int VIX_E_INVALID_SERIALIZED_DATA                = 6004;
	public final static int VIX_E_PROPERTY_TYPE_MISMATCH                 = 6005;
	   /* Completion Errors */
	public final static int VIX_E_BAD_VM_INDEX                           = 8000;

	   /* Message errors */
	public final static int VIX_E_INVALID_MESSAGE_HEADER                 = 10000;
	public final static int VIX_E_INVALID_MESSAGE_BODY                   = 10001;

	
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
	public final static int VIX_E_SNAPSHOT_MEMORY_ON_INDEPENDENT_DISK    = 13018;
	public final static int VIX_E_SNAPSHOT_MAXSNAPSHOTS                  = 13019;
	public final static int VIX_E_SNAPSHOT_MIN_FREE_SPACE                = 13020;
	public final static int VIX_E_SNAPSHOT_RRSUSPEND                     = 13021;

	   /* Host Errors */
	public final static int VIX_E_HOST_DISK_INVALID_VALUE                = 14003;
	public final static int VIX_E_HOST_DISK_SECTORSIZE                   = 14004;
	public final static int VIX_E_HOST_FILE_ERROR_EOF                    = 14005;
	public final static int VIX_E_HOST_NETBLKDEV_HANDSHAKE               = 14006;
	public final static int VIX_E_HOST_SOCKET_CREATION_ERROR             = 14007;
	public final static int VIX_E_HOST_SERVER_NOT_FOUND                  = 14008;
	public final static int VIX_E_HOST_NETWORK_CONN_REFUSED              = 14009;
	public final static int VIX_E_HOST_TCP_SOCKET_ERROR                  = 14010;
	public final static int VIX_E_HOST_TCP_CONN_LOST                     = 14011;
	public final static int VIX_E_HOST_NBD_HASHFILE_VOLUME               = 14012;
	public final static int VIX_E_HOST_NBD_HASHFILE_INIT                 = 14013;
	   
	   /* Disklib errors */
	public final static int VIX_E_DISK_INVAL                             = 16000;
	public final static int VIX_E_DISK_NOINIT                            = 16001;
	public final static int VIX_E_DISK_NOIO                              = 16002;
	public final static int VIX_E_DISK_PARTIALCHAIN                      = 16003;
	public final static int VIX_E_DISK_NEEDSREPAIR                       = 16006;
	public final static int VIX_E_DISK_OUTOFRANGE                        = 16007;
	public final static int VIX_E_DISK_CID_MISMATCH                      = 16008;
	public final static int VIX_E_DISK_CANTSHRINK                        = 16009;
	public final static int VIX_E_DISK_PARTMISMATCH                      = 16010;
	public final static int VIX_E_DISK_UNSUPPORTEDDISKVERSION            = 16011;
	public final static int VIX_E_DISK_OPENPARENT                        = 16012;
	public final static int VIX_E_DISK_NOTSUPPORTED                      = 16013;
	public final static int VIX_E_DISK_NEEDKEY                           = 16014;
	public final static int VIX_E_DISK_NOKEYOVERRIDE                     = 16015;
	public final static int VIX_E_DISK_NOTENCRYPTED                      = 16016;
	public final static int VIX_E_DISK_NOKEY                             = 16017;
	public final static int VIX_E_DISK_INVALIDPARTITIONTABLE             = 16018;
	public final static int VIX_E_DISK_NOTNORMAL                         = 16019;
	public final static int VIX_E_DISK_NOTENCDESC                        = 16020;
	public final static int VIX_E_DISK_NEEDVMFS                          = 16022;
	public final static int VIX_E_DISK_RAWTOOBIG                         = 16024;
	public final static int VIX_E_DISK_TOOMANYOPENFILES                  = 16027;
	public final static int VIX_E_DISK_TOOMANYREDO                       = 16028;
	public final static int VIX_E_DISK_RAWTOOSMALL                       = 16029;
	public final static int VIX_E_DISK_INVALIDCHAIN                      = 16030;
	public final static int VIX_E_DISK_KEY_NOTFOUND                      = 16052; // metadata key is not found
	public final static int VIX_E_DISK_SUBSYSTEM_INIT_FAIL               = 16053;
	public final static int VIX_E_DISK_INVALID_CONNECTION                = 16054;
	public final static int VIX_E_DISK_ENCODING                          = 16061;
	public final static int VIX_E_DISK_CANTREPAIR                        = 16062;
	public final static int VIX_E_DISK_INVALIDDISK                       = 16063;
	public final static int VIX_E_DISK_NOLICENSE                         = 16064;
	public final static int VIX_E_DISK_NODEVICE                          = 16065;
	public final static int VIX_E_DISK_UNSUPPORTEDDEVICE                 = 16066;

	   /* Crypto Library Errors */
	public final static int VIX_E_CRYPTO_UNKNOWN_ALGORITHM               = 17000;
	public final static int VIX_E_CRYPTO_BAD_BUFFER_SIZE                 = 17001;
	public final static int VIX_E_CRYPTO_INVALID_OPERATION               = 17002;
	public final static int VIX_E_CRYPTO_RANDOM_DEVICE                   = 17003;
	public final static int VIX_E_CRYPTO_NEED_PASSWORD                   = 17004;
	public final static int VIX_E_CRYPTO_BAD_PASSWORD                    = 17005;
	public final static int VIX_E_CRYPTO_NOT_IN_DICTIONARY               = 17006;
	public final static int VIX_E_CRYPTO_NO_CRYPTO                       = 17007;
	public final static int VIX_E_CRYPTO_ERROR                           = 17008;
	public final static int VIX_E_CRYPTO_BAD_FORMAT                      = 17009;
	public final static int VIX_E_CRYPTO_LOCKED                          = 17010;
	public final static int VIX_E_CRYPTO_EMPTY                           = 17011;
	public final static int VIX_E_CRYPTO_KEYSAFE_LOCATOR                 = 17012;

	   /* Remoting Errors. */
	public final static int VIX_E_CANNOT_CONNECT_TO_HOST                 = 18000;
	public final static int VIX_E_NOT_FOR_REMOTE_HOST                    = 18001;
	public final static int VIX_E_INVALID_HOSTNAME_SPECIFICATION         = 18002;
	    
	/* Screen Capture Errors. */
	public final static int VIX_E_SCREEN_CAPTURE_ERROR                   = 19000;
	public final static int VIX_E_SCREEN_CAPTURE_BAD_FORMAT              = 19001;
	public final static int VIX_E_SCREEN_CAPTURE_COMPRESSION_FAIL        = 19002;
	public final static int VIX_E_SCREEN_CAPTURE_LARGE_DATA              = 19003;
	
	
	/* Guest Errors */
	public final static int VIX_E_NOT_A_FILE                             = 20001;
	public final static int VIX_E_NOT_A_DIRECTORY                        = 20002;
	public final static int VIX_E_NO_SUCH_PROCESS                        = 20003;
	public final static int VIX_E_FILE_NAME_TOO_LONG                     = 20004;

	   /* Tools install errors */
	public final static int VIX_E_TOOLS_INSTALL_NO_IMAGE                 = 21000;
	public final static int VIX_E_TOOLS_INSTALL_IMAGE_INACCESIBLE        = 21001;
	public final static int VIX_E_TOOLS_INSTALL_NO_DEVICE                = 21002;
	public final static int VIX_E_TOOLS_INSTALL_DEVICE_NOT_CONNECTED     = 21003;
	public final static int VIX_E_TOOLS_INSTALL_CANCELLED                = 21004;
	public final static int VIX_E_TOOLS_INSTALL_INIT_FAILED              = 21005;
	public final static int VIX_E_TOOLS_INSTALL_AUTO_NOT_SUPPORTED       = 21006;
	public final static int VIX_E_TOOLS_INSTALL_GUEST_NOT_READY          = 21007;
	public final static int VIX_E_TOOLS_INSTALL_SIG_CHECK_FAILED         = 21008;
	public final static int VIX_E_TOOLS_INSTALL_ERROR                    = 21009;
	public final static int VIX_E_TOOLS_INSTALL_ALREADY_UP_TO_DATE       = 21010;
	public final static int VIX_E_TOOLS_INSTALL_IN_PROGRESS              = 21011;

	   /* Wrapper Errors */
	public final static int VIX_E_WRAPPER_WORKSTATION_NOT_INSTALLED      = 22001;
	public final static int VIX_E_WRAPPER_VERSION_NOT_FOUND              = 22002;
	public final static int VIX_E_WRAPPER_SERVICEPROVIDER_NOT_FOUND      = 22003;
	public final static int VIX_E_WRAPPER_PLAYER_NOT_INSTALLED           = 22004;

	
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
