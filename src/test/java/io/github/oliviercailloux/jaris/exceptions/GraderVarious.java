package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.exceptions.TryCatchAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraderVarious {
	public void gradeCode() {
		TryCatchAll<Integer> tryTarget = TryCatchAll.instance();
		TConsumer< ?> consumer = i -> {};
		TryCatchAll<Integer> got = tryTarget.andConsume(consumer);
	}
}
