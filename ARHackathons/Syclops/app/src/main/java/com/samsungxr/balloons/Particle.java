package com.samsungxr.balloons;

import com.samsungxr.SXRBehavior;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.utility.Log;
import org.joml.Matrix4f;
import org.joml.Vector3f;

class Particle extends SXRBehavior
{
    static private long TYPE_PARTICLE = newComponentType(com.samsungxr.balloons.Particle.class);
    public float            Velocity;
    public Vector3f         Direction;
    public float            Distance;
    private Vector3f        mStartPos = new Vector3f(0, 0, 0);
    private Vector3f        mCurPos = new Vector3f(0, 0, 0);
    
    Particle(SXRContext ctx, float velocity, Vector3f direction)
    {
        super(ctx);
        Velocity = velocity;
        Direction = direction;
        mType = TYPE_PARTICLE;
    }

    static public long getComponentType() { return TYPE_PARTICLE; }

    public void setPosition(Vector3f pos)
    {
        SXRNode owner = getOwnerObject();
        Distance = 0;
        mCurPos.x = mStartPos.x = pos.x;
        mCurPos.y = mStartPos.y = pos.y;
        mCurPos.z = mStartPos.z = pos.z;
        if (owner != null)
        {
            owner.getTransform().setPosition(mCurPos.x, mCurPos.y, mCurPos.z);
        }
    }

    public Vector3f getPosition()
    {
        Matrix4f localmtx = getOwnerObject().getTransform().getLocalModelMatrix4f();
        localmtx.getTranslation(mCurPos);
        return mCurPos;
    }
    
    public void onAttach(SXRNode owner)
    {
        super.onAttach(owner);
        getPosition();
        mStartPos.x = mCurPos.x;
        mStartPos.y = mCurPos.y;
        mStartPos.z = mCurPos.z;
    }
    
    public void move(float time)
    {
        SXRNode owner = getOwnerObject();
        
        if (owner == null)
        {
            return;
        }
        getPosition();
        mCurPos.x += Direction.x * Velocity * time;
        mCurPos.y += Direction.y * Velocity * time;
        mCurPos.z += Direction.z * Velocity * time;
        owner.getTransform().setPosition(mCurPos.x, mCurPos.y, mCurPos.z);;
        Distance = mCurPos.sub(mStartPos, mCurPos).length();
    }
}
