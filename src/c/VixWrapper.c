/** Implementation of the VIX JNI Wrapper
 *
 * @TODO the exception handling code in http://java.sun.com/docs/books/jni/html/exceptions.html
 *   probably needs to be implemented here
 *
 * Callbacks
   =========

   VIX supports the concept of either using callbacks or polling to determine completion
   of long-running tasks.

   The simplest case, polling, involves invoking a method with
   'null' for the callbackProc and clientData parameters. In these cases, the function returns
   immediately with a 'job handle', which can then be used as a paremeter to
   VixJob_CheckCompletion, which can be called repeatedly until the task is complete.
   Alternatively, VixJob_Wait can be invoked, which will cause the calling function to
   sleep until the desired vmware operation has completed. In this way, multiple
   vmware tasks can be queued up or run in parallel (depending on the threading model used).

   A more sophisticated callback approach is also provided. The C API represents this
   via a 'callbackProc' function, which is invoked as the job's state changes, and a
   'clientData' pointer, which is passed to this function, to allow it to differentiate
   between multiple jobs which may be using the same callback.

   jvix attempts to reproduce this behaviour using Java objects. the VixWrapper class
   allows a VixEventProc to be used in lieu of a callbackProc, and an arbitary java object to
   be used in lieu of a clientData pointer. It does this under the hood by using a
   fixed callbackProc implementation defined in this file (defaultCallback), which delegates
   control to the appropriate Java VixEventProc, passing in the appropriate client data object.

   To do this, it needs to allocate memory on the heap to keep track of the java class
   to call when the callback is invoked; this memory must be released manually, through
   a mechanism that doesn't actually exist yet. (i.e. memory will be leaked until the
   program terminates). Something to fix, no doubt.

   Perhaps the VixWrapper could keep a map of jobHandles->CombinedClientData pointers;
   as a jobHandle is reclaimed/released, the memory allocated to these pointers could
   be freed (via weak references?). This assumes jobHandles are not reused by the VIX API
   across calling threads or the virtual machines being called, of course.

   Early free'ing of client data memory would result in a callback invoking arbitrary
   instructions in the VM, which would almost certainly crash the thing.

 *
 * @author knoxg
 * @version $Id$
 */

/* to prevent conflicting ssize_t declaration between:
     /usr/lib/gcc/i686-pc-mingw32/3.4.4/../../../../i686-pc-mingw32/include/sys/types.h:104
     C:/Program Files/VMware/VMware VIX/vm_basic_types.h:721
*/
#define _SSIZE_T_DECLARED

#include "vix.h"
#include "jni.h"

#define LOGGER_NAME "net.sf.jvix.VixWrapper"
#define VIX_E_JNI_TOO_MANY_PROPERTIES                30001
#define VIX_E_JNI_CANNOT_RETURN_UNKNOWN_PROPERTYTYPE 30002
#define null NULL

/******************************************************************************************
 ** TYPE DEFINITIONS
 **/

typedef struct {
  int       size;
  char*     value;
} Blob;

typedef union {
  int       intValue;
  char*     stringValue;
  Bool      boolValue;
  VixHandle handleValue;
  int64     int64Value;
  Blob      blobValue;
} PropertyResult;

/** I'm going to use the clientData object to contain a reference to a combined structure
 *  that contains both an object implementing the net.sf.jvix.VixEventProc interface
 *  and the original clientData passed to the VIX API
 */
typedef struct {
    JavaVM  *javaVM;
    /* JNIEnv  *env;       handle to JVM environment. not sure if this needs to be tied to the calling thread.  */
    jobject eventProc;  /* event procedure to invoke in java */
    jobject clientData; /* original client data reference passed in from java (will be passed to eventProc as a parameter) */
} CombinedClientData;



/******************************************************************************************
 ** UTILITY FUNCTIONS
 **/

/** Log the text supplied using the log4j logger for
 * 'net.sf.jvix.VixWrapper' at debug level
 */
void logDebug(JNIEnv *env, char *text) {

    // used to prevent logger messages filling up System.out
    static int loggerError = 0;

	  // just going to dump these to stdout for the time being...
	  // printf("%s\n", text);
	
	
    jclass loggerClass = (*env)->FindClass(env, "org/apache/log4j/Logger");
    if (loggerClass == 0) {
        if (loggerError == 0) {
            printf("Could not retrieve org.apache.log4j.Logger class -- logging disabled");
            loggerError = 1;
        }
        return;
    }

    jmethodID getLoggerMethodId = (*env)->GetStaticMethodID(env, loggerClass, "getLogger", "(Ljava/lang/String;)Lorg/apache/log4j/Logger;");
    if (getLoggerMethodId == 0) {
        printf("Could not retrieve getLogger() method of org.apache.log4j.Logger");
        return;
    }
    jstring loggerName = (*env)->NewStringUTF(env, (const char *) LOGGER_NAME);
    jobject loggerObject = (*env)->CallStaticObjectMethod(env, loggerClass, getLoggerMethodId, loggerName);
    if (loggerObject == null) {
        if (loggerError == 0) {
            printf("Could not retrieve logger for '%s'", LOGGER_NAME);
            loggerError = 1;
        }
        return;
    }

    jmethodID debugMethodId = (*env)->GetMethodID(env, loggerClass, "debug", "(Ljava/lang/Object;)V");
    if (debugMethodId == 0) {
        if (loggerError == 0) {
            printf("Could not retrieve debug() method of org.apache.log4j.Logger");
            loggerError = 1;
        }
        return;
    }
    jstring loggerText = (*env)->NewStringUTF(env, (const char *) text);
    (*env)->CallVoidMethod(env, loggerObject, debugMethodId, loggerText);
}

/** Converts a net.sf.jvix.VixHandle object into a
 *  VixHandle object
 *
 * @param env a pointer to the JNI environment
 * @param handle a net.sf.jvix.VixHandle object
 *
 * @returns a VixHandle object
 *
 */
VixHandle unwrapVixHandle(JNIEnv *env, jobject handle) {
    jclass vixHandleClass = (*env)->GetObjectClass(env, handle);
    jfieldID valueFieldId = (*env)->GetFieldID(env, vixHandleClass , "value" , "I");
    jint value = (*env)->GetIntField(env, handle, valueFieldId);
    return (VixHandle) value;
}

/** Create a new net.sf.jvix.VixHandle object representing a
 *  VixHandle object
 *
 * @param env a pointer to the JNI environment
 * @param handle the handle to wrap
 *
 * @returns a net.sf.jvix.VixHandle object
 */
jobject wrapVixHandle(JNIEnv *env, VixHandle handle) {

    char dbgBuffer[40];
    snprintf(dbgBuffer, 40, "Creating new vix handle with value %d", handle);
    logDebug(env, dbgBuffer);

	/* printf("Creating new vix handle with value %d\n", handle);  */
    jclass vixHandleClass = (*env)->FindClass(env, "net/sf/jvix/VixHandle");
    jmethodID constructorId = (*env)->GetMethodID(env, vixHandleClass, "<init>" , "(I)V");
    jobject newHandle = (*env)->NewObject(env, vixHandleClass, constructorId, (jint) handle);
    return newHandle;
}

/** Create and throw a new VixException
 *
 * @param env JNI environment
 * @param errorCode the VIX errorCode to associate with this exception
 */
void throwVixException(JNIEnv *env, int errorCode)
{
  jclass vixExceptionClass = (*env)->FindClass(env, "net/sf/jvix/VixException");
  jmethodID constructorMethodId = (*env)->GetMethodID(env, vixExceptionClass, "<init>" , "(I)V");
  jobject newException = (*env)->NewObject(env, vixExceptionClass, constructorMethodId, (jint) errorCode);
  (*env)->Throw(env, (jthrowable) newException);
}

/** Create and throw a NullPointerException
 *
 * @param env JNI environment
 * @param errorCode the VIX errorCode to associate with this exception
 */
void throwNullPointerException(JNIEnv *env, char *text)
{
  jclass vixExceptionClass = (*env)->FindClass(env, "java/lang/NullPointerException");
  jmethodID constructorMethodId = (*env)->GetMethodID(env, vixExceptionClass, "<init>" , "(Ljava/lang/String;)V");
  jstring exceptionText = (*env)->NewStringUTF(env, (const char *) text);
  jobject newException = (*env)->NewObject(env, vixExceptionClass, constructorMethodId, exceptionText);
  (*env)->Throw(env, (jthrowable) newException);
}


/** Return a list item (items within the list must always be Integers for this to work).
 * Bad things will happen if this exceeds the known size of the list, or if other types of objects
 * are supplied.
 *
 * @param env pointer to the JNI environment
 * @param list a List of Integers
 * @param index the index of the value to return
 *
 * @param the index'th item in the list
 */
int getListItem(JNIEnv *env, jobject list, int index) {

    char dbgBuffer[40];
    snprintf(dbgBuffer, 40, "Returning %d'th item from list", index);
    logDebug(env, dbgBuffer);

	/* printf("Returning %d'th item from list\n", index); */
    jclass listClass = (*env)->GetObjectClass(env, list);
    jmethodID getMethodId = (*env)->GetMethodID(env, listClass, "get" , "(I)Ljava/lang/Object;");	
    jobject listItemObject = (*env)->CallObjectMethod(env, list, getMethodId, (jint) index);

		// @TODO rather import to check these types here
    jclass integerClass = (*env)->GetObjectClass(env, listItemObject);
    jmethodID intValueMethodId = (*env)->GetMethodID(env, integerClass, "intValue" , "()I");	
    jint listItem = (*env)->CallIntMethod(env, listItemObject, intValueMethodId);

    return listItem;
}

/** Create an Integer object (for property list return values
 *
 * @param env pointer to the JNI environment
 * @param intValue integer to wrap
 */
jobject createInteger(JNIEnv *env, int intValue) {
    jclass integerClass = (*env)->FindClass(env, "java/lang/Integer");
    jmethodID constructorId = (*env)->GetMethodID(env, integerClass, "<init>" , "(I)V");
    jobject newInteger = (*env)->NewObject(env, integerClass, constructorId, (jint) intValue);
    return newInteger;
} 

/** Create a Boolean object (for property list return values
 *
 * @param env pointer to the JNI environment
 * @param boolValue boolean to wrap
 */
jobject createBoolean(JNIEnv *env, Bool boolValue) {
    jclass booleanClass = (*env)->FindClass(env, "java/lang/Boolean");
    jmethodID constructorId = (*env)->GetMethodID(env, booleanClass, "<init>" , "(Z)V");
    jobject newBoolean = (*env)->NewObject(env, booleanClass, constructorId, (jboolean) boolValue);
    return newBoolean;
} 

/** Create a Long object (for property list return values
 *
 * @param env pointer to the JNI environment
 * @param longValue int64 value to wrap
 */
jobject createLong(JNIEnv *env, int64 longValue) {
    jclass longClass = (*env)->FindClass(env, "java/lang/Long");
    jmethodID constructorId = (*env)->GetMethodID(env, longClass, "<init>" , "(J)V");
    jobject newLong = (*env)->NewObject(env, longClass, constructorId, (jlong) longValue);
    return newLong;
} 

/** Create a Java byte array object (for property list blob values)
 *
 * @param env pointer to the JNI environment
 * @param BlobValue bloc value to wrap
 */
jobject createBlob(JNIEnv *env, Blob blobValue) {
    jbyteArray newByteArray = (*env)->NewByteArray(env, blobValue.size);
    // not sure if we need to perform a GetByteArrayRegion before running this...
    (*env)->SetByteArrayRegion(env, newByteArray, 0, blobValue.size, blobValue.value);
} 


/** Create a List containing property values. Memory for VIX Strings are released
 * as part of this process.
 *
 * @param propTypes an array of property types
 * @param props an array of properties
 */
jobject createPropertyList(JNIEnv *env, char *apiCall, int size, VixPropertyType *propTypes, PropertyResult *props)
{
	int i;
	char dbgBuffer[100];

    // should really do more error checking here
    jclass arrayListClass = (*env)->FindClass(env, "java/util/ArrayList");
    if (arrayListClass==NULL) { printf("Could not find class java/util/ArrayList\n"); }
    jmethodID constructorMethodId = (*env)->GetMethodID(env, arrayListClass, "<init>" , "()V");
    if (constructorMethodId==NULL) { printf("Could not find constructor for class java/util/ArrayList\n"); }
    jmethodID addMethodId = (*env)->GetMethodID(env, arrayListClass, "add" , "(Ljava/lang/Object;)Z");
    if (addMethodId==NULL) { printf("Could not find add method for class java/util/ArrayList\n"); }
    jobject resultList = (*env)->NewObject(env, arrayListClass, constructorMethodId);
    if (resultList==NULL) { printf("Could not create class java/util/ArrayList\n"); }

	for (i = 0; i < size; i++) {
		switch (propTypes[i]) {
			case VIX_PROPERTYTYPE_ANY:
				snprintf(dbgBuffer, 100, "%s cannot return VIX_PROPERTYTYPE_ANY type (property #%d); returning null", apiCall, i);
	            throwVixException(env, VIX_E_JNI_CANNOT_RETURN_UNKNOWN_PROPERTYTYPE);
                return NULL;

			case VIX_PROPERTYTYPE_INTEGER:
				snprintf(dbgBuffer, 100, "%s: Returning integer property %d", apiCall, props[i].intValue);
			    logDebug(env, dbgBuffer);
				(*env)->CallBooleanMethod(env, resultList, addMethodId, createInteger(env, props[i].intValue));
				break;
			
			case VIX_PROPERTYTYPE_STRING:
				snprintf(dbgBuffer, 100, "%s: Returning string property '%s'", apiCall, props[i].stringValue);
			    logDebug(env, dbgBuffer);
				(*env)->CallBooleanMethod(env, resultList, addMethodId, (*env)->NewStringUTF(env, props[i].stringValue));
				Vix_FreeBuffer(props[i].stringValue); 
				break;
				
			case VIX_PROPERTYTYPE_BOOL:
				snprintf(dbgBuffer, 100, "%s: Returning boolean property %d (%s)", apiCall, props[i].boolValue, (props[i].boolValue==0 ? "false" : "true"));
			    logDebug(env, dbgBuffer);
				(*env)->CallBooleanMethod(env, resultList, addMethodId, createBoolean(env, props[i].boolValue));
				break;
			
			case VIX_PROPERTYTYPE_HANDLE:
				snprintf(dbgBuffer, 100, "%s: Returning handle property %d", apiCall, props[i].handleValue);
			    logDebug(env, dbgBuffer);
				(*env)->CallBooleanMethod(env, resultList, addMethodId, wrapVixHandle(env, props[i].handleValue));
				break;

			case VIX_PROPERTYTYPE_INT64:
			    // @TODO this printf mask is probably wrong
				snprintf(dbgBuffer, 100, "%s: Returning int64 property %d", apiCall, props[i].int64Value);
			    logDebug(env, dbgBuffer);
				(*env)->CallBooleanMethod(env, resultList, addMethodId, createLong(env, props[i].int64Value));
				break;

			case VIX_PROPERTYTYPE_BLOB:
				snprintf(dbgBuffer, 100, "%s: Returning blob property", apiCall);
			    logDebug(env, dbgBuffer);
				(*env)->CallBooleanMethod(env, resultList, addMethodId, createBlob(env, props[i].blobValue));
				// TODO: release memory
				break;
		}
	}
	return resultList;
	
}

/** Default callback implementation that simply defers processing to the java callback
 * defined in the CombinedClientData structure
 *
 * @param jobHandle handle supplied by VIX API
 * @param eventType one of VIX_EVENTTYPE_JOB_COMPLETED, VIX_EVENTTYPE_JOB_PROGRESS,
 *    VIX_EVENTTYPE_FIND_ITEM or VIX_EVENTTYPE_HOST_INITIALIZED
 * @param moreEventInfo an optional additional handle supplied by the VIX API (use depends on initial
 *    function invoked)
 * @param clientData a pointer to a CombinedClientData structure which contains both the
 *    original clientData object and a java implementation of the event handler
 */
void defaultCallback(VixHandle jobHandle, VixEventType eventType, VixHandle moreEventInfo, void *clientData)
{
    CombinedClientData *ccd = (CombinedClientData*) clientData;
    JavaVM *javaVM = ccd->javaVM;
    // JNIEnv *env = ccd->env; // probably not valid any more
    JNIEnv *env;    
    
    /* printf("Invoking callback handler [calling env=%d]\n", env); */
    (*javaVM)->AttachCurrentThread(javaVM, (void**) &env, null);
    
    jint result = (*javaVM)->GetEnv(javaVM, (void**) &env, JNI_VERSION_1_2);
    logDebug(env, "invoking callback handler...");
    // @TODO we should verify that the class here is what we expect
    jclass vixEventProcClass = (*env)->GetObjectClass(env, ccd->eventProc);
    jmethodID callbackMethodId = (*env)->GetMethodID(env, vixEventProcClass , "callback" , "(Lnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixHandle;Ljava/lang/Object;)V");
  	(*env)->CallVoidMethod(env, ccd->eventProc, callbackMethodId, wrapVixHandle(env, jobHandle), (jint) eventType, wrapVixHandle(env, moreEventInfo), ccd->clientData);
    /* printf("Detaching from thread\n"); */
    (*javaVM)->DetachCurrentThread(ccd->javaVM);
}

/** Returns a pointer to a CombinedClientData object which can be used as an argument to
 *  VIX API calls generated by this wrapper. The memory for this structure is allocated by this
 * procedure; this memory is currently never reclaimed
 *
 * @TODO fix this by having another JNI entry point to release it when we know the operation
 *   is complete
 *
 * @param callbackProc a net.sf.jvix.VixEventProc object which will implement the callback handler
 * @param clientData  an arbitrary object supplied by the user which will be handed to the event procedure handler
 */
CombinedClientData *getCombinedClientData(JNIEnv *env, jobject callbackProc, jobject clientData) {
	if (callbackProc==null) {
	    return null;
	}	
    CombinedClientData *ccd = malloc(sizeof(CombinedClientData));
    if (ccd==null) {
        // @TODO throw outofmemoryexception
        return null;
    }
    if (callbackProc!=null) { callbackProc = (*env)->NewGlobalRef(env, callbackProc); }
    if (clientData!=null) { clientData = (*env)->NewGlobalRef(env, clientData); }
    (*env)->GetJavaVM(env, &(ccd->javaVM));
    ccd->eventProc = callbackProc;
    ccd->clientData = clientData;
    /* ccd->env = env; */
    return ccd;
}



/******************************************************************************************
 ** JNI FUNCTIONS
 **/

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixHost_Connect
 * Signature: (IILjava/lang/String;ILjava/lang/String;Ljava/lang/String;ILnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixHost_1Connect
  (JNIEnv *env, jclass clazz, jint apiVersion, jint hostType, jstring hostName,
   jint hostPort, jstring userName, jstring password, jint options,
   jobject propertyListHandle, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixHost_Connect begin");
    if ((*env)->ExceptionOccurred(env)) { return; }

    char *hostNameChars = (char *) 0 ;
    char *userNameChars = (char *) 0 ;
    char *passwordChars = (char *) 0 ;

    /* these functions can return 0 if they fail, but I'm going to ignore that */
    if (hostName) { hostNameChars = (char*) (*env)->GetStringUTFChars(env, hostName, 0); }
    if (userName) { userNameChars = (char*) (*env)->GetStringUTFChars(env, userName, 0); }
    if (password) { passwordChars = (char*) (*env)->GetStringUTFChars(env, password, 0); }

    /* this memory needs to be reclaimed at a later point, but I'm going to ignore that as well */
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
	VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixHost_Connect(
      (int) apiVersion,
      (VixServiceProvider) hostType,
      (char const *) hostNameChars,
      (int) hostPort,
      (char const *) userNameChars,
      (char const *) passwordChars,
      (VixHostOptions) options,
      unwrapVixHandle(env, propertyListHandle),
      callback,
      (void*) ccd);

    if (hostName) { (*env)->ReleaseStringUTFChars(env, hostName, (const char *) hostNameChars); }
    if (userName) { (*env)->ReleaseStringUTFChars(env, userName, (const char *) userNameChars); }
    if (password) { (*env)->ReleaseStringUTFChars(env, password, (const char *) passwordChars); }

    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixHost_Connect end");
    return methodResult;
}


/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_Open
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1Open
  (JNIEnv *env, jclass clazz, jobject hostHandle, jstring vmxFilePathName, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_Open begin");
    char *vmxFilePathNameChars = (char *) 0 ;

    if (vmxFilePathName) { vmxFilePathNameChars = (char*) (*env)->GetStringUTFChars(env, vmxFilePathName, 0); } 
    /* printf("path is %s\n", vmxFilePathNameChars); } */
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_Open (
      unwrapVixHandle(env, hostHandle),
      vmxFilePathNameChars,
      callback,
      (void*) ccd);
    if (vmxFilePathName) { (*env)->ReleaseStringUTFChars(env, vmxFilePathName, (const char *) vmxFilePathNameChars); }
    /* printf("result=%d\n", result); */
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_Open end");
    return methodResult;
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_PowerOff
 * Signature: (Lnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1PowerOff
  (JNIEnv *env, jclass clazz, jobject vmHandle, jint powerOffOptions, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_PowerOff begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_PowerOff (
      unwrapVixHandle(env, vmHandle),
      (VixVMPowerOpOptions) powerOffOptions,
      callback,
      (void*) ccd);

    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_PowerOff end");
    return methodResult;
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_PowerOn
 * Signature: (Lnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1PowerOn
  (JNIEnv *env, jclass clazz, jobject vmHandle, jint powerOpOptions, jobject propertyListHandle, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_PowerOn begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_PowerOn (
      unwrapVixHandle(env, vmHandle),
      (VixVMPowerOpOptions) powerOpOptions,
      unwrapVixHandle(env, propertyListHandle),
      callback,
      (void*) ccd);

    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_PowerOn end");
    return methodResult;
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    Vix_ReleaseHandle
 * Signature: (Lnet/sf/jvix/VixHandle;)V
 */
JNIEXPORT void JNICALL Java_net_sf_jvix_VixWrapper_Vix_1ReleaseHandle
  (JNIEnv *env, jclass clazz, jobject handle)
{
    logDebug(env, "Vix_ReleaseHandle begin");
    Vix_ReleaseHandle (unwrapVixHandle(env, handle));
    logDebug(env, "Vix_ReleaseHandle end");

};


/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixJob_Wait
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/util/List;)Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixJob_1Wait
  (JNIEnv *env, jclass clazz, jobject jobHandleObject, jobject propertyIds)
{
    char dbgBuffer[100];
    int  i;
    VixError  error;
    VixHandle jobHandle;
    
    logDebug(env, "VixJob_Wait begin");
		
	// since this is a varargs call, I'm going to have to code up versions of these depending
	// on the size of the list passed in to this method. Which is annoying. Would have been
	// nice if we'd been supplied a vprintf()-style version.
		
	jobHandle = unwrapVixHandle(env, jobHandleObject);
		
    // @TODO we should verify that the class here is what we expect
    jclass listClass = (*env)->GetObjectClass(env, propertyIds);
    jmethodID sizeMethodId = (*env)->GetMethodID(env, listClass, "size" , "()I");
    jint size = (*env)->CallIntMethod(env, propertyIds, sizeMethodId);
    VixPropertyID   propIds[6];
    VixPropertyType propTypes[6];
    PropertyResult  props[6];

    snprintf(dbgBuffer, 100, "size of list passed to VixJob_Wait: %d; jobHandle=%d", size, jobHandle);
    logDebug(env, dbgBuffer);

    if (size > 6) {
    	  logDebug(env, "VixJob_Wait thrown exception");
    	  throwVixException(env, VIX_E_JNI_TOO_MANY_PROPERTIES);
        return NULL;
    }
    for (i = 0; i < size; i++) {
    	propIds[i] = getListItem(env, propertyIds, i);
    	error = Vix_GetPropertyType(jobHandle, propIds[i], &propTypes[i]);
	    if (error!=VIX_OK) {
			snprintf(dbgBuffer, 100, "VixJob_Wait has thrown exception determining propertyType for propId %d\n", i);	    
    		logDebug(env, dbgBuffer);
    	  	throwVixException(env, error);
        	return NULL;
        }
    }
    switch (size) {
    	  case 0:
    	  	error = VixJob_Wait(jobHandle, VIX_PROPERTY_NONE);
    	  	break;
    	  	
    	  case 1:
    	  	error = VixJob_Wait(jobHandle, propIds[0], &props[0],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 2:
    	  	error = VixJob_Wait(jobHandle, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 3:
    	  	error = VixJob_Wait(jobHandle, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  propIds[2], &props[2],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 4:
    	  	error = VixJob_Wait(jobHandle, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  propIds[2], &props[2],
    	  	  propIds[3], &props[3],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 5:
    	  	error = VixJob_Wait(jobHandle, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  propIds[2], &props[2],
    	  	  propIds[3], &props[3],
    	  	  propIds[4], &props[4],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 6:
    	  	error = VixJob_Wait(jobHandle, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  propIds[2], &props[2],
    	  	  propIds[3], &props[3],
    	  	  propIds[4], &props[4],
    	  	  propIds[5], &props[5],
    	  	  VIX_PROPERTY_NONE);
    	  	break;
    }
    snprintf(dbgBuffer, 80, "VixJob_Wait invoked returnCode=%d", error);
    logDebug(env, dbgBuffer);

    if (error!=VIX_OK) {
    	logDebug(env, "VixJob_Wait has thrown an exception");
    	throwVixException(env, error);
        return NULL;

    } else {
        jobject resultList = createPropertyList(env, "VixJob_Wait", size, propTypes, props);
        // return resultList;
        if (resultList == NULL) {
        	logDebug(env, "VixJob_Wait has thrown an exception");
        } else {
        	logDebug(env, "VixJob_Wait end");
        }
        return resultList;
    }
}


/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VM_KillProcessInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;JILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VM_1KillProcessInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jlong pid, jint options, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_KillProcessInGuest begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_KillProcessInGuest (
      unwrapVixHandle(env, vmHandle),
      (uint64) pid,
      (int) options,
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_KillProcessInGuest end");
    return methodResult;	
}




/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixSnapshot_GetChild
 * Signature: (Lnet/sf/jvix/VixHandle;I)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixSnapshot_1GetChild
  (JNIEnv *env, jclass clazz, jobject parentSnapshotHandle, jint index)
{
    VixHandle childSnapshotHandle;
    logDebug(env, "VixSnapshot_GetChild begin");
    VixError error = (VixHandle) VixSnapshot_GetChild (
      unwrapVixHandle(env, parentSnapshotHandle),
      (int) index,
      &childSnapshotHandle );

    if (error!=VIX_OK) {
    	  logDebug(env, "VixSnapshot_GetChild thrown exception\n");
    	  throwVixException(env, error);
        return NULL;
    } else {
        jobject methodResult = wrapVixHandle(env, childSnapshotHandle);
        logDebug(env, "VixSnapshot_GetChild end");
	    return methodResult;
	}
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixSnapshot_GetNumChildren
 * Signature: (Lnet/sf/jvix/VixHandle;)I
 */
JNIEXPORT jint JNICALL Java_net_sf_jvix_VixWrapper_VixSnapshot_1GetNumChildren
  (JNIEnv *env, jclass clazz, jobject parentSnapshotHandle)
{
    int numChildSnapshots;
    logDebug(env, "VixSnapshot_GetNumChildren begin");
    VixError error = (VixHandle) VixSnapshot_GetNumChildren (
      unwrapVixHandle(env, parentSnapshotHandle),
      &numChildSnapshots
    );
    if (error!=VIX_OK) {
        logDebug(env, "VixSnapshot_GetNumChildren thrown exception\n");
        throwVixException(env, error);
        return (jint) NULL;
    } else {
        logDebug(env, "VixSnapshot_GetNumChildren end");
	    return (jint) numChildSnapshots;
	}
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixSnapshot_GetParent
 * Signature: (Lnet/sf/jvix/VixHandle;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixSnapshot_1GetParent
  (JNIEnv *env, jclass clazz, jobject snapshotHandle)
{
    VixHandle parentSnapshotHandle;
    logDebug(env, "VixSnapshot_GetParent begin");
    VixError error = (VixHandle) VixSnapshot_GetParent (
      unwrapVixHandle(env, snapshotHandle),
      &parentSnapshotHandle);

    if (error!=VIX_OK) {
        logDebug(env, "VixSnapshot_GetParent thrown exception\n");
    	throwVixException(env, error);
        return NULL;
    } else {
        jobject methodResult = wrapVixHandle(env, parentSnapshotHandle);
		logDebug(env, "VixSnapshot_GetParent end");
		return methodResult;
	}
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_AddSharedFolder
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Ljava/lang/String;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1AddSharedFolder
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring shareName, jstring hostPathName, jint flags, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_AddSharedFolder begin");
    char *shareNameChars = 0, *hostPathNameChars = 0;
    if (shareName) { shareNameChars = (char*) (*env)->GetStringUTFChars(env, shareName, 0); }
  	if (hostPathName) { hostPathNameChars = (char*) (*env)->GetStringUTFChars(env, hostPathName, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_AddSharedFolder (
      unwrapVixHandle(env, vmHandle),
      shareNameChars,
      hostPathNameChars,
      (VixMsgSharedFolderOptions) flags,
      callback,
      (void*) ccd);
    if (shareName) { (*env)->ReleaseStringUTFChars(env, shareName, (const char *) shareNameChars); }
    if (hostPathName) { (*env)->ReleaseStringUTFChars(env, hostPathName, (const char *) hostPathNameChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_AddSharedFolder end");
    return methodResult;	
}


/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_CopyFileFromGuestToHost
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Ljava/lang/String;ILnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1CopyFileFromGuestToHost
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring guestPathName, jstring hostPathName, jint options,
  jobject propertyListHandle, jobject callbackProc, jobject clientData)
{
		char *guestPathNameChars = 0, *hostPathNameChars = 0;
    logDebug(env, "VixVM_CopyFileFromGuestToHost begin");
    if (guestPathName) { guestPathNameChars = (char*) (*env)->GetStringUTFChars(env, guestPathName, 0); }
  	if (hostPathName) { hostPathNameChars = (char*) (*env)->GetStringUTFChars(env, hostPathName, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_CopyFileFromGuestToHost (
      unwrapVixHandle(env, vmHandle),
      guestPathNameChars,
      hostPathNameChars,
      (VixMsgSharedFolderOptions) options,
      unwrapVixHandle(env, propertyListHandle),
      callback,
      (void*) ccd);
    if (guestPathName) { (*env)->ReleaseStringUTFChars(env, guestPathName, (const char *) guestPathNameChars); }
    if (hostPathName) { (*env)->ReleaseStringUTFChars(env, hostPathName, (const char *) hostPathNameChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_CopyFileFromGuestToHost end");
    return methodResult;	
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_CopyFileFromHostToGuest
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Ljava/lang/String;ILnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1CopyFileFromHostToGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring hostPathName, jstring guestPathName,
   jint options, jobject propertyListHandle, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_CopyFileFromHostToGuest begin");
    char *hostPathNameChars = 0, *guestPathNameChars = 0;
    if (hostPathName) { hostPathNameChars = (char*) (*env)->GetStringUTFChars(env, hostPathName, 0); }
    if (guestPathName) { guestPathNameChars = (char*) (*env)->GetStringUTFChars(env, guestPathName, 0); }
  	
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_CopyFileFromHostToGuest (
      unwrapVixHandle(env, vmHandle),
      hostPathNameChars,
      guestPathNameChars,
      (VixMsgSharedFolderOptions) options,
      unwrapVixHandle(env, propertyListHandle),
      callback,
      (void*) ccd);

    if (hostPathName) { (*env)->ReleaseStringUTFChars(env, hostPathName, (const char *) hostPathNameChars); }
    if (guestPathName) { (*env)->ReleaseStringUTFChars(env, guestPathName, (const char *) guestPathNameChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_CopyFileFromHostToGuest end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_CreateSnapshot
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Ljava/lang/String;ILnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1CreateSnapshot
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring name, jstring description, jint options, jobject propertyListHandle, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_CreateSnapshot begin");
    char *nameChars = 0, *descriptionChars = 0;
    if (name) { nameChars = (char*) (*env)->GetStringUTFChars(env, name, 0); }
    if (description) { descriptionChars = (char*) (*env)->GetStringUTFChars(env, description, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_CopyFileFromGuestToHost (
      unwrapVixHandle(env, vmHandle),
      nameChars,
      descriptionChars,
      (int) options,
      unwrapVixHandle(env, propertyListHandle),
      callback,
      (void*) ccd);

    if (name) { (*env)->ReleaseStringUTFChars(env, name, (const char *) nameChars); }
    if (description) { (*env)->ReleaseStringUTFChars(env, description, (const char *) descriptionChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_CreateSnapshot end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_EnableSharedFolders
 * Signature: (Lnet/sf/jvix/VixHandle;ZILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1EnableSharedFolders
  (JNIEnv *env, jclass clazz, jobject vmHandle, jboolean enabled, jint option, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_EnableSharedFolders begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_EnableSharedFolders (
      unwrapVixHandle(env, vmHandle),
      (int) enabled,
      (int) option,
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_EnableSharedFolders end");
    return methodResult;
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_GetCurrentSnapshot
 * Signature: (Lnet/sf/jvix/VixHandle;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1GetCurrentSnapshot
  (JNIEnv *env, jclass clazz, jobject vmHandle)
{
    logDebug(env, "VixVM_GetCurrentSnapshot begin");
    VixHandle snapshotHandle;
    VixHandle error = (VixHandle) VixVM_GetCurrentSnapshot (
      unwrapVixHandle(env, vmHandle),
      &snapshotHandle);
    if (error!=VIX_OK) {
   	    logDebug(env, "VixVM_GetCurrentSnapshot thrown exception\n");
   	    throwVixException(env, error);
        return NULL;
    } else {
	    jobject methodResult = wrapVixHandle(env, snapshotHandle);
        logDebug(env, "VixVM_GetCurrentSnapshot end");
	    return methodResult;		
    }
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_GetNamedSnapshot
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1GetNamedSnapshot
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring snapshotName)
{
    logDebug(env, "VixVM_GetNamedSnapshot begin");
    VixHandle snapshotHandle;
    char *snapshotNameChars = 0;
    if (snapshotName) { snapshotNameChars = (char*) (*env)->GetStringUTFChars(env, snapshotName, 0); }
    VixHandle error = (VixHandle) VixVM_GetNamedSnapshot (
      unwrapVixHandle(env, vmHandle),
		  snapshotNameChars,
		  &snapshotHandle);
    if (snapshotName) { (*env)->ReleaseStringUTFChars(env, snapshotName, (const char *) snapshotNameChars); }
    if (error!=VIX_OK) {
    	logDebug(env, "VixVM_GetNamedSnapshot thrown exception\n");
    	throwVixException(env, error);
        return NULL;
    } else {
	    jobject methodResult = wrapVixHandle(env, snapshotHandle);
        logDebug(env, "VixVM_GetNamedSnapshot end");
		return methodResult;
    }
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_GetNumRootSnapshots
 * Signature: (Lnet/sf/jvix/VixHandle;)I
 */
JNIEXPORT jint JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1GetNumRootSnapshots
  (JNIEnv *env, jclass clazz, jobject vmHandle)
{
    logDebug(env, "VixVM_GetNumRootSnapshots begin");
    int result;
    VixError error = (VixHandle) VixVM_GetNumRootSnapshots (
      unwrapVixHandle(env, vmHandle),
      &result);
    if (error!=VIX_OK) {
   	    logDebug(env, "VixVM_GetNumRootSnapshots thrown exception\n");
   	    throwVixException(env, error);
        return (jint) NULL;
    } else {
	    logDebug(env, "VixVM_GetNumRootSnapshots end");
	    return (jint) result;		
    }
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_GetRootSnapshot
 * Signature: (Lnet/sf/jvix/VixHandle;I)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1GetRootSnapshot
  (JNIEnv *env, jclass clazz, jobject vmHandle, jint index)
{
    logDebug(env, "VixVM_GetRootSnapshot begin");
    int result;
    VixHandle snapshotHandle;
    VixError error = (VixHandle) VixVM_GetRootSnapshot (
      unwrapVixHandle(env, vmHandle),
      (int) index,
      &snapshotHandle);
    if (error!=VIX_OK) {
    	logDebug(env, "VixVM_GetRootSnapshot thrown exception\n");
    	throwVixException(env, error);
        return NULL;
    } else {
	    jobject methodResult = wrapVixHandle(env, snapshotHandle);
        logDebug(env, "VixVM_GetRootSnapshot end");
		return methodResult;
    }
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_ListProcessesInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1ListProcessesInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jint options, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_ListProcessesInGuest begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_ListProcessesInGuest (
      unwrapVixHandle(env, vmHandle),
      (int) options,
      callback,
      (void*) ccd);

    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_ListProcessesInGuest end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_LoginInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Ljava/lang/String;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1LoginInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring username, jstring password, jint options, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_LoginInGuest begin");
    char *usernameChars = 0, *passwordChars = 0;
    if (username) { usernameChars = (char*) (*env)->GetStringUTFChars(env, username, 0); }
  	if (password) { passwordChars = (char*) (*env)->GetStringUTFChars(env, password, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_LoginInGuest (
      unwrapVixHandle(env, vmHandle),
      usernameChars,
      passwordChars,
      (int) options,
      callback,
      (void*) ccd);
    if (username) { (*env)->ReleaseStringUTFChars(env, username, (const char *) usernameChars); }
   	if (password) { (*env)->ReleaseStringUTFChars(env, password, (const char *) passwordChars); }
    jobject methodResult = wrapVixHandle(env, result);
   	logDebug(env, "VixVM_LoginInGuest end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_LogoutFromGuest
 * Signature: (Lnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1LogoutFromGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_LogoutFromGuest begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_LogoutFromGuest (
      unwrapVixHandle(env, vmHandle),
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_LogoutFromGuest end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_OpenUrlInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;ILnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1OpenUrlInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring url, jint windowState, jobject propertyListHandle, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_OpenUrlInGuest begin");
    char *urlChars = 0;
    if (url) { urlChars = (char*) (*env)->GetStringUTFChars(env, url, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_OpenUrlInGuest (
      unwrapVixHandle(env, vmHandle),
      urlChars,
      windowState,
      unwrapVixHandle(env, propertyListHandle),
      callback,
      (void*) ccd);
    if (url) { (*env)->ReleaseStringUTFChars(env, url, (const char *) urlChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_OpenUrlInGuest end");
    return methodResult;		
}


/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_RevertToSnapshot
 * Signature: (Lnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1RevertToSnapshot
  (JNIEnv *env, jclass clazz, jobject vmHandle, jobject snapshotHandle, jint options, jobject propertyListHandle, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_RevertToSnapshot begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_RevertToSnapshot (
      unwrapVixHandle(env, vmHandle),
      unwrapVixHandle(env, snapshotHandle),
      (int) options,
      unwrapVixHandle(env, propertyListHandle),
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_RevertToSnapshot end");
    return methodResult;		
}


/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_RunProgramInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Ljava/lang/String;ILnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1RunProgramInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring guestProgramName, jstring commandLineArgs, jint options, jobject propertyListHandle, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_RunProgramInGuest begin");
    char *guestProgramNameChars = 0, *commandLineArgsChars = 0;
    if (guestProgramName) { guestProgramNameChars = (char*) (*env)->GetStringUTFChars(env, guestProgramName, 0); }
    if (commandLineArgs) { commandLineArgsChars = (char*) (*env)->GetStringUTFChars(env, commandLineArgs, 0); }

    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_RunProgramInGuest (
      unwrapVixHandle(env, vmHandle),
      guestProgramNameChars,
      commandLineArgsChars,
      (VixRunProgramOptions) options,
      unwrapVixHandle(env, propertyListHandle),
      callback,
      (void*) ccd);
    if (guestProgramName) { (*env)->ReleaseStringUTFChars(env, guestProgramName, (const char *) guestProgramNameChars); }
    if (commandLineArgs) { (*env)->ReleaseStringUTFChars(env, commandLineArgs, (const char *) commandLineArgsChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_RunProgramInGuest end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_RunScriptInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Ljava/lang/String;ILnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1RunScriptInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring interpreter, jstring scriptText, jint options, jobject propertyListHandle, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_RunScriptInGuest begin");
    char *interpreterChars = 0, *scriptTextChars = 0;
    if (interpreter) { interpreterChars = (char*) (*env)->GetStringUTFChars(env, interpreter, 0); }
    if (scriptText) { scriptTextChars = (char*) (*env)->GetStringUTFChars(env, scriptText, 0); }

    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_RunProgramInGuest (
      unwrapVixHandle(env, vmHandle),
      interpreterChars,
      scriptTextChars,
      (VixRunProgramOptions) options,
      unwrapVixHandle(env, propertyListHandle),
      callback,
      (void*) ccd);
    if (interpreter) { (*env)->ReleaseStringUTFChars(env, interpreter, (const char *) interpreterChars); }
    if (scriptText) { (*env)->ReleaseStringUTFChars(env, scriptText, (const char *) scriptTextChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_RunScriptInGuest end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_WaitForToolsInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1WaitForToolsInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jint timeout, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_WaitForToolsInGuest begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_WaitForToolsInGuest (
      unwrapVixHandle(env, vmHandle),
      (int) timeout,
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_WaitForToolsInGuest end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixHost_Disconnect
 * Signature: (Lnet/sf/jvix/VixHandle;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT void JNICALL Java_net_sf_jvix_VixWrapper_VixHost_1Disconnect
  (JNIEnv *env, jclass clazz, jobject hostHandle)
{
    logDebug(env, "VixHost_Disconnect begin");
    VixHost_Disconnect (unwrapVixHandle(env, hostHandle));
    logDebug(env, "VixHost_Disconnect end");
    return;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixHost_FindItems
 * Signature: (Lnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixHost_1FindItems
  (JNIEnv *env, jclass clazz, jobject hostHandle, jint searchType, jobject searchCriteria, jint timeout, 
   jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixHost_FindItems begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : defaultCallback);
    VixHandle result = (VixHandle) VixHost_FindItems (
      unwrapVixHandle(env, hostHandle),
      (int) searchType,
      unwrapVixHandle(env, searchCriteria),
      (int) timeout,
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixHost_FindItems end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixHost_RegisterVM
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixHost_1RegisterVM
  (JNIEnv *env, jclass clazz, jobject hostHandle, jstring vmxFilePath, jobject callbackProc, jobject clientData) 
{
    logDebug(env, "VixHost_RegisterVM begin");
    char *vmxFilePathChars = 0;
    if (vmxFilePath) { vmxFilePathChars = (char*) (*env)->GetStringUTFChars(env, vmxFilePath, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixHost_RegisterVM (
      unwrapVixHandle(env, hostHandle),
      vmxFilePathChars, 
      callback,
      (void*) ccd);
    if (vmxFilePath) { (*env)->ReleaseStringUTFChars(env, vmxFilePath, (const char *) vmxFilePathChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixHost_RegisterVM end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixHost_UnregisterVM
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixHost_1UnregisterVM
  (JNIEnv *env, jclass clazz, jobject hostHandle, jstring vmxFilePath, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixHost_UnregisterVM begin");
    char *vmxFilePathChars = 0;
    if (vmxFilePath) { vmxFilePathChars = (char*) (*env)->GetStringUTFChars(env, vmxFilePath, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixHost_UnregisterVM (
      unwrapVixHandle(env, hostHandle),
      vmxFilePathChars, 
      callback,
      (void*) ccd);
    if (vmxFilePath) { (*env)->ReleaseStringUTFChars(env, vmxFilePath, (const char *) vmxFilePathChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixHost_UnregisterVM end");
    return methodResult;		
}


/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixJob_CheckCompletion
 * Signature: (Lnet/sf/jvix/VixHandle;)Z
 */
JNIEXPORT jboolean JNICALL Java_net_sf_jvix_VixWrapper_VixJob_1CheckCompletion
  (JNIEnv *env, jclass clazz, jobject jobHandle)
{
    logDebug(env, "VixJob_CheckCompletion begin");
    Bool complete;
    VixError error = VixJob_CheckCompletion (
      unwrapVixHandle(env, jobHandle),
      &complete);
    if (error!=VIX_OK) {
    	  logDebug(env, "VixJob_CheckCompletion has thrown exception\n");
    	  throwVixException(env, error);
        return (jboolean) 0;
    } else {
	    logDebug(env, "VixJob_CheckCompletion end");
	    // assuming we can just cast here
	    return (jboolean) complete;
    }
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_CreateDirectoryInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Lnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1CreateDirectoryInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring pathName, jobject propertyListHandle, 
  jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_CreateDirectoryInGuest begin");
    char *pathNameChars = 0;
    if (pathName) { pathNameChars = (char*) (*env)->GetStringUTFChars(env, pathName, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_CreateDirectoryInGuest (
      unwrapVixHandle(env, vmHandle),
      pathNameChars, 
      unwrapVixHandle(env, propertyListHandle),
      callback,
      (void*) ccd);
    if (pathName) { (*env)->ReleaseStringUTFChars(env, pathName, (const char *) pathNameChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_CreateDirectoryInGuest end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_CreateTempFileInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1CreateTempFileInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jint options, jobject propertyListHandle, 
   jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_CreateTempFileInGuest begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_CreateTempFileInGuest (
      unwrapVixHandle(env, vmHandle),
      (int) options, 
      unwrapVixHandle(env, propertyListHandle),
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_CreateTempFileInGuest end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_Delete
 * Signature: (Lnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1Delete
  (JNIEnv *env, jclass clazz, jobject vmHandle, jint deleteOptions, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_Delete begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_Delete (
      unwrapVixHandle(env, vmHandle),
      (int) deleteOptions, 
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_Delete end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_DeleteDirectoryInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1DeleteDirectoryInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring pathName, jint options, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_DeleteDirectoryInGuest begin");
    char *pathNameChars = 0;
    if (pathName) { pathNameChars = (char*) (*env)->GetStringUTFChars(env, pathName, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_DeleteDirectoryInGuest (
      unwrapVixHandle(env, vmHandle),
      pathNameChars,
      (int) options, 
      callback,
      (void*) ccd);
    if (pathName) { (*env)->ReleaseStringUTFChars(env, pathName, (const char *) pathNameChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_DeleteDirectoryInGuest end");
    return methodResult;		
}  

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_DeleteFileInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1DeleteFileInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring guestPathName, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_DeleteFileInGuest begin");
    char *guestPathNameChars = 0;
    if (guestPathName) { guestPathNameChars = (char*) (*env)->GetStringUTFChars(env, guestPathName, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_DeleteFileInGuest (
      unwrapVixHandle(env, vmHandle),
      guestPathNameChars,
      callback,
      (void*) ccd);
    if (guestPathName) { (*env)->ReleaseStringUTFChars(env, guestPathName, (const char *) guestPathNameChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_DeleteFileInGuest end");
    return methodResult;		
}


/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_DirectoryExistsInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1DirectoryExistsInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring pathName, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_DirectoryExistsInGuest begin");
    char *pathNameChars = 0;
    if (pathName) { pathNameChars = (char*) (*env)->GetStringUTFChars(env, pathName, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_DirectoryExistsInGuest (
      unwrapVixHandle(env, vmHandle),
      pathNameChars,
      callback,
      (void*) ccd);
    if (pathName) { (*env)->ReleaseStringUTFChars(env, pathName, (const char *) pathNameChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_DirectoryExistsInGuest end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_FileExistsInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1FileExistsInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring guestPathName, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_FileExistsInGuest begin");
    char *guestPathNameChars = 0;
    if (guestPathName) { guestPathNameChars = (char*) (*env)->GetStringUTFChars(env, guestPathName, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_FileExistsInGuest (
      unwrapVixHandle(env, vmHandle),
      guestPathNameChars,
      callback,
      (void*) ccd);
    if (guestPathName) { (*env)->ReleaseStringUTFChars(env, guestPathName, (const char *) guestPathNameChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_FileExistsInGuest end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_GetNumSharedFolders
 * Signature: (Lnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1GetNumSharedFolders
  (JNIEnv *env, jclass clazz, jobject vmHandle, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_GetNumSharedFolders begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_GetNumSharedFolders (
      unwrapVixHandle(env, vmHandle),
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_GetNumSharedFolders end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_InstallTools
 * Signature: (Lnet/sf/jvix/VixHandle;ILjava/lang/String;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1InstallTools
  (JNIEnv *env, jclass clazz, jobject vmHandle, jint options, jstring commandLineArgs, 
   jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_InstallTools begin");
    char *commandLineArgsChars = 0;
    if (commandLineArgs) { commandLineArgsChars = (char*) (*env)->GetStringUTFChars(env, commandLineArgs, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_InstallTools (
      unwrapVixHandle(env, vmHandle),
      (int) options,
      commandLineArgsChars, 
      callback,
      (void*) ccd);
    if (commandLineArgs) { (*env)->ReleaseStringUTFChars(env, commandLineArgs, (const char *) commandLineArgsChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_InstallTools end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_KillProcessInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;JILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1KillProcessInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jlong pid, jint options, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_KillProcessInGuest begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_KillProcessInGuest (
      unwrapVixHandle(env, vmHandle),
      (uint64) pid,
      (int) options,
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_KillProcessInGuest end");
    return methodResult;		
}
  
/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_ListDirectoryInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1ListDirectoryInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring pathName, jint options, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_ListDirectoryInGuest begin");
    char *pathNameChars = 0;
    if (pathName) { pathNameChars = (char*) (*env)->GetStringUTFChars(env, pathName, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_ListDirectoryInGuest (
      unwrapVixHandle(env, vmHandle),
      pathNameChars,
      (int) options,
      callback,
      (void*) ccd);
    if (pathNameChars) { (*env)->ReleaseStringUTFChars(env, pathName, (const char *) pathNameChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_ListDirectoryInGuest end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_RemoveSharedFolder
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1RemoveSharedFolder
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring pathName, jint options, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_RemoveSharedFolder begin");
    char *pathNameChars = 0;
    if (pathName) { pathNameChars = (char*) (*env)->GetStringUTFChars(env, pathName, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_RemoveSharedFolder (
      unwrapVixHandle(env, vmHandle),
      pathNameChars,
      (int) options,
      callback,
      (void*) ccd);
    if (pathNameChars) { (*env)->ReleaseStringUTFChars(env, pathName, (const char *) pathNameChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_RemoveSharedFolder end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_RemoveSnapshot
 * Signature: (Lnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1RemoveSnapshot
  (JNIEnv *env, jclass clazz, jobject vmHandle, jobject snapshotHandle, jint options, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_RemoveSnapshot begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_RemoveSnapshot (
      unwrapVixHandle(env, vmHandle),
      unwrapVixHandle(env, snapshotHandle),
      (int) options,
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_RemoveSnapshot end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_RenameFileInGuest
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Ljava/lang/String;ILnet/sf/jvix/VixHandle;Lnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1RenameFileInGuest
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring oldName, jstring newName, jint options, 
  jobject propertyListHandle, jobject callbackProc, jobject clientData) 
{
    logDebug(env, "VixVM_RenameFileInGuest begin");
    char *oldNameChars=0, *newNameChars=0;
    if (oldName) { oldNameChars = (char*) (*env)->GetStringUTFChars(env, oldName, 0); }
    if (newName) { newNameChars = (char*) (*env)->GetStringUTFChars(env, newName, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_RenameFileInGuest (
      unwrapVixHandle(env, vmHandle),
      oldNameChars,
      newNameChars,
      (int) options,
      unwrapVixHandle(env, propertyListHandle),
      callback,
      (void*) ccd);
    if (oldNameChars) { (*env)->ReleaseStringUTFChars(env, oldName, (const char *) oldNameChars); }
    if (newNameChars) { (*env)->ReleaseStringUTFChars(env, newName, (const char *) newNameChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_RenameFileInGuest end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_Reset
 * Signature: (Lnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1Reset
  (JNIEnv *env, jclass clazz, jobject vmHandle, jint powerOnOptions, jobject callbackProc, jobject clientData) 
{
    logDebug(env, "VixVM_Reset begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_Reset (
      unwrapVixHandle(env, vmHandle),
      (int) powerOnOptions,
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_Reset end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_SetSharedFolderState
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/lang/String;Ljava/lang/String;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1SetSharedFolderState
  (JNIEnv *env, jclass clazz, jobject vmHandle, jstring shareName, jstring hostPathName, jint flags, 
  jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_SetSharedFolderState begin");
    char *shareNameChars = 0, *hostPathNameChars=0;
    if (shareName) { shareNameChars = (char*) (*env)->GetStringUTFChars(env, shareName, 0); }
    if (hostPathName) { hostPathNameChars = (char*) (*env)->GetStringUTFChars(env, hostPathName, 0); }
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_SetSharedFolderState (
      unwrapVixHandle(env, vmHandle),
      shareNameChars,
      hostPathNameChars,
      (int) flags,
      callback,
      (void*) ccd);
    if (shareNameChars) { (*env)->ReleaseStringUTFChars(env, shareName, (const char *) shareNameChars); }
    if (hostPathNameChars) { (*env)->ReleaseStringUTFChars(env, hostPathName, (const char *) hostPathNameChars); }
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_SetSharedFolderState end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_Suspend
 * Signature: (Lnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1Suspend
  (JNIEnv *env, jclass clazz, jobject vmHandle, jint powerOffOptions, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_Suspend begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_Suspend (
      unwrapVixHandle(env, vmHandle),
      (int) powerOffOptions,
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_Suspend end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_UpgradeVirtualHardware
 * Signature: (Lnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1UpgradeVirtualHardware
  (JNIEnv *env, jclass clazz, jobject vmHandle, jint options, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_UpgradeVirtualHardware begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_UpgradeVirtualHardware (
      unwrapVixHandle(env, vmHandle),
      (int) options,
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_UpgradeVirtualHardware end");
    return methodResult;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixJob_GetNumProperties
 * Signature: (Lnet/sf/jvix/VixHandle;I)I
 */
JNIEXPORT jint JNICALL Java_net_sf_jvix_VixWrapper_VixJob_1GetNumProperties
  (JNIEnv *env, jclass clazz, jobject jobHandle, jint resultPropertyId)
{
    logDebug(env, "VixJob_GetNumProperties begin");
    jint result = VixJob_GetNumProperties (
      unwrapVixHandle(env, jobHandle),
      resultPropertyId);
    logDebug(env, "VixJob_GetNumProperties end");
    return result;		
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixJob_GetNthProperties
 * Signature: (Lnet/sf/jvix/VixHandle;ILjava/util/List;)Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixJob_1GetNthProperties__Lnet_sf_jvix_VixHandle_2ILjava_util_List_2
  (JNIEnv *env, jclass clazz, jobject jobHandleObject, jint index, jobject propertyIds)
{
	char dbgBuffer[100];
	int  i;
	VixError error;

	logDebug(env, "VixJob_GetNthProperties begin");
	VixHandle jobHandle = unwrapVixHandle(env, jobHandleObject);
		
    // @TODO we should verify that the class here is what we expect
    jclass listClass = (*env)->GetObjectClass(env, propertyIds);
    jmethodID sizeMethodId = (*env)->GetMethodID(env, listClass, "size" , "()I");
    jint size = (*env)->CallIntMethod(env, propertyIds, sizeMethodId);
    VixPropertyID   propIds[6];
    VixPropertyType propTypes[6];
    PropertyResult  props[6];

    snprintf(dbgBuffer, 100, "size of list passed to VixJob_GetNthProperties: %d; jobHandle=%d", size, jobHandle);
    logDebug(env, dbgBuffer);

    if (size > 6) {
    	  logDebug(env, "VixJob_GetNthProperties thrown exception");
    	  throwVixException(env, VIX_E_JNI_TOO_MANY_PROPERTIES);
        return NULL;
    }
    for (i = 0; i < size; i++) {
    	propIds[i] = getListItem(env, propertyIds, i);
    	error = Vix_GetPropertyType(jobHandle, propIds[i], &propTypes[i]);
	    if (error!=VIX_OK) {
			snprintf(dbgBuffer, 100, "VixJob_GetNthProperties has thrown exception determining propertyType for propId %d\n", i);	    
    		logDebug(env, dbgBuffer);
    	  	throwVixException(env, error);
        	return NULL;
        }
    }
    switch (size) {
    	  case 0:
    	    // pointless, but we'll allow it
    	  	error = VixJob_GetNthProperties(jobHandle, index, VIX_PROPERTY_NONE);
    	  	break;
    	  	
    	  case 1:
    	  	error = VixJob_GetNthProperties(jobHandle, index, propIds[0], &props[0],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 2:
    	  	error = VixJob_GetNthProperties(jobHandle, index, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 3:
    	  	error = VixJob_GetNthProperties(jobHandle, index, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  propIds[2], &props[2],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 4:
    	  	error = VixJob_GetNthProperties(jobHandle, index, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  propIds[2], &props[2],
    	  	  propIds[3], &props[3],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 5:
    	  	error = VixJob_GetNthProperties(jobHandle, index, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  propIds[2], &props[2],
    	  	  propIds[3], &props[3],
    	  	  propIds[4], &props[4],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 6:
    	  	error = VixJob_GetNthProperties(jobHandle, index, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  propIds[2], &props[2],
    	  	  propIds[3], &props[3],
    	  	  propIds[4], &props[4],
    	  	  propIds[5], &props[5],
    	  	  VIX_PROPERTY_NONE);
    	  	break;
    }
    snprintf(dbgBuffer, 80, "VixJob_GetNthProperties invoked returnCode=%d", error);
    logDebug(env, dbgBuffer);

    if (error!=VIX_OK) {
    	logDebug(env, "VixJob_GetNthProperties has thrown an exception");
    	throwVixException(env, error);
        return NULL;

    } else {
        jobject resultList = createPropertyList(env, "VixJob_GetNthProperties", size, propTypes, props);
        // return resultList;
        if (resultList == NULL) {
        	logDebug(env, "VixJob_GetNthProperties has thrown an exception");
        } else {
        	logDebug(env, "VixJob_GetNthProperties end");
        }
        return resultList;
    }
}

/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    Vix_GetProperties
 * Signature: (Lnet/sf/jvix/VixHandle;Ljava/util/List;)Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_Vix_1GetProperties
  (JNIEnv *env, jclass clazz, jobject handleObject, jobject propertyIds)
{
	char dbgBuffer[100];
	int  i;
	VixError error;

	logDebug(env, "Vix_GetProperties begin");
	VixHandle handle = unwrapVixHandle(env, handleObject);
		
    // @TODO we should verify that the class here is what we expect
    jclass listClass = (*env)->GetObjectClass(env, propertyIds);
    jmethodID sizeMethodId = (*env)->GetMethodID(env, listClass, "size" , "()I");
    jint size = (*env)->CallIntMethod(env, propertyIds, sizeMethodId);
    VixPropertyID   propIds[6];
    VixPropertyType propTypes[6];
    PropertyResult  props[6];

    snprintf(dbgBuffer, 100, "size of list passed to Vix_GetProperties: %d; handle=%d", size, handle);
    logDebug(env, dbgBuffer);

    if (size > 6) {
    	  logDebug(env, "Vix_GetProperties has thrown an exception");
    	  throwVixException(env, VIX_E_JNI_TOO_MANY_PROPERTIES);
        return NULL;
    }
    for (i = 0; i < size; i++) {
    	propIds[i] = getListItem(env, propertyIds, i);
    	error = Vix_GetPropertyType(handle, propIds[i], &propTypes[i]);
	    if (error!=VIX_OK) {
			snprintf(dbgBuffer, 100, "Vix_GetProperties has thrown exception determining propertyType for propId %d\n", i);	    
    		logDebug(env, dbgBuffer);
    	  	throwVixException(env, error);
        	return NULL;
        }
    }
    switch (size) {
    	  case 0:
    	    // pointless, but we'll allow it
    	  	error = Vix_GetProperties(handle, VIX_PROPERTY_NONE);
    	  	break;
    	  	
    	  case 1:
    	  	error = Vix_GetProperties(handle, propIds[0], &props[0],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 2:
    	  	error = Vix_GetProperties(handle, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 3:
    	  	error = Vix_GetProperties(handle, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  propIds[2], &props[2],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 4:
    	  	error = Vix_GetProperties(handle, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  propIds[2], &props[2],
    	  	  propIds[3], &props[3],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 5:
    	  	error = Vix_GetProperties(handle, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  propIds[2], &props[2],
    	  	  propIds[3], &props[3],
    	  	  propIds[4], &props[4],
    	  	  VIX_PROPERTY_NONE);
    	  	break;

    	  case 6:
    	  	error = Vix_GetProperties(handle, propIds[0], &props[0],
    	  	  propIds[1], &props[1],
    	  	  propIds[2], &props[2],
    	  	  propIds[3], &props[3],
    	  	  propIds[4], &props[4],
    	  	  propIds[5], &props[5],
    	  	  VIX_PROPERTY_NONE);
    	  	break;
    }
    snprintf(dbgBuffer, 80, "VixJob_GetProperties invoked returnCode=%d", error);
    logDebug(env, dbgBuffer);

    if (error!=VIX_OK) {
    	logDebug(env, "VixJob_GetProperties has thrown an exception");
    	throwVixException(env, error);
        return NULL;

    } else {
        jobject resultList = createPropertyList(env, "VixJob_GetProperties", size, propTypes, props);
        // return resultList;
        if (resultList == NULL) {
        	logDebug(env, "VixJob_GetProperties has thrown an exception");
        } else {
        	logDebug(env, "VixJob_GetProperties end");
        }
        return resultList;
    }
}


/*
 * Class:     net_sf_jvix_VixWrapper
 * Method:    VixVM_GetSharedFolderState
 * Signature: (Lnet/sf/jvix/VixHandle;ILnet/sf/jvix/VixEventProc;Ljava/lang/Object;)Lnet/sf/jvix/VixHandle;
 */
JNIEXPORT jobject JNICALL Java_net_sf_jvix_VixWrapper_VixVM_1GetSharedFolderState
  (JNIEnv *env, jclass clazz, jobject vmHandle, jint index, jobject callbackProc, jobject clientData)
{
    logDebug(env, "VixVM_GetSharedFolderState begin");
    CombinedClientData *ccd = getCombinedClientData(env, callbackProc, clientData);
    VixEventProc *callback = (ccd==NULL ? NULL : *(VixEventProc **) &defaultCallback);
    VixHandle result = (VixHandle) VixVM_GetSharedFolderState (
      unwrapVixHandle(env, vmHandle),
      (int) index,
      callback,
      (void*) ccd);
    jobject methodResult = wrapVixHandle(env, result);
    logDebug(env, "VixVM_GetSharedFolderState end");
    return methodResult;		
}

