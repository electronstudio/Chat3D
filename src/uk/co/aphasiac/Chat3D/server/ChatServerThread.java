package uk.co.aphasiac.Chat3D.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServerThread extends Thread
{

  /* the socket the server will listen for connections on */
  ServerSocket servSock = null;

  /* this group holds all the clients connected to the server in a vector. it must be a
  seperate entity so that it also can be a thread and therefore clean out dead clients periodically */
  ClientGroup group;

  /* when we contruct a new chatserver thread we give it a port to listen on */
  public ChatServerThread(int portNumber)
  {
      try
      {
          servSock = new ServerSocket(portNumber);
      }
      catch (IOException e)
      {
          System.out.println(e);
          System.out.println("Could not initialise server. Exiting");
          System.exit(1);
      }

      System.out.println("Server successfully initialised on port "+portNumber+". Waiting for connection...");

      /* now we create a new client group and start it running */
      group = new ClientGroup();
      group.start();
  }

  public void run()
  {
      Socket tempSock;

      while(servSock!=null)
      {
          try
          {
              /* this method blocks until a connection is recieved. then it returns a socket */
              tempSock = servSock.accept();
              System.out.println("Recieved new connection");
              /* the new client is then added to the group */
              group.addClient(tempSock);
          }

          catch(IOException e)
          {
              System.out.println("New connection failed");
          }

      }
  }

}