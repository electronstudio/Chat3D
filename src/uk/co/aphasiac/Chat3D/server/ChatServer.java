package uk.co.aphasiac.Chat3D.server;

/* this class creates a chat server thread listening on a user specified port.
   If none is given on the command line a default of 7843 is used */
public class ChatServer
{

    public static void main(String[] args)
    {
        if(args.length != 0)
            new ChatServerThread(Integer.parseInt(args[0])).start();

        else new ChatServerThread(7843).start();
    }
}