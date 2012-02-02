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
package org.eclipse.koneki.protocols.omadm.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.koneki.protocols.omadm.DMNode;
import org.eclipse.koneki.protocols.omadm.Result;
import org.eclipse.koneki.protocols.omadm.StatusCode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ResultTest {
	private final StatusCode statusCode;
	private final DMNode node;
	private final Result result;

	public ResultTest(final StatusCode statusCode, final DMNode node, final Result result) {
		this.statusCode = statusCode;
		this.node = node;
		this.result = result;
	}

	@Test(expected = NullPointerException.class)
	public void testNullStatusCode() {
		new Result(null, this.node);
	}

	@Test(expected = NullPointerException.class)
	public void testNullNode() {
		new Result(this.statusCode, null);
	}

	@Test
	public void testGetCode() {
		assertEquals(this.statusCode.getCode(), this.result.getCode());
	}

	@Test
	public void testGetDescription() {
		assertEquals(this.statusCode.getDescription(), this.result.getDescription());
	}

	@Test
	public void testGetResult() {
		assertSame(this.node, this.result.getResult());
	}

	@Test
	public void testHaveResult() {
		assertTrue(this.result.haveResult());
	}

	@Test
	public void testGetDelayedProcessing() {
		assertNull(this.result.getDelayedProcessing());
	}

	@Test
	public void testHaveDelayedProcessing() {
		assertFalse(this.result.haveDelayedProcessing());
	}

	@Test
	public void testToString() {
		assertEquals(this.statusCode.toString(), this.result.toString());
	}

	@Parameters
	public static Collection<Object[]> parameters() {
		final Collection<Object[]> parameters = new ArrayList<Object[]>(StatusCode.values().length);
		for (final StatusCode statusCode : StatusCode.values()) {
			final DMNode node = new DMNode("", "", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			parameters.add(new Object[] { statusCode, node, new Result(statusCode, node) });
		}
		return parameters;
	}

}
