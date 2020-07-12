package com.fantasy.rogueStrategy.services

import com.fantasy.rogueStrategy.atoms.Dot
import com.fantasy.rogueStrategy.atoms.PaintObj
import com.fantasy.rogueStrategy.atoms.RGBA
import com.fantasy.rogueStrategy.enums.PaintObjType

object ObjectsService {

    fun createTriangle(a: Dot, b: Dot, c: Dot, color: RGBA? = null, pars: Map<String, Any>? = null) : PaintObj {
        val aa = Dot(a.x, a.y, a.z, color ?: a.color, 0f, 0f)
        val bb = Dot(b.x, b.y, b.z, color ?: b.color, 0f, 1f)
        val cc = Dot(c.x, c.y, c.z, color ?: c.color, 1f, 0f)

        return PaintObj(
            dots = arrayListOf(aa, bb, cc),
            type = PaintObjType.TRIANGLE,
            pars = pars?.toMutableMap()
        )
    }

    fun createTriangle(a: Dot, radius: Float, color: RGBA? = null, pars:Map<String, Any>? = null) : PaintObj {
        val aa = Dot(
            a.x - radius,
            a.y - radius,
            a.z+0,
            color ?: a.color,
            0f,
            0f
        )
        val bb = Dot(
            a.x + radius,
            a.y - radius,
            a.z+0,
            color ?: a.color,
            0f,
            1f
        )
        val cc = Dot(
            a.x+0,
            a.y + radius,
            a.z+0,
            color ?: a.color,
            1f,
            0f
        )

        return PaintObj(
            dots = arrayListOf(aa, bb, cc),
            type = PaintObjType.TRIANGLE,
            pars = pars?.toMutableMap()
        )
    }

    fun createTriangle(position: Dot? = null, color: RGBA? = null, pars:Map<String, Any>? = null) : PaintObj {
        return PaintObj(
            dots = arrayListOf(
                Dot(-0.5f, -0.25f, 0.0f, color, 0f, 0f),
                Dot(0.5f, -0.25f, 0.0f, color, 0f, 1f),
                Dot(0.0f, 0.559f, 0.0f, color, 1f, 0f)
            ),
            type = PaintObjType.TRIANGLE,
            pars = pars?.toMutableMap()
        ).translate(position)
    }

    fun createSquare(a: Dot, radius: Float, position: Dot? = null, color: RGBA? = null, pars:Map<String, Any>? = null) : PaintObj {
        return PaintObj(
            dots = arrayOf(
                Dot(-radius, -radius, 0.0f, color, 0f, 0f),
                Dot(-radius, radius, 0.0f, color, 0f, 1f),
                Dot(radius, -radius, 0.0f, color, 1f, 0f),
                Dot(radius, radius, 0.0f, color, 1f, 1f)
            ).map {
                it.x += a.x
                it.y += a.y
                it.z += a.z
                return@map it
            }.toMutableList(),
            type = PaintObjType.SQUARE,
            pars = pars?.toMutableMap()
        ).translate(position)
    }

    fun createSquare(radius: Float, z: Float = 0f, position: Dot? = null, color: RGBA? = null, pars:Map<String, Any>? = null) : PaintObj {
        return PaintObj(
            dots = arrayListOf(
                Dot(-radius, -radius, z, color, 0f, 0f),
                Dot(-radius, radius, z, color, 0f, 1f),
                Dot(radius, -radius, z, color, 1f, 0f),
                Dot(radius, radius, z, color, 1f, 1f)
            ),
            type = PaintObjType.SQUARE,
            pars = pars?.toMutableMap()
        ).translate(position)
    }
}