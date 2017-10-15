package com.pedromassango.tap.model

/**
 * Created by Pedro Massango on 07/06/2017 at 19:31.
 */

class TapButton() {
    var id: Int = 0
    var name: String? = null
    var color: Int = 0


    constructor(id: Int,
                name: String, color: Int):this() {
        this.id = id

        this.name = name
        this.color = color
    }
}
