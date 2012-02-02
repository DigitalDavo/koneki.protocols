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
 * A delayed processing.
 */
public final class DelayedProcessing implements Status {

	/**
	 * Constructs a delayed processing.
	 * 
	 * @param statusCode
	 *            the status code of the delayed processing
	 * @param delayedProcessing
	 *            the delayed processing
	 * @throws NullPointerException
	 *             if {@code statusCode} or {@code delayedProcessing} are {@code null}
	 */
	public DelayedProcessing(final StatusCode statusCode, final Callable<DMGenericAlert> delayedProcessing) {
		if (statusCode != null && delayedProcessing != null) {
			this.statusCode = statusCode;
			this.delayedProcessing = delayedProcessing;
		} else {
			throw new NullPointerException();
		}
	}

	@Override
	public int getCode() {
		return this.statusCode.getCode();
	}

	@Override
	public String getDescription() {
		return this.statusCode.getDescription();
	}

	@Override
	public DMNode getResult() {
		return null;
	}

	@Override
	public boolean haveResult() {
		return false;
	}

	@Override
	public Callable<DMGenericAlert> getDelayedProcessing() {
		return this.delayedProcessing;
	}

	@Override
	public boolean haveDelayedProcessing() {
		return true;
	}

	@Override
	public String toString() {
		return this.statusCode.toString();
	}

	private final StatusCode statusCode;
	private final Callable<DMGenericAlert> delayedProcessing;

}