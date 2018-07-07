package uk.co.aphasiac.Chat3D.client;

import com.sun.j3d.loaders.vrml97.VrmlLoader;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.*;
import uk.co.aphasiac.Chat3D.common.Person;

import javax.media.j3d.*;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicListUI;
import javax.swing.text.*;
import javax.vecmath.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;

import uk.co.aphasiac.Chat3D.common.*;

public class Client3D extends JPanel implements ChatClient
{

	String codebase;

        Client3DPanel parent;

    /* avatar details */
    //String[] filenames = {"maggie.wrl","yoda.wrl","ant.wrl","legoman.wrl","robo.wrl"};
    double[] sizes =     {   1.0,         1.5,       1.2,      0.05,         1.0};
    double[] ypos =      {   1.0,         0.6,       0.0,      1.15,          1.0};
    BranchGroup[] avatars;

    /* world variables */
    World3D world;
    Person3DViewer me;
    double movesize;
    float floorsize = 100f;
    float floorheight = -0.0f;
    boolean smoothMovement = true;

    /* chat client variables */
    ChatClientThread chatClient;
    int port;
    String host;
    String alias;
    JTextField chatbox;
    long time;
    JList userlist;
    JTextPane chatlist;

    /* Java Swing Variables */
    Client3DOptions optionsWindow;


    /* Java 3D interface variables */
    Canvas3D canvas;
    SimpleUniverse universe;
    BranchGroup scene;
    int avatar;
    BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0),floorsize*1.5);
    BoundingLeaf boundingLeaf = new BoundingLeaf(new BoundingSphere());
    Shape3D grid;
    Appearance gridApp;

    Background background;

    Color backgrdColour = Color.white;

    Appearance floorApp;

    ExponentialFog fog;
    boolean fogEnabled = false;

    public Client3D(Client3DPanel parent,String codebase)
    {
        this.codebase = codebase; // get codebase
        this.parent = parent; // set parent
        this.avatars = parent.frontend.avatarviewer.getAvatars();

        /* set the layout manager of this panel */
        this.setLayout(new BorderLayout());

        /* now lets create our 3D scene */
        /* first create the canvas and add it to our panel */
        canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());


        /* now add a keylistener to the canvas */
        canvas.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e)
            {
                //System.out.println("key pressed!");

                int keycode = e.getKeyCode();

                if(e.isAltDown()) /* alt is down */
                  switch(keycode)
                  {
                    case KeyEvent.VK_LEFT:
                    if(!me.moving())
                      if(world.movePersonLeft(me,movesize))
                        chatClient.sendMessage(LEFT);
                      break;

                    case KeyEvent.VK_RIGHT:
                    if(!me.moving())
                      if(world.movePersonRight(me,movesize))
                        chatClient.sendMessage(RIGHT);
                    break;
                  }

                else switch(keycode) /* alt is not down */
              	{
                  case KeyEvent.VK_UP:
                    if(!me.moving())
                      if(world.movePersonForwards(me,movesize))
                        chatClient.sendMessage(FORWARDS);
                    break;

                  case KeyEvent.VK_DOWN:
                    if(!me.moving())
                      if(world.movePersonBackwards(me,movesize))
                        chatClient.sendMessage(BACKWARDS);
                    break;

                  case KeyEvent.VK_LEFT:
                    if(!me.rotating())
                    {
                      me.rotateCCW();
                      chatClient.sendMessage(COUNTERCLOCKWISE);
                    }
                    break;

                  case KeyEvent.VK_RIGHT:
                    if(!me.rotating())
                    {
                      me.rotateCW();
                      chatClient.sendMessage(CLOCKWISE);
                    }
                    break;

                  case KeyEvent.VK_ENTER:
                    if(chatbox.getText().length() > 0)
                      handleChat(chatbox.getText());
                    break;

                  case KeyEvent.VK_BACK_SPACE: // delete & backspace do the same thing
                    if(chatbox.getText().length() > 0)
                      chatbox.setText(chatbox.getText().substring(0,chatbox.getText().length()-1));
                    break;

                  case KeyEvent.VK_DELETE:    // delete & backspace do the same thing
                    if(chatbox.getText().length() > 0)
                      chatbox.setText(chatbox.getText().substring(0,chatbox.getText().length()-1));
                    break;

                  case KeyEvent.VK_INSERT:
                    {

                    }
                    break;

                  case KeyEvent.VK_NUMPAD5:
                    smoothMovement = !smoothMovement;
                    setSmoothMovement(smoothMovement);
                    break;

                  case KeyEvent.VK_F1:
                    me.setViewMode(Person3DViewer.VIEW_FIRST_PERSON);
                    break;

                  case KeyEvent.VK_F2:
                    me.setViewMode(Person3DViewer.VIEW_THIRD_PERSON);
                    break;

                  case KeyEvent.VK_F3:
                    me.setViewMode(Person3DViewer.VIEW_ABOVE);
                    break;

                  case KeyEvent.VK_ESCAPE:
                    if(optionsWindow.isShowing())
                      optionsWindow.hide();
                    else
                    {
                      optionsWindow.show();
                      optionsWindow.requestFocus();
                    }
                  break;


                  default:
                    if(Character.isDefined(e.getKeyChar()))
                    chatbox.setText(chatbox.getText()+e.getKeyChar());
                    break;

                }
            }
        });

		/* for efficiency purposes, if the canvas 3D is not in focus we want to turn off smooth movement */
		canvas.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e)
			{
				setSmoothMovement(false);
                                universe.setJ3DThreadPriority(Thread.currentThread().getPriority()-1);

			}

			public void focusGained(FocusEvent e)
			{
                                setSmoothMovement(smoothMovement);
                                // the options window should disappear if user goes back to main program
                               optionsWindow.hide();
                               universe.setJ3DThreadPriority(Thread.currentThread().getPriority()-(optionsWindow.renderingCheckbox.isSelected()?1:0));
			}
			});

        /* now lets create a simple universe (this creates a universe, locale, camera and viewingplatform
           three transform groups are specified, so that we can use different ones for moving, rotating and
		   setting the position of the person
        */
        universe = new SimpleUniverse(canvas,3);
        //universe.setJ3DThreadPriority(Thread.currentThread().getPriority()-1);

        /* now lets create the scene branchgroup */
        scene = new BranchGroup();
        /* we need to set bits to allow the adding of new children to the scene */
        scene.setCapability(Group.ALLOW_CHILDREN_WRITE);
        scene.setCapability(Group.ALLOW_CHILDREN_READ);
        scene.setCapability(Group.ALLOW_CHILDREN_EXTEND);

        /* set boundingleaf to be part of platform geometry (so it's always activated) */
        PlatformGeometry plat = new PlatformGeometry();
        plat.addChild(boundingLeaf);
        plat.compile();
        universe.getViewingPlatform().setPlatformGeometry(plat);

        /* create lighting and geometry here...*/
          scene.addChild(createSceneGraph());

        /* compile the scene (for performance purposes */
        scene.compile();

        /* finally add the scene to our universe */
        universe.addBranchGraph(scene);


	//////////////// CREATE GUI HERE /////////////////////////////////////

	/* first the canvas and chatbox */
	JPanel mainpanel = new JPanel(new BorderLayout());
	mainpanel.add(canvas,BorderLayout.CENTER);

        /* create the options panel */
        optionsWindow = new Client3DOptions(this);

        //mainpanel.add(chatbox,BorderLayout.SOUTH);
	mainpanel.setMinimumSize(new Dimension(1,0));
	mainpanel.setPreferredSize(new Dimension(400,0));

	 //this.add(mainpanel,BorderLayout.CENTER);

		/* lets add a user list */

		JPanel users = new JPanel(new BorderLayout());
		//users.setPreferredSize(new Dimension(100,0));
                JLabel usersLabel = new JLabel("Users",SwingConstants.CENTER);
                usersLabel.setBackground(new Color(166,202,240));
                usersLabel.setOpaque(true);
		users.add(usersLabel,BorderLayout.NORTH);


		userlist = new JList();
                // create a list ui to enable togg
                userlist.setUI(new BasicListUI(){
                  protected MouseInputListener createMouseInputListener()
                  {
                    return new MouseInputHandler(){
                      //react on mouse pressed...
                      public void mousePressed(MouseEvent e)
                      {
                        //fortunately BasicListUI provides what we need...
                        int index = convertYToRow(e.getY());
                        if(userlist.isSelectedIndex(index))
                          userlist.clearSelection();
                        else
                        {
                          userlist.addSelectionInterval(index,index);
                        }
                      }
                    };
                  }
                });
                userlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


                JScrollPane usersscroller = new JScrollPane(userlist);
		users.add(usersscroller,BorderLayout.CENTER);

		/* and a chat list */
		JPanel chat = new JPanel(new BorderLayout());
                JLabel chatLabel = new JLabel("Chat",SwingConstants.CENTER);
                chatLabel.setBackground(new Color(166,202,240));
                chatLabel.setOpaque(true);
		chat.add(chatLabel,BorderLayout.NORTH);

		chatlist = new JTextPane();
		chatlist.setEditable(false);

		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style s;
        Style regular = chatlist.addStyle("regular", def);
        //StyleConstants.setFontFamily(def, "SansSerif");

		s = chatlist.addStyle("name", def);
            StyleConstants.setForeground(s,Color.blue);
	   StyleConstants.setBold(s, true);

           s = chatlist.addStyle("whisper", def);
           StyleConstants.setForeground(s,new Color(0.0f,0.7f,0.0f));
	   StyleConstants.setBold(s, true);

	   s = chatlist.addStyle("command", def);
           StyleConstants.setForeground(s,Color.red);
	   StyleConstants.setBold(s, true);

	   s = chatlist.addStyle("shout", def);
	   StyleConstants.setBold(s, true);
	   StyleConstants.setItalic(s, true);

	   this.addStringtoChatWindow("Welcome to Java3D Chat!","command");


		JScrollPane chatscroller = new JScrollPane(chatlist);
		chat.add(chatscroller,BorderLayout.CENTER);


		/* now put both of these components in a split pane */
		JSplitPane splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,users,chat);
		splitpane.setPreferredSize(new Dimension(200,0));
		splitpane.setMinimumSize(new Dimension(0,0));
		//splitpane.setOneTouchExpandable(true);
		splitpane.setDividerSize(6);


		JSplitPane mainsplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,mainpanel,splitpane);
		//mainsplitpane.setOneTouchExpandable(true);
		this.add(mainsplitpane,BorderLayout.CENTER);
		mainsplitpane.setDividerSize(6);

		chatbox = new JTextField();




		this.add(chatbox,BorderLayout.SOUTH);

		/* action is performed when enter is pressed */
		chatbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if(chatbox.getText().length() > 0)
				   handleChat(chatbox.getText());

				canvas.requestFocus();

                chatbox.setText("");
			}
			});

    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public String getAlias()
    {
        return alias;
    }

   public void say(Person p, String s)
  {
	StringTokenizer tok = new StringTokenizer(s,SEPERATOR);

    int id = Integer.parseInt(tok.nextToken());
    String text = tok.nextToken();

	p = world.getPersonWithID(id);

        this.chatWindowAddSpeech(p.getName(),text);

    p.setSpeech(text);
  }

  public void forwards(Person p, String s)
  {
	world.getPersonWithID(Integer.parseInt(s)).moveFowards(movesize);
  }

  public void backwards(Person p, String s)
  {
	world.getPersonWithID(Integer.parseInt(s)).moveBackwards(movesize);
  }

  public void clockwise(Person p, String s)
  {
	world.getPersonWithID(Integer.parseInt(s)).rotateCW();
  }

  public void counterclockwise(Person p, String s)
  {
	world.getPersonWithID(Integer.parseInt(s)).rotateCCW();
  }

  public void whisper(Person p, String s)
  {
      System.out.println("got whisper text"+s);

      StringTokenizer tok = new StringTokenizer(s,SEPERATOR);

      int id = Integer.parseInt(tok.nextToken());
      String text = tok.nextToken();

      p = world.getPersonWithID(id);


      p.setSpeech(text);

     this.chatWindowAddWhisperFrom(p.getName(),text);
  }

  public void login(Person p, String s)
  {
	StringTokenizer tok = new StringTokenizer(s,SEPERATOR);

    String name = tok.nextToken();
	int id = Integer.parseInt(tok.nextToken());
	double x = Double.parseDouble(tok.nextToken());
	double y = Double.parseDouble(tok.nextToken());
	int direction = Integer.parseInt(tok.nextToken());
    String details = tok.nextToken();

    int av = Integer.parseInt(details);

	this.chatWindowAddCommand(name+" has entered the world");

    /* we don't want to add ourselves */
    if(!name.equals(alias))
	{
          Person3D newPerson = new Person3D(name,id,x,y,direction,this.createAvatar(av),av);

          newPerson.getNameText().setVisibility(optionsWindow.namesCheckBox.isSelected());

          world.addPerson(newPerson);

          userlist.setListData(world.getPeopleNames());
	}
  }

  public void logout(Person p, String s)
  {
	StringTokenizer tok = new StringTokenizer(s,SEPERATOR);

	int id = Integer.parseInt(tok.nextToken());

	p = world.getPersonWithID(id);

	/* remove the person from the world */
	 world.removePerson(id);

	 userlist.setListData(world.getPeopleNames());

	 this.chatWindowAddCommand(p.getName()+" has left the world");
  }

  public void ping(Person p, String s)
  {
	//chatbox.setText("Ping="+(System.currentTimeMillis()-time)+"ms");
	this.chatWindowAddCommand("Ping="+(System.currentTimeMillis()-time)+"ms");
  }

  public void world(Person p, String s)
  {
	StringTokenizer tok = new StringTokenizer(s,SEPERATOR);

    int worldsize = Integer.parseInt(tok.nextToken());

    /* lets initilise our world model */
    world = new World3D(worldsize,scene);

    movesize = Double.parseDouble(tok.nextToken());

    String name, details;
    double x,y;
    int id, direction;
    Person3D person;

    while(tok.hasMoreTokens())
    {

        name = tok.nextToken();
	    id = Integer.parseInt(tok.nextToken());
        x = Double.parseDouble(tok.nextToken());
        y = Double.parseDouble(tok.nextToken());
        direction = Integer.parseInt(tok.nextToken());
        details = tok.nextToken();

        int a = Integer.parseInt(details);

        /* if i am the viewer the camera is my 3D shape */
        if(name.equalsIgnoreCase(alias))
        {
			MultiTransformGroup temp = universe.getViewingPlatform().getMultiTransformGroup();

            me =  new Person3DViewer(name,id,x,y,direction,this.createAvatar(a),a,temp);

            person = (Person3D) me;

            //world.addMe(person);

            world.addPerson(person);

        }

        else
        {
			person = new Person3D(name,id,x,y,direction,this.createAvatar(a),a);

             world.addPerson(person);
        }


            }

		/* set the list of users */
		userlist.setListData(world.getPeopleNames());
  }

  public void shout(Person p, String s)
  {
	StringTokenizer tok = new StringTokenizer(s,SEPERATOR);

    int id = Integer.parseInt(tok.nextToken());
    String text = tok.nextToken().toUpperCase();

	p = world.getPersonWithID(id);

	this.chatWindowAddShout(p.getName(),text);

    p.setSpeech(text);
  }

  public void sync(Person p, String s)
  {
	StringTokenizer tok = new StringTokenizer(s,SEPERATOR);

	double x = Double.parseDouble(tok.nextToken());
	double y = Double.parseDouble(tok.nextToken());
	int direction = Integer.parseInt(tok.nextToken());

	me.setPosition(x,y);
	me.setDirection(direction);

	chatClient.sendMessage(SYNC);
  }

  public void right(Person p, String s)
  {
	world.getPersonWithID(Integer.parseInt(s)).moveRight(movesize);
  }

  public void left(Person p, String s)
  {
	world.getPersonWithID(Integer.parseInt(s)).moveLeft(movesize);
  }

  public BranchGroup createSceneGraph()
  {
      BranchGroup group = new BranchGroup();


      /* create lighting */

      Color3f light1Color = new Color3f(1.0f, 1.0f, 0.9f);
      Vector3f light1Direction  = new Vector3f(4.0f, -7.0f, -12.0f);
      Color3f light2Color = new Color3f(0.3f, 0.3f, 0.4f);
      Vector3f light2Direction  = new Vector3f(-6.0f, -2.0f, -1.0f);
      Color3f light3Color = new Color3f(0.9f, 0.9f, 0.8f);
      Vector3f light3Direction  = new Vector3f(-4.0f, -1.0f, 10.0f);


      DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
      light1.setInfluencingBounds(bounds);
      group.addChild(light1);  // add light to branch group

//      DirectionalLight light2 = new DirectionalLight(light2Color, light2Direction);
//      light2.setInfluencingBounds(bounds);
//      group.addChild(light2);  // add light to branch group

      DirectionalLight light3 = new DirectionalLight(light3Color, light3Direction);
      light3.setInfluencingBounds(bounds);
      group.addChild(light3);  // add light to branch group

//      PointLight light3 = new PointLight();
//      light3.setInfluencingBounds(bounds);
//      group.addChild(light3);

      AmbientLight light4 = new AmbientLight(new Color3f(0.9f,0.9f,0.9f));
      light4.setInfluencingBounds(bounds);
      scene.addChild(light4);

      /* add fog */

      fog = new ExponentialFog();
      fog.setCapability(Fog.ALLOW_COLOR_WRITE);
      fog.setCapability(fog.ALLOW_DENSITY_WRITE);

      fog.setDensity(0.0f);
      /* use a bounding leaf, for efficiency */

      fog.setInfluencingBounds(bounds);

      group.addChild(fog);


          /* create tree geometry */

        VrmlLoader newvrml = new VrmlLoader();
        try
        {
            scene.addChild(newvrml.load("models/folage.wrl").getSceneGroup());
        }
        catch(FileNotFoundException e)
        {
            System.out.println("tree vrml file not found");
        }

		  /* creare textured floor */

		  /* quad array is just a big square */
		  QuadArray plane = new QuadArray(4, GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);

		  /* add the points */
          Point3f p = new Point3f(floorsize,  floorheight, floorsize);
          plane.setCoordinate(0, p);
          p.set(-floorsize,  floorheight, floorsize);
          plane.setCoordinate(1, p);
          p.set(-floorsize,  floorheight, -floorsize);
          plane.setCoordinate(2, p);
          p.set(floorsize,  floorheight, -floorsize);
          plane.setCoordinate(3, p);

          float quality = 1.0f;

		  /* add the texture coordinates */
          Point2f q = new Point2f( 0.0f, floorsize*quality);
          plane.setTextureCoordinate(0, q);
          q.set(0.0f, 0.0f);
          plane.setTextureCoordinate(1, q);
          q.set( floorsize*quality, 0.0f);
          plane.setTextureCoordinate(2, q);
          q.set( floorsize*quality, floorsize*quality);
          plane.setTextureCoordinate(3, q);

		  /* create a new appearance for the floor */
          floorApp = new Appearance();
          floorApp.setCapability(Appearance.ALLOW_TEXTURE_READ);

		  /* load the floor texture */
          String filename = "models/grass.jpg";
          TextureLoader loader = new TextureLoader(filename, null);
          ImageComponent2D image = loader.getImage();

      if(image == null) {
            System.out.println("load failed for texture: "+filename);
      }

      // can't use parameterless constuctor
      Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,image.getWidth(), image.getHeight());
      texture.setCapability(Texture2D.ALLOW_ENABLE_READ);
      texture.setCapability(Texture2D.ALLOW_ENABLE_WRITE);

      texture.setImage(0, image);

      // set nicest quality for close textures (so they don't pixilate)
      texture.setMagFilter(Texture.NICEST);

      floorApp.setTexture(texture);

	  /* maje the floor green (so it looks nice when not using texture */
      floorApp.setColoringAttributes(new ColoringAttributes(0.4f,0.8f,0.4f,ColoringAttributes.FASTEST));

      PolygonAttributes polyAttrib = new PolygonAttributes();
      polyAttrib.setCullFace(PolygonAttributes.CULL_FRONT);
      polyAttrib.setBackFaceNormalFlip(true);
      floorApp.setPolygonAttributes(polyAttrib);

      //appear.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.FASTEST, 0.1f));

	  /* finally, add the floor quad to the scene */
      Shape3D planeObj = new Shape3D(plane, floorApp);
      group.addChild(planeObj);


	  /* create floor grid pattern (useful when not using floor texture */

            LineArray landGeom = new LineArray(88, GeometryArray.COORDINATES
                                            | GeometryArray.COLOR_3);
            float l = -floorsize;



            for(int c = 0; c < 88; c+=4)
            {

                landGeom.setCoordinate( c+0, new Point3f( -floorsize, floorheight+0.1f,  l ));
                landGeom.setCoordinate( c+1, new Point3f(  floorsize, floorheight+0.1f,  l ));
                landGeom.setCoordinate( c+2, new Point3f(   l   , floorheight+0.1f, -floorsize ));
                landGeom.setCoordinate( c+3, new Point3f(   l   , floorheight+0.1f,  floorsize ));
                l += 10.0f;
            }

        Color3f c = new Color3f(0.0f, 0.0f, 0.0f);
        for(int i = 0; i < 44; i++) landGeom.setColor( i, c);

		gridApp = new Appearance();
		RenderingAttributes rend = new RenderingAttributes();
		rend.setCapability(RenderingAttributes.ALLOW_VISIBLE_READ);
		rend.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
		gridApp.setRenderingAttributes(rend);
		gridApp.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
		gridApp.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
		gridApp.getRenderingAttributes().setVisible(false);

		grid = new Shape3D(landGeom,gridApp);
		//grid.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
		//grid.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);

		group.addChild(grid);

		/* add background */

        background = new Background();
        background.setCapability(Background.ALLOW_COLOR_WRITE);
        //background.setApplicationBounds(bounds);
        background.setApplicationBoundingLeaf(boundingLeaf);
        group.addChild(background);



        return group;
    }

	public void setBackGroundColour(Color color)
	{
            backgrdColour = color;

            float[] rgb = backgrdColour.getRGBColorComponents(null);


		background.setColor(rgb[0],rgb[1],rgb[2]);
		fog.setColor(rgb[0],rgb[1],rgb[2]);

//		Person3D[] p = world.getPeople3DArray();
//
//		for(int i=0;i<p.length;i++)
//		   if(p[i].getOId() != me.getOId())
//                       p[i].getNameText().setTextColour(1-rgb[0],1-rgb[1],1-rgb[2]);
	}

	public void setSmoothMovement(boolean b)
	{
		Person3D[] p = world.getPeople3DArray();

		for(int i=0;i<p.length;i++)
		   //if(p[i].getOId() != me.getOId())
		       p[i].setSmoothMovement(b);
	}

    private BranchGroup createAvatar(int avatarNum)
    {
        BranchGroup bg = new BranchGroup();

        BranchGroup body;

        body = (BranchGroup) avatars[avatarNum].cloneTree(true);

        Transform3D t3d = new Transform3D();
        t3d.setScale(sizes[avatarNum]);
        t3d.setTranslation(new Vector3d(0,ypos[avatarNum],0));

        TransformGroup pos = new TransformGroup(t3d);

        pos.addChild(body);

        bg.addChild(pos);

        bg.compile();

        return bg;
    }

    public void setAvatarNumber(int avatar)
    {
        this.avatar = avatar;
    }

    public void setTexturedFloorVisibility(boolean b)
    {
        floorApp.getTexture().setEnable(b);
        gridApp.getRenderingAttributes().setVisible(!b);
    }

    public void connect()
    {
        /* initilise our chatclient thread */
        chatClient = new ChatClientThread(this);


        /* check if it's connected to server */
        if(chatClient.isConnected())
        {
            chatClient.start(); //if so start it running and then send a login message
            chatClient.login(""+avatar);
        }
        else // else show a warning message
        {
        }

    }

	private void chatWindowAddSpeech(String name, String text)
	{
		this.addStringtoChatWindow("\n"+name+":","name");
		this.addStringtoChatWindow(text,"regular");
	}

	private void chatWindowAddShout(String name, String text)
	{
		this.addStringtoChatWindow("\n"+name+" shouts ","name");
		this.addStringtoChatWindow(text,"shout");
	}

        private void chatWindowAddWhisperFrom(String name, String text)
	{
		this.addStringtoChatWindow("\n"+name+" whispers to you:","whisper");
		this.addStringtoChatWindow(text,"regular");
	}

        private void chatWindowAddWhisperTo(String name, String text)
	{
		this.addStringtoChatWindow("\nYou whisper to "+name+":","whisper");
		this.addStringtoChatWindow(text,"regular");
	}

	private void chatWindowAddCommand(String text)
	{
		this.addStringtoChatWindow("\n"+text,"command");
	}

	private void addStringtoChatWindow(String message,String style)
	{
		Document doc = chatlist.getDocument();

		try
		{
		    doc.insertString(doc.getLength(),message,chatlist.getStyle(style));
		}
		catch (BadLocationException ble)
		{
            System.err.println("Couldn't insert text into chat window");
        }

		chatlist.setCaretPosition(doc.getLength());

	}

    public void disconnect()
    {
        if(chatClient != null && chatClient.isConnected())
            chatClient.finalise();
    }

	public void setPort(int port)
	{
		this.port = port;
	}

	public void setAlias(String alias)
	{
		this.alias = alias;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

    public void handleChat(String s)
    {

        if(s.startsWith("?ping"))
        {
            chatClient.sendMessage(PING);
            time = System.currentTimeMillis();
        }

        else if(s.toUpperCase().equals(s))
        {
          chatClient.sendMessage(SHOUT,s);
          this.chatWindowAddShout(me.getName(),s);
        }

        else
        {
          if(userlist.getSelectedIndex() != -1) // if someone's selected
          {
              // get the person..
              Person whisperto = world.getPersonWithName(userlist.getSelectedValue().toString());

              // if it's you tell yourself off!
              if(whisperto.getOId() == me.getOId())
              {
                  addStringtoChatWindow("\nYou cannot whisper to yourself!","whisper");
              }

              // if they're too far away print a warning!
               else if (whisperto.distance(me) > HEARINGRANGE)
              {
                  addStringtoChatWindow("\n"+whisperto.getName()+" is too far away!","whisper");
              }


              else
              {
                chatClient.sendMessage(WHISPER,whisperto.getOId()+SEPERATOR+s);
                this.chatWindowAddWhisperTo(whisperto.getName(),s);
              }
          }

          else
          {
            chatClient.sendMessage(SAY,s);
            this.chatWindowAddSpeech(me.getName(),s);
          }
        }

		chatbox.setText("");
    }
}