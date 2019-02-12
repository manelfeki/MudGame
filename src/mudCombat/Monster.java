package mudCombat;

import java.io.Serializable;

public class Monster implements Serializable {

	private int inventory;
	private String piece;

	public Monster(String piece) {
		this.inventory = 5;
		this.piece = piece;
	}

	public int getInventory() {
		return inventory;
	}

	public void setInventory(int inventory) {
		this.inventory = inventory;
	}

}
