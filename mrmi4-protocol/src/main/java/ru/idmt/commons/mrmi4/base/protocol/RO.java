package ru.idmt.commons.mrmi4.base.protocol;

public class RO {
	private short[] classUIDs;
	private long uid;

	public RO(short[] classUIDs, long uid) {
		this.classUIDs = classUIDs;
		this.uid = uid;
	}

	public short[] getClassUIDs() {
		return classUIDs;
	}

	public long getUid() {
		return uid;
	}
}
