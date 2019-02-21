package com.samsungxr.arpet.service.share;

import android.graphics.Bitmap;
import android.opengl.Matrix;
import android.util.Log;

import com.samsungxr.SXREventReceiver;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.mixedreality.IMixedReality;
import com.samsungxr.mixedreality.SXRAnchor;
import com.samsungxr.mixedreality.SXRHitResult;
import com.samsungxr.mixedreality.SXRLightEstimate;
import com.samsungxr.mixedreality.SXRMarker;
import com.samsungxr.mixedreality.SXRMixedReality;
import com.samsungxr.mixedreality.SXRPlane;

import com.samsungxr.arpet.PetContext;
import com.samsungxr.arpet.constant.ArPetObjectType;
import com.samsungxr.arpet.constant.PetConstants;
import com.samsungxr.arpet.service.IMessageService;
import com.samsungxr.arpet.service.MessageService;
import com.samsungxr.arpet.service.event.UpdatePosesReceivedMessage;
import com.samsungxr.arpet.util.EventBusUtils;
import com.samsungxr.mixedreality.SXRPointCloud;

import org.greenrobot.eventbus.Subscribe;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SharedMixedReality implements IMixedReality {

    private static final String TAG = SharedMixedReality.class.getSimpleName();

    public static final int OFF = 0;
    public static final int HOST = 1;
    public static final int GUEST = 2;

    private final IMixedReality mMixedReality;
    private final PetContext mPetContext;
    private final List<SharedSceneObject> mSharedSceneObjects;
    private final IMessageService mMessageService;
    private SXREventReceiver mListeners;

    @PetConstants.ShareMode
    private int mMode = PetConstants.SHARE_MODE_NONE;
    private SXRAnchor mSharedAnchor = null;
    private SXRNode mSharedAnchorObject = null;
    private float[] mSpaceMatrix = new float[16];

    public SharedMixedReality(PetContext petContext) {
        mMixedReality = new SXRMixedReality(petContext.getMainScene(), true);
        mPetContext = petContext;
        mSharedSceneObjects = new ArrayList<>();
        mMessageService = MessageService.getInstance();
        Matrix.setIdentityM(mSpaceMatrix, 0);
        mSharedAnchorObject = new SXRNode(petContext.getSXRContext());
    }

    @Override
    public float getARToVRScale() { return mMixedReality.getARToVRScale(); }

    @Override
    public void resume() {
        mMixedReality.resume();
    }

    @Override
    public void pause() {
        mMixedReality.pause();
    }

    public SXREventReceiver getEventReceiver() { return mMixedReality.getEventReceiver(); }

    /**
     * Starts the sharing mode
     *
     * @param mode {@link PetConstants#SHARE_MODE_HOST} or {@link PetConstants#SHARE_MODE_GUEST}
     */
    public void startSharing(SXRAnchor sharedAnchor, @PetConstants.ShareMode int mode) {
        Log.d(TAG, "startSharing => " + mode);

        if (mMode != PetConstants.SHARE_MODE_NONE) {
            return;
        }

        EventBusUtils.register(this);

        mSharedAnchor = sharedAnchor;
        mSharedAnchorObject.attachComponent(mSharedAnchor);

        mMode = mode;

        if (mode == PetConstants.SHARE_MODE_HOST) {
            mPetContext.runOnPetThread(mSharingLoop);
        } else {
            startGuest();
        }
    }

    public void stopSharing() {
        EventBusUtils.unregister(this);
        mSharedAnchorObject.detachComponent(SXRAnchor.getComponentType());
        if (mMode == PetConstants.SHARE_MODE_GUEST) {
            stopGuest();
        }
        mMode = PetConstants.SHARE_MODE_NONE;
    }

    public SXRAnchor getSharedAnchor() {
        return mSharedAnchor;
    }

    private synchronized void startGuest() {
        for (SharedSceneObject shared : mSharedSceneObjects) {
            initAsGuest(shared);
        }
    }

    private synchronized void initAsGuest(SharedSceneObject shared) {
        shared.parent = shared.object.getParent();
        if (shared.parent != null) {
            shared.parent.removeChildObject(shared.object);
            mPetContext.getMainScene().addNode(shared.object);
        }
    }

    private synchronized void stopGuest() {
        Iterator<SharedSceneObject> iterator = mSharedSceneObjects.iterator();
        SharedSceneObject shared;
        while (iterator.hasNext()) {
            shared = iterator.next();
            if (shared.parent != null) {
                shared.object.getTransform().setModelMatrix(shared.localMtx);
                mPetContext.getMainScene().removeNode(shared.object);
                shared.parent.addChildObject(shared.object);
            }
        }
        mPetContext.getPlaneHandler().resetPlanes();
    }

    public synchronized void registerSharedObject(SXRNode object, @ArPetObjectType String type,
                                                  boolean repeat) {
        for (SharedSceneObject shared : mSharedSceneObjects) {
            if (shared.object == object) {
                shared.repeat = repeat;
                return;
            }
        }

        SharedSceneObject newShared = new SharedSceneObject(type, object);
        newShared.repeat = repeat;
        if (mMode == PetConstants.SHARE_MODE_GUEST) {
            initAsGuest(newShared);
        }
        mSharedSceneObjects.add(newShared);
    }

    public synchronized void unregisterSharedObject(SXRNode object) {
        Iterator<SharedSceneObject> iterator = mSharedSceneObjects.iterator();
        SharedSceneObject shared;
        while (iterator.hasNext()) {
            shared = iterator.next();
            if (shared.object == object) {
                iterator.remove();
            }
        }
    }
    @Override
    public float getScreenDepth() { return mMixedReality.getScreenDepth(); }

    @Override
    public SXRNode getPassThroughObject() {
        return mMixedReality.getPassThroughObject();
    }

    @Override
    public ArrayList<SXRPlane> getAllPlanes() {
        return mMixedReality.getAllPlanes();
    }

    @Override
    public SXRAnchor createAnchor(float[] pose) {
        return mMixedReality.createAnchor(pose);
    }

    @Override
    public SXRNode createAnchorNode(float[] pose) {
        return mMixedReality.createAnchorNode(pose);
    }

    @Override
    public void hostAnchor(SXRAnchor sxrAnchor, CloudAnchorCallback cloudAnchorCallback) {
        mMixedReality.hostAnchor(sxrAnchor, cloudAnchorCallback);
    }

    @Override
    public void updateAnchorPose(SXRAnchor sxrAnchor, float[] pose) {
        mMixedReality.updateAnchorPose(sxrAnchor, pose);
    }

    @Override
    public void removeAnchor(SXRAnchor sxrAnchor) {
        mMixedReality.removeAnchor(sxrAnchor);
    }

    @Override
    public void resolveCloudAnchor(String anchorId, IMixedReality.CloudAnchorCallback cb) {
        mMixedReality.resolveCloudAnchor(anchorId, cb);
    }

    @Override
    public void setEnableCloudAnchor(boolean enableCloudAnchor) {
        mMixedReality.setEnableCloudAnchor(enableCloudAnchor);
    }

    @Override
    public SXRHitResult hitTest(SXRPicker.SXRPickedObject sxrPickedObject) {
        return null;
    }

    @Override
    public SXRHitResult hitTest(float x, float y) {
        return mMixedReality.hitTest(x, y);
    }

    @Override
    public SXRLightEstimate getLightEstimate() {
        return mMixedReality.getLightEstimate();
    }

    @Override
    public void setMarker(Bitmap bitmap) {
        mMixedReality.setMarker(bitmap);
    }

    @Override
    public void setMarkers(ArrayList<Bitmap> arrayList) {
        mMixedReality.setMarkers(arrayList);
    }

    @Override
    public ArrayList<SXRMarker> getAllMarkers() {
        return mMixedReality.getAllMarkers();
    }

    @Override
    public float[] makeInterpolated(float[] poseA, float[] poseB, float t) {
        return mMixedReality.makeInterpolated(poseA, poseB, t);
    }

    @Override
    public SXRPointCloud acquirePointCloud() {
        return mMixedReality.acquirePointCloud();
    }

    @Override
    public void setPlaneFindingMode(SXRMixedReality.PlaneFindingMode planeFindingMode) {
        mMixedReality.setPlaneFindingMode(planeFindingMode);
    }

    @PetConstants.ShareMode
    public int getMode() {
        return mMode;
    }

    private synchronized void sendSharedSceneObjects() {
        Matrix.invertM(mSpaceMatrix, 0,
                mSharedAnchor.getTransform().getModelMatrix(), 0);

        List<SharedObjectPose> poses = new ArrayList<>();

        for (SharedSceneObject shared : mSharedSceneObjects) {
            float[] result = new float[16];
            Matrix.multiplyMM(result, 0, mSpaceMatrix, 0,
                    shared.object.getTransform().getModelMatrix(), 0);
            poses.add(new SharedObjectPose(shared.type, result));
        }

        mMessageService.updatePoses(poses.toArray(new SharedObjectPose[0]));
    }

    private synchronized void onUpdatePosesReceived(SharedObjectPose[] poses) {
        mSpaceMatrix = mSharedAnchor.getTransform().getModelMatrix();

        for (SharedObjectPose pose : poses) {
            for (SharedSceneObject shared : mSharedSceneObjects) {
                if (shared.type.equals(pose.getObjectType())) {
                    float[] result = new float[16];
                    Matrix.multiplyMM(result, 0, mSpaceMatrix, 0, pose.getModelMatrix(), 0);
                    shared.object.getTransform().setModelMatrix(result);

                    if (!shared.repeat) {
                        mSharedSceneObjects.remove(shared);
                    }
                    break;
                }
            }
        }
    }

    private Runnable mSharingLoop = new Runnable() {

        final int LOOP_TIME = 500;

        @Override
        public void run() {
            if (mMode != PetConstants.SHARE_MODE_NONE) {
                sendSharedSceneObjects();
                mPetContext.runDelayedOnPetThread(this, LOOP_TIME);
            }
        }
    };

    private static class SharedSceneObject {

        @ArPetObjectType
        String type;

        // Shared object
        SXRNode object;
        // Parent of shared object.
        SXRNode parent;
        // Local matrix to be used in guest mode after the share experience has been finished
        Matrix4f localMtx;

        boolean repeat;

        SharedSceneObject(String type, SXRNode object) {
            this.type = type;
            this.object = object;
            this.repeat = true;
            this.localMtx = object.getTransform().getLocalModelMatrix4f();
        }

        @Override
        public String toString() {
            return "SharedSceneObject{" +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    @Subscribe
    public void handleReceivedMessage(UpdatePosesReceivedMessage message) {
        onUpdatePosesReceived(message.getSharedObjectPoses());
    }
}
