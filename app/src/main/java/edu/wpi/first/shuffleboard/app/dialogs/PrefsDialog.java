package edu.wpi.first.shuffleboard.app.dialogs;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.theme.Themes;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.app.components.DashboardTab;
import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import com.google.common.collect.ImmutableList;

import edu.wpi.first.desktop.component.SettingsSheet;
import edu.wpi.first.desktop.component.editor.ThemePropertyEditor;
import edu.wpi.first.desktop.settings.Category;
import edu.wpi.first.desktop.settings.Group;
import edu.wpi.first.desktop.settings.Setting;
import edu.wpi.first.desktop.settings.SettingsDialog;
import edu.wpi.first.desktop.theme.Theme;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.control.Dialog;
import javafx.util.Callback;

/**
 * Dialog for editing application and plugin preferences.
 */
public final class PrefsDialog {

  private static final String DIALOG_TITLE = "Shuffleboard Preferences";

  private static final Callback<PropertySheet.Item, PropertyEditor<?>> propertyEditorFactory = item -> {
    if (item.getType() == Theme.class) {
      return new ThemePropertyEditor(Themes.getDefault().getThemeContainer(), item);
    }
    return SettingsSheet.DEFAULT_EDITOR_FACTORY.call(item);
  };

  /**
   * Shows the preferences dialog.
   */
  public void show(DashboardTabPane tabPane) {
    Dialog dialog = createDialog(tabPane);
    dialog.showAndWait();
  }

  private SettingsDialog createDialog(DashboardTabPane tabPane) {
    List<Category> pluginCategories = new ArrayList<>();
    for (Plugin plugin : PluginLoader.getDefault().getLoadedPlugins()) {
      if (plugin.getSettings().isEmpty()) {
        continue;
      }
      Category category = Category.of(plugin.getName(), plugin.getSettings());
      pluginCategories.add(category);
    }
    Category appSettings = AppPreferences.getInstance().getSettings();
    Category plugins = Category.of("Plugins", pluginCategories, ImmutableList.of());
    Category tabs = Category.of("Tabs",
        tabPane.getTabs().stream()
            .flatMap(TypeUtils.castStream(DashboardTab.class))
            .map(DashboardTab::getSettings)
            .collect(Collectors.toList()),
        ImmutableList.of(
            Group.of("Default Settings",
                Setting.of("Default tile size", AppPreferences.getInstance().defaultTileSizeProperty())
            )
        ));

    SettingsDialog dialog = new SettingsDialog();
    dialog.setRootCategories(List.of(appSettings, plugins, tabs));
    dialog.setPropertyEditorFactory(propertyEditorFactory);
    Themes.getDefault().getThemeManager().addScene(dialog.getDialogPane().getScene());
    dialog.setTitle(DIALOG_TITLE);
    return dialog;
  }

}
