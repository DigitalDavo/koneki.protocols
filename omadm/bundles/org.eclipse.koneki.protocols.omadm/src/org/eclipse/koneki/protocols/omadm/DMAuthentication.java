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

	private AuthenticationType authenticationType;

	private String userName;
	private String password;

	public DMAuthentication() {
		this.authenticationType = AuthenticationType.NONE;
		this.userName = "";
		this.password = "";
	}

	public DMAuthentication(final AuthenticationType authenticationType, final String userName, final String password) {
		this.authenticationType = authenticationType;
		this.userName = userName;
		this.password = password;
	}

	/**
	 * @return the authenticationType
	 */
	public AuthenticationType getAuthenticationType() {
		return authenticationType;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
}
