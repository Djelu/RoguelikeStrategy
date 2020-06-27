package com.fantasy.rogueStrategy

object Game {
    var world: MutableMap<String,PaintObj?> = mutableMapOf(
        "tri" to null
    )

    fun getAllVerticesData(): FloatArray{
        val allData = mutableListOf<Float>()
        world.forEach{
            it.value?.getAllDots()!!.forEach { dot ->
                allData.addAll(dot.getVerticesData())
            }
        }
        return allData.toFloatArray()
    }

    fun drawAllObjects(){
        world.forEach{
            it.value?.drawIt()
        }
    }
}