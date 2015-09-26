package ru.idmt.commons.mrmi4.example;

import com.spellmaster.micrormi.LocalObjectManager;
import com.spellmaster.micrormi.MicroRMIFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MrmiExampleServer {
	public static void main(String[] args) throws IOException {
		final List<String> list = new ArrayList<String>();
		for (int index = 0; index < 100; index++) {
			list.add("TEST");
		}

		MicroRMIFactory factory = new MicroRMIFactory(10, 10000000);
		LocalObjectManager objectManager = factory.startServer(6969);
		objectManager.register("example", new IExample() {
			public String getValue() {
				return "TESTTESTTESTTESTTESTTESTTESTTEST";
			}

			public List<String> getList() {
				return list;
			}
		});
	}
}
