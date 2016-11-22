import java.awt.Point;

public class Item {
	// 아이템 관리 클래스
	Point pos;
	Point dis;
	int speed;
	int cnt;
	int kind;

	Item(int x, int y, int kind) {
		this.kind = kind;
		pos = new Point(x, y);
		dis = new Point(x / 100, y / 100);
		speed = -200;
		cnt = 0;
	}

	public boolean move() {
		boolean ret = false;
		pos.x -= speed;
		cnt++;
		if (cnt >= 50)
			speed = 200;
		else if (cnt >= 30)
			speed = 100;
		else if (cnt >= 20)
			speed = -100;
		dis.x = pos.x / 100;
		if (pos.x < 0)
			ret = true;
		return ret;
	}
}

