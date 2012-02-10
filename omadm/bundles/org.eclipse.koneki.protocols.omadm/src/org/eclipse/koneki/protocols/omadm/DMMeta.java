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

import java.util.HashMap;

public class DMMeta extends HashMap<Short, String> {

	public static final short UNKNOWN = 0;
	public static final short FORMAT = UNKNOWN + 1;
	public static final short TYPE = FORMAT + 1;
	public static final short MARK = TYPE + 1;
	public static final short CUSTOM = UNKNOWN + 1000;

	private static final long serialVersionUID = -5507971288868840747L;

	private static final String DEFAULT_FORMAT = "chr"; //$NON-NLS-1$
	private static final String DEFAULT_TYPE = "text/plain"; //$NON-NLS-1$
	private static final String DEFAULT_MARK = "informational"; //$NON-NLS-1$

	public DMMeta() {
		super();
	}

	public DMMeta(final DMMeta m) {
		super(m);
	}

	public final String getFormat() {
		return containsKey(FORMAT) ? get(FORMAT) : DEFAULT_FORMAT;
	}

	public final String getType() {
		return containsKey(TYPE) ? get(TYPE) : DEFAULT_TYPE;
	}

	public final String getMark() {
		return containsKey(MARK) ? get(MARK) : DEFAULT_MARK;
	}

	public static short getKey(final String tag) {
		if (tag.equals("Format")) { //$NON-NLS-1$
			return FORMAT;
		} else if (tag.equals("Type")) { //$NON-NLS-1$
			return TYPE;
		} else if (tag.equals("Mark")) { //$NON-NLS-1$
			return MARK;
		} else {
			return UNKNOWN;
		}
	}

}
