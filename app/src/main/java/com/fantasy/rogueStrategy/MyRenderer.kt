package com.fantasy.rogueStrategy

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
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

    /** Определяем матрицу ВИДА. Её можно рассматривать как камеру. Эта матрица описывает пространство;
     *  она задает положение предметов относительно нашего глаза.*/
    private val mViewMatrix = FloatArray(16)

    /** Сохраняем матрицу моделей. Она используется для перемещения моделей со своим пространством  (где каждая модель рассматривается
     *  относительно центра системы координат нашего мира) в пространстве мира.*/
    private val mModelMatrix = FloatArray(16)

    /** Сохраняем матрицу проекции.Она используется для преобразования трехмерной сцены в 2D изображение.  */
    /** Выделяем массив для хранения объединеной матрицы. Она будет передана в программу шейдера.  */
    private val mMVPMatrix = FloatArray(16)

    private val mBytesPerFloat = 4 //Количество байт занимаемых одним числом.
    private val mStrideBytes = 7 * mBytesPerFloat //Количество элементов в вершине.
    private val mPositionOffset = 0 //Смещение в массиве данных.
    private val mPositionDataSize = 3 //Размер массива позиций в элементах.
    private val mColorOffset = 3 //Смещение для данных цвета.
    private val mColorDataSize = 4 //Размер данных цвета в элементах.


    private val mProjectionMatrix = FloatArray(16)

    /** Будем хранить наши данные в числовом буфере.  */
    private var mTriangleVertices: MutableList<FloatBuffer> = ArrayList()

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

        val shaderCode :String?
        val shaderType :Int?

        if(type == "vertex") {
            shaderCode = vertexShader
            shaderType = GLES20.GL_VERTEX_SHADER
        }else{
            shaderCode = fragmentShader
            shaderType = GLES20.GL_FRAGMENT_SHADER
        }


        // Загрузка шейдера.
        var shaderHandle = GLES20.glCreateShader(shaderType)

        if (shaderHandle != 0) {
            // Передаем в наш шейдер программу.
            GLES20.glShaderSource(shaderHandle, shaderCode)

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

    private fun frustumM(m: FloatArray, offset: Int, pars: FloatArray) {
        Matrix.frustumM(m, offset, pars[0], pars[1], pars[2], pars[3], pars[4], pars[5]);
    }

    /**
     * Рисуем треугольники из массива данных вершин.
     *
     * @param aTriangleBuffer Буфер содержащий данные о вершинах.
     */
    private fun drawTriangle(aTriangleBuffer: FloatBuffer) {
        // Передаем значения о расположении.
        aTriangleBuffer.position(mPositionOffset)
        GLES20.glVertexAttribPointer(
            mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
            mStrideBytes, aTriangleBuffer
        )
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // Передаем значения о цвете.
        aTriangleBuffer.position(mColorOffset)
        GLES20.glVertexAttribPointer(
            mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
            mStrideBytes, aTriangleBuffer
        )
        GLES20.glEnableVertexAttribArray(mColorHandle)

        // Перемножаем матрицу ВИДА на матрицу МОДЕЛИ, и сохраняем результат в матрицу MVP
        // (которая теперь содержит модель*вид).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)

        // Перемножаем матрицу модели*вида на матрицу проекции, сохраняем в MVP матрицу.
        // (которая теперь содержит модель*вид*проекцию).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
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
        GLES20.glViewport(0, 0, width, height)

        // Создаем новую матрицу проекции. Высота остается та же,
        // а ширина будет изменяться в соответствии с соотношением сторон.

        val ratio = width.toFloat() / height
        frustumM(mProjectionMatrix, 0, floatArrayOf(
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
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f)

        mTriangleVertices.forEach{
            drawTriangle(it)
        }
    }
}