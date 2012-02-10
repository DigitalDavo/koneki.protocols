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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

final class DMStatusManager {

	private final Map<CommandNode, Map<String, ItemsNode>> itemsNodes;
	private final Queue<CommandNode> commandNodes;
	private final Map<CommandNode, Queue<String>> statusCodeNodes;
	private final Map<CommandNode, Boolean> isUnique;

	public DMStatusManager() {
		this.itemsNodes = new HashMap<DMStatusManager.CommandNode, Map<String, ItemsNode>>();
		this.commandNodes = new LinkedList<DMStatusManager.CommandNode>();
		this.statusCodeNodes = new HashMap<DMStatusManager.CommandNode, Queue<String>>();
		this.isUnique = new HashMap<DMStatusManager.CommandNode, Boolean>();
	}

	public void putStatus(final String msgID, final String cmdID, final String cmd, final String targetURI, final String sourceURI,
			final String statusCode) {
		putStatus(msgID, cmdID, cmd, targetURI, sourceURI, statusCode, null, null, null);
	}

	public void putStatus(final String msgID, final String cmdID, final String cmd, final String targetURI, final String sourceURI,
			final String statusCode, final String format, final String type, final String data) {
		final CommandNode commandNode = new CommandNode(msgID, cmdID, cmd);

		if (!this.itemsNodes.containsKey(commandNode)) {
			this.itemsNodes.put(commandNode, new HashMap<String, DMStatusManager.ItemsNode>());
			this.commandNodes.offer(commandNode);
			this.statusCodeNodes.put(commandNode, new LinkedList<String>());
			this.isUnique.put(commandNode, true);
		}

		if (!this.itemsNodes.get(commandNode).containsKey(statusCode)) {
			this.itemsNodes.get(commandNode).put(statusCode, new ItemsNode());
			this.statusCodeNodes.get(commandNode).offer(statusCode);
			if (this.isUnique.get(commandNode) && this.itemsNodes.get(commandNode).keySet().size() >= 2) {
				this.isUnique.put(commandNode, false);
			}
		}

		if (targetURI != null) {
			this.itemsNodes.get(commandNode).get(statusCode).getTargetURI().add(targetURI);
			if (format != null && type != null && data != null) {
				final List<Results> results = this.itemsNodes.get(commandNode).get(statusCode).getResults();
				final Results newResultsNode = new Results(targetURI, format, type, data);
				results.add(newResultsNode);
				if (results.size() == 1) {
					this.itemsNodes.get(commandNode).get(statusCode).setFormat(format);
					this.itemsNodes.get(commandNode).get(statusCode).setType(type);
				} else {
					final String globalFormat = this.itemsNodes.get(commandNode).get(statusCode).getFormat();
					if (globalFormat != null) {
						if (globalFormat != format) {
							this.itemsNodes.get(commandNode).get(statusCode).setFormat(null);
						}
					}
					final String globalType = this.itemsNodes.get(commandNode).get(statusCode).getType();
					if (globalType != null) {
						if (globalType != type) {
							this.itemsNodes.get(commandNode).get(statusCode).setType(null);
						}
					}
				}
			}
		}

		if (sourceURI != null) {
			this.itemsNodes.get(commandNode).get(statusCode).getSourceURI().add(sourceURI);
		}
	}

	public boolean onlySyncHdrStatus() {
		return this.commandNodes.size() <= 1;
	}

	public boolean isValidStatus() {
		return this.commandNodes.size() != 0;
	}

	public void nextStatus() {
		final Queue<String> statusCodeNodesQueue = this.statusCodeNodes.get(currentCommandNode());
		this.itemsNodes.get(currentCommandNode()).remove(statusCodeNodesQueue.poll());
		if (statusCodeNodesQueue.isEmpty()) {
			this.itemsNodes.remove(currentCommandNode());
			this.statusCodeNodes.remove(currentCommandNode());
			this.isUnique.remove(currentCommandNode());
			this.commandNodes.remove();
		}
	}

	public String getMsgRef() {
		return currentCommandNode().getMsgID();
	}

	public String getCmdRef() {
		return currentCommandNode().getCmdID();
	}

	public String getCmd() {
		return currentCommandNode().cmd;
	}

	public List<String> getTargetRef() {
		return currentIsUnique() ? new ArrayList<String>(0) : currentItemNode().getTargetURI();
	}

	public List<String> getSourceRef() {
		return currentIsUnique() ? new ArrayList<String>(0) : currentItemNode().getSourceURI();
	}

	public String getStatusCode() {
		return currentStatusCode();
	}

	public List<Results> getResults() {
		return currentItemNode().results;
	}

	public String getGlobalFormat() {
		return currentItemNode().getFormat();
	}

	public String getGlobalType() {
		return currentItemNode().getType();
	}

	private CommandNode currentCommandNode() {
		return this.commandNodes.peek();
	}

	private String currentStatusCode() {
		return this.statusCodeNodes.get(currentCommandNode()).peek();
	}

	private ItemsNode currentItemNode() {
		return this.itemsNodes.get(currentCommandNode()).get(currentStatusCode());
	}

	private boolean currentIsUnique() {
		return this.isUnique.get(currentCommandNode());
	}

	private static final class CommandNode {
		private final String msgID;
		private final String cmdID;
		private final String cmd;

		public CommandNode(final String msgID, final String cmdID, final String cmd) {
			this.msgID = msgID;
			this.cmdID = cmdID;
			this.cmd = cmd;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.cmd.hashCode();
			result = prime * result + this.cmdID.hashCode();
			result = prime * result + this.msgID.hashCode();
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj != null && obj instanceof CommandNode) {
				final CommandNode other = (CommandNode) obj;
				return this.msgID.equals(other.msgID) && this.cmdID.equals(other.cmdID) && this.cmd.equals(other.cmd);
			} else {
				return false;
			}
		}

		/**
		 * @return the msgID
		 */
		public String getMsgID() {
			return msgID;
		}

		/**
		 * @return the cmdID
		 */
		public String getCmdID() {
			return cmdID;
		}
	}

	private static final class ItemsNode {

		private final List<String> targetURI;
		private final List<String> sourceURI;
		private String format;
		private String type;
		private final List<Results> results;

		public ItemsNode() {
			this.targetURI = new LinkedList<String>();
			this.sourceURI = new LinkedList<String>();
			this.results = new LinkedList<DMStatusManager.Results>();
		}

		/**
		 * @return the targetURI
		 */
		public List<String> getTargetURI() {
			return targetURI;
		}

		/**
		 * @return the sourceURI
		 */
		public List<String> getSourceURI() {
			return sourceURI;
		}

		/**
		 * @return the format
		 */
		public String getFormat() {
			return format;
		}

		/**
		 * @param format
		 *            the format to set
		 */
		public void setFormat(String format) {
			this.format = format;
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * @param type
		 *            the type to set
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * @return the results
		 */
		public List<Results> getResults() {
			return results;
		}
	}

	public static final class Results {
		private final String sourceURI;
		private final String format;
		private final String type;
		private final String data;

		public Results(final String sourceURI, final String format, final String type, final String data) {
			this.sourceURI = sourceURI;
			this.format = format;
			this.type = type;
			this.data = data;
		}

		public String getFormat() {
			return this.format;
		}

		public String getType() {
			return this.type;
		}

		public String getSourceURI() {
			return this.sourceURI;
		}

		public String getData() {
			return this.data;
		}

	}

}
