package com.mozhimen.taskk.provider.basic.impls

import com.mozhimen.taskk.provider.basic.cons.intErrorCode2strError


/**
 * @ClassName AppDownloadException
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/7 15:10
 * @Version 1.0
 */
fun Int.intErrorCode2taskException(): TaskException =
    TaskException(this)

fun Int.intErrorCode2taskException(msg: String): TaskException =
    TaskException(this, msg)

class TaskException : Exception {
    constructor(e: Exception) : this(e.hashCode(), e.message ?: "")
    constructor(code: Int) : this(code, code.intErrorCode2strError())
    constructor(code: Int, msg: String) : super() {
        _code = code
        _msg = msg
    }

    private var _code: Int
    val code get() = _code
    private var _msg: String
    val msg get() = _msg

    override fun toString(): String {
        return "TaskException(code=$_code, msg='$_msg')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TaskException) return false

        if (_code != other._code) return false
        return _msg == other._msg
    }

    override fun hashCode(): Int {
        var result = _code
        result = 31 * result + _msg.hashCode()
        return result
    }
}