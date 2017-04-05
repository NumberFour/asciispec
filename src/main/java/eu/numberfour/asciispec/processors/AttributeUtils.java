package eu.numberfour.asciispec.processors;

import java.util.Map;

public class AttributeUtils {

	static public int getLeveloffset(Map<String, Object> attributes) {
		int leveloffset = 0;
		try {
			if (attributes.containsKey("leveloffset")) {
				leveloffset = Integer.parseInt(String.valueOf(attributes.get("leveloffset")));
			}
		} catch (Throwable t) {
		}
		return leveloffset;
	}

	static public void appendLeveloffset(StringBuilder strb, int leveloffset, boolean undo) {
		if (leveloffset != 0) {
			if (undo) {
				leveloffset = -leveloffset;
			}
			strb.append(":leveloffset: ");
			if (leveloffset > 0) {
				strb.append("+");
			}
			strb.append(String.valueOf(leveloffset)).append("\n");
		}
	}

	static public boolean isInSelectedLineRange(Map<String, Object> attributes, int relLine) {
		if (!attributes.containsKey("lines") || attributes.get("lines") == null)
			return true;

		String selectionStr = String.valueOf(attributes.get("lines"));
		String[] selectionsStr = selectionStr.split("[,;]");
		for (String sel : selectionsStr) {
			if (sel.contains("..")) {
				// it should be a range, e.g.: 1..7
				String[] range = sel.split("\\.\\.");
				int start = Integer.valueOf(range[0]);
				int end = Integer.valueOf(range[1]);
				if (end == -1)
					end = Integer.MAX_VALUE;

				if (relLine >= start && relLine <= end)
					return true;
			} else {
				// it should be a simple number
				int number = Integer.valueOf(sel);
				if (relLine == number)
					return true;
			}
		}

		return false;
	}
}
