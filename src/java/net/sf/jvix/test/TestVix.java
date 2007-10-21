package net.sf.jvix.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import net.sf.jvix.VixException;
import net.sf.jvix.VixHost;
import net.sf.jvix.VixSnapshot;
import net.sf.jvix.VixVM;
import net.sf.jvix.VixWrapper;
import net.sf.jvix.data.VixFile;
import net.sf.jvix.data.VixProcess;
import net.sf.jvix.data.VixSharedFolderState;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * JVIX Java object API test. This class exercises most of the jvix methods in 
 * the OO interface (and therefore will also test the underlying JNI code).  
 * 
 * @author knoxg
 * @version $Id$
 */
public class TestVix extends TestCase {

	/** Revision to use in stack traces */
	public static String _revision = "$Id$";

	/** Test vmware machine */
	public static final String VM_LOCATION = "C:\\Documents and Settings\\knoxg\\My Documents\\My Virtual Machines\\test01\\Windows XP Professional.vmx";
	
	/** Default host that this vmware machine resides on */
	public static final String VM_HOST = "127.0.0.1";
	
	/** Default login for privileged tasks in this vmware machine */
	public static final String VM_LOGIN_USERNAME = "knoxg";
	
	/** Default password for privileged tasks in this vmware machine */
	public static final String VM_LOGIN_PASSWORD = "abc123";

	// set defaults
	/** Actual VM location used in tests */
	String vmLocation = VM_LOCATION;
	
	/** Actual host name used in tests */
	String vmHostName = VM_HOST;
	
	/** Actual port number used in tests */
	int vmHostPort = 0;
	
	/** Login for privileged tasks in test vmware machine */
	String vmLoginUsername = VM_LOGIN_USERNAME;
	
	/** Password for privileged tasks in test vmware machine */
	String vmLoginPassword = VM_LOGIN_PASSWORD;
	
	
	/** Create a new TestVix class */
	public TestVix(String name)
    {
        super(name);
    }
	
	/** Perform test setup actions */
	protected void setUp() throws IOException {
		
		// setup log4j
		Properties log4jProps = new Properties();
		log4jProps.put("log4j.rootLogger", "DEBUG, TEST");
		log4jProps.put("log4j.appender.TEST", "org.apache.log4j.ConsoleAppender");
		log4jProps.put("log4j.appender.TEST.layout", "org.apache.log4j.PatternLayout");
		log4jProps.put("log4j.appender.TEST.layout.ConversionPattern", "%d{dd/MM/yy HH:mm:ss.SSS} %5p %c{1} - %m%n");
		PropertyConfigurator.configure(log4jProps);

		Logger logger = Logger.getLogger("net.sf.jvix.VixWrapper");
		logger.debug("VixWrapper debug logger enabled");

		// load properties for this host/username
		String machineName;
		try {
			machineName = InetAddress.getLocalHost().getHostName();
		} catch(Exception ex) {
			machineName = "localhost";
		}
		String username = System.getProperty("user.name");
		String propsFile = "resources/properties/" + machineName + "-" + username + ".properties"; 
		logger.info("Loading properties from '" + propsFile + "'");
		InputStream is = TestVix.class.getClassLoader().getResourceAsStream(propsFile);
		if (is==null) {
			logger.info("No properties file found; using defaults");
		} else {
			Properties props = new Properties();
			props.load(is);
			String p = props.getProperty("test.vmHostName"); if (p!=null) { vmHostName = p; }
			p = props.getProperty("test.vmHostPort"); if (p!=null) { vmHostPort = Integer.parseInt(p); }
			p = props.getProperty("test.vmLocation"); if (p!=null) { vmLocation = p; }
			p = props.getProperty("test.vmLoginUsername"); if (p!=null) { vmLoginUsername = p; }
			p = props.getProperty("test.vmLoginPassword"); if (p!=null) { vmLoginPassword = p; }
		}
		
		//String vmLocation = "/home/knoxg/vmware/000 Clean WinXP/000 Clean WinXP.vmx";
		//String vmHost = "monk";
		//int vmHostPort = 0;  // 902 = default
		
		System.out.println("TextVix started");
		System.out.println("VM Hostname: " + vmHostName);
		System.out.println("VM Hostport: " + vmHostPort);
		System.out.println("VM Location: " + vmLocation);
		System.out.println("VM Login username: " + vmLoginUsername);
		System.out.println("VM Login password: " + vmLoginPassword);
	}

	/** Return a new VixHost object for use in a test */
	private VixHost getVixHost() throws VixException {
		return new VixHost(VixWrapper.VIX_API_VERSION,
		  VixWrapper.VIX_SERVICEPROVIDER_VMWARE_WORKSTATION, 
		  vmHostName, vmHostPort, 
		  "presumablyIgnoredUsername", "presumablyIgnoredPassword" );		
	}
	
	/** Return a new VixVM object for use in a test */
	private VixVM getVixVM(VixHost vixHost) throws VixException {
		return vixHost.open(vmLocation);
	}

	/** Define a test suits to ensure the order of the tests */
	public static Test suite() {
	    TestSuite testsToRun = new TestSuite();
	    testsToRun.addTest(new TestVix("testFindVMs"));
	    testsToRun.addTest(new TestVix("testFindSnapshots"));
	    testsToRun.addTest(new TestVix("testStartVM"));
	    testsToRun.addTest(new TestVix("testFileOperations"));
	    testsToRun.addTest(new TestVix("testDirectoryOperations"));
	    testsToRun.addTest(new TestVix("testTempFileOperations"));
	    testsToRun.addTest(new TestVix("testListDirectories"));
	    testsToRun.addTest(new TestVix("testListProcesses"));
	    testsToRun.addTest(new TestVix("testOpenUrl"));
	    testsToRun.addTest(new TestVix("testRunProcess"));
	    testsToRun.addTest(new TestVix("testPowerOperations"));
	    return testsToRun;
	}
	
	public void testFindVMs() throws VixException {
		VixHost vixHost = null;
		
		try {
			vixHost = getVixHost();
			List vms = vixHost.findItems(VixWrapper.VIX_FIND_REGISTERED_VMS);
			System.out.println("== start registered VMs listing");
			for (Iterator i = vms.iterator(); i.hasNext(); ) {
				Object obj = i.next();
				System.out.println("  (" + obj.getClass().getName() + ") " + obj.toString());
			}
			System.out.println("== end registered VMs listing");
			
			vms = vixHost.findItems(VixWrapper.VIX_FIND_RUNNING_VMS);
			System.out.println("== start running VMs listing");
			for (Iterator i = vms.iterator(); i.hasNext(); ) {
				Object obj = i.next();
				System.out.println("  (" + obj.getClass().getName() + ") " + obj.toString());
			}
			System.out.println("== end running VMs listing");
		} finally {
			if (vixHost!=null) { vixHost.close(); }
		}
	}
	
	public void testFindSnapshots() throws VixException {
		VixHost vixHost = null;
		VixVM vixVM = null;

		try {
			vixHost = getVixHost();
			vixVM = getVixVM(vixHost);

			VixSnapshot vixRootSnapshot = null;
			VixSnapshot vixChildSnapshot = null;
			
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
		} finally {
			if (vixVM!=null) { vixVM.close(); }	
			if (vixHost!=null) { vixHost.close(); }
		}
	}
	
	public void testStartVM() throws VixException {
		VixHost vixHost = null;
		VixVM vixVM = null;

		try {
			vixHost = getVixHost();
			vixVM = getVixVM(vixHost);

			// poweron / login
			vixVM.powerOn(VixWrapper.VIX_VMPOWEROP_LAUNCH_GUI);
			vixVM.waitForToolsInGuest(300);
			vixVM.loginInGuest(vmLoginUsername, vmLoginPassword);
		} finally {
			if (vixVM!=null) { vixVM.close(); }	
			if (vixHost!=null) { vixHost.close(); }
		}
	}
	
	public void testFileOperations() throws VixException, IOException {
		VixHost vixHost = getVixHost();
		VixVM vixVM = getVixVM(vixHost);
		
		try {
			vixHost = getVixHost();
			vixVM = getVixVM(vixHost);
			vixVM.loginInGuest(vmLoginUsername, vmLoginPassword);

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
			if (vixVM.directoryExistsInGuest("c:\\vix-directory")) {
				vixVM.deleteDirectoryInGuest("c:\\vix-directory");
				if (vixVM.directoryExistsInGuest("c:\\vix-directory")) {
					System.out.println("Deletion of directory failed");
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
		} finally {
			if (vixVM!=null) { vixVM.close(); }	
			if (vixHost!=null) { vixHost.close(); }
		}

	}
	
	public void testDirectoryOperations() throws VixException {
		VixHost vixHost = null;
		VixVM vixVM = null;

		try {
			vixHost = getVixHost();
			vixVM = getVixVM(vixHost);
			vixVM.loginInGuest(vmLoginUsername, vmLoginPassword);

			// test for directory existence
			if (!vixVM.directoryExistsInGuest("C:\\Windows")) {
				System.out.println("Guest directory exists test failed");
			}
			if (vixVM.directoryExistsInGuest("C:\\slkdjksdj")) {
				System.out.println("Guest directory non-existence test failed");
			}
	
			vixVM.createDirectoryInGuest("c:\\vix-directory");
			if (!vixVM.directoryExistsInGuest("c:\\vix-directory")) {
				System.out.println("Create directory in guest failed");
			} else {
				vixVM.deleteDirectoryInGuest("c:\\vix-directory");
				if (vixVM.directoryExistsInGuest("c:\\vix-directory")) {
					System.out.println("Deletion of directory failed");
				}
			}
		} finally {
			if (vixVM!=null) { vixVM.close(); }	
			if (vixHost!=null) { vixHost.close(); }
		}
	}
	
	public void testTempFileOperations() throws VixException {
		VixHost vixHost = null;
		VixVM vixVM = null;

		try {
			vixHost = getVixHost();
			vixVM = getVixVM(vixHost);
			vixVM.loginInGuest(vmLoginUsername, vmLoginPassword);

			String tempFilename = vixVM.createTempFileInGuest();
			System.out.println("Created temp file '" + tempFilename + "'");
			if (!vixVM.fileExistsInGuest(tempFilename)) {
				System.out.println("Temp file not found");
			} else {
				vixVM.deleteFileInGuest(tempFilename);
				if (vixVM.fileExistsInGuest(tempFilename)) {
					System.out.println("Deletion of temp file failed");
				}
			}
		} finally {
			if (vixVM!=null) { vixVM.close(); }	
			if (vixHost!=null) { vixHost.close(); }
		}
	}
	
	public void testSharedFolders() throws VixException {
		VixHost vixHost = null;
		VixVM vixVM = null;

		try {
			vixHost = getVixHost();
			vixVM = getVixVM(vixHost);

			int numSharedFolders = vixVM.getNumSharedFolders();
			System.out.println("Number of shared folders: " + numSharedFolders);
			System.out.println("== start shared folder listing");
			boolean removeVixShare = false;
			for (int i=0; i<numSharedFolders; i++) {
				VixSharedFolderState vixFolderState = vixVM.getSharedFolderState(i);
				System.out.println("  " +
				  "name='" + vixFolderState.getName() + "', " +
				  "host='" + vixFolderState.getHost() + "', " +
				  "flags=" + vixFolderState.getFlags());
				if (vixFolderState.getName().equals("VixShare")) {
					removeVixShare = true;
				}
			}
			System.out.println("== end shared folder listing");
	
			if (removeVixShare) {
				System.out.println("Removing share 'VixShare'");
				vixVM.removeSharedFolder("VixShare");
				numSharedFolders = vixVM.getNumSharedFolders();
				for (int i=0; i<numSharedFolders; i++) {
					VixSharedFolderState vixFolderState = vixVM.getSharedFolderState(i);
					if (vixFolderState.getName().equals("VixShare")) {
						System.out.println("Remove share failed");
					}
				}
			}
			
			// shared folders; may need reboot to take effect?
			File localShare = new File("c:\\vix-shared-folder");
			localShare.mkdir();
			vixVM.enableSharedFolders(true);
			vixVM.addSharedFolder("VixShare", "c:\\vix-shared-folder", VixWrapper.VIX_SHAREDFOLDER_WRITE_ACCESS);
		} finally {
			if (vixVM!=null) { vixVM.close(); }	
			if (vixHost!=null) { vixHost.close(); }
		}
	}
	
	public void testListDirectories() throws VixException {
		VixHost vixHost = null;
		VixVM vixVM = null;

		try {
			vixHost = getVixHost();
			vixVM = getVixVM(vixHost);
			vixVM.loginInGuest(vmLoginUsername, vmLoginPassword);

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
		} finally {
			if (vixVM!=null) { vixVM.close(); }	
			if (vixHost!=null) { vixHost.close(); }
		}
	}
	
	public void testListProcesses() throws VixException {
		VixHost vixHost = null;
		VixVM vixVM = null;

		try {
			vixHost = getVixHost();
			vixVM = getVixVM(vixHost);
			vixVM.loginInGuest(vmLoginUsername, vmLoginPassword);

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
		} finally {
			if (vixVM!=null) { vixVM.close(); }	
			if (vixHost!=null) { vixHost.close(); }
		}
	}
	
	public void testOpenUrl() throws VixException {
		VixHost vixHost = null;
		VixVM vixVM = null;

		try {
			vixHost = getVixHost();
			vixVM = getVixVM(vixHost);
		
			// need console for web browsing & console processes
			vixVM.loginInGuest(VixWrapper.VIX_CONSOLE_USER_NAME, null);
			
			// web browsing
			vixVM.openUrlInGuest("http://www.google.com");
		} finally {
			if (vixVM!=null) { vixVM.close(); }	
			if (vixHost!=null) { vixHost.close(); }
		}
	}
	
	public void testRunProcess() throws VixException {
		VixHost vixHost = null;
		VixVM vixVM = null;

		try {
			vixHost = getVixHost();
			vixVM = getVixVM(vixHost);
			vixVM.loginInGuest(VixWrapper.VIX_CONSOLE_USER_NAME, null);

			// processes
			vixVM.copyFileFromHostToGuest("c:\\cygwin\\bin\\cygwin1.dll", "c:\\cygwin1.dll");
			vixVM.copyFileFromHostToGuest("..\\exe\\sleep.exe", "c:\\sleep.exe");
			System.out.println("Start guest sleep (3 seconds, return value = 5)...");
			long startTime = System.currentTimeMillis();
			VixProcess process = vixVM.runProgramInGuest("c:\\sleep.exe", "3 5", 0);
			long endTime = System.currentTimeMillis();
			long elapsed = endTime - startTime;
			System.out.println("Process " + process.getPid() + " complete, " +
			  "exit code = " + process.getGuestProgramExitCode() + ", " +
			  "reported elapsed time = " + process.getGuestProgramElapsedTime());
			System.out.println("Measured elapsed time (" + elapsed + " msec)");
			// if this is less than 3 seconds, it probably didn't work
			if (elapsed < 3000) {
				System.out.println("runProgramInGuest() failed");
			}
			
	
			startTime = System.currentTimeMillis();
			process = vixVM.runProgramInGuest("c:\\sleep.exe", "3 5", 
				VixWrapper.VIX_RUNPROGRAM_RETURN_IMMEDIATELY);
			try {
				Thread.sleep(500);
			} catch (InterruptedException ie) {
				// safe to ignore
			}
			
			boolean foundProcess = false;
			List processList = vixVM.listProcessesInGuest();
			for (Iterator i = processList.iterator(); i.hasNext(); ) {
				VixProcess vixProcess = (VixProcess) i.next();
				if (vixProcess.getPid()==process.getPid()) {
					foundProcess = true;
				}
			}
			if (foundProcess) {
				vixVM.killProcessInGuest(process.getPid());
				foundProcess = false;
				processList = vixVM.listProcessesInGuest();
				for (Iterator i = processList.iterator(); i.hasNext(); ) {
					VixProcess vixProcess = (VixProcess) i.next();
					if (vixProcess.getPid()==process.getPid()) {
						foundProcess = true;
					}
				}
				if (foundProcess) {
					System.out.println("killProcessInGuest failed");
				}
			} else {
				System.out.println("runProgramInGuest (return immediately) failed");
			}
			
			endTime = System.currentTimeMillis();
			elapsed = endTime - startTime;
			System.out.println("Process " + process.getPid() + " complete, " +
			  "exit code = " + process.getGuestProgramExitCode() + ", " +
			  "reported elapsed time = " + process.getGuestProgramElapsedTime());
			System.out.println("Measured elapsed time (" + elapsed + " msec)");
			
			vixVM.logoutFromGuest();
		} finally {
			if (vixVM!=null) { vixVM.close(); }	
			if (vixHost!=null) { vixHost.close(); }
		}
	}
	
	public void testPowerOperations() throws VixException {
		VixHost vixHost = null;
		VixVM vixVM = null;

		try {
			vixHost = getVixHost();
			vixVM = getVixVM(vixHost);

			System.out.println("Suspending VM");
			vixVM.suspend();
			System.out.println("VM suspended, powering on VM");
			vixVM.powerOn(VixWrapper.VIX_VMPOWEROP_LAUNCH_GUI);
			System.out.println("VM powered on, resetting VM");
			vixVM.reset();
			System.out.println("VM reset; powering off VM");
			vixVM.powerOff();
		} finally {
			if (vixVM!=null) { vixVM.close(); }	
			if (vixHost!=null) { vixHost.close(); }
		}
	}
	
	
    public void main(String args[]) throws VixException, InterruptedException, IOException {
    	junit.textui.TestRunner.run(suite());
        return;
    }
}
