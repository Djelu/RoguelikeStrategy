package com.fantasy.rogueStrategy.services

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.fantasy.rogueStrategy.Game
import com.fantasy.rogueStrategy.R
import com.fantasy.rogueStrategy.atoms.Dot
import com.fantasy.rogueStrategy.atoms.PaintObj
import com.fantasy.rogueStrategy.objects.Textures
import com.fantasy.rogueStrategy.util.ShaderUtils
import com.fantasy.rogueStrategy.util.TextureUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

object GLESService {

    var fps: Long = 144
    var nextObjPosition = 0
    var vertexData: FloatBuffer? = null
    var allVerticesData: FloatArray = floatArrayOf()

    var context: Context? = null
    var glSurfaceView: GLSurfaceView? = null

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
    var mMatrixHandle = 0
    var mPositionHandle = 0
    var mColorHandle = 0
    var mTextureHandle = 0
    var mTextureUnitHandle = 0

    var programHandle = 0;

    private const val mPositionOffset = 0 //Смещение в массиве данных.
    private const val mPositionDataSize = 3 //Размер массива позиций в элементах.
    private const val mColorOffset = 3 //Смещение для данных цвета.
    private const val mColorDataSize = 4 //Размер данных цвета в элементах.
    private const val mTextureOffset = 7 //Смещение для данных цвета.
    private const val mTextureDataSize = 2 //Размер данных цвета в элементах.
    private const val mBytesPerFloat = 4 //Количество байт занимаемых одним числом.
    private const val mStrideBytes = (mPositionDataSize+mColorDataSize+mTextureDataSize) * mBytesPerFloat //Количество элементов в вершине.


    /** Определяем матрицу ВИДА. Её можно рассматривать как камеру. Эта матрица описывает пространство;
     *  она задает положение предметов относительно нашего глаза.*/
    val mViewMatrix = FloatArray(16)

    /** Сохраняем матрицу моделей. Она используется для перемещения моделей со своим пространством  (где каждая модель рассматривается
     *  относительно центра системы координат нашего мира) в пространстве мира.*/
    val mModelMatrix = FloatArray(16)

    /** Сохраняем матрицу проекции.Она используется для преобразования трехмерной сцены в 2D изображение.  */
    val mProjectionMatrix = FloatArray(16)

    /** Выделяем массив для хранения объединеной матрицы. Она будет передана в программу шейдера.  */
    val mMatrix = FloatArray(16)

    fun createProjectionMatrix(width: Int, height: Int) {
        var ratio = 1f
        var left = -1f
        var right = 1f
        var bottom = -1f
        var top = 1f
        val near = 2f
        val far = 12f
        if (width > height) {
            ratio = width.toFloat() / height
            left *= ratio
            right *= ratio
        } else {
            ratio = height.toFloat() / width
            bottom *= ratio
            top *= ratio
        }
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far)
    }

    fun bindMatrix(){
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0)
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMatrix, 0)
    }

    private fun createProgram() : Int{

        programHandle = ShaderUtils.createProgram(
            ShaderUtils.createShader(context, GLES20.GL_VERTEX_SHADER, R.raw.vertex_shader),
            ShaderUtils.createShader(context, GLES20.GL_FRAGMENT_SHADER, R.raw.fragment_shader)
        )

        if(programHandle == 0){
            throw RuntimeException("Error creating program.")
        }

        return programHandle
    }
    // Установить настройки вручную. Это будет позже использовано для передачи значений в программу.
    private fun setHandles(){
        mMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_Matrix")
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position")
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color")
        mTextureHandle = GLES20.glGetAttribLocation(programHandle, "a_Texture")
        mTextureUnitHandle = GLES20.glGetUniformLocation(programHandle, "u_TextureUnit")
    }

    fun bindData(textureId: Int = 0){
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

            // координаты текстур
            vertexData!!.position(mTextureOffset)
            GLES20.glVertexAttribPointer(
                mTextureHandle, mTextureDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, vertexData
            )
            GLES20.glEnableVertexAttribArray(mTextureHandle)

            // помещаем текстуру в target 2D юнита 0
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

            // юнит текстуры
            GLES20.glUniform1i(mTextureHandle, 0)
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

    //Запускать при удалении элементов из мира
    private fun initializeBuffer(){
        allVerticesData = Game.getAllVerticesData()

        // Инициализируем буфер.
        vertexData = ByteBuffer
            .allocateDirect(allVerticesData.size * mBytesPerFloat)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        vertexData?.put(allVerticesData)
                  ?: throw RuntimeException("VertexData is null.")
    }

    fun addToData(objs: List<PaintObj>) :MutableList<PaintObj>{

        val dots: ArrayList<Dot> = ArrayList()
        objs.forEach{
            dots.addAll(it.getAllDots())
        }
        val vertices: ArrayList<Float> = ArrayList()
        dots.forEach{
            vertices.addAll(it.getVerticesData())
        }

        allVerticesData += vertices

        // Инициализируем буфер.
        vertexData = ByteBuffer
            .allocateDirect(allVerticesData.size * mBytesPerFloat)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        vertexData?.put(allVerticesData)
            ?: throw RuntimeException("VertexData is null.")

        return objs.toMutableList()
    }

    fun initializeTextures(){
        context?.let {
            Textures.box = TextureUtils.loadTexture(it, R.drawable.box)
        } ?: throw RuntimeException("Error while init textures")
    }

    fun prepareAllData(){

        setLookAtM(mViewMatrix, 0, floatArrayOf(
            0.0f, 0.0f, 7f, // Положение глаза, точки наблюдения в пространстве.
            0.0f, 0.0f, 0f, // На какое расстояние мы можем видеть вперед. Ограничивающая плоскость обзора.
            0.0f, 1.0f, 0.0f // Устанавливаем вектор. Положение где наша голова находилась бы если бы мы держали камеру.
        ))

        //Загрузка программы шейдеров
        GLES20.glUseProgram(createProgram())

        //Получаем правильные ids
        setHandles()

        // Инициализируем буфер
        //initializeBuffer()
    }

}