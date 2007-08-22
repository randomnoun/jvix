package net.sf.jvix.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import net.sf.jvix.VixException;
import net.sf.jvix.VixHost;
import net.sf.jvix.VixVM;
import net.sf.jvix.VixWrapper;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * JVIX Java object API test
 * 
 * @author knoxg
 * @version $Id$
 */
public class TestVix {

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
		
		VixHost vixHost = null;
		VixVM   vixVM = null;
		try {
			vixHost = new VixHost(VixWrapper.VIX_API_VERSION,
			  VixWrapper.VIX_SERVICEPROVIDER_VMWARE_WORKSTATION, 
			  vmHost, vmHostPort, 
			  "presumablyIgnoredUsername", "presumablyIgnoredPassword" );
			vixVM = vixHost.open(vmLocation);
			vixVM.powerOn(VixWrapper.VIX_VMPOWEROP_LAUNCH_GUI);
			vixVM.waitForToolsInGuest(300);
			vixVM.loginInGuest("knoxg", "abc123");
			vixVM.powerOff();
		} finally {
			if (vixVM!=null) { vixVM.close(); }	
			if (vixHost!=null) { vixHost.close(); }
		}
	}
}
