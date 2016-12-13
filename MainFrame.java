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

// delf: �̸��� GameFrame���� ����
public class MainFrame extends Frame implements FocusListener, KeyListener, Runnable {
	// �⺻ �����츦 �����ϴ� �������� �����
	// KeyListener : Ű���� �Է� �̺�Ʈ�� �޴´�
	// Runnable : �����带 �����ϰ� �Ѵ�

	public final static int UP_PRESSED = 0x001;
	public final static int DOWN_PRESSED = 0x002;
	public final static int LEFT_PRESSED = 0x004;
	public final static int RIGHT_PRESSED = 0x008;
	public final static int FIRE_PRESSED = 0x010;

	GameScreen gamescreen;// Canvas ��ü�� ����� ȭ�� ��ȭ ���� Ŭ����
	Thread mainwork;// ������ ��ü
	boolean loop = true;// ������ ���� ����
	Random rnd = new Random(); // ���� ����

	private Client client;
	private String id;
	// ���� ��� ���� ����
	int status;// ������ ����
	int cnt;// ���� ����� ��Ʈ�� ����
	int delay;// ���� ������. 1/1000�� ����.
	long pretime;// ���� ������ �����ϱ� ���� �ð� üũ��
	int keybuff;// Ű ���۰�
	// int controlP2;

	// AudioClip[] aclip=new AudioClip[3];

	// ���ӿ� ����
	// delf: ���ӿ� ���� �� 2���� �ʿ��� ���� �迭ȭ, �� �ٸ� Ŭ���� ���� ������ ����
	int[] score = new int[2];// ����
	int[] playerLife = new int[2];// ���� ���
	int[] direction = new int[2];// �÷��̾� �̵� ����
	int[] x = new int[2];
	int[] y = new int[2];
	int[] control = new int[2]; // delf: keybuff�� �� ���� ���� ��

	// TODO: �̰͵� �ΰ��� ������ �ҰŰ���.
	int gameCnt;// ���� �帧 ��Ʈ��
	int scrSpeed = 16;// ��ũ�� �ӵ�
	int level;// ���� ����

	int playerSpeed;// �÷��̾� �̵� �ӵ�
	// ���� 4����Ű-8���� ���۰迡���� �̵� ������ ������ �������� ������ ���⼭�� �巡 ��ġ��ũ�� �������̽���
	// �̽ĵ� ���� ����� 4����Ű ���۰踦 0, 45, 90, 135, 180, 225, 270, 315�� �������� �����ϴ� ������ �Ѵ�.
	int pWidth, pHeight;// �÷��̾� ĳ������ �ʺ� ����

	int mymode = 1;// �÷��̾� ĳ������ ���� (0���� ������� ����,����(����),���÷���,������,���)
	public final static int UNBEATABLE = 0;
	public final static int APPEARANCE = 1;
	public final static int ONPLAY = 2;
	public final static int DAMAGE = 3;
	public final static int DEATH = 4;

	int pImg;// �÷��̾� �̹���
	int mycnt;
	boolean isShotKeyPressed = false;// �Ѿ� �߻簡 ������ �ִ°�
	int myshield;// �ǵ� ���� ����
	boolean keyReverse = false;// Ű���� ����

	int screenWidth = 640;// ���� ȭ�� �ʺ�
	int screenHeight = 480;// ���� ȭ�� ����

	Vector<Bullet> bullets = new Vector<Bullet>(); // �Ѿ� ����. �Ѿ��� ������ ������ �� ���� ������ ���������� �����Ѵ�.
	Vector<Enemy> enemies = new Vector<Enemy>(); // �� ĳ���� ����.
	Vector<Effect> effects = new Vector<Effect>(); // ����Ʈ ����
	Vector<Item> items = new Vector<Item>(); // ������ ����
	// ���� ���̺��� ����� ������ ó���ӵ��� �ǿ����� ��ĥ �� �ִ�.

	// �ӵ��� ���ؼ��� ũ�⸦ �˳��ϰ� ���� ���̺��� ����ϴµ�, �ҽ��� ������������, ���ʿ��� �޸𸮸� �����ϰ� �ǹǷ� ������ ���� �����Ѵ�.
	// ��, C ���̽� �÷������� �̽��� ��츦 ����� �Ѵٸ� class�� Vector, Hashtable ���� ���� �̽��ϱ� ��������Ƿ� ������ ���Ѵ�.

	public MainFrame(Client client) {
		this.client = client;
		id = "" + client.getPlayerId();

		// �⺻���� ������ ���� ����. ���Ӱ� �������� ����� ���� ���� ������ ���� â�� �غ��ϴ� ����.
		setIconImage(makeImage("./rsc/icon.png"));
		setBackground(new Color(0xffffff));// ������ �⺻ ���� ���� (R=ff, G=ff, B=ff : �Ͼ��)
		setTitle("���ȫ髤�������ë�-�� Fan Game");// ������ �̸� ����
		setLayout(null);// �������� ���̾ƿ��� ������ ����
		setBounds(100, 100, 640, 480);// �������� ���� ��ġ�� �ʺ� ���� ����
		setResizable(false);// �������� ũ�⸦ ������ �� ����
		setVisible(true);// ������ ǥ��
		
		addKeyListener(this);// Ű �Է� �̺�Ʈ ������ Ȱ��ȭ
		addWindowListener(new MyWindowAdapter());// �������� �ݱ� ��ư Ȱ��ȭ
		addFocusListener(this);
		
		gamescreen = new GameScreen(this);// ȭ�� ��ȭ�� ���� ĵ���� ��ü
		gamescreen.setBounds(0, 0, screenWidth, screenHeight);
		add(gamescreen);// Canvas ��ü�� �����ӿ� �ø���

		initProgram();
		initialize();
	}

	public void initProgram() {// ���α׷� �ʱ�ȭ
		status = 0;
		cnt = 0;
		delay = 17;// 17/1000�� = 58 (������/��)
		keybuff = 0;

		mainwork = new Thread(this);
		mainwork.start();
	}

	public void initialize() {// ���� �ʱ�ȭ
		initTitle();
		gamescreen.repaint();
	}

	// ������ ��Ʈ
	public void run() {
		try {
			while (loop) {
				pretime = System.currentTimeMillis();
				gamescreen.repaint();// ȭ�� ������Ʈ
				keyprocess();// Ű ó��
				/* Ű�� ���� ������ �۾� */
				process();// ���� ó��

				if (System.currentTimeMillis() - pretime < delay)
					Thread.sleep(delay - System.currentTimeMillis() + pretime);
				// ���� ������ ó���ϴµ� �ɸ� �ð��� üũ�ؼ� �����̰����� �����Ͽ� �����̸� �����ϰ� �����Ѵ�.
				// ���� ���� �ð��� ������ �ð����� ũ�ٸ� ���� �ӵ��� �������� �ȴ�.

				if (status != 4)
					cnt++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Ű �̺�Ʈ ������ ó��
	public void keyPressed(KeyEvent e) {
		System.out.println("Ű ����");
		// if(status==2&&(mymode==2||mymode==0)){
		if (status == INGAME) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				keybuff |= FIRE_PRESSED;
				break;
			case KeyEvent.VK_LEFT:
				keybuff |= LEFT_PRESSED;// ��ƼŰ�� ������ ó��
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
				System.out.println("����Ʈ �׽�Ʈ");
				Effect effect=new Effect(0, RAND(30,gScreenWidth-30)*100,RAND(30,gScreenHeight-30)*100, 0);
				effects.add(effect);
				break;*/
			default:
				break;
			}
		}
		else if (status != INGAME) {
			System.out.println("������ �ƴѻ��¿��� ���� ����");
			keybuff = e.getKeyCode();
			System.out.println("���� Ű�� " + keybuff);
		}
		if (keybuff != 0x00) {
			sendKey(keybuff); // delf: ���� Ű�� ������ �����Ѵ�.
		}

	}

	/** keybuf�� ���� �� Ű ���� ������ ����. "command id key"�� �������� ���۵ȴ�.
	 * @param key ���� �������ִ� Ű ���� �ش��ϴ� ����. ���� keybuff�� ����Ǿ� �ִ� ����.
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
			keybuff &= ~LEFT_PRESSED;// ��ƼŰ�� ���� ó��
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
		sendKey(keybuff); // delf: ���� Ű�� ������ �����Ѵ�.
		// }
		// PC ȯ�濡���� �⺻������ Ű������ �ݺ��Է��� ����������,
		// �׷��� ���� �÷��������� Ű ���۰��� ���� ������ ������ ���� ������ ����Ʈ ���θ� �����Ѵ�.
	}

	public void keyTyped(KeyEvent e) {
	}

	public final static int TITLE = 0;
	public final static int START = 1;
	public final static int INGAME = 2;
	public final static int GAMEOVER = 3;
	public final static int PAUSE = 4;

	// ���� �Ǵ�, ������ �̺�Ʈ, CPU ���� ó��
	private void process() {
		switch (status) {
		case TITLE:// Ÿ��Ʋȭ��
			break;
		case START:// ��ŸƮ
			processPlayer1();
			processPlayer2();
			if (mymode == APPEARANCE)
				status = INGAME;
			break;
		case INGAME:// ����ȭ��
			processPlayer1();
			processPlayer2();
			processEnemy();
			processBullet();
			processEffect();
			processGameFlow();
			processItem();
			break;
		case GAMEOVER:// ���ӿ���
			processEnemy();
			processBullet();
			processGameFlow();
			break;
		case PAUSE:// �Ͻ�����
			break;
		default:
			break;
		}
		if (status != PAUSE)
			gameCnt++;
	}

	public void setKeybuff() {

	}

	// Ű �Է� ó��
	// Ű �̺�Ʈ���� �Է� ó���� �� ���, �̺�Ʈ ���������� �߻��� �� �����Ƿ� �̺�Ʈ������ Ű ���۸��� �����ϰ�, ���� ������ ���۰��� ���� ó���� �Ѵ�.
	private void keyprocess() {
		switch (status) {
		case TITLE:// Ÿ��Ʋȭ��
			if (keybuff == KeyEvent.VK_SPACE) { // delf: �ӽ÷� keybuff�� ����
				System.out.println("�����̽�");
				initGame();
				initPlayer();
				status = START;
			}
			break;
		case INGAME:// ����ȭ��
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
			// TODO: ���� ���� �ʿ�
			if (gameCnt++ >= 200 && control[G.P1] == KeyEvent.VK_3)
				status = 2;
			break;
		default:
			break;
		}
	}

	// �����ƾ �϶�
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

		gamescreen.bg = makeImage("./rsc/����.JPG");// bg.png
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
		gamescreen.item[2] = makeImage("./rsc/game/item2.png");// ������ �߰�
		gamescreen._start = makeImage("./rsc/game/start.png");
		gamescreen._over = makeImage("./rsc/game/gameover.png");
		gamescreen.shield = makeImage("./rsc/game/shield.png");
		gamescreen.enemy[1] = makeImage("./rsc/game/enemy1.png");// ���� �߰�
		gamescreen.enemy[2] = makeImage("./rsc/game/enemy2.png");// ��ġ �׿����

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
		// mywidth, myheight;//�÷��̾� ĳ������ �ʺ� ����
		mymode = 1;
		pImg = 2;
		mycnt = 0;
		playerLife[G.P1] = 10000000;
		keybuff = 0;

		// delf: �ӽ��ڵ�
		x[G.P2] = 0;
		y[G.P2] = 30000;
		direction[G.P2] = -1;
		playerLife[G.P1] = 10000000;
	}

	/** �÷��̾ ���� ó���� �Ѵ�. �� ��Ȳ�� ���� ��� �ൿ �ؾ����� ó���� */
	public void processPlayer1() { // delf: �޼ҵ� �̸� ������
		Bullet shoot;
		switch (mymode) {
		case APPEARANCE: // delf: ���� ��,
			x[G.P1] += 200; // delf: ���� ��ġ���� ������ �̵�
			if (x[G.P1] > 20000)
				mymode = ONPLAY;
			break;
		case UNBEATABLE: // delf: ����
			if (mycnt-- == 0) { // delf: ���� �ð� ������
				mymode = ONPLAY; // delf: ���� �簳
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
		case APPEARANCE: // ���� ��,
			x[G.P2] += 200; // ���� ��ġ���� ������ �̵�
			if (x[G.P2] > 20000)
				mymode = ONPLAY; // �����ϸ� ���� ����
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
				bullets.remove(i);// ȭ�� ������ ������ �Ѿ� ����
				continue;
			}
			if (bullet.from == 0) {// �÷��̾ �� �Ѿ��� ������ ���� ����
				for (j = 0; j < enemies.size(); j++) {
					enemy = enemies.elementAt(j);
					dist = getDistance(bullet.dis.x, bullet.dis.y, enemy.dis.x, enemy.dis.y);
					// if(dist<1500) {//�߰��� �Ÿ��� ���� ������ ������ ������ ���� ��
					if (dist < enemy.hitrange) {// �߰��� �Ÿ��� ���� ������ ������ ������ ���� �� - hitrange : �� ĳ���͸��� �׸��� ���� ���������Ǵ� ������ �ٸ���
						if (enemy.life-- <= 0) {// �� ������ ����
							if (enemy.kind == 1) {
								if (gameCnt < 2100)
									gameCnt = 2100;
							}
							enemies.remove(j);// �� ĳ���� �Ұ�
							effect = new Effect(0, enemy.pos.x, bullet.pos.y, 0);
							effects.add(effect);// ���� ����Ʈ �߰�
							// Item tem=new Item(ebuff.pos.x, buff.pos.y, RAND(1,(level+1)*20)/((level+1)*20));//���� ����� �ִ밪�� ���� �����Ǵ� �������� 1�� �ȴ�
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
						score[G.P1]++;// ���� �߰�
						bullets.remove(i);// �Ѿ� �Ұ�
						break;// �Ѿ��� �ҰŵǾ����Ƿ� ���� �ƿ�
					}
				}
			}
			else { // ���� �� �Ѿ��� �÷��̾�� ���� ����
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
					else {// �ǵ尡 ���� ���
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
		// ���� ���� �������� �߰�
		if (gamescreen.boss) {
			// ������ �����Ǿ� �ִ� ��Ȳ�� ó��
			if (level > 1) {// ���� ������ 2 �̻��̸�, ������ ���߿� ���� ĳ���͵��� ������� ���´�
				// ������ ���� �ó�����
				// : ����ī��Ʈ(gamecnt) 0~200 : ������ ����
				// : ����ī��Ʈ(gamecnt) 801~1000 : ������ 60ī��Ʈ ������ ����
				// : ����ī��Ʈ(gamecnt) 1601~2199 : ������ 30ī���� ������ ����
				if (800 < gameCnt && gameCnt < 1000) {// ������ �����ϰ� �ð��� �� ������ ���� ĳ���͵��� �������� �����Ѵ�
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
				else if (1600 < gameCnt && gameCnt < 2200) {// �������� �Ĺݺο� ���鼭 ���� ��������� ������ �ż�����
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
			if (gameCnt > 2210) {// ������ Ÿ�� �ƿ����� �������� �����Ѵ�
				gamescreen.boss = false;
				gameCnt = 0;
				System.out.println("���� Ÿ�Ӿƿ�");
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
				// ������ ������ �÷��ִ� �κп���, ������ �÷��ָ鼭 ���� ĳ���͸� �����Ų��
				System.out.println("���� ����");
				gamescreen.boss = true;
				Enemy en = new Enemy(this, 1, screenWidth * 100, 24000, 1, 0);// img ���� 1, kind ���� 1
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
			if (dist < 1000) {// ������ ȹ��
				switch (buff.kind) {
				case SCORE:// �Ϲ� ����
					score[G.P1] += 100;
					break;
				case SHIELD:// �ǵ�
					myshield = 5;
					break;
				case DESTROY_ALL_ENEMIES:// ���� ������
					// Enemy ebuff;
					// Effect expl;

					// �� ���� ȿ��
					int j = enemies.size();
					for (int k = 0; k < j; k++) {
						Enemy ebuff = (Enemy) (enemies.elementAt(k));
						if (ebuff == null)
							continue;// ���� �ش� �ε����� �� ĳ���Ͱ� �����Ǿ����� ���� ��츦 ���
						if (ebuff.kind == 1) {// �ش� �ε����� �Ҵ�� �� ĳ���Ͱ� ���� ĳ������ ���� ���꿡 �ش����� �ʰ� HP�� ������ ���δ�. 1 ���϶�� 1.
							score[G.P1] += 300;
							ebuff.life = ebuff.life / 2;
							if (ebuff.life <= 1)
								ebuff.life = 1;
							continue;
						}
						Effect expl = new Effect(0, ebuff.pos.x, ebuff.pos.y, 0);
						effects.add(expl);// ���� ����Ʈ �߰�
						ebuff.pos.x = -10000;// ���� ó������ �Ұŵ� �� �ֵ���
						score[G.P1] += 50;
						// enemies.remove(ebuff);//�� ĳ���� �Ұ�
					}

					// �� �Ѿ� ���� �Ұ�
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
			// �������
			MediaTracker mt = new MediaTracker(this);
			mt.addImage(img, 0);
			mt.waitForID(0);
			// �������, getImage�� �о���� �̹����� �ε��� �Ϸ�ƴ��� Ȯ���ϴ� �κ�
		} catch (Exception ee) {
			ee.printStackTrace();
			return null;
		}
		return img;
	}

	public int getDistance(int x1, int y1, int x2, int y2) {
		return Math.abs((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}

	public int RAND(int startnum, int endnum) // ��������(startnum���� ramdom����), �������� ����� ����.
	{
		int a, b;
		if (startnum < endnum)
			b = endnum - startnum; // b�� ���� ���� �߻� ��
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