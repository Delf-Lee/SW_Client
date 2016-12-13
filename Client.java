import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/** @author delf */
public class Client extends Thread {

	public final static int BUFSIZE = 128;
	public final static int CMD = 0;
	public final static int ID = 1;
	public final static int KEY = 2;

	private InetAddress serverIP = InetAddress.getLoopbackAddress();
	private int port = 13131;
	// private Socket socket = new Socket();
	private DatagramSocket sndSocket;
	private DatagramSocket rcvSocket;
	// private DataInputStream dis;
	// private DataOutputStream dos;
	private MainFrame game;
	private int id;

	public static int SENDPORT = 13131;
	public static int RECEIVEPORT = 13132;

	public Client() {
		connectServer();
		System.out.println("ストライクウィッチ-ズ Start!");
		game = new MainFrame(this);
	}

	// delf: udp 변경 부분
	/** 서버와 연결 */
	public void connectServer() {
		// setSocketPort(13132);
		while (true) {
			try {
				// sndSocket = new DatagramSocket(SENDPORT); // 전송용 소켓
				rcvSocket = new DatagramSocket(RECEIVEPORT); // 수신용 소켓
				if (rcvSocket != null) { // socket이 null값이 아닐때 즉! 연결되었을때
					System.out.println("> 클라이언트 수신스레드 시작");
					start(); // 수신 스레드 시작
					break;
				}
			} catch (BindException be) {
				//be.printStackTrace();
				System.out.println(RECEIVEPORT);
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		} // end of loop
	} // end of thread

	/** 서버로부터 받은 메시지를 분석하고 명령어에 따라 적절한 처리를 한다.
	 * @param msg 서버로 부터 받은 메시지 */
	public void handlingMsg(String msg) {
		System.out.println("recv msg from Server = " + msg);

		String splitMsg[];
		msg = msg.trim(); // delf: 받은 메시지를 쪼갠다.
		splitMsg = msg.split(G.BLANK); // delf: 빈칸을 기준으로 나누어 담는다.
		int target = Integer.parseInt(splitMsg[ID]); // delf: 메시지에 대한 타겟을 저장

		switch (splitMsg[CMD]) { // delf: 받은 메세지의 명령어가
		case G.KEY: // delf: "키 변경" 이라면
			game.control[target] = Integer.parseInt(splitMsg[KEY]); // delf: 타겟의 키 값을 다음과 같이 설정
			break;
		case G.ACCESS:
			this.id = Integer.parseInt(splitMsg[ID]);
			break;

		default:
			break;
		}
	}

	public static void main(String[] args) {
		new Client();
	}

	/** 서버에게 메시지를 전송한다.
	 * @param str 서버에게 전송할 메시지 문자열 */
	// public void sendMsg(String str) {
	// byte[] bb = new byte[BUFSIZE];
	// bb = str.getBytes();
	// try {
	// dos.write(bb);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	/** 서버로 부터 메시지를 받아 저장하고 반환한다.
	 * @return 서버로부터 받은 메시지 문자열 */
	// private String readMeg() throws IOException {
	// byte[] buffer = new byte[BUFSIZE];
	// String msg;
	//
	// dis.read(buffer); // 대기
	// msg = new String(buffer);
	// msg = msg.trim(); // delf: 공백 정리
	// System.out.println("받은 메세지: " + msg);
	//
	// return msg;
	// }

	public int getPlayerId() {
		return this.id;
	}

	public void sendMsg(String msg) {
		System.out.println("send msg to server[" + SENDPORT + "]: " + msg);
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

	/** 소켓의 포트를 설정한다.
	 * @param port 수신소켓에 할당될 포트번호 전송소켓 포트번호는 port+1로 할당된다. */
	private void setSocketPort(int port) {
		SENDPORT = port;
		RECEIVEPORT = port + 1;
	}
}
