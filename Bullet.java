import java.awt.Point;

public class Bullet {
	// 게임에 등장하는 총알을 처리하기 위한 클래스
	// 메모리 효율을 위해서는 총알에 관한 최소한의 정보만 담는 것이 좋지만, 처리 샘플을 위해 간단한 자체 처리 루틴을 포함한다.
	Point dis;// 총알의 표시 좌표. 실제 좌표보다 *100 상태이다.
	Point pos;// 총알의 계산 좌표. 실제 좌표보다 *100 상태이다.
	Point _pos;// 총알의 직전 좌표
	int degree;// 총알의 진행 방향 (각도)
	// 총알의 진행 방향은 x, y 증가량으로도 표시 가능하다. 하지만 그 경우 정밀한 탄막 구현이 어려워진다.
	int speed;// 총알의 이동 속도
	int img_num;// 총알의 이미지 번호
	int from;// 총알을 누가 발사했는가

	Bullet(int x, int y, int img_num, int from, int degree, int speed) {
		pos = new Point(x, y);
		dis = new Point(x / 100, y / 100);
		_pos = new Point(x, y);
		this.img_num = img_num;
		this.from = from;
		this.degree = degree;
		this.speed = speed;
	}

	public void move() {
		_pos = pos;// 이전 좌표 보존
		pos.x -= (speed * Math.sin(Math.toRadians(degree)) * 100);
		pos.y -= (speed * Math.cos(Math.toRadians(degree)) * 100);
		dis.x = pos.x / 100;
		dis.y = pos.y / 100;
		// if(pos.x<0||pos.y>gScreenWidth*100||pos.y<0||pos.y>gScreenHeight*100) ebullet[i].pic=255;
	}
}

