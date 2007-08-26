package net.sf.jvix.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import net.sf.jvix.VixException;
import net.sf.jvix.VixHost;
import net.sf.jvix.VixSnapshot;
import net.sf.jvix.VixVM;
import net.sf.jvix.VixWrapper;
import net.sf.jvix.data.VixFile;
import net.sf.jvix.data.VixProcess;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * JVIX Java object API test.
 * 
 * @author knoxg
 * @version $Id$
 */
public class TestVix {

	/** Revision to use in stack traces */
	public static String _revision = "$Id$";
	
	public static void main(String[] args) throws VixException, InterruptedException, IOException {

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
		
		VixHost     vixHost = null;
		VixVM       vixVM = null;
		VixSnapshot vixRootSnapshot = null;
		VixSnapshot vixChildSnapshot = null;
		try {
			// retieve VM
			vixHost = new VixHost(VixWrapper.VIX_API_VERSION,
			  VixWrapper.VIX_SERVICEPROVIDER_VMWARE_WORKSTATION, 
			  vmHost, vmHostPort, 
			  "presumablyIgnoredUsername", "presumablyIgnoredPassword" );
			vixVM = vixHost.open(vmLocation);
			
			// snapshot management (only 1 level deep)
			System.out.println("== start snapshot listing");
			int numRootSnapshots = vixVM.getNumRootSnapshots();
			for (int i=0; i<numRootSnapshots; i++) {
				System.out.println("Found root snapshot " + i);
				vixRootSnapshot = vixVM.getRootSnapshot(i);
				int numChildSnapshots = vixRootSnapshot.getNumChildren();
				for (int j=0; j<numChildSnapshots; j++) {
					System.out.println("  Found child snapshot " + j);
					vixChildSnapshot = vixRootSnapshot.getChild(j);
					// verify parent handle is correct
					if (!vixChildSnapshot.getParent().getVixHandle().equals(
						vixRootSnapshot.getVixHandle())) 
					{
						System.err.println("Inconsistent snapshots");
					}
					vixChildSnapshot.close();
				}
				vixRootSnapshot.close();
			}
			System.out.println("== end snapshot listing");
			
			// poweron / login
			vixVM.powerOn(VixWrapper.VIX_VMPOWEROP_LAUNCH_GUI);
			vixVM.waitForToolsInGuest(300);
			vixVM.loginInGuest("knoxg", "abc123");
			
			// file operations
			// -- clear up old files first
			if (vixVM.fileExistsInGuest("c:\\vix-guest-write.txt")) {
				vixVM.deleteFileInGuest("c:\\vix-guest-write.txt");
				if (vixVM.fileExistsInGuest("c:\\vix-guest-write.txt")) {
					System.out.println("Deletion of file failed");
				}
			}
			if (vixVM.fileExistsInGuest("c:\\vix-guest-rename.txt")) {
				vixVM.deleteFileInGuest("c:\\vix-guest-rename.txt");
				if (vixVM.fileExistsInGuest("c:\\vix-guest-rename.txt")) {
					System.out.println("Deletion of file failed");
				}
			}

			File hostFile1 = new File("c:\\vix-host-source.txt");
			File hostFile2 = new File("c:\\vix-host-dest.txt");
			if (hostFile1.exists()) { hostFile1.delete(); }
			if (hostFile2.exists()) { hostFile2.delete(); }

			// perform a copy to the guest VM
			PrintWriter pw = new PrintWriter(new FileOutputStream(hostFile1));
			pw.println("This is a text file");
			pw.println("Used for testing file transfers");
			pw.close();
			vixVM.copyFileFromHostToGuest("c:\\vix-host-source.txt", "c:\\vix-guest-write.txt");
			
			// and a copy to the host
			if (!vixVM.fileExistsInGuest("c:\\vix-guest-write.txt")) {
				System.out.println("Copy file to guest failed");
			} else {
				vixVM.copyFileFromGuestToHost("c:\\vix-guest-write.txt", "c:\\vix-host-dest.txt" );
				if (!hostFile2.exists()) {
					System.out.println("Copy file to host failed");
				} else {
					hostFile2.delete();
				}
				vixVM.renameFileInGuest("c:\\vix-guest-write.txt", "c:\\vix-guest-rename.txt");
				if (vixVM.fileExistsInGuest("c:\\vix-guest-write.txt") ||
					!vixVM.fileExistsInGuest("c:\\vix-guest-rename.txt")) {
					System.out.println("Guest file rename failed");
				} else {
					vixVM.deleteFileInGuest("c:\\vix-guest-rename.txt");
					if (vixVM.fileExistsInGuest("c:\\vix-guest-rename.txt")) {
						System.out.println("Guest file delete failed");
					}
				}
			}
			hostFile1.delete();
			
			// test for directory existence
			if (!vixVM.directoryExistsInGuest("C:\\Windows")) {
				System.out.println("Guest directory exists test failed");
			}
			if (vixVM.directoryExistsInGuest("C:\\slkdjksdj")) {
				System.out.println("Guest directory non-existence test failed");
			}
			
			// directory list
			List directoryList = vixVM.listDirectoryInGuest("C:\\");
			System.out.println("== start directory listing");
			for (Iterator i = directoryList.iterator(); i.hasNext(); ) {
				VixFile vixFile = (VixFile) i.next();
				System.out.println("  " +
				  "name='" + vixFile.getName() + "', " +
				  "flags=" + vixFile.getFileFlags()); 
			}
			System.out.println("== end directory listing");

			// process list
			List processList = vixVM.listProcessesInGuest();
			System.out.println("== start process listing");
			for (Iterator i = processList.iterator(); i.hasNext(); ) {
				VixProcess vixProcess = (VixProcess) i.next();
				System.out.println("  " +
				  "name='" + vixProcess.getName() + "', " +
				  "pid=" + vixProcess.getPid() + ", " +
				  "owner='" + vixProcess.getOwner() + "', " +
				  "command='" + vixProcess.getCommand() + "'"); 
			}
			System.out.println("== end process listing");

			
			vixVM.logoutFromGuest();
			
			// vixVM.powerOff();
		} finally {
			if (vixVM!=null) { vixVM.close(); }	
			if (vixHost!=null) { vixHost.close(); }
		}
	}
}
