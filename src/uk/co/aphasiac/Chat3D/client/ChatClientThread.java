package uk.co.aphasiac.Chat3D.client;

import uk.co.aphasiac.Chat3D.common.ChatVariables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import uk.co.aphasiac.Chat3D.common.*;

public class ChatClientThread extends Thread
{
    private Socket socket;
    private BufferedReader in;
    private PrintStream ps;
    private ChatClient parent;
    private boolean connected = false;

    /* when we create a new chat client thread, we give it a host and port to connect to */
    public ChatClientThread(ChatClient parent)
    {
        this.parent = parent;

        /* here we initilise our socket and IO streams */
        try
        {
            /* the host and port # is given by our parent ChatClient */
            socket = new Socket(parent.getHost(),parent.getPort());

            /* disable Nagle's algorithm, to massively improve latency */
            try
            {
                socket.setTcpNoDelay(true);
            }
            catch(SocketException e)
            {
                System.out.println("disabling Nagle's algorithm threw an exception");
            }

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ps = new PrintStream(socket.getOutputStream());
            connected = true;
        }
        /* if exception occurs we set the socket to null, to indicate something went wrong) */
        catch(IOException e)
        {

        }

    }

    /* the run method of the thread initilaises the socket and data streams, and then runs a loop,
       waiting for input from the server*/
    public void run()
    {
        String input;

        while(socket != null)
        {
            input = null;

            /* this method blocks until there's data waiting in the stream. it then sends the data to
               this thread's parent chatclient*/
            try
            {
                input = in.readLine();

                if(input != null)
                  handleInput(input);
            }
            catch(IOException e)
            {
            }

        }

        connected = false;
    }

    /* this method is called when a person wants to log into the chat server
       login command and */
    public boolean login(String details)
    {
        return sendMessage(ChatVariables.LOGIN,parent.getAlias()+ChatVariables.SEPERATOR+details);
    }

	public void handleInput(String packet)
	{
            System.out.println(parent.getAlias()+" got "+packet);

		int command = Integer.parseInt(packet.substring(0,ChatVariables.COMMANDLENGTH));
      /* there might not be another token, as sometimes the name is not send to preserve bandwidth
		 (name can be derived from which thread called the method */
        String val = "";
        if(packet.length() > ChatVariables.COMMANDLENGTH)
           val = packet.substring(ChatVariables.COMMANDLENGTH,packet.length());

		switch(command)
		{
            case ChatVariables.FORWARDS:
            parent.forwards(null,val);
			break;

			case ChatVariables.BACKWARDS:
            parent.backwards(null,val);
			break;

			case ChatVariables.RIGHT:
            parent.right(null,val);
			break;

			case ChatVariables.LEFT:
            parent.left(null,val);
			break;

			case ChatVariables.CLOCKWISE:
            parent.clockwise(null,val);
			break;

			case ChatVariables.COUNTERCLOCKWISE:
            parent.counterclockwise(null,val);
			break;

			case ChatVariables.PING:
            parent.ping(null,null);
			break;

			case ChatVariables.SAY:
            parent.say(null,val);
			break;

                        case ChatVariables.WHISPER:
                        parent.whisper(null,val);
			break;

			case ChatVariables.SHOUT:
            parent.shout(null,val);
			break;

			case ChatVariables.SYNC:
			parent.sync(null,val);
			break;

			case ChatVariables.LOGIN:
            parent.login(null,val);
			break;

        /* logout = a client is finished and wants to leave the room */
		   case ChatVariables.LOGOUT:
           parent.logout(null,val);
		   break;

        /* this is called when the program recieves a list of clients
            worldsize and movesize are first two tokens*/
			case ChatVariables.WORLD:
            parent.world(null,val);
			break;
		}
	}

    public boolean sendMessage(int type,String msg)
    {
		if(type < 10)
		  ps.println("0"+type+msg);
		else ps.println(type+msg);

      //System.out.println("type+msg");

        return ps.checkError();
    }

    public boolean sendMessage(int type)
    {
        if(type < 10)
		  ps.println("0"+type);
		else ps.println(type+"");

        return ps.checkError();
    }

    /* this method is called to destoy the Chat Client thread*/
    public void finalise()
    {
        sendMessage(ChatVariables.LOGOUT);

        try
        {
            in.close();
            ps.close();
            socket.close();
        }
        catch (IOException e)
        {
        }

        socket = null;
    }

    public boolean isConnected()
    {
        return connected;
    }
}