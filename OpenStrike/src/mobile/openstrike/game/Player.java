
package mobile.openstrike.game;

public class Player {
	int health;
	String name;
	
	public Player() {
		health = 100;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isAlive() {
		return (health>0);
	}

}
