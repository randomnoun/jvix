/** A program that just sleeps, used for testing process start/termination.
 *
 * It takes two arguments, a sleep time (in seconds) and a return value. The main function
 * will sleep for the specified number of seconds, and then return the value
 * specified to the operating system.
 *
 * @author knoxg
 * @version $Id$
 */

#include <stdio.h>
#include <sys/time.h>
#include <time.h>
#include <unistd.h>


/******************************************************************************************
 ** MAIN FUNCTION
 **/

int main(int argc, char **argv) {
	int sleepSec, returnValue;
	if (argc!=3) {
		printf("Usage: sleep [delay] [returnValue]\n");
		exit(1);
	}
	
	sleepSec = atoi(argv[1]);
	returnValue = atoi(argv[2]);
	
	printf("Sleeping for %d seconds...\n", sleepSec, returnValue);
	// return select(0, (fd_set *)0, (fd_set *)0, (fd_set *)0, &tv);
	// select(0, 0, 0, 0, &tv);
	
	sleep(sleepSec);
	printf("Returning %d...\n", returnValue);
	return returnValue;
}
