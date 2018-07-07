package uk.co.aphasiac.Chat3D.client;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Client3DFrontEnd extends JPanel {

        String[] filenames = {"maggie.wrl","yoda.wrl","ant.wrl","legoman.wrl","robo.wrl"};
	double[] sizes =     {   1.0,         1.5,       1.0,      0.03,         1.0};
	double[] ypos =      {   0.0,         0.0,      -1.0,      0.5,          0.5};
        double[] rotations = {   0.0,         0.5,       0.0,      0.5,          0.25};

	String codebase;

	Client3DPanel parent;

	AvatarViewer avatarviewer = new AvatarViewer(filenames,sizes,ypos,rotations);

    BorderLayout borderLayout1 = new BorderLayout();
    JTextField nameField = new JTextField();
    JLabel jLabel1 = new JLabel();
    JTextField hostField = new JTextField();
    JLabel jLabel2 = new JLabel();
    JTextField portField = new JTextField();
    JLabel jLabel3 = new JLabel();
    JButton connectbutton = new JButton();
    TitledBorder titledBorder1;
    TitledBorder titledBorder2;
    JCheckBox jCheckBox1 = new JCheckBox();
    JLabel jLabel6 = new JLabel();
    JLabel jLabel7 = new JLabel();

    public Client3DFrontEnd(Client3DPanel p,String codebase)
	{
	   this.codebase = codebase;
		parent = p;

		try {

            jbInit();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    void jbInit() throws Exception {
        titledBorder1 = new TitledBorder("");
        titledBorder2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(142, 142, 142)),"hkbghki");
        hostField.setText("localhost");
        hostField.setBounds(new Rectangle(105, 344, 141, 23));
        jLabel1.setBackground(Color.white);
        jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel1.setHorizontalTextPosition(SwingConstants.RIGHT);
        jLabel1.setText("Username");
        jLabel1.setBounds(new Rectangle(17, 77, 74, 25));

		avatarviewer.setBounds(new Rectangle(300, 20, 250, 250));

        nameField.setBounds(new Rectangle(95, 77, 97, 25));
        this.setLayout(null);
        //this.setLayout(borderLayout1);
        jLabel2.setText("Server Address");
        jLabel2.setBounds(new Rectangle(13, 340, 91, 28));
        portField.setText("7843");
        portField.setBounds(new Rectangle(104, 373, 41, 25));

        jLabel3.setText("Port");
        jLabel3.setBounds(new Rectangle(72, 369, 70, 24));

        connectbutton.setBackground(Color.white);
    connectbutton.setText("Connect");
        connectbutton.setBounds(new Rectangle(459, 401, 98, 28));
		connectbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
			{

                                if(nameField.getText().length() == 0)
                                {
                                    JOptionPane.showMessageDialog(null,"Please enter a username!!!","Warning!",JOptionPane.ERROR_MESSAGE);
                                    return;
                                }

                                if(hostField.getText().length() == 0)
                                {
                                    JOptionPane.showMessageDialog(null,"Please enter a valid server address!!","Warning!",JOptionPane.ERROR_MESSAGE);
                                    return;
                                }

                                if(portField.getText().length() == 0)
                                {
                                    JOptionPane.showMessageDialog(null,"Please enter a valid port number!!","Warning!",JOptionPane.ERROR_MESSAGE);
                                    return;
                                }

                                parent.display.setAlias(nameField.getText());

				parent.display.setPort(Integer.parseInt(portField.getText()));
				parent.display.setHost(hostField.getText());
                                parent.display.setAvatarNumber(avatarviewer.getCurrentAvatarNumber());

				parent.setClient3DFrame();

                                avatarviewer.dettachAllAvatars();

				parent.display.connect();
			}
		});


        this.setBackground(new Color(166, 202, 240));
    this.setMaximumSize(new Dimension(640, 480));

		ButtonGroup buttongroup1 = new ButtonGroup();
		jCheckBox1.setOpaque(false);
    jCheckBox1.setText("Change Server details");
        jCheckBox1.setBounds(new Rectangle(94, 313, 166, 20));
        jCheckBox1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
			{
                if(jCheckBox1.isSelected())
				{
					hostField.setEnabled(true);
					portField.setEnabled(true);
					jLabel2.setEnabled(true);
					jLabel3.setEnabled(true);
				}
				else
				{
					hostField.setEnabled(false);
					portField.setEnabled(false);
					jLabel2.setEnabled(false);
					jLabel3.setEnabled(false);
				}
            }
        });
		hostField.setEnabled(false);
		portField.setEnabled(false);
		jLabel2.setEnabled(false);
		jLabel3.setEnabled(false);

        jLabel6.setForeground(Color.black);
        jLabel6.setText("User Details");
        jLabel6.setBounds(new Rectangle(33, 41, 171, 25));
        jLabel7.setText("Please Select an Avatar");
        jLabel7.setBounds(new Rectangle(344, 279, 148, 27));


        this.add(avatarviewer,null);
        this.add(hostField, null);
        this.add(portField, null);
        this.add(jLabel2, null);
        this.add(jLabel3, null);
        this.add(jCheckBox1, null);
        this.add(connectbutton, null);
    this.add(nameField, null);
    this.add(jLabel6, null);
    this.add(jLabel1, null);
    this.add(jLabel7, null);

    }

}
