package ru.idmt.commons.mrmi4.example.server;

import com.spellmaster.micrormi.LocalObjectManager;
import com.spellmaster.micrormi.MicroRMIFactory;

import java.io.IOException;

public class MrmiExampleServer {
	public static void main(String[] args) throws IOException {
		MicroRMIFactory factory = new MicroRMIFactory(10, 10000000);
		LocalObjectManager objectManager = factory.startServer(6969);
		objectManager.register("example", new ExampleImpl());
	}
}
