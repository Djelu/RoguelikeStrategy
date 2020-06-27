package com.fantasy.rogueStrategy

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyRenderer : GLSurfaceView.Renderer {


    override fun onSurfaceCreated(glUnused: GL10?, config: EGLConfig?) {

        // Устанавливаем цвет фона светло серый.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)

        Game.world = mutableMapOf(
            "tri" to ObjectsService.createTriangle(
                Dot(-0.5f, -0.25f, 0.0f),
                Dot(0.5f, -0.25f, 0.0f),
                Dot(0.0f, 0.559f, 0.0f), RGBA(255, 0, 0)
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

        // Создаем новую матрицу проекции. Высота остается та же,
        // а ширина будет изменяться в соответствии с соотношением сторон.
        val ratio = width.toFloat() / height
        GLESService.frustumM(GLESService.mProjectionMatrix, 0, floatArrayOf(
            -ratio, ratio, // left, right,
            -1.0f, 1.0f,   // bottom, top,
             1.0f, 10.0f   // near, far
        ))
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        // Делаем полный оборот при вращении за 10 секунд.
        val time = SystemClock.uptimeMillis() % 10000L
        val angleInDegrees = 360.0f / 10000.0f * time.toInt()

        // Рисуем треугольники плоскостью к нам.
        Matrix.setIdentityM(GLESService.mModelMatrix, 0)
        Matrix.rotateM(GLESService.mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f)

        Game.drawAllObjects()
    }
}