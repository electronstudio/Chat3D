package uk.co.aphasiac.Chat3D.server;

import uk.co.aphasiac.Chat3D.common.ChatEntity;
import uk.co.aphasiac.Chat3D.common.Person;
import uk.co.aphasiac.Chat3D.common.World;

import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;
import uk.co.aphasiac.Chat3D.common.*;

public class ClientGroup extends Thread implements ChatEntity
{
  private int worldsize = 20;
  private double movesize = 1;

  private boolean debugging = true;

  private Vector clients = new Vector();
  private World world = new World(worldsize,true);

  /* adds a new client to the group*/
  public void addClient(Socket s)
  {
      ClientThread tempThread = new ClientThread(s,this);
      clients.addElement(tempThread);
      tempThread.start();

      /* person is not added to world object until login procedure is completed.
	 See handleInout method below */
  }

  /* send a message "msg" of type "type" to all clients*/
  public void broadcast(int type, String msg)
  {
      for(int i=0;i<clients.size();i++)
	  {
	      if(type < 10)
            ((ClientThread) clients.elementAt(i)).message("0"+type+msg);
		  else ((ClientThread) clients.elementAt(i)).message(type+msg);
	  }
  }

  /* send a message "msg" of type "type" to all clients, excluding the person with the name "exclude"*/
  public void broadcast(int type, String msg, int excludeId)
  {
      for(int i=0;i<clients.size();i++)
          if(((ClientThread) clients.elementAt(i)).getOId() != excludeId)
		  {
		    if(type < 10)
			    ((ClientThread) clients.elementAt(i)).message("0"+type+msg);
            else ((ClientThread) clients.elementAt(i)).message(type+msg);
		  }
  }

   /* send a message "msg" of type "type" to client with the alias "target"*/
  public void sendMessage(int type, String msg, String target)
  {
      ClientThread tempThread;

      for(int i=0;i<clients.size();i++)
      {
          tempThread = ((ClientThread) clients.elementAt(i));

          if(tempThread.getName().equals(target))
          {
		      if(type < 10)
                  tempThread.message("0"+type+msg);
			  else tempThread.message(type+msg);
              return;
          }
      }
  }

  /* send a message "msg" of type "type" to client with the id "id"*/
  public void sendMessage(int type, String msg, int id)
  {
      ClientThread tempThread;

      for(int i=0;i<clients.size();i++)
      {
          tempThread = ((ClientThread) clients.elementAt(i));

          if(tempThread.getOId() == id)
          {
		      if(type < 10)
                  tempThread.message("0"+type+msg);
			  else tempThread.message(type+msg);
              return;
          }
      }
  }

  /* send a message "msg" of type "type" to clientthread */
  public void sendMessage(int type, String msg, ClientThread target)
  {
		if(type < 10)
		    target.message("0"+type+msg);
	    else target.message(type+msg);
  }

  /* this goes through the vector of clients and removes any that are "dead" (i.e disconnected) */
  public void cleanHouse()
  {
      ClientThread tempThread;

      for(int i=0;i<clients.size();i++)
      {
          tempThread = ((ClientThread) clients.elementAt(i));

          if(tempThread ==null || !tempThread.isAlive())
              clients.removeElement(tempThread);
      }
  }

  /* here's the thread's run method */
  public void run()
  {
      /* cleanhouse is called every 30 seconds */
      while(true)
      {
          try
          {
              Thread.sleep(30000);
              System.out.println("cleaning house");
          }
          catch(InterruptedException e)
          {
          }

          cleanHouse();
      }
  }

  /* this handles input from the clients */
  public void handleInput(String s, ClientThread t)
  {
      /* for debugging, will take this out later */
	  if(debugging)
        System.out.println("Got "+s+" from "+t.getAlias()+"("+t.getOId()+")");

      int command = Integer.parseInt(s.substring(0,COMMANDLENGTH));
      /* there might not be another token, as sometimes the name is not send to preserve bandwidth
	 (name can be derived from which thread called the method */
      String val = "";
      if(s.length() > COMMANDLENGTH)
         val = s.substring(COMMANDLENGTH,s.length());

	  Person p = world.getPersonWithID(t.getOId());

	  switch(command)
	  {
	    case PING:
		this.ping(p,null);
		break;

	    case SAY:
	    say(p,val);
	    break;

	    case SHOUT:
	    shout(p,val);
	    break;

      /* someone tries to move forwards (positive number) */
		case FORWARDS:
		forwards(p,null);
		break;

		case BACKWARDS:
		backwards(p,null);
		break;

		case RIGHT:
		right(p,null);
		break;

		case LEFT:
		left(p,null);
		break;

		case CLOCKWISE:
		clockwise(p,null);
		break;

		case COUNTERCLOCKWISE:
		counterclockwise(p,null);
		break;

		case SYNC:
		sync(p,null);
		break;

		case WHISPER:
		whisper(p,val);
		break;

      /* login = a new person is logging in. Set the alias, send a welcome message, and then send
         everyone an updated list of client names. Client may also include extra details which could
         mean anything and can be decoded by other clients (e.g for a 3d chatroom this may be which
         avatar they're using). These extra details are tagged on to the end of the packet
         packet looks like this LOGIN<person's name><seperator><login message><seperator><details> */

		 case LOGIN:
		 if(debugging)
		    System.out.println("someone is trying to log in!");

		  // check if name if taken ..

		  // create new person
          StringTokenizer tok = new StringTokenizer(val,SEPERATOR);
          String name = tok.nextToken();
	      int id = world.nextAvailableId();
          String details = "";
          if(tok.hasMoreTokens())
            details = tok.nextToken();

		  /* set the clients name to the name they request */
          t.setAlias(name);
	      t.setOId(id);
	      /* we may now create a person object and add them to the world */
	      Person tempPerson = new Person(t.getAlias(),t.getOId(),0,0,0,details);
	      world.addNewPerson(tempPerson);

		  /* now log them in */
		  login(tempPerson,null);
		  break;

		  case LOGOUT:
		  /* destroy clientthread */
		  t.finalise();
		  /* then log the person out */
		  logout(p,null);
		  break;
	  }
  }

  /* someone wants to say something
         packet looks like this -- SAY<name><seperator><text>
     packet should only be sent to people within target range
         */
  public void say(Person p, String s)
  {
        Person[] people = world.getPeopleArray();

        // for all people in world, if person is within hearing range of p and is not p send message to them
        for (int i = 0; i < people.length; i++)
            if(people[i].distance(p) <= HEARINGRANGE && people[i].getOId() != p.getOId())
              sendMessage(SAY,p.getOId()+SEPERATOR+s,people[i].getOId());
  }

  public void forwards(Person p, String s)
  {
	/* if person is out of sync ignore all requests to move until they are back in sync */
	if(p.isSynchronising())
		return;

    if(world.movePersonForwards(p,movesize))
		broadcast(FORWARDS,""+p.getOId(),p.getOId());
	else
		handleSync(p);

  }

  public void backwards(Person p, String s)
  {
	/* if person is out of sync ignore all requests to move until they are back in sync */
	if(p.isSynchronising())
		return;

    if(world.movePersonBackwards(p,movesize))
		broadcast(BACKWARDS,""+p.getOId(),p.getOId());
	else
		handleSync(p);
  }

  public void clockwise(Person p, String s)
  {
	p.rotateCW();

	broadcast(CLOCKWISE,""+p.getOId(),p.getOId());
  }

  public void counterclockwise(Person p, String s)
  {
	p.rotateCCW();

	broadcast(COUNTERCLOCKWISE,""+p.getOId(),p.getOId());
  }

  public void whisper(Person p, String s)
  {
	StringTokenizer tok = new StringTokenizer(s,SEPERATOR);
	int whisperTo = Integer.parseInt(tok.nextToken());
	String text = tok.nextToken();

	sendMessage(WHISPER,p.getOId()+SEPERATOR+text,whisperTo);
  }

  public void login(Person p, String s)
  {
	  /* send the whole world to the person logging in here */
        sendMessage(WORLD,worldInfo(),p.getOId());

	  /* now tell everyone this new person has joined (excluding the person themselves) */
          broadcast(LOGIN,p.toSendableString(SEPERATOR),p.getOId());

		  if(debugging)
		    System.out.println(p.getName()+" has logged in");
  }

  public void logout(Person p, String s)
  {
	/* remove the person from the world */
	  world.removePerson(p.getOId());

	  /* then send a message telling everyone they've left */
	  broadcast(LOGOUT,p.getOId()+"");

	  if(debugging)
		    System.out.println(p.getName()+" has logged out");
  }

  public void ping(Person p, String s)
  {
	sendMessage(PING,"",p.getOId());
  }

  public void world(Person p, String s)
  {
	sendMessage(WORLD,worldInfo(),p.getOId());
  }

  public void shout(Person p, String s)
  {
	broadcast(SHOUT,p.getOId()+SEPERATOR+s,p.getOId());
  }

  public void sync(Person p, String s)
  {
	 p.setSynchronising(false);
  }

  public void right(Person p, String s)
  {
	/* if person is out of sync ignore all requests to move until they are back in sync */
	if(p.isSynchronising())
		return;

    if(world.movePersonRight(p,movesize))
		broadcast(RIGHT,""+p.getOId(),p.getOId());
	else
		handleSync(p);
  }

  public void left(Person p, String s)
  {
	/* if person is out of sync ignore all requests to move until they are back in sync */
	if(p.isSynchronising())
		return;

    if(world.movePersonLeft(p,movesize))
		broadcast(LEFT,""+p.getOId(),p.getOId());
	else
		handleSync(p);
  }

  private void handleSync(Person p)
  {
	  /* set the synchronising boolean (this means any further messages from this client are
		 thrown away, until they have sent a SYNC message back */
	  p.setSynchronising(true);

	  /* send their correct position */
	  sendMessage(SYNC,p.getX()+SEPERATOR+p.getY()+SEPERATOR+p.getDirection(),p.getOId());
  }

  /* this builds a list of all clients along with their x,y positions, direction, and
     extra details all sperate by the SEPERATOR character in chat variables
     also world size and move size are tagged onto the front */
  public String worldInfo()
  {
      StringBuffer str = new StringBuffer();

      str.append(worldsize);
      str.append(SEPERATOR);

      str.append(movesize);
      str.append(SEPERATOR);

	  Person[] pArray = world.getPeopleArray();

      for (int i = 0; i < pArray.length; i++)
      {
          Person tempPerson = pArray[i];

          str.append(tempPerson.toSendableString(SEPERATOR));

      }

      return str.toString();
  }



}