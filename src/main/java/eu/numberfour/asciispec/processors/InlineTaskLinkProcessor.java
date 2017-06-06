/**
 * Copyright (c) 2016 NumberFour AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   NumberFour AG - Initial API and implementation
 */
package eu.numberfour.asciispec.processors;

import static eu.numberfour.asciispec.AdocUtils.createLinkWithIcon;
import static eu.numberfour.asciispec.AdocUtils.getMultiValuedAttribute;
import static eu.numberfour.asciispec.AdocUtils.transformVariable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;

import eu.numberfour.asciispec.issue.IssueAcceptor;
import eu.numberfour.asciispec.issue.IssuePrinter;

/**
 * A block macro processor that creates links to tasks on GitHub or JIRA.
 */
public class InlineTaskLinkProcessor extends InlineMacroProcessor {

	// TODO: make this configurable
	private final IssueAcceptor issueAcceptor = new IssuePrinter();
	
	private Map<String, RepositoryConfig> repositoryConfigs;

	private class RepositoryConfig {
		public final String prefix;
		@SuppressWarnings("unused")
		public final String name;
		public final String description;
		public final String urlPattern;
		public final String icon;
		public final String textPattern;
		public final String taskStatusFileUrl;

		private Map<String, TaskStatus> taskStatusCache;

		public RepositoryConfig(String prefix, String name, String description, String urlPattern, String icon,
				String textPattern, String taskStatusFileUrl) {
			this.prefix = prefix;
			this.name = name;
			this.description = description;
			this.urlPattern = urlPattern;
			this.icon = icon;
			this.textPattern = textPattern;
			this.taskStatusFileUrl = taskStatusFileUrl;
		}

		public TaskStatus getTaskStatus(String taskId) {
			TaskStatus status = taskStatusCache.get(taskId);
			if (status == null)
				status = TaskStatus.UNKNOWN;
			return status;
		}

		public void initializeTaskStatusCache(ContentNode node) {
			taskStatusCache = loadTaskStatusCache(node);
		}

		private Map<String, TaskStatus> loadTaskStatusCache(ContentNode node) {
			Map<String, TaskStatus> result = new HashMap<>();

			if (!taskStatusFileUrl.isEmpty()) {
				try {
					URL url = new URL(taskStatusFileUrl);
					try (@SuppressWarnings("resource")
					Scanner scanner = new Scanner(url.openStream()).useDelimiter("\\n")) {
						int lineNumber = 1;
						while (scanner.hasNext()) {
							String line = scanner.next();
							String[] parts = line.split(":");

							if (parts.length == 2) {
								String taskId = parts[0];
								TaskStatus taskStatus = TaskStatus.valueOf(parts[1]);
								if (taskStatus != null) {
									result.put(taskId, taskStatus);
								} else {
									issueAcceptor.warn(node, "Unknown task status '" + parts[1] + "' in line "
											+ lineNumber + " of task status file " + taskStatusFileUrl);
								}
							} else {
								issueAcceptor.warn(node, "Malformed task status entry in line " + lineNumber
										+ " of task status file " + taskStatusFileUrl);
							}

							lineNumber++;
						}
					}
				} catch (MalformedURLException e) {
					issueAcceptor.error(node, "Malformed task status file URL: " + taskStatusFileUrl);
				} catch (IOException e) {
					issueAcceptor.error(node, "Error while fetching task status file from URL " + taskStatusFileUrl
							+ ": " + e.getMessage());
				}
			}
			return result;
		}
	}

	private static enum TaskStatus {
		OPEN, CLOSED, UNKNOWN
	}

	@Override
	public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
		try {
			RepositoryConfig repositoryConfig = getRepositoryConfig(parent.getDocument(), target);
			if (repositoryConfig == null) {
				String message = "Unknown task repository for task '" + target + "'";
				issueAcceptor.error(parent, message);
				return message;
			}

			return createTaskLink(parent, target, repositoryConfig);
		} catch (IllegalArgumentException e) {
			String message = e.getMessage();
			issueAcceptor.error(parent, message);
			return message;
		}
	}

	private static final String TASK_ID_NAME = "TASK_ID";
	private static final String CONFIG_NAME_PREFIX = "task_def_";

	private RepositoryConfig getRepositoryConfig(ContentNode document, String target) {
		final String targetLower = target.toLowerCase();

		final Map<String, RepositoryConfig> repositoryConfigs = getRepositoryConfigs(document);
		for (Entry<String, RepositoryConfig> entry : repositoryConfigs.entrySet()) {
			String prefix = entry.getKey();
			if (targetLower.startsWith(prefix))
				return entry.getValue();
		}
		return null;
	}

	private Map<String, RepositoryConfig> getRepositoryConfigs(ContentNode document) {
		if (repositoryConfigs == null) {
			Map<String, String> values = getMultiValuedAttribute(document, CONFIG_NAME_PREFIX);
			if (values.isEmpty())
				throw new IllegalArgumentException("Missing task configuration");

			repositoryConfigs = new HashMap<>();

			for (Entry<String, String> entry : values.entrySet()) {
				String prefix = entry.getKey().toLowerCase();
				String configStr = entry.getValue();
				repositoryConfigs.put(prefix, parseRepositoryConfig(document, prefix, configStr));
			}
		}

		return repositoryConfigs;
	}
		
	private RepositoryConfig parseRepositoryConfig(ContentNode node, String prefix, String configStr) {
		String[] parts = configStr.split(";");
		if (parts.length < 5 || parts.length > 6)
			throw new IllegalArgumentException("Invalid repository configuration string: '" + configStr + "'");

		String taskStatusFileUrl = parts.length >= 6 ? parts[5] : "";
		RepositoryConfig config = new RepositoryConfig(prefix, parts[0], parts[1], parts[2], parts[3], parts[4],
				taskStatusFileUrl);
		config.initializeTaskStatusCache(node);
		return config;
	}

	private Object createTaskLink(ContentNode parent, String target, RepositoryConfig repositoryConfig) {
		String taskId = getTaskId(target, repositoryConfig);
		String taskUrl = getTaskUrl(taskId, repositoryConfig);
		String taskText = getTaskText(taskId, repositoryConfig);
		String taskTitle = repositoryConfig.description;
		String iconName = repositoryConfig.icon;
		
		TaskStatus status = repositoryConfig.getTaskStatus(taskId);
		String role = "status_" + status.toString();

		return createLinkWithIcon(this, parent, taskUrl, taskText, taskTitle, role, iconName).convert();
	}

	private String getTaskId(String target, RepositoryConfig repositoryConfig) {
		return target.substring(repositoryConfig.prefix.length());
	}

	private String getTaskUrl(String taskId, RepositoryConfig repositoryConfig) {
		return transformVariable(repositoryConfig.urlPattern, TASK_ID_NAME, taskId);
	}

	private String getTaskText(String taskId, RepositoryConfig repositoryConfig) {
		return transformVariable(repositoryConfig.textPattern, TASK_ID_NAME, taskId);
	}
}
