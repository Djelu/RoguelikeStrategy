package com.fantasy.rogueStrategy

class Dot(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var z: Float = 0.0f,

    var color: RGBA = RGBA()
) {
    constructor(arr: FloatArray) : this() {
        x = arr[0]; y = arr[1]; z = arr[2]
        color = RGBA(arr[3], arr[4], arr[5], arr[6])
    }

    constructor(arr: FloatArray, color: RGBA) : this() {
        x = arr[0]; y = arr[1]; z = arr[2]
        this.color = color
    }
}