package com.fantasy.rogueStrategy

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

object GLESService {

    var nextObjPosition = 0
    var vertexData: FloatBuffer? = null

    val vertexShader = """
        uniform mat4 u_MVPMatrix;      
        attribute vec4 a_Position;     
        attribute vec4 a_Color;        
        varying vec4 v_Color;          
        void main() {                              
           v_Color = a_Color;          
           gl_Position = u_MVPMatrix * a_Position;   
        }                              
    """

    val fragmentShader = """
        precision mediump float;       
        varying vec4 v_Color;          
        void main() {                              
           gl_FragColor = v_Color;     
        }                              
    """

    // Установить настройки вручную. Это будет позже использовано для передачи значений в программу.
    var mMVPMatrixHandle = 0
    var mPositionHandle = 0
    var mColorHandle = 0
    var programHandle = 0;

    var mBytesPerFloat = 4
    var mStrideBytes = 28 //7*mBytesPerFloat  //Количество элементов в вершине.
    var mPositionOffset = 0                   //Смещение в массиве данных.
    var mPositionDataSize = 3                 //Размер массива позиций в элементах.
    var mColorOffset = 3                      //Смещение для данных цвета.
    var mColorDataSize = 4                     //Размер данных цвета в элементах.

    /** Определяем матрицу ВИДА. Её можно рассматривать как камеру. Эта матрица описывает пространство;
     *  она задает положение предметов относительно нашего глаза.*/
    val mViewMatrix = FloatArray(16)

    /** Сохраняем матрицу моделей. Она используется для перемещения моделей со своим пространством  (где каждая модель рассматривается
     *  относительно центра системы координат нашего мира) в пространстве мира.*/
    val mModelMatrix = FloatArray(16)

    /** Сохраняем матрицу проекции.Она используется для преобразования трехмерной сцены в 2D изображение.  */
    val mProjectionMatrix = FloatArray(16)

    /** Выделяем массив для хранения объединеной матрицы. Она будет передана в программу шейдера.  */
    val mMVPMatrix = FloatArray(16)


    fun bindMatrix(){
        // Перемножаем матрицу ВИДА на матрицу МОДЕЛИ, и сохраняем результат в матрицу MVP
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        // Перемножаем матрицу модели*вида на матрицу проекции, сохраняем в MVP матрицу.
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)
    }

    private fun compileShader(type: String) : Int{

        val shaderCode :String?
        val shaderType :Int?

        if(type == "vertex") {
            shaderCode = GLESService.vertexShader
            shaderType = GLES20.GL_VERTEX_SHADER
        }else{
            shaderCode = GLESService.fragmentShader
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

    private fun createProgram() : Int{

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

        if(programHandle == 0){
            throw RuntimeException("Error creating program.")
        }else{
            this.programHandle = programHandle
        }

        return programHandle
    }
    // Установить настройки вручную. Это будет позже использовано для передачи значений в программу.
    private fun setHandles(){
        this.mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix")
        this.mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position")
        this.mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color")
    }

    fun bindData(){
        if(vertexData != null) {
            // Передаем значения о расположении.
            vertexData!!.position(mPositionOffset)
            GLES20.glVertexAttribPointer(
                mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, vertexData
            )
            GLES20.glEnableVertexAttribArray(mPositionHandle)

            // Передаем значения о цвете.
            vertexData!!.position(mColorOffset)
            GLES20.glVertexAttribPointer(
                mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, vertexData
            )
            GLES20.glEnableVertexAttribArray(mColorHandle)
        }else {
            throw RuntimeException("vertexData == null.")
        }
    }

    private fun setLookAtM(rm: FloatArray, rmOffSet: Int, pars: FloatArray) {
        Matrix.setLookAtM(rm, rmOffSet, pars[0], pars[1], pars[2], pars[3], pars[4], pars[5], pars[6], pars[7], pars[8])
    }

    fun frustumM(m: FloatArray, offset: Int, pars: FloatArray) {
        Matrix.frustumM(m, offset, pars[0], pars[1], pars[2], pars[3], pars[4], pars[5]);
    }

    private fun initializeBuffer(){
        val allVerticesData = Game.getAllVerticesData()

        // Инициализируем буфер.
        vertexData = ByteBuffer
            .allocateDirect(allVerticesData.size * mBytesPerFloat)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        vertexData?.put(allVerticesData)
                  ?.position(0)
                  ?: throw RuntimeException("VertexData is null.")
    }

    fun prepareAllData(){

        setLookAtM(mViewMatrix, 0, floatArrayOf(
            0.0f, 0.0f, 1.5f, // Положение глаза, точки наблюдения в пространстве.
            0.0f, 0.0f, -5.0f, // На какое расстояние мы можем видеть вперед. Ограничивающая плоскость обзора.
            0.0f, 1.0f, 0.0f // Устанавливаем вектор. Положение где наша голова находилась бы если бы мы держали камеру.
        ))

        //Загрузка программы шейдеров
        GLES20.glUseProgram(createProgram())

        //Получаем правильные ids
        setHandles()

        // Инициализируем буфер.
        initializeBuffer()
    }

}