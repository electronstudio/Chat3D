package uk.co.aphasiac.Chat3D.client;

import com.sun.j3d.utils.universe.MultiTransformGroup;

import javax.media.j3d.*;
import javax.vecmath.*;

public class Person3DViewer extends Person3D
{
  public static final int VIEW_FIRST_PERSON = 1;
  public static final int VIEW_THIRD_PERSON = 2;
  public static final int VIEW_ABOVE = 3;

  protected int viewMode;

  protected TransformGroup cameraPosTrans; /* transform group used to position the camera */
  protected TransformGroup cameraMoveTrans; /* transform group used to move the camera */
  protected TransformGroup cameraRotTrans; /* transform group used to rotate the camera */

  protected PositionPathInterpolator cameraMover; /* a interpolator used to smooth out movement */
  protected Alpha cameraMoveAlpha;

  protected PositionPathInterpolator cameraPositioner; /* a interpolator used animate the positioning of the camera */
  protected Alpha cameraPosAlpha;

  protected RotationInterpolator cameraRotator; /* a interpolator used to smooth out camera's rotation */
  protected Alpha cameraRotationAlpha; /* alpha used to drive rotation interpolator */

  protected Vector3d cameraPosVector = new Vector3d();
  protected Point3f cameraPos = new Point3f();

  protected double thirdPersonDistance = 5.5; /* distance from camera to person in 3rd perosn mode */


  public Person3DViewer(String name, int id, double x, double y, int direction,
                        BranchGroup body, int avatarNumber, MultiTransformGroup multiTrans)
  {
      super(name,id,x,y,direction,body,avatarNumber);

      // set all the transform groups
      cameraPosTrans = multiTrans.getTransformGroup(0);
      cameraMoveTrans = multiTrans.getTransformGroup(1);
      cameraRotTrans = multiTrans.getTransformGroup(2);

      BranchGroup interpolators = new BranchGroup();

      // set correct initial position
      cameraPos.x = (int) x;
      cameraPos.z = (int) y;

      // initilise the camera movement interpolator
      cameraMoveAlpha = new Alpha(1,moveTime);
      cameraMover = new PositionPathInterpolator(moveAlpha, cameraMoveTrans, new Transform3D(), knots, positions);
      cameraMover.setSchedulingBounds(bounds);
      interpolators.addChild(cameraMover);

      // initlise the camera rotation interpolator
      cameraRotationAlpha = new Alpha(1,rotationTime/dirIncrement);
      cameraRotator = new RotationInterpolator(rotAlpha, cameraRotTrans);
      cameraRotator.setSchedulingBounds(bounds);
      interpolators.addChild(cameraRotator);

      // initilise the camera position interpolator
//      cameraPosAlpha = new Alpha(0,moveTime);
//      Point3f[] p = {new Point3f(0.0f,1.0f,0.0f),new Point3f(0.0f,1.0f,0.0f)};
//      cameraPositioner = new PositionPathInterpolator(cameraPosAlpha, cameraPosTrans, new Transform3D(), knots, p);
//      cameraPositioner.setSchedulingBounds(bounds);
//      interpolators.addChild(cameraPositioner);

      group.addChild(interpolators);

      setViewMode(Person3DViewer.VIEW_FIRST_PERSON);
      viewMode = VIEW_FIRST_PERSON;

      nametext.setVisibility(false);
  }

  public void setViewMode(int viewmode)
  {
     Transform3D t3d = new Transform3D();

    switch(viewmode)
    {
        case VIEW_FIRST_PERSON:
        if(viewMode == VIEW_FIRST_PERSON)
          return;
        else viewMode = VIEW_FIRST_PERSON;
        System.out.println("setting 1st person mode");

        // set position of camera
        cameraPosVector.set(0.0,1.0,0.0);
         t3d.setIdentity();
        t3d.setTranslation(cameraPosVector);
        cameraPosTrans.setTransform(t3d);

        // set body visibility
        this.setBodyVisibility(false);

        // set camera axis of rotation
        t3d.setTranslation(new Vector3d(0.0,0.0,0.0));
        cameraRotator.setAxisOfRotation(t3d);

        // set rotation of camera
        this.setDirection(direction);

        break;

        case VIEW_THIRD_PERSON:
        if(viewMode == VIEW_THIRD_PERSON)
          return;
        else viewMode = VIEW_THIRD_PERSON;
        System.out.println("setting 3rd person mode");

        // set position of camera
        cameraPosVector.set(0.0,2.0,thirdPersonDistance);
        t3d.setIdentity();
        t3d.setTranslation(cameraPosVector);
        cameraPosTrans.setTransform(t3d);

        // set temporary rotation position of camera relative to body (so it's correct until interpolator is activated)

        t3d.setIdentity(); // first rotation
        t3d.rotY(-(Math.PI*2)/dirIncrement*direction);

        // and now position
        double camerax = thirdPersonDistance*Math.sin(2*Math.PI/dirIncrement*direction);
        double cameray = thirdPersonDistance*Math.cos(2*Math.PI/dirIncrement*direction) - thirdPersonDistance;

        //System.out.println("camerax="+camerax);

        cameraPosVector.set(-camerax,0.0,cameray); //-x because it is a translation backwards
        t3d.setTranslation(cameraPosVector);
        cameraRotTrans.setTransform(t3d);

        // set body visibility
        this.setBodyVisibility(true);

        // set camera axis of rotation
        t3d.setTranslation(new Vector3d(0.0,0.0,-thirdPersonDistance));
        cameraRotator.setAxisOfRotation(t3d);

//        // start interpolator positioning camera
//        cameraPos.x = 0.0f;
//        cameraPos.y = 0.0f;
//        cameraPos.z = 0.0f;
//        cameraPositioner.setPosition(0,cameraPos);
//
//        cameraPos.x = 0.0f;
//        cameraPos.y = 2.0f;
//        cameraPos.z = (float) thirdPersonDistance;
//        cameraPositioner.setPosition(1,cameraPos);
//
//        cameraPosAlpha.setLoopCount(1);
//        cameraPosAlpha.setStartTime(System.currentTimeMillis());


        break;

        case VIEW_ABOVE:
         if(viewMode == VIEW_ABOVE)
          return;
        else viewMode = VIEW_ABOVE;
        System.out.println("setting above view mode");

        // set position of camera
        cameraPosVector.set(0.0,10.0,0.0);
        t3d.setIdentity();
        t3d.rotX(-Math.PI/2);
        t3d.setTranslation(cameraPosVector);
        cameraPosTrans.setTransform(t3d);

        // set body visibility
        this.setBodyVisibility(true);

        // set camera axis of rotation
        t3d.setTranslation(new Vector3d(0.0,0.0,0.0));
        cameraRotator.setAxisOfRotation(t3d);

        // set rotation of camera
        this.setDirection(direction);

        break;
    }


  }

  public void setDirection(int direction)
  {
      super.setDirection(direction);

      if(cameraRotTrans != null)
      {
        Transform3D t = new Transform3D();
        t.rotY(-(Math.PI*2)/dirIncrement*direction);
        cameraRotTrans.setTransform(t);
      }
  }

  public void moveFowards(double distance)
  {
      cameraMover.setPosition(0,cameraPos);

      super.moveFowards(distance);

      cameraPos.x = (float) x;

//      if(viewMode == VIEW_ABOVE)
//        cameraPos.y = (float) y;
//      else
        cameraPos.z = (float) y;

    //  System.out.println("x="+x+" y="+y);

      cameraMover.setPosition(1,cameraPos);

      cameraMoveAlpha.setStartTime(System.currentTimeMillis());
  }

  public void moveBackwards(double distance)
  {
      cameraMover.setPosition(0,cameraPos);

      super.moveBackwards(distance);

      cameraPos.x = (float) x;

//      if(viewMode == VIEW_ABOVE)
//        cameraPos.y = (float) y;
//      else
        cameraPos.z = (float) y;

    //  System.out.println("x="+x+" y="+y);

      cameraMover.setPosition(1,cameraPos);

      cameraMoveAlpha.setStartTime(System.currentTimeMillis());
  }

  public void moveRight(double distance)
  {
      cameraMover.setPosition(0,cameraPos);

      super.moveRight(distance);

      cameraPos.x = (float) x;

//      if(viewMode == VIEW_ABOVE)
//        cameraPos.y = (float) y;
//      else
        cameraPos.z = (float) y;

    //  System.out.println("x="+x+" y="+y);

      cameraMover.setPosition(1,cameraPos);

      cameraMoveAlpha.setStartTime(System.currentTimeMillis());
  }

  public void moveLeft(double distance)
  {
      cameraMover.setPosition(0,cameraPos);

      super.moveLeft(distance);

      cameraPos.x = (float) x;

//      if(viewMode == VIEW_ABOVE)
//        cameraPos.y = (float) y;
//      else
        cameraPos.z = (float) y;

   //   System.out.println("x="+x+" y="+y);

      cameraMover.setPosition(1,cameraPos);

      cameraMoveAlpha.setStartTime(System.currentTimeMillis());
  }

  public void rotateCW()
  {
      int correctDirection = dirIncrement-direction;

      cameraRotator.setMinimumAngle(((float) Math.PI*2)/dirIncrement*correctDirection);

      cameraRotator.setMaximumAngle(((float) Math.PI*2)/dirIncrement*(correctDirection-1));

     // rotAlpha.setStartTime(System.currentTimeMillis());

      super.rotateCW();
  }

  public void rotateCCW()
  {
      int correctDirection = dirIncrement-direction;

      cameraRotator.setMinimumAngle(((float) Math.PI*2)/dirIncrement*correctDirection);

      cameraRotator.setMaximumAngle(((float) Math.PI*2)/dirIncrement*(correctDirection+1));

      //rotAlpha.setStartTime(System.currentTimeMillis());

      super.rotateCCW();
  }



}