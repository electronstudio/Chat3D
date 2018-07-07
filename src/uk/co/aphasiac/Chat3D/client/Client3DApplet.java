package uk.co.aphasiac.Chat3D.client;

import javax.swing.*;

/*
<applet code="Client3D.class" width=640 height=480>
</applet>
*/

public class Client3DApplet extends JApplet
{
	Client3DPanel client3D;


    public Client3DApplet()
	{
		client3D = new Client3DPanel(this.getDocumentBase().toString());

		this.getContentPane().add(client3D);
    }

	public void destroy()
	{
        super.destroy();

		client3D.disconnect();
    }


}