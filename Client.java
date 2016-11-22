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
		System.out.println("���ȫ髤�������ë�-�� Start!");
		ShootingFrame wsf = new ShootingFrame(this);
	}

	/** ������ ���� */
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
		System.out.println("���� ��");
	}

	/** ��Ʈ�� ���� */
	public void Connection() { // ���� ���� �޼ҵ� ����κ�
		try { // ��Ʈ�� ����
			System.out.println("��Ʈ�� ����");
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("��Ʈ�� ���� ����");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				readMeg();
			} catch (IOException e) {
				System.out.println("�޽��� ���� ����");
				// ������ ���� ��ſ� ������ ������ ��� ������ �ݴ´�
				try {
					dis.close();
					dos.close();
					socket.close();
					break; // ���� �߻��ϸ� while�� ����
				} catch (IOException e1) {
					e.printStackTrace();
				}
			}
		} // while�� ��
	} // run�޼ҵ� ��

	public static void main(String[] args) {
		new Client();
	}

	/** �������� �޽����� �����Ѵ�. 
	 * @param str �������� ������ �޽��� ���ڿ� */
	public void sendMsg(String str) {
		System.out.println("�������� �޽���: " + str);
		byte[] bb = new byte[128];
		bb = str.getBytes();
		try {
			dos.write(bb);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/** ������ ���� �޽����� �޾� �����ϰ� ��ȯ�Ѵ�. 
	 * @return �����κ��� ���� �޽��� ���ڿ� */
	private String readMeg() throws IOException {
		byte[] buffer = new byte[128];
		String msg;
		String splitMsg[];
		dis.read(buffer); // ���
		msg = new String(buffer);
		System.out.println("���� �޼���: " + msg);

		return msg;
		// msg = msg.trim();
		// splitMsg = msg.split(" ");
		// return null;
	}

}
