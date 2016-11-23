import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
/** @author delf */
public class Client extends Thread {

	public final static int CMD = 0;
	public final static int ID = 1;
	public final static int KEY = 2;

	private Socket socket = new Socket();
	private DataInputStream dis;
	private DataOutputStream dos;
	private MainFrame game;
	private int id;

	public Client() {
		connectServer();
		System.out.println("ストライクウィッチ-ズ Start!");
		game = new MainFrame(this);
	}

	/** 서버와 연결 */
	public void connectServer() {
		try {
			socket = new Socket("127.0.0.1", 30023);
			System.out.println("서버연결");
			if (socket != null) {// socket이 null값이 아닐때 즉! 연결되었을때
				Connection(); // 연결 메소드를 호출
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("소켓 연결 에러");
			e.printStackTrace();
		}
		start();
	}

	/** 스트림 설정 */
	public void Connection() { // 실직 적인 메소드 연결부분
		try { // 스트림 설정
			System.out.println("스트림 설정");
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("스트림 설정 에러");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				handlingMsg(readMeg()); // delf: 받은 메시지에 따라 적절한 처리를 한다.
			} catch (IOException e) {
				System.out.println("메시지 수선 에러");
				// 서버와 소켓 통신에 문제가 생겼을 경우 소켓을 닫는다
				try {
					dis.close();
					dos.close();
					socket.close();
					break; // 에러 발생하면 while문 종료
				} catch (IOException e1) {
					e.printStackTrace();
				}
			}
		} // while문 끝
	} // run메소드 끝

	/** 서버로부터 받은 메시지를 분석하고 명령어에 따라 적절한 처리를 한다.
	 * @param msg 서버로 부터 받은 메시지 */
	public void handlingMsg(String msg) {
		String splitMsg[];
		// msg = msg.trim(); // delf: 받은 메시지를 쪼갠다.
		splitMsg = msg.split(G.BLANK); // delf: 빈칸을 기준으로 나누어 담는다.
		int target = Integer.parseInt(splitMsg[ID]); // delf: 메시지에 대한 타겟을 저장

		switch (splitMsg[CMD]) { // delf: 받은 메세지의 명령어가
		case G.KEY: // delf: "키 변경" 이라면
			game.control[target] = Integer.parseInt(splitMsg[KEY]); // delf: 타겟의 키 값을 다음과 같이 설정
			break;
		case G.MYID:
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
	 * @param str 서벙베게 전송할 메시지 문자열 */
	public void sendMsg(String str) {
		byte[] bb = new byte[128];
		bb = str.getBytes();
		try {
			dos.write(bb);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** 서버로 부터 메시지를 받아 저장하고 반환한다.
	 * @return 서버로부터 받은 메시지 문자열 */
	private String readMeg() throws IOException {
		byte[] buffer = new byte[128];
		String msg;

		dis.read(buffer); // 대기
		msg = new String(buffer);
		msg = msg.trim(); // delf: 받은 메시지를 쪼갠다.
		System.out.println("받은 메세지: " + msg);

		return msg;
	}

	public int getPlayerId() {
		return this.id;
	}
}
