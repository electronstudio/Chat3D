package uk.co.aphasiac.Chat3D.client;

import com.sun.j3d.loaders.vrml97.VrmlLoader;
import com.sun.j3d.utils.universe.*;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;


public class AvatarViewer extends JPanel
{
    private Canvas3D canvas;
    private SimpleUniverse universe;
    private BranchGroup scene;
    private TransformGroup rotGroup;
    private TransformGroup scaleGroup;
    private Alpha rotationAlpha;

    private BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0),100);

    private BranchGroup[] avatars;
    private BranchGroup[] avatarTemplates;
    private double[] sizes;
    private double[] ypos;
    private double[] rotations;

    private int currentAvatar = 0;

	JButton forwards;
	JButton backwards;

    public AvatarViewer(String[] filenames, double[] scales,double[] ys,double[] rot)
	{
		sizes = scales;
		ypos = ys;
                rotations = rot;
		avatars = new BranchGroup[filenames.length];
                avatarTemplates = new BranchGroup[filenames.length];

	    /* first lets create all of the avatars */
		for(int i=0;i<filenames.length;i++)
		{
			VrmlLoader newvrml = new VrmlLoader();
                        try
                        {
                                Transform3D t3d = new Transform3D();
                                t3d.rotY(2*Math.PI*rotations[i]);
                                TransformGroup trans = new TransformGroup(t3d);

                                // load the avatar geometry
                                trans.addChild(newvrml.load("models/"+filenames[i]).getSceneGroup());

                                BranchGroup bg = new BranchGroup();
                                bg.addChild(trans);

                                avatarTemplates[i] = bg;
				avatarTemplates[i].setCapability(BranchGroup.ALLOW_DETACH);

                                avatars[i] = (BranchGroup) avatarTemplates[i].cloneTree(true);
                        }
                        catch(FileNotFoundException e)
                        {
				System.out.println("vrml file "+filenames[i]+"not found");
                        }

		}

		/* create a canvas 3D */
        canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());

        /* create a simple universe */
        universe = new SimpleUniverse(canvas);
//		System.out.println("j3d thread priority="+universe.getJ3DThreadPriority());
		universe.setJ3DThreadPriority(Thread.currentThread().getPriority()-1);

        /* create the contents branch group */
        scene = new BranchGroup();

        /* set panel's layout manager to border layout */
        this.setLayout(new BorderLayout());

        /* add canvas to this panel */
        this.add(canvas,BorderLayout.CENTER);

		Panel buttons = new Panel();
		buttons.setLayout(new GridLayout(0,2,2,2));

		forwards = new JButton("Next");
		forwards.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				avatars[currentAvatar].detach();

				currentAvatar++;
				backwards.setEnabled(true);

				if(currentAvatar+1 == avatars.length)
				{
					forwards.setEnabled(false);
					//backwards.requestFocus();
				}

				setAvatar();
			}
			});

		backwards = new JButton("Previous");
		backwards.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				avatars[currentAvatar].detach();

				currentAvatar--;
				forwards.setEnabled(true);

				if(currentAvatar == 0)
				{
				    backwards.setEnabled(false);
					//forwards.requestFocus();
				}

				setAvatar();

			}
			});

		buttons.add(backwards);
		buttons.add(forwards);

		backwards.setEnabled(false);

		this.add(buttons,BorderLayout.SOUTH);

		/* now lets create a rotation interpolator, to rotate everything */
        /* create a group to perform the rotation, with correct capabilities */
        rotGroup = new TransformGroup();
        rotGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		rotGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
		rotGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);

        /* create an alpha to drive the rotation */
        rotationAlpha = new Alpha(-1,3000);
        /* crerate a little delay before the alpha starts (so the avatar doesn't start rotating
           instantly) */
        rotationAlpha.setTriggerTime(500);

        /* create a rotation interpolator to actually perform the rotation */
        RotationInterpolator rotator = new RotationInterpolator(rotationAlpha,rotGroup,new Transform3D(),0.0f,(float) Math.PI*2);

        /* add the rotation interpolator to the scene */
        rotGroup.addChild(rotator);

        /* add a bounding sphere to trigger the behaviour */
        rotator.setSchedulingBounds(bounds);

		/* now lets create a transform group top set the scale of the avatar*/
		scaleGroup = new TransformGroup();
		scaleGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		scaleGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		/*add rotation group to this one */
		scaleGroup.addChild(rotGroup);

		/* add scale group to the scene */
		scene.addChild(scaleGroup);

		setAvatar();


        /* add some lights to the scene */
        // Set up the directional lights
        Color3f light1Color = new Color3f(1.0f, 1.0f, 0.9f);
        Vector3f light1Direction  = new Vector3f(4.0f, -7.0f, -12.0f);
        Color3f light2Color = new Color3f(0.3f, 0.3f, 0.4f);
        Vector3f light2Direction  = new Vector3f(-6.0f, -2.0f, -1.0f);

        DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
        light1.setInfluencingBounds(bounds);
        scene.addChild(light1);  // add light to branch group

        DirectionalLight light2 = new DirectionalLight(light2Color, light2Direction);
        light2.setInfluencingBounds(bounds);
        scene.addChild(light2);  // add light to branch group

        /* compile the scene for efficientcy */
        scene.compile();

        /* finally add the scene to the universe */
        universe.addBranchGraph(scene);
    }

	private void setAvatar()
	{
		Transform3D t3d = new Transform3D();

                t3d.rotY(Math.PI);
		t3d.setScale(sizes[currentAvatar]);
		t3d.setTranslation(new Vector3d(0,ypos[currentAvatar],-3));

		scaleGroup.setTransform(t3d);

		rotGroup.addChild(avatars[currentAvatar]);

		rotationAlpha.setStartTime(System.currentTimeMillis());
	}

	public BranchGroup getCurrentAvatar()
	{
		return avatarTemplates[currentAvatar];
	}

        public int getCurrentAvatarNumber()
        {
            return currentAvatar;
        }

        public BranchGroup[] getAvatars()
        {
            return avatarTemplates;
        }

	public Transform3D getCurrentTransform()
	{
		Transform3D t = new Transform3D();

		scaleGroup.getTransform(t);

		return t;
	}

        public void dettachAllAvatars()
        {
            avatars[currentAvatar].detach();
        }

	public static void main(String[] args)
	{
		JFrame frame = new JFrame("My Avatar Viewer");

		String[] filenames = {"maggie.wrl","yoda.wrl","ant.wrl","legoman.wrl","robo.wrl"};
		double[] sizes =     {   1.0,         1.5,       1.2,      0.03,         1.0};
		double[] y_offset =  {   0.0,         0.0,      -1.0,      0.5,          0.5};
                double[] rotations = {   0.0,         0.5,       1.0,      0.5,          0.5};


		 frame.getContentPane().add(new AvatarViewer(filenames,sizes,y_offset,rotations),BorderLayout.CENTER);

		 frame.setSize(300,300);

		 frame.show();

	}
}