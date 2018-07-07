package uk.co.aphasiac.Chat3D.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Client3DFrame extends JFrame
{
	Client3DPanel client;

    public Client3DFrame()
	{
	        client = new Client3DPanel("file:");

			this.getContentPane().add(client,BorderLayout.CENTER);

			this.setSize(640,480);

			this.setTitle("3D Chat Client");

			this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            {
                  client.disconnect();
                  System.exit(0);
            }
        });

			this.show();
    }

	public static void main(String[] args)
    {
            new Client3DFrame();
    }
}