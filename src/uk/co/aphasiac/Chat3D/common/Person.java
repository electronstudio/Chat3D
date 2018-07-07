package uk.co.aphasiac.Chat3D.common;

import java.awt.*;

/* this class holds the details of a person in a world */

/* REMEMBER, WE ASSUME THE WORLD AXIS ARE AS FOLLOWS         -------> X
                                                             |
                                                             |
                                                             |
                                                             V
                                                             Y         */

public class Person
{
  public static final int MOVETYPE_FORWARDS = 1;
  public static final int MOVETYPE_RIGHT = 2;
  public static final int MOVETYPE_BACKWARDS = 3;
  public static final int MOVETYPE_LEFT = 4;

  protected String name; /* person's name */
  protected int id; /*person's id number, used to identify them */
  protected double x,y; /* this is the person's (x,y) position in the world */
  protected int dirIncrement = 32; /* number of degree of rotation the person has */
  protected int direction; /* direction of the person 0 <= direction < dirIncrement */
  protected Rectangle boundingBox; /* used for collision detection */
  protected int radius = 1; /* each person has a physical shape of this radius */

  protected String speech = ""; /* this is used when a person is talking */
  protected boolean collisionDetection = true; /* used to indicate whether collision detection is on or off
                                               (by default it's on) */

  protected boolean synchronising = false; /* used to block further moves if client and server get out of
										      sync */

  protected String details; /* this field contains extra details given by the chat client. it can contain
                               anything (e.g avatar details for a 3d client), as long as other clients then
                               knows how to decode it */

  public Person(String name, int id, double x,double y,int direction,String details)
  {
      this.name = name;
      this.id = id;
      this.x = x;
      this.y = y;
      this.direction = direction;
      this.details = details;

      if(collisionDetection)
        createBoundingBox();
  }

  private void createBoundingBox()
  {
      boundingBox = new Rectangle();

      boundingBox.width = (int)radius*2;
      boundingBox.height = (int)radius*2;

      updateBoundingBox();
  }

  private void updateBoundingBox()
  {

      /* bounding box looks like this  p1 ____ p2
                                         |    |
                                       p4|____|p3

                                       with x,y in the centre*/

      boundingBox.x = (int)x - radius;
      boundingBox.y = (int)y - radius;

  }

  /* determines whether two people are colliding or not, using their bounding boxes */
  public synchronized boolean isCollidingWith(Person p)
  {
      //return boundingBox.intersects(p.boundingBox);

	  return this.distance(p) < radius*2;
  }

  public synchronized boolean willCollideWith(Person p, double movesize, int moveType)
  {
      int newx;
      int newy;

	  switch(moveType)
	  {
		case MOVETYPE_FORWARDS:
		newx = (int) (x + movesize*Math.sin(2*Math.PI/dirIncrement*direction));
		newy = (int) (y - movesize*Math.cos(2*Math.PI/dirIncrement*direction));
		break;

		case MOVETYPE_BACKWARDS:
		newx = (int) (x - movesize*Math.sin(2*Math.PI/dirIncrement*direction));
		newy = (int) (y + movesize*Math.cos(2*Math.PI/dirIncrement*direction));
		break;

		case MOVETYPE_RIGHT:
		int rightDir = direction + dirIncrement/4;
	        if(rightDir > dirIncrement)
	          rightDir-=dirIncrement;
                newx = (int) (x + movesize*Math.sin(2*Math.PI/dirIncrement*rightDir));
		newy = (int) (y - movesize*Math.cos(2*Math.PI/dirIncrement*rightDir));
		break;

		case MOVETYPE_LEFT:
		int leftDir = direction - dirIncrement/4;
		if(leftDir < 1)
	          leftDir+=dirIncrement;
                newx = (int) (x + movesize*Math.sin(2*Math.PI/dirIncrement*leftDir));
		newy = (int) (y - movesize*Math.cos(2*Math.PI/dirIncrement*leftDir));
		break;

		default:
		return false;
	  }

      //Rectangle tempbox = new Rectangle(newx,newy, boundingBox.width, boundingBox.height);

	  double dx = newx - p.getX();
	  double dy = newy - p.getY();

	  double distance = Math.sqrt(dx*dx + dy*dy);

	  return distance < radius*2;

//      return tempbox.intersects(p.boundingBox);
  }

  public synchronized boolean willBeOutOfBounds(Rectangle bounds, double movesize, int moveType)
  {
      int newx;
      int newy;

	  switch(moveType)
	  {
		case MOVETYPE_FORWARDS:
		newx = (int) (boundingBox.x + movesize*Math.sin(2*Math.PI/dirIncrement*direction));
		newy = (int) (boundingBox.y - movesize*Math.cos(2*Math.PI/dirIncrement*direction));
		break;

		case MOVETYPE_BACKWARDS:
		newx = (int) (boundingBox.x - movesize*Math.sin(2*Math.PI/dirIncrement*direction));
		newy = (int) (boundingBox.y + movesize*Math.cos(2*Math.PI/dirIncrement*direction));
		break;

		case MOVETYPE_RIGHT:
		int rightDir = direction + dirIncrement/4;
	        if(rightDir > dirIncrement)
	          rightDir-=dirIncrement;
                newx = (int) (boundingBox.x + movesize*Math.sin(2*Math.PI/dirIncrement*rightDir));
		newy = (int) (boundingBox.y - movesize*Math.cos(2*Math.PI/dirIncrement*rightDir));
		break;

		case MOVETYPE_LEFT:
		int leftDir = direction - dirIncrement/4;
		if(leftDir < 1)
	          leftDir+=dirIncrement;
                newx = (int) (boundingBox.x + movesize*Math.sin(2*Math.PI/dirIncrement*leftDir));
		newy = (int) (boundingBox.y - movesize*Math.cos(2*Math.PI/dirIncrement*leftDir));
		break;

		default:
		return false;
	  }

      Rectangle tempbox = new Rectangle(newx,newy, boundingBox.width, boundingBox.height);

      return !bounds.contains(tempbox);
  }

  /* how far is this person from person p */
  public synchronized double distance(Person p)
  {
      double dx = this.x - p.getX();
      double dy = this.y - p.getY();

      return Math.sqrt(dx*dx + dy*dy);
  }

  public synchronized void rotateCW()
  {
      if(direction == dirIncrement)
          direction = 1;
      else direction++;
  }

  public synchronized void rotateCCW()
  {
      if(direction == 1)
          direction = dirIncrement;
      else direction--;
  }

  public int getDirection()
  {
      return direction;
  }


  public int getDirIncrement()
  {
      return dirIncrement;
  }

  public synchronized void moveFowards(double distance)
  {
      x+=distance*Math.sin(2*Math.PI/dirIncrement*direction);

      y-=distance*Math.cos(2*Math.PI/dirIncrement*direction);

      if (collisionDetection)
        updateBoundingBox();
  }

  public synchronized void moveBackwards(double distance)
  {
      x+=-distance*Math.sin(2*Math.PI/dirIncrement*direction);

      y-=-distance*Math.cos(2*Math.PI/dirIncrement*direction);

      if (collisionDetection)
        updateBoundingBox();
  }

  public synchronized void moveRight(double distance)
  {
      int rightDir = direction + dirIncrement/4;
	  if(rightDir > dirIncrement)
	     rightDir-=dirIncrement;

      x+=distance*Math.sin(2*Math.PI/dirIncrement*rightDir);

      y-=distance*Math.cos(2*Math.PI/dirIncrement*rightDir);

      if (collisionDetection)
        updateBoundingBox();
  }

  public synchronized void moveLeft(double distance)
  {
	  int leftDir = direction - dirIncrement/4;
	  if(leftDir < 1)
	     leftDir+=dirIncrement;

      x+=distance*Math.sin(2*Math.PI/dirIncrement*leftDir);

      y-=distance*Math.cos(2*Math.PI/dirIncrement*leftDir);

      if (collisionDetection)
        updateBoundingBox();
  }

  public synchronized void setPosition(double x,double y)
  {
      this.x = x;
      this.y = y;

	  if (collisionDetection)
        updateBoundingBox();
  }

  public void setSpeech(String s)
  {
      speech = s;
  }

  public void setDirection(int direction)
  {
      this.direction = direction;
  }

  public double getX()
  {
      return x;
  }

  public double getY()
  {
      return y;
  }

  public String getName()
  {
      return name;
  }

  public int getOId()
  {
      return id;
  }

  public void setId(int id)
  {
      this.id = id;
  }

  public Rectangle getBoundingBox()
  {
      return boundingBox;
  }

  public int getRadius()
  {
      return radius;
  }

  public String getSpeech()
  {
      return speech;
  }

  public String getDetails()
  {
      return details;
  }

  public void setCollisionDetection(boolean c)
  {
      collisionDetection = c;
  }

  public synchronized String toSendableString(String seperator)
  {
	   StringBuffer str = new StringBuffer();

	   str.append(name);
       str.append(seperator);
	   str.append(id);
       str.append(seperator);
       str.append(x);
       str.append(seperator);
       str.append(y);
       str.append(seperator);
       str.append(direction);
       str.append(seperator);
       str.append(details);
	   str.append(seperator);

	   return str.toString();
  }

  public void setSynchronising(boolean s)
  {
       synchronising = s;
  }

  public boolean isSynchronising()
  {
       return synchronising;
  }

  public String toString()
  {
      return "Person:"+name+"\n"+
			 "ID:"+id+"\n"+
             "X position:"+x+"\n"+
             "Y position:"+y+"\n"+
             "Direction:"+direction+"\n";
  }
}