package com.fantasy.rogueStrategy

import android.opengl.GLES20
import android.opengl.Matrix
import com.fantasy.rogueStrategy.PaintObjType.*
import java.util.*
import kotlin.collections.ArrayList

class PaintObj(
    private var paintPars: MutableMap<String, Any> = HashMap(),
    pars: MutableMap<String, Any> = HashMap(),
    private var objList: MutableList<PaintObj> = ArrayList(),
    type: PaintObjType = NO_DRAW
) {

    private var pars: MutableMap<String, Any>? = pars
    private val type: PaintObjType = if(paintPars.isEmpty()) NO_DRAW else type

    init {
        //Устанавливаем дефолтные для типов параметры
        paintPars.putAll(when(type) {
            TRIANGLE -> mapOf(
                "mBytesPerFloat" to 4,                    //Количество байт занимаемых одним числом.
                "mStrideBytes" to 28, //7*mBytesPerFloat  //Количество элементов в вершине.
                //"mPositionOffset" to 0,                   //Смещение в массиве данных.
                "mPositionDataSize" to 3,                 //Размер массива позиций в элементах.
                "mColorOffset" to 3,                      //Смещение для данных цвета.
                "mColorDataSize" to 4                     //Размер данных цвета в элементах.
            )
            SQUARE -> TODO()
            NO_DRAW -> TODO()
        })
    }

    fun add(obj: PaintObj) {
        objList.add(obj)
    }

    fun isObjList(): Boolean {
        return objList.size > 0
    }

    fun find(pars: MutableMap<Any, Any>? = null, paintPars: MutableMap<String, Any>? = null, type: PaintObjType? = null): PaintObj {
        return PaintObj(objList = objList.filter { obj ->
            var equalsPars = true

            pars?.forEach {
                if (obj.pars?.get(it.key)?.equals(it.value) != true) {
                    equalsPars = false
                    return@forEach
                }
            }
            paintPars?.forEach {
                if (obj.pars?.get(it.key)?.equals(it.value) != true) {
                    equalsPars = false
                    return@forEach
                }
            }

            equalsPars && type != null && obj.type == type
        }.toMutableList())
    }

    fun drawIt() {

        val dots = pars?.get("dots") as FloatArray
        // Передаем значения о расположении.
        //aTriangleBuffer.position(mPositionOffset)
        //dots.get(paintPars["mPositionOffset"] as Int)
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

        objList.forEach{
            it.drawIt()
        }
    }
}