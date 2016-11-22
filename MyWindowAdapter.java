import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MyWindowAdapter extends WindowAdapter {
	// 윈도우를 닫기 위한 부가 클래스. 실제 닫는 동작은
	// setVisible(false);
	// dispose();
	// System.exit(0);
	// 이상 세 라인으로 이루어진다.
	//
	MyWindowAdapter() {
	}

	public void windowClosing(WindowEvent e) {
		Window wnd = e.getWindow();
		wnd.setVisible(false);
		wnd.dispose();
		System.exit(0);
	}
}