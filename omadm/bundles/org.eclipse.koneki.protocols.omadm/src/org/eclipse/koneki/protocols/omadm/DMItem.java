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

public final class DMItem {

	private final String targetURI;
	private final String sourceURI;
	private final DMMeta meta;
	private final String data;

	public DMItem(final String targetURI, final String sourceURI, final DMMeta meta, final String data) {
		this.targetURI = targetURI;
		this.sourceURI = sourceURI;
		this.meta = meta;
		this.data = data;
	}

	public String getTargetURI() {
		return this.targetURI;
	}

	public String getSourceURI() {
		return this.sourceURI;
	}

	public DMMeta getMeta() {
		return this.meta;
	}

	public String getData() {
		return this.data;
	}

}