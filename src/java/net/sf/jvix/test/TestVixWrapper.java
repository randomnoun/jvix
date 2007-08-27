package net.sf.jvix.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import net.sf.jvix.VixException;
import net.sf.jvix.VixHandle;
import net.sf.jvix.VixWrapper;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * JVIX Wrapper API test.
 * 
 * @author knoxg
 * @version $Id$
 */
public class TestVixWrapper {

	/** Revision to use in stack traces */
	public static String _revision = "$Id$";
	
	public static void main(String[] args) throws VixException, InterruptedException {

		// setup log4j (seems to create first file though... arg!)
		Properties props = new Properties();
		props.put("log4j.rootLogger", "DEBUG, TEST");
		
		props.put("log4j.appender.TEST", "org.apache.log4j.ConsoleAppender");
		props.put("log4j.appender.TEST.layout", "org.apache.log4j.PatternLayout");
		props.put("log4j.appender.TEST.layout.ConversionPattern", "=%d{dd/MM/yy HH:mm:ss.SSS} %p %c{1} - %m%n");
		PropertyConfigurator.configure(props);
		
		Logger logger = Logger.getLogger("net.sf.jvix.VixWrapper");
		logger.debug("VixWrapper debug logger enabled");

		String vmLocation = "C:\\Documents and Settings\\knoxg\\My Documents\\My Virtual Machines\\test01\\Windows XP Professional.vmx";
		String vmHost = "127.0.0.1";
		int vmHostPort = 0;
		
		//String vmLocation = "/home/knoxg/vmware/000 Clean WinXP/000 Clean WinXP.vmx";
		//String vmHost = "monk";
		//int vmHostPort = 0;  // 902 = default
		
		/* only for local VMs
		if (!(new File(vmLocation).exists())) {
			throw new IllegalStateException("VM " + vmLocation + " should exist");
		} 
		*/
		
		// @TODO should use try/catch/finally's to release any handles before quitting
		
		List jobWaitProperties;
		List result;
		VixHandle jobHandle = VixWrapper.VixHost_Connect(
		  VixWrapper.VIX_API_VERSION,
		  VixWrapper.VIX_SERVICEPROVIDER_VMWARE_WORKSTATION,
		  vmHost,
		  vmHostPort,
		  "presumablyIgnoredUsername", // VMs are running on local machine
		  "presumablyIgnoredPassword",
		  VixWrapper.VIX_VMPOWEROP_NORMAL,
		  VixHandle.VIX_INVALID_HANDLE,
		  null, null);
		  
		// properties to retrieve from VixJob_Wait call
		// (maybe it would be cleaner to pass these in as an int[] )
		jobWaitProperties = new ArrayList();
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_HANDLE)); 
		result = VixWrapper.VixJob_Wait(jobHandle, jobWaitProperties);
		VixWrapper.Vix_ReleaseHandle(jobHandle);
		VixHandle hostHandle = (VixHandle) result.get(0);

		jobHandle = VixWrapper.VixVM_Open(
		  hostHandle,
		  vmLocation,
		  null, null);
		jobWaitProperties = new ArrayList();
		jobWaitProperties.add(new Integer(VixWrapper.VIX_PROPERTY_JOB_RESULT_HANDLE)); 
		result = VixWrapper.VixJob_Wait(jobHandle, jobWaitProperties);
		VixWrapper.Vix_ReleaseHandle(jobHandle);
		VixHandle vmHandle = (VixHandle) result.get(0);
	
		jobHandle = VixWrapper.VixVM_PowerOn(vmHandle,
		  VixWrapper.VIX_VMPOWEROP_LAUNCH_GUI,
		  VixHandle.VIX_INVALID_HANDLE,
		  null, null);		  	
		result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		VixWrapper.Vix_ReleaseHandle(jobHandle);

		// wait for tools to load; 300secs seems to be standard
		jobHandle = VixWrapper.VixVM_WaitForToolsInGuest(vmHandle, 300, null, null);
		result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		VixWrapper.Vix_ReleaseHandle(jobHandle);		 
	
		jobHandle = VixWrapper.VixVM_LoginInGuest(vmHandle, "knoxg", "abc123", 0, null, null);
		try {
			result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		} catch (VixException ve) {
			if (ve.getErrorCode() == VixException.VIX_E_GUEST_USER_PERMISSIONS) {
				System.out.println("Invalid login credentials");
				// @TODO clean up other handles created in this method
				return;
			}
		} finally {
			VixWrapper.Vix_ReleaseHandle(jobHandle);	
		}
		
		jobHandle = VixWrapper.VixVM_PowerOff(vmHandle,
		  VixWrapper.VIX_VMPOWEROP_NORMAL,
		  null, null);
		result = VixWrapper.VixJob_Wait(jobHandle, Collections.EMPTY_LIST);
		VixWrapper.Vix_ReleaseHandle(jobHandle);
		
	}
}
