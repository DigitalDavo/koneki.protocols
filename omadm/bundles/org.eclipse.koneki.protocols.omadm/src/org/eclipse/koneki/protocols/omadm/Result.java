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
 * A result.
 */
public final class Result implements Status {

	/**
	 * Constructs a result.
	 * 
	 * @param statusCode
	 *            the status code of the result
	 * @param result
	 *            the result
	 * @throws NullPointerException
	 *             if {@code statusCode} or {@code result} are {@code null}
	 */
	public Result(final StatusCode statusCode, final DMNode result) {
		if (statusCode != null && result != null) {
			this.statusCode = statusCode;
			this.result = result;
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
		return this.result;
	}

	@Override
	public boolean haveResult() {
		return true;
	}

	@Override
	public Callable<DMGenericAlert> getDelayedProcessing() {
		return null;
	}

	@Override
	public boolean haveDelayedProcessing() {
		return false;
	}

	public String toString() {
		return this.statusCode.toString();
	}

	private final StatusCode statusCode;
	private final DMNode result;

}