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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.eclipse.koneki.protocols.omadm.CommandHandler;
import org.eclipse.koneki.protocols.omadm.DMGenericAlert;
import org.eclipse.koneki.protocols.omadm.DMItem;
import org.eclipse.koneki.protocols.omadm.DMMeta;
import org.eclipse.koneki.protocols.omadm.DMNode;
import org.eclipse.koneki.protocols.omadm.ProtocolListener;
import org.eclipse.koneki.protocols.omadm.Status;
import org.eclipse.koneki.protocols.omadm.StatusCode;
import org.eclipse.koneki.protocols.omadm.client.DMClientException;
import org.eclipse.koneki.protocols.omadm.client.basic.DMBasicClient.DMMessenger;
import org.eclipse.koneki.protocols.omadm.client.basic.DMStatusManager.Results;
import org.eclipse.koneki.protocols.omadm.client.internal.Activator;

final class DMBasicSession implements Runnable {

	private static final String ENCODING = "UTF-8"; //$NON-NLS-1$

	private static final short UNKNOWN = 0;
	private static final short STATUS = UNKNOWN + 1;
	private static final short GET = STATUS + 1;
	private static final short FINAL = GET + 1;
	private static final short ADD = FINAL + 1;
	private static final short DELETE = ADD + 1;
	private static final short REPLACE = DELETE + 1;
	private static final short COPY = REPLACE + 1;
	private static final short SEQUENCE = COPY + 1;
	private static final short ATOMIC = SEQUENCE + 1;
	private static final short EXEC = ATOMIC + 1;
	private static final short ALERT = EXEC + 1;
	private final DMBasicClient dmClient;
	private URI server;
	private final URI client;
	private final String sessionId;
	private final DMNode[] devInfoNodes;
	private final CommandHandler commandHandler;
	private final ProtocolListener[] protocolLinsteners;
	private final DMGenericAlert[] genericAlerts;
	private final DMIDGenerator idGenerator;
	private final DMStatusManager statusManager;
	private final Map<String, Object[]> commandSends;
	private boolean isSessionContinue;
	private boolean isClientAuthenticated;
	private boolean isSetupPhaseFired;
	private boolean isManagementPhaseFired;
	private String currentServerMsgID;
	private final String userAuth;

	public DMBasicSession(final DMBasicClient dmClient, final URI server, final String userAuth, final URI client, final String sessionId,
			final DMNode[] devInfoNodes, final CommandHandler commandHandler, final ProtocolListener[] protocolLinsteners,
			final DMGenericAlert[] genericAlerts) {
		this.userAuth = userAuth;
		this.dmClient = dmClient;
		this.server = server;
		this.client = client;
		this.sessionId = sessionId;
		this.devInfoNodes = devInfoNodes;
		this.commandHandler = commandHandler;
		this.protocolLinsteners = protocolLinsteners;
		this.genericAlerts = genericAlerts;
		this.idGenerator = new DMIDGenerator();
		this.statusManager = new DMStatusManager();
		this.commandSends = new HashMap<String, Object[]>();
	}

	@Override
	public void run() {
		fireSessionBegin(this.sessionId);
		try {
			this.isSessionContinue = false;
			this.isClientAuthenticated = false;
			this.isSetupPhaseFired = false;
			this.isManagementPhaseFired = false;
			do {
				sendPackageAndReceivePackage();
			} while (this.isSessionContinue);
			fireSessionEnd();
		} catch (final IOException e) {
			fireSessionEnd(e);
		} catch (final DMClientException e) {
			fireSessionEnd(e);
		} catch (final RuntimeException e) {
			fireSessionEnd(e);
			throw e;
		}
	}

	void sendPackageAndReceivePackage() throws IOException, DMClientException {
		this.dmClient.sendAndReceiveMessage(this.server, ENCODING, new DMMessenger() {
			@Override
			public void writeMessage(final OutputStream out) throws DMClientException {
				try {
					if (!DMBasicSession.this.isSetupPhaseFired) {
						DMBasicSession.this.fireSetupPhaseBegin();
						DMBasicSession.this.isSetupPhaseFired = true;
					}
					if (!DMBasicSession.this.isManagementPhaseFired && DMBasicSession.this.isClientAuthenticated) {
						DMBasicSession.this.fireManagementPhaseBegin();
						DMBasicSession.this.isManagementPhaseFired = true;
					}
					if (DMBasicSession.this.protocolLinsteners.length != 0) {
						final ByteArrayOutputStream message = new ByteArrayOutputStream();
						DMBasicSession.this.writeMessage(new TeeOutputStream(out, message));
						DMBasicSession.this.fireNewClientPackage(message.toString(ENCODING));
					} else {
						DMBasicSession.this.writeMessage(out);
					}
				} catch (final XMLStreamException e) {
					throw new DMClientException(e);
				} catch (final UnsupportedEncodingException e) {
					throw new DMClientException(e);
				}
			}

			@Override
			public void readMessage(final InputStream in) throws DMClientException {
				try {
					if (DMBasicSession.this.protocolLinsteners.length != 0) {
						final ByteArrayOutputStream message = new ByteArrayOutputStream();
						DMBasicSession.this.readMessage(new TeeInputStream(in, message));
						DMBasicSession.this.fireNewServerPackage(message.toString(ENCODING));
					} else {
						DMBasicSession.this.readMessage(in);
					}
					if (!DMBasicSession.this.isManagementPhaseFired && DMBasicSession.this.isClientAuthenticated) {
						DMBasicSession.this.fireSetupPhaseEnd();
					}
					if (!DMBasicSession.this.isSessionContinue) {
						DMBasicSession.this.fireManagementPhaseEnd();
					}
				} catch (final XMLStreamException e) {
					throw new DMClientException(e);
				} catch (final UnsupportedEncodingException e) {
					throw new DMClientException(e);
				}
			}
		});
	}

	private void writeMessage(final OutputStream out) throws XMLStreamException {
		final XMLStreamWriter writer = this.dmClient.createXMLStreamWriter(out, ENCODING);
		writer.writeStartDocument(ENCODING, "1.0"); //$NON-NLS-1$
		// CHECKSTYLE:OFF (imbricated blocks)
		{
			writer.writeStartElement("SyncML"); //$NON-NLS-1$
			writer.writeAttribute("xmlns", "SYNCML:SYNCML1.2"); //$NON-NLS-1$//$NON-NLS-2$
			{
				writer.writeStartElement("SyncHdr"); //$NON-NLS-1$
				{
					writer.writeStartElement("VerDTD"); //$NON-NLS-1$
					writer.writeCharacters("1.2"); //$NON-NLS-1$
					writer.writeEndElement();
					writer.writeStartElement("VerProto"); //$NON-NLS-1$
					writer.writeCharacters("DM/1.2"); //$NON-NLS-1$
					writer.writeEndElement();
					writer.writeStartElement("SessionID"); //$NON-NLS-1$
					writer.writeCharacters(this.sessionId);
					writer.writeEndElement();
					writer.writeStartElement("MsgID"); //$NON-NLS-1$
					writer.writeCharacters(String.valueOf(this.idGenerator.nextMsgID()));
					writer.writeEndElement();
					writer.writeStartElement("Target"); //$NON-NLS-1$
					{
						writer.writeStartElement("LocURI"); //$NON-NLS-1$
						writer.writeCharacters(this.server.toString());
						writer.writeEndElement();
					}
					writer.writeEndElement();
					writer.writeStartElement("Source"); //$NON-NLS-1$
					{
						writer.writeStartElement("LocURI"); //$NON-NLS-1$
						writer.writeCharacters(this.client.toString());
						writer.writeEndElement();
					}
					writer.writeEndElement();

					/*
					 * Add basic authentication
					 */
					writer.writeStartElement("Cred"); //$NON-NLS-1$
					{
						writer.writeStartElement("Meta"); //$NON-NLS-1$
						{
							writer.writeStartElement("Format"); //$NON-NLS-1$
							writer.writeAttribute("xmlns", "syncml:metinf"); //$NON-NLS-1$ //$NON-NLS-2$
							writer.writeCharacters("b64"); //$NON-NLS-1$
							writer.writeEndElement();

							writer.writeStartElement("Type"); //$NON-NLS-1$
							writer.writeAttribute("xmlns", "syncml:metinf"); //$NON-NLS-1$ //$NON-NLS-2$
							writer.writeCharacters("syncml:auth-basic"); //$NON-NLS-1$
							writer.writeEndElement();
						}
						writer.writeEndElement();

						writer.writeStartElement("Data"); //$NON-NLS-1$
						writer.writeCharacters(userAuth);
						writer.writeEndElement();

					}
					writer.writeEndElement();
					/*
					 * End authentication
					 */
				}
				writer.writeEndElement();
				writer.writeStartElement("SyncBody"); //$NON-NLS-1$
				{
					writeStatus(writer);
					if (!this.isClientAuthenticated) {
						writeAlert(writer, "1201"); //$NON-NLS-1$
						writeGenericAlert(writer, this.genericAlerts);
						writeReplace(writer, this.devInfoNodes);
					}

					writer.writeStartElement("Final"); //$NON-NLS-1$
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		// CHECKSTYLE:ON
		writer.writeEndDocument();
		writer.flush();
		writer.close();
	}

	private void writeStatus(final XMLStreamWriter writer) throws XMLStreamException {
		while (this.statusManager.isValidStatus()) {
			// CHECKSTYLE:OFF (imbricated blocks)
			writer.writeStartElement("Status"); //$NON-NLS-1$
			{
				writer.writeStartElement("CmdID"); //$NON-NLS-1$
				writer.writeCharacters(String.valueOf(this.idGenerator.nextCmdID()));
				writer.writeEndElement();
				writer.writeStartElement("MsgRef"); //$NON-NLS-1$
				writer.writeCharacters(this.statusManager.getMsgRef());
				writer.writeEndElement();
				writer.writeStartElement("CmdRef"); //$NON-NLS-1$
				writer.writeCharacters(this.statusManager.getCmdRef());
				writer.writeEndElement();
				writer.writeStartElement("Cmd"); //$NON-NLS-1$
				writer.writeCharacters(this.statusManager.getCmd());
				writer.writeEndElement();
				for (final String targetRef : this.statusManager.getTargetRef()) {
					writer.writeStartElement("TargetRef"); //$NON-NLS-1$
					writer.writeCharacters(targetRef);
					writer.writeEndElement();
				}
				for (final String sourceRef : this.statusManager.getSourceRef()) {
					writer.writeStartElement("SourceRef"); //$NON-NLS-1$
					writer.writeCharacters(sourceRef);
					writer.writeEndElement();
				}
				writer.writeStartElement("Data"); //$NON-NLS-1$
				writer.writeCharacters(this.statusManager.getStatusCode());
				writer.writeEndElement();
			}
			writer.writeEndElement();
			if (!this.statusManager.getResults().isEmpty()) {
				writer.writeStartElement("Results"); //$NON-NLS-1$
				{
					writer.writeStartElement("CmdID"); //$NON-NLS-1$
					writer.writeCharacters(String.valueOf(this.idGenerator.nextCmdID()));
					writer.writeEndElement();
					writer.writeStartElement("MsgRef"); //$NON-NLS-1$
					writer.writeCharacters(this.statusManager.getMsgRef());
					writer.writeEndElement();
					writer.writeStartElement("CmdRef"); //$NON-NLS-1$
					writer.writeCharacters(this.statusManager.getCmdRef());
					writer.writeEndElement();
					// final String globalFormat = this.statusManager.getGlobalFormat();
					// final String globalType = this.statusManager.getGlobalType();
					// if (globalFormat != null || globalType != null) {
					// writer.writeStartElement("Meta");
					// {
					// if (globalFormat != null) {
					// writer.writeStartElement("Format");
					// writer.writeAttribute("xmlns", "syncml:metinf");
					// writer.writeCharacters(globalFormat);
					// writer.writeEndElement();
					// }
					// if (globalType != null) {
					// writer.writeStartElement("Type");
					// writer.writeAttribute("xmlns", "syncml:metinf");
					// writer.writeCharacters(globalType);
					// writer.writeEndElement();
					// }
					// }
					// writer.writeEndElement();
					// }
					for (final Results results : this.statusManager.getResults()) {
						writer.writeStartElement("Item"); //$NON-NLS-1$
						{
							writer.writeStartElement("Source"); //$NON-NLS-1$
							{
								writer.writeStartElement("LocURI"); //$NON-NLS-1$
								writer.writeCharacters(results.getSourceURI());
								writer.writeEndElement();
							}
							writer.writeEndElement();
							// if (globalFormat == null || globalType == null) {
							writer.writeStartElement("Meta"); //$NON-NLS-1$
							{
								// if (globalFormat == null) {
								writer.writeStartElement("Format"); //$NON-NLS-1$
								writer.writeAttribute("xmlns", "syncml:metinf"); //$NON-NLS-1$ //$NON-NLS-2$
								writer.writeCharacters(results.getFormat());
								writer.writeEndElement();
								// }
								// if (globalType == null) {
								writer.writeStartElement("Type"); //$NON-NLS-1$
								writer.writeAttribute("xmlns", "syncml:metinf"); //$NON-NLS-1$ //$NON-NLS-2$
								writer.writeCharacters(results.getType());
								writer.writeEndElement();
								// }
							}
							writer.writeEndElement();
							// }
							writer.writeStartElement("Data"); //$NON-NLS-1$
							writer.writeCharacters(results.getData());
							writer.writeEndElement();
						}
						writer.writeEndElement();
					}
				}
				writer.writeEndElement();
			}
			// CHECKSTYLE:ON
			this.statusManager.nextStatus();
		}
	}

	private void writeAlert(final XMLStreamWriter writer, final String statusCode) throws XMLStreamException {
		writer.writeStartElement("Alert"); //$NON-NLS-1$
		// CHECKSTYLE:OFF (imbricated blocks)
		{
			writer.writeStartElement("CmdID"); //$NON-NLS-1$
			writer.writeCharacters(String.valueOf(this.idGenerator.nextCmdID()));
			writer.writeEndElement();
			writer.writeStartElement("Data"); //$NON-NLS-1$
			writer.writeCharacters(statusCode);
			writer.writeEndElement();
		}
		// CHECKSTYLE:ON
		writer.writeEndElement();
	}

	private void writeGenericAlert(final XMLStreamWriter writer, final DMGenericAlert[] genAlerts) throws XMLStreamException {
		for (final DMGenericAlert genericAlert : genAlerts) {
			final String cmdID = String.valueOf(this.idGenerator.nextCmdID());
			final String alertCode = "1226"; //$NON-NLS-1$
			final DMMeta meta = new DMMeta();
			meta.put(DMMeta.TYPE, genericAlert.getType());
			meta.put(DMMeta.FORMAT, genericAlert.getFormat());
			meta.put(DMMeta.MARK, genericAlert.getMark());
			this.commandSends.put(cmdID,
					new Object[] { alertCode, genericAlert.getCorrelator(),
							new DMItem[] { new DMItem(null, genericAlert.getSource(), meta, genericAlert.getData()) } });
			writer.writeStartElement("Alert"); //$NON-NLS-1$
			// CHECKSTYLE:OFF (imbricated blocks)
			{
				writer.writeStartElement("CmdID"); //$NON-NLS-1$
				writer.writeCharacters(cmdID);
				writer.writeEndElement();
				writer.writeStartElement("Data"); //$NON-NLS-1$
				writer.writeCharacters(alertCode);
				writer.writeEndElement();
				if (genericAlert.getCorrelator() != null) {
					writer.writeStartElement("Correlator"); //$NON-NLS-1$
					writer.writeCharacters(genericAlert.getCorrelator());
					writer.writeEndElement();
				}
				writer.writeStartElement("Item"); //$NON-NLS-1$
				{
					if (genericAlert.getSource() != null) {
						writer.writeStartElement("Source"); //$NON-NLS-1$
						{
							writer.writeStartElement("LocURI"); //$NON-NLS-1$
							writer.writeCharacters(genericAlert.getSource());
							writer.writeEndElement();
						}
						writer.writeEndElement();
					}
					writer.writeStartElement("Meta"); //$NON-NLS-1$
					{
						writer.writeStartElement("Type"); //$NON-NLS-1$
						writer.writeAttribute("xmlns", "syncml:metinf"); //$NON-NLS-1$ //$NON-NLS-2$
						writer.writeCharacters(genericAlert.getType());
						writer.writeEndElement();
						writer.writeStartElement("Format"); //$NON-NLS-1$
						writer.writeAttribute("xmlns", "syncml:metinf"); //$NON-NLS-1$ //$NON-NLS-2$
						writer.writeCharacters(genericAlert.getFormat());
						writer.writeEndElement();
						writer.writeStartElement("Mark"); //$NON-NLS-1$
						writer.writeAttribute("xmlns", "syncml:metinf"); //$NON-NLS-1$ //$NON-NLS-2$
						writer.writeCharacters(genericAlert.getMark());
						writer.writeEndElement();
					}
					writer.writeEndElement();
					writer.writeStartElement("Data"); //$NON-NLS-1$
					writer.writeCharacters(genericAlert.getData());
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		// CHECKSTYLE:ON
	}

	private void writeReplace(final XMLStreamWriter writer, final DMNode[] nodes) throws XMLStreamException {
		if (this.devInfoNodes.length >= 1) {
			boolean sameFormat = true;
			boolean sameType = true;
			if (this.devInfoNodes.length >= 2) {
				for (int i = 1; i < this.devInfoNodes.length; ++i) {
					if (sameFormat && !this.devInfoNodes[i].getFormat().equals(this.devInfoNodes[0].getFormat())) {
						sameFormat = false;
					}
					if (sameType && !this.devInfoNodes[i].getType().equals(this.devInfoNodes[0].getType())) {
						sameType = false;
					}
				}
			}
			writer.writeStartElement("Replace"); //$NON-NLS-1$
			// CHECKSTYLE:OFF (imbricated blocks)
			{
				writer.writeStartElement("CmdID"); //$NON-NLS-1$
				writer.writeCharacters(String.valueOf(this.idGenerator.nextCmdID()));
				writer.writeEndElement();
				if (sameFormat || sameType) {
					writer.writeStartElement("Meta"); //$NON-NLS-1$
					{
						if (sameFormat) {
							writer.writeStartElement("Format"); //$NON-NLS-1$
							writer.writeAttribute("xmlns", "syncml:metinf"); //$NON-NLS-1$ //$NON-NLS-2$
							writer.writeCharacters(this.devInfoNodes[0].getFormat());
							writer.writeEndElement();
						}
						if (sameType) {
							writer.writeStartElement("Type"); //$NON-NLS-1$
							writer.writeAttribute("xmlns", "syncml:metinf"); //$NON-NLS-1$ //$NON-NLS-2$
							writer.writeCharacters(this.devInfoNodes[0].getType());
							writer.writeEndElement();
						}
					}
					writer.writeEndElement();
				}
				for (final DMNode devInfoNode : this.devInfoNodes) {
					writer.writeStartElement("Item"); //$NON-NLS-1$
					{
						writer.writeStartElement("Source"); //$NON-NLS-1$
						{
							writer.writeStartElement("LocURI"); //$NON-NLS-1$
							writer.writeCharacters(devInfoNode.getURI());
							writer.writeEndElement();
						}
						writer.writeEndElement();
						if (!sameFormat || !sameType) {
							writer.writeStartElement("Meta"); //$NON-NLS-1$
							{
								if (!sameFormat) {
									writer.writeStartElement("Format"); //$NON-NLS-1$
									writer.writeAttribute("xmlns", "syncml:metinf"); //$NON-NLS-1$ //$NON-NLS-2$
									writer.writeCharacters(devInfoNode.getFormat());
									writer.writeEndElement();
								}
								if (!sameType) {
									writer.writeStartElement("Type"); //$NON-NLS-1$
									writer.writeAttribute("xmlns", "syncml:metinf"); //$NON-NLS-1$ //$NON-NLS-2$
									writer.writeCharacters(devInfoNode.getType());
									writer.writeEndElement();
								}
							}
							writer.writeEndElement();
						}
						writer.writeStartElement("Data"); //$NON-NLS-1$
						writer.writeCharacters(devInfoNode.getData());
						writer.writeEndElement();
					}
					writer.writeEndElement();
				}
			}
			writer.writeEndElement();
			// CHECKSTYLE:ON
		}
	}

	private void readMessage(final InputStream in) throws XMLStreamException {
		final XMLStreamReader reader = this.dmClient.createXMLStreamReader(in, ENCODING, new StreamFilter() {

			@Override
			public boolean accept(final XMLStreamReader reader) {
				return !reader.isWhiteSpace() && !reader.isStandalone();
			}

		});

		jumpToStartTag(reader, "SyncHdr"); //$NON-NLS-1$

		readSyncHdr(reader);
		reader.nextTag();

		readSyncBody(reader);
		reader.nextTag();

		reader.close();
	}

	private void readSyncHdr(final XMLStreamReader reader) throws XMLStreamException {
		jumpToStartTag(reader, "MsgID"); //$NON-NLS-1$

		this.currentServerMsgID = reader.getElementText();

		jumpToStartTag(reader, "RespURI"); //$NON-NLS-1$
		try {
			String newServer = reader.getElementText();
			if (newServer != null) {
				server = new URI(newServer);
			}
		} catch (URISyntaxException e) {
			Activator.logError("Malformed RespURI in sync header", e); //$NON-NLS-1$
		}

		jumpToEndTag(reader, "SyncHdr"); //$NON-NLS-1$

		this.statusManager.putStatus(this.currentServerMsgID, "0", "SyncHdr", null, null, //$NON-NLS-1$ //$NON-NLS-2$
				String.valueOf(StatusCode.AUTHENTICATION_ACCEPTED.getCode()));
	}

	private void readSyncBody(final XMLStreamReader reader) throws XMLStreamException {
		boolean continueSyncBody = true;
		do {

			int next = reader.nextTag();
			String name = reader.getLocalName();

			switch (next) {
			case XMLEvent.START_ELEMENT:
				switch (getKey(name)) {
				case STATUS:
					readStatus(reader);
					break;
				case ADD:
					readAdd(reader, new DMMeta());
					break;
				case COPY:
					readCopy(reader);
					break;
				case DELETE:
					readDelete(reader);
					break;
				case GET:
					readGet(reader);
					break;
				case REPLACE:
					readReplace(reader, new DMMeta());
					break;
				case EXEC:
					readExec(reader, new DMMeta());
					break;
				case SEQUENCE:
					readSequence(reader);
					break;
				case ATOMIC:
					readAtomic(reader);
					break;
				case FINAL:
					readFinal(reader);
					break;
				case ALERT:
					readAlert(reader);
					break;
				default:
					break;
				}
				break;
			case XMLEvent.END_ELEMENT:
				continueSyncBody = false;
				break;
			default:
				break;
			}
		} while (continueSyncBody);
	}

	private void readAlert(final XMLStreamReader reader) throws XMLStreamException {
		/*
		 * TODO : Manage Alert commands
		 */
		reader.nextTag();
		// CmdID
		final String cmdID = reader.getElementText();
		reader.nextTag();

		this.statusManager.putStatus(this.currentServerMsgID, cmdID, "Alert", null, null, "406"); //$NON-NLS-1$ //$NON-NLS-2$

		jumpToEndTag(reader, "Alert"); //$NON-NLS-1$
	}

	private void readStatus(final XMLStreamReader reader) throws XMLStreamException {
		jumpToStartTag(reader, "CmdRef"); //$NON-NLS-1$

		// CmdRef
		final String cmdRef = reader.getElementText();
		reader.nextTag();

		// Cmd
		final String cmd = reader.getElementText();
		jumpToStartTag(reader, "Data"); //$NON-NLS-1$

		// Data
		final int data = Integer.parseInt(reader.getElementText());
		jumpToEndTag(reader, "Status"); //$NON-NLS-1$

		// Performs the status
		if (cmd.equals("SyncHdr")) { //$NON-NLS-1$
			switch (data) {
			case 212:
				this.isClientAuthenticated = true;
				break;
			case 407:
				this.isClientAuthenticated = false;
				break;
			default:
				break;
			}
		}
		if (this.commandSends.containsKey(cmdRef)) {
			final Object[] objects = this.commandSends.get(cmdRef);
			if (cmd.equals("Alert")) { //$NON-NLS-1$
				for (final ProtocolListener messageListener : this.protocolLinsteners) {
					messageListener.clientAlert((String) objects[0], (String) objects[1], (DMItem[]) objects[2], StatusCode.fromInt(data));
				}
			}
			this.commandSends.remove(cmdRef);
		}
	}

	private void readGet(final XMLStreamReader reader) throws XMLStreamException {
		reader.nextTag();

		// CmdID
		final String cmdID = reader.getElementText();
		reader.nextTag();

		// Meta?
		if (reader.getLocalName().equals("Meta")) { //$NON-NLS-1$
			jumpToEndTag(reader, "Meta"); //$NON-NLS-1$
			reader.nextTag();
		}

		// Item+
		boolean continueGet = true;
		do {
			switch (reader.getEventType()) {
			case XMLStreamReader.START_ELEMENT:
				// Performs the get command
				final DMItem item = readItem(reader, new DMMeta());
				reader.nextTag();
				final Status status = this.commandHandler.get(item.getTargetURI());
				final DMNode results = status.getResult();
				if (results != null) {
					this.statusManager.putStatus(this.currentServerMsgID, cmdID, "Get", item.getTargetURI(), null, String.valueOf(status.getCode()), //$NON-NLS-1$
							results.getFormat(), results.getType(), results.getData());
				} else {
					this.statusManager.putStatus(this.currentServerMsgID, cmdID, "Get", item.getTargetURI(), null, String.valueOf(status.getCode())); //$NON-NLS-1$
				}

				// Fire get event
				for (final ProtocolListener messageListener : this.protocolLinsteners) {
					messageListener.get(item.getTargetURI(), status);
				}
				break;
			case XMLStreamReader.END_ELEMENT:
				continueGet = false;
				break;
			default:
				break;
			}
		} while (continueGet);
	}

	private void readAdd(final XMLStreamReader reader, final DMMeta parentMeta) throws XMLStreamException {
		reader.nextTag();

		// CmdID
		final String cmdID = reader.getElementText();
		reader.nextTag();

		// Meta?
		final DMMeta globalMeta = new DMMeta(parentMeta);
		if (reader.getLocalName().equals("Meta")) { //$NON-NLS-1$
			globalMeta.putAll(readMeta(reader));
			reader.nextTag();
		}

		// Item+
		boolean continueAdd = true;
		do {
			switch (reader.getEventType()) {
			case XMLEvent.START_ELEMENT:
				// Performs the add command
				final DMItem item = readItem(reader, globalMeta);
				reader.nextTag();
				final Status status = this.commandHandler.add(item.getTargetURI(), item.getMeta().getFormat(), item.getMeta().getType(),
						item.getData());
				this.statusManager.putStatus(this.currentServerMsgID, cmdID, "Add", item.getTargetURI(), null, String.valueOf(status.getCode())); //$NON-NLS-1$

				// Fire add event
				for (final ProtocolListener messageListener : this.protocolLinsteners) {
					messageListener.add(item.getTargetURI(), item.getData(), status);
				}
				break;
			case XMLEvent.END_ELEMENT:
				continueAdd = false;
				break;
			default:
				break;
			}
		} while (continueAdd);
	}

	private void readDelete(final XMLStreamReader reader) throws XMLStreamException {
		reader.nextTag();

		// CmdID
		final String cmdID = reader.getElementText();
		reader.nextTag();

		// Meta?
		if (reader.getLocalName().equals("Meta")) { //$NON-NLS-1$
			jumpToEndTag(reader, "Meta"); //$NON-NLS-1$
			reader.nextTag();
		}

		// Item+
		boolean continueDelete = true;
		do {
			switch (reader.getEventType()) {
			case XMLEvent.START_ELEMENT:
				final DMItem item = readItem(reader, new DMMeta());
				reader.nextTag();
				final Status status = this.commandHandler.delete(item.getTargetURI());
				this.statusManager.putStatus(this.currentServerMsgID, cmdID, "Delete", item.getTargetURI(), null, String.valueOf(status.getCode())); //$NON-NLS-1$

				// Fire delete event
				for (final ProtocolListener messageListener : this.protocolLinsteners) {
					messageListener.delete(item.getTargetURI(), status);
				}
				break;
			case XMLEvent.END_ELEMENT:
				continueDelete = false;
				break;
			default:
				break;
			}
		} while (continueDelete);
	}

	private void readReplace(final XMLStreamReader reader, final DMMeta parentMeta) throws XMLStreamException {
		reader.nextTag();

		// CmdID
		final String cmdID = reader.getElementText();
		reader.nextTag();

		// Meta?
		final DMMeta globalMeta = new DMMeta(parentMeta);
		if (reader.getLocalName().equals("Meta")) { //$NON-NLS-1$
			globalMeta.putAll(readMeta(reader));
			reader.nextTag();
		}

		// Item+
		boolean continueReplace = true;
		do {
			switch (reader.getEventType()) {
			case XMLEvent.START_ELEMENT:
				final DMItem item = readItem(reader, globalMeta);
				reader.nextTag();
				final Status status = this.commandHandler.replace(item.getTargetURI(), item.getMeta().getFormat(), item.getMeta().getType(),
						item.getData());
				this.statusManager.putStatus(this.currentServerMsgID, cmdID, "Replace", item.getTargetURI(), null, String.valueOf(status.getCode())); //$NON-NLS-1$

				// Fire replace event
				for (final ProtocolListener messageListener : this.protocolLinsteners) {
					messageListener.replace(item.getTargetURI(), item.getData(), status);
				}
				break;
			case XMLEvent.END_ELEMENT:
				continueReplace = false;
				break;
			default:
				break;
			}
		} while (continueReplace);
	}

	private void readCopy(final XMLStreamReader reader) throws XMLStreamException {
		reader.nextTag();

		// CmdID
		final String cmdID = reader.getElementText();
		reader.nextTag();

		// Meta?
		if (reader.getLocalName().equals("Meta")) { //$NON-NLS-1$
			jumpToEndTag(reader, "Meta"); //$NON-NLS-1$
			reader.nextTag();
		}

		// Item+
		boolean continueCopy = true;
		do {
			switch (reader.getEventType()) {
			case XMLEvent.START_ELEMENT:
				final DMItem item = readItem(reader, null);
				reader.nextTag();
				final Status status = this.commandHandler.copy(item.getTargetURI(), item.getSourceURI());
				this.statusManager.putStatus(this.currentServerMsgID, cmdID, "Copy", item.getTargetURI(), item.getSourceURI(), //$NON-NLS-1$
						String.valueOf(status.getCode()));

				// Fire copy event
				for (final ProtocolListener messageListener : this.protocolLinsteners) {
					messageListener.copy(item.getTargetURI(), item.getSourceURI(), status);
				}
				break;
			case XMLEvent.END_ELEMENT:
				continueCopy = false;
				break;
			default:
				break;
			}
		} while (continueCopy);
	}

	private void readExec(final XMLStreamReader reader, final DMMeta parentMeta) throws XMLStreamException {
		reader.nextTag();

		// CmdID
		final String cmdID = reader.getElementText();
		reader.nextTag();

		// Meta?
		final DMMeta globalMeta = new DMMeta(parentMeta);
		if (reader.getLocalName().equals("Meta")) { //$NON-NLS-1$
			globalMeta.putAll(readMeta(reader));
			reader.nextTag();
		}

		// Correlator?
		final String correlator;
		if (reader.getLocalName().equals("Correlator")) { //$NON-NLS-1$
			correlator = reader.getElementText();
			reader.nextTag();
		} else {
			correlator = null;
		}

		// Item
		final DMItem item = readItem(reader, globalMeta);
		reader.nextTag();

		// Performs the exec command
		final Status status = this.commandHandler.exec(item.getTargetURI(), correlator, item.getMeta().getFormat(), item.getMeta().getType(),
				item.getData());
		this.statusManager.putStatus(this.currentServerMsgID, cmdID, "Exec", item.getTargetURI(), item.getSourceURI(), //$NON-NLS-1$
				String.valueOf(status.getCode()));
		if (status.getDelayedProcessing() != null) {
			this.dmClient.execute(this.client, new Runnable() {

				@Override
				public void run() {
					try {
						dmClient.initiateManagementSession(server, "", client, devInfoNodes, commandHandler, protocolLinsteners, //$NON-NLS-1$
								new DMGenericAlert[] { status.getDelayedProcessing().call() });
					} catch (final Exception e) {
						Activator.logError("Error while initializing management session", e); //$NON-NLS-1$
					}
				}

			});
		}

		// Fire exec event
		for (final ProtocolListener messageListener : this.protocolLinsteners) {
			messageListener.exec(item.getTargetURI(), correlator, item.getData(), status);
		}
	}

	private void readSequence(final XMLStreamReader reader) throws XMLStreamException {
		reader.nextTag();

		// CmdID
		final String cmdID = reader.getElementText();
		reader.nextTag();

		// Meta?
		final DMMeta globalMeta;
		if (reader.getLocalName().equals("Meta")) { //$NON-NLS-1$
			globalMeta = readMeta(reader);
			reader.nextTag();
		} else {
			globalMeta = new DMMeta();
		}

		// Procces the sequence element
		this.statusManager.putStatus(this.currentServerMsgID, cmdID, "Sequence", null, null, String.valueOf(StatusCode.OK.getCode())); //$NON-NLS-1$

		boolean continueSequence = true;
		do {
			switch (reader.getEventType()) {
			case XMLEvent.START_ELEMENT:
				switch (getKey(reader.getLocalName())) {
				case ADD:
					readAdd(reader, globalMeta);
					break;
				case COPY:
					readCopy(reader);
					break;
				case DELETE:
					readDelete(reader);
					break;
				case GET:
					readGet(reader);
					break;
				case REPLACE:
					readReplace(reader, globalMeta);
					break;
				case EXEC:
					readExec(reader, globalMeta);
					break;
				default:
					break;
				}
				reader.nextTag();
				break;
			case XMLEvent.END_ELEMENT:
				continueSequence = false;
				break;
			default:
				break;
			}
		} while (continueSequence);
	}

	private void readAtomic(final XMLStreamReader reader) throws XMLStreamException {
		reader.nextTag();

		// CmdID
		final String cmdID = reader.getElementText();
		jumpToEndTag(reader, "Atomic"); //$NON-NLS-1$

		// Performs atomic command
		this.statusManager.putStatus(this.currentServerMsgID, cmdID, "Atomic", null, null, //$NON-NLS-1$
				String.valueOf(StatusCode.OPTIONAL_FEATURE_NOT_SUPPORTED.getCode()));
	}

	private DMMeta readMeta(final XMLStreamReader reader) throws XMLStreamException {
		final DMMeta meta = new DMMeta();
		boolean continueMeta = true;
		do {
			switch (reader.next()) {
			case XMLEvent.START_ELEMENT:
				switch (DMMeta.getKey(reader.getLocalName())) {
				case DMMeta.FORMAT:
					meta.put(DMMeta.FORMAT, reader.getElementText());
					break;
				case DMMeta.TYPE:
					meta.put(DMMeta.TYPE, reader.getElementText());
					break;
				default:
					break;
				}
				break;
			case XMLEvent.END_ELEMENT:
				continueMeta = false;
				break;
			default:
				break;
			}
		} while (continueMeta);

		return meta;
	}

	private DMItem readItem(final XMLStreamReader reader, final DMMeta parentMeta) throws XMLStreamException {
		reader.nextTag();

		// Target?
		final String targetURI;
		if (reader.getLocalName().equals("Target")) { //$NON-NLS-1$
			reader.nextTag();
			// LocURI
			targetURI = reader.getElementText();
			reader.nextTag();
			// LocName?
			if (reader.getLocalName().equals("LocName")) { //$NON-NLS-1$
				jumpToEndTag(reader, "LocName"); //$NON-NLS-1$
				reader.nextTag();
			}
			reader.nextTag();
		} else {
			targetURI = null;
		}

		// Source?
		final String sourceURI;
		if (reader.getLocalName().equals("Source")) { //$NON-NLS-1$
			reader.nextTag();
			// LocURI
			sourceURI = reader.getElementText();
			reader.nextTag();
			// LocName?
			if (reader.getLocalName().equals("LocName")) { //$NON-NLS-1$
				jumpToEndTag(reader, "LocName"); //$NON-NLS-1$
				reader.nextTag();
			}
			reader.nextTag();
		} else {
			sourceURI = null;
		}

		// Meta?
		final DMMeta meta = new DMMeta(parentMeta);
		if (reader.getLocalName().equals("Meta")) { //$NON-NLS-1$
			meta.putAll(readMeta(reader));
			reader.nextTag();
		}

		// Data?
		final String data;
		if (reader.getLocalName().equals("Data")) { //$NON-NLS-1$
			data = reader.getElementText();
			reader.nextTag();
		} else {
			data = null;
		}

		return new DMItem(targetURI, sourceURI, meta, data);
	}

	private void readFinal(final XMLStreamReader reader) throws XMLStreamException {
		reader.nextTag();

		this.isSessionContinue = !this.statusManager.onlySyncHdrStatus();
	}

	private static short getKey(final String tag) {
		if (tag.equals("Status")) { //$NON-NLS-1$
			return STATUS;
		} else if (tag.equals("Get")) { //$NON-NLS-1$
			return GET;
		} else if (tag.equals("Final")) { //$NON-NLS-1$
			return FINAL;
		} else if (tag.equals("Add")) { //$NON-NLS-1$
			return ADD;
		} else if (tag.equals("Delete")) { //$NON-NLS-1$
			return DELETE;
		} else if (tag.equals("Replace")) { //$NON-NLS-1$
			return REPLACE;
		} else if (tag.equals("Copy")) { //$NON-NLS-1$
			return COPY;
		} else if (tag.equals("Sequence")) { //$NON-NLS-1$
			return SEQUENCE;
		} else if (tag.equals("Atomic")) { //$NON-NLS-1$
			return ATOMIC;
		} else if (tag.equals("Exec")) { //$NON-NLS-1$
			return EXEC;
		} else if (tag.equals("Alert")) { //$NON-NLS-1$
			return ALERT;
		} else {
			return UNKNOWN;
		}
	}

	private static void jumpToStartTag(final XMLStreamReader reader, final String tag) throws XMLStreamException {
		while (reader.next() != XMLStreamReader.START_ELEMENT || !reader.getLocalName().equals(tag)) {
			continue;
		}
	}

	private static void jumpToEndTag(final XMLStreamReader reader, final String tag) throws XMLStreamException {
		while (reader.next() != XMLStreamReader.END_ELEMENT || !reader.getLocalName().equals(tag)) {
			continue;
		}
	}

	private void fireSessionBegin(final String sessionID) {
		for (final ProtocolListener messageListener : this.protocolLinsteners) {
			messageListener.sessionBegin(sessionID);
		}
	}

	private void fireSessionEnd() {
		for (final ProtocolListener messageListener : this.protocolLinsteners) {
			messageListener.sessionEnd();
		}
	}

	private void fireSessionEnd(final Throwable t) {
		for (final ProtocolListener messageListener : this.protocolLinsteners) {
			messageListener.sessionEnd(t);
		}
	}

	private void fireSetupPhaseBegin() {
		for (final ProtocolListener messageListener : this.protocolLinsteners) {
			messageListener.setupPhaseBegin();
		}
	}

	private void fireSetupPhaseEnd() {
		for (final ProtocolListener messageListener : this.protocolLinsteners) {
			messageListener.setupPhaseEnd();
		}
	}

	private void fireManagementPhaseBegin() {
		for (final ProtocolListener messageListener : this.protocolLinsteners) {
			messageListener.managementPhaseBegin();
		}
	}

	private void fireManagementPhaseEnd() {
		for (final ProtocolListener messageListener : this.protocolLinsteners) {
			messageListener.managementPhaseEnd();
		}
	}

	private void fireNewClientPackage(final String message) {
		for (final ProtocolListener messageListener : this.protocolLinsteners) {
			messageListener.newClientPackage(message);
		}
	}

	private void fireNewServerPackage(final String message) {
		for (final ProtocolListener messageListener : this.protocolLinsteners) {
			messageListener.newServerPackage(message);
		}
	}

}
