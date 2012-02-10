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

import java.util.concurrent.Callable;

/**
 * A status code.
 */
public enum StatusCode implements Status {

	OK(200, "OK"), ACCEPTED_FOR_PROCESSING(202, "Accepted for processing"), AUTHENTICATION_ACCEPTED(212, "Authentication accepted"), CHUNCKED_ITEM_ACCEPTED( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			213, "Chunked item accepted"), OPERATION_CANCELLED(214, "Operation Cancelled"), NOT_EXECUTED(215, "Not executed"), ATOMIC_ROLL_BACK_OK( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			216, "Atomic roll back OK"), NOT_MODIFIED(304, "Not modified"), UNAUTHORIZED(401, "Unauthorized"), FORBIDDEN(403, "Forbidden"), NOT_FOUND( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			404, "Not found"), COMMAND_NOT_ALLOWED(405, "Command not allowed"), OPTIONAL_FEATURE_NOT_SUPPORTED(406, "Optional Feature Not Supported"), AUTHENTICATION_REQUIRED( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			407, "Authentication required"), REQUEST_TIMEOUT(408, "Request timeout"), INCOMPLETE_COMMAND(412, "Incomplete command"), REQUEST_ENTITY_TOO_LARGE( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			413, "Request entity too large"), URI_TOO_LONG(414, "URI too long"), UNSUPPORTED_MEDIA_TYPE_OR_FORMAT(415, //$NON-NLS-1$ //$NON-NLS-2$
			"Unsupported media type or format"), REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested range not satisfiable"), ALREADY_EXISTS(418, //$NON-NLS-1$ //$NON-NLS-2$
			"Already exists"), DEVICE_FULL(420, "Device full"), SIZE_MISMATCH(424, "Size mismatch"), PERMISSION_DENIED(425, "Permission denied"), COMMAND_FAILED( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			500, "Command failed"), DATA_STORE_FAILURE(510, "Data store failure"), ATOMIC_ROLL_BACK_FAILED(516, "Atomic roll back failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * Status codes can be returned by an add command.
	 */
	public static final StatusCode[] ADD_CODES = { OK, NOT_EXECUTED, ATOMIC_ROLL_BACK_OK, UNAUTHORIZED, NOT_FOUND, COMMAND_NOT_ALLOWED,
			AUTHENTICATION_REQUIRED, REQUEST_ENTITY_TOO_LARGE, URI_TOO_LONG, UNSUPPORTED_MEDIA_TYPE_OR_FORMAT, ALREADY_EXISTS, DEVICE_FULL,
			SIZE_MISMATCH, PERMISSION_DENIED, COMMAND_FAILED, ATOMIC_ROLL_BACK_FAILED };

	/**
	 * Status codes can be returned by a copy command.
	 */
	public static final StatusCode[] COPY_CODES = { OK, NOT_EXECUTED, ATOMIC_ROLL_BACK_OK, UNAUTHORIZED, FORBIDDEN, COMMAND_NOT_ALLOWED,
			OPTIONAL_FEATURE_NOT_SUPPORTED, AUTHENTICATION_REQUIRED, URI_TOO_LONG, ALREADY_EXISTS, DEVICE_FULL, PERMISSION_DENIED, COMMAND_FAILED,
			DATA_STORE_FAILURE, ATOMIC_ROLL_BACK_FAILED };

	/**
	 * Status codes can be returned by a delete command.
	 */
	public static final StatusCode[] DELETE_CODES = { OK, NOT_EXECUTED, ATOMIC_ROLL_BACK_OK, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, COMMAND_NOT_ALLOWED,
			AUTHENTICATION_REQUIRED, URI_TOO_LONG, PERMISSION_DENIED, ATOMIC_ROLL_BACK_FAILED };

	/**
	 * Status codes can be returned by a get command.
	 */
	public static final StatusCode[] GET_CODES = { OK, NOT_EXECUTED, UNAUTHORIZED, NOT_FOUND, COMMAND_NOT_ALLOWED, OPTIONAL_FEATURE_NOT_SUPPORTED,
			AUTHENTICATION_REQUIRED, REQUEST_ENTITY_TOO_LARGE, URI_TOO_LONG, UNSUPPORTED_MEDIA_TYPE_OR_FORMAT, PERMISSION_DENIED, COMMAND_FAILED };

	/**
	 * Status codes can be returned by a replace command.
	 */
	public static final StatusCode[] REPLACE_CODES = { OK, NOT_EXECUTED, ATOMIC_ROLL_BACK_OK, UNAUTHORIZED, FORBIDDEN, NOT_FOUND,
			COMMAND_NOT_ALLOWED, AUTHENTICATION_REQUIRED, REQUEST_ENTITY_TOO_LARGE, URI_TOO_LONG, UNSUPPORTED_MEDIA_TYPE_OR_FORMAT, ALREADY_EXISTS,
			DEVICE_FULL, SIZE_MISMATCH, PERMISSION_DENIED, COMMAND_FAILED, ATOMIC_ROLL_BACK_FAILED };

	/**
	 * Status codes can be returned by a exec command.
	 */
	public static final StatusCode[] EXEC_CODES = { NOT_EXECUTED, UNAUTHORIZED, FORBIDDEN, COMMAND_NOT_ALLOWED, OPTIONAL_FEATURE_NOT_SUPPORTED,
			AUTHENTICATION_REQUIRED, URI_TOO_LONG, DEVICE_FULL, PERMISSION_DENIED, COMMAND_FAILED, DATA_STORE_FAILURE };

	private final int code;
	private final String description;

	private StatusCode(final int code, final String description) {
		this.code = code;
		this.description = description;
	}

	@Override
	public int getCode() {
		return this.code;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public DMNode getResult() {
		return null;
	}

	@Override
	public boolean haveResult() {
		return false;
	}

	@Override
	public Callable<DMGenericAlert> getDelayedProcessing() {
		return null;
	}

	@Override
	public boolean haveDelayedProcessing() {
		return false;
	}

	@Override
	public String toString() {
		return String.valueOf(this.code) + " " + this.description; //$NON-NLS-1$
	}

	/**
	 * Returns the corresponding status code from an integer.
	 * 
	 * If no status code is found, {@code null} is returned.
	 * 
	 * @param code
	 *            the code representing by an integer
	 * @return the corresponding status code from an integer
	 */
	public static StatusCode fromInt(final int code) {
		for (final StatusCode dmCode : StatusCode.values()) {
			if (dmCode.code == code) {
				return dmCode;
			}
		}
		return null;
	}

}