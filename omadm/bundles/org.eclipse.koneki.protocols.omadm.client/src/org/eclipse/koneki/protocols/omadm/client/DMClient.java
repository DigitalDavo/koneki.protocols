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
package org.eclipse.koneki.protocols.omadm.client;

import java.net.URI;

import org.eclipse.koneki.protocols.omadm.CommandHandler;
import org.eclipse.koneki.protocols.omadm.DMGenericAlert;
import org.eclipse.koneki.protocols.omadm.DMNode;
import org.eclipse.koneki.protocols.omadm.ProtocolListener;

/**
 * Interface of a DM client.
 */
public interface DMClient {

	/**
	 * Initiate a management session between the server and the client.
	 * 
	 * @param server
	 *            the DM server
	 * @param client
	 *            the DM client
	 * @param devInfoNodes
	 *            the DevInfo node must be send in the first DM message
	 * @param commandHandler
	 *            the command handler which process DM commands
	 * @throws NullPointerException
	 *             if {@code server}, {@code client}, {@code devInfoNodes} or {@code commandHandler} are {@code null}
	 */
	void initiateManagementSession(final URI server, final URI client, final DMNode[] devInfoNodes, final CommandHandler commandHandler);

	/**
	 * Initiate a management session between the server and the client.
	 * 
	 * @param server
	 *            the DM server
	 * @param client
	 *            the DM client
	 * @param devInfoNodes
	 *            the DevInfo node must be send in the first DM message
	 * @param commandHandler
	 *            the command handler which process DM commands
	 * @param protocolListeners
	 *            objects want listen protocol event
	 * @throws NullPointerException
	 *             if {@code server}, {@code client}, {@code devInfoNodes}, {@code commandHandler} or {@code protocolLinsteners} are {@code null}
	 */
	void initiateManagementSession(final URI server, final URI client, final DMNode[] devInfoNodes, final CommandHandler commandHandler,
			final ProtocolListener[] protocolLinsteners);

	/**
	 * Initiate a management session between the server and the client.
	 * 
	 * @param server
	 *            the DM server
	 * @param client
	 *            the DM client
	 * @param devInfoNodes
	 *            the DevInfo node must be send in the first DM message
	 * @param commandHandler
	 *            the command handler which process DM commands
	 * @param genericAlerts
	 *            generic alerts must be send to server at the beginning of the DM session
	 * @throws NullPointerException
	 *             if {@code server}, {@code client}, {@code devInfoNodes}, {@code commandHandler} or {@code genericAlerts} are {@code null}
	 */
	void initiateManagementSession(final URI server, final URI client, final DMNode[] devInfoNodes, final CommandHandler commandHandler,
			final DMGenericAlert[] genericAlerts);

	/**
	 * Initiate a management session between the server and the client.
	 * 
	 * @param server
	 *            the DM server
	 * @param userAuth
	 *            the user authentication
	 * @param client
	 *            the DM client
	 * @param devInfoNodes
	 *            the DevInfo node must be send in the first DM message
	 * @param commandHandler
	 *            the command handler which process DM commands
	 * @param protocolListeners
	 *            objects want listen protocol event
	 * @param genericAlerts
	 *            generic alerts must be send to server at the beginning of the DM session
	 * @throws NullPointerException
	 *             if {@code server}, {@code client}, {@code devInfoNodes}, {@code commandHandler}, {@code protocolLinsteners} or
	 *             {@code genericAlerts} are {@code null}
	 */
	void initiateManagementSession(final URI server, final String userAuth, final URI client, final DMNode[] devInfoNodes,
			final CommandHandler commandHandler, final ProtocolListener[] protocolLinsteners, final DMGenericAlert[] genericAlerts);

}
