package uk.co.aphasiac.Chat3D.common;

import java.awt.*;
import java.util.Random;
import java.util.Vector;

/* a world is a collection of people */

/* REMEMBER, WE ASSUME THE WORLD AXIS ARE AS FOLLOWS         -------> X
                                                             |
                                                             |
                                                             |
                                                             V
                                                             Y         */
public class World
{

  protected Vector people;
  protected boolean[] idset;
  protected int maxSize;
  protected boolean collisionDetection = true; /* do we want collsion detection or not? */
  protected int worldsize = 200;
  protected Rectangle worldbounds;

  public World(int maxSize)
  {
      people = new Vector(maxSize);
      idset = new boolean[maxSize];
      this.maxSize = maxSize;
      worldbounds = new Rectangle(-worldsize/2,-worldsize/2,worldsize,worldsize);
  }

  public World(int maxSize,boolean collisionDetection)
  {
      people = new Vector(maxSize);
      idset = new boolean[maxSize];
      this.maxSize = maxSize;
      worldbounds = new Rectangle(-worldsize/2,-worldsize/2,worldsize,worldsize);

      this.collisionDetection = collisionDetection;
  }

  public void addPerson(Person p)
  {
      p.setCollisionDetection(collisionDetection);
      people.addElement(p);
      idset[p.getOId()] = true;
  }

  /* this should only be called by the server */
  public void addNewPerson(Person p)
  {
      System.out.println("adding new person to world!");

      p.setCollisionDetection(collisionDetection);

	  Random random = new Random();

	  while(isColliding(p))
	  {
	      System.out.println("collision detected, calculating new position");
		  p.setPosition(random.nextInt(11),random.nextInt(11));
		  System.out.println("x="+p.getX()+" y="+p.getY());
	  }

	  people.addElement(p);
      idset[p.getOId()] = true;

  }

  public boolean worldFull()
  {
      return maxSize == people.size();
  }

  public Person getPersonWithID(int id)
  {
      for (int i=0;i<people.size();i++)
          if( ((Person)people.elementAt(i)).getOId() == id)
              return (Person)people.elementAt(i);

        return null;
  }

  public Person getPersonWithName(String name)
  {
       for (int i=0;i<people.size();i++)
          if( ((Person)people.elementAt(i)).getName().equals(name) )
              return (Person)people.elementAt(i);

        return null;
  }

  public Person[] getPeopleArray()
  {
		Person[] pArray = new Person[people.size()];

		for(int i=0; i<people.size(); i++)
		  pArray[i] = (Person) people.elementAt(i);

		return pArray;
  }


  /* returns an array containing the names of everyone in the world */
  public String[] getPeopleNames()
  {
		String[] names = new String[people.size()];

		for(int i=0;i<people.size();i++)
		   names[i] = ((Person)people.elementAt(i)).getName();

		return names;
  }

  /* moves person then checks for collisions. if one exists person is moved back giving impression
     of no movement (slighty inefficient) */
  public synchronized boolean movePersonForwards(Person p1, double distance)
  {

     // check if they are outside of world bounds
     if(p1.willBeOutOfBounds(worldbounds,distance,Person.MOVETYPE_FORWARDS))
        return false;


      if(collisionDetection)
      {

          for (int i = 0; i < people.size(); i++)
          {
              Person p2 = (Person) people.elementAt(i);

              /* make it doesn't try and detect collision with yourself */
              if(p1.getOId() != p2.getOId())
			     if(p1.willCollideWith(p2,distance,Person.MOVETYPE_FORWARDS))
                   return false;
          }
      }

      p1.moveFowards(distance);

      return true;
  }

  public synchronized boolean movePersonBackwards(Person p1, double distance)
  {
      // check if they are outside of world bounds
      if(p1.willBeOutOfBounds(worldbounds,distance,Person.MOVETYPE_BACKWARDS))
        return false;

      if(collisionDetection)
      {

          for (int i = 0; i < people.size(); i++)
          {
              Person p2 = (Person) people.elementAt(i);

              /* make it doesn't try and detect collision with yourself */
              if(p1.getOId() != p2.getOId())
			     if(p1.willCollideWith(p2,distance,Person.MOVETYPE_BACKWARDS))
                   return false;
          }
      }

      p1.moveBackwards(distance);

      return true;
  }

  public synchronized boolean movePersonRight(Person p1, double distance)
  {
      // check if they are outside of world bounds
      if(p1.willBeOutOfBounds(worldbounds,distance,Person.MOVETYPE_RIGHT))
        return false;


      if(collisionDetection)
      {

          for (int i = 0; i < people.size(); i++)
          {
              Person p2 = (Person) people.elementAt(i);

              /* make it doesn't try and detect collision with yourself */
              if(p1.getOId() != p2.getOId())
			     if(p1.willCollideWith(p2,distance,Person.MOVETYPE_RIGHT))
                   return false;
          }
      }

      p1.moveRight(distance);

      return true;
  }

  public synchronized boolean movePersonLeft(Person p1, double distance)
  {
      // check if they are outside of world bounds
      if(p1.willBeOutOfBounds(worldbounds,distance,Person.MOVETYPE_LEFT))
        return false;


      if(collisionDetection)
      {

          for (int i = 0; i < people.size(); i++)
          {
              Person p2 = (Person) people.elementAt(i);

              /* make it doesn't try and detect collision with yourself */
              if(p1.getOId() != p2.getOId())
			     if(p1.willCollideWith(p2,distance,Person.MOVETYPE_LEFT))
                   return false;
          }
      }

      p1.moveLeft(distance);

      return true;
  }

  public boolean removePerson(int id)
  {
      for (int i=0;i<people.size();i++)
      {
          if( ((Person)people.elementAt(i)).getOId() == id )
          {
              people.removeElementAt(i);
	          idset[id] = false;
              return true;
          }
      }

      return false;
  }

  public synchronized boolean isColliding(Person p)
  {

      for(int i=0; i<people.size(); i++)
	  {
		Person p2 = (Person) people.elementAt(i);

	     if(p.getOId() != p2.getOId())
		    if(p.isCollidingWith(p2))
			    return true;
	  }

	  return false;

  }

  public synchronized boolean outOfBounds(Person p)
  {
      return !worldbounds.contains(p.getBoundingBox());
  }

  public int getMaxSize()
  {
      return maxSize;
  }

  /* finds the next avaialble id number in the id set */
  public synchronized int nextAvailableId()
  {
      for(int i=0; i<idset.length; i++)
	if(!idset[i])
	  return i;

      return -1;
  }

  public int numOfPeople()
  {
      return people.size();
  }

  public String toString()
    {
	StringBuffer str = new StringBuffer("====World====\n");

	for(int i=0;i<numOfPeople();i++)
	{
	   Person p = (Person) people.elementAt(i);
	   str.append(p.getName()+" x="+p.getX()+" y="+p.getY()+" dir="+p.getDirection()+"\n");
	}

	return str.toString();
    }
}