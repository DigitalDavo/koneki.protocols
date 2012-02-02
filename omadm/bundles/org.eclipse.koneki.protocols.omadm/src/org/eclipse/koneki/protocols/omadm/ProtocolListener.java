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
package org.eclipse.koneki.protocols.omadm;

/**
 * Interface of a DM protocol listener.
 */
public interface ProtocolListener {

	/**
	 * The session begin.
	 * 
	 * @param sessionID
	 *            the sessionID of the session
	 * @throws NullPointerException
	 *             if {@code sessionID} is {@code null}
	 */
	void sessionBegin(final String sessionID);

	/**
	 * The session end.
	 */
	void sessionEnd();

	/**
	 * The session end with an exception.
	 * 
	 * @param t
	 *            the exception
	 * @throws NullPointerException
	 *             if {@code t} is {@code null}
	 */
	void sessionEnd(final Throwable t);

	/**
	 * The setup phase begin.
	 */
	void setupPhaseBegin();

	/**
	 * The setup phase end.
	 */
	void setupPhaseEnd();

	/**
	 * A management phase begin.
	 */
	void managementPhaseBegin();

	/**
	 * A management phase end.
	 */
	void managementPhaseEnd();

	/**
	 * A new client package is send.
	 * 
	 * @param message
	 *            the message of the package
	 * @throws NullPointerException
	 *             if {@code message} is {@code null}
	 */
	void newClientPackage(final String message);

	/**
	 * A new server package is receive.
	 * 
	 * @param message
	 *            the message of the package
	 * @throws NullPointerException
	 *             if {@code message} is {@code null}
	 */
	void newServerPackage(final String message);

	/**
	 * A client alert command is send.
	 * 
	 * @param alertCode
	 *            the alert code of the alert
	 * @param correlator
	 *            the optional correlator of the alert
	 * @param items
	 *            items of the alert
	 */
	void clientAlert(final String alertCode, final String correlator, final DMItem[] items, final Status status);

	/**
	 * An add command is performed.
	 * 
	 * @param target
	 *            the target of the new node
	 * @param data
	 *            the data of the node to add
	 * @param status
	 *            the status of the command
	 * @throws NullPointerException
	 *             if {@code target}, {@code data} or {@code status} are {@code null}
	 */
	void add(final String target, final String data, final Status status);

	/**
	 * A copy command is performed.
	 * 
	 * @param target
	 *            the target of the new node
	 * @param source
	 *            the source node to copy data
	 * @param status
	 *            the status of the command
	 * @throws NullPointerException
	 *             if {@code target}, {@code source} or {@code status} are {@code null}
	 */
	void copy(final String target, final String source, final Status status);

	/**
	 * A delete command is performed.
	 * 
	 * @param target
	 *            the target of the node to delete
	 * @param status
	 *            the status of the command
	 * @throws NullPointerException
	 *             if {@code target} or {@code status} are {@code null}
	 */
	void delete(final String target, final Status status);

	/**
	 * Handles an exec command.
	 * 
	 * @param target
	 *            the target of the node to exec
	 * @param correlator
	 *            the optional correlator of the exec
	 * @param data
	 *            the optional data of the exec command
	 * @param status
	 *            the status of the command
	 * @throws NullPointerException
	 *             if {@code target} or {@code status} are {@code null}
	 */
	void exec(final String target, final String correlator, final String data, final Status status);

	/**
	 * A get command is performed.
	 * 
	 * @param target
	 *            the target of the node to get
	 * @param status
	 *            the status of the command
	 * @throws NullPointerException
	 *             if {@code target} or {@code status} are {@code null}
	 */
	void get(final String target, final Status status);

	/**
	 * A replace command is performed.
	 * 
	 * @param target
	 *            the target of the node to replace
	 * @param data
	 *            the new data of the node
	 * @param status
	 *            the status of the command
	 * @throws NullPointerException
	 *             if {@code target}, {@code data} or {@code status} are {@code null}
	 */
	void replace(final String target, final String data, final Status status);

}
