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

public final class DMGenericAlert {

	private final String type;
	private final String format;
	private final String mark;
	private final String data;
	private final String correlator;
	private final String source;

	public DMGenericAlert(final String type, final String format, final String mark, final String data, final String correlator, final String source) {
		if (type != null && format != null && mark != null && data != null) {
			this.type = type;
			this.format = format;
			this.mark = mark;
			this.data = data;
			this.correlator = correlator;
			this.source = source;
		} else {
			throw new NullPointerException();
		}
	}

	public String getType() {
		return this.type;
	}

	public String getFormat() {
		return this.format;
	}

	public String getMark() {
		return this.mark;
	}

	public String getData() {
		return this.data;
	}

	public String getCorrelator() {
		return this.correlator;
	}

	public String getSource() {
		return this.source;
	}

}
