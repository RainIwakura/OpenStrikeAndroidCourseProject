
package mobile.openstrike.game;

public class Player {
	
	int health;
	String name;
	String team;
	String type;
	
	public Player(String name) {
		this.name = name;
		health = 100;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}

	public Player(String name, String team) {
		this.name = name;
		this.team = team;
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
