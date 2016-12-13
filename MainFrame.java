import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;
import java.util.Vector;

// delf: 이름을 GameFrame으로 변경
public class MainFrame extends Frame implements FocusListener, KeyListener, Runnable {
	// 기본 윈도우를 형성하는 프레임을 만든다
	// KeyListener : 키보드 입력 이벤트를 받는다
	// Runnable : 스레드를 가능하게 한다

	public final static int UP_PRESSED = 0x001;
	public final static int DOWN_PRESSED = 0x002;
	public final static int LEFT_PRESSED = 0x004;
	public final static int RIGHT_PRESSED = 0x008;
	public final static int FIRE_PRESSED = 0x010;

	GameScreen gamescreen;// Canvas 객체를 상속한 화면 묘화 메인 클래스
	Thread mainwork;// 스레드 객체
	boolean loop = true;// 스레드 루프 정보
	Random rnd = new Random(); // 랜덤 선언

	private Client client;
	private String id;
	// 게임 제어를 위한 변수
	int status;// 게임의 상태
	int cnt;// 루프 제어용 컨트롤 변수
	int delay;// 루프 딜레이. 1/1000초 단위.
	long pretime;// 루프 간격을 조절하기 위한 시간 체크값
	int keybuff;// 키 버퍼값
	// int controlP2;

	// AudioClip[] aclip=new AudioClip[3];

	// 게임용 변수
	// delf: 게임용 변수 중 2개가 필요한 것을 배열화, 및 다른 클래스 내의 변수도 변경
	int[] score = new int[2];// 점수
	int[] playerLife = new int[2];// 남은 목숨
	int[] direction = new int[2];// 플레이어 이동 방향
	int[] x = new int[2];
	int[] y = new int[2];
	int[] control = new int[2]; // delf: keybuff를 두 개로 나눈 것

	// TODO: 이것도 두개로 만들어야 할거같다.
	int gameCnt;// 게임 흐름 컨트롤
	int scrSpeed = 16;// 스크롤 속도
	int level;// 게임 레벨

	int playerSpeed;// 플레이어 이동 속도
	// 보통 4방향키-8방향 조작계에서는 이동 방향을 각도로 관리하지 않지만 여기서는 장래 터치스크린 인터페이스로
	// 이식될 것을 고려해 4방향키 조작계를 0, 45, 90, 135, 180, 225, 270, 315도 방향으로 조작하는 것으로 한다.
	int pWidth, pHeight;// 플레이어 캐릭터의 너비 높이

	int mymode = 1;// 플레이어 캐릭터의 상태 (0부터 순서대로 무적,등장(무적),온플레이,데미지,사망)
	public final static int UNBEATABLE = 0;
	public final static int APPEARANCE = 1;
	public final static int ONPLAY = 2;
	public final static int DAMAGE = 3;
	public final static int DEATH = 4;

	int pImg;// 플레이어 이미지
	int mycnt;
	boolean isShotKeyPressed = false;// 총알 발사가 눌리고 있는가
	int myshield;// 실드 남은 수비량
	boolean keyReverse = false;// 키보드 반전

	int screenWidth = 640;// 게임 화면 너비
	int screenHeight = 480;// 게임 화면 높이

	Vector<Bullet> bullets = new Vector<Bullet>(); // 총알 관리. 총알의 갯수를 예상할 수 없기 때문에 가변적으로 관리한다.
	Vector<Enemy> enemies = new Vector<Enemy>(); // 적 캐릭터 관리.
	Vector<Effect> effects = new Vector<Effect>(); // 이펙트 관리
	Vector<Item> items = new Vector<Item>(); // 아이템 관리
	// 가변 테이블을 사용한 관리는 처리속도에 악영향을 끼칠 수 있다.

	// 속도를 위해서는 크기를 넉넉하게 잡은 테이블을 사용하는데, 소스가 지저분해지고, 불필요한 메모리를 낭비하게 되므로 적절한 것을 선택한다.
	// 또, C 베이스 플랫폼으로 이식할 경우를 고려야 한다면 class나 Vector, Hashtable 같은 것은 이식하기 어려워지므로 가급적 피한다.

	public MainFrame(Client client) {
		this.client = client;
		id = "" + client.getPlayerId();

		// 기본적인 윈도우 정보 세팅. 게임과 직접적인 상관은 없이 게임 실행을 위한 창을 준비하는 과정.
		setIconImage(makeImage("./rsc/icon.png"));
		setBackground(new Color(0xffffff));// 윈도우 기본 배경색 지정 (R=ff, G=ff, B=ff : 하얀색)
		setTitle("ストライクウィッチ-ズ Fan Game");// 윈도우 이름 지정
		setLayout(null);// 윈도우의 레이아웃을 프리로 설정
		setBounds(100, 100, 640, 480);// 윈도우의 시작 위치와 너비 높이 지정
		setResizable(false);// 윈도우의 크기를 변경할 수 없음
		setVisible(true);// 윈도우 표시
		
		addKeyListener(this);// 키 입력 이벤트 리스너 활성화
		addWindowListener(new MyWindowAdapter());// 윈도우의 닫기 버튼 활성화
		addFocusListener(this);
		
		gamescreen = new GameScreen(this);// 화면 묘화를 위한 캔버스 객체
		gamescreen.setBounds(0, 0, screenWidth, screenHeight);
		add(gamescreen);// Canvas 객체를 프레임에 올린다

		initProgram();
		initialize();
	}

	public void initProgram() {// 프로그램 초기화
		status = 0;
		cnt = 0;
		delay = 17;// 17/1000초 = 58 (프레임/초)
		keybuff = 0;

		mainwork = new Thread(this);
		mainwork.start();
	}

	public void initialize() {// 게임 초기화
		initTitle();
		gamescreen.repaint();
	}

	// 스레드 파트
	public void run() {
		try {
			while (loop) {
				pretime = System.currentTimeMillis();
				gamescreen.repaint();// 화면 리페인트
				keyprocess();// 키 처리
				/* 키를 서버 보내는 작업 */
				process();// 각종 처리

				if (System.currentTimeMillis() - pretime < delay)
					Thread.sleep(delay - System.currentTimeMillis() + pretime);
				// 게임 루프를 처리하는데 걸린 시간을 체크해서 딜레이값에서 차감하여 딜레이를 일정하게 유지한다.
				// 루프 실행 시간이 딜레이 시간보다 크다면 게임 속도가 느려지게 된다.

				if (status != 4)
					cnt++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 키 이벤트 리스너 처리
	public void keyPressed(KeyEvent e) {
		System.out.println("키 눌림");
		// if(status==2&&(mymode==2||mymode==0)){
		if (status == INGAME) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				keybuff |= FIRE_PRESSED;
				break;
			case KeyEvent.VK_LEFT:
				keybuff |= LEFT_PRESSED;// 멀티키의 누르기 처리
				break;
			case KeyEvent.VK_UP:
				keybuff |= UP_PRESSED;
				break;
			case KeyEvent.VK_RIGHT:
				keybuff |= RIGHT_PRESSED;
				break;
			case KeyEvent.VK_DOWN:
				keybuff |= DOWN_PRESSED;
				break;
			case KeyEvent.VK_1:
				if (playerSpeed > 1)
					playerSpeed--;
				break;
			case KeyEvent.VK_2:
				if (playerSpeed < 9)
					playerSpeed++;
				break;
			case KeyEvent.VK_3:
				if (status == INGAME)
					status = PAUSE;
				break;
			/*case KeyEvent.VK_1:
				System.out.println("이펙트 테스트");
				Effect effect=new Effect(0, RAND(30,gScreenWidth-30)*100,RAND(30,gScreenHeight-30)*100, 0);
				effects.add(effect);
				break;*/
			default:
				break;
			}
		}
		else if (status != INGAME) {
			System.out.println("게임중 아닌상태에서 무언가 눌림");
			keybuff = e.getKeyCode();
			System.out.println("눌린 키는 " + keybuff);
		}
		if (keybuff != 0x00) {
			sendKey(keybuff); // delf: 눌린 키를 서버에 전송한다.
		}

	}

	/** keybuf에 저장 된 키 값을 서버에 전송. "command id key"의 형식으로 전송된다.
	 * @param key 현재 눌려져있는 키 값에 해당하는 정수. 현재 keybuff에 저장되어 있는 정수.
	 * @author delf*/
	public void sendKey(int key) {
		System.out.println("pressed key: " + key);
		String msg = Client.createMsg(G.KEY, id, key + "");
		client.sendMsg(msg);
	}

	public void keyReleased(KeyEvent e) {
		// if(status==2&&(mymode==2||mymode==0)) {
		// if(status==2){
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE:
			keybuff &= ~FIRE_PRESSED;
			isShotKeyPressed = true;
			break;
		case KeyEvent.VK_LEFT:
			keybuff &= ~LEFT_PRESSED;// 멀티키의 떼기 처리
			break;
		case KeyEvent.VK_UP:
			keybuff &= ~UP_PRESSED;
			break;
		case KeyEvent.VK_RIGHT:
			keybuff &= ~RIGHT_PRESSED;
			break;
		case KeyEvent.VK_DOWN:
			keybuff &= ~DOWN_PRESSED;
			break;
		}
		sendKey(keybuff); // delf: 눌린 키를 서버에 전송한다.
		// }
		// PC 환경에서는 기본적으로 키보드의 반복입력을 지원하지만,
		// 그렇지 않은 플랫폼에서는 키 버퍼값에 떼고 눌렀을 때마다 값을 변경해 리피트 여부를 제어한다.
	}

	public void keyTyped(KeyEvent e) {
	}

	public final static int TITLE = 0;
	public final static int START = 1;
	public final static int INGAME = 2;
	public final static int GAMEOVER = 3;
	public final static int PAUSE = 4;

	// 각종 판단, 변수나 이벤트, CPU 관련 처리
	private void process() {
		switch (status) {
		case TITLE:// 타이틀화면
			break;
		case START:// 스타트
			processPlayer1();
			processPlayer2();
			if (mymode == APPEARANCE)
				status = INGAME;
			break;
		case INGAME:// 게임화면
			processPlayer1();
			processPlayer2();
			processEnemy();
			processBullet();
			processEffect();
			processGameFlow();
			processItem();
			break;
		case GAMEOVER:// 게임오버
			processEnemy();
			processBullet();
			processGameFlow();
			break;
		case PAUSE:// 일시정지
			break;
		default:
			break;
		}
		if (status != PAUSE)
			gameCnt++;
	}

	public void setKeybuff() {

	}

	// 키 입력 처리
	// 키 이벤트에서 입력 처리를 할 경우, 이벤트 병목현상이 발생할 수 있으므로 이벤트에서는 키 버퍼만을 변경하고, 루프 내에서 버퍼값에 따른 처리를 한다.
	private void keyprocess() {
		switch (status) {
		case TITLE:// 타이틀화면
			if (keybuff == KeyEvent.VK_SPACE) { // delf: 임시로 keybuff로 변경
				System.out.println("스페이스");
				initGame();
				initPlayer();
				status = START;
			}
			break;
		case INGAME:// 게임화면
			if (mymode == ONPLAY || mymode == UNBEATABLE) {
				switch (control[G.P1]) {
				case 0:
					direction[G.P1] = -1;
					pImg = 0;
					break;
				case FIRE_PRESSED:
					direction[G.P1] = -1;
					pImg = 6;
					break;
				case UP_PRESSED:
					direction[G.P1] = 0;
					pImg = 2;
					break;
				case UP_PRESSED | FIRE_PRESSED:
					direction[G.P1] = 0;
					pImg = 6;
					break;
				case LEFT_PRESSED:
					direction[G.P1] = 90;
					pImg = 4;
					break;
				case LEFT_PRESSED | FIRE_PRESSED:
					direction[G.P1] = 90;
					pImg = 6;
					break;
				case RIGHT_PRESSED:
					direction[G.P1] = 270;
					pImg = 2;
					break;
				case RIGHT_PRESSED | FIRE_PRESSED:
					direction[G.P1] = 270;
					pImg = 6;
					break;
				case UP_PRESSED | LEFT_PRESSED:
					direction[G.P1] = 45;
					pImg = 4;
					break;
				case UP_PRESSED | LEFT_PRESSED | FIRE_PRESSED:
					direction[G.P1] = 45;
					pImg = 6;
					break;
				case UP_PRESSED | RIGHT_PRESSED:
					direction[G.P1] = 315;
					pImg = 2;
					break;
				case UP_PRESSED | RIGHT_PRESSED | FIRE_PRESSED:
					direction[G.P1] = 315;
					pImg = 6;
					break;
				case DOWN_PRESSED:
					direction[G.P1] = 180;
					pImg = 2;
					break;
				case DOWN_PRESSED | FIRE_PRESSED:
					direction[G.P1] = 180;
					pImg = 6;
					break;
				case DOWN_PRESSED | LEFT_PRESSED:
					direction[G.P1] = 135;
					pImg = 4;
					break;
				case DOWN_PRESSED | LEFT_PRESSED | FIRE_PRESSED:
					direction[G.P1] = 135;
					pImg = 6;
					break;
				case DOWN_PRESSED | RIGHT_PRESSED:
					direction[G.P1] = 225;
					pImg = 2;
					break;
				case DOWN_PRESSED | RIGHT_PRESSED | FIRE_PRESSED:
					direction[G.P1] = 225;
					pImg = 6;
					break;
				default:
					// System.out.println(""+control[G.P1]);
					control[G.P1] = 0;
					direction[G.P1] = -1;
					pImg = 0;
					break;
				}

				switch (control[G.P2]) {
				case 0:
					direction[G.P2] = -1;
					pImg = 0;
					break;
				case FIRE_PRESSED:
					direction[G.P2] = -1;
					pImg = 6;
					break;
				case UP_PRESSED:
					direction[G.P2] = 0;
					pImg = 2;
					break;
				case UP_PRESSED | FIRE_PRESSED:
					direction[G.P2] = 0;
					pImg = 6;
					break;
				case LEFT_PRESSED:
					direction[G.P2] = 90;
					pImg = 4;
					break;
				case LEFT_PRESSED | FIRE_PRESSED:
					direction[G.P2] = 90;
					pImg = 6;
					break;
				case RIGHT_PRESSED:
					direction[G.P2] = 270;
					pImg = 2;
					break;
				case RIGHT_PRESSED | FIRE_PRESSED:
					direction[G.P2] = 270;
					pImg = 6;
					break;
				case UP_PRESSED | LEFT_PRESSED:
					direction[G.P2] = 45;
					pImg = 4;
					break;
				case UP_PRESSED | LEFT_PRESSED | FIRE_PRESSED:
					direction[G.P2] = 45;
					pImg = 6;
					break;
				case UP_PRESSED | RIGHT_PRESSED:
					direction[G.P2] = 315;
					pImg = 2;
					break;
				case UP_PRESSED | RIGHT_PRESSED | FIRE_PRESSED:
					direction[G.P2] = 315;
					pImg = 6;
					break;
				case DOWN_PRESSED:
					direction[G.P2] = 180;
					pImg = 2;
					break;
				case DOWN_PRESSED | FIRE_PRESSED:
					direction[G.P2] = 180;
					pImg = 6;
					break;
				case DOWN_PRESSED | LEFT_PRESSED:
					direction[G.P2] = 135;
					pImg = 4;
					break;
				case DOWN_PRESSED | LEFT_PRESSED | FIRE_PRESSED:
					direction[G.P2] = 135;
					pImg = 6;
					break;
				case DOWN_PRESSED | RIGHT_PRESSED:
					direction[G.P2] = 225;
					pImg = 2;
					break;
				case DOWN_PRESSED | RIGHT_PRESSED | FIRE_PRESSED:
					direction[G.P2] = 225;
					pImg = 6;
					break;
				default:
					// System.out.println(""+control[G.P2]);
					control[G.P2] = 0;
					direction[G.P2] = -1;
					pImg = 0;
					break;
				}
			}
			break;
		case GAMEOVER:
			if (gameCnt++ >= 200 && control[G.P1] == KeyEvent.VK_SPACE) {
				initTitle();
				status = 0;
				control[G.P1] = 0;
			}

			if (gameCnt++ >= 200 && control[G.P2] == KeyEvent.VK_SPACE) {
				initTitle();
				status = 0;
				control[G.P2] = 0;
			}
			break;
		case PAUSE:
			// TODO: 로직 수정 필요
			if (gameCnt++ >= 200 && control[G.P1] == KeyEvent.VK_3)
				status = 2;
			break;
		default:
			break;
		}
	}

	// 서브루틴 일람
	public void initTitle() {
		/*gamescreen.bg=null;
		gamescreen.bg_f=null;
		for(i=0;i<4;i++) gamescreen.cloud[i]=null;
		for(i=0;i<4;i++) gamescreen.bullet[i]=null;
		gamescreen.enemy[0]=null;
		gamescreen.explo=null;
		gamescreen.item=null;
		gamescreen._start=null;
		gamescreen._over=null;
		System.gc();*/

		gamescreen.title = makeImage("./rsc/title.png");
		gamescreen.title_key = makeImage("./rsc/pushspace.png");

		// aclip[0]=myaudio.getClip("./snd/bgm_0.au");
		// aclip[0].loop();
	}

	public void initGame() {
		int i;
		/*gamescreen.title=null;
		gamescreen.title_key=null;
		System.gc();*/

		gamescreen.bg = makeImage("./rsc/구름.JPG");// bg.png
		gamescreen.bgFlip = makeImage("./rsc/cloud_flip.jpg");// 2013-10
		gamescreen.bg_f = makeImage("./rsc/bg_f.png");
		for (i = 0; i < 4; i++)
			gamescreen.cloud[i] = makeImage("./rsc/cloud" + i + ".png");
		for (i = 0; i < 4; i++)
			gamescreen.bullet[i] = makeImage("./rsc/game/bullet_" + i + ".png");
		gamescreen.enemy[0] = makeImage("./rsc/game/enemy0.png");
		gamescreen.explo = makeImage("./rsc/game/explode.png");
		gamescreen.item[0] = makeImage("./rsc/game/item0.png");
		gamescreen.item[1] = makeImage("./rsc/game/item1.png");
		gamescreen.item[2] = makeImage("./rsc/game/item2.png");// 아이템 추가
		gamescreen._start = makeImage("./rsc/game/start.png");
		gamescreen._over = makeImage("./rsc/game/gameover.png");
		gamescreen.shield = makeImage("./rsc/game/shield.png");
		gamescreen.enemy[1] = makeImage("./rsc/game/enemy1.png");// 보스 추가
		gamescreen.enemy[2] = makeImage("./rsc/game/enemy2.png");// 위치 네우로이

		gamescreen.num = makeImage("./rsc/gnum.png");// 2013-10
		gamescreen.uiUp = makeImage("./rsc/ui_up.png");// 2013-10

		keybuff = 0;
		bullets.clear();
		enemies.clear();
		effects.clear();
		items.clear();
		level = 0;
		gameCnt = 0;
	}

	public void initPlayer() {
		for (int i = 0; i < 9; i++) {
			if (i < 10)
				gamescreen.chr[i] = makeImage("./rsc/player/my_0" + i + ".png");
			else
				gamescreen.chr[i] = makeImage("./rsc/player/my_" + i + ".png");
		}
		initPlayerData();
	}

	public void initPlayerData() {
		score[G.P1] = 0;
		x[G.P1] = 0;
		y[G.P1] = 17000;
		playerSpeed = 4;
		direction[G.P1] = -1;
		// mywidth, myheight;//플레이어 캐릭터의 너비 높이
		mymode = 1;
		pImg = 2;
		mycnt = 0;
		playerLife[G.P1] = 10000000;
		keybuff = 0;

		// delf: 임시코드
		x[G.P2] = 0;
		y[G.P2] = 30000;
		direction[G.P2] = -1;
		playerLife[G.P1] = 10000000;
	}

	/** 플레이어에 대한 처리를 한다. 각 상황에 따라 어떻게 행동 해야할지 처리함 */
	public void processPlayer1() { // delf: 메소드 이름 변경함
		Bullet shoot;
		switch (mymode) {
		case APPEARANCE: // delf: 등장 시,
			x[G.P1] += 200; // delf: 일정 위치까지 앞으로 이동
			if (x[G.P1] > 20000)
				mymode = ONPLAY;
			break;
		case UNBEATABLE: // delf: 무적
			if (mycnt-- == 0) { // delf: 일정 시간 지나면
				mymode = ONPLAY; // delf: 게임 재개
				pImg = 0;
			}
		case ONPLAY:
			if (direction[G.P1] != -1 && keyReverse)
				direction[G.P1] = (direction[G.P1] + 180) % 360;
			if (direction[G.P1] > -1) {
				x[G.P1] -= (playerSpeed * Math.sin(Math.toRadians(direction[G.P1])) * 100);
				y[G.P1] -= (playerSpeed * Math.cos(Math.toRadians(direction[G.P1])) * 100);
			}
			if (pImg == 6) {
				x[G.P1] -= 20;
				if (cnt % 4 == 0 || isShotKeyPressed) {
					isShotKeyPressed = false;
					shoot = new Bullet(x[G.P1] + 2500, y[G.P1] + 1500, 0, 0, RAND(245, 265), 8);
					bullets.add(shoot);
					shoot = new Bullet(x[G.P1] + 2500, y[G.P1] + 1500, 0, 0, RAND(268, 272), 9);
					bullets.add(shoot);
					shoot = new Bullet(x[G.P1] + 2500, y[G.P1] + 1500, 0, 0, RAND(275, 295), 8);
					bullets.add(shoot);
				}
				// 8myy+=70;
			}
			break;
		case DAMAGE:
			pImg = 8;
			if (mycnt-- == 0) {
				mymode = 0;
				mycnt = 50;
			}
			break;
		}
		if (x[G.P1] < 2000)
			x[G.P1] = 2000;
		if (x[G.P1] > 62000)
			x[G.P1] = 62000;
		if (y[G.P1] < 3000)
			y[G.P1] = 3000;
		if (y[G.P1] > 45000)
			y[G.P1] = 45000;
	}

	public void processPlayer2() {
		Bullet shoot;
		switch (mymode) {
		case APPEARANCE: // 등장 시,
			x[G.P2] += 200; // 일정 위치까지 앞으로 이동
			if (x[G.P2] > 20000)
				mymode = ONPLAY; // 도착하면 게임 시작
			break;
		case UNBEATABLE:
			if (mycnt-- == 0) {
				mymode = ONPLAY;
				pImg = 0;
			}
		case ONPLAY:
			if (direction[G.P2] != -1 && keyReverse)
				direction[G.P2] = (direction[G.P2] + 180) % 360;
			if (direction[G.P2] > -1) {
				x[G.P2] -= (playerSpeed * Math.sin(Math.toRadians(direction[G.P2])) * 100);
				y[G.P2] -= (playerSpeed * Math.cos(Math.toRadians(direction[G.P2])) * 100);
			}
			if (pImg == 6) {
				x[G.P2] -= 20;
				if (cnt % 4 == 0 || isShotKeyPressed) {
					isShotKeyPressed = false;
					shoot = new Bullet(x[G.P2] + 2500, y[G.P2] + 1500, 0, 0, RAND(245, 265), 8);
					bullets.add(shoot);
					shoot = new Bullet(x[G.P2] + 2500, y[G.P2] + 1500, 0, 0, RAND(268, 272), 9);
					bullets.add(shoot);
					shoot = new Bullet(x[G.P2] + 2500, y[G.P2] + 1500, 0, 0, RAND(275, 295), 8);
					bullets.add(shoot);
				}
				// 8myy+=70;
			}
			break;
		case DAMAGE:
			pImg = 8;
			if (mycnt-- == 0) {
				mymode = 0;
				mycnt = 50;
			}
			break;
		}
		if (x[G.P2] < 2000)
			x[G.P2] = 2000;
		if (x[G.P2] > 62000)
			x[G.P2] = 62000;
		if (y[G.P2] < 3000)
			y[G.P2] = 3000;
		if (y[G.P2] > 45000)
			y[G.P2] = 45000;
	}

	public void processEnemy() {
		int i;
		Enemy buff;
		for (i = 0; i < enemies.size(); i++) {
			buff = (Enemy) (enemies.elementAt(i));
			if (!buff.move())
				enemies.remove(i);
		}
	}

	public void processBullet() {
		Bullet bullet;
		Enemy enemy;
		Effect effect;
		int i, j, dist;
		for (i = 0; i < bullets.size(); i++) {
			bullet = bullets.elementAt(i);
			bullet.move();
			if (bullet.dis.x < 10 || bullet.dis.x > screenWidth + 10 || bullet.dis.y < 10 || bullet.dis.y > screenHeight + 10) {
				bullets.remove(i);// 화면 밖으로 나가면 총알 제거
				continue;
			}
			if (bullet.from == 0) {// 플레이어가 쏜 총알이 적에게 명중 판정
				for (j = 0; j < enemies.size(); j++) {
					enemy = enemies.elementAt(j);
					dist = getDistance(bullet.dis.x, bullet.dis.y, enemy.dis.x, enemy.dis.y);
					// if(dist<1500) {//중간점 거리가 명중 판정이 가능한 범위에 왔을 때
					if (dist < enemy.hitrange) {// 중간점 거리가 명중 판정이 가능한 범위에 왔을 때 - hitrange : 적 캐릭터마다 그림에 따라 명중판정되는 범위가 다르다
						if (enemy.life-- <= 0) {// 적 라이프 감소
							if (enemy.kind == 1) {
								if (gameCnt < 2100)
									gameCnt = 2100;
							}
							enemies.remove(j);// 적 캐릭터 소거
							effect = new Effect(0, enemy.pos.x, bullet.pos.y, 0);
							effects.add(effect);// 폭발 이펙트 추가
							// Item tem=new Item(ebuff.pos.x, buff.pos.y, RAND(1,(level+1)*20)/((level+1)*20));//난수 결과가 최대값일 때만 생성되는 아이템이 1이 된다
							int itemKind = RAND(1, 100);
							Item tem;
							if (itemKind <= 70)
								tem = new Item(enemy.pos.x, bullet.pos.y, 0);
							else if (itemKind <= 95)
								tem = new Item(enemy.pos.x, bullet.pos.y, 2);
							else
								tem = new Item(enemy.pos.x, bullet.pos.y, 1);
							items.add(tem);
						}
						// expl=new Effect(0, ebuff.pos.x, buff.pos.y, 0);
						effect = new Effect(0, bullet.pos.x, bullet.pos.y, 0);
						effects.add(effect);
						score[G.P1]++;// 점수 추가
						bullets.remove(i);// 총알 소거
						break;// 총알이 소거되었으므로 루프 아웃
					}
				}
			}
			else { // 적이 쏜 총알이 플레이어에게 명중 판정
				if (mymode != ONPLAY)
					continue;
				dist = getDistance(x[G.P1] / 100, y[G.P1] / 100, bullet.dis.x, bullet.dis.y);
				if (dist < 500) {
					if (myshield == 0) {
						mymode = 3;
						mycnt = 30;
						bullets.remove(i);
						effect = new Effect(0, x[G.P1] - 2000, y[G.P1], 0);
						effects.add(effect);
						if (--playerLife[G.P1] <= 0) {
							status = 3;
							gameCnt = 0;
						}
					}
					else {// 실드가 있을 경우
						myshield--;
						bullets.remove(i);
					}
				}
			}
		}
	}

	public void processEffect() {
		int i;
		Effect buff;
		for (i = 0; i < effects.size(); i++) {
			buff = (Effect) (effects.elementAt(i));
			if (cnt % 3 == 0)
				buff.cnt--;
			if (buff.cnt == 0)
				effects.remove(i);
		}
	}

	public void processGameFlow() {
		int control = 0;
		int newy = 0, mode = 0;
		// 보스 관련 수정사항 추가
		if (gamescreen.boss) {
			// 보스가 생성되어 있는 상황의 처리
			if (level > 1) {// 게임 레벨이 2 이상이면, 보스전 도중에 소형 캐릭터들이 지원기로 나온다
				// 지원기 등장 시나리오
				// : 게임카운트(gamecnt) 0~200 : 지원기 없음
				// : 게임카운트(gamecnt) 801~1000 : 지원기 60카운트 단위로 등장
				// : 게임카운트(gamecnt) 1601~2199 : 지원기 30카운터 단위로 등장
				if (800 < gameCnt && gameCnt < 1000) {// 보스가 등장하고 시간이 좀 지나서 소형 캐릭터들이 덤벼오기 시작한다
					if (gameCnt % 60 == 0) {
						newy = RAND(30, screenHeight - 30) * 100;
						if (newy < 24000)
							mode = 0;
						else
							mode = 1;
						Enemy en = new Enemy(this, 0, screenWidth * 100, newy, 0, mode);
						enemies.add(en);
					}
				}
				else if (1600 < gameCnt && gameCnt < 2200) {// 보스전이 후반부에 들어서면서 소형 지원기들의 공격이 거세진다
					if (gameCnt % 30 == 0) {
						Enemy en;
						newy = RAND(30, screenHeight - 30) * 100;
						if (newy < 24000)
							mode = 0;
						else
							mode = 1;
						if (level > 1 && RAND(1, 100) < level * 10)
							en = new Enemy(this, 2, screenWidth * 100, newy, 2, 0);
						else
							en = new Enemy(this, 0, screenWidth * 100, newy, 0, mode);
						enemies.add(en);
					}
				}
			}
			if (gameCnt > 2210) {// 보스전 타임 아웃으로 보스전을 종료한다
				gamescreen.boss = false;
				gameCnt = 0;
				System.out.println("보스 타임아웃");
			}
		}
		else {
			if (gameCnt < 500)
				control = 1;
			else if (gameCnt < 1000)
				control = 2;
			else if (gameCnt < 1300)
				control = 0;
			else if (gameCnt < 1700)
				control = 1;
			else if (gameCnt < 2000)
				control = 2;
			else if (gameCnt < 2400)
				control = 3;
			else {
				// 기존에 레벨만 올려주던 부분에서, 레벨을 올려주면서 보스 캐릭터를 등장시킨다
				System.out.println("보스 등장");
				gamescreen.boss = true;
				Enemy en = new Enemy(this, 1, screenWidth * 100, 24000, 1, 0);// img 값이 1, kind 값이 1
				enemies.add(en);
				gameCnt = 0;
				level++;
			}
			if (control > 0) {
				newy = RAND(30, screenHeight - 30) * 100;
				if (newy < 24000)
					mode = 0;
				else
					mode = 1;
			}
			Enemy en;
			switch (control) {
			case 1:
				if (gameCnt % 90 == 0) {
					if (RAND(1, 3) != 3 && level > 0)
						en = new Enemy(this, 2, screenWidth * 100, newy, 2, mode);
					else
						en = new Enemy(this, 0, screenWidth * 100, newy, 0, mode);
					enemies.add(en);
				}
				break;
			case 2:
				if (gameCnt % 50 == 0) {
					if (RAND(1, 3) != 3 && level > 0)
						en = new Enemy(this, 2, screenWidth * 100, newy, 2, mode);
					else
						en = new Enemy(this, 0, screenWidth * 100, newy, 0, mode);
					enemies.add(en);
				}
				break;
			case 3:
				if (gameCnt % 20 == 0) {
					if (RAND(1, 3) != 3 && level > 0)
						en = new Enemy(this, 2, screenWidth * 100, newy, 2, mode);
					else
						en = new Enemy(this, 0, screenWidth * 100, newy, 0, mode);
					enemies.add(en);
				}
				break;
			}
		}
	}

	public static final int SCORE = 0;
	public static final int SHIELD = 1;
	public static final int DESTROY_ALL_ENEMIES = 2;

	public void processItem() {
		int i, dist;
		Item buff;
		for (i = 0; i < items.size(); i++) {
			buff = (Item) (items.elementAt(i));
			dist = getDistance(x[G.P1] / 100, y[G.P1] / 100, buff.dis.x, buff.dis.y);
			if (dist < 1000) {// 아이템 획득
				switch (buff.kind) {
				case SCORE:// 일반 득점
					score[G.P1] += 100;
					break;
				case SHIELD:// 실드
					myshield = 5;
					break;
				case DESTROY_ALL_ENEMIES:// 전멸 아이템
					// Enemy ebuff;
					// Effect expl;

					// 적 전멸 효과
					int j = enemies.size();
					for (int k = 0; k < j; k++) {
						Enemy ebuff = (Enemy) (enemies.elementAt(k));
						if (ebuff == null)
							continue;// 만일 해당 인덱스에 적 캐릭터가 생성되어있지 않을 경우를 대비
						if (ebuff.kind == 1) {// 해당 인덱스에 할당된 적 캐릭터가 보스 캐릭터일 경우는 전멸에 해당하지 않고 HP만 반으로 줄인다. 1 이하라면 1.
							score[G.P1] += 300;
							ebuff.life = ebuff.life / 2;
							if (ebuff.life <= 1)
								ebuff.life = 1;
							continue;
						}
						Effect expl = new Effect(0, ebuff.pos.x, ebuff.pos.y, 0);
						effects.add(expl);// 폭발 이펙트 추가
						ebuff.pos.x = -10000;// 다음 처리에서 소거될 수 있도록
						score[G.P1] += 50;
						// enemies.remove(ebuff);//적 캐릭터 소거
					}

					// 적 총알 전부 소거
					j = bullets.size();
					for (int k = 0; k < j; k++) {
						Bullet bbuff = (Bullet) (bullets.elementAt(k));
						if (bbuff.from != 0) {
							bbuff.pos.x = -10000;
							score[G.P1]++;
						}
						// bullets.remove(bbuff);
					}
					break;
				}
				items.remove(i);
			}
			else if (buff.move())
				items.remove(i);
		}
	}

	public Image makeImage(String furl) {
		Image img;
		Toolkit tk = Toolkit.getDefaultToolkit();
		img = tk.getImage(furl);
		try {
			// 여기부터
			MediaTracker mt = new MediaTracker(this);
			mt.addImage(img, 0);
			mt.waitForID(0);
			// 여기까지, getImage로 읽어들인 이미지가 로딩이 완료됐는지 확인하는 부분
		} catch (Exception ee) {
			ee.printStackTrace();
			return null;
		}
		return img;
	}

	public int getDistance(int x1, int y1, int x2, int y2) {
		return Math.abs((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}

	public int RAND(int startnum, int endnum) // 랜덤범위(startnum부터 ramdom까지), 랜덤값이 적용될 변수.
	{
		int a, b;
		if (startnum < endnum)
			b = endnum - startnum; // b는 실제 난수 발생 폭
		else
			b = startnum - endnum;
		a = Math.abs(rnd.nextInt() % (b + 1));
		return (a + startnum);
	}

	int getAngle(int sx, int sy, int dx, int dy) {
		int vx = dx - sx;
		int vy = dy - sy;
		double rad = Math.atan2(vx, vy);
		int degree = (int) ((rad * 180) / Math.PI);
		return (degree + 180);
	}

	public boolean readGameFlow(String fname) {
		String buff;
		try {
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			if ((buff = fin.readLine()) != null) {
				System.out.println(Integer.parseInt(buff));
			}
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void focusGained(FocusEvent e) {
		
	}

	@Override
	public void focusLost(FocusEvent e) {
		this.requestFocus();
	}
}