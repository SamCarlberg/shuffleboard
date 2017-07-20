package edu.wpi.first.shuffleboard.dnd;

import edu.wpi.first.shuffleboard.components.TilePane;
import edu.wpi.first.shuffleboard.components.WidgetTile;
import edu.wpi.first.shuffleboard.widget.TileSize;

import java.util.Map;
import java.util.WeakHashMap;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

/**
 * {@link TileDragResizer} can be used to add mouse listeners to a {@link WidgetTile} and make it
 * resizable by the user by clicking and dragging the border in the same way as a window.
 */
public final class TileDragResizer {

  /**
   * Keep track of resizers to avoid creating more than one for the same tile.
   */
  private static final Map<WidgetTile, TileDragResizer> resizers = new WeakHashMap<>();

  /**
   * The margin around the control that a user can click in to start resizing the tile.
   */
  private static final int RESIZE_MARGIN = 10;

  private final TilePane tilePane;
  private final WidgetTile tile;

  private double lastX;
  private double lastY;

  private boolean didDragInit;
  private boolean dragging;
  private ResizeLocation resizeLocation = ResizeLocation.NONE;
  private TileSize startSize;

  private enum ResizeLocation {
    NONE(Cursor.DEFAULT, false, false),
    NORTH(Cursor.N_RESIZE, true, false),
    NORTH_EAST(Cursor.NE_RESIZE, true, true),
    EAST(Cursor.E_RESIZE, false, true),
    SOUTH_EAST(Cursor.SE_RESIZE, true, true),
    SOUTH(Cursor.S_RESIZE, true, false),
    SOUTH_WEST(Cursor.SW_RESIZE, true, true),
    WEST(Cursor.W_RESIZE, false, true),
    NORTH_WEST(Cursor.NW_RESIZE, true, true);

    /**
     * The cursor to use when resizing in this location.
     */
    public final Cursor cursor;
    /**
     * Whether or not this location allows a tile to be resized vertically.
     */
    public final boolean isVertical;
    /**
     * Whether or not this location allows a tile to be resized horizontally.
     */
    public final boolean isHorizontal;

    ResizeLocation(Cursor cursor, boolean isVertical, boolean isHorizontal) {
      this.cursor = cursor;
      this.isVertical = isVertical;
      this.isHorizontal = isHorizontal;
    }
  }

  private TileDragResizer(TilePane tilePane, WidgetTile tile) {
    this.tilePane = tilePane;
    this.tile = tile;
    tile.addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
    tile.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
    tile.addEventHandler(MouseEvent.MOUSE_MOVED, this::mouseOver);
    tile.addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
    tile.addEventHandler(DragEvent.DRAG_DONE, __ -> reset());
  }

  /**
   * Makes the given tile resizable.
   *
   * @param tilePane the pane containing the tile to make resizable
   * @param tile     the tile to make resizable
   */
  public static TileDragResizer makeResizable(TilePane tilePane, WidgetTile tile) {
    return resizers.computeIfAbsent(tile, __ -> new TileDragResizer(tilePane, tile));
  }

  private void reset() {
    didDragInit = false;
    dragging = false;
    resizeLocation = ResizeLocation.NONE;
    lastX = 0;
    lastY = 0;
  }

  private void mouseReleased(MouseEvent event) {
    if (!dragging) {
      return;
    }
    dragging = false;
    final boolean moveLeft = resizeLocation.name().contains("WEST");
    final boolean moveUp = resizeLocation.name().contains("NORTH");
    tile.setCursor(Cursor.DEFAULT);
    GridPane.setValignment(tile, VPos.TOP);
    GridPane.setHalignment(tile, HPos.LEFT);
    resizeLocation = ResizeLocation.NONE;

    // round size to nearest tile size
    final int tileWidth = tilePane.roundWidthToNearestTile(tile.getMinWidth());
    final int tileHeight = tilePane.roundHeightToNearestTile(tile.getMinHeight());

    // limit size to prevent exceeding the bounds of the grid
    final int boundedWidth = Math.min(tilePane.getNumColumns() - GridPane.getColumnIndex(tile),
                                      tileWidth);
    final int boundedHeight = Math.min(tilePane.getNumRows() - GridPane.getRowIndex(tile),
                                       tileHeight);

    tile.setMinWidth(tilePane.tileSizeToWidth(boundedWidth));
    tile.setMinHeight(tilePane.tileSizeToHeight(boundedHeight));
    TileSize newSize = new TileSize(boundedWidth, boundedHeight);
    tile.setSize(newSize);
    if (moveLeft) {
      GridPane.setColumnIndex(tile, GridPane.getColumnIndex(tile) + startSize.getWidth() - newSize.getWidth());
    }
    if (moveUp) {
      GridPane.setRowIndex(tile, GridPane.getRowIndex(tile) + startSize.getHeight() - newSize.getHeight());
    }
    GridPane.setColumnSpan(tile, boundedWidth);
    GridPane.setRowSpan(tile, boundedHeight);
  }

  private void mouseOver(MouseEvent event) {
    if (isInDraggableZone(event) || dragging) {
      tile.setCursor(resizeLocation.cursor);
    } else {
      tile.setCursor(Cursor.DEFAULT);
    }
  }

  /**
   * Gets the most appropriate resize location for a mouse event.
   */
  private ResizeLocation getResizeLocation(MouseEvent event) {
    final double mouseX = event.getX();
    final double mouseY = event.getY();
    final double w = tile.getWidth();
    final double h = tile.getHeight();

    final boolean top = inRange(-RESIZE_MARGIN, RESIZE_MARGIN, mouseY);
    final boolean left = inRange(-RESIZE_MARGIN, RESIZE_MARGIN, mouseX);
    final boolean bottom = inRange(h - RESIZE_MARGIN, h + RESIZE_MARGIN, mouseY);
    final boolean right = inRange(w - RESIZE_MARGIN, w + RESIZE_MARGIN, mouseX);

    if (left) {
      if (top) {
        return ResizeLocation.NORTH_WEST;
      } else if (bottom) {
        return ResizeLocation.SOUTH_WEST;
      } else {
        return ResizeLocation.WEST;
      }
    } else {
      if (right) {
        if (top) {
          return ResizeLocation.NORTH_EAST;
        } else if (bottom) {
          return ResizeLocation.SOUTH_EAST;
        } else {
          return ResizeLocation.EAST;
        }
      } else if (top) {
        return ResizeLocation.NORTH;
      } else if (bottom) {
        return ResizeLocation.SOUTH;
      }
    }
    // not close enough to an edge
    return ResizeLocation.NONE;
  }

  private static boolean inRange(double min, double max, double check) {
    return check >= min && check <= max;
  }

  private boolean isInDraggableZone(MouseEvent event) {
    resizeLocation = getResizeLocation(event);
    return resizeLocation != ResizeLocation.NONE;
  }

  private void mouseDragged(MouseEvent event) {
    if (!dragging) {
      return;
    }

    final double mouseX = event.getX();
    final double mouseY = event.getY();

    final double widthChange;
    final double heightChange;

    if (resizeLocation.name().contains("WEST")) {
      widthChange = lastX - mouseX;
    } else {
      widthChange = mouseX - lastX;
    }

    if (resizeLocation.name().contains("NORTH")) {
      heightChange = lastY - mouseY;
    } else {
      heightChange = mouseY - lastY;
    }

    final double newWidth = tile.getMinWidth() + widthChange;
    final double newHeight = tile.getMinHeight() + heightChange;

    if (resizeLocation.name().contains("NORTH")) {
      GridPane.setValignment(tile, VPos.BOTTOM);
    }
    if (resizeLocation.name().contains("WEST")) {
      GridPane.setHalignment(tile, HPos.RIGHT);
    }

    if (resizeLocation.isHorizontal && newWidth >= tilePane.getTileSize()) {
      tile.setMinWidth(newWidth);
      tile.setMaxWidth(newWidth);
    }
    if (resizeLocation.isVertical && newHeight >= tilePane.getTileSize()) {
      tile.setMinHeight(newHeight);
      tile.setMaxHeight(newHeight);
    }

    lastX = mouseX;
    lastY = mouseY;
  }

  private void mousePressed(MouseEvent event) {
    // ignore clicks outside of the draggable margin
    if (!isInDraggableZone(event)) {
      return;
    }

    dragging = true;

    // make sure that the minimum size is set to the current size once;
    // setting a min size that is smaller than the current size will have no effect
    if (!didDragInit) {
      tile.setMinHeight(tile.getHeight());
      tile.setMinWidth(tile.getWidth());
      startSize = tile.getSize();
      didDragInit = true;
    }

    lastX = event.getX();
    lastY = event.getY();
  }

  public boolean isDragging() {
    return dragging;
  }

}
