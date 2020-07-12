package com.fantasy.rogueStrategy

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.fantasy.rogueStrategy.atoms.Dot
import com.fantasy.rogueStrategy.atoms.PaintObj
import com.fantasy.rogueStrategy.atoms.RGBA
import com.fantasy.rogueStrategy.objects.Textures
import com.fantasy.rogueStrategy.services.GLESService
import com.fantasy.rogueStrategy.services.ObjectsService
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyRenderer(context: Context, glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer {

    init {
        GLESService.context = context
        GLESService.glSurfaceView = glSurfaceView
    }

    override fun onSurfaceCreated(glUnused: GL10?, config: EGLConfig?) {

        // Устанавливаем цвет фона светло серый.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)

        // Инициализируем текстуры
        GLESService.initializeTextures();

        Game.world = mutableMapOf(
            "squares" to PaintObj(
                objList = GLESService.addToData(listOf(
                    ObjectsService.createSquare(
                        1f,1f,
                        pars = mapOf("name" to "square1")
                    ).setColor(listOf(
                        RGBA(1f, 0f, 0f),
                        RGBA(0f, 1f, 0f),
                        RGBA(0f, 0f, 1f),
                        RGBA(1f, 0f, 1f)
                    )).setTexture(Textures.box)
                      .rotate(30f, Dot(0f,0f,1f))
                ))
            )
        )

        GLESService.prepareAllData()
    }

    /**
     * Этот метод вызывается всякий раз, когда происходит изменения поверхности,
     * например, при переключении с портретной на альбомную ориентацию.
     * Он также вызывается после того как поверхность была создана.
     */
    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        // Устанавливаем OpenGL окно просмотра того же размера что и поверхность экрана.
        GLES20.glViewport(0, 0, width, height)

        GLESService.createProjectionMatrix(width, height)

        GLESService.bindMatrix()
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        /*
        // Делаем полный оборот при вращении за 10 секунд.
        val time = SystemClock.uptimeMillis() % 10000L
        val angleInDegrees = 360.0f / 10000.0f * time.toInt()

        Game.world["squares"]?.find("name", "square1")?.forEach {
            it.rotate(angleInDegrees,
                Dot(0f, 0f, 1f)
            )
        }
        */

        Game.drawAllObjects()

        Thread.sleep(1000/ GLESService.fps)
    }
}