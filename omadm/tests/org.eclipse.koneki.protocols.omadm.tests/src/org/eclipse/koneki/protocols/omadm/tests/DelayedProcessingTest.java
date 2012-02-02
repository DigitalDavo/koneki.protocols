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
import java.util.concurrent.Callable;

import org.eclipse.koneki.protocols.omadm.DMGenericAlert;
import org.eclipse.koneki.protocols.omadm.DelayedProcessing;
import org.eclipse.koneki.protocols.omadm.Result;
import org.eclipse.koneki.protocols.omadm.StatusCode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DelayedProcessingTest {
	private final StatusCode statusCode;
	private final Callable<DMGenericAlert> callable;
	private final DelayedProcessing delayedProcessing;

	public DelayedProcessingTest(final StatusCode statusCode, Callable<DMGenericAlert> callable, final DelayedProcessing delayedProcessing) {
		this.statusCode = statusCode;
		this.callable = callable;
		this.delayedProcessing = delayedProcessing;
	}

	@Test(expected = NullPointerException.class)
	public void testNullStatusCode() {
		new DelayedProcessing(null, this.callable);
	}

	@Test(expected = NullPointerException.class)
	public void testNullCallable() {
		new Result(this.statusCode, null);
	}

	@Test
	public void testGetCode() {
		assertEquals(this.statusCode.getCode(), this.delayedProcessing.getCode());
	}

	@Test
	public void testGetDescription() {
		assertEquals(this.statusCode.getDescription(), this.delayedProcessing.getDescription());
	}

	@Test
	public void testGetResult() {
		assertNull(this.delayedProcessing.getResult());
	}

	@Test
	public void testHaveResult() {
		assertFalse(this.delayedProcessing.haveResult());
	}

	@Test
	public void testGetDelayedProcessing() {
		assertSame(this.callable, this.delayedProcessing.getDelayedProcessing());
	}

	@Test
	public void testHaveDelayedProcessing() {
		assertTrue(this.delayedProcessing.haveDelayedProcessing());
	}

	@Test
	public void testToString() {
		assertEquals(this.statusCode.toString(), this.delayedProcessing.toString());
	}

	@Parameters
	public static Collection<Object[]> parameters() {
		final Collection<Object[]> parameters = new ArrayList<Object[]>(StatusCode.values().length);
		for (final StatusCode statusCode : StatusCode.values()) {
			final Callable<DMGenericAlert> callable = new Callable<DMGenericAlert>() {

				@Override
				public DMGenericAlert call() throws Exception {
					return null;
				}

			};
			parameters.add(new Object[] { statusCode, callable, new DelayedProcessing(statusCode, callable) });
		}
		return parameters;
	}
}
