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

import eu.numberfour.asciispec.AdocUtils;
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
		public final String taskInfoFileUrl;

		private Map<String, TaskInfo> taskInfoCache;

		public RepositoryConfig(String prefix, String name, String description, String urlPattern, String icon,
				String textPattern, String taskInfoFileUrl) {
			this.prefix = prefix;
			this.name = name;
			this.description = description;
			this.urlPattern = urlPattern;
			this.icon = icon;
			this.textPattern = textPattern;
			this.taskInfoFileUrl = taskInfoFileUrl;
		}

		public TaskStatus getTaskStatus(String taskId) {
			TaskStatus status = TaskStatus.UNKNOWN;
			if (taskInfoCache.containsKey(taskId))
				status = taskInfoCache.get(taskId).status;
			return status;
		}

		public String getTaskTitle(String taskId) {
			String title = null;
			if (taskInfoCache.containsKey(taskId))
				title = taskInfoCache.get(taskId).title;
			return title;
		}

		public void initializeTaskStatusCache(ContentNode node) {
			taskInfoCache = loadTaskInfoCache(node);
		}

		private Map<String, TaskInfo> loadTaskInfoCache(ContentNode node) {
			Map<String, TaskInfo> result = new HashMap<>();

			if (!taskInfoFileUrl.isEmpty()) {
				try {
					URL url = new URL(taskInfoFileUrl);
					try (@SuppressWarnings("resource")
					Scanner scanner = new Scanner(url.openStream()).useDelimiter("\\n")) {

						readStatusFileContent(node, result, scanner);

					}
				} catch (MalformedURLException e) {
					issueAcceptor.error(node, "Malformed task status file URL: " + taskInfoFileUrl);
				} catch (IOException e) {
					issueAcceptor.error(node, "Error while fetching task status file from URL " + taskInfoFileUrl
							+ ": " + e.getMessage());
				}
			}
			return result;
		}

		private void readStatusFileContent(ContentNode node, Map<String, TaskInfo> result, Scanner scanner) {
			int lineNumber = 0;
			while (scanner.hasNext()) {
				String line = scanner.next();
				lineNumber++;

				// limit=3, since the tooltip contains all the rest string
				String[] parts = line.split(":", 3);

				if (parts.length < 2) {
					String msg = String.format("Malformed task status entry in line %d of task status file %s",
							lineNumber, taskInfoFileUrl);
					issueAcceptor.warn(node, msg);
					continue;
				}

				String taskId = parts[0];
				TaskStatus taskStatus = null;
				if (parts[1] != null && !parts[1].isEmpty()) {
					taskStatus = TaskStatus.valueOf(parts[1]);
				}

				if (taskStatus == null) {
					String msg = String.format("Unknown task status '%s' in line %d of task status file %s", parts[1],
							lineNumber, taskInfoFileUrl);
					issueAcceptor.warn(node, msg);
					continue;
				}

				String title = null;
				if (parts.length > 2) {
					title = parts[2]; // the title information is optional
				}

				TaskInfo info = new TaskInfo(taskId, taskStatus, title);
				result.put(taskId, info);
			}
		}
	}

	static class TaskInfo {
		final String id;
		final TaskStatus status;
		final String title;

		TaskInfo(String id, TaskStatus status, String title) {
			this.id = id;
			this.status = status;
			this.title = title;
		}
	}

	static enum TaskStatus {
		OPEN, CLOSED, UNKNOWN;
		
		public String getRole() {
			switch (this) {
			case OPEN:
				return "maroon strong";
			case CLOSED:
				return "green line-through";
			default:
				return "gray";
			}
		}
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

			return createTaskLink(parent, target, repositoryConfig, attributes);
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

	private Object createTaskLink(ContentNode parent, String target, RepositoryConfig repositoryConfig,
			Map<String, Object> attributes) {

		String taskId = getTaskId(target, repositoryConfig);
		String taskUrl = getTaskUrl(taskId, repositoryConfig);
		String taskText = getTaskText(taskId, repositoryConfig, attributes);
		String taskTitle = getTaskTitle(taskId, repositoryConfig);
		String iconName = repositoryConfig.icon;
		
		TaskStatus status = repositoryConfig.getTaskStatus(taskId);
		String role = status.getRole();

		return createLinkWithIcon(this, parent, taskUrl, taskText, taskTitle, role, iconName).convert();
	}

	private String getTaskTitle(String taskId, RepositoryConfig repositoryConfig) {
		String defaultTitle = repositoryConfig.description;
		String title = repositoryConfig.getTaskTitle(taskId);
		title = (title == null) ? defaultTitle : defaultTitle + ": " + title;

		return title;
	}

	private String getTaskId(String target, RepositoryConfig repositoryConfig) {
		return target.substring(repositoryConfig.prefix.length());
	}

	private String getTaskUrl(String taskId, RepositoryConfig repositoryConfig) {
		return transformVariable(repositoryConfig.urlPattern, TASK_ID_NAME, taskId);
	}

	private String getTaskText(String taskId, RepositoryConfig repositoryConfig, Map<String, Object> attributes) {
		String defaultText = transformVariable(repositoryConfig.textPattern, TASK_ID_NAME, taskId);

		Map<String, Object> attrs = new HashMap<>();
		for (Map.Entry<String, Object> e : attributes.entrySet())
			attrs.put(e.getKey(), e.getValue());
		String text = AdocUtils.getAttributeAsString(defaultText, attrs, "title", "0", "1");
		return text;
	}

}
