package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.exceptions.MyImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserClass {
	public void gradeCode() {
		MyImpl tryTarget = MyImpl.instance();
		TConsumer<?> consumer = i -> {};
		MyImpl got = tryTarget.andConsume(consumer);
	}
}
