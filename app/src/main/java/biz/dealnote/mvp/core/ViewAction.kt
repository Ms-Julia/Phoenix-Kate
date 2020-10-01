package biz.dealnote.mvp.core

interface ViewAction<V> {
    fun call(view: V)
}