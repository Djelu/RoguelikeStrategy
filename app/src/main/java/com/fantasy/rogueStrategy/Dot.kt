package com.fantasy.rogueStrategy

class Dot(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var z: Float = 0.0f,

    var color: RGBA? = null
) {
    val size = if(color == null) 3 else 7

    constructor(arr: FloatArray) : this() {
        x = arr[0]; y = arr[1]; z = arr[2]
        color = RGBA(arr[3], arr[4], arr[5], arr[6])
    }

    constructor(arr: FloatArray, color: RGBA) : this() {
        x = arr[0]; y = arr[1]; z = arr[2]
        this.color = color
    }

    fun getVerticesData() : ArrayList<Float>{
        val color = color?: RGBA(1f,1f,1f)
        return arrayListOf(
            x, y, z,
            color.r, color.g, color.b, color.a
        )
    }
}