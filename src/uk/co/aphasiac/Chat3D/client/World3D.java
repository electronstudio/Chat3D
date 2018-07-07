package uk.co.aphasiac.Chat3D.client;

import javax.media.j3d.*;

import uk.co.aphasiac.Chat3D.common.*;

public class World3D extends World
{

  /* this branchgroup holds all the 3D objects in the display */
  BranchGroup scene;

  public World3D(int maxsize, BranchGroup scene)
  {
      super(maxsize);

      collisionDetection = true;

      /* sets the branchgroup - w'll assume the correct capability bits have been set and that
         it's been compiled */
      this.scene = scene;
  }

  public World3D(int maxsize, BranchGroup scene, boolean collisionDetection)
  {
      super(maxsize);

      this.collisionDetection = collisionDetection;

      /* sets the branchgroup - w'll assume the correct capability bits have been set and that
         it's been compiled */
      this.scene = scene;
  }

  /* to add a person to our 3D world we add them as normal..*/
  public void addPerson(Person3D p)
  {
      /* like so..*/
      super.addPerson(p);

      /* but we must also add their transformgroup to the scene */
      scene.addChild(p.getBranchGroup());

  }

  /* adds the main user person to the scene (doesn't need to add his geometry, as their geometry is
     the camera and that's already been added */
  public void addMe(Person3D p)
  {
      super.addPerson(p);
  }

  public Person3D[] getPeople3DArray()
  {
		Person3D[] pArray = new Person3D[people.size()];

		for(int i=0; i<people.size(); i++)
		  pArray[i] = (Person3D) people.elementAt(i);

		return pArray;
  }

  /* to remove a person we remove them as normal.. */
  public boolean removePerson(int id)
  {
    Person3D p;

    for (int i=0;i<people.size();i++)
      {
          p = (Person3D) people.elementAt(i);

          if(p.getOId() == id)
          {
              /* like this */
              people.removeElementAt(i);

              /* but we must also delete them from the 3D scene by detaching it's parent branchGroup*/
              p.getBranchGroup().detach();

              return true;
          }
      }

      return false;


  }
}