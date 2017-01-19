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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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

	private static class RepositoryConfig {
		public final String prefix;
		@SuppressWarnings("unused")
		public final String name;
		public final String description;
		public final String urlPattern;
		public final String icon;
		public final String textPattern;

		public RepositoryConfig(String prefix, String name, String description, String urlPattern, String icon,
				String textPattern) {
			this.prefix = prefix;
			this.name = name;
			this.description = description;
			this.urlPattern = urlPattern;
			this.icon = icon;
			this.textPattern = textPattern;
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
		Map<String, String> values = getMultiValuedAttribute(document, CONFIG_NAME_PREFIX);
		if (values.isEmpty())
			throw new IllegalArgumentException("Missing task configuration");

		Map<String, RepositoryConfig> result = new HashMap<>();

		for (Entry<String, String> entry : values.entrySet()) {
			String prefix = entry.getKey().toLowerCase();
			String configStr = entry.getValue();
			result.put(prefix, parseRepositoryConfig(prefix, configStr));
		}

		return result;
	}

	private RepositoryConfig parseRepositoryConfig(String prefix, String configStr) {
		String[] parts = configStr.split(";");
		if (parts.length != 5)
			throw new IllegalArgumentException("Invalid repository configuration string: '" + configStr + "'");

		return new RepositoryConfig(prefix, parts[0], parts[1], parts[2], parts[3], parts[4]);
	}

	private Object createTaskLink(ContentNode parent, String target, RepositoryConfig repositoryConfig) {
		String taskId = getTaskId(target, repositoryConfig);
		String taskUrl = getTaskUrl(taskId, repositoryConfig);
		String taskText = getTaskText(taskId, repositoryConfig);
		String taskTitle = repositoryConfig.description;
		String iconName = repositoryConfig.icon;

		return createLinkWithIcon(this, parent, taskUrl, taskText, taskTitle, iconName).convert();
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
