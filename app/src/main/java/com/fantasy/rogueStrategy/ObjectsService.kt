package com.fantasy.rogueStrategy

object ObjectsService {

    fun createTriangle(x: Dot, y: Dot, z: Dot, color: RGBA) :PaintObj{
        val xx = x; xx.color = color
        val yy = y; yy.color = color
        val zz = z; zz.color = color

        return PaintObj(
            dots = arrayListOf(xx, yy, zz),
            type = PaintObjType.TRIANGLE
        )
    }
}