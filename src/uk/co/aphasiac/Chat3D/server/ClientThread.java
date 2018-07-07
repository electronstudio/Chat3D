package uk.co.aphasiac.Chat3D.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;

public class ClientThread extends Thread
{

  private ClientGroup parent;
  private Socket socket;
  private BufferedReader in;
  private PrintStream ps;
  private String alias;
  private int id;

  public ClientThread(Socket s, ClientGroup p)
  {
      socket = s;

      /* disable Nagle's algorithm, to improve latency */
      try
      {
          socket.setTcpNoDelay(true);
      }
      catch(SocketException e)
      {
          System.out.println("disabling Nagle's algoithm threw an exception");
      }

      parent = p;
  }

  public void run()
  {
      /* try and create new data streams */
      try
      {
          in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          ps = new PrintStream(socket.getOutputStream());
      }
      catch(IOException e)
      {
      }

      String input;

      /* now try and keep reading input while client is still active*/
      while(socket != null)
      {
          input = null;

          try
          {
              /* this method blocks until data is available in the steam*/
              input = in.readLine();

              /* if there is input send it to the parent Clientgroup to handle */
              if(input != null)
                  parent.handleInput(input,this);
          }
          catch(IOException e)
          {
          }
      }
  }

  /* sends this client a message, returns a boolean indicating whether the message was sent */
  public boolean message(String msg)
  {
      ps.println(msg);

	 // System.out.println("sending "+msg);

      return ps.checkError();
  }

  /* sets the client's name */
  public void setAlias(String s)
  {
      alias = s;
  }

  /* set the client's id number */
  public void setOId(int id)
  {
      this.id = id;
  }

  /* returns the clients name */
  public String getAlias()
  {
      return alias;
  }

  public int getOId()
  {
      return id;
  }

  /* this method is called to destoy the thread and therefore the client*/
  public void finalise()
  {
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

}