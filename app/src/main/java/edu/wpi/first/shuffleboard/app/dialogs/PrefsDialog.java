package edu.wpi.first.shuffleboard.app.dialogs;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.prefs.Category;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.app.StageProvider;
import edu.wpi.first.shuffleboard.app.components.DashboardTab;
import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;
import edu.wpi.first.shuffleboard.app.prefs.SettingsDialog;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.fxmisc.easybind.EasyBind;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Dialog;

/**
 * Dialog for editing application and plugin preferences.
 */
public final class PrefsDialog {

  private static final String DIALOG_TITLE = "Shuffleboard Preferences";

  private final AppPreferences appPreferences;
  private final StageProvider stageProvider;

  private final ObservableValue<List<String>> stylesheets;
  private final PluginLoader pluginLoader;

  @Inject
  public PrefsDialog(AppPreferences appPreferences, StageProvider stageProvider, PluginLoader pluginLoader) {
    this.appPreferences = appPreferences;
    this.stageProvider = stageProvider;
    this.pluginLoader = pluginLoader;
    stylesheets = EasyBind.map(appPreferences.themeProperty(), Theme::getStyleSheets);
  }

  /**
   * Shows the preferences dialog.
   */
  public void show(DashboardTabPane tabPane) {
    Dialog<Boolean> dialog = createDialog(tabPane);
    dialog.showAndWait();
  }

  private SettingsDialog createDialog(DashboardTabPane tabPane) {
    List<Category> pluginCategories = new ArrayList<>();
    for (Plugin plugin : pluginLoader.getLoadedPlugins()) {
      if (plugin.getSettings().isEmpty()) {
        continue;
      }
      Category category = Category.of(plugin.getName(), plugin.getSettings());
      pluginCategories.add(category);
    }
    Category appSettings = appPreferences.getSettings();
    Category plugins = Category.of("Plugins", pluginCategories, ImmutableList.of());
    Category tabs = Category.of("Tabs",
        tabPane.getTabs().stream()
            .flatMap(TypeUtils.castStream(DashboardTab.class))
            .map(DashboardTab::getSettings)
            .collect(Collectors.toList()),
        ImmutableList.of(
            Group.of("Default Settings",
                Setting.of("Default tile size", appPreferences.defaultTileSizeProperty())
            )
        ));

    SettingsDialog dialog = new SettingsDialog(appSettings, plugins, tabs);
    FxUtils.bind(dialog.getDialogPane().getStylesheets(), stylesheets);
    dialog.setTitle(DIALOG_TITLE);
    dialog.initOwner(stageProvider.getPrimaryStage());
    return dialog;
  }

}
