package com.fantasy.rogueStrategy

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    /** Создаем экземпляр нашего GLSurfaceView  */
    private var mGLSurfaceView: GLSurfaceView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mGLSurfaceView = GLSurfaceView(this)

        // Проверяем поддереживается ли OpenGL ES 2.0.

        // Проверяем поддереживается ли OpenGL ES 2.0.
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        val supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000

        if (supportsEs2) {
            // Запрос OpenGL ES 2.0 для установки контекста.
            mGLSurfaceView?.setEGLContextClientVersion(2)

            // Устанавливаем рендеринг, создаем экземпляр класса, он будет описан ниже.
            mGLSurfaceView?.setRenderer(MyRenderer())
        } else {
            // Устройство поддерживает только OpenGL ES 1.x
            // опишите реализацию рендеринга здесь, для поддержку двух систем ES 1 and ES 2.
            return
        }

        setContentView(mGLSurfaceView)
    }

    override fun onResume() {
        super.onResume()
        mGLSurfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mGLSurfaceView?.onPause()
    }
}