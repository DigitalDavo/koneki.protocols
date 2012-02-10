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
package org.eclipse.koneki.protocols.omadm;

/**
 * Interface of a DM command handler.
 */
public interface CommandHandler {

	/**
	 * Handles an add command.
	 * 
	 * @param target
	 *            the target of the new node
	 * @param format
	 *            the format of the data
	 * @param type
	 *            the type of the data
	 * @param data
	 *            the data of the node to add
	 * @return the status of the command
	 * @throws NullPointerException
	 *             if {@code target}, {@code format}, {@code type} or {@code data} are {@code null}
	 */
	Status add(final String target, final String format, final String type, final String data);

	/**
	 * Handles a copy command.
	 * 
	 * @param target
	 *            the target of the new node
	 * @param source
	 *            the source node to copy data
	 * @return the status of the command
	 * @throws NullPointerException
	 *             if {@code target} or {@code source} are {@code null}
	 */
	Status copy(final String target, final String source);

	/**
	 * Handles a delete command.
	 * 
	 * @param target
	 *            the target of the node to delete
	 * @return the status of the command
	 * @throws NullPointerException
	 *             if {@code target} is {@code null}
	 */
	Status delete(final String target);

	/**
	 * Handles an exec command.
	 * 
	 * @param target
	 *            the target of the node to exec
	 * @param correlator
	 *            the optional correlator of the exec
	 * @param format
	 *            the format of the data
	 * @param type
	 *            the type of the data
	 * @param data
	 *            the optional data of the exec command
	 * @return the status of the command
	 * @throws NullPointerException
	 *             if {@code target} is {@code null}
	 * @throws NullPointerException
	 *             if {@code format} or {@code type} are {@code null} when {@code data} is not {@code null}
	 */
	Status exec(final String target, final String correlator, final String format, final String type, final String data);

	/**
	 * Handles a get command.
	 * 
	 * @param target
	 *            the target of the node to get
	 * @return the status of the command
	 * @throws NullPointerException
	 *             if {@code target} is {@code null}
	 */
	Status get(final String target);

	/**
	 * Handles a replace command.
	 * 
	 * @param target
	 *            the target of the node to replace
	 * @param format
	 *            the format of the data
	 * @param type
	 *            the type of the data
	 * @param data
	 *            the new data of the node
	 * @return the status of the command
	 * @throws NullPointerException
	 *             if {@code target}, {@code format}, {@code type} or {@code data} are {@code null}
	 */
	Status replace(final String target, final String format, final String type, final String data);

}
