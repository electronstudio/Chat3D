package uk.co.aphasiac.Chat3D.common;

public interface ChatEntity extends ChatVariables
{
	/* for each command type, a chat entity must have a method to handle it */

	public void say(Person p, String s);

	public void forwards(Person p, String s);

	public void backwards(Person p, String s);

	public void clockwise(Person p, String s);

	public void counterclockwise(Person p, String s);

	public void whisper(Person p, String s);

	public void login(Person p, String s);

	public void logout(Person p, String s);

	public void ping(Person p, String s);

	public void world(Person p, String s);

	public void shout(Person p, String s);

	public void sync(Person p, String s);

	public void right(Person p, String s);

	public void left(Person p, String s);

}