package io.openems.backend.metadata.gridvolt;

import java.time.ZonedDateTime;

import io.openems.backend.common.metadata.Edge;

public class MyEdge extends Edge {

	private final String apikey;

	public MyEdge(MetadataGridvolt parent, String edgeId, String apikey, String comment, String version,
			String producttype, ZonedDateTime lastMessage) {
		super(parent, edgeId, comment, version, producttype, lastMessage);
		this.apikey = apikey;
	}

	public String getApikey() {
		return this.apikey;
	}

}

