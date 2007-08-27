package net.sf.jvix;

/** Callback handler for vmware functions.
 * 
 * <p>From the vmware documentation:
 * 
 * <p>Callback Events
 * Note that a callback might be called several times, for several different reasons. For example, it might be called for periodic progress updates. The eventType parameter indicates why the callback is being called. The supported event types are: 
 * 
 * VIX_EVENTTYPE_JOB_COMPLETED -- This event indicates that the asynchronous action has completed, whether successfully or not. 
 * VIX_EVENTTYPE_JOB_PROGRESS -- This event may be passed several times to report progress on an asynchronous action. 
 * VIX_EVENTTYPE_FIND_ITEM -- This event is used by VixHost_FindItems() . 
 * VIX_EVENTTYPE_HOST_INITIALIZED -- This event is used by VixHost_Connect() . 
 * 
 * @author knoxg
 * @version $Id$
 */
public abstract class VixEventProc {

	/* * Internal buffer used by the jvix framework.  
	private transient byte[] jvixBuffer;
	*/
	
/*
	typedef void VixEventProc(VixHandle handle,
							  VixEventType eventType,
							  VixHandle moreEventInfo,
							  void *clientData);
*/
	/**
	 * Callback handler for native vmware functions
	 * 
	 * @param handle a job handle for the callback
	 * @param eventType one of the VixWrapper.VIX_EVENTTYPE_* constants
	 * @param moreEventInfo an optional handle returned by the vmware function
	 * @param clientData an opaque data object used in the original call that 
	 *   can be used to create a calling context for the function
	 */
	public abstract void callback(VixHandle handle, int eventType, 
		VixHandle moreEventInfo, Object clientData);
	
	
}
