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
package org.eclipse.koneki.protocols.omadm.client.http.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;
import org.eclipse.koneki.protocols.omadm.client.DMClientException;
import org.eclipse.koneki.protocols.omadm.client.basic.DMBasicClient;

public class DMHttpClient extends DMBasicClient {

	private final HttpClient httpClient;

	public DMHttpClient() {
		final SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory())); //$NON-NLS-1$
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory())); //$NON-NLS-1$
		this.httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(schemeRegistry));
	}

	@Override
	protected void sendAndReceiveMessage(final URI server, final String encoding, final DMMessenger messenger) throws IOException, DMClientException {
		try {
			final HttpPost post = new HttpPost(server);

			final EntityTemplate entity = new EntityTemplate(new ContentProducer() {

				@Override
				public void writeTo(final OutputStream out) throws IOException {
					try {
						messenger.writeMessage(out);
					} catch (final DMClientException e) {
						throw new IOException(e);
					}
				}

			});
			entity.setChunked(false);
			entity.setContentEncoding(encoding);
			entity.setContentType("application/vnd.syncml.dm+xml"); //$NON-NLS-1$
			post.setEntity(entity);

			final HttpResponse response = this.httpClient.execute(post);

			if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299) {
				throw new DMClientException(response.getStatusLine().toString());
			}

			messenger.readMessage(response.getEntity().getContent());

			EntityUtils.consume(response.getEntity());
		} catch (final IOException e) {
			if (e.getCause() != null && e.getCause() instanceof DMClientException) {
				throw (DMClientException) e.getCause();
			} else {
				throw e;
			}
		}
	}

}