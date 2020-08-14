package com.jay.base_component.base.mvp

import com.jay.base_component.network.bean.wan.BaseResponse
import com.jay.base_component.network.default_net.DefaultNetFactory
import com.jay.base_component.network.default_net.WanDroidNetwork
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.lang.ref.WeakReference

open class BasePresenter<V : IView> : IPresenter<V> {

    private lateinit var viewReference: WeakReference<V>
    private var disposable: CompositeDisposable = CompositeDisposable()

    fun <T> addSubscribe(observable: Observable<BaseResponse<T>>, baseObserver: BaseResponseObserver<T>) {
        val observer =
            observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(baseObserver)
        disposable.add(observer)
    }

    fun <T> addSubscribe(observable: Observable<T>, baseObserver: BaseObserver<T>) {
        val observer =
            observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(baseObserver)
        disposable.add(observer)
    }

    fun <T> addSubscribe(observer: DisposableObserver<T>) {
        disposable.add(observer)
    }

    fun unsubscribe() {
        disposable.dispose()
    }

    fun getDefaultNet(): WanDroidNetwork {
        return DefaultNetFactory.getDefaultNet()
    }

    override fun attachView(view: V) {
        viewReference = WeakReference(view)
    }

    override fun detachView() {
        viewReference.clear()
    }

    override fun isViewAttached(): Boolean {
        return viewReference.get() != null
    }

    override fun getView(): V? {
        return viewReference.get()
    }

}