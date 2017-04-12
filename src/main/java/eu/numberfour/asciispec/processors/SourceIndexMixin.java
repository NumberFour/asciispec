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

/**
 * This interface provides a set of default methods for loading and querying a
 * {@link SourceIndexDatabase} during parsing of included Asciidoctor documents.
 * These methods are stateful and thus rely on the state instance of type
 * {@link SourceIndexMixinState} which has to be by the implementing client
 * class. Besides, this interface is also base on a file search method and an
 * error printing method.
 * <p>
 * Since the methods are already implemented, this Java interface is called
 * <i>Mixin</i>.
 */
public interface SourceIndexMixin {
	static class SourceIndexMixinState {
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

	/**
	 * Returns the state instance of this interface.
	 */
	SourceIndexMixinState getState();

	/**
	 * Searches a file during a default method call.
	 */
	File searchFile(String fileName) throws FileNotFoundException, MultipleFileMatchesException;

	/**
	 * Prints an error that occurred during a default method call.
	 */
	String error(Document document, String consoleMsg, String inlineMsg);

	/**
	 * Configures and loads the database.
	 * <p>
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

	/**
	 * Returns true iff the state object is not configured yet.
	 */
	default boolean isConfiguring() {
		return getState().configuring;
	}

	/**
	 * Resets this Mixin.
	 */
	default void reset() {
		getState().configuring = true;
		getState().database = null;
		getState().gendirPath = null;
		getState().indexFile = null;
	}

	/**
	 * Sets the state variables {@link SourceIndexMixinState#gendirPath} and
	 * {@link SourceIndexMixinState#indexFile}. The given filename must be
	 * absolute.
	 *
	 * @param genadocdir
	 *            must be absolute
	 */
	default void setIndexFile(File genadocdir) throws FileNotFoundException, MultipleFileMatchesException {
		if (!isConfiguring()) {
			if (getState().gendirPath != null && getState().gendirPath.equals(genadocdir.toPath())) {
				return; // ignore same path config
			} else {
				throw new IllegalArgumentException(
						"The configuration must not be specified after first use of the source link macro");
			}
		}

		getState().gendirPath = genadocdir.toPath();
		getState().indexFile = getState().gendirPath.resolve(INDEX_FILE_NAME).toFile();
	}

	/**
	 * Returns the path to all generated Asciidoc files.
	 */
	default Path getGendirPath() {
		return getState().gendirPath;
	}

	/**
	 * Returns the path to the generated API module files.
	 */
	default Path getGendirModules() {
		return getGendirPath().resolve(GEN_DIR_MODULES).normalize();
	}

	/**
	 * Returns the index file.
	 */
	default File getIndexFile() {
		return getState().indexFile;
	}

	/**
	 * Queries the database for a given PQN string.
	 * <p>
	 * The parameters document and macro are used for displaying error messages
	 * only.
	 */
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
