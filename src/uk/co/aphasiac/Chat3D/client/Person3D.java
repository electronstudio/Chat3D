package uk.co.aphasiac.Chat3D.client;

import uk.co.aphasiac.Chat3D.common.*;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;

import javax.media.j3d.*;
import javax.vecmath.*;

// this class represents a 3D person in my java3D environment
public class Person3D extends Person
{
  /* this is the group that the person and all their geometry belongs to.
  To add and remove a person from the scene we add and remove their branch group */
  protected BranchGroup group;

  protected BranchGroup body; /* the body object that represents them in the world (i.e. their avatar) */

  protected TransformGroup moveTransform; /* transform group used to move the person */
  protected TransformGroup rotTransform; /* transform group used to rotate the person */
 // private TransformGroup posTransform; /* transform group used to position the person in the world */

  protected RotationInterpolator rotator; /* a interpolator used to smooth out person's rotation */
  protected Alpha rotAlpha; /* counter used to drive the rotation interpolator */

  protected PositionPathInterpolator mover; /* a interpolator used to smooth out movement */
  protected Alpha moveAlpha; /* counter used to drive the position interpolator */
  protected Point3f[] positions; /* positions the interpolator will move between */
  protected float[] knots = {0,1}; /* relative speed between positions */
  protected Point3f position = new Point3f();
  protected Vector3d positionVector = new Vector3d();

  protected int rotationTime = 6000;
  protected int moveTime = 300;
  protected boolean smooth_movement = true; /* indicates whether we want to use interpolators to smooth out
                                             movement (best not to on slow systems */
  protected Transform3D trans = new Transform3D(); /* used instead of interpolators */

  protected BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 200.0);
  //protected BoundingSphere bounds = new BoundingSphere();
  protected Transform3D zaxis;

  protected NameText nametext; /* text2d that represents the person's name in 3d space */

  protected SpeechBalloon speechballoon; /* speechballoon that shows what the person is saying */

  protected boolean bodyVisible = true; /* is the body currently visible */

  public Person3D(String name,int id, double x,double y,int direction,BranchGroup b,int avatarNumber)
  {
      super(name,id,x,y,direction,""+avatarNumber);

      // set the geometric shape to be used as this person's body
      body = new BranchGroup();
      body.setCapability(BranchGroup.ALLOW_DETACH);
      body.addChild(b);

      /* initilise the 3D parts of the person */
      init3D();
      initInterpolators();

      /* now apply the correct rotation and position transformations (in case they're wrong) */

      /* first for the position..*/

      this.setPosition(x,y);

      /* now the direction */
      this.setDirection(direction);

      /* compile the scene graph (for efficiency )*/
      group.compile();

  }

  /* initilises the person's interpolators */
  private void initInterpolators()
  {
     // BranchGroup interpolators = new BranchGroup();

     int correctDirection = dirIncrement-direction;

     rotAlpha = new Alpha(1,rotationTime/dirIncrement);
      rotator = new RotationInterpolator(rotAlpha, rotTransform);
      rotator.setMinimumAngle(((float) Math.PI*2)/dirIncrement*correctDirection);
      rotator.setMaximumAngle(((float) Math.PI*2)/dirIncrement*correctDirection);
      rotator.setSchedulingBounds(bounds);

      //interpolators.addChild(rotator);
      rotTransform.addChild(rotator);


//	  knots = new float[2];
	  positions = new Point3f[2];
	  positions[0] = new Point3f((float)x,position.y,(float)y);
	  positions[1] = new Point3f((float)x,position.y,(float)y);

//	  Transform3D zaxis = new Transform3D();
//	  zaxis.rotY(Math.PI/2);

	  moveAlpha = new Alpha(1,moveTime);
	  mover = new PositionPathInterpolator(moveAlpha,moveTransform,new Transform3D(),knots,positions);
	  mover.setSchedulingBounds(bounds);

	  //interpolators.addChild(mover);
           moveTransform.addChild(mover);

      //rotTransform.addChild(interpolators);

  }

  private void initTransformGroups()
  {
      /* create a new transform group used to move this person */
      moveTransform = new TransformGroup();

      /* set bits so it can be written to and read from */
      moveTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
      moveTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      moveTransform.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);

	  /* create a new transform group used to rotate this person */
      rotTransform = new TransformGroup();

      /* set bits so it can be written to and read from */
      rotTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
      rotTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      rotTransform.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
      rotTransform.setCapability(Group.ALLOW_CHILDREN_WRITE);


	  /* add the rotation group to the move transform group */
	  moveTransform.addChild(rotTransform);

  }

  /* initilises the 3D parts of this person (not needed if you are the viewer) */
  private void init3D()
  {
      ////////////////////// Create Avatar ////////////////////////////////

      /* create a branchgroup this person will belong in */
      group = new BranchGroup();

      /* set bits to allow it to be detached later (to remove this person from the 3D scene) */
      group.setCapability(BranchGroup.ALLOW_DETACH);
      group.setCapability(Group.ALLOW_CHILDREN_EXTEND);

      initTransformGroups();

      /* now the body is added to the rotate transform group */
      rotTransform.addChild(body);


      /* now the transform group is added to the branchgroup */
      group.addChild(moveTransform);


      ////////////////////// Create name tag ///////////////////////////////////

      nametext = new NameText(name,radius);

      moveTransform.addChild(nametext.getBranchGroup());


      ///////////////////////// Create speech balloon ///////////////////////////////

      speechballoon = new SpeechBalloon(new Point3d(0.0, 1.5, 0.0));

      moveTransform.addChild(speechballoon.getBranchGroup());


      //////////////////////// Compile branchgroup //////////////////////////////////
  }

  public NameText getNameText()
  {
    return nametext;
  }

  public void rotateCW()
  {
      if(smooth_movement)
      {
        int correctDirection = dirIncrement-direction;

        rotator.setMinimumAngle(((float) Math.PI*2)/dirIncrement*correctDirection);

        rotator.setMaximumAngle(((float) Math.PI*2)/dirIncrement*(correctDirection-1));

	     rotAlpha.setStartTime(System.currentTimeMillis());

        super.rotateCW();
      }

      else
      {
        super.rotateCW();

        this.setDirection(direction);
      }
  }

  public void rotateCCW()
  {
      if(smooth_movement)
      {
        int correctDirection = dirIncrement-direction;

        rotator.setMinimumAngle(((float) Math.PI*2)/dirIncrement*correctDirection);

        rotator.setMaximumAngle(((float) Math.PI*2)/dirIncrement*(correctDirection+1));

        rotAlpha.setStartTime(System.currentTimeMillis());

         super.rotateCCW();
      }

      else
      {
         super.rotateCCW();

          this.setDirection(direction);
      }
  }

  public void moveFowards(double distance)
  {
     if(smooth_movement)
     {
	     mover.setPosition(0,position);

	     super.moveFowards(distance);

	     position.x = (float) x;
	     position.z = (float) y;

	     mover.setPosition(1,position);

	     moveAlpha.setStartTime(System.currentTimeMillis());
      }

      else
      {
        super.moveFowards(distance);

        positionVector.x = (float) x;
		positionVector.z = (float) y;

		 position.x = (float) x;
	     position.z = (float) y;

        trans.setIdentity();

        trans.setTranslation(positionVector);

        moveTransform.setTransform(trans);

      }
  }

  public void moveBackwards(double distance)
  {
    if(smooth_movement)
    {
	     mover.setPosition(0,position);

        super.moveBackwards(distance);

	     position.x = (float) x;
	     position.z = (float) y;

	     mover.setPosition(1,position);

	     moveAlpha.setStartTime(System.currentTimeMillis());
     }

     else
      {
        super.moveBackwards(distance);

        positionVector.x = (float) x;
		positionVector.z = (float) y;

		position.x = (float) x;
		position.z = (float) y;

        trans.setIdentity();

        trans.setTranslation(positionVector);

        moveTransform.setTransform(trans);

      }

  }

  public void moveRight(double distance)
  {
	  if(smooth_movement)
    {
	     mover.setPosition(0,position);

        super.moveRight(distance);

	     position.x = (float) x;
	     position.z = (float) y;

	     mover.setPosition(1,position);

	     moveAlpha.setStartTime(System.currentTimeMillis());
     }

     else
      {
        super.moveRight(distance);

        positionVector.x = (float) x;
        positionVector.z = (float) y;

		position.x = (float) x;
		position.z = (float) y;

        trans.setIdentity();

        trans.setTranslation(positionVector);

        moveTransform.setTransform(trans);

      }

  }

  public void moveLeft(double distance)
  {
    if(smooth_movement)
    {
      mover.setPosition(0,position);

      super.moveLeft(distance);

      position.x = (float) x;
      position.z = (float) y;

      mover.setPosition(1,position);

      moveAlpha.setStartTime(System.currentTimeMillis());
     }

     else
      {
        super.moveLeft(distance);

        positionVector.x = (float) x;
		positionVector.z = (float) y;

		position.x = (float) x;
		position.z = (float) y;

        trans.setIdentity();

        trans.setTranslation(positionVector);

        moveTransform.setTransform(trans);

      }
  }


  public boolean moving()
  {
		return !moveAlpha.finished();
  }

  public boolean rotating()
  {
	    return !rotAlpha.finished();
  }

  public void setSmoothMovement(boolean b)
  {
      smooth_movement = b;
  }

  public boolean getSmoothMovement()
  {
      return smooth_movement;
  }

  public void setDirection(int direction)
  {
      super.setDirection(direction);

      trans.setIdentity();
      trans.rotY(-(Math.PI*2)/dirIncrement*direction);
      /* now get the current transformation and modify it by multiplying the two transformations */
      //Transform3D t = new Transform3D(rot);
      rotTransform.setTransform(trans);
  }

  public void setSpeech(String s)
  {
      speech = s;

      speechballoon.setText(s);
  }

  public boolean getBodyVisibility()
  {
    return bodyVisible;
  }

  public void setBodyVisibility(boolean b)
  {
    if(b) // user wants body to appear
    {
        if(bodyVisible) // if it is already visible return
          return;

        rotTransform.addChild(body); // add body geometry back to scene

        bodyVisible = true;
    }

    else // user wants body to disappear
    {
        if(!bodyVisible) // if it is already invisible return
          return;

        body.detach();
        speechballoon.setVisibility(false);

        bodyVisible = false;

    }
  }

  public void setPosition(double x,double y)
  {
     /* we set the position as normal */
     super.setPosition(x,y);

     /* but we also need to move the body and name text to the new position */

     trans.setIdentity();
     trans.setTranslation(new Vector3d(x,position.y,y));
     moveTransform.setTransform(trans);

	 position.x = (float) x;
	 position.z = (float) y;
  }

  public BranchGroup getBranchGroup()
  {
      return group;
  }
}