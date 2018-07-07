package uk.co.aphasiac.Chat3D.client;

import javax.swing.*;
import java.awt.*;

public class Client3DPanel extends JPanel
{
        Client3D display;
	Client3DFrontEnd frontend;

    public Client3DPanel(String filebase)
    {

		this.setLayout(new BorderLayout());

		 frontend = new Client3DFrontEnd(this,filebase);
                 display = new Client3D(this,filebase);

         this.add(frontend,BorderLayout.CENTER);
    }


	public void setClient3DFrame()
	{
		this.remove(frontend);
		this.add(display,BorderLayout.CENTER);
		this.validate();
	}

	public void disconnect()
	{
		display.disconnect();
        System.exit(0);
	}

}