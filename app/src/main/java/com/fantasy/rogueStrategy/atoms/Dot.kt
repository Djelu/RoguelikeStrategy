package com.fantasy.rogueStrategy.atoms

class Dot(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var z: Float = 0.0f,

    var color: RGBA? = null,

    var t1: Float = 0.0f,
    var t2: Float = 0.0f
) {
    init {
        this.color = color ?: RGBA(1f, 1f, 1f, 1f)
    }

    val size = 10

    override fun equals(other: Any?): Boolean {
        if(other !is Dot) return false

        val o = other as Dot

        return  this.x==o.x &&
                this.y==o.y &&
                this.z==o.z
    }

    override fun hashCode(): Int {
        return "${this.x}${this.y}${this.z}".hashCode()
    }

    operator fun plus(dot: Dot): Dot {
        return Dot(this.x+dot.x, this.y+dot.y, this.z+dot.z)
    }

    fun getVerticesData() : ArrayList<Float>{
        val color = color?: RGBA(1f, 1f, 1f, 1f)
        return arrayListOf(
            x, y, z,
            color.r, color.g, color.b, color.a,
            t1, t2
        )
    }

}