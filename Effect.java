import java.awt.Point;

class Effect {
	// 폭발 등의 이펙트 관리 클래스
	Point pos;
	Point _pos;
	Point dis;
	int img;
	int kind;
	int cnt;

	Effect(int img, int x, int y, int kind) {
		pos = new Point(x, y);
		_pos = new Point(x, y);
		dis = new Point(x / 100, y / 100);
		this.kind = kind;
		this.img = img;
		cnt = 16;
	}
}