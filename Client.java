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
		System.out.println("���ȫ髤�������ë�-�� Start!");
		game = new MainFrame(this);
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
		start();
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
				handlingMsg(readMeg()); // delf: ���� �޽����� ���� ������ ó���� �Ѵ�.
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

	/** �����κ��� ���� �޽����� �м��ϰ� ��ɾ ���� ������ ó���� �Ѵ�.
	 * @param msg ������ ���� ���� �޽��� */
	public void handlingMsg(String msg) {
		String splitMsg[];
		// msg = msg.trim(); // delf: ���� �޽����� �ɰ���.
		splitMsg = msg.split(G.BLANK); // delf: ��ĭ�� �������� ������ ��´�.
		int target = Integer.parseInt(splitMsg[ID]); // delf: �޽����� ���� Ÿ���� ����

		switch (splitMsg[CMD]) { // delf: ���� �޼����� ��ɾ
		case G.KEY: // delf: "Ű ����" �̶��
			game.control[target] = Integer.parseInt(splitMsg[KEY]); // delf: Ÿ���� Ű ���� ������ ���� ����
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

	/** �������� �޽����� �����Ѵ�.
	 * @param str �������� ������ �޽��� ���ڿ� */
	public void sendMsg(String str) {
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

		dis.read(buffer); // ���
		msg = new String(buffer);
		msg = msg.trim(); // delf: ���� �޽����� �ɰ���.
		System.out.println("���� �޼���: " + msg);

		return msg;
	}

	public int getPlayerId() {
		return this.id;
	}
}
