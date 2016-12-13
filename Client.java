import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/** @author delf */
public class Client extends Thread {

	public final static int BUFSIZE = 128;
	public final static int CMD = 0;
	public final static int ID = 1;
	public final static int KEY = 2;
	public final static int DELAY = 3;

	private InetAddress serverIP ;
	private int port = 13131;
	// private Socket socket = new Socket();
	private DatagramSocket sndSocket;
	private DatagramSocket rcvSocket;
	// private DataInputStream dis;
	// private DataOutputStream dos;
	private MainFrame game;
	private MessageQueue messageQueue;
	private int id;

	private int delay;

	public static int SENDPORT = 13131;
	public static int RECEIVEPORT = 13132;

	public Client() throws UnknownHostException {
		serverIP = InetAddress.getByName("113.198.81.16");
		connectServer();
		System.out.println("ストライクウィッチ-ズ Start!");
		game = new MainFrame(this);
		messageQueue = new MessageQueue(this);
	}

	/** 서버와 연결 */
	public void connectServer() {
		// setSocketPort(13132);
		while (true) {
			try {
				// sndSocket = new DatagramSocket(SENDPORT); // 전송용 소켓
				rcvSocket = new DatagramSocket(RECEIVEPORT); // 수신용 소켓
				if (rcvSocket != null) { // socket이 null값이 아닐때 즉! 연결되었을때
					System.out.println("> 클라이언트 수신스레드 시작 (receive port: " + RECEIVEPORT + ")");
					start(); // 수신 스레드 시작
					break;
				}
			} catch (BindException be) {
				// be.printStackTrace();
				RECEIVEPORT++;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		System.out.println("> 서버에게 접속을 알림");
		String msg = createMsg(G.ACCESS); // delf: 접속을 알림
		sendMsg(msg);
	}

	@Override
	public void run() {
		// 받을 패킷을 저장
		byte[] bb = new byte[BUFSIZE];
		DatagramPacket rcvPacket = new DatagramPacket(bb, bb.length);
		while (true) {
			try {
				rcvSocket.receive(rcvPacket); // delf :메시지 수신
				handlingMsg(new String(bb)); // delf: 받은 메시지에 따라 적절한 처리를 한다.
				initByte(bb);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} // end of loop
	} // end of thread

	/** 서버로부터 받은 메시지를 분석하고 명령어에 따라 적절한 처리를 한다.
	 * @param msg 서버로 부터 받은 메시지 */
	public void handlingMsg(String msg) {

		String splitMsg[];
		msg = msg.trim(); // delf: 받은 메시지를 쪼갠다.
		splitMsg = msg.split(G.BLANK); // delf: 빈칸을 기준으로 나누어 담는다.
		int target = Integer.parseInt(splitMsg[ID]);
		if (!splitMsg[CMD].equals("/Test")) {
			System.out.println("recv msg from Server = " + msg);
		}

		switch (splitMsg[CMD]) { // delf: 받은 메세지의 명령어가
		case G.KEY: // delf: "키 변경" 이라면
			messageQueue.enQueue(msg);
			// delay = setDelay(Integer.parseInt(splitMsg[DELAY]));
			// Thread.sleep(delay);
			// game.control[target] = Integer.parseInt(splitMsg[KEY]); // delf: 타겟의 키 값을 다음과 같이 설정
			break;

		case G.ACCESS:
			System.out.println("받은 id = " + splitMsg[ID]);
			this.id = target;
			break;

		case G.READY:
			System.out.println("서버로부터 시작명령을 받음");
			game.initCnt();
			game.setGameStatus(MainFrame.START);

			break;
		case "/Test":
			sendMsg(msg);
			break;

		default:
			break;
		}
	}

	public int setDelay(int d) {
		if (d < 0) {
			return 0;
		}
		return d;
	}

	/** 타겟의 키 값을 다음과 같이 설정 */
	public void setControl(int id, int key) {
		game.control[id] = key;
	}

	public static void main(String[] args) throws UnknownHostException {
		new Client();
	}

	public int getPlayerId() {
		return this.id;
	}

	public void sendMsg(String msg) {
		if (!((msg.split(" ")[0]).equals("/Test"))) {
			System.out.println("send msg to server[" + SENDPORT + "]: " + msg);
		}
		try {
			byte[] bb = new byte[BUFSIZE];
			bb = msg.getBytes();
			DatagramPacket sendData = new DatagramPacket(bb, bb.length, serverIP, SENDPORT);
			rcvSocket.send(sendData);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/** 입력 받은 파라미터들로 프로토콜 형식으로 만든다.
	 * @param par 프로토콜을 만드는 요소 문자열
	 * @author delf */
	public static String createMsg(String... par) {
		String msg = "";
		for (int i = 0; i < par.length - 1; i++) {
			msg += par[i] + G.BLANK;
		}
		msg += par[par.length - 1];
		return msg;
	}


	private void initByte(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			b[i] = 0;
		}
	}
}
