package eu.numberfour.asciispec.processors;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.asciidoctor.ast.Document;

import eu.numberfour.asciispec.ParseException;
import eu.numberfour.asciispec.processors.SourceLinkPreprocessor.IndexEntryInfoResult;
import eu.numberfour.asciispec.sourceindex.AmbiguousPQNExcpetion;
import eu.numberfour.asciispec.sourceindex.IndexEntryInfo;
import eu.numberfour.asciispec.sourceindex.IndexFileParser;
import eu.numberfour.asciispec.sourceindex.NotInSourceIndexExcpetion;
import eu.numberfour.asciispec.sourceindex.PQNParser;
import eu.numberfour.asciispec.sourceindex.SourceIndexDatabase;

public interface SourceLinkMixin {
	static class SourceLinkMixinState {
		private SourceIndexDatabase database;
		private boolean configuring = true;
	}

	SourceLinkMixinState getState();

	File getIndexFile();

	String error(Document document, String consoleMsg, String inlineMsg);

	/**
	 * The expected structure of the root directory is as follows:
	 * <ul>
	 * <li>root/
	 * <ul>
	 * <li>adoc_gen/
	 * <ul>
	 * <li>index.idx
	 * <li>...
	 * </ul>
	 * <li>html/
	 * <li>...
	 * </ul>
	 * </ul>
	 */
	default void ensureDatabase() throws IOException, ParseException {
		if (getState().database != null)
			return;

		getState().database = new SourceIndexDatabase();
		getState().database = IndexFileParser.parse(getIndexFile().toPath(), StandardCharsets.UTF_8);
		getState().configuring = false;
	}

	default boolean isConfiguring() {
		return getState().configuring;
	}

	default IndexEntryInfoResult getIndexEntryInfo(SourceLinkMixinState state, Document document, String macro,
			String givenPQN) {

		IndexEntryInfo iei = null;
		String completePQN = givenPQN;
		String url = null;
		String errorMsg = "";

		List<String> pqnStack = null;
		try {
			pqnStack = PQNParser.parse(givenPQN);
			iei = state.database.getEntry(pqnStack);

		} catch (ParseException e) {
			errorMsg += error(document, "macro could not be parsed: '" + macro + "'.", "PQN malformed");
		} catch (AmbiguousPQNExcpetion e) {
			errorMsg += error(document, "PQN is ambiguous: '" + givenPQN + "'.", "Ambiguous PQN");
		} catch (NotInSourceIndexExcpetion e) {
			errorMsg += error(document, "PQN not found: '" + givenPQN + "'.", "PQN not found");
		}

		IndexEntryInfoResult result = new IndexEntryInfoResult(iei, url, completePQN, errorMsg);
		return result;
	}
}
