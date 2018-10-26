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

package com.samsungxr.sample.remote_scripting;

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.samsungxr.SXRContext;
import com.samsungxr.IViewEvents;
import com.samsungxr.debug.cli.LineProcessor;
import com.samsungxr.nodes.SXRViewNode;
import com.samsungxr.script.SXRScriptManager;

import java.io.StringWriter;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class EditorUtils {
    private final SXRContext sxrContext;
    private final SXRViewNode layoutNode;
    private GearVRScripting activity;

    private static final float QUAD_X = 2.0f;
    private static final float QUAD_Y = 1.0f;
    private final ScriptHandler mScriptHandler;

    private TextView updateButton;


    public EditorUtils(SXRContext context) {
        sxrContext = context;
        activity = (GearVRScripting) context.getActivity();

        layoutNode = new SXRViewNode(sxrContext, R.layout.main, viewEventsHandler,
                sxrContext.createQuad(QUAD_X, QUAD_Y));

        layoutNode.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        layoutNode.setName("editor");

        mScriptHandler = new ScriptHandler(sxrContext);
    }

    public void show() {
        sxrContext.getMainScene().addNode(layoutNode);
    }

    public void setPosition(float x, float y, float z) {
        layoutNode.getTransform().setPosition(x, y, z);
    }

    public void setRotationByAxis(float angle, float x, float y, float z) {
        layoutNode.getTransform().setRotationByAxis(angle, x, y, z);
    }

    public void hide() {
        sxrContext.getMainScene().removeNode(layoutNode);
    }


    private IViewEvents viewEventsHandler = new IViewEvents() {
        @Override
        public void onInitView(SXRViewNode sxrViewNode, View view) {
            final EditText editor = (EditText) view.findViewById(R.id.editor);
            editor.requestFocus();
            editor.setDrawingCacheEnabled(false);
            editor.setBackgroundColor(Color.BLACK);

            updateButton = (TextView) view.findViewById(R.id.update);
            updateButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    android.util.Log.d("Editor", "update was clicked");
                    // get text
                    String script = editor.getText().toString();
                    // execute script
                    mScriptHandler.processLine(script);
                }
            });
        }

        @Override
        public void onStartRendering(SXRViewNode sxrViewNode, View view) {

        }
    };

    class ScriptHandler implements LineProcessor {
        protected String prompt;
        protected ScriptEngine mScriptEngine;
        protected ScriptContext mScriptContext;
        protected StringWriter mWriter;

        public ScriptHandler(SXRContext sxrContext) {
            prompt = "";
            mScriptEngine = sxrContext.getScriptManager().getEngine(SXRScriptManager.LANG_JAVASCRIPT);
            mScriptContext = mScriptEngine.getContext();

            mWriter = new StringWriter();
            mScriptContext.setWriter(mWriter);
            mScriptContext.setErrorWriter(mWriter);
        }

        @Override
        public String processLine(String line) {
            try {
                mWriter.getBuffer().setLength(0);
                mScriptEngine.eval(line, mScriptContext);
                mWriter.flush();
                if (mWriter.getBuffer().length() != 0)
                    return mWriter.toString();
                else
                    return "";
            } catch (ScriptException e) {
                return e.toString();
            }
        }

        @Override
        public String getPrompt() {
            return prompt;
        }
    }
}

