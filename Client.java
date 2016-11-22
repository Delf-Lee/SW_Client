import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Thread {

	private Socket socket = new Socket();
	private DataInputStream dis;
	private DataOutputStream dos;

	public Client() {
		connectServer();
		System.out.println("ストライクウィッチ-ズ Start!");
		ShootingFrame wsf = new ShootingFrame(this);
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
		System.out.println("설정 끝");
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
				readMeg();
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

	public static void main(String[] args) {
		new Client();
	}

	/** 서버에게 메시지를 전송한다. 
	 * @param str 서벙베게 전송할 메시지 문자열 */
	public void sendMsg(String str) {
		System.out.println("보내려는 메시지: " + str);
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
		String splitMsg[];
		dis.read(buffer); // 대기
		msg = new String(buffer);
		System.out.println("받은 메세지: " + msg);

		return msg;
		// msg = msg.trim();
		// splitMsg = msg.split(" ");
		// return null;
	}

}
