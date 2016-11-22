import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Thread {

	private Socket socket = new Socket();
	private ObjectOutputStream oos;
	private ObjectInputStream ois;

	public Client() {
		connectServer();
		System.out.println("ストライクウィッチ-ズ Start!");
		ShootingFrame wsf = new ShootingFrame();
	}

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
	}

	public void Connection() { // 실직 적인 메소드 연결부분
		try { // 스트림 설정
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("스트림 설정 에러");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// while (true) {
		// try {
		//
		// } catch (IOException e) {
		// System.out.println("메시지 수선 에러");
		// // 서버와 소켓 통신에 문제가 생겼을 경우 소켓을 닫는다
		// try {
		// ois.close();
		// oos.close();
		// socket.close();
		// break; // 에러 발생하면 while문 종료
		// } catch (IOException e1) {
		// e.printStackTrace();
		// }
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// }
		// } // while문 끝
	}// run메소드 끝

	public static void main(String[] args) {
		new Client();
	}
}
