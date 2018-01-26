package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;

/**
 * A data class representing the state of the application layout at the moment it is saved.
 */
public final class DashboardData {

  private final double dividerPosition;
  private final DashboardTabPane tabPane;
  private final int selectedTabIndex;

  /**
   * Creates a new dashboard data object.
   *
   * @param dividerPosition  the position of the divider between the dashboard tabs and the sources/gallery pane
   * @param tabPane          the dashboard tab pane
   * @param selectedTabIndex the index of the selected tab, or -1 if no tab (or the adder tab) is selected
   */
  public DashboardData(double dividerPosition, DashboardTabPane tabPane, int selectedTabIndex) {
    this.dividerPosition = dividerPosition;
    this.tabPane = tabPane;
    this.selectedTabIndex = selectedTabIndex;
  }

  public double getDividerPosition() {
    return dividerPosition;
  }

  public DashboardTabPane getTabPane() {
    return tabPane;
  }

  public int getSelectedTabIndex() {
    return selectedTabIndex;
  }
}
