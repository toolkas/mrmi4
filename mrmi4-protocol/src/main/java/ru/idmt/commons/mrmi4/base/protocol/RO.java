package ru.idmt.commons.mrmi4.base.protocol;

import java.io.Serializable;

public class RO implements Serializable {
	private static final long serialVersionUID = 1188930664782255584L;

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
