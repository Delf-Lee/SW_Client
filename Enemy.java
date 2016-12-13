import java.awt.Point;

public class Enemy {
	// 게임에 등장하는 적 캐릭터 관리 클래스
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
	int hitrange;// 다양한 크기의 적 캐릭터 수용을 위해 명중 판정 범위를 추가한다.
	Bullet bul;

	Enemy(MainFrame main, int img, int x, int y, int kind, int mode) {
		this.main = main;
		pos = new Point(x, y);
		_pos = new Point(x, y);
		dis = new Point(x / 100, y / 100);
		this.kind = kind;
		this.img = img;
		this.mode = mode;
		life = 6 + main.RAND(0, 5) * main.level;// 게임 레벨에 따라 라이프와 탄을 쏘는 시간이 짧아진다
		cnt = main.RAND(main.level * 5, 80);
		shoottype = main.RAND(0, 4);
		hitrange = 1500;
		switch (kind) {
		case 0:// 큐브 네우로이 전용 셋팅
			break;
		case 1:// 보스 전용 셋팅
			life = 400 + 300 * main.level;
			mode = 0;
			hitrange = 12000;
			break;
		case 2:// 위치 네우로이 전용 셋팅
			life = 20 + main.RAND(0, 10) * main.level;
			hitrange = 2000;
			cnt = -(main.RAND(30, 50));
			break;
		}
		/*
		if(kind==1){//보스일 경우 세부 파라미터를 재지정한다
			life=400+300*main.level;
			mode=0;
			hitrange=12000;
		}
		*/
	}

	// 적의 종류
	public static final int NOMAL_ENEMY = 0;
	public static final int BOSS = 1;
	public static final int NUROI_ENEMY = 2;

	// 공격 패턴
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

		// 우선은 공격
		switch (kind) {
		case NUROI_ENEMY:// 위치 네우로이의 경우
			if (mode != 4)
				break;
			if (cnt < 30 && cnt % 5 == 0) {
				bul = new Bullet(pos.x, pos.y, 2, 2, 90, 5);
				main.bullets.add(bul);
			}
			if (cnt > 50) {
				if (main.RAND(1, 100) < 50) {
					mode = 1;// 새로 이동할 방향을 결정
					cnt = 30;
				}
				else {
					mode = 5;// 퇴장
					cnt = 0;
				}
			}
			break;
		case NOMAL_ENEMY:// 일반 적 캐릭터일 경우
			switch (shoottype) {// 공격 형태에 따라 각기 다른 공격을 한다.
			case THREE_ATTACK:// 플레이어를 향해 3발을 점사한다
				if (cnt % 100 == 0 || cnt % 103 == 0 || cnt % 106 == 0) {// cnt로 공격 간격을 체크한다
					bul = new Bullet(pos.x, pos.y, 2, 2, main.getAngle(pos.x, pos.y, main.x[G.P1], main.y[G.P1]), 3);
					main.bullets.add(bul);
					
				}
				break;
			case FOUR_ATTACK:// 타이머에 맞춰 4방향탄을 발사한다
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
			case SINGLE_ATTACK:// 짧은 간격으로 플레이어 근처를 향해 한 발씩 발사한다
				if (cnt % 30 == 0 || cnt % 60 == 0 || cnt % 90 == 0 || cnt % 120 == 0 || cnt % 150 == 0 || cnt % 180 == 0) {
					bul = new Bullet(pos.x, pos.y, 2, 1, (main.getAngle(pos.x, pos.y, main.x[G.P1], main.y[G.P1]) + main.RAND(-20, 20)) % 360, 2);
					main.bullets.add(bul);
					
				}
				break;
			case TRIPLE_ATTACK:// 플레이어를 향해 3갈래탄을 발사한다
				if (cnt % 90 == 0 || cnt % 110 == 0 || cnt % 130 == 0) {
					bul = new Bullet(pos.x, pos.y, 2, 2, main.getAngle(pos.x, pos.y, main.x[G.P1], main.y[G.P1]), 2);
					main.bullets.add(bul);
					
					bul = new Bullet(pos.x, pos.y, 2, 2, (main.getAngle(pos.x, pos.y, main.x[G.P1], main.y[G.P1]) - 20) % 360, 2);
					main.bullets.add(bul);
					
					bul = new Bullet(pos.x, pos.y, 2, 2, (main.getAngle(pos.x, pos.y, main.x[G.P1], main.y[G.P1]) + 20) % 360, 2);
					main.bullets.add(bul);
					
				}
				break;
			case NONE:// 아무런 공격도 하지않는다
				break;
			}
			break;
		case BOSS:// 보스 캐릭터일 경우는, mode에 따라 공격 방식을 바꾼다.
			int lv, i;
			switch (mode) {// mode 값은 원래 움직임을 결정한다. 움직임에 맞춰 공격 방식도 바꿔준다.
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

		// 이동 처리
		switch (kind) {
		case NUROI_ENEMY:// 위치 네우로이의 경우
			/*
			위치 네우로이 이동 시나리오
			1. 오른쪽에서 등장하여 왼쪽으로 이동하다 화면 70~80% 정도 위치에 정지한다.
			2. 플레이어의 y좌표를 참조하여 상하로 일정 간격 이동하다
			3. 정지하여
			4. 손을 앞으로 내밀고 공격
			5. 2~4를 몇 번 수행한 후
			6. 뒤로 돌아 물러간다 
			*/
			switch (mode) {
			case FROM_LEFT:// 왼쪽에서 오른쪽으로 등장
				pos.x -= 500;
				if (cnt >= 0 && pos.x < main.screenWidth * 80) {
					mode = 1;
					cnt = 0;
				}
				break;
			case STOP_MOMENT:// 제자리에서 잠시 정지
				if (pos.x > main.screenWidth * 80) {// 만일 아직 충분한 x위치가 아니면 좀 더 왼쪽으로 이동한다
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
			case MOVE_DOWN:// 아래로 이동
				if (pos.y < main.screenHeight * 90 && cnt < 20)
					pos.y += 250;
				else {
					mode = 4;// 공격
					cnt = 0;
				}
				break;
			case MOVE_UP:// 위로 이동
				if (pos.y > 6400 && cnt < 20)
					pos.y -= 250;
				else {
					mode = 4;// 공격
					cnt = 0;
				}
				break;
			case EXIT_BACK:// 뒤로 돌아 퇴장
				pos.x += 350;
				break;
			case STOP_SHOT:// 정지해서 손을 앞으로 내밀고 총알 발사
				break;
			}
			break;

		case NOMAL_ENEMY:// 일반 캐릭터
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
		case BOSS:// 보스캐릭터
			/*
				보스 캐릭터 움직임 시나리오 (mode 값에 따라)
				0. 화면 우측에서 조금 안쪽으로 들어온다.
				1. 플레이어보다 위에 있으면 아래로(mode=2), 아래에 있으면 위로(mode=3) 일정 간격 (실제 좌표 기준으로 120도트) 이동한다.
				4. 화면 중앙까지 나온다
				5. 0.의 위치까지 되돌아간다.
				6. 화면 바깥으로 나가 사라진다.
				7. 잠시 그 자리에서 대기한 뒤 mode=1로 돌아간다
				8. 잠시 그 자리에서 대기한 뒤 mode=5로 전환한다
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
					_pos.y = pos.y;// 이동 거리를 체크하기 위해 이동이 시작되는 위치를 지정
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
