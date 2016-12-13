import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

public class GameScreen extends Canvas {
	// 실제 게임의 메인 제어를 행하는 클래스
	// 가급적 화면 출력에서는 게임에서 공통으로 쓰이는 변수값의 변경 등을 행하지 않는 것이 좋다.
	MainFrame main;

	int cnt, gamecnt;

	// 화면 그리기용 변수
	int x, y;// 플레이어 캐릭터의 좌표

	Image dblbuff;// 더블버퍼링용 백버퍼
	Graphics gc;// 더블버퍼링용 그래픽 컨텍스트

	Image bg, bg_f, bgFlip;// 배경화면 //2013-10
	Image cloud[] = new Image[5];// 구름
	Image title, title_key;// 타이틀화면

	Image chr[] = new Image[9];// 플레이어 캐릭터
	Image enemy[] = new Image[5];// 적 캐릭터
	Image bullet[] = new Image[5];// 총알
	Image explo;// 폭염
	Image item[] = new Image[3];// 아이템

	Image num;// 2013-10
	Image uiUp;// 2013-10

	Image _start;// 시작로고
	Image _over;// 게임오버로고
	Image shield;// 실드

	Font font;

	int v[] = { -2, -1, 0, 1, 2, 1, 0, -1 };
	int v2[] = { -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7 };
	int step = 0;

	boolean boss = false;// 보스관련 추가

	GameScreen(MainFrame main) {
		this.main = main;
		font = new Font("Default", Font.PLAIN, 9);
	}

	public void paint(Graphics g) {
		if (gc == null) {
			dblbuff = createImage(main.screenWidth, main.screenHeight);// 더블 버퍼링용 오프스크린 버퍼 생성. 필히 paint 함수 내에서 해 줘야 한다. 그렇지 않으면 null이 반환된다.
			if (dblbuff == null)
				System.out.println("오프스크린 버퍼 생성 실패");
			else
				gc = dblbuff.getGraphics();// 오프스크린 버퍼에 그리기 위한 그래픽 컨텍스트 획득
			return;
		}
		update(g);
	}

	public void update(Graphics g) {// 화면 깜박거림을 줄이기 위해, paint에서 화면을 바로 묘화하지 않고 update 메소드를 호출하게 한다.
		cnt = main.cnt;
		gamecnt = main.gameCnt;
		if (gc == null)
			return;
		dblpaint();// 오프스크린 버퍼에 그리기
		g.drawImage(dblbuff, 0, 0, this);// 오프스크린 버퍼를 메인화면에 그린다.
	}

	public void dblpaint() {
		// 실제 그리는 동작은 이 함수에서 모두 행한다.
		switch (main.status) {
		case MainFrame.TITLE:// 타이틀화면
			drawTitle();
			gc.setColor(new Color(0));
			gc.drawString("Education ver.", 10, 40);
			break;
		case MainFrame.START:// 게임 스타트
			drawBackgroundCloud();
			drawPlayer1();
			drawPlayer2();
			drawBackgroundFog();
			drawImageAnc(_start, 0, 270, 3);
			break;
		case MainFrame.INGAME:// 게임화면
		case MainFrame.PAUSE:// 일시정지
			drawBackgroundCloud();
			drawPlayer1();
			drawPlayer2();
			drawEnemy();
			drawBullet();
			Draw_EFFECT();
			Draw_ITEM();
			drawBackgroundFog();
			Draw_UI();
			break;

		case 3:// 게임오버
			drawBackgroundCloud();
			drawEnemy();
			drawBullet();
			drawBackgroundFog();
			drawImageAnc(_over, 320, 240, 4);
			Draw_UI();
			break;
		default:
			break;
		}
	}

	public void drawTitle() {
		gc.drawImage(title, 0, 0, this);
		if (cnt % 20 < 10)
			gc.drawImage(title_key, 320 - 201, 370, this);
	}

	public void drawBackgroundCloud() {
		int i;
		// 원경을 움직이기 위한 추가 코드
		int step1 = (cnt % 2560) / 2;// 2013-10
		step1 = step1 >= 640 ? step1 - 1280 : step1;// 2013-10
		int step2 = step1 >= 0 ? 640 - step1 : -(640 + step1);// 2013-10
		gc.drawImage(bg, 0 - step1, 0, this);// 2013-10
		gc.drawImage(bgFlip, step2, 0, this);// 2013-10

		for (i = 0; i < 12; i++)
			gc.drawImage(cloud[3], i * 64 - ((cnt / 1) % 128), 370, this);// 2013-10 : 원경이 움직이게 되면서 먼 곳의 작은 구름도 이동 속도를 조금 변경
		for (i = -1; i < 14; i++)
			gc.drawImage(cloud[2], i * 64 - (cnt % 128) * 2, 395, this);
	}

	public void drawBackgroundFog() {
		int i;
		step = (cnt % (bg_f.getWidth(this) / main.scrSpeed)) * main.scrSpeed;
		gc.drawImage(bg_f, 0 - step, 540 - bg_f.getHeight(this) + v[(cnt / 20) % 8] * 2, this);
		// System.out.println("요기"+step);
		if (step >= bg_f.getWidth(this) - main.screenWidth) {
			gc.drawImage(bg_f, 0 - step + bg_f.getWidth(this), 540 - bg_f.getHeight(this) + v[(cnt / 20) % 8] * 2, this);
		}
		for (i = -1; i < 14; i++)
			gc.drawImage(cloud[0], i * 128 - (cnt % 128) * 8, 435, this);
	}

	public void drawPlayer1() {
		int px, py;
		px = main.x[G.P1] / 100;
		py = main.y[G.P1] / 100;
		switch (main.mymode) {
		case MainFrame.UNBEATABLE:// 무적
		case MainFrame.APPEARANCE:// 무적이면서 등장
			if (main.cnt % 20 < 10)
				drawImageAnc(chr[2 + (main.cnt / 5) % 2], px, py, 4);
			break;
		case MainFrame.ONPLAY:// 온플레이
			if (main.pImg == 6)
				drawImageAnc(chr[main.pImg + (main.cnt / 3) % 2], px, py, 4);
			else if (main.pImg != 8)
				drawImageAnc(chr[main.pImg + (main.cnt / 5) % 2], px, py, 4);
			else if (main.pImg == 8)
				drawImageAnc(chr[main.pImg], px, py, 4);
			break;
		case MainFrame.DAMAGE:// 데미지
			if (main.cnt % 6 < 3)
				drawImageAnc(chr[8], px, py, 4);
			break;
		}
		if (main.myshield > 2)
			drawImageAnc(shield, (int) (Math.sin(Math.toRadians((cnt % 72) * 5)) * 16 + px), (int) (Math.cos(Math.toRadians((cnt % 72) * 5)) * 16 + py), (main.cnt / 6 % 7) * 64, 0, 64, 64, 4);// 실드 라이프가 3 이상
		else if (main.myshield > 0 && main.cnt % 4 < 2)
			drawImageAnc(shield, (int) (Math.sin(Math.toRadians((cnt % 72) * 5)) * 16 + px), (int) (Math.cos(Math.toRadians((cnt % 72) * 5)) * 16 + py), (main.cnt / 6 % 7) * 64, 0, 64, 64, 4);// 실드 라이프가 1, 2면 점멸
	}

	public void drawPlayer2() {
		int px, py;
		px = main.x[G.P2] / 100;
		py = main.y[G.P2] / 100;
		switch (main.mymode) {
		case MainFrame.UNBEATABLE:// 무적
		case MainFrame.APPEARANCE:// 무적이면서 등장
			if (main.cnt % 20 < 10)
				drawImageAnc(chr[2 + (main.cnt / 5) % 2], px, py, 4);
			break;
		case MainFrame.ONPLAY:// 온플레이
			if (main.pImg == 6)
				drawImageAnc(chr[main.pImg + (main.cnt / 3) % 2], px, py, 4);
			else if (main.pImg != 8)
				drawImageAnc(chr[main.pImg + (main.cnt / 5) % 2], px, py, 4);
			else if (main.pImg == 8)
				drawImageAnc(chr[main.pImg], px, py, 4);
			break;
		case MainFrame.DAMAGE:// 데미지
			if (main.cnt % 6 < 3)
				drawImageAnc(chr[8], px, py, 4);
			break;
		}
		if (main.myshield > 2)
			drawImageAnc(shield, (int) (Math.sin(Math.toRadians((cnt % 72) * 5)) * 16 + px), (int) (Math.cos(Math.toRadians((cnt % 72) * 5)) * 16 + py), (main.cnt / 6 % 7) * 64, 0, 64, 64, 4);// 실드 라이프가 3 이상
		else if (main.myshield > 0 && main.cnt % 4 < 2)
			drawImageAnc(shield, (int) (Math.sin(Math.toRadians((cnt % 72) * 5)) * 16 + px), (int) (Math.cos(Math.toRadians((cnt % 72) * 5)) * 16 + py), (main.cnt / 6 % 7) * 64, 0, 64, 64, 4);// 실드 라이프가 1, 2면 점멸
	}

	public void drawEnemy() {
		int i;
		Enemy buff;
		for (i = 0; i < main.enemies.size(); i++) {
			buff = (Enemy) (main.enemies.elementAt(i));
			switch (buff.img) {
			case 0:
				drawImageAnc(enemy[0], buff.dis.x, buff.dis.y, ((cnt / 8) % 7) * 36, 0, 36, 36, 4);
				break;
			case 1:
				drawImageAnc(enemy[1], buff.dis.x, buff.dis.y, 4);// 보스 출력
				break;
			case 2:// 위치 네우로이
				switch (buff.mode) {
				case 0:// 왼쪽에서 오른쪽으로 등장
				case 1:// 제자리에서 잠시 정지
				case 2:// 아래로 이동
				case 3:// 위로 이동
					drawImageAnc(enemy[2], buff.dis.x, buff.dis.y, 0, 0, 36, 50, 4);
					break;
				case 5:// 뒤로 돌아 퇴장
					drawImageAnc(enemy[2], buff.dis.x, buff.dis.y, 72, 0, 36, 50, 4);
					break;
				case 4:// 정지해서 손을 앞으로 내밀고 수평으로 총알 발사
					drawImageAnc(enemy[2], buff.dis.x, buff.dis.y, 36, 0, 36, 50, 4);
					break;
				}
			default:
				break;
			}
		}
	}

	public void drawBullet() {
		Bullet buff;
		int i;
		for (i = 0; i < main.bullets.size(); i++) {
			buff = (Bullet) (main.bullets.elementAt(i));
			switch (buff.img_num) {
			case 0:
			case 1:
			case 2:
			case 3:
				drawImageAnc(bullet[buff.img_num], buff.dis.x - 6, buff.dis.y - 6, 4);
				break;
			}
		}
	}

	public void Draw_EFFECT() {
		int i;
		Effect buff;
		for (i = 0; i < main.effects.size(); i++) {
			buff = (Effect) (main.effects.elementAt(i));
			drawImageAnc(explo, buff.dis.x, buff.dis.y, ((16 - buff.cnt) % 4) * 64, ((16 - buff.cnt) / 4) * 64, 64, 64, 4);
		}
	}

	public void Draw_ITEM() {
		int i;
		Item buff;
		for (i = 0; i < main.items.size(); i++) {
			buff = (Item) (main.items.elementAt(i));
			drawImageAnc(item[buff.kind], buff.dis.x, buff.dis.y, ((main.cnt / 4) % 7) * 36, 0, 36, 36, 4);
		}
	}

	public void Draw_UI() {
		// String str1="SCORE "+main.score+" LIFE "+main.mylife+" SPEED "+main.myspeed+" LEVEL "+(main.level+1);//2013-10
		String str2 = "[1] Speed down   [2] Speed up   [3] Pause";
		// gc.setColor(new Color(0));
		// gc.drawString(str1, 9,40);
		// gc.drawString(str1, 11,40);
		// gc.drawString(str1, 10,39);
		// gc.drawString(str1, 10,41);
		// gc.setColor(new Color(0xffffff));
		// gc.drawString(str1, 10,40);

		gc.setColor(new Color(0));
		gc.drawString(str2, 9, main.screenHeight - 10);
		gc.drawString(str2, 11, main.screenHeight - 10);
		gc.drawString(str2, 10, main.screenHeight - 11);
		gc.drawString(str2, 10, main.screenHeight - 9);
		gc.setColor(new Color(0xffffff));
		gc.drawString(str2, 10, main.screenHeight - 10);

		// 그래피컬 UI를 위한 추가 코드
		gc.drawImage(uiUp, 16, 25, this);// 2013-10

		drawImageNum(num, 320, 40, main.score[G.P1], 8);// 2013-10
		drawImageNum(num, 52, 40, main.playerLife[G.P1], 2);// 2013-10
		drawImageNum(num, 576, 40, main.level, 2);// 2013-10

	}

	// 2013-10
	public void drawImageNum(Image img, int x, int y, int value, int digit) {
		// 숫자를 이미지로 변환해 보여준다

		int width = img.getWidth(this) / 10;
		int height = img.getHeight(this);

		String valueStr = String.valueOf(value);
		if (valueStr.length() < digit)
			valueStr = "0000000000".substring(0, digit - valueStr.length()) + valueStr;
		int _xx = x - valueStr.length() * width / 2;
		for (int i = 0; i < valueStr.length(); i++)
			drawImageAnc(num, _xx + i * width, y, (valueStr.charAt(i) - '0') * width, 0, width, height, 0);

	}
	// 2013-10

	public void drawImageAnc(Image img, int x, int y, int anc) {
		// 앵커값을 참조해 이미지 출력 위치를 보정한다.
		// 예) anc==0 : 좌상단이 기준, anc==4 : 이미지 중앙이 기준
		int imgw, imgh;
		imgw = img.getWidth(this);
		imgh = img.getHeight(this);
		x = x - (anc % 3) * (imgw / 2);
		y = y - (anc / 3) * (imgh / 2);

		gc.drawImage(img, x, y, this);
	}

	public void drawImageAnc(Image img, int x, int y, int sx, int sy, int wd, int ht, int anc) {// sx,sy부터 wd,ht만큼 클리핑해서 그린다.
		if (x < 0) {
			wd += x;
			sx -= x;
			x = 0;
		}
		if (y < 0) {
			ht += y;
			sy -= y;
			y = 0;
		}
		if (wd < 0 || ht < 0)
			return;
		x = x - (anc % 3) * (wd / 2);
		y = y - (anc / 3) * (ht / 2);
		gc.setClip(x, y, wd, ht);
		gc.drawImage(img, x - sx, y - sy, this);
		gc.setClip(0, 0, main.screenWidth + 10, main.screenHeight + 30);
	}

}