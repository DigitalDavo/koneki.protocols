/*******************************************************************************
 * Copyright (c) 2012 Sierra Wireless and others.
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
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

final class DMSessionExecutor {

	private final ExecutorService executorService;
	private final ConcurrentHashMap<URI, Queue<Runnable>> commands;

	public DMSessionExecutor(final ExecutorService executorService) {
		this.executorService = executorService;
		this.commands = new ConcurrentHashMap<URI, Queue<Runnable>>();
	}

	public void execute(final URI client, final Runnable command) {
		final Queue<Runnable> clientCommands = getClientCommands(client);
		synchronized (clientCommands) {
			clientCommands.offer(command);
			if (clientCommands.size() == 1) {
				this.executorService.execute(new DMSessionRunnableProxy(client));
			}
		}
	}

	private Queue<Runnable> getClientCommands(final URI client) {
		final Queue<Runnable> newClientCommands = new LinkedList<Runnable>();
		final Queue<Runnable> clientCommands = this.commands.putIfAbsent(client, newClientCommands);
		return clientCommands != null ? clientCommands : newClientCommands;
	}

	private final class DMSessionRunnableProxy implements Runnable {

		private final URI client;

		public DMSessionRunnableProxy(final URI client) {
			this.client = client;
		}

		@Override
		public void run() {
			final Queue<Runnable> clientCommands = getClientCommands(this.client);
			final Runnable command;
			synchronized (clientCommands) {
				command = clientCommands.element();
			}
			command.run();
			synchronized (clientCommands) {
				clientCommands.remove();
				if (clientCommands.size() >= 1) {
					DMSessionExecutor.this.executorService.execute(this);
				}
			}
		}

	}

}
