package uk.co.aphasiac.Chat3D.client;

import com.sun.j3d.utils.geometry.*;

import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.*;

public class NameText
{
  private BranchGroup group; // main branchgroup of this component
  private BranchGroup nameGroup; // group that will contain the text2d (we can dettach it later to change text colour)
  private TransformGroup nameRotation; // transform group used by interpolator to rotate name

  private String name; // text of nametag
  private Text2D text2d; // text2d that will represent name in 3d space
  private int radius; // the radius of the circle the text will rotate in

  private Alpha rotAlpha; // alpha that will drive the name animation
  private RotationInterpolator nameRotator; // interpolator that will rotate the name text2d

  private Color3f color; // colour of text (grrrr @ american spellings!)

  public NameText(String name, int radius)
  {
      // set user-given variables
      this.name = name;
      this.radius = radius;

      // initilise branch and transform groups with appropriate capabilities
      group = new BranchGroup();

      nameRotation = new TransformGroup();
      nameRotation.setCapability(Group.ALLOW_CHILDREN_WRITE); // allow namegroup to be detached
      nameRotation.setCapability(Group.ALLOW_CHILDREN_EXTEND); // allow namegroup to be readded
      nameRotation.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

      nameGroup = new BranchGroup();
      nameGroup.setCapability(BranchGroup.ALLOW_DETACH); // allow it to detach at runtime

      // create a group to position the text2d in the 3d world
      Transform3D t1 = new Transform3D();
      t1.setTranslation(new Vector3d(0,1,radius)); // text2d is 1 metre above ground, and radius away from person
      TransformGroup posTrans = new TransformGroup(t1);

      // set up groups correctly
      group.addChild(posTrans); // group is at top

      posTrans.addChild(nameRotation); // position group is next

      nameRotation.addChild(nameGroup); // then rotation group, then finally nameGroup

      // initilise text2d
      color = new Color3f(1f,0f,0f); // default colour is black

      text2d = createText();

      // add text2d to nameGroup
      nameGroup.addChild(text2d);

      // initilise interpolator and alpha
      rotAlpha = new Alpha(-1, 4000);

      Transform3D t2 = new Transform3D();
      t2.setTranslation(new Vector3d(0,0,-radius)); // axis of rotation is -radius
      nameRotator = new RotationInterpolator(rotAlpha, nameRotation, t2, 0.0f, (float) Math.PI*2.0f);

      nameRotator.setSchedulingBounds(new BoundingSphere(new Point3d(0,0,0),radius));
      nameRotation.addChild(nameRotator); // add interpolator to scene

      // compile for efficiency
      group.compile();

  }

  private Text2D createText()
  {
      Text2D t = new Text2D(name,color,"Helvetica",70,Font.BOLD); // create text2d
      t.setCapability(Shape3D.ALLOW_APPEARANCE_READ);

      //set it so text2d appears when it faces away from viewer
      Appearance app = t.getAppearance();
      app.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
      PolygonAttributes polyAttrib = new PolygonAttributes();
      polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
      polyAttrib.setBackFaceNormalFlip(true);
      app.setPolygonAttributes(polyAttrib);

      // set it so that text can be set to visible and invisible at runtime
      RenderingAttributes rendAttrib = new RenderingAttributes();
      rendAttrib.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
      app.setRenderingAttributes(rendAttrib);

      return t;
  }

  public BranchGroup getBranchGroup()
  {
    return group;
  }

  public void setVisibility(boolean visibility)
  {
      text2d.getAppearance().getRenderingAttributes().setVisible(visibility);
      nameRotator.setEnable(visibility);

      if(visibility)
	    rotAlpha.setLoopCount(-1); // start the rotation alpha
      else rotAlpha.setLoopCount(0); // stop the rotation alpha (saves a littel cpu)
  }

  public void setTextColour(float r, float g, float b)
  {

      color.set(r,g,b); // set new colour

      nameGroup.detach(); // get rid of old text2d

      text2d = createText(); // create new text2d

      nameGroup = new BranchGroup(); // recreate branchgroup
      nameGroup.setCapability(BranchGroup.ALLOW_DETACH);

      nameGroup.addChild(text2d); // add text2d to namegroup

      nameRotation.addChild(nameGroup); // re-add namegroup
  }
}