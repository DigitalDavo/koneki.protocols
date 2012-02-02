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
package org.eclipse.koneki.protocols.omadm.client.basic;

final class DMIDGenerator {

	public DMIDGenerator() {
		this.msgID = 0;
		this.cmdID = 0;
	}

	public int nextMsgID() {
		if (this.msgID < Integer.MAX_VALUE) {
			++this.msgID;
		} else {
			this.msgID = 1;
		}
		this.cmdID = 0;
		return this.msgID;
	}

	public int nextCmdID() {
		if (this.cmdID < Integer.MAX_VALUE) {
			++this.cmdID;
		} else {
			this.cmdID = 1;
		}
		return this.cmdID;
	}

	private int msgID;
	private int cmdID;

}
