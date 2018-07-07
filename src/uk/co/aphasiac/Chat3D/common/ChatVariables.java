package uk.co.aphasiac.Chat3D.common;

public interface ChatVariables
{
	/* variables used to save bandwidth when sending packets over the network. Every entity that
	   uses these variabls (i.e. server or client) must have methods to handle packets of that
	   type *

    For example, when someone wants to say something SAY can be appended onto the packet, and as long as
    the client also implements this interface they should be able to decode the type of message and
	send it to their say method*/

    public static final int SAY = 0;        /* someone has said something */
	public static final int FORWARDS = 1;   /* someone has moved forwards */
	public static final int BACKWARDS = 2;  /* someone has rotated clockwise */
	public static final int CLOCKWISE = 3;  /* someone has rotated clockwise */
	public static final int COUNTERCLOCKWISE = 4;   /* someone has rotated counterclockwise */
	public static final int WHISPER = 5; /* some has whisper */
	public static final int LOGIN = 6;  /* someone is trying to log in */
	public static final int LOGOUT = 7; /* someone is trying to log out */
	public static final int PING = 8;   /* ping command, usued to measure latency */
	public static final int WORLD = 9;  /* used to send world data when someone first logs in */
	public static final int SHOUT = 10; /* someone has shouted */
	public static final int SYNC = 11; /* used to update the person recieving's world position */
	public static final int RIGHT = 12; /* sidestep right */
	public static final int LEFT = 13; /* sidestep left */

	/* this specifies how many characters each command in this class occupies. i.e.
       if commands are in the range 0 - 9 then length = 1
                                    10-99 then length = 2
                                  100-999 then length = 3 and so on
       this is important so that a command can be out on the front of a packet without a seperating character */
    public final int COMMANDLENGTH = 2;

    /* used as a seperator between values, in case a packet contains multiple bits of information */
    public final String SEPERATOR = "|";

    /* used if packet needs a second seperator */
    public final String SEPERATOR2 = "&";

    public final int HEARINGRANGE = 20;


}