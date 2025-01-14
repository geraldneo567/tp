package unibook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import unibook.commons.core.Config;
import unibook.commons.core.LogsCenter;
import unibook.commons.core.Version;
import unibook.commons.exceptions.DataConversionException;
import unibook.commons.util.ConfigUtil;
import unibook.commons.util.StringUtil;
import unibook.logic.Logic;
import unibook.logic.LogicManager;
import unibook.model.Model;
import unibook.model.ModelManager;
import unibook.model.ReadOnlyUniBook;
import unibook.model.ReadOnlyUserPrefs;
import unibook.model.UniBook;
import unibook.model.UserPrefs;
import unibook.model.util.SampleDataUtil;
import unibook.storage.JsonUniBookStorage;
import unibook.storage.JsonUserPrefsStorage;
import unibook.storage.Storage;
import unibook.storage.StorageManager;
import unibook.storage.UniBookStorage;
import unibook.storage.UserPrefsStorage;
import unibook.ui.Ui;
import unibook.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final Version VERSION = new Version(1, 3, 2, true);

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    protected Config config;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing UniBook ]===========================");
        super.init();

        AppParameters appParameters = AppParameters.parse(getParameters());
        config = initConfig(appParameters.getConfigPath());

        UserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(config.getUserPrefsFilePath());
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        UniBookStorage uniBookStorage = new JsonUniBookStorage(userPrefs.getUniBookFilePath());
        storage = new StorageManager(uniBookStorage, userPrefsStorage);

        initLogging(config);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic);

        ModelManager modelManager = (ModelManager) model;
        modelManager.setUi(ui);
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s unibook and {@code userPrefs}. <br>
     * The data from the sample unibook will be used instead if {@code storage}'s unibook is not found,
     * or an empty unibook will be used instead if errors occur when reading {@code storage}'s unibook.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        Optional<ReadOnlyUniBook> uniBookOptional;
        ReadOnlyUniBook initialData;
        try {
            uniBookOptional = storage.readUniBook();
            if (!uniBookOptional.isPresent()) {
                logger.info("Data file not found. Will be starting with a sample UniBook");
            }
            initialData = uniBookOptional.orElseGet(SampleDataUtil::getSampleUniBook);
        } catch (DataConversionException e) {
            logger.warning("Data file not in the correct format. Will be starting with an empty UniBook");
            initialData = new UniBook();
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty UniBook");
            initialData = new UniBook();
        }

        return new ModelManager(initialData, userPrefs);
    }

    private void initLogging(Config config) {
        LogsCenter.init(config);
    }

    /**
     * Returns a {@code Config} using the file at {@code configFilePath}. <br>
     * The default file path {@code Config#DEFAULT_CONFIG_FILE} will be used instead
     * if {@code configFilePath} is null.
     */
    protected Config initConfig(Path configFilePath) {
        Config initializedConfig;
        Path configFilePathUsed;

        configFilePathUsed = Config.DEFAULT_CONFIG_FILE;

        if (configFilePath != null) {
            logger.info("Custom Config file specified " + configFilePath);
            configFilePathUsed = configFilePath;
        }

        logger.info("Using config file : " + configFilePathUsed);

        try {
            Optional<Config> configOptional = ConfigUtil.readConfig(configFilePathUsed);
            initializedConfig = configOptional.orElse(new Config());
        } catch (DataConversionException e) {
            logger.warning("Config file at " + configFilePathUsed + " is not in the correct format. "
                + "Using default config properties");
            initializedConfig = new Config();
        }

        //Update config file in case it was missing to begin with or there are new/unused fields
        try {
            ConfigUtil.saveConfig(initializedConfig, configFilePathUsed);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }
        return initializedConfig;
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(UserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using prefs file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataConversionException e) {
            logger.warning("UserPrefs file at " + prefsFilePath + " is not in the correct format. "
                + "Using default user prefs");
            initializedPrefs = new UserPrefs();
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty UniBook");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting UniBook " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping UniBook ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
