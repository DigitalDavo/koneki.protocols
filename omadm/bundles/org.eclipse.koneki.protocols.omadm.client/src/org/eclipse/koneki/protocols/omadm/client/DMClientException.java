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
package org.eclipse.koneki.protocols.omadm.client;

public class DMClientException extends Exception {

	private static final long serialVersionUID = -5407008910655727534L;

	public DMClientException() {
		super();
	}

	public DMClientException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public DMClientException(final String message) {
		super(message);
	}

	public DMClientException(final Throwable cause) {
		super(cause);
	}

}
