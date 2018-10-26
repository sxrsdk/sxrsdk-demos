package com.gearvrf.fasteater;

import com.samsungxr.SXRNode;

/**
 * Created by b1.miller on 7/29/2016.
 */
public class FlyingItem {

    private static int DEFAULT_SPEED = 25;


    public enum ItemStatus {
        HIDDEN, IN_MOTION, ARRIVED_AT_CAMERA
    }

    private String name;
    private String assetFilename;

    private SXRNode object;
    private ItemStatus currentStatus;

    public FlyingItem(String name, SXRNode object) {
        this.name = name;
        this.object = object;
        this.currentStatus = ItemStatus.HIDDEN;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SXRNode getNode() {
        return object;
    }

    public void setNode(SXRNode object) {
        this.object = object;
    }

    public ItemStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(ItemStatus newStatus) {
        currentStatus = newStatus;
    }

    public boolean isHidden() {
        return (currentStatus == ItemStatus.HIDDEN);
    }

    public boolean isInMotion() {
        return (currentStatus == ItemStatus.IN_MOTION);
    }

    public boolean isArrived() {
        return (currentStatus == ItemStatus.ARRIVED_AT_CAMERA);
    }
}
