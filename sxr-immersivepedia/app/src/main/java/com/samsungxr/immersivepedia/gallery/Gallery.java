/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsungxr.immersivepedia.gallery;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRCollider;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRRenderPass.SXRCullFaceEnum;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.animation.SXRPositionAnimation;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.focus.FocusListener;
import com.samsungxr.immersivepedia.focus.FocusableSceneObject;
import com.samsungxr.immersivepedia.input.TouchPadInput;
import com.samsungxr.immersivepedia.props.Totem;
import com.samsungxr.immersivepedia.props.TotemEventListener;
import com.samsungxr.immersivepedia.util.AudioClip;
import com.samsungxr.immersivepedia.util.RenderingOrderApplication;
import com.samsungxr.io.SXRTouchPadGestureListener.Action;

import java.util.ArrayList;

public class Gallery extends FocusableSceneObject implements PhotoEventListener, TotemEventListener {

    private static final int COLS = 3;

    private static final int GALLERY_CLOSED = 0x01;
    private static final int GALLERY_OPENED = 0x02;

    private static float GALLERY_WIDTH = 12.0f;
    private static float GALLERY_HEIGHT = 4.2f;

    public static final float GALLERY_POSITION_X = 0f;
    public static final float GALLERY_POSITION_Y = 3.0f;
    public static final float GALLERY_POSITION_Z = 6.0f;

    private static final float GALLERY_HIGHLIGHT_PHOTO_POSITION_X = 0f;
    private static final float GALLERY_HIGHLIGHT_PHOTO_POSITION_Y = -1.0f;
    private static final float GALLERY_HIGHLIGHT_PHOTO_POSITION_Z = 2.5f;

    private static final float GALLERY_ARROW_WIDTH = 0.3f;
    private static final float GALLERY_ARROW_HEIGHT = 0.3f;

    private static final float GALLERY_SCROLLBAR_AREA_HEIGHT = Gallery.GALLERY_HEIGHT * 0.64f;
    private static final float GALLERY_SCROLLBAR_X_POSITION = 4.5f;
    private static final float GALLERY_SCROLLBAR_Y_OFFSET_POSITION = -0.86f;
    private static final float GALLERY_SCROLLBAR_Y_INITIAL_POSITION = GALLERY_SCROLLBAR_Y_OFFSET_POSITION
            - (GALLERY_SCROLLBAR_AREA_HEIGHT / 2.0f);
    private static final float GALLERY_SCROLLBAR_WIDTH = Gallery.GALLERY_WIDTH * 0.025f;

    private static final float GALLERY_SCROLLBAR_ANIMATION_TIME = 0.6f;

    private static final float GALLERY_SPACE_BETWEEN_HIGHLIGHT_POSITIONS = 1.0f;

    private static final float GALLERY_ARROWS_POSITION_X = 1.5f;
    private static final float GALLERY_ARROWS_POSITION_Y = GALLERY_HIGHLIGHT_PHOTO_POSITION_Y - 1.8f;
    private static final float GALLERY_ARROWS_POSITION_Z = GALLERY_HIGHLIGHT_PHOTO_POSITION_Z + 0.01f;

    private SXRSceneObject highlightPhotoView;
    private SXRSceneObject highlightLeftPhotoView;
    private SXRSceneObject highlightRightPhotoView;

    private int currentState = GALLERY_CLOSED;

    protected int currentPage = 3;

    private SXRSceneObject scrollbar = null;

    protected boolean locked = false;

    protected static final float PHOTO_VIEW_DISTANCE_BETWEEN_PHOTOS = 1.0f + (PhotoView.HEIGHT / 2.0f);

    protected static final float PHOTO_VIEW_HORIZONTAL_SPACING = 1.5f;

    private ArrayList<PhotoView> photos = new ArrayList<PhotoView>();
    private ArrayList<PhotoGridItem> gridItems = new ArrayList<PhotoGridItem>();

    private SXRContext sxrContext;
    private SXRCollider galleryCollider;
    private int[] photoIds;

    private SXRSceneObject leftArrow;
    private SXRSceneObject rightArrow;

    public Gallery(SXRContext sxrContext, int[] resources) {
        this(sxrContext);
        this.insertPhotos(resources);

        this.createHighlightGridItem();
        this.createAllGridItems();
        this.createInitialPhotos();
        this.startScrollInPage(this.currentPage);
        this.createScrollbar();
        this.createArrows();
        this.setInitialGalleryState();

    }

    private void createScrollbar() {

        float height = GALLERY_SCROLLBAR_AREA_HEIGHT / this.getNumberOfPages();

        scrollbar = new SXRSceneObject(sxrContext, Gallery.GALLERY_SCROLLBAR_WIDTH,
                height,
                this.sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(this.sxrContext,
                        R.drawable.scrollbar)));
        scrollbar.getTransform().setPositionX(GALLERY_SCROLLBAR_X_POSITION);
        scrollbar.getTransform().setPositionY(GALLERY_SCROLLBAR_Y_INITIAL_POSITION);
        scrollbar.getRenderData().setRenderingOrder(RenderingOrderApplication.GALLERY_SCROLLBAR);

        this.addChildObject(scrollbar);
        this.updateScrollbar(false, this.currentPage);
    }

    private void updateScrollbar(boolean animated, int scrollIndex) {

        float scrollFactor = GALLERY_SCROLLBAR_AREA_HEIGHT / (this.getNumberOfPages() - 2);

        float newYPosition = GALLERY_SCROLLBAR_Y_INITIAL_POSITION
                + ((scrollIndex - 1) * scrollFactor);

        if (animated) {
            new SXRPositionAnimation(this.scrollbar, GALLERY_SCROLLBAR_ANIMATION_TIME,
                    this.scrollbar.getTransform().getPositionX(), newYPosition, this.scrollbar
                            .getTransform().getPositionZ()).start(this.sxrContext
                    .getAnimationEngine());

        } else {
            this.scrollbar.getTransform().setPositionY(newYPosition);
        }
    }

    public void closeThis() {
        AudioClip.getInstance(getSXRContext().getContext()).playSound(AudioClip.getUITextDisappearSoundID(), 1.0f, 1.0f);
        closeAction();

        for (PhotoView photoView : photos) {
            if (photoView.currentState == PhotoView.PHOTO_VIEW_OPENED) {
                photoView.closeAction();
            }
        }
    }

    private void setInitialGalleryState() {
        closeAction();
    }

    public void openAction() {
        galleryCollider.setEnable(false);
        currentState = GALLERY_OPENED;
        this.getTransform().setScale(1f, 1f, 1f);
        for (PhotoView view : this.photos) {
            view.appear();
        }
    }

    public void closeAction() {
        this.getTransform().setScale(0f, 0f, 0f);
        galleryCollider.setEnable(true);
        currentState = GALLERY_CLOSED;
        this.getRenderData().setCullFace(SXRCullFaceEnum.None);
        this.showInteractiveCursor = false;
        this.getRenderData().getMaterial().setOpacity(0f);
        for (PhotoView view : this.photos) {
            view.disappear();
        }
    }

    public Gallery(SXRContext sxrContext) {
        super(sxrContext, GALLERY_WIDTH, GALLERY_HEIGHT, sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.empty_clickable)));
        this.sxrContext = sxrContext;
        this.getRenderData().setRenderingOrder(RenderingOrderApplication.GALLERY);
        this.setName("gallery");
        galleryCollider = new SXRMeshCollider(getSXRContext(), true);
        this.attachCollider(galleryCollider);

        this.focusListener = new FocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {
            }

            @Override
            public void inFocus(FocusableSceneObject object) {
                Gallery.this.process();
            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {
            }
        };
    }

    private void createAllGridItems() {
        int cell = 0;

        for (int r = 0; r < this.getNumberOfPages(); r++) {
            for (int c = 0; c < Gallery.COLS; c++) {

                PhotoGridItem item = new PhotoGridItem();

                item.z = 0f;
                item.x = 0f;

                if (cell == (Gallery.COLS - 1))
                    item.x = ((PhotoView.WIDTH / 2.0f) + Gallery.PHOTO_VIEW_HORIZONTAL_SPACING)
                            * -1.0f;
                if (cell == 0)
                    item.x = ((PhotoView.WIDTH / 2.0f) + Gallery.PHOTO_VIEW_HORIZONTAL_SPACING);

                cell++;
                if (cell > (Gallery.COLS - 1))
                    cell = 0;

                item.y = r * PHOTO_VIEW_DISTANCE_BETWEEN_PHOTOS;
                gridItems.add(item);
            }
        }
    }

    private void createHighlightGridItem() {
        this.highlightPhotoView = new SXRSceneObject(this.sxrContext);
        this.highlightLeftPhotoView = new SXRSceneObject(this.sxrContext);
        this.highlightRightPhotoView = new SXRSceneObject(this.sxrContext);

        this.highlightPhotoView.getTransform().setPosition(GALLERY_HIGHLIGHT_PHOTO_POSITION_X,
                GALLERY_HIGHLIGHT_PHOTO_POSITION_Y, GALLERY_HIGHLIGHT_PHOTO_POSITION_Z);

        this.highlightLeftPhotoView.getTransform().setPosition(
                this.highlightPhotoView.getTransform().getPositionX()
                        + GALLERY_SPACE_BETWEEN_HIGHLIGHT_POSITIONS,
                this.highlightPhotoView.getTransform().getPositionY(),
                this.highlightPhotoView.getTransform().getPositionZ());

        this.highlightRightPhotoView.getTransform().setPosition(
                this.highlightPhotoView.getTransform().getPositionX()
                        - GALLERY_SPACE_BETWEEN_HIGHLIGHT_POSITIONS,
                this.highlightPhotoView.getTransform().getPositionY(),
                this.highlightPhotoView.getTransform().getPositionZ());

        this.addChildObject(this.highlightPhotoView);
        this.addChildObject(this.highlightLeftPhotoView);
        this.addChildObject(this.highlightRightPhotoView);
    }

    private void createInitialPhotos() {
        int index = 0;

        PhotoGridItem centered = new PhotoGridItem();
        PhotoGridItem left = new PhotoGridItem();
        PhotoGridItem right = new PhotoGridItem();

        for (PhotoGridItem item : this.gridItems) {

            PhotoView photo = PhotoView.createPhotoView(this.sxrContext, this.photoIds[index]);
            this.addChildObject(photo);
            this.photos.add(photo);
            photo.getTransform().setPosition(item.x, item.y, item.z);
            photo.getRenderData().setCullFace(SXRCullFaceEnum.None);
            photo.gridItem = item;
            photo.centeredGridItem = centered;
            photo.leftGridItem = left;
            photo.rightGridItem = right;
            photo.getRenderData().setRenderingOrder(RenderingOrderApplication.GALLERY);

            photo.centeredGridItem.x = this.highlightPhotoView.getTransform().getPositionX();
            photo.centeredGridItem.y = this.highlightPhotoView.getTransform().getPositionY();
            photo.centeredGridItem.z = this.highlightPhotoView.getTransform().getPositionZ();

            photo.leftGridItem.x = this.highlightLeftPhotoView.getTransform().getPositionX();
            photo.leftGridItem.y = this.highlightLeftPhotoView.getTransform().getPositionY();
            photo.leftGridItem.z = this.highlightLeftPhotoView.getTransform().getPositionZ();

            photo.rightGridItem.x = this.highlightRightPhotoView.getTransform().getPositionX();
            photo.rightGridItem.y = this.highlightRightPhotoView.getTransform().getPositionY();
            photo.rightGridItem.z = this.highlightRightPhotoView.getTransform().getPositionZ();

            photo.PhotoItemListener = this;
            item.index = index;
            photo.applyOpacityConstrants();

            photo.gridItem.page++;
            index++;
        }
    }

    private void lockAllPhotoViewExcept(PhotoView view) {
        for (PhotoView photo : this.photos) {
            if (photo.gridItem.index != view.gridItem.index)
                photo.currentState = PhotoView.PHOTO_VIEW_LOCK;
        }
    }

    private void unlockAllPhotoViewExcept(PhotoView view) {
        for (PhotoView photo : this.photos) {
            if (photo.gridItem.index != view.gridItem.index)
                photo.currentState = PhotoView.PHOTO_VIEW_CLOSED;
        }
    }

    public void insertPhotos(int[] identifiers) {

        int index = 0;
        photoIds = new int[identifiers.length];
        for (int i : identifiers) {
            photoIds[index] = i;
            index++;
        }
    }

    @Override
    public void itemSelected(PhotoView view) {
        lockAllPhotoViewExcept(view);
    }

    @Override
    public void itemUnselected(PhotoView view) {
    }

    @Override
    public void itemSelectedAnimationFinished(PhotoView view) {
    }

    @Override
    public void itemUnselectedAnimationFinished(PhotoView view) {
        unlockAllPhotoViewExcept(view);
    }

    private void startScrollInPage(int page) {

        currentPage = page;

        for (PhotoGridItem item : this.gridItems) {
            item.originalX = item.x;
            item.originalY = item.y;
            item.originalZ = item.z;
        }

        for (PhotoView view : this.photos) {
            view.applyScrollPage(currentPage);
            view.applyOpacityConstrants();
        }
    }

    public void process() {

        if (TouchPadInput.getCurrent().swipeDirection == Action.SwipeUp) {
            if (this.checkIfThereIsAnyPhotoWIthStatus(PhotoView.PHOTO_VIEW_OPENED) == false)
                scrollUp(true);
        }
        if (TouchPadInput.getCurrent().swipeDirection == Action.SwipeDown) {
            if (this.checkIfThereIsAnyPhotoWIthStatus(PhotoView.PHOTO_VIEW_OPENED) == false)
                scrollDown(true);
        }
    }

    private void scrollUp(boolean animated) {

        if (currentPage <= 1 || currentState == Gallery.GALLERY_CLOSED)
            return;

        currentPage = currentPage - 2;
        updateScrollbar(true, this.currentPage);

        for (PhotoView view : this.photos) {
            view.slideUp(animated);
        }
    }

    private void scrollDown(boolean animated) {

        if (currentPage >= (this.getNumberOfPages() - 1) || currentState == Gallery.GALLERY_CLOSED)
            return;

        currentPage = currentPage + 2;
        updateScrollbar(true, this.currentPage);

        for (PhotoView view : this.photos) {
            view.slideDown(animated);
        }
    }

    @Override
    public void onFinishLoadingTotem(Totem totem) {
        AudioClip.getInstance(getSXRContext().getContext()).playSound(AudioClip.getUITextAppearSoundID(), 1.0f, 1.0f);
        openAction();
    }

    @Override
    public boolean shouldTotemAppear(Totem totem) {
        if (this.currentState == GALLERY_CLOSED)
            return true;
        return false;
    }

    @Override
    public boolean shouldOpenItem(PhotoView view) {
        return (this.currentState == Gallery.GALLERY_OPENED);
    }

    @Override
    public boolean shouldHoverItem(PhotoView view) {
        return (this.currentState == Gallery.GALLERY_OPENED);
    }

    @Override
    public boolean shouldCloseItem(PhotoView view) {
        return this.currentState == Gallery.GALLERY_OPENED;
    }

    @Override
    public void itemSwipedToRight(PhotoView view) {
        int index = view.gridItem.index - 1;
        boolean wrapAround = false;
        if (index < 0) {
            index = this.photos.size() - 1;
            wrapAround = true;
        }

        PhotoView nextPhoto = photos.get(index);
        if (nextPhoto.getRenderData().getMaterial().getOpacity() < 0.9f) {
            currentPage--;
            if (wrapAround)
                currentPage = this.getNumberOfPages() - 1;
        }

        syncGrid();
        nextPhoto.openActionWithAnimation(PhotoView.PHOTO_VIEW_ANIMATION_LEFT_TO_RIGHT);
    }

    @Override
    public void itemSwipedToLeft(PhotoView view) {
        int index = view.gridItem.index + 1;
        boolean wrapAround = false;
        if (index > (this.photos.size() - 1)) {
            index = 0;
            wrapAround = true;
        }

        PhotoView nextPhoto = photos.get(index);
        if (nextPhoto.getRenderData().getMaterial().getOpacity() < 0.9f) {
            currentPage++;
            if (wrapAround)
                currentPage = 1;
        }

        syncGrid();
        nextPhoto.openActionWithAnimation(PhotoView.PHOTO_VIEW_ANIMATION_RIGHT_TO_LEFT);
    }

    public void syncGrid() {

        resetAllGridPositon();
        for (PhotoView view : this.photos) {

            view.applyScrollPage(currentPage);
            view.applyOpacityConstrants();
        }
        this.updateScrollbar(false, this.currentPage);
    }

    private void resetAllGridPositon() {
        for (PhotoGridItem item : this.gridItems) {
            item.x = item.originalX;
            item.y = item.originalY;
            item.z = item.originalZ;
        }
    }

    @Override
    public void dispatchLockRequest() {
        locked = true;
    }

    @Override
    public void dispatchUnlockRequest() {
        locked = false;

    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    private boolean checkIfThereIsAnyPhotoWIthStatus(int status) {
        for (PhotoView v : this.photos) {
            if (v.currentState == status)
                return true;
        }
        return false;
    }

    private int getNumberOfPages() {
        return this.photoIds.length / Gallery.COLS;
    }

    private void createArrows() {
        this.leftArrow = new SXRSceneObject(this.sxrContext, GALLERY_ARROW_WIDTH,
                GALLERY_ARROW_HEIGHT, this.sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(
                        this.sxrContext, R.drawable.arrowleft)));

        this.rightArrow = new SXRSceneObject(this.sxrContext, GALLERY_ARROW_WIDTH,
                GALLERY_ARROW_HEIGHT, this.sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(
                        this.sxrContext, R.drawable.arrowright)));

        this.addChildObject(this.leftArrow);
        this.addChildObject(this.rightArrow);

        this.leftArrow.getTransform().setPosition(-GALLERY_ARROWS_POSITION_X,
                GALLERY_ARROWS_POSITION_Y, GALLERY_ARROWS_POSITION_Z);
        this.rightArrow.getTransform().setPosition(GALLERY_ARROWS_POSITION_X,
                GALLERY_ARROWS_POSITION_Y, GALLERY_ARROWS_POSITION_Z);

    }

    public boolean isOpen() {
        if (currentState == GALLERY_OPENED) {
            return true;
        } else {
            return false;
        }
    }
}
