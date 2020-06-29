package com.fantasy.rogueStrategy

import android.opengl.GLES20
import android.opengl.Matrix
import com.fantasy.rogueStrategy.GLESService.mModelMatrix
import com.fantasy.rogueStrategy.PaintObjType.*

class PaintObj(
    private val dots: ArrayList<Dot> = ArrayList(),
    private var paintPars: MutableMap<String, Any> = HashMap(),
    pars: MutableMap<String, Any>? = null,
    var objList: MutableList<PaintObj> = ArrayList(),
    type: PaintObjType = NO_DRAW
) {

    private var pars: MutableMap<String, Any>? = pars ?: HashMap()
    private val type: PaintObjType = if(dots.size == 0) NO_DRAW else type
    private var position: Int = 0

    init {
        if(type != NO_DRAW) {
            position = GLESService.nextObjPosition
            //Пропускаем позиции точек текущего объекта
            GLESService.nextObjPosition += dots.size * 3
            //Пропускаем позиции цвета
            if(dots[0].color != null){
                GLESService.nextObjPosition += dots.size * 4
            }
        }
    }

    fun add(obj: PaintObj) {
        objList.add(obj)
    }

    fun isObjList(): Boolean {
        return objList.size > 0
    }

    fun find(par: Any, like: Any): List<PaintObj> {
        return objList.filter { obj ->
            val value =  obj.pars?.get(par)
            val l = if(like is List<*>) like.toList() else arrayListOf(like)
            return@filter l.any{
                if(it is String) {
                    val v = value as String
                    return@any v.contains(it)
                }else
                    return@any value == it
            }
        }
    }

    fun findByFilters(pars: Map<Any, Any>? = null, paintPars: Map<String, Any>? = null, type: PaintObjType? = null): List<PaintObj> {
        return objList.filter { obj ->
            var equalsPars = true

            pars?.forEach {
                if (it.value != obj.pars?.get(it.key)) {
                    equalsPars = false
                    return@forEach
                }
            }
            paintPars?.forEach {
                if (it.value != obj.pars?.get(it.key)) {
                    equalsPars = false
                    return@forEach
                }
            }

            equalsPars && (type == null || obj.type == type)
        }
    }

    fun getAllDots(): ArrayList<Dot>{
        val allDots = ArrayList<Dot>()

        dots.forEach{
            allDots.add(it)
        }
        objList.forEach{
            allDots.addAll(it.getAllDots())
        }

        return allDots
    }

    private var translate: Dot? = null
    private var rotate: MutableMap<Dot,Float> = mutableMapOf()

    fun translate(vector: Dot? = null) :PaintObj{
        translate = vector
        return this
    }

    fun rotate(degree: Float, rotateBy: Dot? = null) :PaintObj{
        val rBy = rotateBy ?: Dot(0f,0f,0f)
        rotate[rBy] = degree

        return this
    }

    fun drawIt() {
        if(type != NO_DRAW) {
            Matrix.setIdentityM(mModelMatrix, 0)

            if(translate != null){
                val t = translate!!
                Matrix.translateM(mModelMatrix, 0, t.x, t.y, t.z)
            }

            if(rotate.isNotEmpty()) {
                rotate.forEach{
                    val rBy = it.key
                    val degree = it.value//Наслаиваются друг на друга
                    Matrix.rotateM(mModelMatrix, 0, degree, rBy.x, rBy.y, rBy.z)
                }
            }

            GLESService.bindData()
            GLESService.bindMatrix()

            when(type){
                TRIANGLE -> GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
                SQUARE -> TODO()
            }
        }

        objList.forEach{
            it.drawIt()
        }
    }
}