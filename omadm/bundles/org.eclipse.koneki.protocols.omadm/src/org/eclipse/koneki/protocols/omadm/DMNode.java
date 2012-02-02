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

public final class DMNode {

	public DMNode(final String uri, final String format, final String type, final String data) {
		if (uri == null || format == null || type == null || data == null) {
			throw new NullPointerException();
		} else {
			this.uri = uri;
			this.format = format;
			this.type = type;
			this.data = data;
		}
	}

	public String getURI() {
		return this.uri;
	}

	public String getFormat() {
		return this.format;
	}

	public String getType() {
		return this.type;
	}

	public String getData() {
		return this.data;
	}

	private final String uri;
	private final String format;
	private final String type;
	private final String data;

}
