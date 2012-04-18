package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "de.fu_berlin.imp.seqan.usability_analyzer.srv")
public class Rectangle {
	private int x;
	private int y;
	private int width;
	private int height;

	public Rectangle() {
	}

	public Rectangle(int x, int y, int width, int height) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@XmlAttribute
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	@XmlAttribute
	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@XmlAttribute
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@XmlAttribute
	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + width;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rectangle other = (Rectangle) obj;
		if (height != other.height)
			return false;
		if (width != other.width)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Bounds(x=" + x + ",y=" + y + ",width=" + width + ",height="
				+ height + ")";
	}

}
