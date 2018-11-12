/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.samsungxr.videoplayer.component.gallery;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTransform;
import com.samsungxr.IViewEvents;
import com.samsungxr.nodes.SXRViewNode;
import com.samsungxr.videoplayer.R;
import com.samsungxr.videoplayer.component.FadeableObject;
import com.samsungxr.videoplayer.component.MessageText;
import com.samsungxr.videoplayer.model.Album;
import com.samsungxr.videoplayer.model.ExternalHomeItem;
import com.samsungxr.videoplayer.model.GalleryItem;
import com.samsungxr.videoplayer.model.HomeItem;
import com.samsungxr.videoplayer.model.LocalHomeItem;
import com.samsungxr.videoplayer.model.Video;
import com.samsungxr.videoplayer.provider.asyntask.AlbumAsyncTask;
import com.samsungxr.videoplayer.provider.asyntask.ExternalVideoAsyncTask;
import com.samsungxr.videoplayer.provider.asyntask.GetDataCallback;
import com.samsungxr.videoplayer.provider.asyntask.LocalVideoAsyncTask;

import java.util.LinkedList;
import java.util.List;

public class Gallery extends FadeableObject implements OnItemsSelectionListener {

    private static final String TAG = Gallery.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private SXRViewNode mObjectViewGallery;
    private List<GalleryItem> mItemList = new LinkedList<>();
    private Breadcrumb mBreadcrumb;
    private OnGalleryEventListener mOnGalleryEventListener;
    private boolean mIsConnected = false;
    private CountdownTimer mCountdownTimer;
    private MessageText mMessageText;

    @SuppressLint("InflateParams")
    public Gallery(SXRContext sxrContext) {
        super(sxrContext);

        mObjectViewGallery = new SXRViewNode(sxrContext, R.layout.gallery_layout,
                new IViewEvents() {
                    @Override
                    public void onInitView(SXRViewNode sxrViewNode, View view) {
                        onInitRecyclerView(view);
                        onInitBreadcrumb(view);
                    }

                    @Override
                    public void onStartRendering(SXRViewNode sxrViewNode, View view) {
                        sxrViewNode.getTransform().setScale(6f, 6, 1);
                        // Set the texture buffer to maximum value to avoid anti-aliasing issue
                        sxrViewNode.setTextureBufferSize(1024);
                        addChildObject(sxrViewNode);
                    }
                });

        onInitMessageText();
    }

    private OnMessageListener mOnMessageListener = new OnMessageListener() {
        @Override
        public void onTimesUp() {
            mMessageText.hide();
        }
    };

    private void onInitMessageText() {
        mMessageText = new MessageText(getSXRContext(), true, "Connect to a network to watch\n the content.", mOnMessageListener);
        mMessageText.getTransform().setScale(0.4f, 0.4f, 1.f);
        mMessageText.getTransform().setPositionZ(1.0f);
        mMessageText.setEnable(false);
        mObjectViewGallery.addChildObject(mMessageText);
        mCountdownTimer = new CountdownTimer(mMessageText);
    }

    public void setIsConnected(boolean connected) {
        mIsConnected = connected;
        Log.d(TAG, "Network state changed: " + connected);
    }

    // UI Thread
    private void onInitRecyclerView(View galleryLayout) {
        mRecyclerView = galleryLayout.findViewById(R.id.recycler_view);
        GalleryItemAdapter adapterGallery = new GalleryItemAdapter<>(mItemList);
        adapterGallery.setOnItemSelectionListener(this);
        mRecyclerView.setAdapter(adapterGallery);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getSXRContext().getContext(), 2));
        loadHome();
    }

    // UI Thread
    private void onInitBreadcrumb(View galleryLayout) {
        mBreadcrumb = new Breadcrumb(galleryLayout.findViewById(R.id.breadcrumb));
        mBreadcrumb.showHome();
        mBreadcrumb.setOnBreadcrumbListener(new OnBreadcrumbListener() {
            @Override
            public void onHomeClicked() {
                loadHome();
            }

            @Override
            public void onSourceClicked(@SourceType int sourceType) {
                if (sourceType == SourceType.LOCAL) {
                    loadLocalAlbums();
                } else if (sourceType == SourceType.EXTERNAL) {
                    loadExternalVideos();
                }
            }
        });
    }

    // UI Thread
    private void loadHome() {
        setGridNumColumns(2);
        mItemList.clear();
        mItemList.addAll(createHomeItems());
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    private void loadLocalAlbums() {
        setGridNumColumns(3);
        new AlbumAsyncTask(new GetDataCallback<List<Album>>() {
            @Override
            public void onResult(List<Album> data) {
                mItemList.clear();
                mItemList.addAll(data);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }).execute();
    }

    private void loadLocalVideos(String albumTitle) {
        setGridNumColumns(3);
        new LocalVideoAsyncTask(albumTitle, new GetDataCallback<List<Video>>() {
            @Override
            public void onResult(List<Video> data) {
                mItemList.clear();
                mItemList.addAll(data);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }).execute();
    }

    private void loadExternalVideos() {
        setGridNumColumns(3);
        new ExternalVideoAsyncTask(new GetDataCallback<List<Video>>() {
            @Override
            public void onResult(List<Video> data) {
                mItemList.clear();
                mItemList.addAll(data);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }).execute();

    }

    @Override
    public void onItemSelected(List<? extends GalleryItem> itemList) {

        GalleryItem item = itemList.get(0);

        switch (item.getType()) {
            case GalleryItem.Type.TYPE_HOME:
                if (item instanceof LocalHomeItem) {
                    mBreadcrumb.showSource(SourceType.LOCAL);
                    loadLocalAlbums();
                } else {
                    mBreadcrumb.showSource(SourceType.EXTERNAL);
                    loadExternalVideos();
                }
                break;
            case GalleryItem.Type.TYPE_ALBUM:
                loadLocalVideos(((Album) item).getTitle());
                mBreadcrumb.showAlbum(((Album) item).getTitle());
                break;
            case GalleryItem.Type.TYPE_VIDEO:
                Video video = (Video) itemList.get(0);
                if (video.getVideoType() == Video.VideoType.EXTERNAL && !mIsConnected) {
                    if (!mCountdownTimer.isRunning()) {
                        mMessageText.show();
                        mCountdownTimer.start();
                    }
                } else if (mOnGalleryEventListener != null) {
                    mOnGalleryEventListener.onVideosSelected((List<Video>) itemList);
                }
                break;
            default:
                Log.d(TAG, "Unknown type: " + item.getType());
        }
    }

    public void setOnGalleryEventListener(OnGalleryEventListener listener) {
        this.mOnGalleryEventListener = listener;
    }

    private List<HomeItem> createHomeItems() {
        List<HomeItem> homeItems = new LinkedList<>();
        homeItems.add(new LocalHomeItem(getSXRContext().getContext()));
        homeItems.add(new ExternalHomeItem(getSXRContext().getContext()));
        return homeItems;
    }

    @NonNull
    @Override
    protected SXRNode getFadeable() {
        return mObjectViewGallery;
    }


    public void reposition(float[] newModelMatrix) {
        SXRTransform ownerTrans = getTransform();

        float scaleX = ownerTrans.getScaleX();
        float scaleY = ownerTrans.getScaleY();
        float scaleZ = ownerTrans.getScaleZ();


        ownerTrans.setModelMatrix(newModelMatrix);
        ownerTrans.setScale(scaleX, scaleY, scaleZ);

        //ownerTrans.setPosition(newModelMatrix[8] * -8, newModelMatrix[9] * -8, newModelMatrix[10] * -8);
        ownerTrans.setPosition(newModelMatrix[8] * -8, newModelMatrix[9] * -8, newModelMatrix[10] * -8);
    }

    private void setGridNumColumns(int numColumns) {
        mItemList.clear();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        ((GridLayoutManager) mRecyclerView.getLayoutManager()).setSpanCount(numColumns);
    }

    public boolean onBackPressed() {

        if (mBreadcrumb.getLevel() == 0) {
            return false;
        }

        if (mBreadcrumb.getLevel() == 2) {
            mBreadcrumb.showSource();
            if (mBreadcrumb.getSourceType() == SourceType.LOCAL) {
                loadLocalAlbums();
            } else {
                loadExternalVideos();
            }
            return true;
        }

        mBreadcrumb.showHome();
        loadHome();
        return true;
    }

    private static class CountdownTimer extends Handler {
        static final int MAX_COUNT = 1;
        int count = MAX_COUNT;
        private boolean mIsRunning = false;
        private MessageText mMessageText;

        CountdownTimer(MessageText mMessageText) {
            this.mMessageText = mMessageText;
        }

        public void start() {
            mIsRunning = true;
            reset();
            tick();
        }

        private void reset() {
            mIsRunning = false;
            removeMessages(0);
            count = MAX_COUNT;
        }

        private void tick() {
            count--;
            sendEmptyMessageDelayed(0, 1000);
        }

        public boolean isRunning() {
            return mIsRunning;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (count < 0) {
                mMessageText.notifyTimesUp();
                reset();
            } else {
                tick();
            }
        }
    }
}