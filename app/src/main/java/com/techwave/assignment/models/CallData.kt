package com.techwave.assignment.models

class CallData {
    var num = ""
    var type = ""
    var ts: Long = 0
    var dur: Long = 0

    constructor() {}
    constructor(num1: String, type1: String, ts1: Long, dur1: Long) {
        num = num1
        type = type1
        ts = ts1
        dur = dur1
    }
}