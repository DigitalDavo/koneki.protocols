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
package org.eclipse.koneki.protocols.omadm.client.basic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.Executors;

import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.koneki.protocols.omadm.CommandHandler;
import org.eclipse.koneki.protocols.omadm.DMAuthentication;
import org.eclipse.koneki.protocols.omadm.DMGenericAlert;
import org.eclipse.koneki.protocols.omadm.DMNode;
import org.eclipse.koneki.protocols.omadm.ProtocolListener;
import org.eclipse.koneki.protocols.omadm.client.DMClient;
import org.eclipse.koneki.protocols.omadm.client.DMClientException;

/**
 * A basic thread safe DM client over HTTP.
 */
public abstract class DMBasicClient implements DMClient {

	private final DMSessionExecutor sessionExecutor;
	private final XMLInputFactory xmlInputFactory;
	private final XMLOutputFactory xmlOutputFactory;
	private final DMSessionIDGenerator sessionIDGenerator;

	public DMBasicClient() {
		this.sessionExecutor = new DMSessionExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
		this.xmlInputFactory = XMLInputFactory.newInstance();
		this.xmlOutputFactory = XMLOutputFactory.newInstance();
		this.sessionIDGenerator = new DMSessionIDGenerator();
	}

	@Override
	public void initiateManagementSession(final URI server, final URI client, final DMNode[] devInfoNodes, final CommandHandler commandHandler) {
		initiateManagementSession(server, new DMAuthentication(), client, devInfoNodes, commandHandler, new ProtocolListener[] {},
				new DMGenericAlert[] {}); //$NON-NLS-1$
	}

	@Override
	public void initiateManagementSession(final URI server, final URI client, final DMNode[] devInfoNodes, final CommandHandler commandHandler,
			final ProtocolListener[] protocolLinsteners) {
		initiateManagementSession(server, new DMAuthentication(), client, devInfoNodes, commandHandler, protocolLinsteners, new DMGenericAlert[] {}); //$NON-NLS-1$
	}

	@Override
	public void initiateManagementSession(final URI server, final URI client, final DMNode[] devInfoNodes, final CommandHandler commandHandler,
			final DMGenericAlert[] genericAlerts) {
		initiateManagementSession(server, new DMAuthentication(), client, devInfoNodes, commandHandler, new ProtocolListener[] {}, genericAlerts); //$NON-NLS-1$
	}

	@Override
	public void initiateManagementSession(final URI server, final DMAuthentication userAuth, final URI client, final DMNode[] devInfoNodes,
			final CommandHandler commandHandler, final ProtocolListener[] protocolLinsteners, final DMGenericAlert[] genericAlerts) {
		if (server == null || client == null || devInfoNodes == null || commandHandler == null || protocolLinsteners == null || genericAlerts == null) {
			throw new NullPointerException();
		} else {
			final String sessionID = String.valueOf(this.sessionIDGenerator.nextSessionID(server, client));
			execute(client, new DMBasicSession(this, server, userAuth, client, sessionID, devInfoNodes, commandHandler, protocolLinsteners,
					genericAlerts));
		}
	}

	final void execute(final URI client, final Runnable command) {
		this.sessionExecutor.execute(client, command);
	}

	final XMLStreamReader createXMLStreamReader(final InputStream stream, final String encoding, final StreamFilter filter) throws XMLStreamException {
		return this.xmlInputFactory.createFilteredReader(this.xmlInputFactory.createXMLStreamReader(stream, encoding), filter);
	}

	final XMLStreamWriter createXMLStreamWriter(final OutputStream stream, final String encoding) throws XMLStreamException {
		return this.xmlOutputFactory.createXMLStreamWriter(stream, encoding);
	}

	protected static interface DMMessenger {

		void writeMessage(final OutputStream out) throws DMClientException;

		void readMessage(final InputStream in) throws DMClientException;

		String getAuthenticationValue(final ByteArrayOutputStream out) throws DMClientException;

	}

	protected abstract void sendAndReceiveMessage(final URI server, final String encoding, final DMMessenger messenger) throws IOException,
			DMClientException;

}
