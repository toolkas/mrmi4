package ru.idmt.commons.mrmi4.base.protocol;

import java.io.Serializable;

public class RA implements Serializable {
	private static final long serialVersionUID = -2188848504221507668L;

	private long uid;

	public RA(long uid) {
		this.uid = uid;
	}

	public long getUid() {
		return uid;
	}
}
