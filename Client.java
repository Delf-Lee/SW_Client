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
		System.out.println("���ȫ髤�������ë�-�� Start!");
		ShootingFrame wsf = new ShootingFrame();
	}

	public void connectServer() {

		try {
			socket = new Socket("127.0.0.1", 30023);
			System.out.println("��������");
			if (socket != null) {// socket�� null���� �ƴҶ� ��! ����Ǿ�����
				Connection(); // ���� �޼ҵ带 ȣ��
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("���� ���� ����");
			e.printStackTrace();
		}
	}

	public void Connection() { // ���� ���� �޼ҵ� ����κ�
		try { // ��Ʈ�� ����
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("��Ʈ�� ���� ����");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// while (true) {
		// try {
		//
		// } catch (IOException e) {
		// System.out.println("�޽��� ���� ����");
		// // ������ ���� ��ſ� ������ ������ ��� ������ �ݴ´�
		// try {
		// ois.close();
		// oos.close();
		// socket.close();
		// break; // ���� �߻��ϸ� while�� ����
		// } catch (IOException e1) {
		// e.printStackTrace();
		// }
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// }
		// } // while�� ��
	}// run�޼ҵ� ��

	public static void main(String[] args) {
		new Client();
	}
}
