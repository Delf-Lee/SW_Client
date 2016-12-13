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
		System.out.println("���ȫ髤�������ë�-�� Start!");
		game = new MainFrame(this);
	}

	// delf: udp ���� �κ�
	/** ������ ���� */
	public void connectServer() {
		// setSocketPort(13132);
		while (true) {
			try {
				// sndSocket = new DatagramSocket(SENDPORT); // ���ۿ� ����
				rcvSocket = new DatagramSocket(RECEIVEPORT); // ���ſ� ����
				if (rcvSocket != null) { // socket�� null���� �ƴҶ� ��! ����Ǿ�����
					System.out.println("> Ŭ���̾�Ʈ ���Ž����� ����");
					start(); // ���� ������ ����
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
		System.out.println("> �������� ������ �˸�");
		String msg = createMsg(G.ACCESS); // delf: ������ �˸�
		sendMsg(msg);
	}

	@Override
	public void run() {
		// ���� ��Ŷ�� ����
		byte[] bb = new byte[BUFSIZE];
		DatagramPacket rcvPacket = new DatagramPacket(bb, bb.length);
		while (true) {
			try {
				rcvSocket.receive(rcvPacket); // delf :�޽��� ����
				handlingMsg(new String(bb)); // delf: ���� �޽����� ���� ������ ó���� �Ѵ�.
			} catch (IOException e) {
				e.printStackTrace();
			}
		} // end of loop
	} // end of thread

	/** �����κ��� ���� �޽����� �м��ϰ� ��ɾ ���� ������ ó���� �Ѵ�.
	 * @param msg ������ ���� ���� �޽��� */
	public void handlingMsg(String msg) {
		System.out.println("recv msg from Server = " + msg);

		String splitMsg[];
		msg = msg.trim(); // delf: ���� �޽����� �ɰ���.
		splitMsg = msg.split(G.BLANK); // delf: ��ĭ�� �������� ������ ��´�.
		int target = Integer.parseInt(splitMsg[ID]); // delf: �޽����� ���� Ÿ���� ����

		switch (splitMsg[CMD]) { // delf: ���� �޼����� ��ɾ
		case G.KEY: // delf: "Ű ����" �̶��
			game.control[target] = Integer.parseInt(splitMsg[KEY]); // delf: Ÿ���� Ű ���� ������ ���� ����
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

	/** �������� �޽����� �����Ѵ�.
	 * @param str �������� ������ �޽��� ���ڿ� */
	// public void sendMsg(String str) {
	// byte[] bb = new byte[BUFSIZE];
	// bb = str.getBytes();
	// try {
	// dos.write(bb);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	/** ������ ���� �޽����� �޾� �����ϰ� ��ȯ�Ѵ�.
	 * @return �����κ��� ���� �޽��� ���ڿ� */
	// private String readMeg() throws IOException {
	// byte[] buffer = new byte[BUFSIZE];
	// String msg;
	//
	// dis.read(buffer); // ���
	// msg = new String(buffer);
	// msg = msg.trim(); // delf: ���� ����
	// System.out.println("���� �޼���: " + msg);
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

	/** �Է� ���� �Ķ���͵�� �������� �������� �����.
	 * @param par ���������� ����� ��� ���ڿ�
	 * @author delf */
	public static String createMsg(String... par) {
		String msg = "";
		for (int i = 0; i < par.length - 1; i++) {
			msg += par[i] + G.BLANK;
		}
		msg += par[par.length - 1];
		return msg;
	}

	/** ������ ��Ʈ�� �����Ѵ�.
	 * @param port ���ż��Ͽ� �Ҵ�� ��Ʈ��ȣ ���ۼ��� ��Ʈ��ȣ�� port+1�� �Ҵ�ȴ�. */
	private void setSocketPort(int port) {
		SENDPORT = port;
		RECEIVEPORT = port + 1;
	}
}
