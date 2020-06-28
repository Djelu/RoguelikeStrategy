package com.fantasy.rogueStrategy

class RGBA(
    var r: Float = 0.0f,
    var g: Float = 0.0f,
    var b: Float = 0.0f,
    var a: Float = 1.0f
) {
    constructor(rgb: String, a: Int) : this() {
        this.r = normalize(rgb.substring(0,2).toInt(16))
        this.g = normalize(rgb.substring(2,4).toInt(16))
        this.b = normalize(rgb.substring(4,6).toInt(16))
        this.a = normalize(a)
    }

    constructor(r: Int, g: Int, b: Int, a: Int) : this() {
        this.r = normalize(r)
        this.g = normalize(g)
        this.b = normalize(b)
        this.a = normalize(a)
    }

    constructor(r: Int, g: Int, b: Int) : this() {
        this.r = normalize(r)
        this.g = normalize(g)
        this.b = normalize(b)
        this.a = 1f
    }

    private fun normalize(num: Int): Float {
        return if(num!=0) (255/num).toFloat() else 0f
    }
}