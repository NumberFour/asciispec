package eu.numberfour.asciispec.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.asciidoctor.ast.Document;

import eu.numberfour.asciispec.ParseException;
import eu.numberfour.asciispec.findresolver.MultipleFileMatchesException;
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
		private Path gendirPath;
		private File indexFile;
	}

	public static class IndexEntryInfoResult {
		public final IndexEntryInfo iei;
		public final String url;
		public final String completePQN;
		public final String errorMsg;

		IndexEntryInfoResult(IndexEntryInfo iei, String url, String completePQN, String errorMsg) {
			this.iei = iei;
			this.url = url;
			this.completePQN = completePQN;
			this.errorMsg = errorMsg;
		}
	}

	/** File name of the index file */
	String INDEX_FILE_NAME = "index.idx";
	String GEN_DIR_MODULES = "modules";

	SourceLinkMixinState getState();

	File searchFile(String fileName) throws FileNotFoundException, MultipleFileMatchesException;

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

		try {
			getState().database = new SourceIndexDatabase();
			getState().database = IndexFileParser.parse(getState().indexFile.toPath(), StandardCharsets.UTF_8);
			getState().configuring = false;
		} catch (IOException e) {
			String msg = "IOException: " + e.getMessage();
			throw new IOException(msg);
		}
	}

	default boolean isConfiguring() {
		return getState().configuring;
	}

	/**
	 * Sets the state variables {@link SourceLinkMixinState#gendirPath} and
	 * {@link SourceLinkMixinState#indexFile}. The given filename must be
	 * absolute.
	 *
	 * @param genadocdir
	 *            must be absolute
	 */
	default void setIndexFile(File genadocdir) throws FileNotFoundException, MultipleFileMatchesException {
		if (!isConfiguring()) {
			throw new IllegalArgumentException(
					"The configuration must not be specified after first use of the source link macro");
		}

		getState().gendirPath = genadocdir.toPath();
		getState().indexFile = getState().gendirPath.resolve(INDEX_FILE_NAME).toFile();
	}

	default Path getGendirPath() {
		return getState().gendirPath;
	}

	default Path getGendirModules() {
		return getGendirPath().resolve(GEN_DIR_MODULES).normalize();
	}

	default File getIndexFile() {
		return getState().indexFile;
	}

	default IndexEntryInfoResult getIndexEntryInfo(Document document, String macro, String givenPQN) {
		IndexEntryInfo iei = null;
		String completePQN = givenPQN;
		String url = null;
		String errorMsg = null;

		List<String> pqnStack = null;
		try {
			pqnStack = PQNParser.parse(givenPQN);
			iei = getState().database.getEntry(pqnStack);

		} catch (ParseException e) {
			errorMsg = error(document, "PQN could not be parsed: '" + macro + "'.", "PQN malformed");
		} catch (AmbiguousPQNExcpetion e) {
			errorMsg = error(document, "PQN is ambiguous: '" + givenPQN + "'.", "Ambiguous PQN");
		} catch (NotInSourceIndexExcpetion e) {
			errorMsg = error(document, "PQN not found: '" + givenPQN + "'.", "PQN not found");
		}

		IndexEntryInfoResult result = new IndexEntryInfoResult(iei, url, completePQN, errorMsg);
		return result;
	}
}
