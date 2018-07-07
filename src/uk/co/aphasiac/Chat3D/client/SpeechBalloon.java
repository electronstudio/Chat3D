package uk.co.aphasiac.Chat3D.client;

import com.sun.j3d.utils.geometry.*;

import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class SpeechBalloon
{

  private BranchGroup group; // the objects main branchgroup
  private TransformGroup positionTrans; // used to set the position of this object in the world
  private TransformGroup mainTrans; // all object geometry should be put into here

  private Billboard billboard; // behaviour that makes the speech balloon always face the viewer

  private int max_string_length = 100;

  private BufferedImage image;

  private float[] data; // this array holds the points of the speech balloon

  private Texture2D texture; // this is the speech balloon text

  private Appearance borderAppearance;
  private Appearance spikeAppearance;
  private Appearance faceAppearance;

  private Color background;
  private Color foreground;

  private boolean isVisible = false;

  private Timer timer;
  private DisappearTask disappearTask;
  private int disappearTime = 10; // seconds that a speech balloon will appear for

  private class DisappearTask extends TimerTask
  {
      public void run()
      {
        setVisibility(false);
      }
  }

  public SpeechBalloon(Point3d position)
  {
      // initilise image
      image = new BufferedImage(256,128,BufferedImage.TYPE_INT_ARGB);
      Graphics g = image.getGraphics();
      g.setColor(Color.white);
      g.fillRect(0,0,image.getWidth(),image.getHeight());

      // initilise colours
      background = new Color(1.0f,1.0f,1.0f);
      foreground = new Color(0.0f,0.0f,0.0f);

      // initilise timer and timertask
      timer = new Timer();
      disappearTask = new DisappearTask();

      // set the points that make the speech balloon
      setPointData();

      // create branchGroup, and make it dettachable
      group = new BranchGroup();
      group.setCapability(BranchGroup.ALLOW_DETACH);

      // create position group, and set correct translation
      positionTrans = new TransformGroup();
      Transform3D t3d = new Transform3D();
      t3d.setTranslation(new Vector3d(position.x,position.y,position.z));
      positionTrans.setTransform(t3d);
      // add this position group to main branchgroup
      group.addChild(positionTrans);

      // create transform group, give it a billboard behaviour and add it to position group
      mainTrans = new TransformGroup();
      mainTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

      billboard = new Billboard(mainTrans);
      billboard.setSchedulingBounds(new BoundingSphere());


      mainTrans.addChild(billboard);
      positionTrans.addChild(mainTrans);

      createBorder();

      createFace();

      createSpike();

     // setText("Hello li you are gorgeous yay!");

     this.setVisibility(false);

        // compile, for efficency
        group.compile();

  }

  public BranchGroup getBranchGroup()
  {
      return group;
  }

  public void dettach()
  {
      group.detach();
  }

  public boolean isVisible()
  {
      return isVisible;
  }

  public void setVisibility(boolean b)
  {
      faceAppearance.getRenderingAttributes().setVisible(b); // set face visibility
      borderAppearance.getRenderingAttributes().setVisible(b); // set border visibility
      spikeAppearance.getRenderingAttributes().setVisible(b); // set spike visiblity
      billboard.setEnable(b); // set billboard behaviour enabled

      isVisible = b; // set our isVisible flag
  }

  public void setForegroundColour(Color foreground)
  {
      this.foreground = foreground;
  }

  public void setBackgroundColour(Color background)
  {
      this.background = background;
  }

  public void setText(String text)
  {
      int fontSize;

      if(text.length() < max_string_length/2)
        fontSize = 22;
      else fontSize = 15;

      Font font = new Font("",Font.PLAIN,fontSize);

      boolean antiAliasing = true;

      FontRenderContext frc = new FontRenderContext(null,antiAliasing,false);

      // get graphics of our image
      Graphics2D offGraphics = (Graphics2D) image.createGraphics();

      // get the font metric of those graphics for our font
      FontMetrics metrics = offGraphics.getFontMetrics();

      // set text antialiasing on (it looks nicer!)
      if(antiAliasing)
        offGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // height of each line
      int lineHeight = (int) font.getStringBounds(text,frc).getHeight();


      // colour in the background
      offGraphics.setColor(background);
      offGraphics.fillRect(0,0,image.getWidth(),image.getHeight());

      offGraphics.setColor(foreground);
      offGraphics.setFont(font);

      StringTokenizer tok = new StringTokenizer(text," ");

      int borderSize = 4;

      int leftMargin = borderSize;
      int rightMargin = image.getWidth() -borderSize;

      int x = leftMargin;
      int y = lineHeight;

      while(tok.hasMoreTokens())
      {
          String word = tok.nextToken();

          // if this word is too long perform word wrap and go to next line
          if( x+font.getStringBounds(word,frc).getWidth() > rightMargin)
          {
              y += lineHeight;
              x = leftMargin;
          }

          offGraphics.drawString(word,x,y);

          x += font.getStringBounds(word+" ",frc).getWidth();
      }

      ImageComponent2D imageComp = new ImageComponent2D(ImageComponent.FORMAT_RGBA8,image);

       texture.setImage(0, imageComp);

       // make the speech balloon appear, and then disappear again in the time specified in disappearTime
       this.setVisibility(true);

       // if the task is already scheduling cancell it!
        disappearTask.cancel();

        // create a new disappear task and schedule it
        disappearTask = new DisappearTask();
       timer.schedule(disappearTask,disappearTime*1000);

  }

  private void createSpike()
  {
      // the spike is just one triangle in a triangle array
      TriangleArray spike = new TriangleArray(3, GeometryArray.COORDINATES);

      // first create the geometric points
      Point3f p = new Point3f(data[6], data[7], data[8]);  //(top left of spike)     6,7,8
      spike.setCoordinate(0, p);
      p.set(data[9], data[10], data[11]);                    //(bottom of spike)       9,10,11
      spike.setCoordinate(1, p);
      p.set(data[12], data[13], data[14]);                 //(top right of spike)    12,13,14
      spike.setCoordinate(2, p);

      // then give it an appearance
      spikeAppearance = new Appearance();
      spikeAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);

      // and allow it to have its visibility changed at runtime
      RenderingAttributes rend = new RenderingAttributes();
      rend.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
      spikeAppearance.setRenderingAttributes(rend);

      mainTrans.addChild(new Shape3D(spike, spikeAppearance));
  }

  private void createFace()
  {

      // the face is just one quad in a quad array
      QuadArray plane = new QuadArray(4, GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);

      // first create the geometric points
      Point3f p = new Point3f(data[0], data[1], data[2]);  //(top LH corner)        0,1,2
      plane.setCoordinate(0, p);
      p.set(data[3], data[4], data[5]);                    //(bottom LH corner)     3,4,5
      plane.setCoordinate(1, p);
      p.set(data[15], data[16], data[17]);                 //(bottom RH corner)      15,16,17
      plane.setCoordinate(2, p);
      p.set(data[18], data[19], data[20]);                 //(top RH corner)         18,19,20
      plane.setCoordinate(3, p);

      //then the texture points
      Point2f q = new Point2f( 0.0f, 1f);
      plane.setTextureCoordinate(0, q);
      q.set(0.0f, 0.0f);
      plane.setTextureCoordinate(1, q);
      q.set( 1f, 0.0f);
      plane.setTextureCoordinate(2, q);
      q.set( 1f, 1f);
      plane.setTextureCoordinate(3, q);

      // create the faces appeance
      faceAppearance = new Appearance();
      faceAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);

      // allow to to change visibility at runtime
      RenderingAttributes rend = new RenderingAttributes();
      rend.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
      faceAppearance.setRenderingAttributes(rend);

      // create an image component from our image
      ImageComponent2D imageComp = new ImageComponent2D(ImageComponent.FORMAT_RGBA8,image);


      // can't use parameterless constuctor
      texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
                                        imageComp.getWidth(), imageComp.getHeight());
      texture.setCapability(Texture2D.ALLOW_IMAGE_WRITE);
      texture.setCapability(Texture.ALLOW_ENABLE_WRITE);

      texture.setMagFilter(Texture.NICEST);
      //texture.setMinFilter(Texture.NICEST);

      texture.setImage(0, imageComp);

      faceAppearance.setTexture(texture);

      Shape3D planeObj = new Shape3D(plane, faceAppearance);

      mainTrans.addChild(planeObj);
  }

  private void setPointData()
  {
      data = new float[8*3];

      int i = 0;

      data[i++]= -1.0f; data[i++]= 1.5f; data[i++]= 0.0f; //0 (top LH corner)        0,1,2
      data[i++]= -1.0f; data[i++]= 0.5f; data[i++]= 0.0f; //1 (bottom LH corner)     3,4,5
      data[i++]= 0.1f; data[i++]= 0.5f; data[i++]= 0.0f; //2 (top left of spike)     6,7,8
      data[i++]= 0.0f; data[i++]= 0.0f; data[i++]= 0.0f; //3 (bottom of spike)       9,10,11
      data[i++]= 0.5f; data[i++]= 0.5f; data[i++]= 0.0f; //4 (top right of spike)    12,13,14
      data[i++]= 1.0f; data[i++]= 0.5f; data[i++]= 0.0f; //5 (bottom RH corner)      15,16,17
      data[i++]= 1.0f; data[i++]= 1.5f; data[i++]= 0.0f; //6 (top RH corner)         18,19,20
      data[i++]= -1.0f; data[i++]= 1.5f; data[i++]= 0.0f; //7 (top LH corner again)  21,22,23
  }

  private void createBorder()
  {
        int[] sc = {8}; // this is needed to create a linestrip array

        // create border using line strip array
        LineStripArray lineArray = new LineStripArray(8, LineArray.COORDINATES, sc); //*****
        lineArray.setCoordinates(0, data);

        // make it blue and two points across
        borderAppearance = new Appearance();
        borderAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);

        // set colour
        ColoringAttributes blueColoring = new ColoringAttributes();
        blueColoring.setColor(0.0f, 0.0f, 0.0f);
        borderAppearance.setColoringAttributes(blueColoring);

        // set line width
        LineAttributes lineAttrib = new LineAttributes();
        lineAttrib.setLineWidth(1.0f);
        borderAppearance.setLineAttributes(lineAttrib);

        // allow it to be set to visible or invisible at runtime
        RenderingAttributes rend = new RenderingAttributes();
        rend.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        borderAppearance.setRenderingAttributes(rend);

        // add speech balloon border to group (we need to set it slightly forwards)
        Transform3D t3d = new Transform3D();
        t3d.setTranslation(new Vector3f(0,0,0.01f));
        TransformGroup tempTrans = new TransformGroup(t3d);
        tempTrans.addChild(new Shape3D(lineArray, borderAppearance));
        mainTrans.addChild(tempTrans);
  }
}