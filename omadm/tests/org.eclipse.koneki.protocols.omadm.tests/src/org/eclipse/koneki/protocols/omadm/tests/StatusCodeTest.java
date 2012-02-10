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
package org.eclipse.koneki.protocols.omadm.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.koneki.protocols.omadm.StatusCode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class StatusCodeTest {
	private final StatusCode statusCode;

	public StatusCodeTest(final StatusCode statusCode) {
		this.statusCode = statusCode;
	}

	@Test
	public void testGetResult() {
		assertNull(this.statusCode.getResult());
	}

	@Test
	public void testHaveResult() {
		assertFalse(this.statusCode.haveResult());
	}

	@Test
	public void testGetDelayedProcessing() {
		assertNull(this.statusCode.getDelayedProcessing());
	}

	@Test
	public void testHaveDelayedProcessing() {
		assertFalse(this.statusCode.haveDelayedProcessing());
	}

	@Test
	public void testToString() {
		assertEquals(this.statusCode.getCode() + " " + this.statusCode.getDescription(), this.statusCode.toString()); //$NON-NLS-1$
	}

	@Test
	public void testFromInt() {
		assertSame(this.statusCode, StatusCode.fromInt(this.statusCode.getCode()));
	}

	@Parameters
	public static Collection<Object[]> parameters() {
		final Collection<Object[]> parameters = new ArrayList<Object[]>(StatusCode.values().length);
		for (final StatusCode statusCode : StatusCode.values()) {
			parameters.add(new Object[] { statusCode });
		}
		return parameters;
	}

}
