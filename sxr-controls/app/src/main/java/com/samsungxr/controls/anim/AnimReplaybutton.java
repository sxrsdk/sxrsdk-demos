/* Copyright 2015 Samsung Electronicimport com.samsungxr.controls.R;
import com.samsungxr.controls.focus.ControlSceneObject;
import com.samsungxr.controls.shaders.ButtonShader;
import com.samsungxr.controls.util.RenderingOrder;
" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsungxr.controls.anim;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.controls.R;
import com.samsungxr.controls.focus.ControlSceneObject;
import com.samsungxr.controls.shaders.ButtonShader;
import com.samsungxr.controls.shaders.ColorSwapShader;
import com.samsungxr.controls.util.RenderingOrder;

public class AnimReplaybutton extends ControlSceneObject {

    private final int IDLE_STATE = 0;
    private final int HOVER_STATE = 1;
    private final int SELECTED_STATE = 2;

    public AnimReplaybutton(SXRContext gvrContext) {
        super(gvrContext);

        SXRMesh sMesh = getSXRContext().createQuad(0.3f, 0.3f);

        attachRenderData(new SXRRenderData(gvrContext));
        getRenderData().setMaterial(
                new SXRMaterial(gvrContext, new SXRShaderId(ButtonShader.class)));
        getRenderData().setMesh(sMesh);
        createTextures(gvrContext);

        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        getRenderData().setRenderingOrder(RenderingOrder.MOVE_BUTON);

        attachComponent(new SXRMeshCollider(gvrContext, false));

    }

    private void createTextures(SXRContext gvrContext) {

        SXRTexture empty = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.raw.empty));
        SXRTexture idle = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext,
                R.drawable.bt_replay_idle));
        SXRTexture hover = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext,
                R.drawable.bt_replay_hover));
        SXRTexture selected = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext,
                R.drawable.bt_replay_pressed));

        getRenderData().getMaterial().setTexture(ButtonShader.STATE1_BACKGROUND_TEXTURE, empty);
        getRenderData().getMaterial().setTexture(ButtonShader.STATE1_TEXT_TEXTURE, idle);

        getRenderData().getMaterial().setTexture(ButtonShader.STATE2_BACKGROUND_TEXTURE, empty);
        getRenderData().getMaterial().setTexture(ButtonShader.STATE2_TEXT_TEXTURE, hover);

        getRenderData().getMaterial().setTexture(ButtonShader.STATE3_BACKGROUND_TEXTURE, empty);
        getRenderData().getMaterial().setTexture(ButtonShader.STATE3_TEXT_TEXTURE, selected);
    }

    @Override
    protected void gainedFocus() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, HOVER_STATE);
    }

    @Override
    protected void lostFocus() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
    }

    @Override
    protected void singleTap() {
        super.singleTap();
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, SELECTED_STATE);
    }
}
