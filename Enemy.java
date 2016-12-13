import java.awt.Point;

public class Enemy {
	// ���ӿ� �����ϴ� �� ĳ���� ���� Ŭ����
	MainFrame main;
	Point pos;
	Point _pos;
	Point dis;
	int img;
	int kind;
	int life;
	int mode;
	int cnt;
	int shoottype;
	int hitrange;// �پ��� ũ���� �� ĳ���� ������ ���� ���� ���� ������ �߰��Ѵ�.
	Bullet bul;

	Enemy(MainFrame main, int img, int x, int y, int kind, int mode) {
		this.main = main;
		pos = new Point(x, y);
		_pos = new Point(x, y);
		dis = new Point(x / 100, y / 100);
		this.kind = kind;
		this.img = img;
		this.mode = mode;
		life = 6 + main.RAND(0, 5) * main.level;// ���� ������ ���� �������� ź�� ��� �ð��� ª������
		cnt = main.RAND(main.level * 5, 80);
		shoottype = main.RAND(0, 4);
		hitrange = 1500;
		switch (kind) {
		case 0:// ť�� �׿���� ���� ����
			break;
		case 1:// ���� ���� ����
			life = 400 + 300 * main.level;
			mode = 0;
			hitrange = 12000;
			break;
		case 2:// ��ġ �׿���� ���� ����
			life = 20 + main.RAND(0, 10) * main.level;
			hitrange = 2000;
			cnt = -(main.RAND(30, 50));
			break;
		}
		/*
		if(kind==1){//������ ��� ���� �Ķ���͸� �������Ѵ�
			life=400+300*main.level;
			mode=0;
			hitrange=12000;
		}
		*/
	}

	// ���� ����
	public static final int NOMAL_ENEMY = 0;
	public static final int BOSS = 1;
	public static final int NUROI_ENEMY = 2;

	// ���� ����
	public static final int THREE_ATTACK = 0;
	public static final int FOUR_ATTACK = 1;
	public static final int SINGLE_ATTACK = 2;
	public static final int TRIPLE_ATTACK = 3;
	public static final int NONE = 4;

	public final static int FROM_LEFT = 0;
	public final static int STOP_MOMENT = 1;
	public final static int MOVE_DOWN = 2;
	public final static int MOVE_UP = 3;
	public final static int STOP_SHOT = 4;
	public final static int EXIT_BACK = 5;

	public boolean move() {
		boolean ret = true;

		// �켱�� ����
		switch (kind) {
		case NUROI_ENEMY:// ��ġ �׿������ ���
			if (mode != 4)
				break;
			if (cnt < 30 && cnt % 5 == 0) {
				bul = new Bullet(pos.x, pos.y, 2, 2, 90, 5);
				main.bullets.add(bul);
			}
			if (cnt > 50) {
				if (main.RAND(1, 100) < 50) {
					mode = 1;// ���� �̵��� ������ ����
					cnt = 30;
				}
				else {
					mode = 5;// ����
					cnt = 0;
				}
			}
			break;
		case NOMAL_ENEMY:// �Ϲ� �� ĳ������ ���
			switch (shoottype) {// ���� ���¿� ���� ���� �ٸ� ������ �Ѵ�.
			case THREE_ATTACK:// �÷��̾ ���� 3���� �����Ѵ�
				if (cnt % 100 == 0 || cnt % 103 == 0 || cnt % 106 == 0) {// cnt�� ���� ������ üũ�Ѵ�
					bul = new Bullet(pos.x, pos.y, 2, 2, main.getAngle(pos.x, pos.y, main.x[G.P1], main.y[G.P1]), 3);
					main.bullets.add(bul);
					
				}
				break;
			case FOUR_ATTACK:// Ÿ�̸ӿ� ���� 4����ź�� �߻��Ѵ�
				if (cnt % 90 == 0 || cnt % 100 == 0 || cnt % 110 == 0) {
					bul = new Bullet(pos.x, pos.y, 2, 2, (0 + (cnt % 36) * 10) % 360, 3);
					main.bullets.add(bul);
					
					bul = new Bullet(pos.x, pos.y, 2, 2, (30 + (cnt % 36) * 10) % 360, 3);
					main.bullets.add(bul);
					
					bul = new Bullet(pos.x, pos.y, 2, 2, (60 + (cnt % 36) * 10) % 360, 3);
					main.bullets.add(bul);
					
					bul = new Bullet(pos.x, pos.y, 2, 2, (90 + (cnt % 36) * 10) % 360, 3);
					main.bullets.add(bul);
					
				}
				break;
			case SINGLE_ATTACK:// ª�� �������� �÷��̾� ��ó�� ���� �� �߾� �߻��Ѵ�
				if (cnt % 30 == 0 || cnt % 60 == 0 || cnt % 90 == 0 || cnt % 120 == 0 || cnt % 150 == 0 || cnt % 180 == 0) {
					bul = new Bullet(pos.x, pos.y, 2, 1, (main.getAngle(pos.x, pos.y, main.x[G.P1], main.y[G.P1]) + main.RAND(-20, 20)) % 360, 2);
					main.bullets.add(bul);
					
				}
				break;
			case TRIPLE_ATTACK:// �÷��̾ ���� 3����ź�� �߻��Ѵ�
				if (cnt % 90 == 0 || cnt % 110 == 0 || cnt % 130 == 0) {
					bul = new Bullet(pos.x, pos.y, 2, 2, main.getAngle(pos.x, pos.y, main.x[G.P1], main.y[G.P1]), 2);
					main.bullets.add(bul);
					
					bul = new Bullet(pos.x, pos.y, 2, 2, (main.getAngle(pos.x, pos.y, main.x[G.P1], main.y[G.P1]) - 20) % 360, 2);
					main.bullets.add(bul);
					
					bul = new Bullet(pos.x, pos.y, 2, 2, (main.getAngle(pos.x, pos.y, main.x[G.P1], main.y[G.P1]) + 20) % 360, 2);
					main.bullets.add(bul);
					
				}
				break;
			case NONE:// �ƹ��� ���ݵ� �����ʴ´�
				break;
			}
			break;
		case BOSS:// ���� ĳ������ ����, mode�� ���� ���� ����� �ٲ۴�.
			int lv, i;
			switch (mode) {// mode ���� ���� �������� �����Ѵ�. �����ӿ� ���� ���� ��ĵ� �ٲ��ش�.
			case 5:
				if (main.level >= 10)
					lv = 5;
				else
					lv = (10 - main.level) * 5;
				if (cnt % lv == 0 || cnt % (lv + 5) == 0 || cnt % (lv + 15) == 0) {
					for (i = 0; i < 4 + (50 - lv) / 5; i++) {
						bul = new Bullet(pos.x, pos.y, 2, 2, (30 * i + (cnt % 36) * 10) % 360, 5);
						main.bullets.add(bul);
						
					}
					/*bul=new Bullet(pos.x, pos.y, 2, 1, (30+(cnt%36)*10)%360, 4);
					main.bullets.add(bul);
					bul=new Bullet(pos.x, pos.y, 2, 1, (60+(cnt%36)*10)%360, 4);
					main.bullets.add(bul);
					bul=new Bullet(pos.x, pos.y, 2, 1, (90+(cnt%36)*10)%360, 4);
					main.bullets.add(bul);*/
				}
				break;
			case 7:
				if (main.level >= 10)
					lv = 1;
				else
					lv = 10 - main.level;
				if (cnt % lv == 0) {
					bul = new Bullet(pos.x - 3000 + main.RAND(-10, +10) * 100, pos.y + main.RAND(10, 80) * 100, 2, 2, 90, 5 + (10 - lv) / 2);
					main.bullets.add(bul);
				
				}
				break;
			}
			break;
		}

		// �̵� ó��
		switch (kind) {
		case NUROI_ENEMY:// ��ġ �׿������ ���
			/*
			��ġ �׿���� �̵� �ó�����
			1. �����ʿ��� �����Ͽ� �������� �̵��ϴ� ȭ�� 70~80% ���� ��ġ�� �����Ѵ�.
			2. �÷��̾��� y��ǥ�� �����Ͽ� ���Ϸ� ���� ���� �̵��ϴ�
			3. �����Ͽ�
			4. ���� ������ ���а� ����
			5. 2~4�� �� �� ������ ��
			6. �ڷ� ���� �������� 
			*/
			switch (mode) {
			case FROM_LEFT:// ���ʿ��� ���������� ����
				pos.x -= 500;
				if (cnt >= 0 && pos.x < main.screenWidth * 80) {
					mode = 1;
					cnt = 0;
				}
				break;
			case STOP_MOMENT:// ���ڸ����� ��� ����
				if (pos.x > main.screenWidth * 80) {// ���� ���� ����� x��ġ�� �ƴϸ� �� �� �������� �̵��Ѵ�
					mode = 0;
					break;
				}
				if (cnt >= 30) {
					if (pos.y > main.y[G.P1])
						mode = 3;
					else
						mode = 2;
					cnt = 0;
				}
				break;
			case MOVE_DOWN:// �Ʒ��� �̵�
				if (pos.y < main.screenHeight * 90 && cnt < 20)
					pos.y += 250;
				else {
					mode = 4;// ����
					cnt = 0;
				}
				break;
			case MOVE_UP:// ���� �̵�
				if (pos.y > 6400 && cnt < 20)
					pos.y -= 250;
				else {
					mode = 4;// ����
					cnt = 0;
				}
				break;
			case EXIT_BACK:// �ڷ� ���� ����
				pos.x += 350;
				break;
			case STOP_SHOT:// �����ؼ� ���� ������ ���а� �Ѿ� �߻�
				break;
			}
			break;

		case NOMAL_ENEMY:// �Ϲ� ĳ����
			switch (mode) {
			case FROM_LEFT:
				pos.x -= 500;
				pos.y += 80;
				if (pos.x < main.x[G.P1])
					mode = 2;
				break;
			case STOP_MOMENT:
				pos.x -= 500;
				pos.y -= 80;
				if (pos.x < main.x[G.P1])
					mode = 3;
				break;
			case MOVE_DOWN:
				pos.x += 600;
				pos.y += 240;
				break;
			case MOVE_UP:
				pos.x += 600;
				pos.y -= 240;
				break;
			}
			break;
		case BOSS:// ����ĳ����
			/*
				���� ĳ���� ������ �ó����� (mode ���� ����)
				0. ȭ�� �������� ���� �������� ���´�.
				1. �÷��̾�� ���� ������ �Ʒ���(mode=2), �Ʒ��� ������ ����(mode=3) ���� ���� (���� ��ǥ �������� 120��Ʈ) �̵��Ѵ�.
				4. ȭ�� �߾ӱ��� ���´�
				5. 0.�� ��ġ���� �ǵ��ư���.
				6. ȭ�� �ٱ����� ���� �������.
				7. ��� �� �ڸ����� ����� �� mode=1�� ���ư���
				8. ��� �� �ڸ����� ����� �� mode=5�� ��ȯ�Ѵ�
			*/
			if (main.gameCnt == 1200)
				mode = 4;
			if (main.gameCnt == 2210)
				mode = 6;
			
			switch (mode) {
			case 0:
				pos.x -= 100;
				if (pos.x < 53000)
					mode = 1;
				break;
			case 1:
				if (cnt % 30 == 0) {
					if (pos.y > main.y[G.P1])
						mode = 3;
					else if (pos.y < main.y[G.P1])
						mode = 2;
					_pos.x = pos.x;
					_pos.y = pos.y;// �̵� �Ÿ��� üũ�ϱ� ���� �̵��� ���۵Ǵ� ��ġ�� ����
				}
				break;
			case 2:
				if (pos.y + 400 < 42000)
					pos.y += 400;
				else {
					cnt = 0;
					mode = 7;
				}
				if (pos.y - _pos.y > 12000) {
					cnt = 0;
					mode = 7;
				}
				break;
			case 3:
				if (pos.y - 400 > 6000)
					pos.y -= 400;
				else {
					cnt = 0;
					mode = 7;
				}
				if (_pos.y - pos.y > 12000) {
					cnt = 0;
					mode = 7;
				}
				break;
			case 4:
				pos.x -= 800;
				if (pos.x < 30000) {
					mode = 8;
					cnt = 0;
				}
				break;
			case 5:
				pos.x += 350;
				if (pos.x > 53000)
					mode = 1;
				break;
			case 6:
				pos.x += 500;
				break;
			case 7:
				if (cnt > 100)
					mode = 1;
				break;
			case 8:
				if (cnt > 90)
					mode = 5;
				break;
			}
			break;
		}
		dis.x = pos.x / 100;
		dis.y = pos.y / 100;
		if (dis.x < 0 || dis.x > 640 || dis.y < 0 || dis.y > 480)
			ret = false;

		cnt++;
		return ret;
	}
}
