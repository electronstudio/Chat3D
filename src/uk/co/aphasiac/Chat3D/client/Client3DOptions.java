package uk.co.aphasiac.Chat3D.client;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

public class Client3DOptions extends JWindow
{
  Client3D parent;

  JLabel jLabel1 = new JLabel();
  TitledBorder titledBorder1;
  JCheckBox fogCheckBox = new JCheckBox();
  JLabel jLabel3 = new JLabel();
  JCheckBox namesCheckBox = new JCheckBox();
  JCheckBox floorCheckBox = new JCheckBox();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel5 = new JLabel();
  JButton closeButton = new JButton();
  JPanel jPanel1 = new JPanel();

  String[] colours = {"white","black","blue","red"};
  JComboBox skyComboBox = new JComboBox(colours);

  String[] views = {"default","further","whole world"};
  JComboBox viewComboBox = new JComboBox(views);

  JLabel skyColourLabel = new JLabel();

  int mousex,mousey;

  public Client3DOptions(Client3D parent) {
    this.parent = parent;

    this.setSize(450,300);

    this.addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e)
      {
          Point p = getLocation();

          p.x += (e.getX()-mousex);
          p.y += (e.getY()-mousey);

          setLocation(p);
      }
    });

    this.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e)
      {
          mousex = e.getX();
          mousey = e.getY();
      }
    });

    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    setHelpText();

    this.repaint();

  }
  private void jbInit() throws Exception {
    titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Detail Options");
    jLabel1.setText("Instructions");
    jLabel1.setBounds(new Rectangle(18, 12, 211, 25));
    this.getContentPane().setBackground(new Color(166, 202, 240));
    this.getContentPane().setLayout(null);

    fogCheckBox.setBackground(Color.lightGray);
    fogCheckBox.setBorder(null);
    fogCheckBox.setOpaque(false);
    fogCheckBox.setText("Fog");
    fogCheckBox.setBounds(new Rectangle(241, 62, 104, 25));
    fogCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e)
      {
          if(e.getStateChange() == ItemEvent.SELECTED)
            parent.fog.setDensity(0.6f);
          else  parent.fog.setDensity(0.0f);
      }
    });

    jLabel3.setText("View Distance");
    jLabel3.setBounds(new Rectangle(310, 200, 89, 23));
    namesCheckBox.setBackground(Color.lightGray);
    namesCheckBox.setBorder(null);
    namesCheckBox.setOpaque(false);
    namesCheckBox.setSelected(true);
    namesCheckBox.setText("Rotating User Names");
    namesCheckBox.setBounds(new Rectangle(241, 90, 144, 29));
    namesCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e)
      {
          boolean b = e.getStateChange() == ItemEvent.SELECTED;

          Person3D[] p = parent.world.getPeople3DArray();

          for (int i = 0; i < p.length; i++)
            if(p[i].getOId() != parent.me.getOId())
              p[i].getNameText().setVisibility(b);
      }
    });

    floorCheckBox.setBackground(Color.lightGray);
    floorCheckBox.setBorder(null);
    floorCheckBox.setOpaque(false);
    floorCheckBox.setSelected(true);
    floorCheckBox.setText("Floor Texture");
    floorCheckBox.setBounds(new Rectangle(241, 122, 154, 29));
    floorCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e)
      {
          if(e.getStateChange() == ItemEvent.SELECTED)
            parent.setTexturedFloorVisibility(true);
          else  parent.setTexturedFloorVisibility(false);
      }
    });

    jLabel2.setText("3D Options");
    jLabel2.setBounds(new Rectangle(241, 9, 143, 31));
    jLabel5.setText("Sky Colour");
    jLabel5.setBounds(new Rectangle(309, 159, 69, 20));

    /* button that sets background colour */

    closeButton.setBackground(Color.white);
    closeButton.setMargin(new Insets(0, 0, 0, 0));
    closeButton.setText("close");
    closeButton.setBounds(new Rectangle(402, 7, 41, 23));
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
          hide();
      }
    });

    jPanel1.setBackground(new Color(166, 202, 240));
    jPanel1.setBorder(BorderFactory.createRaisedBevelBorder());
    jPanel1.setBounds(new Rectangle(0, 0, this.getWidth(), this.getHeight()));


    skyComboBox.setBounds(new Rectangle(235, 158, 71, 23));
    skyComboBox.setLightWeightPopupEnabled(false);

    viewComboBox.setLightWeightPopupEnabled(false);
    viewComboBox.setBounds(new Rectangle(236, 197, 71, 22));
    viewComboBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {

          String view = (String)viewComboBox.getSelectedItem();

          double viewDistance = 10;

          if(view.equalsIgnoreCase("default"))
            viewDistance = 10;

          else if(view.equalsIgnoreCase("further"))
            viewDistance = 30;

          else if(view.equalsIgnoreCase("whole world"))
            viewDistance = 60;

          parent.canvas.getView().setBackClipDistance(viewDistance);




        }
    });

    skyComboBox.setLightWeightPopupEnabled(false);
    skyComboBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
          Color c = null;

          String skyColour = (String)skyComboBox.getSelectedItem();

          if(skyColour.equalsIgnoreCase("white"))
            c = Color.white;

          else if(skyColour.equalsIgnoreCase("black"))
            c = Color.black;

          else if(skyColour.equalsIgnoreCase("blue"))
            c = new Color(166,202,240);

          else if(skyColour.equalsIgnoreCase("red"))
            c = new Color(255,147,147);

          parent.setBackGroundColour(c);

          skyColourLabel.setBackground(c);

    }
});

    skyComboBox.setSelectedIndex(2);
    skyColourLabel.setBorder(BorderFactory.createLineBorder(Color.black));
    skyColourLabel.setOpaque(true);
    skyColourLabel.setBounds(new Rectangle(380, 155, 35, 31));
    jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    jScrollPane1.setBounds(new Rectangle(14, 43, 212, 248));
    jTextArea1.setEditable(false);
    jTextArea1.setLineWrap(true);
    renderingCheckbox.setOpaque(false);
    renderingCheckbox.setSelected(true);
    renderingCheckbox.setText("Low 3D rendering priority");
    renderingCheckbox.setBounds(new Rectangle(244, 232, 193, 24));
    renderingCheckbox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e)
      {
          if(e.getStateChange() == ItemEvent.SELECTED)
            parent.universe.setJ3DThreadPriority(Thread.currentThread().getPriority()-1);
          else  parent.universe.setJ3DThreadPriority(Thread.currentThread().getPriority());
      }
    });
    this.getContentPane().add(fogCheckBox, null);
    this.getContentPane().add(floorCheckBox, null);
    this.getContentPane().add(jLabel3, null);
    this.getContentPane().add(namesCheckBox, null);
    this.getContentPane().add(closeButton, null);
    this.getContentPane().add(viewComboBox, null);
    this.getContentPane().add(skyComboBox, null);
    this.getContentPane().add(jLabel5, null);
    this.getContentPane().add(skyColourLabel, null);
    this.getContentPane().add(jScrollPane1, null);
    this.getContentPane().add(jLabel2, null);
    this.getContentPane().add(jLabel1, null);
    this.getContentPane().add(renderingCheckbox, null);
    this.getContentPane().add(jPanel1, null);
    jScrollPane1.getViewport().add(jTextArea1, null);
  }

  private void setHelpText()
  {
    StringBuffer str = new StringBuffer("");

    str.append("Keys\n");
    str.append("Up = walk forwards\n");
    str.append("Down = walk backwards\n");
    str.append("Left = rotate left\n");
    str.append("Right = rotate right\n");
    str.append("Left + Alt = sidestep left\n");
    str.append("Right + Alt = sidestep right\n");
    str.append("Return = send current message\n");
    str.append("All other keys = chat\n");
    str.append("\n");
    str.append("View options\n");
    str.append("F1 = 1st person view\n");
    str.append("F2 = 3rd person view\n");
    str.append("F3 = above view");

    jTextArea1.append(str.toString());

  }

  public void show()
  {
      Point p = parent.getLocationOnScreen();
      p.x += (parent.getWidth()/2 - this.getWidth()/2);
      p.y += (parent.getHeight()/2 - this.getHeight()/2);

      this.setLocation(p);
      super.show();
  }
  JScrollPane jScrollPane1 = new JScrollPane();
  JTextArea jTextArea1 = new JTextArea();
  JCheckBox renderingCheckbox = new JCheckBox();

}