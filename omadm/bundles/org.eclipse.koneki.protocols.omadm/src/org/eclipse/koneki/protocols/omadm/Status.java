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

import java.util.concurrent.Callable;

/**
 * Interface of a status returned by dm commands.
 */
public interface Status {

	/**
	 * Returns the code of the status.
	 * 
	 * @return the code of the status
	 */
	int getCode();

	/**
	 * Returns the description of the status.
	 * 
	 * @return the description of the status
	 */
	String getDescription();

	/**
	 * <p>
	 * Returns the result of a status.
	 * </p>
	 * 
	 * <p>
	 * If a status have no result, {@code null} is returned.
	 * </p>
	 * 
	 * @return the result of a status, can be {@code null}
	 */
	DMNode getResult();

	/**
	 * Returns {@code true} if the status have a result, otherwise {@code false}.
	 * 
	 * @return {@code true} if the status have a result, otherwise {@code false}
	 */
	boolean haveResult();

	/**
	 * <p>
	 * Returns the delayed processing of a status.
	 * </p>
	 * 
	 * <p>
	 * If a status have no delayed processing, {@code null} is returned.
	 * </p>
	 * 
	 * @return the delayed processing of a status, can be {@code null}
	 */
	Callable<DMGenericAlert> getDelayedProcessing();

	/**
	 * Returns {@code true} if the status have a delayed processing, otherwise {@code false}.
	 * 
	 * @return {@code true} if the status have a delayed processing, otherwise {@code false}
	 */
	boolean haveDelayedProcessing();

}
