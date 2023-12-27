package io.openems.backend.metadata.gridvolt.postgres;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.openems.backend.common.metadata.Edge;
import io.openems.backend.metadata.gridvolt.MyEdge;
import io.openems.common.channel.Level;
import io.openems.common.utils.ThreadPoolUtils;

/**
 * This worker combines writes to lastMessage and lastUpdate fields, to avoid
 * DDOSing Postgres by writing too often.
 */
public class PeriodicWriteWorker {

	/**
	 * DEBUG_MODE activates printing of reqular statistics about queued tasks.
	 */
	private static final boolean DEBUG_MODE = true;

	private static final int UPDATE_INTERVAL_IN_SECONDS = 120;

	private final Logger log = LoggerFactory.getLogger(PeriodicWriteWorker.class);
	private final PostgresHandler parent;

	/**
	 * Holds the scheduled task.
	 */
	private ScheduledFuture<?> future = null;

	/**
	 * Executor for subscriptions task.
	 */
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10,
			new ThreadFactoryBuilder().setNameFormat("Metadata.Gridvolt.PGPeriodic-%d").build());

	public PeriodicWriteWorker(PostgresHandler parent) {
		this.parent = parent;
	}

	/**
	 * Starts the {@link PeriodicWriteWorker}.
	 */
	public synchronized void start() {
		this.future = this.executor.scheduleWithFixedDelay(//
				() -> this.task.accept(this.parent.edge), //
				PeriodicWriteWorker.UPDATE_INTERVAL_IN_SECONDS, PeriodicWriteWorker.UPDATE_INTERVAL_IN_SECONDS,
				TimeUnit.SECONDS);
	}

	/**
	 * Stops the {@link PeriodicWriteWorker}.
	 */
	public synchronized void stop() {
		// unsubscribe regular task
		if (this.future != null) {
			this.future.cancel(true);
		}
		// Shutdown executor
		ThreadPoolUtils.shutdownAndAwaitTermination(this.executor, 5);
	}

	private final LinkedBlockingQueue<String> lastMessageEdgeIds = new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<String> isOnlineEdgeIds = new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<String> isOfflineEdgeIds = new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<String> sumStateOk = new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<String> sumStateInfo = new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<String> sumStateWarning = new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<String> sumStateFault = new LinkedBlockingQueue<>();

	private final Consumer<PgEdgeHandler> task = edge -> {
		if (PeriodicWriteWorker.DEBUG_MODE) {
			this.debugLog();
		}

		try {
			// Last Message
			edge.updateLastMessage(drainToSet(this.lastMessageEdgeIds));

			// Online/Offline
			edge.updateOpenemsIsConnected(drainToSet(this.isOfflineEdgeIds), false);
			edge.updateOpenemsIsConnected(drainToSet(this.isOnlineEdgeIds), true);

			// Sum-State // TODO: Re-add setting sum state
			edge.updateSumState(drainToSet(this.sumStateOk), Level.OK);
			edge.updateSumState(drainToSet(this.sumStateInfo), Level.INFO);
			edge.updateSumState(drainToSet(this.sumStateWarning), Level.WARNING);
			edge.updateSumState(drainToSet(this.sumStateFault), Level.FAULT);

		} catch (SQLException e) {
			this.log.error("Unable to execute WriteWorker task: " + e.getMessage());
		}
	};

	/**
	 * Called on {@link Edge.Events#ON_SET_LAST_MESSAGE_TIMESTAMP} event.
	 *
	 * @param edge the {@link MyEdge}.
	 */
	public void onLastMessage(MyEdge edge) {
		this.lastMessageEdgeIds.add(edge.getId());
	}

	/**
	 * Called on {@link Edge.Events#ON_SET_ONLINE} event.
	 *
	 * @param edge     the {@link MyEdge}.
	 * @param isOnline true if online, false if offline
	 */
	public void onSetOnline(MyEdge edge, boolean isOnline) {
		var id = edge.getId();
		if (isOnline) {
			this.isOnlineEdgeIds.add(id);
		} else {
			this.isOfflineEdgeIds.add(id);
		}
	}

	/**
	 * Called on {@link Edge.Events#ON_SET_SUM_STATE} event.
	 *
	 * @param edge     the {@link MyEdge}.
	 * @param sumState Sum-State {@link Level}
	 */
	public void onSetSumState(MyEdge edge, Level sumState) {
		var id = edge.getId();
		switch (sumState) {
		case OK:
			this.sumStateOk.add(id);
			break;
		case INFO:
			this.sumStateInfo.add(id);
			break;
		case WARNING:
			this.sumStateWarning.add(id);
			break;
		case FAULT:
			this.sumStateFault.add(id);
			break;
		}
	}

	/**
	 * Moves all entries of a {@link LinkedBlockingQueue} to a Set and clears the
	 * queue. This is thread-safe.
	 * 
	 * @param queue the {@link LinkedBlockingQueue}
	 * @return the {@link Set}
	 */
	protected static Set<String> drainToSet(LinkedBlockingQueue<String> queue) {
		Set<String> result = new HashSet<>(queue.size());
		queue.drainTo(result);
		return result;
	}

	/*
	 * From here required for DEBUG_MODE
	 */
	private LocalDateTime lastExecute = null;

	private synchronized void debugLog() {
		var now = LocalDateTime.now();
		if (this.lastExecute != null) {
			this.parent.logInfo(this.log, "PeriodicWriteWorker. " //
					+ "Time since last run: [" + ChronoUnit.SECONDS.between(this.lastExecute, now) + "s]" //
			);
		}
		this.lastExecute = now;
	}
}
