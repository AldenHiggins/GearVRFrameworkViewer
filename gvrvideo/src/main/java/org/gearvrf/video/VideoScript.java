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

package org.gearvrf.video;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Future;

import org.gearvrf.*;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.util.FPSCounter;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;

public class VideoScript extends GVRScript
{
    // Tag for all logging
    private static final String TAG = "KilographPhotoViewer";
    // Set the width of the surrounding cube
    private static final float CUBE_WIDTH = 20.0f;
    // Set the default cubemap name to use
    private static final String DEFAULT_CUBEMAP_NAME = "Interior_02";
    private GVRContext mGVRContext = null;

    @Override
    public void onInit(GVRContext gvrContext)
    {
        mGVRContext = gvrContext;

        GVRScene scene = mGVRContext.getNextMainScene();

        // Set the IPD to .065
        scene.getMainCameraRig().setCameraSeparationDistance(.065f);

        Log.v(TAG, "Camera Distance is: " + scene.getMainCameraRig().getCameraSeparationDistance());

        // ////////////////////////////////////////////////////////////
        //////////////// Generate the right eye cubemap ///////////////
        // ////////////////////////////////////////////////////////////
        GVRCubeSceneObject rightEyeCube = new GVRCubeSceneObject(
                gvrContext, false, generateCubemapTexture(gvrContext, "Interior_02", true));
        rightEyeCube.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH, CUBE_WIDTH);
        // Add the render mask to all of the cube environment's children
        for (int cubeFaceIndex = 0; cubeFaceIndex < rightEyeCube.getChildrenCount();cubeFaceIndex++)
        {
            rightEyeCube.getChildByIndex(cubeFaceIndex).getRenderData().setRenderMask(GVRRenderMaskBit.Left);
        }
        scene.addSceneObject(rightEyeCube);

        // ////////////////////////////////////////////////////////////
        //////////////// Generate the left eye cubemap ///////////////
        // ////////////////////////////////////////////////////////////
        GVRCubeSceneObject leftEyeCube = new GVRCubeSceneObject(
                gvrContext, false, generateCubemapTexture(gvrContext, "Interior_02", false));
        leftEyeCube.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                CUBE_WIDTH);
        // Add the render mask to all of the cube environment's children
        for (int cubeFaceIndex = 0; cubeFaceIndex < leftEyeCube.getChildrenCount();cubeFaceIndex++)
        {
            leftEyeCube.getChildByIndex(cubeFaceIndex).getRenderData().setRenderMask(GVRRenderMaskBit.Right);
        }
        scene.addSceneObject(leftEyeCube);
    }


    // Helper function to generate a cubemap texture from a name
    private ArrayList<Future<GVRTexture>> generateCubemapTexture(GVRContext gvrContext, String cubemapName, boolean isRightEyeTexture)
    {
        // Set the rightOrLeft string to the correct value
        String rightOrLeft = "L";
        if (isRightEyeTexture)
        {
            rightOrLeft = "R";
        }

        // Populate the cubemap
        ArrayList<Future<GVRTexture>> cubemapTexture = null;
        try
        {
            // List of textures (one per face)
            cubemapTexture = new ArrayList<Future<GVRTexture>>(6);
            cubemapTexture.add(gvrContext
                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
                            cubemapName + "/" + cubemapName + "_" + rightOrLeft + "_Back.jpg")));
            cubemapTexture.add(gvrContext
                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
                            cubemapName + "/" + cubemapName + "_" + rightOrLeft + "_Right.jpg")));
            cubemapTexture.add(gvrContext
                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
                            cubemapName + "/" + cubemapName + "_" + rightOrLeft + "_Front.jpg")));
            cubemapTexture.add(gvrContext
                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
                            cubemapName + "/" + cubemapName + "_" + rightOrLeft + "_Left.jpg")));
            cubemapTexture.add(gvrContext
                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
                            cubemapName + "/" + cubemapName + "_" + rightOrLeft + "_Up.jpg")));
            cubemapTexture.add(gvrContext
                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
                            cubemapName + "/" + cubemapName + "_" + rightOrLeft + "_Down.jpg")));
        }
        catch (IOException e)
        {
            Log.e(TAG, "Could not load: " + cubemapName + " check to see if the directory and all the images are correctly named!");
            e.printStackTrace();
            return null;
        }

        return cubemapTexture;
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
    }
}
