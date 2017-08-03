/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package familyclassifier;

/**
*
* @author msg
*/
public class Constants {
	
	public static final String[] EXTRACTION_LABELS = new String[] { "Classwise IG", "Classwise NAD", "IG", "NAD" };
	
	public static final String VIRUS_TOTAL_TOKEN_CONFIG = "virus_total_token";

	public static final String REPORTS_PATH_CONFIG = "reports_path";
	public static final String TRAINING_PATH_CONFIG = "training_path";
	public static final String TEST_PATH_CONFIG = "test_path";
	public static final String CUCKOO_PATH_CONFIG = "cuckoo_path";
	public static final String FILE_EXTENSION_CONFIG = "file_extension";

	public static final String DISTINCTIVE_FEATURES_FILE_CONFIG = "distinctive_features_file";
	public static final String ID_MAPPING_FILE = "id_mapping_file";
	
	public static final String[] CLASS_NAMES = new String[] { "cerber", "crysis", "hydracrypt", "wannacry"};

	public static final String FEATURE_TYPE = "feature_type";
	public static final String KNN_KVALUE_CONFIG = "knn_value";
	public static final String K_NAD_CONFIG = "k_value";
	public static final String NGRAM_SIZE_CONFIG = "ngram_size";
	public static final String TOP_RANKED_SIZE_CONFIG = "top_ranked_size";
	public static final String HEATMAP_SIZE_CONFIG = "heat_map_size";
	public static final String REGEX_PATTERN_CONFIG = "regex_pattern";
	
	public static final String NUMBER_OF_DATA_INPUT_CONFIG = "number_of_data_input";
	public static final String UNDERSCORE = "_";
	
	public static final String NGRAM_FEATURES = "ngram";
	
	public static final String APICALL_FEATURES = "apicall";
	public static final String APICALLNGRAM_FEATURES = "apicallngram";
	public static final String APICALLWILDCARD_FEATURES = "apicallnwildcard";
	public static final String APIWITHARGS_FEATURES = "apicallwithargs";
	public static final String APIFREQUENCY_FEATURES = "apicallfreq";
	
	public static final String SYSCALL_FEATURES = "syscall";
	public static final String SYSCALLNGRAM_FEATURES = "syscallngram";
	public static final String SYSCALLWILDCARD_FEATURES = "syscallnwildcard";
	public static final String SYSCALLWITHARGS_FEATURES = "syscallwithargs";
	
	
	public static final String FILE_FEATURES = "file";
	public static final String DLL_FEATURES = "dll";
	public static final String REGKEY_FEATURES = "regkey";
	public static final String MUTEX_FEATURES = "mutex";
	
	
	public static final String EXTENSION_SEPERATOR = ".";
	public static final String COUNT_OF_SAMPLES_PER_CLASS = "CLASSSAMPLES";
	public static final String PRIOR_PER_CLASS = "CLASSPRIORS";
	public static final String TEST_LABEL = "test";
	public static final String TRAIN_LABEL = "train";
	public static final String COLUMN_CHAR = ":";
	public static final String TAB_CHAR = "\t";
	public static final String NEW_LINE = "\n";

	public static final String VT_URL_RETRIEVE_REPORTS_API = "https://www.virustotal.com/vtapi/v2/file/report";
	public static final String VT_URL_FILE_SEARCH_API = "https://www.virustotal.com/vtapi/v2/file/search";

	public static final String VT_URL_DOWNLOAD_HASHES_API = "https://www.virustotal.com/vtapi/v2/file/download";

	public static final String CUCKOO_CREATE_FILE_API = "http://localhost:8090/tasks/create/file";
	public static final String CUCKOO_CREATE_URL_API = "http://localhost:8090/tasks/create/url";
	public static final int PROCESS_TIMEOUT_SECS = 80;
	public static final int CUCKOO_TIMEOUT_SECS = 80;

}
