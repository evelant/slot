package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.GraphView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;

import java.util.function.Consumer;

final class SlotAtlasGraphView extends GraphView {
    private Consumer<AtlasCamera> cameraListener = camera -> {
    };
    private Runnable perFrameTick = () -> {
    };
    private Float pinnedContentScale = null;

    void setPinnedContentScale(Float scale) {
        this.pinnedContentScale = scale;
    }

    float scaleForContent() {
        return pinnedContentScale != null ? pinnedContentScale : getScale();
    }

    void onCameraChanged(Consumer<AtlasCamera> listener) {
        cameraListener = listener == null ? camera -> {
        } : listener;
    }

    void setPerFrameTick(Runnable hook) {
        perFrameTick = hook == null ? () -> {
        } : hook;
    }

    @Override
    public void drawBackgroundTexture(com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext guiContext) {
        perFrameTick.run();
        super.drawBackgroundTexture(guiContext);
    }

    void captureCamera() {
        cameraListener.accept(new AtlasCamera(getOffsetX(), getOffsetY(), getScale()));
    }

    void restoreCamera(AtlasCamera camera) {
        if (camera == null || getContentWidth() <= 0 || getContentHeight() <= 0) {
            return;
        }
        float viewWidth = getContentWidth() / camera.scale();
        float viewHeight = getContentHeight() / camera.scale();
        fit(
                camera.offsetX(),
                camera.offsetY(),
                camera.offsetX() + viewWidth,
                camera.offsetY() + viewHeight,
                camera.scale()
        );
        captureCamera();
    }

    void resetToOverview() {
        fitToChildren(72f, 0.45f);
        captureCamera();
    }

    boolean beginViewportPan(UIEvent event) {
        if (event == null || !getGraphViewStyle().allowPan()) {
            return false;
        }
        if (event.button != 0 && event.button != 2) {
            return false;
        }
        if (!isSelfOrChildHover() || !isMouseOverContent(event.x, event.y)) {
            return false;
        }
        startDrag(new DragOffset(getOffsetX(), getOffsetY()), null);
        return true;
    }

    int worldX(float screenX) {
        return Math.round(getOffsetX() + (screenX - getContentX()) / Math.max(0.0001f, getScale()));
    }

    int worldY(float screenY) {
        return Math.round(getOffsetY() + (screenY - getContentY()) / Math.max(0.0001f, getScale()));
    }

    float screenX(float worldX) {
        return (worldX - getOffsetX()) * getScale() + getContentX();
    }

    float screenY(float worldY) {
        return (worldY - getOffsetY()) * getScale() + getContentY();
    }

    float worldUnitsForPixels(float pixels) {
        return pixels / Math.max(0.0001f, scaleForContent());
    }

    int screenPixelsForWorldUnits(float worldUnits) {
        return Math.round(worldUnits * scaleForContent());
    }

    @Override
    protected void onMouseWheel(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event) {
        // If a child listener already consumed the wheel (e.g. shift-scroll
        // transfer on an atlas card) we must not also zoom. The default
        // GraphView early-returns unless event.target == this; the hack
        // below rewrites target so super sees itself. Skip the rewrite
        // when propagation was stopped so the zoom doesn't fire alongside
        // the child handler's own action.
        if (event.propagationStopped) {
            return;
        }
        UIElement target = event.target;
        if (target != this && isSelfOrChildHover()) {
            event.target = this;
        }
        super.onMouseWheel(event);
        event.target = target;
        captureCamera();
    }

    @Override
    protected void onDragSourceUpdate(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event) {
        super.onDragSourceUpdate(event);
        captureCamera();
    }
}
