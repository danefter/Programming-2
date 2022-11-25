/**
 * @author Dan Jensen
 *
 * **/

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class Place extends Circle {

	private final String name;
	private final double x;
	private final double y;
	private final Text text;

	public Place(String name, double x, double y) {
		super (10, 10, 10);
		this.name = name;
		this.x = x;
		this.y = y;
		super.setCenterX(this.x);
		super.setCenterY(this.y);
		super.setId(this.name);
		super.setFill(Color.BLUE);
		this.text = new Text(this.name);
		text.setX(this.x);
		text.setY(this.y - 15.0);
	}



	public Text getText() {
		return text;
	}

	public String getName() {
		return name;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}


	//returns formatted location with name and x/y points
	@Override
	public String toString() {
		return String.format("Location: %s (%.1f %.1f)", name, x, y);
	}
}

