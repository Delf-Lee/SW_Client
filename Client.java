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
		System.out.println("���ȫ髤�������ë�-�� Start!");
		game = new MainFrame(this);
		messageQueue = new MessageQueue(this);
	}

	/** ������ ���� */
	public void connectServer() {
		// setSocketPort(13132);
		while (true) {
			try {
				// sndSocket = new DatagramSocket(SENDPORT); // ���ۿ� ����
				rcvSocket = new DatagramSocket(RECEIVEPORT); // ���ſ� ����
				if (rcvSocket != null) { // socket�� null���� �ƴҶ� ��! ����Ǿ�����
					System.out.println("> Ŭ���̾�Ʈ ���Ž����� ���� (receive port: " + RECEIVEPORT + ")");
					start(); // ���� ������ ����
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
				initByte(bb);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} // end of loop
	} // end of thread

	/** �����κ��� ���� �޽����� �м��ϰ� ��ɾ ���� ������ ó���� �Ѵ�.
	 * @param msg ������ ���� ���� �޽��� */
	public void handlingMsg(String msg) {

		String splitMsg[];
		msg = msg.trim(); // delf: ���� �޽����� �ɰ���.
		splitMsg = msg.split(G.BLANK); // delf: ��ĭ�� �������� ������ ��´�.
		int target = Integer.parseInt(splitMsg[ID]);
		if (!splitMsg[CMD].equals("/Test")) {
			System.out.println("recv msg from Server = " + msg);
		}

		switch (splitMsg[CMD]) { // delf: ���� �޼����� ��ɾ
		case G.KEY: // delf: "Ű ����" �̶��
			messageQueue.enQueue(msg);
			// delay = setDelay(Integer.parseInt(splitMsg[DELAY]));
			// Thread.sleep(delay);
			// game.control[target] = Integer.parseInt(splitMsg[KEY]); // delf: Ÿ���� Ű ���� ������ ���� ����
			break;

		case G.ACCESS:
			System.out.println("���� id = " + splitMsg[ID]);
			this.id = target;
			break;

		case G.READY:
			System.out.println("�����κ��� ���۸���� ����");
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

	/** Ÿ���� Ű ���� ������ ���� ���� */
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


	private void initByte(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			b[i] = 0;
		}
	}
}
