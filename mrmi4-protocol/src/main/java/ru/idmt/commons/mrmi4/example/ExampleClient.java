package ru.idmt.commons.mrmi4.example;

import ru.idmt.commons.mrmi4.api.client.RClient;
import ru.idmt.commons.mrmi4.api.client.RSession;
import ru.idmt.commons.mrmi4.impl.async.client.AsyncClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class ExampleClient {
	public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
		RClient client = new AsyncClient(new ExampleUIDManager());

		RSession session = null;
		try {
			session = client.connect("localhost", 6969);
			final IExample example = session.get(IExample.class);

			final List<Double> results = new ArrayList<Double>();

			int threads = 10;
			final CountDownLatch latch = new CountDownLatch(threads);
			for (int index = 0; index < threads; index++) {
				new Thread() {
					@Override
					public void run() {
						try {
							Monitor monitor = testAction(new Action() {
								public void execute() throws InterruptedException, TimeoutException, IOException {
									example.getList();
								}
							}, 100000);

							results.add(monitor.avg());
							latch.countDown();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.start();
			}

			latch.await();

			double sum = 0;
			for (double result : results) {
				sum += result;
			}

			System.out.println(sum/results.size());
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	private static Monitor testAction(Action action, final int count) throws InterruptedException, IOException, TimeoutException {
		final long start = System.currentTimeMillis();
		for (int index = 0; index < count; index++) {
			action.execute();
		}
		final long finish = System.currentTimeMillis();

		return new Monitor() {
			public double avg() {
				return (finish - start) / (double) count;
			}
		};
	}

	public interface Action {
		void execute() throws InterruptedException, TimeoutException, IOException;
	}

	public interface Monitor {
		double avg();
	}
}