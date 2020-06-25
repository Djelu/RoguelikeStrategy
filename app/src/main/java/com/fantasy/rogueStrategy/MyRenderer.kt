package com.fantasy.rogueStrategy

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyRenderer : GLSurfaceView.Renderer {

    private val vertexShader = """
        uniform mat4 u_MVPMatrix;      
        attribute vec4 a_Position;     
        attribute vec4 a_Color;        
        varying vec4 v_Color;          
        void main() {                              
           v_Color = a_Color;          
           gl_Position = u_MVPMatrix * a_Position;   
        }                              
    """

    private val fragmentShader = """
        precision mediump float;       
        varying vec4 v_Color;          
        void main() {                              
           gl_FragColor = v_Color;     
        }                              
    """


    /**
     * Определяем матрицу ВИДА. Её можно рассматривать как камеру. Эта матрица описывает пространство;
     * она задает положение предметов относительно нашего глаза.
     */
    private val mViewMatrix = FloatArray(16)
    /** Сохраняем матрицу проекции.Она используется для преобразования трехмерной сцены в 2D изображение.  */
    private val mProjectionMatrix = FloatArray(16)

    /** Будем хранить наши данные в числовом буфере.  */
    private var mTriangleVertices: MutableList<FloatBuffer> = ArrayList()

    /** Количество байт занимаемых одним числом.  */
    private val mBytesPerFloat = 4

    // Треугольники красный, зеленый, и синий.
    private val triangle1VerticesData = floatArrayOf(
        // X, Y, Z,
        // R, G, B, A
        -0.5f, -0.25f, 0.0f,
        1.0f, 0.0f, 0.0f, 1.0f,

        0.5f, -0.25f, 0.0f,
        0.0f, 0.0f, 1.0f, 1.0f,

        0.0f, 0.559016994f, 0.0f,
        0.0f, 1.0f, 0.0f, 1.0f
    )

    /** Используется для передачи в матрицу преобразований.  */
    private var mMVPMatrixHandle = 0
    /** Используется для передачи информации о положении модели.  */
    private var mPositionHandle = 0
    /** Используется для передачи информации о цвете модели.  */
    private var mColorHandle = 0

    private fun compileShader(type: String) : Int{

        val shader =
            if(type == "vertex") {
                vertexShader
            }else{
                fragmentShader
            }


        // Загрузка шейдера.
        var shaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)

        if (shaderHandle != 0) {
            // Передаем в наш шейдер программу.
            GLES20.glShaderSource(shaderHandle, shader)

            // Компиляция шейреда
            GLES20.glCompileShader(shaderHandle)

            // Получаем результат процесса компиляции
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

            // Если компиляция не удалась, удаляем шейдер.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(shaderHandle)
                shaderHandle = 0
            }
        }

        return shaderHandle
    }

    private fun createProgram() : Int {

        val vertexShaderHandle = compileShader("vertex")
        if (vertexShaderHandle == 0) {
            throw RuntimeException("Error creating vertex shader.")
        }

        val fragmentShaderHandle = compileShader("fragment")
        if (fragmentShaderHandle == 0) {
            throw RuntimeException("Error creating fragment shader.")
        }

        //Создаем объект программы вместе со ссылкой на нее.
        var programHandle = GLES20.glCreateProgram()

        if (programHandle != 0) {
            // Подключаем вершинный шейдер к программе.
            GLES20.glAttachShader(programHandle, vertexShaderHandle)

            // Подключаем фрагментный шейдер к программе.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle)

            // Подключаем атрибуты цвета и положения
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position")
            GLES20.glBindAttribLocation(programHandle, 1, "a_Color")

            // Объединяем оба шейдера в программе.
            GLES20.glLinkProgram(programHandle)

            // Получаем ссылку на программу.
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)

            // Если ссылку не удалось получить, удаляем программу.
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programHandle)
                programHandle = 0
            }
        }

        return programHandle
    }

    private fun setLookAtM(rm: FloatArray, rmOffSet: Int, pars: FloatArray) {
        Matrix.setLookAtM(rm, rmOffSet, pars[0], pars[1], pars[2], pars[3], pars[4], pars[5], pars[6], pars[7], pars[8])
    }

    private fun frustumM() {

    }

    override fun onSurfaceCreated(glUnused: GL10?, config: EGLConfig?) {

        // Устанавливаем цвет фона светло серый.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)

        setLookAtM(mViewMatrix, 0, floatArrayOf(
            0.0f, 0.0f, 1.5f, // Положение глаза, точки наблюдения в пространстве.
            0.0f, 0.0f, -5.0f, // На какое расстояние мы можем видеть вперед. Ограничивающая плоскость обзора.
            0.0f, 1.0f, 0.0f // Устанавливаем вектор. Положение где наша голова находилась бы если бы мы держали камеру.
        ))

        //Загрузка программы шейдеров
        val programHandle = createProgram()
        if (programHandle == 0) {
            throw java.lang.RuntimeException("Error creating program.")
        }

        // Установить настройки вручную. Это будет позже использовано для передачи значений в программу.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix")
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position")
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color")

        // Сообщить OpenGL чтобы использовал эту программу при рендеринге.
        GLES20.glUseProgram(programHandle)



        // Инициализируем буфер.
        val triangle = ByteBuffer.allocateDirect(triangle1VerticesData.size * mBytesPerFloat)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        triangle.put(triangle1VerticesData)
            .position(0)

        mTriangleVertices.add(triangle)
    }

    /**
     * Этот метод вызывается всякий раз, когда происходит изменения поверхности,
     * например, при переключении с портретной на альбомную ориентацию.
     * Он также вызывается после того как поверхность была создана.
     */
    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        // Устанавливаем OpenGL окно просмотра того же размера что и поверхность экрана.

        // Устанавливаем OpenGL окно просмотра того же размера что и поверхность экрана.
        GLES20.glViewport(0, 0, width, height)

        // Создаем новую матрицу проекции. Высота остается та же,
        // а ширина будет изменяться в соответствии с соотношением сторон.

        // Создаем новую матрицу проекции. Высота остается та же,
        // а ширина будет изменяться в соответствии с соотношением сторон.
        val ratio: Float = width.toFloat() / height
        val left = -ratio
        val bottom = -1.0f
        val top = 1.0f
        val near = 1.0f
        val far = 10.0f

        Matrix.frustumM(mProjectionMatrix, 0, left, ratio, bottom, top, near, far)
    }

    override fun onDrawFrame(p0: GL10?) {
        TODO("Not yet implemented")
    }
}