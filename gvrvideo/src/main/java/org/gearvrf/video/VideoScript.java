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
    private static final float CUBE_WIDTH = 20.0f;
    private static final float SCALE_FACTOR = 2.0f;
    private GVRContext mGVRContext = null;

    // Type of object for the environment
    // 0: surrounding sphere using GVRSphereSceneObject
    // 1: surrounding cube using GVRCubeSceneObject and 1 GVRCubemapTexture
    //    (method A)
    // 2: surrounding cube using GVRCubeSceneObject and compressed ETC2 textures
    //    (method B, best performance)
    // 3: surrounding cube using GVRCubeSceneObject and 6 GVRTexture's
    //    (method C)
    // 4: surrounding cylinder using GVRCylinderSceneObject
    // 5: surrounding cube using six GVRSceneOjbects (quads)
    private static final int mEnvironmentType = 2;

    // Type of object for the reflective object
    // 0: reflective sphere using GVRSphereSceneObject
    // 1: reflective sphere using OBJ model
    private static final int mReflectiveType = 0;

    @Override
    public void onInit(GVRContext gvrContext)
    {
        mGVRContext = gvrContext;

        GVRScene scene = mGVRContext.getNextMainScene();

        try
        {
            // List of textures (one per face)
            ArrayList<Future<GVRTexture>> futureTextureList = new ArrayList<Future<GVRTexture>>(6);
            futureTextureList.add(gvrContext
                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
                            "comcast/back.jpg")));
            futureTextureList.add(gvrContext
                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
                            "comcast/right.jpg")));
            futureTextureList.add(gvrContext
                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
                            "comcast/front.jpg")));
            futureTextureList.add(gvrContext
                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
                            "comcast/left.jpg")));
            futureTextureList.add(gvrContext
                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
                            "comcast/top.jpg")));
            futureTextureList.add(gvrContext
                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
                            "comcast/bottom.jpg")));


            // ////////////////////////////////////////////////////////////
            // create surrounding cube using GVRCubeSceneObject method A //
            // ////////////////////////////////////////////////////////////
            GVRCubeSceneObject mCubeEvironment = new GVRCubeSceneObject(
                    gvrContext, false, futureTextureList);
//            mCubeEvironment.getRenderData().setRenderMask(GVRRenderMaskBit.Left);
            mCubeEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                    CUBE_WIDTH);
            // Add the render mask to all of the cube environment's children
            for (int cubeFaceIndex = 0; cubeFaceIndex < mCubeEvironment.getChildrenCount();cubeFaceIndex++)
            {
                mCubeEvironment.getChildByIndex(cubeFaceIndex).getRenderData().setRenderMask(GVRRenderMaskBit.Left);
            }
            scene.addSceneObject(mCubeEvironment);


//
//            // List of textures (one per face)
//            ArrayList<Future<GVRTexture>> beachTextureList = new ArrayList<Future<GVRTexture>>(6);
//            beachTextureList.add(gvrContext
//                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
//                            "beach/back.jpg")));
//            beachTextureList.add(gvrContext
//                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
//                            "beach/right.jpg")));
//            beachTextureList.add(gvrContext
//                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
//                            "beach/front.jpg")));
//            beachTextureList.add(gvrContext
//                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
//                            "beach/left.jpg")));
//            beachTextureList.add(gvrContext
//                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
//                            "beach/top.jpg")));
//            beachTextureList.add(gvrContext
//                    .loadFutureTexture(new GVRAndroidResource(gvrContext,
//                            "beach/bottom.jpg")));
//
//            GVRCubeSceneObject otherTestCube = new GVRCubeSceneObject(
//                    gvrContext, false, beachTextureList);
////            otherTestCube.getRenderData().setRenderMask(GVRRenderMaskBit.Right);
//            otherTestCube.getTransform().setScale(CUBE_WIDTH - 1, CUBE_WIDTH - 1,
//                    CUBE_WIDTH - 1);
//            scene.addSceneObject(otherTestCube);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


    }

    @Override
    public void onStep() {
        FPSCounter.tick();
    }
}
