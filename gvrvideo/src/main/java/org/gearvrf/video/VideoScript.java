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
    private static final String DEFAULT_CUBEMAP_NAME = "Interior_01";
    private static final String OTHER_CUBEMAP_NAME = "Interior_02";
    private GVRContext mGVRContext = null;

    // Keep track of when the user taps the screen
    private boolean isTapped;

    // Store the first and second cubemaps so they can be quickly switched when needed
    private GVRCubeSceneObject firstLeft;
    private GVRCubeSceneObject firstRight;

    private GVRCubeSceneObject secondLeft;
    private GVRCubeSceneObject secondRight;

    @Override
    public void onInit(GVRContext gvrContext)
    {
        mGVRContext = gvrContext;
        GVRScene scene = mGVRContext.getNextMainScene();

        // Set the IPD to .065
        scene.getMainCameraRig().setCameraSeparationDistance(.065f);

        // Generate first right eye cubemap
        firstRight = addLeftOrRightCubemapToScene(scene, true, DEFAULT_CUBEMAP_NAME);
        // Generate first left eye cubemap
        firstLeft = addLeftOrRightCubemapToScene(scene, false, DEFAULT_CUBEMAP_NAME);

        // Generate second right eye cubemap
        secondRight = addLeftOrRightCubemapToScene(scene, true, OTHER_CUBEMAP_NAME);
        // Generate second left eye cubemap
        secondLeft = addLeftOrRightCubemapToScene(scene, false, OTHER_CUBEMAP_NAME);

        // Hide the second cubemaps
        setCubeRenderMasks(secondLeft, 0);
        setCubeRenderMasks(secondRight, 0);
    }

    // Helper function to add a new cubemap to the scene
    private GVRCubeSceneObject addLeftOrRightCubemapToScene(GVRScene scene, boolean isRightEye, String cubemapName)
    {
        GVRCubeSceneObject cube = new GVRCubeSceneObject(
                mGVRContext, false, generateCubemapTexture(cubemapName, isRightEye));
        cube.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH, CUBE_WIDTH);

        // Set the rendermask correctly
        int renderMask = isRightEye ? GVRRenderMaskBit.Right : GVRRenderMaskBit.Left;
        // Add the render mask to all of the cube environment's children
        setCubeRenderMasks(cube, renderMask);
        scene.addSceneObject(cube);
        return cube;
    }

    // Helper function to generate a cubemap texture from a name
    private ArrayList<Future<GVRTexture>> generateCubemapTexture(String cubemapName, boolean isRightEyeTexture)
    {
        // Set the rightOrLeft string to the correct value
        String rightOrLeft = isRightEyeTexture ? "R" : "L";

        // Populate the cubemap
        ArrayList<Future<GVRTexture>> cubemapTexture = null;
        // The order to add all of the faces into the cubemap (and the way they should be named)
        String[] faces = { "_Back", "_Right", "_Front", "_Left", "_Up", "_Down" };
        try
        {
            // Generate the list of textures
            cubemapTexture = new ArrayList<Future<GVRTexture>>(6);
            for (int faceIndex = 0; faceIndex < 6; faceIndex++)
            {
                cubemapTexture.add(mGVRContext
                        .loadFutureTexture(new GVRAndroidResource(mGVRContext,
                                cubemapName + "/" + cubemapName + "_" + rightOrLeft + faces[faceIndex] + ".jpg")));
            }
        }
        catch (IOException e)
        {
            Log.e(TAG, "Could not load: " + cubemapName + " check to see if the directory and all the images are correctly named!");
            e.printStackTrace();
            return null;
        }

        return cubemapTexture;
    }

    // Helper functions to set the render mask of all six faces of a cube scene object
    private void setCubeRenderMasks(GVRCubeSceneObject cube, int newRenderMask)
    {
        for (int cubeFaceIndex = 0; cubeFaceIndex < cube.getChildrenCount();cubeFaceIndex++)
        {
            cube.getChildByIndex(cubeFaceIndex).getRenderData().setRenderMask(newRenderMask);
        }
    }

    // Callback function when the user taps the touchpad
    public void onSingleTap(MotionEvent e)
    {
        Log.e(TAG, "Tap was recorded!!!");
        isTapped = true;
    }

    @Override
    public void onStep()
    {
        FPSCounter.tick();

        // Check to see if the user has tapped the screen, if so switch the cubemaps
        if (isTapped)
        {
            Log.e(TAG, "Got in here");
            isTapped = false;

            // Case where the second cubemap is being displayed
            if (firstLeft.getChildByIndex(0).getRenderData().getRenderMask() == 0)
            {
                setCubeRenderMasks(firstLeft, GVRRenderMaskBit.Left);
                setCubeRenderMasks(firstRight, GVRRenderMaskBit.Right);
                setCubeRenderMasks(secondLeft, 0);
                setCubeRenderMasks(secondRight, 0);
            }
            // Case where the first cubemap is being displayed
            else
            {
                setCubeRenderMasks(secondLeft, GVRRenderMaskBit.Left);
                setCubeRenderMasks(secondRight, GVRRenderMaskBit.Right);
                setCubeRenderMasks(firstLeft, 0);
                setCubeRenderMasks(firstRight, 0);
            }
        }
    }
}
