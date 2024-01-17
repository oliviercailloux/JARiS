package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.exceptions.TryCatchAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraderVarious {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(GraderVarious.class);

	public void gradeCode() {
		final TryCatchAll<Integer> tryTarget = TryCatchAll.instance();
		TConsumer<? super Integer, ?> consumer = i -> {};
		final TryCatchAll<Integer> got = tryTarget.andConsume(consumer);
		LOGGER.info("Got: {}.", got);
	}
}
