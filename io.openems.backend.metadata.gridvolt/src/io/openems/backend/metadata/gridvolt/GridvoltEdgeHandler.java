package io.openems.backend.metadata.gridvolt;

import java.sql.SQLException;

import io.openems.backend.common.metadata.EdgeHandler;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.EdgeConfig;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;

public class GridvoltEdgeHandler implements EdgeHandler {
	
	private final MetadataGridvolt parent;
	
	protected GridvoltEdgeHandler(MetadataGridvolt parent) {
		this.parent = parent;
	}
	
	@Override
	public EdgeConfig getEdgeConfig(String edgeId) throws OpenemsNamedException {
		try {
			return this.parent.postgresHandler.edge.getEdgeConfig(edgeId);
		} catch (SQLException e) {
			throw new OpenemsException("Unable to read to EdgeConfig for [" + edgeId + "]: " + e.getMessage());
		}
	}
}
