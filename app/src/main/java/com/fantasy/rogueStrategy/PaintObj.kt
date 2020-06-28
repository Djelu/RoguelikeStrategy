package com.fantasy.rogueStrategy

import android.opengl.GLES20
import com.fantasy.rogueStrategy.PaintObjType.NO_DRAW
import java.util.*
import kotlin.collections.ArrayList

class PaintObj(
    private val dots: ArrayList<Dot> = ArrayList(),
    private var paintPars: MutableMap<String, Any> = HashMap(),
    private var pars: MutableMap<String, Any> = HashMap(),
    var objList: MutableList<PaintObj> = ArrayList(),
    type: PaintObjType = NO_DRAW
) {

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

    fun find(pars: MutableMap<Any, Any>? = null, paintPars: MutableMap<String, Any>? = null, type: PaintObjType? = null): PaintObj {
        return PaintObj(objList = objList.filter { obj ->
            var equalsPars = true

            pars?.forEach {
                if (obj.pars[it.key]?.equals(it.value) != true) {
                    equalsPars = false
                    return@forEach
                }
            }
            paintPars?.forEach {
                if (obj.pars[it.key]?.equals(it.value) != true) {
                    equalsPars = false
                    return@forEach
                }
            }

            equalsPars && type != null && obj.type == type
        }.toMutableList())
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

    fun drawIt() {
        if(type != NO_DRAW) {
            GLESService.bindData()
            GLESService.bindMatrix()
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, this.position, 3)
        }

        objList.forEach{
            it.drawIt()
        }
    }
}