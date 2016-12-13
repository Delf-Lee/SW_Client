import java.util.Vector;

public class MessageQueue extends Thread {
	public final static int ID = 1;
	public final static int KEY = 2;
	public final static int DELAY = 3;

	private Vector<Message> messages = new Vector<Message>(); // 메시지를을 저장할 Vector
	private Client client;
	private long lastEnQueueTime = getNow();
	private long interval;
	private boolean init = true;

	public MessageQueue(Client client) {
		this.client = client;
		start();
	}

	public void enQueue(String msg) {
		if (init) {
			lastEnQueueTime = getNow();
			init = false;
		}
		String splitMsg[];
		msg = msg.trim();
		splitMsg = msg.split(G.BLANK);

		int id = Integer.parseInt(splitMsg[ID]);
		int key = Integer.parseInt(splitMsg[KEY]);
		long netDelay = calcDelay(splitMsg[DELAY]);
		interval = getNow() - lastEnQueueTime;
		lastEnQueueTime = getNow();
		System.out.println("interval = " + interval);
		System.out.println("netDelay = " + netDelay);

		long delay = netDelay + interval; // (int) (lastEnQueueTime & 0xffffffffL);
		Message message = new Message(id, key, delay);
		messages.add(message);
	}

	public long calcDelay(String str) {
		if (Integer.parseInt(str) < 0) {
			return 0;
		}
		return Long.parseLong(str);
	}

	private long getNow() {
		return System.currentTimeMillis();
	}

	private Message deQueue() {
		if (!messages.isEmpty()) {
			return messages.remove(0);
		}
		return null;
	}

	@Override
	public void run() {
		while (true) {
			// System.out.println("size = " + messages.size());
			Message thisMsg = deQueue();
			if (thisMsg == null) {
				init = true;
				continue;
			}
			try {
				sleep(thisMsg.delay);
				// sleep(500);
				client.setControl(thisMsg.id, thisMsg.key);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

/** 메시지 구조 */
class Message {
	int id; // 보낸 플레이어 id
	int key; // 입력한 키 값
	long delay; // (netword delay) + (front msg와 rear msg 사이 시간)

	public Message(int id, int key, long delay) {
		super();
		this.id = id;
		this.key = key;
		this.delay = delay;
	}
}
