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
package eu.numberfour.asciispec.sourceindex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Database of all {@link IndexEntryInfo}s
 */
public class SourceIndexDatabase {
	private final Map<String, Object> pathMap = new HashMap<>();
	private int size;

	/**
	 * Returns an {@link IndexEntryInfo} by a given PQN stack.
	 *
	 * @param pqnStack
	 *            a PQN stack of user defined length
	 * @return the matching {@link IndexEntryInfo}
	 * @throws NotInSourceIndexExcpetion
	 *             if no matching {@link IndexEntryInfo} could be found
	 * @throws AmbiguousPQNExcpetion
	 *             if there exist more than one entries that match to the given PQN
	 */
	@SuppressWarnings("unchecked")
	public IndexEntryInfo getEntry(List<String> pqnStack) throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		Objects.requireNonNull(pqnStack);
		Map<String, ?> curMap = pathMap;

		// First: descend using the nameStack
		for (int i = 0; i < pqnStack.size(); i++) {
			String name = pqnStack.get(i);
			Objects.requireNonNull(name);

			if (!curMap.containsKey(name))
				throw new NotInSourceIndexExcpetion();

			Object nextObj = curMap.get(name);
			if (nextObj instanceof Map)
				curMap = (Map<String, ?>) nextObj;

			if (nextObj instanceof IndexEntryInfo)
				return (IndexEntryInfo) nextObj;
		}

		// Second: One-way descent the rest.
		while (!curMap.isEmpty()) {
			if (curMap.size() > 1)
				throw new AmbiguousPQNExcpetion();

			Object nextObj = curMap.values().iterator().next();
			if (nextObj instanceof Map)
				curMap = (Map<String, ?>) nextObj;

			if (nextObj instanceof IndexEntryInfo)
				return (IndexEntryInfo) nextObj;
		}

		throw new NotInSourceIndexExcpetion();
	}

	/**
	 * Returns all {@link IndexEntryInfo}s of the database.
	 */
	public List<IndexEntryInfo> getAllEntries() {
		List<IndexEntryInfo> allEntries = new ArrayList<>();
		getAllEntries(allEntries, pathMap);
		return allEntries;
	}

	private void getAllEntries(List<IndexEntryInfo> allEntries, Map<?, ?> map) {
		for (Object obj : map.values()) {
			if (obj instanceof IndexEntryInfo)
				allEntries.add((IndexEntryInfo) obj);
			if (obj instanceof Map<?, ?>)
				getAllEntries(allEntries, (Map<?, ?>) obj);
		}
	}

	/**
	 * @return the size of the database
	 */
	public int size() {
		return size;
	}

	/**
	 * Method for inserting {@link IndexEntryInfo}s into the database.
	 *
	 * @throws AlreadyInsertedException
	 *             if the entry matches with an already inserted entry.
	 */
	@SuppressWarnings("unchecked")
	void put(IndexEntryInfo ieInfo) throws AlreadyInsertedException {
		boolean newEntry = false;
		// remove preceeding empty stack entries!!!
		Map<String, Object> curMap = pathMap;
		for (int i = ieInfo.getHierarchyDepth() - 1; i > 0; i--) {
			String name = ieInfo.getHierarchyElement(i);

			if (!curMap.containsKey(name)) {
				Object value = new HashMap<String, Object>();
				newEntry = true;
				curMap.put(name, value);
			}

			Object value = curMap.get(name);
			curMap = (HashMap<String, Object>) value;
		}

		if (!newEntry)
			throw new AlreadyInsertedException("Entry already in database: " + ieInfo);

		String name = ieInfo.getHierarchyElement(0); // repository name
		if (curMap.containsKey(name))
			throw new AlreadyInsertedException("Entry already in database: " + ieInfo);

		curMap.put(name, ieInfo);
		size++;
	}

	/**
	 * @param database
	 *            the database whose entries are added
	 * @throws AlreadyInsertedException
	 *             in case an entry is already in this database.
	 */
	public void addAllEntries(SourceIndexDatabase database) throws AlreadyInsertedException {
		for (IndexEntryInfo sie : database.getAllEntries()) {
			put(sie);
		}
	}

}
