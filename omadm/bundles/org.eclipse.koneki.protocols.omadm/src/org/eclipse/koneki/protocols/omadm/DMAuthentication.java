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
 * TODO Comment this class
 */
public class DMAuthentication {

	private byte[] basicAuthentication;

	public DMAuthentication() {
		basicAuthentication = null;
	}

	public DMAuthentication(final byte[] basicAuthentication) {
		this.basicAuthentication = basicAuthentication;
	}

	/**
	 * @return the basicAuthentication
	 */
	public byte[] getAuthentication() {
		return basicAuthentication;
	}
}
