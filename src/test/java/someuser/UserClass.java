package someuser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import somebug.MyImpl;
import somebug.TConsumer;

public class UserClass {
	public void gradeCode() {
		MyImpl tryTarget = MyImpl.instance();
		TConsumer<?> consumer = i -> {};
		MyImpl got = tryTarget.andConsume(consumer);
	}
}
