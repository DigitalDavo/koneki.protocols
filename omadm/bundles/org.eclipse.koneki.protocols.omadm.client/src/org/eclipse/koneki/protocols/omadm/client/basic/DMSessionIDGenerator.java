/*******************************************************************************
 * Copyright (c) 2011 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.koneki.protocols.omadm.client.basic;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class DMSessionIDGenerator {

	public DMSessionIDGenerator() {
		this.sessionID = new ConcurrentHashMap<DMSessionIDGenerator.ServerClientKey, Integer>();
	}

	public int nextSessionID(final URI server, final URI client) {
		assert (server != null);
		assert (client != null);

		final ServerClientKey key = new ServerClientKey(server, client);

		this.sessionID.putIfAbsent(key, SESSION_ID_MIN);

		int nextSessionID;
		int furureSessionID;
		do {
			nextSessionID = this.sessionID.get(key);
			if (nextSessionID < SESSION_ID_MAX) {
				furureSessionID = nextSessionID + 1;
			} else {
				furureSessionID = SESSION_ID_MIN;
			}
		} while (!this.sessionID.replace(key, nextSessionID, furureSessionID));

		return nextSessionID;
	}

	private final ConcurrentMap<ServerClientKey, Integer> sessionID;

	private static final int SESSION_ID_MIN = 1;
	private static final int SESSION_ID_MAX = 9999;

	private static final class ServerClientKey {

		public ServerClientKey(final URI server, final URI client) {
			this.server = server;
			this.client = client;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.client.hashCode();
			result = prime * result + this.server.hashCode();
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj != null && obj instanceof ServerClientKey) {
				final ServerClientKey other = (ServerClientKey) obj;
				return this.server.equals(other.server) && this.client.equals(other.client);
			} else {
				return false;
			}
		}

		private final URI server;
		private final URI client;

	}

}
