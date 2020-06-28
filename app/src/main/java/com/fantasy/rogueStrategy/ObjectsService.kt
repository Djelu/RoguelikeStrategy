package com.fantasy.rogueStrategy

object ObjectsService {

    fun createTriangle(a: Dot, b: Dot, c: Dot, color: RGBA? = null, pars: Map<String, Any>? = null) :PaintObj{
        val aa = Dot(a.x,a.y,a.z,color ?: a.color)
        val bb = Dot(b.x,b.y,b.z,color ?: b.color)
        val cc = Dot(c.x,c.y,c.z,color ?: c.color)

        return PaintObj(
            dots = arrayListOf(aa, bb, cc),
            type = PaintObjType.TRIANGLE,
            pars = pars?.toMutableMap()
        )
    }

    fun createTriangle(a: Dot, radius: Float, color: RGBA? = null, pars:Map<String, Any>? = null) :PaintObj{
        val aa = Dot(a.x-radius,a.y-radius,a.z,color ?: a.color)
        val bb = Dot(a.x+radius,a.y-radius,a.z,color ?: a.color)
        val cc = Dot(a.x,a.y+radius,a.z,color ?: a.color)

        return PaintObj(
            dots = arrayListOf(aa, bb, cc),
            type = PaintObjType.TRIANGLE,
            pars = pars?.toMutableMap()
        )
    }

    fun createTriangle(position: Dot? = null, color: RGBA? = null, pars:Map<String, Any>? = null) :PaintObj{
        return PaintObj(
            dots = arrayListOf(
                Dot(-0.5f, -0.25f, 0.0f, color),
                Dot(0.5f, -0.25f, 0.0f, color),
                Dot(0.0f, 0.559f, 0.0f, color)
            ),
            type = PaintObjType.TRIANGLE,
            pars = pars?.toMutableMap()
        ).translate(position)
    }
}